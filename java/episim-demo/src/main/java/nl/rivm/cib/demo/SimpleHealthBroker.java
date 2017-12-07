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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
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
import io.coala.config.LocalDateConverter;
import io.coala.config.LocalDateTimeConverter;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Duration;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import io.coala.util.Compare;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.Cultural.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Households;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.demo.DemoModel.Medical.VaxAcceptanceEvaluator;
import nl.rivm.cib.demo.DemoModel.Medical.VaxDose;
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
		// apply subpop(i) scaling: Prod_i(N/size_i)^(time_i)/T = (1000/1000)^(.25) * (1000/2)^(.75)
		@Key( "reproduction-period" )
		@DefaultValue( "12 day" )
		String reproductionDays();

		@Key( "recovery-period" )
		@DefaultValue( "14 day" )
		String recoveryPeriod();

		// apply subpop(i) scaling: Prod_i(N/size_i)^(time_i)/T = (1000/1000)^(.25) * (1000/2)^(.75)
		@Key( "beta-factor" )
		@DefaultValue( "100" )
		double betaFactor();

		@Key( "last-outbreak-end-date" )
		@DefaultValue( "2000-06-30" )
		@ConverterClass( LocalDateConverter.class )
		LocalDate recoveredBefore();

		@Key( "next-outbreak-start-date" )
		@DefaultValue( "2013-07-06T05:43:21" )
		@ConverterClass( LocalDateTimeConverter.class )
		LocalDateTime outbreakStart();

		@Key( "vaccination-degree" )
		@DefaultValue( ".9" )
		double overallVaccinationDegree();

		@Key( "acceptance-evaluator-type" )
		@DefaultValue( "nl.rivm.cib.demo.DemoModel$Medical$VaxAcceptanceEvaluator$Average" )
		Class<?> acceptanceEvaluatorType();

		// BMR0-1 (applied 6-12 months old) no invite (eg. holiday, outbreak)
		// BMR1-1 (applied 1-9 years old) invite for 11 months/48 weeks/335 days
		// BMR2-1 (applied 1-9 years old) invite for 14 months/61 weeks/425 days
		// BMR2-2 (applied 9-19 years old) no invite (eg. ZRA)
		// and reminder(s) after +3 months/+13 weeks/+91 days

		/**
		 * (arbitrary) age when advocation reaches the target (appointment is
		 * made)
		 */
		// 48 weeks = 11 months = age of earliest dose (BMR0-1) or 
		// some preceding dose (eg. DKTP-Hib-HepB + Pneu)
//		@Key( "decision-age" )
//		@DefaultValue( "3 week" )
//		String decisionAge();

		@Key( "cohort-birth-resolution" )
		@DefaultValue( "1 week" )
		String cohortBirthResolution();

		// BMR2 reached, mean(sd): 14.2(0.5) months/61.5(1.9) weeks/425(13) days
		/** typical treatment delay relative to the (arbitrary) decision age */
		@Key( "treatment-delay-dist" )
//		@DefaultValue( "normal(58.5 week;1.85 week)" )
		@DefaultValue( "normal(.5;1.8)" )
		String treatmentDelayDist();

		@Key( "occasion-recurrence" )
		@DefaultValue( "0 0 10 ? * MON-FRI *" )
		String occasionRecurrence();

		/** @see VaxOccasion#utility() */
		@Key( "occasion-utility-dist" )
		@DefaultValue( "const(0.5)" )
		String occasionUtilityDist();

		/** @see VaxOccasion#proximity() */
		@Key( "occasion-proximity-dist" )
		@DefaultValue( "const(0.5)" )
		String occasionProximityDist();

		/** @see VaxOccasion#clarity() */
		@Key( "occasion-clarity-dist" )
		@DefaultValue( "const(0.5)" )
		String occasionClarityDist();

		/** @see VaxOccasion#affinity() */
		@Key( "occasion-affinity-dist" )
		@DefaultValue( "const(0.5)" )
		String occasionAffinityDist();
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
	private ProbabilityDistribution.Parser distParser;

	@Inject
	private SocietyBroker societyBroker;

	private final PublishSubject<EpidemicFact> events = PublishSubject.create();

	private final Map<Object, LocalPressure> homePressure = new HashMap<>();

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

	private Table<Households.HouseholdTuple> households;

	private VaxAcceptanceEvaluator vaxAcceptance;

	private ConditionalDistribution<MSEIRS.Compartment, RegionPeriod> sirStatusDist;

	private ProbabilityDistribution<Double> resistanceDist;

	private double gamma_inv, beta;

	private QuantityDistribution<Time> recoveryPeriodDist;

	/** minimum age for vaccination decision making, in scheduler time units */
//	private BigDecimal vaxDecisionAgeMinimum;

	/** age resolution of vaccination cohorts/bins, in scheduler time units */
	private BigDecimal cohortBirthResolution;

	/** */
	private TreeMap<BigDecimal, List<Object>> birthCohorts = new TreeMap<>();
	/** */
	private ConditionalDistribution<VaxOccasion, VaxDose> vaxOccasionDist;
	/** */
	private QuantityDistribution<Time> vaxTreatmentDelay;

	@Override
	public SimpleHealthBroker reset() throws Exception
	{
		// schedule vaccination occasions
		atEach( Timing.of( this.config.occasionRecurrence() )
				.iterate( scheduler() ), this::scheduleVaccinations );

		// schedule outbreak start
		final LocalDateTime dt = this.config.outbreakStart();
		at( dt ).call( this::outbreakStart );
		LOG.info( "Scheduling outbreak at {}", dt );

		final double reproductionDays = QuantityUtil
				.valueOf( this.config.reproductionDays() )
				.to( scheduler().timeUnit() ).getValue().doubleValue();
		final double recoveryDays = QuantityUtil
				.valueOf( this.config.recoveryPeriod() )
				.to( scheduler().timeUnit() ).getValue().doubleValue();
		final double vaxDegree = this.config.overallVaccinationDegree();
		this.gamma_inv = recoveryDays;
		this.beta = reproductionDays / recoveryDays * this.config.betaFactor();
		LOG.info( "beta = {} = reproduction: {} / recovery: {} * factor: {}",
				this.beta, reproductionDays, recoveryDays,
				this.config.betaFactor() );

		final LocalDate lastOutbreak = this.config.recoveredBefore();
		final Map<String, ProbabilityDistribution<Boolean>> localVaxDegreeDist = new HashMap<>();
		this.sirStatusDist = regPer -> regPer.periodRef()
				.isBefore( lastOutbreak )
						? MSEIRS.Compartment.RECOVERED
						: localVaxDegreeDist
								.computeIfAbsent( regPer.regionRef(),
										k -> this.distFactory
												.createBernoulli( vaxDegree ) )
								.draw() ? MSEIRS.Compartment.VACCINATED
										: MSEIRS.Compartment.SUSCEPTIBLE;

		this.vaxTreatmentDelay = this.distParser.parseQuantity(
				this.config.treatmentDelayDist(), TimeUnits.WEEK );

		this.resistanceDist = this.distFactory.createExponential( 1 );
		this.recoveryPeriodDist = this.distFactory
				.createExponential( this.gamma_inv )
				.toQuantities( TimeUnits.DAYS );

		// setup distribution of vaccination occasion convenience factors
		final ProbabilityDistribution<Number> vaccinationUtilityDist = this.distParser
				.parse( this.config.occasionUtilityDist() );
		final ProbabilityDistribution<Number> vaccinationProximityDist = this.distParser
				.parse( this.config.occasionProximityDist() );
		final ProbabilityDistribution<Number> vaccinationClarityDist = this.distParser
				.parse( this.config.occasionClarityDist() );
		final ProbabilityDistribution<Number> vaccinationAffinityDist = this.distParser
				.parse( this.config.occasionAffinityDist() );
		this.vaxOccasionDist = dose -> VaxOccasion.of(
				vaccinationUtilityDist.draw(), vaccinationProximityDist.draw(),
				vaccinationClarityDist.draw(), vaccinationAffinityDist.draw() );

//		this.vaxDecisionAgeMinimum = QuantityUtil
//				.decimalValue( QuantityUtil.valueOf( this.config.decisionAge() )
//						.to( scheduler().timeUnit() ) );
		this.cohortBirthResolution = QuantityUtil.decimalValue(
				QuantityUtil.valueOf( this.config.cohortBirthResolution() )
						.to( scheduler().timeUnit() ) );

		this.households = this.data.getTable( HouseholdTuple.class );
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
				if( chg.changedType() == Persons.PathogenCompartment.class )
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
						after( this.recoveryPeriodDist.draw() )
								.call( t_r -> pp.getAndUpdate(
										Persons.PathogenCompartment.class,
										prev -> prev == MSEIRS.Compartment.INFECTIVE
												? MSEIRS.Compartment.RECOVERED
												: prev ) );
				}
			}
		} );

		// TODO from config
		this.vaxAcceptance = VaxAcceptanceEvaluator.MIN_CONVENIENCE_GE_AVG_ATTITUDE;
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

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	private void outbreakStart( final Instant t )
	{
		final PersonTuple minResistant = this.persons.stream()
				.filter( pp -> pp.get( Persons.PathogenCompartment.class )
						.isSusceptible() )
				.min( ( l, r ) -> l.get( Persons.PathogenResistance.class )
						.compareTo(
								r.get( Persons.PathogenResistance.class ) ) )
				.orElse( null );

		minResistant.set( Persons.PathogenCompartment.class,
				MSEIRS.Compartment.INFECTIVE );

		final Disposable trackSub = this.persons.changes( minResistant.key() )
				.subscribe( chg -> LOG.warn( "t={} PATIENT ZERO {} change: {}",
						scheduler().nowDT(), minResistant, chg ) );

		final Expectation recovery = after(
				QuantityUtil.valueOf( this.gamma_inv, TimeUnits.DAYS ) )
						//this.recoveryPeriodDist.draw() 
						.call( tRecover ->
						{
							minResistant.set( Persons.PathogenCompartment.class,
									MSEIRS.Compartment.RECOVERED );
							LOG.info( "t={} ENDED IMPORT at index case: {}",
									scheduler().nowDT(), minResistant );
							trackSub.dispose(); // stop tracking patient zero
						} );
		LOG.info( "t={} IMPORTED index case: {}, recovery +{}d= @{}",
				scheduler().nowDT(), minResistant, this.gamma_inv,
				recovery.due() );
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
			target.updateAndGet( Persons.PathogenCompartment.class,
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
				// TODO decrease resistance for partial vax compliance?

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
					.get( Persons.PathogenCompartment.class );
			this.resistance.put( pp.key(),
					sir == MSEIRS.Compartment.SUSCEPTIBLE
							? pp.get( Persons.PathogenResistance.class )
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

	BigDecimal cohortAgeBin( final BigDecimal virtualTime )
	{
		return virtualTime.divide( this.cohortBirthResolution,
				RoundingMode.FLOOR );
	}

	void addToCohort( final PersonTuple pp )
	{
		final BigDecimal ageBin = cohortAgeBin( pp.get( Persons.Birth.class ) );
		this.birthCohorts.computeIfAbsent( ageBin, k -> new ArrayList<>() )
				.add( pp.key() );
	}

	void removeFromCohort( final PersonTuple pp )
	{
		final BigDecimal ageBin = cohortAgeBin( pp.get( Persons.Birth.class ) );
		this.birthCohorts.getOrDefault( ageBin, Collections.emptyList() )
				.remove( pp.key() );
	}

	void onDelete( final PersonTuple pp )
	{
		final Object siteRef = pp.get( Persons.HomeSiteRef.class );
		getLP( siteRef ).depart( pp );
		removeFromCohort( pp );
	}

	void onCreate( final PersonTuple pp )
	{
		final LocalDate birthDT = Instant
				.of( pp.get( Persons.Birth.class ), scheduler().timeUnit() )
				.toJava8( scheduler().offset().toLocalDate() );
		final MSEIRS.Compartment status = this.sirStatusDist.draw(
				RegionPeriod.of( (String) pp.get( Persons.HomeRegionRef.class ),
						birthDT ) );

		pp.set( Persons.PathogenCompartment.class, status );
		if( status == MSEIRS.Compartment.SUSCEPTIBLE )
		{
			pp.set( Persons.PathogenResistance.class,
					this.resistanceDist.draw() );
			addToCohort( pp );
		} else
		{
			pp.set( Persons.PathogenResistance.class, 0d );
//			final ComparableQuantity<Time> age = QuantityUtil.valueOf(
//					now().decimal().subtract( pp.get( Persons.Birth.class ) ),
//					now().unit().asType( Time.class ) );
//			if( this.vaccinationAge.contains( age ) )
//				pp.set( Persons.VaxInviteTime.class, 1 );
		}

		if( this.pendingPressure == null )
			this.pendingPressure = atOnce( this::handleMoves );

		this.nextCreations.add( pp.key() );
	}

//	int statusOf(final VaxDose... doses )
//	{
//		int result = 0;
//		if()
//	}

	private void scheduleVaccinations( final Instant t )
	{
		final BigDecimal now = t.decimal();
		final Range<BigDecimal> decisionAgeBinRange = VaxDose.decisionAgeRange()
				.map( dt -> t.decimal()
						.subtract( QuantityUtil.decimalValue( dt, t.unit() ) )
						.divide( this.cohortBirthResolution,
								RoundingMode.FLOOR ) );

		final NavigableMap<BigDecimal, List<Object>> subMap = decisionAgeBinRange
				.apply( this.birthCohorts, false );
		LOG.trace( "Vaccinate cohorts range {} -> {} : {}..{} <- {} ppl",
				VaxDose.decisionAgeRange(), decisionAgeBinRange,
				subMap.firstKey(), subMap.lastKey(),
				subMap.values().stream().mapToInt( List::size ).sum() );
		final PersonTuple[] removable = subMap.values().stream()
				.flatMap( List::stream ).map( this.persons::select )
				.filter( pp -> pp
						.get( Persons.PathogenCompartment.class ) == MSEIRS.Compartment.SUSCEPTIBLE )
				.filter( pp ->
				{
					final int status = pp.get( Persons.VaxCompliance.class );

					if( VaxDose.isCompliant( status ) )
					{
						LOG.warn( "Already compliant {}", pp );
						pp.updateAndGet( Persons.PathogenCompartment.class,
								epi -> MSEIRS.Compartment.VACCINATED );
						return true;
					}
					final ComparableQuantity<Time> age = QuantityUtil
							.valueOf(
									now.subtract(
											pp.get( Persons.Birth.class ) ),
									scheduler().timeUnit() )
							.asType( Time.class ).to( TimeUnits.WEEK );

					final VaxDose nextDose = VaxDose.nextDefault( status, age );
					if( nextDose == null )
					{
						LOG.warn( "Not covered by NIP, age {} of {}",
								QuantityUtil.pretty( age, TimeUnits.YEAR, 1 ),
								pp );
						return true;
					}
					final VaxOccasion occ = this.vaxOccasionDist
							.draw( nextDose );
					final HouseholdTuple hh = this.households
							.get( pp.get( Persons.HouseholdRef.class ) );
					if( this.vaxAcceptance.test( hh, occ ) )
					{
						startRegimen( nextDose, age, pp );
						return true;
					}

					LOG.info( "Vax {} for {} rejected by {} of {}", occ,
							nextDose, pp, hh );
					return false;
				} ).toArray( PersonTuple[]::new );
		Arrays.stream( removable ).forEach( this::removeFromCohort );
	}

	private void startRegimen( final VaxDose nextDose,
		final ComparableQuantity<Time> age, final PersonTuple pp )
	{
		final int status = pp.get( Persons.VaxCompliance.class );
		final ComparableQuantity<Time> vaxAge = Compare.max(
				nextDose.ageRangeOptional().lowerValue(),
				nextDose.ageRangeDefault().lowerValue()
						.add( this.vaxTreatmentDelay.draw() ) ),
				delay = vaxAge.subtract( age );
		LOG.info( "t={} Vax {} @age {} (t+{}) status {}->{} for {}", dt(),
				nextDose, QuantityUtil.pretty( vaxAge, TimeUnits.WEEK, 1 ),
				QuantityUtil.pretty( delay, TimeUnits.WEEK, 1 ), status,
				nextDose.set( status ), pp );

		after( delay ).call( t_v ->
		{
			pp.updateAndGet( Persons.PathogenCompartment.class,
					epi -> MSEIRS.Compartment.VACCINATED );
			pp.updateAndGet( Persons.VaxCompliance.class,
					old -> nextDose.set( old ) );

			// TODO check attitude & continue next dose until fully compliant
		} );
	}

	//		final Range<BigDecimal> birthRange = this.vaxTreatmentAge
//				.map( age -> t.subtract( age ).decimal() );
//		
//		LOG.debug( "t={}, vaccination occasion: {} for susceptibles born {}",
//				prettyDate( t ), occ.asMap().values(),
//				birthRange.map(
//						age -> prettyDate( Instant.of( age, TimeUnits.ANNUM ) )
//								.toString() ) );

	// for each households evaluated with a positive attitude
//		final List<Long> vax =

//				this.attitudeEvaluator.isPositive( occ, this.hhAttributes )
//				// for each child in the (positive) household, update child if:
//				.flatMap( hh -> Arrays.stream( CHILD_REF_COLUMN_INDICES )
//						.mapToLong( hhAtt -> this.hhAttributes.getAsLong( hh,
//								hhAtt.ordinal() ) ) )
//				// 1. exists
//				.filter( i -> i != NA )
//				// 2. is susceptible
//				.filter( i -> getStatus( i ) == HHMemberStatus.SUSCEPTIBLE )
//				// 3. is of vaccination age
//				.filter( i -> birthRange
//						.contains( this.ppAttributes.getAsBigDecimal( i,
//								HHMemberAttribute.BIRTH.ordinal() ) ) )
//				vaxDecisionAgeMinimum
//				this.persons.stream().filter( pp -> pp.get(
//						Persons.EpiCompartment.class ) == MSEIRS.Compartment.SUSCEPTIBLE )
//				.filter(pp->birthRange.contains(pp.get(Persons.Birth.class ) ))
//				.filter(pp->this.this.households.select(pp.get(Persons.HouseholdRef.class ) ).get(Households.))
//				.forEach( this::setVaccinated );
//	LOG.trace("t={} VACCINATED {} ppl.",
//
//	prettyDate( now() ), 0 );
//	}

//	void onMove( final Object ppRef, final Object oldSiteRef,
//		final Object newSiteRef )
//	{
//		final PersonTuple pp = this.persons.select( ppRef );
//		if( this.pendingPressure == null )
//			this.pendingPressure = atOnce( this::handleMoves );
//
//		if( oldSiteRef != null ) this.nextDepartures
//				.computeIfAbsent( oldSiteRef, k -> new HashSet<>() )
//				.add( pp.key() );
//		this.nextArrivals.computeIfAbsent( newSiteRef, k -> new HashSet<>() )
//				.add( pp );
//	}

}
