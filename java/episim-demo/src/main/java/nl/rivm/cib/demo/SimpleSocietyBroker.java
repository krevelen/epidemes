/* $Id$
 * 
 * Part of ZonMW project no. 50-53000-98-156
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2016 RIVM National Institute for Health and Environment 
 */
package nl.rivm.cib.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.IndexPartition;
import io.coala.data.Table;
import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.Cultural.GatherFact;
import nl.rivm.cib.demo.DemoModel.Cultural.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Epidemical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.CultureRef;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Sites;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.demo.DemoModel.Societies;
import nl.rivm.cib.demo.DemoModel.Societies.SocietyTuple;
import nl.rivm.cib.episim.model.SocialGatherer;
import tec.uom.se.ComparableQuantity;

/**
 * {@link SimpleSocietyBroker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class SimpleSocietyBroker implements SocietyBroker
{

	public interface SocietyConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

		@Key( "motor-factory" )
		@DefaultValue( "nl.rivm.cib.episim.model.SocialGatherer$Factory$SimpleBinding" )
		Class<? extends SocialGatherer.Factory> socialGathererFactory();

	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimpleSocietyBroker.class );

	@InjectConfig
	private SocietyConfig config;

	@Inject
	private LocalBinder binder;

	@Inject
	private Scheduler scheduler;

	@Inject
	private DataLayer data;

//	@Inject
//	private ProbabilityDistribution.Factory distFactory;
	private int maxKm = 100; // TODO from config

	@Inject
	private SiteBroker siteBroker;

	private final PublishSubject<GatherFact> events = PublishSubject.create();

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + JsonUtil.stringify( this );
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public Observable<? extends GatherFact> events()
	{
		return this.events;
	}

	private Table<SocietyTuple> societies;
	private Table<PersonTuple> persons;
//	private Table<HouseholdTuple> households;

	private IndexPartition capacityIndex;

	private NavigableMap<String, SocialGatherer> gatherers;
	private final Map<Object, List<Object>> societyMembers = new HashMap<>();

	private final Map<Object, Object[]> ppSocieties = new HashMap<>();

	@Override
	public SimpleSocietyBroker reset() throws Exception
	{
		final JsonNode gathererConfig = this.config.toJSON( "motors" );
		if( gathererConfig.size() == 0 )
			Thrower.throwNew( IllegalStateException::new,
					() -> "No gatherers configured: " + gathererConfig );

		this.societies = this.data.getTable( SocietyTuple.class );
		this.persons = this.data.getTable( PersonTuple.class );
		this.persons.onCreate( this::join, scheduler()::fail );
		this.persons.onDelete( this::abandon, scheduler()::fail );

		this.capacityIndex = new IndexPartition( this.societies,
				scheduler()::fail );
		this.capacityIndex.groupBy( Societies.Purpose.class );
		this.capacityIndex.groupBy( Societies.MemberCapacity.class,
				Stream.of( 1 ) ); // separate 'full' < 1 <= 'available'
//		this.capacityIndex.groupBy( Societies.CultureRef.class );

		// TODO auto-assign cultures to households

		final Class<? extends SocialGatherer.Factory> factory = this.config
				.socialGathererFactory();
		LOG.trace( "{} creating gatherers: {}", factory, gathererConfig );
		this.gatherers = this.binder.inject( factory )
				.createAll( gathererConfig );

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	void join( final PersonTuple pp )
	{
		final ComparableQuantity<?> age = QuantityUtil.valueOf(
				now().decimal().subtract( pp.get( Persons.Birth.class ) ),
				scheduler().timeUnit() );
		final Map<String, Object> roleMemberships = this.gatherers.entrySet()
				.stream()
				.filter( e -> e.getValue().memberAges().contains( age ) )
				// TODO schedule membership when age reaches membership range
				.collect( Collectors.toMap( Map.Entry::getKey, e ->
				{
					final SocietyTuple soc = findSociety( e.getValue(), pp );
					final List<Object> members = this.societyMembers
							.get( soc.key() );
					if( members.indexOf( pp.key() ) < 0 )
					{
						members.add( pp.key() );
						soc.updateAndGet( Societies.MemberCapacity.class,
								n -> n - 1 );
					} else
						LOG.warn( "Already member: {} in {}", pp, soc );

					return soc.key();
				} ) );

		// TODO emit join event?

		if( roleMemberships.isEmpty() )
			LOG.debug( "{} is not a member in any society", pp );
		else
			this.ppSocieties.compute( pp.key(),
					( k, socKeys ) -> roleMemberships.values()
							.toArray( new Object[roleMemberships.size()] ) );
	}

	void abandon( final PersonTuple pp )
	{
		final Object[] socKeys = this.ppSocieties.remove( pp.key() );
		if( socKeys != null ) Arrays.stream( socKeys )
				.forEach( socKey -> this.societyMembers.compute( socKey,
						( k, members ) -> members != null
								&& members.remove( pp.key() )
								&& members.isEmpty() ? null : members ) );
	}

	void convene( final Object siteKey, final Quantity<Time> dt,
		final List<Object> participants, final Runnable onAdjourn )
	{
//		final SiteTuple site = this.sites.select( siteKey );
//		final Map<MSEIRS.Compartment, Map<Object, Double>> sir = 

		this.events.onNext( new GatherFact().withSite( siteKey )
				.withDuration( dt ).withParticipants( participants ) );

//		participants.stream().map( this.persons::select ).filter( pp -> pp != null )
//				.forEach( pp -> pp.updateAndGet( Persons.SiteRef.class,
//						v -> (Comparable<?>) siteKey ) );
//
//		after( dt ).call( t ->
//		{
//			participants.stream().map( this.persons::select ).filter( pp -> pp != null )
//					.forEach( pp -> pp.updateAndGet( Persons.SiteRef.class,
//							v -> pp.get( Persons.HomeSiteRef.class ) ) );
//		} );
//				.collect( Collectors.groupingBy(
//						pp -> pp.get( Persons.EpiCompartment.class ), Collectors
//								.toMap( pp -> pp.key(), pp -> 0d ) ) );
//		sir.computeIfPresent( MSEIRS.Compartment.SUSCEPTIBLE, ( k, v ) ->
//		{
//			v.replaceAll( ( key, res ) -> this.persons.selectValue( key,
//					Persons.EpiResistance.class ) );
//			return v;
//		} );
//
//		pressurize( site, sir, 0d, QuantityUtil.valueOf( dt ), () ->
//		{
//			sir.values().stream().flatMap( l -> l.keySet().stream() ).allMatch(
//					pp -> depart( this.persons.select( pp ), site ) );
//
//			onAdjourn.run();
//		} );
	}

	double euclideanDistance( final double[] x, final double[] y )
	{
		return Math.sqrt( IntStream.range( 0, x.length )
				.mapToDouble( i -> x[i] - y[i] ).map( dx -> dx * dx ).sum() );
	}

	SocietyTuple findSociety( final SocialGatherer gatherer,
		final PersonTuple person )
	{
		final List<?> socKeys = this.capacityIndex.keys( gatherer.id(), 1
//				,person.get( Persons.CultureRef.class ) 
		);
//		LOG.trace( "find soc {} x >={} : {} <- {}", gatherer.id(), 1, socKeys,
//				this.capacityIndex );
		final Object homeSiteRef = person.get( Persons.HomeSiteRef.class );
		if( socKeys.isEmpty() )
		{
			final SocietyTuple result = createSociety( gatherer, homeSiteRef,
					person.get( CultureRef.class ) );
//			LOG.debug( "Created first {} society: {}, index: {}", gatherer.id(),
//					result, this.capacityIndex );
			return result;
		}
		final double[] origin = this.siteBroker.positionOf( homeSiteRef );
		double deg, degNearest = Double.MAX_VALUE;
		for( int i = 0; i < socKeys.size(); i++ )
		{
			deg = euclideanDistance( origin,
					this.siteBroker.positionOf(
							this.societies.select( socKeys.get( i ) )
									.get( Societies.SiteRef.class ) ) );
			if( deg < degNearest ) degNearest = deg;
		}
		final double kmNearest = degNearest * 111;
		if( kmNearest > this.maxKm )
		{
//			LOG.debug( "Nearest of {} societies ({}) too far at ~{}km from {}",
//					gatherer.id(), socKeys.size(), kmNearest, origin );
			return createSociety( gatherer, homeSiteRef,
					person.get( CultureRef.class ) );
		}

		return this.societies.select( socKeys.get( 0 ) );
	}

	SocietyTuple createSociety( final SocialGatherer gatherer,
		final Object nearHomeRef, final Object cultureRef )
	{
		final SiteTuple site = this.siteBroker.findNearby( gatherer.id(),
				nearHomeRef );
		final String name = gatherer.id() + "@"
				+ site.get( Sites.SiteName.class );
		final Long capacity = gatherer.sizeLimitDist().draw();
		final SocietyTuple soc = this.societies.insertValues( map -> map
				.put( Societies.CultureRef.class, cultureRef )
				.put( Societies.MemberCapacity.class, capacity.intValue() )
				.put( Societies.Purpose.class, gatherer.id() )
				.put( Societies.SiteRef.class, site.key() )
				.put( Societies.SocietyName.class, name ) );

		final List<Object> members = new ArrayList<>();
		this.societyMembers
				//.computeIfAbsent( gatherer.id(), k -> new HashMap<>() )
				.put( soc.key(), members );
//		LOG.trace( "members: {}", this.roleSocietyMembers );

		// initiate gatherings
		gatherer.summon().subscribe( dt ->
		{
			convene( site.key(), dt, members, () ->
			{
				LOG.trace( "Adjourned {}", name );
				// TODO fire event: convene/st, members returned home
			} );
//			LOG.trace( "Gathered {} members: {} (cap: {})", name,
//					members.size(), site.get( Sites.Capacity.class ) );
			// TODO fire event: convene/rq
		}, scheduler()::fail );
		return soc;
	}
}