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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.IndexPartition;
import io.coala.data.Table;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.QuantityUtil;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Expectation;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Sites;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import tec.uom.se.ComparableQuantity;

/**
 * {@link SimpleSiteBroker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class SimpleSiteBroker implements SiteBroker
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimpleSiteBroker.class );

	public interface SiteConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

		double beta();

	}

	@InjectConfig
	private SimpleSiteBroker.SiteConfig config;

	@Inject
	private Scheduler scheduler;

	@Inject
	private DataLayer data;

	@Inject
	private ProbabilityDistribution.Factory distFactory;

	private final PublishSubject<EpidemicFact> events = PublishSubject.create();

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + JsonUtil.stringify( this );
	}

	Pretty prettySIR()
	{
		return Pretty.of( () -> "" );
	}

	Pretty prettySIR( final Object siteRef )
	{
		return Pretty.of( () -> "" );
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

	private final Map<Object, Expectation> pending = new HashMap<>();
	private Table<Persons.PersonTuple> persons;
	private IndexPartition sirIndex;
//	private Table<Regions.RegionTuple> regions;
	private Table<Sites.SiteTuple> sites;
	/** east-west, longitude, meridian */
//	private IndexPartition ewMeridian;
	/** north-south, latitude, parallel */
//	private IndexPartition nsParallel;
	private final Map<String, NavigableSet<Comparable<?>>> regionSites = new HashMap<>();
	private ConditionalDistribution<Object, String> homeSiteDist;

	private double gamma_inv, beta;
	private QuantityDistribution<Time> recoveryPeriodDist;

	@Override
	public SimpleSiteBroker reset() throws Exception
	{
		/** TODO from config */
		final double reproductionDays = 12d, recoveryDays = 14d;
		this.gamma_inv = recoveryDays;
		this.beta = reproductionDays / recoveryDays * 100;
		// apply subpop(i) scaling: Prod_i(N/size_i)^(time_i)/T = (1000/1000)^(.25) * (1000/2)^(.75)
		this.sites = this.data.getTable( SiteTuple.class );
		this.persons = this.data.getTable( PersonTuple.class );

		this.sirIndex = new IndexPartition( this.persons, scheduler()::fail );
		this.sirIndex.groupBy( Persons.EpiCompartment.class );
		this.sirIndex.groupBy( Persons.SiteRef.class );
//		this.sirIndex.groupBy( Persons.EpiResistance.class, Stream.of( 0d ) );

//		this.ewMeridian = new IndexPartition( this.sites, scheduler()::fail );
//		this.ewMeridian.groupBy( Sites.Purpose.class );
//		this.ewMeridian.groupBy( Sites.Longitude.class,
//				Stream.of( 4d, 5d, 6d ) );
//		this.ewMeridian.groupBy( Sites.Capacity.class, Stream.of( 1 ) );

//		this.nsParallel = new IndexPartition( this.sites, scheduler()::fail );
//		this.nsParallel.groupBy( Sites.Purpose.class );
//		this.nsParallel.groupBy( Sites.Latitude.class,
//				Stream.of( 51d, 52d, 53d, 54d ) );
//		this.nsParallel.groupBy( Sites.Capacity.class, Stream.of( 1 ) );

		this.recoveryPeriodDist = this.distFactory
				.createExponential( this.gamma_inv )
				.toQuantities( TimeUnits.DAYS );
//		this.regions = this.data.getTable( RegionTuple.class );
		final ProbabilityDistribution<Double> latDist = this.distFactory
				.createUniformContinuous( 50.5, 54.5 );
		final ProbabilityDistribution<Double> lonDist = this.distFactory
				.createUniformContinuous( 3.5, 6.5 );
		this.homeSiteDist = regName ->
		{
			// TODO get lat,long,zip from CBS distribution
			final SiteTuple site = this.sites.insertValues(
					map -> map.put( Sites.RegionRef.class, regName )
							.put( Sites.Purpose.class, HOME_FUNCTION )
							.put( Sites.Latitude.class, latDist.draw() )
							.put( Sites.Longitude.class, lonDist.draw() )
							.put( Sites.Capacity.class, 10 ) );
			this.regionSites.computeIfAbsent( regName, k -> new TreeSet<>() )
					.add( (Comparable<?>) site.key() );
			return site.key();
		};
		return this;
	}

	private static final String HOME_FUNCTION = "home";

	@Override
	public double[] positionOf( Object siteRef )
	{
		final SiteTuple site = this.sites.get( siteRef );
		return new double[] { site.get( Sites.Latitude.class ),
				site.get( Sites.Longitude.class ) };
	}

	@Override
	public Object findHome( final String regionRef )
	{
		return this.homeSiteDist.draw( regionRef );
	}

	private final AtomicLong siteSeq = new AtomicLong();

	@Override
	public SiteTuple findNearby( final String lifeRole,
		final Object originSiteRef )
	{
		final SiteTuple origin = this.sites
				.select( Objects.requireNonNull( originSiteRef, "homeless?" ) );
		final Double lat = origin.get( Sites.Latitude.class ),
				lon = origin.get( Sites.Longitude.class );
		final Object regRef = origin.get( Sites.RegionRef.class );
		// TODO use actual locations/regions
//		LOG.warn( "TODO find {} site near {}", lifeRole, origin );
		return this.sites
				.insertValues( map -> map.put( Sites.RegionRef.class, regRef )
						.put( Sites.SiteName.class,
								regRef + "/" + this.siteSeq.incrementAndGet() )
						.put( Sites.Purpose.class, lifeRole )
						.put( Sites.Latitude.class, lat )
						.put( Sites.Longitude.class, lon )
						.put( Sites.Capacity.class, 100 ) ); // TODO
	}

	List<Object> infectives( final SiteTuple site )
	{
		return this.sirIndex.keys( MSEIRS.Compartment.INFECTIVE,
				(Comparable<?>) site.key() );
	}

	List<Object> susceptibles( final SiteTuple site )
	{
		return this.sirIndex.keys( MSEIRS.Compartment.SUSCEPTIBLE,
				(Comparable<?>) site.key() );
	}

//	boolean noPressure( final SiteTuple site )
//	{
//		return infectives( site ).isEmpty() || susceptibles( site ).isEmpty();
//	}

	void arrive( final PersonTuple pp, final SiteTuple site )
	{
//		pp.updateAndGet( Persons.SiteRef.class,
//				ref -> (Comparable<?>) site.key() );
//		site.updateAndGet( Sites.Occupancy.class,
//				occ -> occ == null ? 1 : 1 + occ );
	}

	void depart( final PersonTuple pp, final SiteTuple site )
	{
//		pp.updateAndGet( Persons.SiteRef.class,
//				ref -> pp.get( Persons.HomeSiteRef.class ) ); // return to home
//		site.updateAndGet( Sites.Occupancy.class, occ -> occ - 1 );
	}

	Double resistance( final Object ppKey )
	{
		return this.persons.selectValue( ppKey, Persons.EpiResistance.class );
	}

	private void completeTransition( final Object ppKey,
		final MSEIRS.Transition sir )
	{
		final PersonTuple pp = this.persons.get( ppKey );
		final MSEIRS.Compartment old = pp.getAndUpdate(
				Persons.EpiCompartment.class, prev -> sir.outcome() );
		this.events.onNext( //
				new EpidemicFact().withSite( pp.get( Persons.SiteRef.class ) )
						.withTransition( sir ).withSIRDelta( map -> map
								.put( old, -1 ).put( sir.outcome(), 1 ) ) );
	}

	@Override
	public void convene( final Object siteKey, final Quantity<Time> dt,
		final Stream<Object> participants, final Runnable onAdjourn )
	{
		final SiteTuple site = this.sites.select( siteKey );
		final List<Object> visitors = participants.map( ppKey ->
		{
			arrive( this.persons.get( ppKey ), site );
			return ppKey;
		} ).collect( Collectors.toList() );

//		LOG.trace( "t={} @{} convened for {}, SIR: {}", prettyDate( now() ),
//				site.get( Sites.SiteName.class ), dt, prettySIR() );

		pressurize( site, QuantityUtil.valueOf( dt ), () ->
		{
			visitors.forEach(
					ppKey -> depart( this.persons.get( ppKey ), site ) );
			onAdjourn.run();
//			LOG.trace( "t={} @{} adjourned, SIR: {}", prettyDate( now() ),
//					site.get( Sites.SiteName.class ), prettySIR() );
		} );
	}

	void reschedule( final SiteTuple site, final Quantity<Time> dt,
		final Runnable event )
	{
		this.pending.computeIfPresent( site.key(), ( k, next ) ->
		{
			if( next != null && next.unwrap() != now() ) next.remove();
			return dt == null ? null : after( dt ).call( t ->
			{
				this.pending.remove( k );
				event.run();
			} );
		} );
	}

	void pressurize( final SiteTuple site,
		final ComparableQuantity<Time> timeRemaining, final Runnable onTimeOut )
	{
		final List<Object> susceptibles = susceptibles( site );
		if( susceptibles.isEmpty() )
		{
			reschedule( site, timeRemaining, onTimeOut );
			return;
		}
		final List<Object> infectives = infectives( site );
		if( infectives.isEmpty() )
		{
			reschedule( site, timeRemaining, onTimeOut );
			return;
		}
		final Object ppKey = Collections.min( susceptibles,
				( l, r ) -> resistance( l ).compareTo( resistance( r ) ) );
		if( ppKey != susceptibles.get( 0 ) )
			LOG.warn( "Not sorted? {}: {}", site, susceptibles );

		final double lowestResistance = resistance( ppKey ),
				localPressure = ((double) infectives.size())
						/ site.get( Sites.Occupancy.class ),
				resistanceGap = lowestResistance
						- site.get( Sites.Pressure.class );
		final ComparableQuantity<Time> latencyTime = QuantityUtil.valueOf(
				resistanceGap / localPressure / this.beta, TimeUnits.DAYS );

		if( latencyTime.compareTo( timeRemaining ) < 0 )
			// schedule local infection
			reschedule( site, latencyTime, () ->
			{
				site.updateAndGet( Sites.Pressure.class,
						prev -> lowestResistance );
				// infect
				completeTransition( ppKey, MSEIRS.Transition.LATENCY );
				// schedule recovery
				after( this.recoveryPeriodDist.draw() )
						.call( t -> completeTransition( ppKey,
								MSEIRS.Transition.INFECTIOUS ) );
				// schedule next infection/adjourn
				pressurize( site, timeRemaining.subtract( latencyTime ),
						onTimeOut );
			} );
		else // infection skips this meeting, update local pressure anyway	
			reschedule( site, latencyTime, () ->
			{
				onTimeOut.run();
				final double days = QuantityUtil
						.decimalValue( timeRemaining, TimeUnits.DAYS )
						.doubleValue();
				site.updateAndGet( Sites.Pressure.class,
						pressure -> pressure + days * this.beta * pressure );
			} );
	}
}