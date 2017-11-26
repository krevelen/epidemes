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

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.ProbabilityDistribution.Parser;
import io.coala.random.QuantityDistribution;
import io.coala.time.Duration;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.Cultural.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;
import tec.uom.se.ComparableQuantity;

/**
 * {@link SimpleHealthBroker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class SimpleHealthBroker implements HealthBroker
{

	public interface HealthConfig extends YamlConfig
	{

		@Key( "invitation-age" )
		@DefaultValue( "[.5 yr; 4 yr)" )
		String vaccinationInvitationAge();

		@SuppressWarnings( "unchecked" )
		default Range<ComparableQuantity<Time>> vaccinationAgeRange()
			throws ParseException
		{
			return Range
					.parse( vaccinationInvitationAge(), QuantityUtil::valueOf )
					.map( v -> v.asType( Time.class ) );
		}

		@Key( "occasion-recurrence" )
		@DefaultValue( "0 0 0 7 * ? *" )
		String vaccinationRecurrence();

		default Iterable<Instant> vaccinationRecurrence(
			final Scheduler scheduler ) throws ParseException
		{
			return Timing.of( vaccinationRecurrence() ).iterate( scheduler );
		}

		/** @see VaxOccasion#utility() */
		@Key( "occasion-utility-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationUtilityDist();

		default ProbabilityDistribution<Number> vaccinationUtilityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationUtilityDist() );
		}

		/** @see VaxOccasion#proximity() */
		@Key( "occasion-proximity-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationProximityDist();

		default ProbabilityDistribution<Number> vaccinationProximityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationProximityDist() );
		}

		/** @see VaxOccasion#clarity() */
		@Key( "occasion-clarity-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationClarityDist();

		default ProbabilityDistribution<Number> vaccinationClarityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationClarityDist() );
		}

		/** @see VaxOccasion#affinity() */
		@Key( "occasion-affinity-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationAffinityDist();

		default ProbabilityDistribution<Number> vaccinationAffinityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationAffinityDist() );
		}
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimpleHealthBroker.class );

	@InjectConfig
	private HealthConfig config;

	@Inject
	private Scheduler scheduler;

	@Inject
	private DataLayer data;

	@Inject
	private ProbabilityDistribution.Factory distFactory;

	@Inject
	private SocietyBroker societyBroker;

	private final PublishSubject<EpidemicFact> events = PublishSubject.create();

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

	private Table<Persons.PersonTuple> persons;

	private ConditionalDistribution<MSEIRS.Compartment, RegionPeriod> sirStatusDist;

	private ProbabilityDistribution<Double> resistanceDist;

	private double gamma_inv, beta;

	private QuantityDistribution<Time> recoveryPeriodDist;

	private final Map<Object, LocalPressure> homePressure = new HashMap<>();

	@Override
	public SimpleHealthBroker reset() throws Exception
	{

		/** TODO from config */
		// apply subpop(i) scaling: Prod_i(N/size_i)^(time_i)/T = (1000/1000)^(.25) * (1000/2)^(.75)
		final double reproductionDays = 12d, recoveryDays = 14d, vaxDegree = .5,
				betaFactor = 100;
		this.gamma_inv = recoveryDays;
		this.beta = reproductionDays / recoveryDays * betaFactor;

//		final LocalDate lastOutbreak = LocalDate.of( 2006, 6, 30 );
		final Map<RegionPeriod, ProbabilityDistribution<Boolean>> vaxDegreeDist = new HashMap<>();
		this.sirStatusDist = regPer ->
//		regPer.periodRef().isBefore( lastOutbreak ) ? MSEIRS.Compartment.RECOVERED :
		vaxDegreeDist
				.computeIfAbsent( regPer,
						k -> this.distFactory.createBernoulli( vaxDegree ) )
				.draw() ? MSEIRS.Compartment.VACCINATED
						: MSEIRS.Compartment.SUSCEPTIBLE;
		this.resistanceDist = this.distFactory.createExponential( 1 );
		this.recoveryPeriodDist = this.distFactory
				.createExponential( this.gamma_inv )
				.toQuantities( TimeUnits.DAYS );

		this.persons = this.data.getTable( PersonTuple.class );
		this.persons.changes().subscribe( chg ->
		{
			if( chg.crud() == Table.Operation.CREATE )
				onCreate( (PersonTuple) chg.newValue() );
			else if( chg.crud() == Table.Operation.DELETE )
				onDelete( (PersonTuple) chg.oldValue() );
			else if( chg.crud() == Table.Operation.UPDATE )
			{
//				if( chg.changedType() == Persons.SiteRef.class )
//					onMove( chg.sourceRef(), chg.oldValue(), chg.newValue() );
//				else 
				if( chg.changedType() == Persons.EpiCompartment.class )
				{
					final PersonTuple pp = this.persons
							.select( chg.sourceRef() );
//					this.events.onNext( //
//							new EpidemicFact()
//									.withSite( pp.get( Persons.SiteRef.class ) )
//									.withSIRDelta( map -> map
//											.put( (MSEIRS.Compartment) chg
//													.oldValue(), -1 )
//											.put( (MSEIRS.Compartment) chg
//													.newValue(), 1 ) ) );
					if( chg.newValue() == MSEIRS.Compartment.INFECTIVE )
						// schedule recovery after infectious period 
						after( this.recoveryPeriodDist.draw() ).call( t_r -> pp
								.getAndUpdate( Persons.EpiCompartment.class,
										prev -> prev == MSEIRS.Compartment.INFECTIVE
												? MSEIRS.Compartment.RECOVERED
												: prev ) );
				}
			}
		} );

		this.societyBroker.events().subscribe( e ->
		{
			final LocalPressure lp = getLP( e.siteRef );

			final Map<Object, Set<Object>> homeConveners = e.participants
					.stream()
					.collect( Collectors.groupingBy(
							ppRef -> this.persons.selectValue( ppRef,
									Persons.HomeSiteRef.class ),
							Collectors.toSet() ) );

//			LOG.debug( "t={} Co-pressurizing @{} for {}: {}",
//					scheduler().nowDT(), e.siteRef, e.duration,
//					e.participants );
			homeConveners.forEach( ( homeRef, departures ) -> getLP( homeRef )
					.depart( departures.stream() ) );
			lp.arrive( e.participants.stream().map( this.persons::select )
					.filter( pp -> pp != null ) );

			after( e.duration ).call( t ->
			{
				lp.depart( e.participants.stream() );
				homeConveners.forEach( ( homeRef, arrivals ) -> getLP( homeRef )
						.arrive( arrivals.stream().map( this.persons::select )
								.filter( pp -> pp != null ) ) );
//				LOG.debug( "t={} Re-pressured @{}: {}", scheduler().nowDT(),
//						lp.resistance, e.participants );
			} );
		}, scheduler()::fail );

		// schedule outbreak start
		atOnce( this::scheduleOutbreakStart );

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	private void scheduleOutbreakStart()
	{
		final LocalDateTime dt = LocalDateTime.of( 2013, 1, 2, 3, 4, 5 );
		LOG.debug( "Scheduling outbreak at {}", dt );
		at( dt ).call( tOutbreak ->
		{
			final PersonTuple minResistant = this.persons.stream()
					.filter( pp -> pp.get( Persons.EpiCompartment.class )
							.isSusceptible() )
					.min( ( l, r ) -> l.get( Persons.EpiResistance.class )
							.compareTo( r.get( Persons.EpiResistance.class ) ) )
					.orElse( null );

			minResistant.set( Persons.EpiCompartment.class,
					MSEIRS.Compartment.INFECTIVE );

			final Disposable trackSub = this.persons
					.changes( minResistant.key() ).subscribe(
							chg -> LOG.warn( "t={} PATIENT ZERO {} change: {}",
									scheduler().nowDT(), minResistant, chg ) );

			final Expectation recovery = after(
					QuantityUtil.valueOf( this.gamma_inv, TimeUnits.DAYS ) )
							//this.recoveryPeriodDist.draw() 
							.call( tRecover ->
							{
								minResistant.set( Persons.EpiCompartment.class,
										MSEIRS.Compartment.RECOVERED );
								LOG.info( "t={} ENDED IMPORT at index case: {}",
										scheduler().nowDT(), minResistant );
								trackSub.dispose(); // stop tracking patient zero
							} );
			LOG.info( "t={} IMPORTED index case: {}, recovery +{}d= @{}",
					scheduler().nowDT(), minResistant, this.gamma_inv,
					recovery.due() );
		} );
	}

	private ComparableQuantity<Time> infectionTimer( final int nI, final int n )
	{
		return QuantityUtil.valueOf( n / nI / this.beta, TimeUnits.DAYS );
	}

	private LocalPressure getLP( final Object siteRef )
	{
		return this.homePressure.computeIfAbsent(
				Objects.requireNonNull( siteRef, "No site ref?" ),
				k -> new LocalPressure( scheduler(), this.persons::select,
						this::infectionTimer ) );
	}

	static class LocalPressure implements Proactive
	{
		final Scheduler scheduler;
		final Map<Object, Double> resistance = new HashMap<>();
		final AtomicReference<Expectation> pending = new AtomicReference<>();
		final Function<Object, PersonTuple> ppGetter;
		final BiFunction<Integer, Integer, ComparableQuantity<Time>> infectionTimer;
		/**
		 * the scaled period for a susceptible to become infectious at current
		 * pressure
		 */
		ComparableQuantity<Time> pressurizedLatency = null;
		Instant pressureStart = null;

		LocalPressure( final Scheduler scheduler,
			final Function<Object, PersonTuple> ppGetter,
			final BiFunction<Integer, Integer, ComparableQuantity<Time>> infectionTimer )
		{
			this.scheduler = scheduler;
			this.ppGetter = ppGetter;
			this.infectionTimer = infectionTimer;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		// TODO from config;
		private static final ComparableQuantity<Time> HORIZON = QuantityUtil
				.valueOf( 3, TimeUnits.DAYS );

		private Object getTargetAndRetally( final AtomicInteger infectives )
		{
			final List<Object> removed = new ArrayList<>();
			infectives.set( 0 );
			final Object result = this.resistance.entrySet().stream()
					.filter( e ->
					{
						// skip if removed
						final PersonTuple pp = this.ppGetter
								.apply( e.getKey() );
						if( pp == null ) return !removed.add( e.getKey() );

						if( e.getValue() > 0 ) return true;
						if( e.getValue() == 0d ) infectives.incrementAndGet();
						return false;
					} )
					.min( ( l, r ) -> l.getValue().compareTo( r.getValue() ) )
					.map( Map.Entry::getKey ).orElse( null );

			// remove the dead
			removed.forEach( this.resistance::remove );
			return result;
		}

		void infect( final Object targetRef, final Double targetRes )
		{
			final PersonTuple target = this.ppGetter.apply( targetRef );
			target.updateAndGet( Persons.EpiCompartment.class,
					oldSIR -> MSEIRS.Compartment.INFECTIVE );
			// update local resistance (if still here)
			this.resistance.replace( targetRef, targetRes, 0d );
			// shift all resistance
			preschedule();
			reschedule();
		}

		void preschedule()
		{
			if( this.pressurizedLatency == null ) return;

			// decrease resistance so far
			final Duration dtPressure = now().subtract( this.pressureStart );
			if( !dtPressure.isZero() )
			{
				// resistance dr = dt / ( n / nI / beta ) = dt * beta * (nI / n)
				final double resDecrease = DecimalUtil
						.divide( dtPressure
								.toQuantity( this.pressurizedLatency.getUnit() )
								.getValue(),
								this.pressurizedLatency.getValue() )
						.doubleValue();
//				LOG.debug( "t={} delta-resistance {} = {} / {}",
//						scheduler().nowDT(), resDecrease, dtPressure,
//						this.pressurizedLatency );
				this.resistance.replaceAll( ( ppRef, ppRes ) -> ppRes > 0
						? ppRes - resDecrease : ppRes );
			}
			// reset pressure calculations
			this.pressurizedLatency = null;
			this.pressureStart = null;
		}

		void reschedule()
		{
			this.pending.updateAndGet( expPrev ->
			{
				if( expPrev != null ) expPrev.remove();
				final AtomicInteger infectivesCount = new AtomicInteger();
				final Object targetRef = getTargetAndRetally( infectivesCount );

				// no-one to give/receive pressure ?
				if( targetRef == null || infectivesCount.get() == 0 )
					return null; // nothing to schedule

				this.pressureStart = now();
				this.pressurizedLatency = this.infectionTimer
						.apply( infectivesCount.get(), this.resistance.size() );
				final Double targetRes = this.resistance.get( targetRef );
				final ComparableQuantity<Time> dt = this.pressurizedLatency
						.multiply( targetRes );
				return dt.compareTo( HORIZON ) > 0 ? null
						: after( dt )
								.call( t_i -> infect( targetRef, targetRes ) );
			} );
		}

		LocalPressure arrive( final PersonTuple pp )
		{
			preschedule();
			doArrive( pp );
			reschedule();
			return this;
		}

		LocalPressure arrive( final Stream<PersonTuple> ppl )
		{
			preschedule();
			ppl.forEach( this::doArrive );
			reschedule();
			return this;
		}

		void doArrive( final PersonTuple pp )
		{
			final MSEIRS.Compartment sir = pp
					.get( Persons.EpiCompartment.class );
			this.resistance.put( pp.key(),
					sir == MSEIRS.Compartment.SUSCEPTIBLE
							? pp.get( Persons.EpiResistance.class )
							: sir == MSEIRS.Compartment.INFECTIVE ? 0d : -1d );
		}

		LocalPressure depart( final PersonTuple pp )
		{
			preschedule();
			this.resistance.remove( pp.key() );
			reschedule();
			return this;
		}

		LocalPressure depart( final Stream<Object> pplRefs )
		{
			preschedule();
			pplRefs.forEach( this.resistance::remove );
			reschedule();
			return this;
		}

//		private final Map<Object, Expectation> pending = new HashMap<>();
//
//		void reschedule( final SiteTuple site, final Quantity<Time> dt,
//			final Runnable event )
//		{
//			this.pending.compute( site.key(), ( k, next ) ->
//			{
//				if( next != null && next.unwrap() != now() ) next.remove();
//				return dt == null ? null : after( dt ).call( t ->
//				{
//					this.pending.remove( k );
//					event.run();
//				} );
//			} );
//		}
//
//		void pressurize( final SiteTuple site,
//			final Map<MSEIRS.Compartment, Map<Object, Double>> sir,
//			final Double pressureResisted,
//			final ComparableQuantity<Time> timeRemaining, final Runnable onTimeOut )
//		{
//			final Map<Object, Double> susceptibles = sir
//					.get( MSEIRS.Compartment.SUSCEPTIBLE ),
//					infectives = sir.get( MSEIRS.Compartment.INFECTIVE );
//			if( susceptibles == null || infectives == null )
//			{
////				reschedule( site, timeRemaining, () -> onTimeOut.run() );
//				return;
//			}
//
//			// remove anyone that has died/emigrated during pressure cycle
//			sir.values().stream().forEach( ppl -> ppl.keySet()
//					.removeIf( key -> this.persons.select( key ) == null ) );
//			if( susceptibles.isEmpty() || infectives.isEmpty() )
//			{
////				reschedule( site, timeRemaining, () -> onTimeOut.run() );
//				return;
//			}
//
//			final int occ = sir.values().stream().mapToInt( Map::size ).sum();
//
//			final Object nextKey = susceptibles.keySet().stream().min( ( l,
//				r ) -> susceptibles.get( l ).compareTo( susceptibles.get( r ) ) )
//					.orElse( null );
//			if( nextKey == null ) Thrower.throwNew( IllegalStateException::new,
//					() -> "Unexpected, no minimum in " + susceptibles );
//			final double localPressure = site.updateAndGet( Sites.Pressure.class,
//					pressure -> ((double) infectives.size()) / occ ),
//					nextResistance = susceptibles.get( nextKey );
//			if( pressureResisted >= nextResistance )
//				Thrower.throwNew( IllegalStateException::new,
//						() -> "Unexpected, no resistance latency "
//								+ pressureResisted + " >= " + nextResistance );
//			final ComparableQuantity<Time> latencyTime = QuantityUtil.valueOf(
//					(nextResistance - pressureResisted) / localPressure / this.beta,
//					TimeUnits.DAYS );
//
//			LOG.trace( "t={} society pressure @{}: {}", dt(),
//					Pretty.of( () -> site.get( Sites.SiteName.class ) ),
//					Pretty.of( () -> sir.entrySet().stream()
//							.collect( Collectors.toMap( Map.Entry::getKey,
//									e -> e.getValue().size() ) ) ) );
//			if( latencyTime.compareTo( timeRemaining ) < 0 )
//			{
//				LOG.trace(
//						"t={}, infection @{} after {}, pressure = {} in {}=sum{}",
//						dt(), Pretty.of( () -> site.get( Sites.SiteName.class ) ),
//						latencyTime.to( TimeUnits.HOURS ), localPressure, occ,
//						sir );
//				reschedule( site, latencyTime, () ->
//				{
//					infect( this.persons.select( nextKey ) );
//					susceptibles.remove( nextKey );
//					infectives.put( nextKey, 0d );
//					// schedule next infection/adjourn
//					pressurize( site, sir, nextResistance,
//							timeRemaining.subtract( latencyTime ), //onTimeOut
//							null );
//				} );
//			} else // infection skips this meeting, update local pressure anyway	
//			{
//				final double totalResisted = pressureResisted
//						+ QuantityUtil.decimalValue( timeRemaining, TimeUnits.DAYS )
//								.doubleValue() * this.beta * localPressure;
//				LOG.trace( "t={}, skip @{} after {}, pressure = {}, delta = -{}",
//						dt(), Pretty.of( () -> site.get( Sites.SiteName.class ) ),
//						timeRemaining.to( TimeUnits.HOURS ), localPressure,
//						totalResisted );
//				reschedule( site, timeRemaining, () ->
//				{
//					susceptibles.keySet().stream().map( this.persons::select )
//							.filter( pp -> pp != null )
//							.forEach( pp -> pp.updateAndGet(
//									Persons.EpiResistance.class,
//									resistance -> resistance - totalResisted ) );
//					site.updateAndGet( Sites.Pressure.class, pressure -> 0d );
////					onTimeOut.run();
//				} );
//			}
//		}
	}

	private final Set<Object> nextCreations = new HashSet<>();
	private final Map<Object, Set<PersonTuple>> nextArrivals = new TreeMap<>();
	private final Map<Object, Set<Object>> nextDepartures = new TreeMap<>();
	private Expectation pendingPressure = null;

	void handleMoves()
	{
		this.nextCreations
				.stream().map(
						this.persons::select )
				.forEach( pp -> this.nextArrivals.computeIfAbsent(
						Objects.requireNonNull(
								pp.get( Persons.HomeSiteRef.class ),
								"Newly created person has no site assigned yet" ),
						k -> new HashSet<>() ).add( pp ) );
		this.nextArrivals.forEach( ( siteRef, arrivals ) -> getLP( siteRef )
				.arrive( arrivals.stream().filter( pp -> pp != null ) ) );
		this.nextDepartures.forEach( ( siteRef, departures ) -> getLP( siteRef )
				.depart( departures.stream() ) );

		LOG.debug( "Aggregated creations x {}, arrivals x {}, departures x {}",
				this.nextCreations.size(), this.nextArrivals.size(),
				this.nextDepartures.size() );
		this.nextCreations.clear();
		this.nextArrivals.clear();
		this.nextDepartures.clear();
//		this.pendingPressure = null;
	}

	void onDelete( final PersonTuple pp )
	{
		final Object siteRef = pp.get( Persons.HomeSiteRef.class );
		getLP( siteRef ).depart( pp );
	}

	void onCreate( final PersonTuple pp )
	{
		final MSEIRS.Compartment status = this.sirStatusDist.draw( RegionPeriod
				.of( (String) pp.get( Persons.HomeRegionRef.class ), dt() ) );
		final double resistance = status == MSEIRS.Compartment.SUSCEPTIBLE
				? this.resistanceDist.draw() : 0d;
		pp.set( Persons.EpiCompartment.class, status );
		pp.set( Persons.EpiResistance.class, resistance );

		if( this.pendingPressure == null )
			this.pendingPressure = atOnce( this::handleMoves );

		this.nextCreations.add( pp.key() );
	}

	void onMove( final Object ppRef, final Object oldSiteRef,
		final Object newSiteRef )
	{
		final PersonTuple pp = this.persons.select( ppRef );
		if( this.pendingPressure == null )
			this.pendingPressure = atOnce( this::handleMoves );

		if( oldSiteRef != null ) this.nextDepartures
				.computeIfAbsent( oldSiteRef, k -> new HashSet<>() )
				.add( pp.key() );
		this.nextArrivals.computeIfAbsent( newSiteRef, k -> new HashSet<>() )
				.add( pp );
	}

//	private boolean completeTransition( final PersonTuple pp,
//		final MSEIRS.Compartment precondition, final MSEIRS.Transition sir )
//	{
//		if( pp == null ) return false;
//		final MSEIRS.Compartment old = pp.getAndUpdate(
//				Persons.EpiCompartment.class,
//				prev -> prev == precondition ? sir.outcome() : prev );
//		if( old != precondition ) // pre-con changed, person removed/replaced
//			return false;
//		return true;
//	}
}
