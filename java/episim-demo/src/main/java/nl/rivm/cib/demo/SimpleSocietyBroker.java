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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
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
import io.coala.math.Range;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.util.InputStreamConverter;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.Cultural.Culture;
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
import nl.rivm.cib.json.DuoPrimarySchool;
import nl.rivm.cib.json.DuoPrimarySchool.ExportCol;
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

		@Key( "primary-school-densities" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM + "gm_pc4_po_pupils.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream duoPrimarySchoolData();
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

	@Inject
	private ProbabilityDistribution.Factory distFactory;

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

	/** */
	private Table<SocietyTuple> societies;
	/** */
	private Table<PersonTuple> persons;
	/** */
	private Table<SiteTuple> sites;
	/** */
	private IndexPartition capacityIndex;
	/** */
//	private transient ProbabilityDistribution<Boolean> schoolAssortativity;
	/** */
	private int maxKm = 100; // TODO from config
	/** */
	private NavigableMap<String, SocialGatherer> gatherers;
	/** */
	private final Map<Object, List<Object>> societyMembers = new HashMap<>();
	/** */
	private final Map<Object, Object[]> ppSocieties = new HashMap<>();
	/** */
	private final Map<String, EnumMap<ExportCol, JsonNode>> schoolCache = new HashMap<>();
	/** */
	private ConditionalDistribution<String, Object[]> primarySchoolDist;

	/**
	 *
	 */
	@Override
	public SimpleSocietyBroker reset() throws Exception
	{
		final JsonNode gathererConfig = this.config.toJSON( "motors" );
		if( gathererConfig.size() == 0 )
			Thrower.throwNew( IllegalStateException::new,
					() -> "No gatherers configured: " + gathererConfig );

		this.sites = this.data.getTable( SiteTuple.class );
		this.societies = this.data.getTable( SocietyTuple.class );
		this.persons = this.data.getTable( PersonTuple.class );
		this.persons.onCreate( pp -> // pass to new event when home site is set
		atOnce( t -> join( pp ) ), scheduler()::fail );
		this.persons.onDelete( this::abandon, scheduler()::fail );

		this.capacityIndex = new IndexPartition( this.societies,
				scheduler()::fail );
		this.capacityIndex.groupBy( Societies.Purpose.class );
		this.capacityIndex.groupBy( Societies.MemberCapacity.class,
				Stream.of( 1 ) ); // separate 'full' < 1 <= 'available'
//		this.capacityIndex.groupBy( Societies.CultureRef.class );

		// TODO auto-assign cultures to households

//		this.schoolAssortativity = this.config
//				.hesitancySchoolAssortativity( this.distParser );

		final Class<? extends SocialGatherer.Factory> factory = this.config
				.socialGathererFactory();
		LOG.trace( "{} creating gatherers: {}", factory, gathererConfig );
		this.gatherers = this.binder.inject( factory )
				.createAll( gathererConfig );

		try( final InputStream is = this.config.duoPrimarySchoolData() )
		{
			final Map<String, Map<Culture, ProbabilityDistribution<String>>> dists = DuoPrimarySchool
					.parse( is, this.distFactory, ( id, arr ) ->
					{
						this.schoolCache.computeIfAbsent( id, k -> arr );
						return Culture.resolvePO( arr );
					} );

			this.primarySchoolDist = ConditionalDistribution.of( params -> dists
					.computeIfAbsent( params[0].toString(),
							zip -> Collections.emptyMap() )
					.computeIfAbsent( (Culture) params[1], cat -> dists
							.get( params[0] ).get( Culture.OTHERS ) ) );
		}

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	/**
	 * @param pp
	 */
	void join( final PersonTuple pp )
	{
		final Map<String, Object> roleMemberships = this.gatherers.entrySet()
				.stream().filter(
						e -> e.getValue().memberAges().contains( ageOf( pp ) ) )
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

	/**
	 * @param pp
	 */
	void abandon( final PersonTuple pp )
	{
		final Object[] socKeys = this.ppSocieties.remove( pp.key() );
		if( socKeys != null ) Arrays.stream( socKeys )
				.forEach( socKey -> this.societyMembers.compute( socKey,
						( k, members ) -> members != null
								&& members.remove( pp.key() )
								&& members.isEmpty() ? null : members ) );
	}

	/**
	 * @param siteKey
	 * @param dt
	 * @param participants
	 * @param onAdjourn
	 */
	void convene( final Object siteKey, final Quantity<Time> dt,
		final List<Object> participants, final Runnable onAdjourn )
	{
		this.events.onNext( new GatherFact().withSite( siteKey )
				.withDuration( dt ).withParticipants( participants ) );
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	double euclideanDistance( final double[] x, final double[] y )
	{
		return Math.sqrt( IntStream.range( 0, x.length )
				.mapToDouble( i -> x[i] - y[i] ).map( dx -> dx * dx ).sum() );
	}

	double[] positionOf( Object siteRef )
	{
		final SiteTuple site = this.sites.get( siteRef );
		return new double[] { site.get( Sites.Latitude.class ),
				site.get( Sites.Longitude.class ) };
	}

	private Range<ComparableQuantity<Time>> poAges = Range.of( 4.0, 12.5 )
			.map( v -> QuantityUtil.valueOf( v, TimeUnits.YEAR ) );

	/**
	 * @param gatherer
	 * @param person
	 * @return
	 */
	SocietyTuple findSociety( final SocialGatherer gatherer,
		final PersonTuple person )
	{
		final ComparableQuantity<Time> age = ageOf( person );
		if( gatherer.id().equals( "career" ) && this.poAges.contains( age ) )
		{
			final SiteTuple homeSite = this.sites
					.select( person.get( Persons.HomeSiteRef.class ) );
			final String homeZip = homeSite.get( Sites.SiteName.class )
					.substring( 5, 9 );
			final Culture cult = Culture.OTHERS; // TODO from hh
			String schoolName = this.primarySchoolDist.draw( homeZip, cult );
			if( schoolName == null )
				LOG.debug( "No school dist for {} x {}", homeZip, cult );
//			else
//				LOG.debug( "Setting school for {}, age {}: {} x {} -> {}",
//						person, QuantityUtil.pretty( age, TimeUnits.YEAR, 1 ),
//						homeZip, cult,
//						this.schoolCache.get( schoolName ).values() );
		}

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
		final double[] origin = positionOf( homeSiteRef );
		double deg, degNearest = Double.MAX_VALUE;
		for( int i = 0; i < socKeys.size(); i++ )
		{
			deg = euclideanDistance( origin,
					positionOf( this.societies.select( socKeys.get( i ) )
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