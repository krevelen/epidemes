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
import nl.rivm.cib.demo.DemoModel.Households;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.demo.DemoModel.Medical.VaxAcceptanceEvaluator;
import nl.rivm.cib.demo.DemoModel.Medical.VaxDose;
import nl.rivm.cib.demo.DemoModel.Medical.VaxRegimen;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Compartment;
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
		String PATHOGEN_PREFIX = "pathogen" + DemoConfig.KEY_SEP;

		String VACCINATION_PREFIX = "vaccination" + DemoConfig.KEY_SEP;

		String OCCASION_PREFIX = VACCINATION_PREFIX + "occasion"
				+ DemoConfig.KEY_SEP;

		@Key( PATHOGEN_PREFIX + "average-reproduction-period" )
		@DefaultValue( "12 day" )
		String reproductionDays();

		@Key( PATHOGEN_PREFIX + "average-recovery-period" )
		@DefaultValue( "14 day" )
		String recoveryPeriod();

		@Key( PATHOGEN_PREFIX + "beta-factor" )
		@DefaultValue( "1" )
		double betaFactor();

		@Key( PATHOGEN_PREFIX + "last-outbreak-end-date" )
		@DefaultValue( "2000-06-30" )
		@ConverterClass( LocalDateConverter.class )
		LocalDate recoveredBefore();

		@Key( PATHOGEN_PREFIX + "next-outbreak-start-date" )
		@DefaultValue( "2013-07-06T05:43:21" )
		@ConverterClass( LocalDateTimeConverter.class )
		LocalDateTime outbreakStart();

//		@Key( "vaccination-degree" )
//		@DefaultValue( ".9" )
//		double overallVaccinationDegree();

		@Key( VACCINATION_PREFIX + "acceptance-evaluator" )
		@DefaultValue( "nl.rivm.cib.demo.DemoModel$Medical$VaxAcceptanceEvaluator$Average" )
		Class<?> acceptanceEvaluatorType();

		// BMR2 reached, mean(sd): 14.2(0.5) months/61.5(1.9) weeks/425(13) days
		/** typical treatment delay relative to the (arbitrary) decision age */
		@Key( VACCINATION_PREFIX + "treatment-delay-dist" )
//		@DefaultValue( "normal(58.5 week;1.85 week)" )
		@DefaultValue( "normal(.5;1.8)" )
		String treatmentDelayDist();

		// TODO allow multiple occasions, each recurring with unique convenience factors distributions
		@Key( OCCASION_PREFIX + "recurrence" )
		@DefaultValue( "0 0 10 ? * MON-FRI *" )
		String occasionRecurrence();

		/** @see VaxOccasion#utility() */
		@Key( OCCASION_PREFIX + "utility-dist" )
		@DefaultValue( "const(0.5)" )
		String occasionUtilityDist();

		/** @see VaxOccasion#proximity() */
		@Key( OCCASION_PREFIX + "proximity-dist" )
		@DefaultValue( "const(0.5)" )
		String occasionProximityDist();

		/** @see VaxOccasion#clarity() */
		@Key( OCCASION_PREFIX + "clarity-dist" )
		@DefaultValue( "const(0.5)" )
		String occasionClarityDist();

		/** @see VaxOccasion#affinity() */
		@Key( OCCASION_PREFIX + "affinity-dist" )
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

	// TODO from config
	private VaxRegimen regimen = MeaslesRVP.instance();

	// TODO from config
	private VaxAcceptanceEvaluator vaxAcceptance = VaxAcceptanceEvaluator.MIN_CONVENIENCE_GE_AVG_ATTITUDE;
	/** */
	private ConditionalDistribution<VaxOccasion, VaxDose> vaxOccasionDist;
	/** */
	private QuantityDistribution<Time> vaxTreatmentDelay;
	/** age resolution of vaccination cohorts/bins, in scheduler time units */
	private BigDecimal vaxDecisionAgeResolution;
	/** current hesitants/susceptibles within/before decision age, by age bin */
	private TreeMap<BigDecimal, List<Object>> vaxAgeHesitants = new TreeMap<>();
	/** */
	private ConditionalDistribution<Compartment, RegionPeriod> sirStatusDist;
	/** */
	private ProbabilityDistribution<Double> resistanceDist;
	/** */
	private double gamma_inv, beta;
	/** */
	private QuantityDistribution<Time> recoveryPeriodDist;

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
		this.gamma_inv = recoveryDays;
		this.beta = reproductionDays / recoveryDays * this.config.betaFactor();
		LOG.info( "beta = {} = reproduction: {} / recovery: {} * factor: {}",
				this.beta, reproductionDays, recoveryDays,
				this.config.betaFactor() );

		final LocalDate lastOutbreak = this.config.recoveredBefore();
//		final double vaxDegree = this.config.overallVaccinationDegree();
//		final Map<String, ProbabilityDistribution<Boolean>> localVaxDegreeDist = new HashMap<>();
		this.sirStatusDist = regPer -> regPer.periodRef()
				.isBefore( lastOutbreak ) ? Compartment.RECOVERED
//						: localVaxDegreeDist
//								.computeIfAbsent( regPer.regionRef(),
//										k -> this.distFactory
//												.createBernoulli( vaxDegree ) )
//								.draw() ? Compartment.VACCINATED
						: Compartment.SUSCEPTIBLE;

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

		this.vaxDecisionAgeResolution = QuantityUtil
				.decimalValue( QuantityUtil.valueOf( 1, TimeUnits.DAY )
						.to( scheduler().timeUnit().asType( Time.class ) ) );

		this.households = this.data.getTable( HouseholdTuple.class );
		this.persons = this.data.getTable( PersonTuple.class );
		this.persons.onCreate( this::onCreate, scheduler()::fail );
		this.persons.onDelete( this::onDelete, scheduler()::fail );
//		this.persons.onUpdate( Persons.SiteRef.class,this::onMove, scheduler()::fail);
		this.persons.onUpdate( Persons.PathogenCompartment.class,
				this::onCompartmentTransition, scheduler()::fail );

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

	private void onCompartmentTransition( final Object sourceRef,
		final Compartment oldValue, final Compartment newValue )
	{
		final PersonTuple pp = this.persons.select( sourceRef );
		this.events.onNext( new EpidemicFact().withPerson( pp ).withSIRDelta(
				map -> map.put( oldValue, -1 ).put( newValue, 1 ) ) );
		switch( newValue )
		{
		case INFECTIVE:
			// schedule recovery after infectious period 
			after( this.recoveryPeriodDist.draw() ).call(
					t_r -> pp.getAndUpdate( Persons.PathogenCompartment.class,
							prev -> prev == Compartment.INFECTIVE
									? Compartment.RECOVERED : prev ) );
			break;
		default:
			break; // TODO: apply IllnessTrajectory?
		}
	}

	private void outbreakStart( final Instant t )
	{
		// TODO implement a steady way of choosing PATIENT ZERO?
		final PersonTuple minResistant = this.persons.stream()
				.filter( pp -> pp.get( Persons.PathogenCompartment.class )
						.isSusceptible() )
				.min( ( l, r ) -> l.get( Persons.PathogenResistance.class )
						.compareTo(
								r.get( Persons.PathogenResistance.class ) ) )
				.orElse( null );

		minResistant.set( Persons.PathogenCompartment.class,
				Compartment.INFECTIVE );
		this.events.onNext( new EpidemicFact().withPerson( minResistant )
				.withSIRDelta( map -> map.put( Compartment.SUSCEPTIBLE, -1 )
						.put( Compartment.INFECTIVE, 1 ) ) );

		final Disposable trackSub = this.persons.changes( minResistant.key() )
				.subscribe( chg -> LOG.warn( LogUtil.messageOf(
						"t={} PATIENT ZERO {} change: {}", scheduler().nowDT(),
						minResistant.pretty( Persons.PROPERTIES ), chg ),
						new IllegalStateException( "Who updates this?" ) ) );

		final ComparableQuantity<Time> dt = //this.recoveryPeriodDist.draw() 
				QuantityUtil.valueOf( this.gamma_inv, TimeUnits.DAYS );
		final Expectation recovery = after( dt ).call( tRecover ->
		{
			minResistant.set( Persons.PathogenCompartment.class,
					Compartment.RECOVERED );
			this.events.onNext( new EpidemicFact().withPerson( minResistant )
					.withSIRDelta( map -> map.put( Compartment.INFECTIVE, -1 )
							.put( Compartment.RECOVERED, 1 ) ) );
			LOG.info( "t={} index case patient zero removed: {}",
					scheduler().nowDT(),
					minResistant.pretty( Persons.PROPERTIES ) );
			trackSub.dispose(); // stop tracking patient zero
		} );
		LOG.info(
				"t={} IMPORTED index case, patient zero: {}, recovery +{}d= @{}",
				scheduler().nowDT(), minResistant.pretty( Persons.PROPERTIES ),
				this.gamma_inv, recovery.due() );
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

	public static class LocalPressure //extends Accumulator
		implements Proactive
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
					oldSIR -> Compartment.INFECTIVE );
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
				return dt.compareTo( VAX_HORIZON ) > 0 ? null
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
			final Compartment sir = pp.get( Persons.PathogenCompartment.class );
			this.resistance.put( pp.key(),
					sir == Compartment.SUSCEPTIBLE
							? pp.get( Persons.PathogenResistance.class )
							: sir == Compartment.INFECTIVE ? 0d : -1d );
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

	BigDecimal vaxDecisionAgeBin( final BigDecimal tBirth )
	{
		return tBirth.divide( this.vaxDecisionAgeResolution,
				RoundingMode.FLOOR );
	}

	void addToHesitant( final PersonTuple pp )
	{
		final BigDecimal ageBin = vaxDecisionAgeBin(
				pp.get( Persons.Birth.class ) );
		this.vaxAgeHesitants.computeIfAbsent( ageBin, k -> new ArrayList<>() )
				.add( pp.key() );
	}

	void removeFromHesitant( final PersonTuple pp )
	{
		final BigDecimal ageBin = vaxDecisionAgeBin(
				pp.get( Persons.Birth.class ) );
		this.vaxAgeHesitants.getOrDefault( ageBin, Collections.emptyList() )
				.remove( pp.key() );
	}

	void onDelete( final PersonTuple pp )
	{
		final Object siteRef = pp.get( Persons.HomeSiteRef.class );
		getLP( siteRef ).depart( pp );
		removeFromHesitant( pp );
	}

	void onCreate( final PersonTuple pp )
	{
		final LocalDate birthDT = Instant
				.of( pp.get( Persons.Birth.class ), scheduler().timeUnit() )
				.toJava8( scheduler().offset().toLocalDate() );
		final Compartment status = this.sirStatusDist.draw( RegionPeriod.of(
				(String) pp.get( Persons.HomeRegionRef.class ), birthDT ) );

		pp.set( Persons.PathogenCompartment.class, status );
		if( status == Compartment.SUSCEPTIBLE )
		{
			pp.set( Persons.PathogenResistance.class,
					this.resistanceDist.draw() );
			addToHesitant( pp );
		} else
		{
			pp.set( Persons.PathogenResistance.class, 0d );
		}

		if( this.pendingPressure == null )
			this.pendingPressure = atOnce( this::handleMoves );

		this.nextCreations.add( pp.key() );
	}

	private void scheduleVaccinations( final Instant t )
	{
		final Range<BigDecimal> decisionAgeBinRange = this.regimen
				.decisionAgeRange()
				.map( age -> vaxDecisionAgeBin( t.decimal().subtract(
						QuantityUtil.decimalValue( age, t.unit() ) ) ) );

		final NavigableMap<BigDecimal, List<Object>> subMap = decisionAgeBinRange
				.apply( this.vaxAgeHesitants, false );
//		LOG.debug( "Vaccinate cohorts range {} -> {} : {}..{} <- {} ppl",
//				this.regimen.decisionAgeRange(), decisionAgeBinRange,
//				subMap.firstKey(), subMap.lastKey(),
//				subMap.values().stream().mapToInt( List::size ).sum() );
		final PersonTuple[] removable = subMap.values().stream()
				.flatMap( List::stream ).map( this.persons::select )
				.filter( pp -> pp
						.get( Persons.PathogenCompartment.class ) == Compartment.SUSCEPTIBLE )
				.filter( pp ->
				{
					final int status = pp.get( Persons.VaxCompliance.class );

					if( this.regimen.isCompliant( status ) )
					{
						LOG.warn( "Already compliant {}", pp );
						pp.updateAndGet( Persons.PathogenCompartment.class,
								epi -> Compartment.VACCINATED );
						return true; // remove from hesitant
					}
					final ComparableQuantity<Time> age = ageOf( pp );

					final VaxDose nextDose = this.regimen.nextRegular( status,
							age );
					if( nextDose == null )
					{
						LOG.warn( "Not covered by NIP, age {} for pp {}",
								QuantityUtil.pretty( age, TimeUnits.YEAR, 1 ),
								pp.pretty( Persons.PROPERTIES ) );
						return true; // remove from hesitant
					}
					final VaxOccasion occ = this.vaxOccasionDist
							.draw( nextDose );
					final HouseholdTuple hh = this.households
							.get( pp.get( Persons.HouseholdRef.class ) );
					if( hh == null )
					{
						LOG.warn( "No hh for pp, removing {}",
								pp.pretty( Persons.PROPERTIES ) );
						return true; // remove from hesitant
					}
					if( this.vaxAcceptance.test( hh, occ ) )
					{
						startRegimen( nextDose, age, pp );
						return true; // remove from hesitant
					}

					LOG.info( "Vax {} for {} rejected by {} of {}", occ,
							nextDose, pp.pretty( Persons.PROPERTIES ),
							hh.pretty( Households.PROPERTIES ) );
					return false; // remain hesitant
				} ).toArray( PersonTuple[]::new );
		Arrays.stream( removable ).forEach( this::removeFromHesitant );
	}

	private void startRegimen( final VaxDose nextDose,
		final ComparableQuantity<Time> age, final PersonTuple pp )
	{
		final ComparableQuantity<Time> vaxAge = Compare.max(
				nextDose.ageRangeSpecial().lowerValue(),
				nextDose.ageRangeNormal().lowerValue()
						.add( this.vaxTreatmentDelay.draw() ) ),
				delay = vaxAge.subtract( age );

//		final int cOld = pp.get( Persons.VaxCompliance.class ),
//				cNew = nextDose.set( cOld );
//		LOG.trace( "t={} Vax {} @age {} (t+{}) status {}->{} for {}", dt(),
//				nextDose, QuantityUtil.pretty( vaxAge, TimeUnits.WEEK, 1 ),
//				QuantityUtil.pretty( delay, TimeUnits.WEEK, 1 ), cOld, cNew,
//				pp.pretty( Persons.PROPERTIES ) );

		after( delay ).call( t_v ->
		{
			pp.updateAndGet( Persons.PathogenCompartment.class,
					epi -> Compartment.VACCINATED );
			pp.updateAndGet( Persons.VaxCompliance.class,
					old -> nextDose.flippedOn( old ) );

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
//						Persons.EpiCompartment.class ) == Compartment.SUSCEPTIBLE )
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
