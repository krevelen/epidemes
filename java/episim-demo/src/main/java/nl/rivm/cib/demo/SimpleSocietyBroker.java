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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
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
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.CultureRef;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Sites;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Societies;
import nl.rivm.cib.demo.DemoModel.Societies.SocietyTuple;
import nl.rivm.cib.episim.model.SocialGatherer;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.unit.Units;

/**
 * {@link SimpleSocietyBroker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class SimpleSocietyBroker implements SocietyBroker
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimpleSocietyBroker.class );

	public interface SocietyConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

		@Key( "motor-factory" )
		@DefaultValue( "nl.rivm.cib.episim.model.SocialGatherer$Factory$SimpleBinding" )
		Class<? extends SocialGatherer.Factory> socialGathererFactory();

	}

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

	private final PublishSubject<EpidemicFact> events = PublishSubject.create();

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
	public Observable<EpidemicFact> events()
	{
		return this.events;
	}

	private Table<SocietyTuple> societies;
	private IndexPartition capacityIndex;

	private NavigableMap<String, SocialGatherer> gatherers;

	@Override
	public SimpleSocietyBroker reset() throws Exception
	{
		final JsonNode gathererConfig = this.config.toJSON( "motors" );
		if( gathererConfig.size() == 0 )
			Thrower.throwNew( IllegalStateException::new,
					() -> "No gatherers configured: " + gathererConfig );

		this.societies = this.data.getTable( SocietyTuple.class );
		this.capacityIndex = new IndexPartition( this.societies,
				scheduler()::fail );
		this.capacityIndex.groupBy( Societies.Purpose.class );
		this.capacityIndex.groupBy( Societies.MemberCapacity.class,
				Stream.of( 1 ) ); // separate 'full' < 1 <= 'available'
//		this.capacityIndex.groupBy( Societies.CultureRef.class );

		final Class<? extends SocialGatherer.Factory> factory = this.config
				.socialGathererFactory();
		LOG.trace( "{} creating gatherers: {}", factory, gathererConfig );
		this.gatherers = this.binder.inject( factory )
				.createAll( gathererConfig );
		return this;
	}

	private final Map<String, Map<Object, List<Object>>> roleSocietyMembers = new HashMap<>();

	@Inject
	private SiteBroker siteBroker;

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
		this.roleSocietyMembers
				.computeIfAbsent( gatherer.id(), k -> new HashMap<>() )
				.put( soc.key(), members );
//		LOG.trace( "members: {}", this.roleSocietyMembers );

		// initiate gatherings
		gatherer.summon().subscribe( dt ->
		{
			this.siteBroker.convene( site.key(), dt, members.stream(), () ->
			{
				LOG.trace( "Adjourned {}", name );
				// TODO fire event: convene/st, members returned home
			} );
//			LOG.trace( "Gathered {} members: {} (cap: {})", name,
//					members.size(), site.get( Sites.Capacity.class ) );
			// TODO fire event: convene/rq
		}, this::logError );
		return soc;
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
		if( socKeys.isEmpty() ) return createSociety( gatherer, homeSiteRef,
				person.get( CultureRef.class ) );
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
		if( kmNearest > 40 )// TODO lower bound from config
//		{
//			LOG.trace( "nearest {} societies ({}) at ~{}km is too far",
//					gatherer.id(), socKeys.size(), kmNearest );
			return createSociety( gatherer, homeSiteRef,
					person.get( CultureRef.class ) );
//		}

		return this.societies.select( socKeys.get( 0 ) ); // TODO pick random?
	}

	@Override
	public Map<String, Object> join( final PersonTuple person )
	{
		final ComparableQuantity<?> age = QuantityUtil.valueOf(
				now().decimal().subtract( person.get( Persons.Birth.class ) ),
				scheduler().timeUnit() );
		return this.gatherers.entrySet().stream().filter( e ->
		{
			if( !e.getValue().memberAges().contains( age ) )
			{
				LOG.warn( "{} not in {} ages {}",
						age.asType( Time.class ).to( Units.YEAR ), e.getKey(),
						e.getValue().memberAges() );
				return false;
			}
			return true;
		} ).collect( Collectors.toMap( Map.Entry::getKey, e ->
		{
			final SocietyTuple soc = findSociety( e.getValue(), person );
//			LOG.trace( "members: {} x '{}' : {}", e.getKey(), soc.key(), this.roleSocietyMembers );
			final List<Object> members = this.roleSocietyMembers
					.get( e.getKey() ).get( soc.key() );
			if( members.indexOf( person.key() ) < 0 )
			{
				members.add( person.key() );
				soc.updateAndGet( Societies.MemberCapacity.class, n -> n - 1 );
				// TODO emit join event?
			} else
				LOG.warn( "Already member: {} in {}", person, soc );

			return soc.key();
		} ) );
	}

}