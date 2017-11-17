package nl.rivm.cib.pilot;

import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.enums.ValueType;

import io.coala.bind.InjectConfig;
import io.coala.bind.InjectConfig.Scope;
import io.coala.bind.LocalBinder;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.random.QuantityDistribution;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.epidemes.cbs.json.CBSGender;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.episim.model.SocialConnector;
import nl.rivm.cib.episim.model.SocialGatherer;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;
import nl.rivm.cib.pilot.dao.HHStatisticsDao;
import nl.rivm.cib.pilot.dao.PilotConfigDao;
import nl.rivm.cib.pilot.hh.HHAttitudeEvaluator;
import nl.rivm.cib.pilot.hh.HHAttitudePropagator;
import nl.rivm.cib.pilot.hh.HHAttractor;
import nl.rivm.cib.pilot.hh.HHAttractor.Broker;
import nl.rivm.cib.pilot.hh.HHAttribute;
import nl.rivm.cib.pilot.hh.HHMemberAttribute;
import nl.rivm.cib.pilot.hh.HHMemberStatus;
import nl.rivm.cib.pilot.json.HesitancyProfileJson;
import nl.rivm.cib.pilot.json.HesitancyProfileJson.VaccineStatus;
import nl.rivm.cib.pilot.json.RelationFrequencyJson;
import tec.uom.se.ComparableQuantity;

/**
 * {@link PilotScenario} is a simple example {@link Scenario} implementation, of
 * which only one {@link Singleton} instance exists per {@link LocalBinder}
 * 
 * <pre>
      # force of infection (S->E): t=?, viral shedding -> respiratory/surface contact 
      # infection pressure function of #infectious vs. #non-infectious
      pathogen-type: nl.rivm.cib.pilot.Pathogen$MSEIR
      pressure-type: nl.rivm.cib.episim.model.disease.infection.InfectionPressure$Proportional
      # invasion period (S->E) the duration of 'exposure resistance' per individual
      passive-period-dist: tria( 10 day; 20 day; 30 day )
      # latent period (E->I)
      #  - t+0, onset around t+14 but infectious already 4 days before
      #  - exp(0.09) : 1/11d (6-17d) -3d coryza etc before prodromal fever at 7-21d
      latent-period-dist: tria( 1 day; 10 day; 17 day )
      # recovery/infectious period (I->R) = viral shedding until recovery
      #  - exp(.16) : 1/10d (6-14d) = prodromal fever 3-7 days + rash 4-7days
      #  - t+14-4, [onset-4,onset+4] 
      shedding-period-dist: tria( 0 day; 8 day; 10 day )
      # wane period (R->S) = duration of immunity, e.g. forever immune
      wane-period-dist: const( 1000 yr )
      # incubation period (E->C, sub->clinical symptoms)
      #  - exp(0.07) : 1/14 (7-21 days, says CDC)
      #  - e.g. t+7-21 days (CDC) rash, fever, Koplik spots, ... 
      #  - e.g. t+9-12 days (https://www.wikiwand.com/en/Incubation_period)
      incubation-period-dist: tria( 7 day; 11 day; 21 day )
      # symptom/clinical period (C->R, normal/asymptomatic)
      symptom-period-dist: tria( 4 day; 5 day; 7 day )
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class PilotScenario implements Scenario
{

	/**
	 * {@link GenderAge} wraps a {@link Tuple} in: CBSGender x Instant (birth)
	 */
	public static class GenderAge extends Tuple
	{
		public GenderAge( final CBSGender gender, final BigDecimal ageYears )
		{
			super( Arrays.<Comparable<?>>asList( gender, ageYears ) );
		}

		public CBSGender gender()
		{
			return (CBSGender) values().get( 0 );
		}

		public BigDecimal ageYears()
		{
			return (BigDecimal) values().get( 1 );
		}
	}

	/** */
	private static final Logger LOG = LogUtil.getLogger( PilotScenario.class );

	private static final long NA = -1L;

	/** */
//	private static final HHAttribute[] CHILD_REF_COLUMN_INDICES = {
//			// HHAttribute.REFERENT_REF,
//			// HHAttribute.PARTNER_REF,
//			HHAttribute.CHILD1_REF
////			, HHAttribute.CHILD2_REF
////			, HHAttribute.CHILD3_REF 
//	};

	@InjectConfig( Scope.DEFAULT )
	private transient PilotConfig config;

	@Inject
	private transient LocalBinder binder;

	@Inject
	private transient Scheduler scheduler;

	@Inject
	private transient ProbabilityDistribution.Factory distFactory;

	@Inject
	private transient ProbabilityDistribution.Parser distParser;

	/** current (cached) virtual time instant */
	private transient Instant dtInstant = null;
	/** current (cached) date/time */
	private transient LocalDate dtCache = null;
	/** */
	private final AtomicInteger statsIteration = new AtomicInteger();

	/** */
	private Matrix hhAttributes;
	/** */
	private final AtomicLong hhCount = new AtomicLong();
	/** */
	private ProbabilityDistribution<CBSHousehold> hhTypeDist;
	/** */
	private QuantityDistribution<Time> hhRefAgeDist;
	/** */
	private ProbabilityDistribution<Boolean> hhRefMaleDist;
	/** */
	private ConditionalDistribution<Quantity<Time>, RelationFrequencyJson.Category> hhImpressIntervalDist;
	/** */
	private QuantityDistribution<Time> hhMigrateDist;
	/** */
//	private Quantity<Time> hhLeaveHomeAge;

	/** */
	private Matrix ppAttributes;
	/** */
	private final AtomicLong persons = new AtomicLong();

	private final Map<Long, LocalPressure> ppHome = new TreeMap<>();

	/** */
	private Range<ComparableQuantity<Time>> vaccinationAge;
	/** */
	private transient ProbabilityDistribution<VaxOccasion> vaxOccasionDist;

	private final Map<Long, Long> hhIndex = new HashMap<>();

	private final Map<Long, Long> ppIndex = new HashMap<>();

	/** */
	private Matrix hhNetwork;
	/** */
	private NavigableMap<String, HHAttractor> attractors;
	/** */
	private String[] attractorNames;
	/** */
	private HHAttractor.Broker attractorBroker;
	/** */
	private transient ConditionalDistribution<HesitancyProfileJson, HesitancyProfileJson.Category> hesitancyProfileDist;
	/** */
	private transient ProbabilityDistribution<BigDecimal> calculationDist;
	/** */
	private transient ProbabilityDistribution<Boolean> schoolAssortativity;
	/** */
	private transient HHAttitudeEvaluator attitudeEvaluator;
	/** */
	private transient HHAttitudePropagator attitudePropagator;
	/** */
	private transient ConditionalDistribution<Map<HHAttribute, BigDecimal>, HesitancyProfileJson> hesitancyDist;

	private transient ProbabilityDistribution<Double> resistanceDist;

	/** TODO from config */
	private double reproductionDays = 12d, recoveryDays = 14d,
			gamma_inv = recoveryDays, beta = reproductionDays / gamma_inv * 100;
	// apply subpop(i) scaling: Prod_i(N/size_i)^(time_i)/T = (1000/1000)^(.25) * (1000/2)^(.75)

	private ProbabilityDistribution<Double> recoveryDaysDist;

	/** virtual time range of simulation */
//	private transient Range<LocalDate> timeRange = null;
	/** empirical household compositions and referent ages, see CBS 71486 */
//	private transient ConditionalDistribution<Cbs71486json.Category, LocalDate> localHouseholdDist;
	/** zip-codes per borough / ward / municipality / ... */
//	private transient ConditionalDistribution<CbsNeighborhood, Region.ID> hoodDist;

	/** */
	private NavigableMap<String, SocialGatherer> motors;
	/** */
	private LocalPressure[] motorPressure;

//	private transient ConditionalDistribution<Quantity<Time>, GenderAge> peerPressureIntervalDist;

	@Override
	public void init() throws Exception
	{
		final PseudoRandom rng = this.distFactory.getStream();
		LOG.info( "seed: {}, offset: {}", rng.seed(), scheduler().offset() );

		scheduler().atEnd( t -> this.sirTransitions.onComplete(),
				this.sirTransitions::onError );
		this.attractors = this.config.hesitancyAttractors( this.binder );
		this.attractorNames = this.attractors.keySet().stream()
				.toArray( String[]::new );

		final CBSHousehold hhType = this.config
				.householdTypeDist( this.distParser ).draw(); // assuming constant
		final long ppTotal = this.config.populationSize(),
				hhTotal = ppTotal / hhType.size(),
				edges = hhTotal + this.attractors.size();
		LOG.info( "Populate #pp: {}, #hh: {}, #attractors: {}, #link-max: {}",
				ppTotal, hhTotal, this.attractors.size(), edges );

		// or Matrix.Factory.linkToJDBC(host, port, db, table, user, password)
		// or Matrix.Factory.linkTo().file("hugeCSVFile").asDenseCSV(columnSeparator)
		this.hhAttributes = Matrix.Factory.zeros( ValueType.BIGDECIMAL, edges,
				HHAttribute.values().length );
		this.ppAttributes = Matrix.Factory.zeros( ValueType.BIGDECIMAL, ppTotal,
				HHMemberAttribute.values().length );
		this.hhNetwork = SparseMatrix.Factory.zeros( edges, edges );

		this.motors = this.config.mobilityGatherers( this.binder );
		this.motorPressure = this.motors.entrySet().stream()
				.map( e -> new LocalPressure( e.getKey() ).conveneOn(
						e.getValue().summon(),
						i -> this.ppHome.get( i ).depart( i ),
						i -> this.ppHome.get( i ).arrive( i ) ) )
				.toArray( LocalPressure[]::new );
		// TODO expand to local/personal motor

		this.recoveryDaysDist = this.distFactory
				.createExponential( this.gamma_inv );
		this.vaccinationAge = this.config.vaccinationAgeRange();

		this.attractors.forEach( ( name, attractor ) ->
		{
			final int index = (int) this.hhCount.getAndIncrement();
//			this.attractorIndex.put( name, index );
			this.hhAttributes.setAsInt( index, index,
					HHAttribute.ATTRACTOR_REF.ordinal() );
			this.hhAttributes.setAsInt( index, index,
					HHAttribute.IDENTIFIER.ordinal() );
			attractor.adjustments().subscribe( map ->
			{
				LOG.debug( "t={}, disturbance @{}: {}", prettyDate( now() ),
						name, map );
				map.forEach( ( att, val ) -> this.hhAttributes
						.setAsBigDecimal( val, index, att.ordinal() ) );
			}, this::logError );
		} );

		this.hhImpressIntervalDist = this.config
				.hesitancyRelationFrequencyDist( this.distFactory );

//		this.timeRange = Range
//				.upFromAndIncluding( scheduler().offset().toLocalDate() );
//		this.localHouseholdDist = this.config.cbs71486( this.timeRange,
//				this.distFactory );
//		final Region.ID fallbackRegRef = this.config.fallbackRegionRef();
//		this.hoodDist = this.config.neighborhoodDist( this.distFactory,
//				regRef -> fallbackRegRef );
//		final Map<Long, Region.ID> regions = new HashMap<>();
		final Broker broker = hhIndex -> (int) (hhIndex
				% this.attractors.size());
		this.attractorBroker = broker;
		this.hhTypeDist = this.config.householdTypeDist( this.distParser );
		this.hhRefMaleDist = this.config
				.householdReferentMaleDist( this.distFactory );
		this.hhRefAgeDist = this.config
				.householdReferentAgeDist( this.distParser );
//		this.peerPressureIntervalDist = this.config
//				.peerPressureInterval( this.distFactory );

		this.hesitancyProfileDist = this.config.hesitancyProfilesGrouped(
				this.distFactory, HesitancyProfileJson::toCategory );
		this.calculationDist = this.config
				.hesitancyCalculationDist( this.distParser );

//		this.hhLeaveHomeAge = this.config.householdLeaveHomeAge();
		this.hhMigrateDist = this.config
				.householdReplacementDist( this.distFactory, hhTotal );
		after( this.hhMigrateDist.draw() ).call( this::migrateHousehold );

		final ProbabilityDistribution<Number> vaccinationUtilityDist = this.config
				.vaccinationUtilityDist( this.distParser );
		final ProbabilityDistribution<Number> vaccinationProximityDist = this.config
				.vaccinationProximityDist( this.distParser );
		final ProbabilityDistribution<Number> vaccinationClarityDist = this.config
				.vaccinationClarityDist( this.distParser );
		final ProbabilityDistribution<Number> vaccinationAffinityDist = this.config
				.vaccinationAffinityDist( this.distParser );
		this.vaxOccasionDist = () -> VaxOccasion.of(
				vaccinationUtilityDist.draw(), vaccinationProximityDist.draw(),
				vaccinationClarityDist.draw(), vaccinationAffinityDist.draw() );

		// reference to json indices
		this.hesitancyDist = this.config
				.hesitancyProfileSample( this.distFactory.getStream() );

		this.resistanceDist = this.distFactory.createExponential( 1 );

		// populate households
		for( long time = System.currentTimeMillis(), agPrev = 0; this.persons
				.get() < ppTotal; )
		{
			createHousehold( NA );
			if( System.currentTimeMillis() - time > 1000 )
			{
				time = System.currentTimeMillis();
				long agNow = this.persons.get() + this.hhCount.get();
				LOG.trace( "Populating, {} pp ({}%), {} hh (= +{} actors/sec)",
						this.persons.get(), this.persons.get() * 100 / ppTotal,
						this.hhCount.get(), agNow - agPrev );
				agPrev = agNow;
			}
		}

		publishSIR( ppTotal, 0, 0, 0 );

		// schedule disease import
		final long firstCase = //this.distFactory.getStream().nextLong( ppTotal );
				this.ppAttributes
						.selectColumns( Ret.LINK,
								HHMemberAttribute.RESISTANCE.ordinal() )
						.getCoordinatesOfMinimum()[0];
		at( LocalDateTime.of( 2013, 1, 2, 3, 4, 5, 6 ) ) // t=123.1 since 2012-09-01
				.call( t ->
				{
					LOG.trace( "IMPORTED DISEASE at index: {}", firstCase );
					setInfected( firstCase, getResistance( firstCase ) );
				} );

		LOG.info( "Populated: {} pp ({}%) across {} hh in {} attractor/regions",
				this.persons.get(), this.persons.get() * 100 / ppTotal,
				this.hhCount.get() - this.attractors.size(),
				this.attractors.size() );

		final double socialBeta = this.config.hesitancySocialNetworkBeta();
		final SocialConnector conn = new SocialConnector.WattsStrogatz( rng,
				socialBeta );
		final long A = this.attractors.size(), N = this.hhCount.get() - A,
				Na = N / A + 1, // add 1 extra for partition rounding errors
				K = Math.min( Na - 1,
						this.config.hesitancySocialNetworkDegree() );

		this.schoolAssortativity = this.config
				.hesitancySchoolAssortativity( this.distParser );

		final double assortativity = this.config.hesitancySocialAssortativity(),
				dissortativity = (1.0 - assortativity);
		final Supplier<Long> assortK = () -> (long) (assortativity * K * (A - 1)
				/ A);
		final Supplier<Long> dissortK =
//				() -> Math.round( (1.0 - assortativity) * K / (A - 1) )
				this.distFactory.createPoisson( // use poisson for small degree
						dissortativity * K )::draw;

//		final long assortativeK = A < 2 ? K
//				: (long) (K * this.config.hesitancySocialAssortativity()),
//				dissortativeK = A < 2 ? 0
//						: Math.max( 1, (K - assortativeK) / (A - 1) );

		final Matrix[] assorting = LongStream.range( 0, A ).mapToObj( a ->
		{
			final BigDecimal inpeerW = this.hhAttributes.getAsBigDecimal( a,
					HHAttribute.IMPRESSION_INPEER_WEIGHT.ordinal() );
			if( inpeerW.signum() < 1 ) LOG.warn( "no weight: {}", inpeerW );
			final Matrix m = conn.connect( Na, assortK, x -> true,
					x -> inpeerW );
			return m;
		} ).toArray( Matrix[]::new );

		final Matrix dissorting = conn.connect( N, dissortK,
				x -> this.attractorBroker.next( x[0] ) != this.attractorBroker
						.next( x[1] ),
				x -> this.hhAttributes.getAsBigDecimal( x[0] % A,
						HHAttribute.IMPRESSION_OUTPEER_WEIGHT.ordinal() ) );

		// create the social network (between households/parents)
		LongStream.range( A, this.hhAttributes.getRowCount() ).forEach( i ->
		{
			this.hhAttributes.setAsBoolean( this.schoolAssortativity.draw(), i,
					HHAttribute.SCHOOL_ASSORTATIVITY.ordinal() );

			final int aOwn = (int) (i % A);
			final long ia = (i - A) / A; // i within assortative sub-group

			final boolean log = (i - A) % (N / 5) == 0;
			if( A < 2 || assortativity >= 1 )
			{
				final AtomicReference<BigDecimal> totalW = new AtomicReference<>(
						BigDecimal.ZERO );
				final long[] inpeers = SocialConnector
						.availablePeers( assorting[aOwn], ia ).map( ja ->
						{
							final BigDecimal w = SocialConnector
									.getSymmetric( assorting[aOwn], ia, ja );
							totalW.getAndUpdate( bd -> bd.add( w ) );
							final long j = A + A * ja + aOwn;
							SocialConnector.setSymmetric( this.hhNetwork, w, i,
									j );
							return j;
						} ).toArray();
				if( log )
					LOG.trace( "hh #{} ({}/{} -> {}) in-peers: {} = {}/{}", i,
							ia, Na, this.attractorNames[aOwn], inpeers,
							inpeers.length, K + 1 );

				final BigDecimal inpeerW = totalW.get(),
						selfW = inpeerW.multiply(
								this.hhAttributes.getAsBigDecimal( aOwn,
										HHAttribute.IMPRESSION_SELF_MULTIPLIER
												.ordinal() ) ),
						attrW = inpeerW.multiply(
								this.hhAttributes.getAsBigDecimal( aOwn,
										HHAttribute.IMPRESSION_ATTRACTOR_MULTIPLIER
												.ordinal() ) );
				// set stubbornness
//				HHConnector.setSymmetric( this.hhNetwork, selfW, i );
//				this.hhNetwork.setAsBigDecimal( attrW, i, aOwn );
				this.hhAttributes.setAsBigDecimal( inpeerW, i,
						HHAttribute.IMPRESSION_INPEER_WEIGHT.ordinal() );
//				this.hhAttributes.setAsBigDecimal( BigDecimal.ZERO, A + i,
//						HHAttribute.SOCIAL_IMPACT_OUTPEER.ordinal() );
				this.hhAttributes.setAsBigDecimal( selfW, i,
						HHAttribute.IMPRESSION_SELF_MULTIPLIER.ordinal() );
				this.hhAttributes.setAsBigDecimal( attrW, i,
						HHAttribute.IMPRESSION_ATTRACTOR_MULTIPLIER.ordinal() );
				this.hhAttributes.setAsInt( inpeers.length, i,
						HHAttribute.SOCIAL_NETWORK_SIZE.ordinal() );
				this.hhAttributes.setAsBigDecimal( BigDecimal.ONE, i,
						HHAttribute.SOCIAL_ASSORTATIVITY.ordinal() );
			} else
			{
				// get separate assort + dissort j's just for logging
				final AtomicReference<BigDecimal> totalAssortW = new AtomicReference<>(
						BigDecimal.ZERO ),
						totalDissortW = new AtomicReference<>(
								BigDecimal.ZERO );
				final long[] inpeers = SocialConnector
						.availablePeers( assorting[aOwn], ia )
						.filter( ja -> ja * A + aOwn < N ) // skip if >N
						.map( ja ->
						{
							final BigDecimal w = SocialConnector
									.getSymmetric( assorting[aOwn], ia, ja );
							totalAssortW.getAndUpdate( bd -> bd.add( w ) );
							final long j = A + A * ja + aOwn;
							SocialConnector.setSymmetric( this.hhNetwork, w, i,
									j );
							return j;
						} ).toArray();
				// TODO don't copy, but initialize with outpeers already
				final long[] outpeers = SocialConnector
						.availablePeers( dissorting, i ).map( j ->
						{
							final BigDecimal w = SocialConnector
									.getSymmetric( dissorting, i, j );
							totalDissortW.getAndUpdate( bd -> bd.add( w ) );
							SocialConnector.setSymmetric( this.hhNetwork, w, i,
									j );
							return j;
						} ).toArray();
				final int peerTotal = inpeers.length + outpeers.length;
				final long[] stored = contacts( i );
				final List<Long> peers = Stream.of( inpeers, outpeers )
						.flatMap( ll -> Arrays.stream( ll ).mapToObj( l -> l ) )
						.sorted().collect( Collectors.toList() );
				final String[] diff = Arrays.stream( stored )
						.filter( l -> !peers.contains( l ) )
						.mapToObj( l -> "" + l + "\\" + (l % A)
								+ "\\" + SocialConnector
										.getSymmetric( this.hhNetwork, i, l ) )
						.toArray( String[]::new );
				if( peerTotal == 0 || peerTotal != stored.length ) LOG.warn(
						"hh #{} ({}/{} -> {}) peers: in {}({}/{}) "
								+ "+ out {}({}/{}) = {}/{}, added {}: {}",
						i, ia, Na, this.attractorNames[aOwn], inpeers,
						inpeers.length,
						DecimalUtil.toScale( assortativity * K, 1 ), outpeers,
						outpeers.length,
						DecimalUtil.toScale( dissortativity * K, 1 ), peerTotal,
						K, diff.length, diff );
				final BigDecimal inpeerW = totalAssortW.get(),
						outpeerW = totalDissortW.get(),
						selfW = inpeerW.add( outpeerW ).multiply(
								this.hhAttributes.getAsBigDecimal( aOwn,
										HHAttribute.IMPRESSION_SELF_MULTIPLIER
												.ordinal() ) ),
						attrW = inpeerW.add( outpeerW ).multiply(
								this.hhAttributes.getAsBigDecimal( aOwn,
										HHAttribute.IMPRESSION_ATTRACTOR_MULTIPLIER
												.ordinal() ) );
//				HHConnector.setSymmetric( this.hhNetwork, selfW, i );
//				this.hhNetwork.setAsBigDecimal( attrW, i, aOwn );

				this.hhAttributes.setAsBigDecimal( inpeerW, i,
						HHAttribute.IMPRESSION_INPEER_WEIGHT.ordinal() );
				this.hhAttributes.setAsBigDecimal( outpeerW, i,
						HHAttribute.IMPRESSION_OUTPEER_WEIGHT.ordinal() );
				this.hhAttributes.setAsBigDecimal( selfW, i,
						HHAttribute.IMPRESSION_SELF_MULTIPLIER.ordinal() );
				this.hhAttributes.setAsBigDecimal( attrW, i,
						HHAttribute.IMPRESSION_ATTRACTOR_MULTIPLIER.ordinal() );
				this.hhAttributes.setAsInt( peerTotal, i,
						HHAttribute.SOCIAL_NETWORK_SIZE.ordinal() );
				if( peerTotal != 0 ) this.hhAttributes.setAsBigDecimal(
						DecimalUtil.divide( inpeers.length, peerTotal ), i,
						HHAttribute.SOCIAL_ASSORTATIVITY.ordinal() );
			}

		} );

		LOG.info( "Networked, model: {}, degree: {}, beta: {}, assort: {}",
				SocialConnector.WattsStrogatz.class.getSimpleName(), K,
				socialBeta, assortativity );

		// show final links sample
//		LongStream.range( 0, 10 ).map( i -> i * N / 10 ).forEach( i ->
//		{
//			final Matrix row = this.hhNetwork.selectRows( Ret.LINK, A + i );
//			final int attr = this.hhAttributes.getAsInt( i,
//					HHAttribute.ATTRACTOR_REF.ordinal() );
//			final long[] assort = StreamSupport
//					.stream( row.availableCoordinates().spliterator(), false )
//					.mapToLong( x -> x[colDim] )
//					.filter( j -> this.hhAttributes.getAsInt( j,
//							HHAttribute.ATTRACTOR_REF.ordinal() ) == attr )
//					.toArray();
//			final long[] dissort = StreamSupport
//					.stream( row.availableCoordinates().spliterator(), false )
//					.mapToLong( x -> x[colDim] )
//					.filter( j -> this.hhAttributes.getAsInt( j,
//							HHAttribute.ATTRACTOR_REF.ordinal() ) != attr )
//					.toArray();
//			LOG.trace( "hh {} knows {}+{} ({}<>{}): {}+{}", i, assort.length,
//					dissort.length,
//					(double) assort.length / (assort.length + dissort.length),
//					assortativity, assort, dissort );
//		} );

		this.attitudeEvaluator = this.config.attitudeEvaluatorType()
				.newInstance();
		this.attitudePropagator = this.config.attitudePropagatorType()
				.newInstance();

		atEach( this.config.attitudePropagatorRecurrence( scheduler() ),
				this::propagate );

		atEach( this.config.vaccinationRecurrence( scheduler() ),
				this::vaccinate );

		// TODO add expressingRefs from own / neighboring / global placeRef dist

		// final Pathogen measles = this.pathogens.create( "MV-1" );

	}

	class LocalPressure
	{
		final NavigableMap<Double, Long> susceptibles = new TreeMap<>();
		final List<Long> infectious = new ArrayList<>();
		final List<Long> recovered = new ArrayList<>();
		final String name;
		int populationSize = 0;
		double pressure = 0d;
		Expectation next = null, pendingAdjourn = null;

		LocalPressure( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + JsonUtil.stringify( this );
		}

		boolean noPressure()
		{
			return this.infectious.isEmpty() || this.susceptibles.isEmpty();
		}

		double pressure()
		{
			return ((double) this.infectious.size()) / this.populationSize;
		}

		double infectionDelayDays( final double resistanceGap )
		{
			return noPressure() ? Double.NaN
					: resistanceGap / pressure() / beta;
		}

		LocalPressure arrive( final Long i )
		{
			this.populationSize++;
			switch( getStatus( i ) )
			{
			case SUSCEPTIBLE:
				this.susceptibles.put( getResistance( i ), i );
				break;
			case INFECTIOUS:
				this.infectious.add( i );
				break;
			default:
				this.recovered.add( i );
				break;
			}
			rescheduleInfect();
			return this;
		}

		LocalPressure depart( final Long i )
		{
			if( this.susceptibles.remove( getResistance( i ), i )
					|| this.infectious.remove( i )
					|| this.recovered.remove( i ) )
			{
				this.populationSize--;
				rescheduleInfect();
			} else
				LOG.warn( "t={} @{} Could not remove #{}, SIR: {}+{}+{}={}",
						prettyDate( now() ), this.name, i,
						this.susceptibles.size(), this.infectious.size(),
						this.recovered.size(), this.populationSize );
			return this;
		}

		boolean isAdjourned()
		{
			return this.pendingAdjourn == null;
		}

		void unscheduleInfect()
		{
			if( this.next == null ) return;

			if( this.next.unwrap() != now() ) this.next.remove();
			this.next = null;
		}

		void rescheduleInfect()
		{
			// cancel anything pending
			unscheduleInfect();

			if( isAdjourned() || noPressure() ) return;

			final double r0 = this.susceptibles.firstKey(),
					dr = r0 - this.pressure, dt = infectionDelayDays( dr );
			if( dt > 3 ) return; // skip beyond weekend for now, to avoid re-rescheduling
			this.next = after( dt, TimeUnits.DAYS ).call( tI ->
			{
				if( noPressure() ) return;

				infectNext( r0 );
				rescheduleInfect();
			} );
		}

		void infectNext( final double r0 )
		{
			// TODO push transmission event?
			final Map.Entry<Double, Long> first = this.susceptibles
					.firstEntry();
			this.pressure = r0;
			final Long i = first.getValue();
			final Map<String, int[]> localSIR = setInfected( i,
					first.getKey() );
			LOG.trace(
					"t={} INFECTED #{} @{}, resistance={}, pressure={}; SIR={}",
					prettyDate( now() ), i, this.name, first.getKey(),
					this.pressure, localSIR );

			final double recoverDays = recoveryDaysDist.draw();
			after( recoverDays, TimeUnits.DAYS ).call( t -> setRecovered( i ) );
		}

		private int[] getSIR()
		{
			return new int[] { this.susceptibles.size(), this.infectious.size(),
					this.recovered.size() };
		}

		LocalPressure conveneOn( final Observable<Quantity<Time>> summonings,
			final Consumer<Long> summoner, final Consumer<Long> adjourner )
		{
			summonings.subscribe( dt -> convene( dt, summoner, adjourner ),
					PilotScenario.this::logError );
			return this;
		}

		Stream<Long> streamAll()
		{
			return Arrays
					.asList( this.susceptibles.values().stream(),
							this.infectious.stream(), this.recovered.stream() )
					.stream().flatMap( s -> s );
		}

		LocalPressure convene( final Quantity<Time> dt,
			final Consumer<Long> summoner, final Consumer<Long> adjourner )
		{
			if( summoner != null ) streamAll().forEach( summoner );

			// schedule adjourn
			this.pendingAdjourn = after( dt ).call( t -> adjourn( adjourner ) );

			// schedule infection events
			if( noPressure() )
			{
				if( this.pressure > 0 )
					LOG.trace( "t={} @{} convened for {}, SIR: {}+{}+{}",
							prettyDate( now() ), this.name, dt,
							this.susceptibles.size(), this.infectious.size(),
							this.recovered.size() );
			} else
			{
				LOG.trace( "t={} @{} pressured for {}, SIR: {}+{}+{}",
						prettyDate( now() ), this.name, dt,
						this.susceptibles.size(), this.infectious.size(),
						this.recovered.size() );

				resist( QuantityUtil.decimalValue( dt, TimeUnits.DAYS )
						.doubleValue() );
			}
			return this;
		}

		void adjourn( final Consumer<Long> adjourner )
		{
			LOG.trace( "t={} @{} adjourned, SIR: {}+{}+{}", prettyDate( now() ),
					this.name, this.susceptibles.size(), this.infectious.size(),
					this.recovered.size() );
			unscheduleInfect();
			if( this.pendingAdjourn != null
					&& this.pendingAdjourn.unwrap().compareTo( now() ) > 0 )
				this.pendingAdjourn.remove();
			this.pendingAdjourn = null;
			if( adjourner != null ) streamAll().forEach( adjourner );
		}

		void resist( final double daysUnderPressure )
		{
			if( noPressure() ) return;
			final double dr = this.susceptibles.firstKey() - this.pressure,
					infectionDelayDays = infectionDelayDays( dr );
			if( daysUnderPressure < infectionDelayDays )
			{
				// infection skips this meeting, update local pressure anyway
				this.pendingAdjourn = after( daysUnderPressure, TimeUnits.DAYS )
						.call( t ->
						{
							this.pressure += daysUnderPressure * beta
									* pressure();
//					LOG.trace( "t={} @{} adjourn: {}", prettyDate( now() ),
//							this.motorNames[motorIndex], adjourned );
						} );
			} else
			{
//				LOG.trace( "t={} @{} pressure: {}, S->I", prettyDate( now() ),
//						this.motorNames[motorIndex], local.pressure() );
				this.next = after( infectionDelayDays, TimeUnits.DAYS )
						.call( tI ->
						{
							infectNext( dr );
							resist( daysUnderPressure - infectionDelayDays );
						} );
			}
		}
	}

//	private double globalPressure = 0d;

	// S -> I
	private Map<String, int[]> setInfected( final Long i,
		final Double resistance )
	{
		setStatus( i, HHMemberStatus.INFECTIOUS );
		final Map<String, int[]> localSIR = new HashMap<>();
		motorsFor( i ).forEach( local ->
		{
			localSIR.put(
					local.name + (local.isAdjourned() ? "-"
							: "+pres:"
									+ DecimalUtil.toScale( local.pressure, 3 )),
					local.getSIR() );
			local.susceptibles.remove( resistance );
			local.infectious.add( i );
		} );
		publishSIR( -1, 1 );
		return localSIR;
	}

	// S -> R
	private void setVaccinated( final Long i )
	{
		final Double resistance = getResistance( i );
		setStatus( i, HHMemberStatus.ARTIFICIAL_IMMUNE );
		motorsFor( i ).forEach( local ->
		{
			local.susceptibles.remove( resistance );
			local.recovered.add( i );
		} );
		publishSIR( -1, 0, 0, 1 );
	}

	// I -> R
	private void setRecovered( final Long i )
	{
		setStatus( i, HHMemberStatus.NATURAL_IMMUNE );
		motorsFor( i ).forEach( local ->
		{
			LOG.trace( "t={} RECOVERED #{} @{}", prettyDate( now() ), i,
					local.name );
			local.infectious.remove( i );
			local.recovered.add( i );
		} );
		publishSIR( 0, -1, 1 );
	}

	private Double getResistance( final long i )
	{
		return this.ppAttributes.getAsDouble( i,
				HHMemberAttribute.RESISTANCE.ordinal() );
	}

	private HHMemberStatus getStatus( final long i )
	{
		return HHMemberStatus.values()[this.ppAttributes.getAsInt( i,
				HHMemberAttribute.STATUS.ordinal() )];
	}

	private void setStatus( final long i, final HHMemberStatus status )
	{
		this.ppAttributes.setAsInt( status.ordinal(), i,
				HHMemberAttribute.STATUS.ordinal() );
	}

	private void publishSIR( final long... delta )
	{
		final long[] old = this.sirTransitions.getValue(), sir = Arrays
				.copyOf( delta, old == null ? delta.length : old.length );
		if( old != null ) for( int i = old.length; --i != -1; )
			sir[i] += old[i];
		this.sirTransitions.onNext( sir );
	}

	private final BehaviorSubject<long[]> sirTransitions = BehaviorSubject
			.create();

	public Observable<long[]> sirTransitions()
	{
		return this.sirTransitions;
	}

	private Subject<PropertyChangeEvent> networkEvents = PublishSubject
			.create();

	public Observable<PropertyChangeEvent> network()
	{
		return this.networkEvents;
	}

	public Number seed()
	{
		return this.distFactory.getStream().seed();
	}

	public Observable<HHStatisticsDao> statistics()
	{
//		final UUID contextRef = this.binder.id().contextRef();
		return Observable.create( sub ->
		{
			final PilotConfigDao cfg = PilotConfigDao.create( this.binder.id(),
					this.config, seed() );
			scheduler().onReset( scheduler ->
			{
				scheduler.atEnd( t -> sub.onComplete(), sub::onError );
				final Iterable<Instant> when;
				try
				{
					when = this.config.statisticsRecurrence( scheduler() );
				} catch( final ParseException e )
				{
					sub.onError( e );
					return;
				}
				scheduler.atEach( when, t ->
				{
					final int s = this.statsIteration.getAndIncrement();
					LOG.debug( "t={}, exporting statistics #{}",
							prettyDate( t ), s );
					final Matrix hhAttributes = this.hhAttributes.clone();
					final Matrix ppAttributes = this.ppAttributes.clone();

					LongStream.range( 0, this.hhAttributes.getRowCount() )
							.mapToObj( i ->
							{
								final Map<Long, Integer> activity;
								if( i < this.attractorNames.length )
									activity = Collections.emptyMap();
								else
								{
									activity = SocialConnector
											.availablePeers( this.hhNetwork, i )
											.mapToObj( j -> j )
											.collect( Collectors.toMap( j -> j,
													j -> this.hhNetwork//Activity
															.getAsInt(
																	SocialConnector
																			.rowLargest(
																					i,
																					j ) ) ) );
									final int size = this.hhAttributes.getAsInt(
											i, HHAttribute.SOCIAL_NETWORK_SIZE
													.ordinal() );
									if( activity.size() != size ) LOG.warn(
											"Unexpected network size {}, expected {} for hh: {}",
											activity.size(), size, i );
								}
								return HHStatisticsDao.create( cfg, i, t, s,
										this.attractorNames, hhAttributes,
										ppAttributes, activity,
										this.attitudeEvaluator );
							} ).forEach( sub::onNext );
				} );
			} );
		} );
	}

	private void pushChangedAttributes( final long i )
	{
		if( this.networkEvents.hasObservers() ) this.networkEvents.onNext(
				new PropertyChangeEvent( i, "hh" + i, null, HHAttribute.toMap(
						k -> this.hhAttributes.getAsBigDecimal( i, k ),
						HHAttribute.CONFIDENCE, HHAttribute.COMPLACENCY ) ) );
	}

	private BigDecimal lastPropagationInstantDays = null;

	private void propagate( final Instant t )
	{
		LOG.debug( "t={}, propagating...", prettyDate( t ) );

		final BigDecimal nowDays = t.to( TimeUnits.DAYS ).decimal(),
				propagateDays = this.lastPropagationInstantDays == null
						? nowDays
						: nowDays.subtract( this.lastPropagationInstantDays );
		this.lastPropagationInstantDays = nowDays;

		final Matrix interactions = SparseMatrix.Factory
				.zeros( this.hhNetwork.getSize() );
		LongStream.range( this.attractors.size(), this.hhNetwork.getRowCount() )
				//.parallel() 
				// NOTE DefaultSparseGenericMatrix (HashMap) not concurrent
				.forEach( i ->
				{
					final long[] J = contacts( i );
					if( J.length == 0 ) return;
					final BigDecimal days = this.hhAttributes.getAsBigDecimal(
							i, HHAttribute.IMPRESSION_PERIOD_DAYS.ordinal() );
					final ProbabilityDistribution<Long> binom = this.distFactory
							.createBinomial(
									DecimalUtil.divide( propagateDays, days ),
									DecimalUtil.inverse( J.length ) );
					for( int j = J.length; j-- != 0; )
						if( binom.draw() > 0 )
						{
							final long[] x = { i, J[j] };
							SocialConnector.setSymmetric( interactions,
									SocialConnector.getSymmetric(
											this.hhNetwork, x ),
									x );
						}
				} );
		final Map<Long, Integer> changed = this.attitudePropagator
				.propagate( interactions, this.hhAttributes );
		changed.forEach( ( i, n ) ->
		{
			pushChangedAttributes( i );
			final long[] y = { i, HHAttribute.IMPRESSION_FEEDS.ordinal() };
			this.hhAttributes.setAsInt( this.hhAttributes.getAsInt( y ) + n,
					y );
		} );
		LongStream.range( this.attractors.size(), this.hhNetwork.getRowCount() )
				.forEach( i ->
				{
//					impressInitial( i,
//							QuantityUtil.valueOf(
//									this.hhAttributes.getAsBigDecimal( i,
//											HHAttribute.IMPRESSION_PERIOD_DAYS
//													.ordinal() ),
//									TimeUnits.DAYS ) );
					final long[] x = { i,
					HHAttribute.IMPRESSION_ROUNDS.ordinal() };
					this.hhAttributes
							.setAsInt( this.hhAttributes.getAsInt( x ) + 1, x );
				} );
	}

	private void vaccinate( final Instant t )
	{
		final VaxOccasion occ = this.vaxOccasionDist.draw();
		final Range<BigDecimal> birthRange = this.vaccinationAge.map(
				age -> t.subtract( age ).to( TimeUnits.ANNUM ).decimal() );
		LOG.debug( "t={}, vaccination occasion: {} for susceptibles born {}",
				prettyDate( t ), occ.asMap().values(),
				birthRange.map(
						age -> prettyDate( Instant.of( age, TimeUnits.ANNUM ) )
								.toString() ) );

		// for each households evaluated with a positive attitude
		final List<Long> vax =

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

				LongStream.range( 0, this.ppAttributes.getRowCount() ).filter(
						i -> getStatus( i ) == HHMemberStatus.SUSCEPTIBLE )
						.filter( i -> this.distFactory.getStream()
								.nextFloat() < .96f )

						// vaccinate!
						.mapToObj( i -> i ).collect( Collectors.toList() );
		vax.stream().forEach( this::setVaccinated );
		LOG.trace( "t={} VACCINATED {} ppl.", prettyDate( now() ), vax.size() );

//		LOG.debug(
//		"t={}, Vax! (pos) hh #{} (sus) pp #{} born {}",
//		prettyDate( t ), hh, ppRef,
//		prettyDate( Instant.of(
//				this.ppAttributes
//						.getAsBigDecimal( ppRef,
//								HHMemberAttribute.BIRTH
//										.ordinal() ),
//				TimeUnits.ANNUM ) ) );
	}

	private void migrateHousehold( final Instant t )
	{
		final long A = this.attractors.size(),
				N = this.hhAttributes.getRowCount() - A,
				i = A + this.distFactory.getStream().nextLong( N );
		createHousehold( i );

		final Quantity<Time> dt = this.hhMigrateDist.draw();
		LOG.debug( "t={}, replace migrant #{}, next after: {}", prettyDate( t ),
				i, QuantityUtil.toScale( dt, 1 ) );

		after( dt ).call( this::migrateHousehold );
	}

	private int createHousehold( final long oldIndex )
	{
		final long id = this.hhCount.getAndIncrement();
		final long hhIndex;
		if( oldIndex == NA )
		{
			hhIndex = this.attractors.size() + this.hhIndex.computeIfAbsent( id,
					key -> (long) this.hhIndex.size() );
		} else
		{
			hhIndex = oldIndex;
			this.hhIndex.remove( this.hhAttributes.getAsLong( hhIndex,
					HHAttribute.IDENTIFIER.ordinal() ) );
		}

//		final Cbs71486json.Category hhCat = this.localHouseholdDist.draw( dt() );
		final int attractorRef =
//				Region.ID.of( hhCat.regionRef() );
				this.attractorBroker.next( hhIndex );

		final HHAttractor attractor = this.attractors
				.get( this.attractorNames[attractorRef] );
		final HesitancyProfileJson profile = this.hesitancyProfileDist
				.draw( attractor.toHesitancyProfile() );

		final CBSHousehold hhType =
//		hhCat.hhTypeDist( this.distFactory::createCategorical ).draw();
				this.hhTypeDist.draw();

		final Quantity<Time> duration = this.scheduler.config().duration()
				.toQuantity().add( QuantityUtil.valueOf( 1, TimeUnits.DAYS ) );
		final LocalPressure home = new LocalPressure( "hh" + hhIndex ).convene(
				duration, i -> LOG.trace( "{} moves to hh {}", i, hhIndex ),
				i -> LOG.trace( "{} leaves hh {}", i, hhIndex ) );
		final long referentRef = createPerson( oldIndex, hhIndex,
				HHAttribute.REFERENT_REF, profile.status, home );

		createPerson( oldIndex, hhIndex, HHAttribute.PARTNER_REF,
				hhType.adultCount() < 2 ? null : profile.status, home );
		createPerson( oldIndex, hhIndex, HHAttribute.CHILD1_REF,
				hhType.childCount() < 1 ? null : profile.status, home );
		createPerson( oldIndex, hhIndex, HHAttribute.CHILD2_REF,
				hhType.childCount() < 2 ? null : profile.status, home );
		createPerson( oldIndex, hhIndex, HHAttribute.CHILD3_REF,
				hhType.childCount() < 3 ? null : profile.status, home );

		final BigDecimal initialCalculation = this.calculationDist.draw();
		final Map<HHAttribute, BigDecimal> initialHesitancy = this.hesitancyDist
				.draw( profile );

		final Quantity<Time> impressDelay = Arrays
				.stream( RelationFrequencyJson.Relation.values() )
				.map( r -> this.hhImpressIntervalDist
						.draw( new RelationFrequencyJson.Category(
								this.ppAttributes.getAsBoolean( referentRef,
										HHMemberAttribute.MALE.ordinal() ),
								r,
								now().to( TimeUnits.ANNUM )
										.subtract( this.ppAttributes
												.getAsBigDecimal( referentRef,
														HHMemberAttribute.BIRTH
																.ordinal() ) )
										.toQuantity( TimeUnits.ANNUM ) ) )
						.inverse().asType( Frequency.class ) )
				.reduce( ( f1, f2 ) -> f1.add( f2 ) ).get().inverse()
				.asType( Time.class );

		// set household attribute values
		this.hhAttributes.setAsLong( id, hhIndex,
				HHAttribute.IDENTIFIER.ordinal() );
		this.hhAttributes.setAsBigDecimal( now().to( TimeUnits.DAYS ).decimal(),
				hhIndex, HHAttribute.SINCE_DAYS.ordinal() );
		this.hhAttributes.setAsInt( 0, hhIndex,
				HHAttribute.IMPRESSION_ROUNDS.ordinal() );
		this.hhAttributes.setAsInt( attractorRef, hhIndex,
				HHAttribute.ATTRACTOR_REF.ordinal() );
		this.hhAttributes.setAsBigDecimal(
				QuantityUtil.decimalValue( impressDelay, TimeUnits.DAYS ),
				hhIndex, HHAttribute.IMPRESSION_PERIOD_DAYS.ordinal() );
		this.hhAttributes.setAsInt( 0, hhIndex,
				HHAttribute.IMPRESSION_FEEDS.ordinal() );
		this.hhAttributes.setAsBigDecimal( initialCalculation, hhIndex,
				HHAttribute.CALCULATION.ordinal() );
		this.hhAttributes.setAsBigDecimal(
				initialHesitancy.get( HHAttribute.CONFIDENCE ), hhIndex,
				HHAttribute.CONFIDENCE.ordinal() );
		this.hhAttributes.setAsBigDecimal(
				initialHesitancy.get( HHAttribute.COMPLACENCY ), hhIndex,
				HHAttribute.COMPLACENCY.ordinal() );

		// FIXME put each home leaver in a new household
//		after( this.hhLeaveHomeAge.subtract( child1Age ) ).call( t ->
//		{
//			LOG.debug( "t={}, replace home leaver #{}", prettyDate( t ),
//					hhIndex );
//			createHousehold( hhIndex );
//		} );

		return hhType.size();
	}

	private long[] contacts( final long i )
	{
		return SocialConnector.availablePeers( this.hhNetwork, i ).parallel()
				.toArray();
	}

	private Stream<LocalPressure> motorsFor( final long i )
	{
		final long flags = this.ppAttributes.getAsLong( i,
				HHMemberAttribute.MOTOR_REF.ordinal() );
		return Arrays
				.asList( Stream.of( this.ppHome.get( i ) ),
						IntStream.range( 0, this.motorPressure.length )
								.filter( m -> (flags & (1 << m)) != 0 )
								.mapToObj( m -> this.motorPressure[m] ) )
				.stream().flatMap( s -> s );
	}

	private long createPerson( final long oldIndex, final long hhIndex,
		final HHAttribute hhPositionRef, final VaccineStatus vaxStatus,
		final LocalPressure home )
	{
		if( vaxStatus == null )
		{
			this.hhAttributes.setAsLong( NA, hhIndex, hhPositionRef.ordinal() );
			return NA;
		}
		final long id = this.persons.incrementAndGet();
		final long index;
		if( oldIndex == NA )
		{
			index = this.ppIndex.computeIfAbsent( id,
					key -> (long) this.ppIndex.size() );
			this.hhAttributes.setAsLong( index, hhIndex,
					hhPositionRef.ordinal() );
		} else
		{
			index = this.hhAttributes.getAsLong( hhIndex,
					hhPositionRef.ordinal() );
			this.ppIndex.remove( this.ppAttributes.getAsLong( index,
					HHMemberAttribute.IDENTIFIER.ordinal() ) );

			// unregister hh-members from local pressure motors 
			motorsFor( index ).forEach( local -> local.depart( index ) );
		}

		final boolean male = this.hhRefMaleDist.draw();
		final Quantity<Time> nowYear = now().toQuantity( TimeUnits.ANNUM ), age;
		if( hhPositionRef == HHAttribute.REFERENT_REF )
		{
			age = this.hhRefAgeDist.draw();
			// FIXME hhCat.ageDist( this.distFactory::createUniformContinuous ).draw();
		} else
		{
			final int ageDiff;
			switch( hhPositionRef )
			{
			// TODO from distribution, e.g. 60036ned, 37201
			case PARTNER_REF:
				ageDiff = 3;
				break;
			case CHILD1_REF:
				ageDiff = 20;
				break;
			case CHILD2_REF:
				ageDiff = 22;
				break;
			case CHILD3_REF:
			default:
				ageDiff = 24;
				break;
			}
			final long referentRef = this.hhAttributes.getAsLong( hhIndex,
					HHAttribute.REFERENT_REF.ordinal() );
			final BigDecimal referentBirth = this.ppAttributes.getAsBigDecimal(
					referentRef, HHMemberAttribute.BIRTH.ordinal() );
			age = nowYear.subtract( QuantityUtil.valueOf(
					referentBirth.add( BigDecimal.valueOf( ageDiff ) ),
					TimeUnits.ANNUM ) );
		}

		final HHMemberStatus status = oldIndex == NA // newborn
				|| vaxStatus == VaccineStatus.none
				|| this.vaccinationAge.lowerValue()
						.isGreaterThanOrEqualTo( age )
								? HHMemberStatus.SUSCEPTIBLE
								: HHMemberStatus.ARTIFICIAL_IMMUNE;

		this.ppAttributes.setAsLong( id, index,
				HHMemberAttribute.IDENTIFIER.ordinal() );
		this.ppAttributes.setAsDouble(
				nowYear.subtract( age ).getValue().doubleValue(), index,
				HHMemberAttribute.BIRTH.ordinal() );
		this.ppAttributes.setAsBoolean( male, index,
				HHMemberAttribute.MALE.ordinal() );
		this.ppAttributes.setAsInt( status.ordinal(), index,
				HHMemberAttribute.STATUS.ordinal() );

		this.ppAttributes.setAsDouble( this.resistanceDist.draw(), index,
				HHMemberAttribute.RESISTANCE.ordinal() );

		// TODO individual localization values from config/dists
		final int flags = (1 << this.motors.size() + 1) - 1;
		this.ppAttributes.setAsLong( flags, index,
				HHMemberAttribute.MOTOR_REF.ordinal() );

		this.ppHome.put( index, home );

		// register at all pressure locations/motors 
		motorsFor( index ).forEach( local -> local.arrive( index ) );

		return index;
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	protected Pretty prettyDate( final Instant t )
	{
		return Pretty.of( () ->
		{
			final ZonedDateTime zdt = scheduler().offset().plus(
					t.to( TimeUnits.MINUTE ).value().longValue(),
					ChronoUnit.MINUTES );
			return QuantityUtil.toScale( t.toQuantity( TimeUnits.DAYS ), 1 )
					+ ";" + zdt.toLocalDateTime() + ";"
					+ zdt.format( DateTimeFormatter.ISO_WEEK_DATE );
		} );
	}

	protected LocalDate dt()
	{
		// FIXME fix daylight savings adjustment, seems to adjust the wrong way
		return now().equals( this.dtInstant ) ? this.dtCache
				: (this.dtCache = (this.dtInstant = now())
						.toJava8( scheduler().offset().toLocalDate() ));
	}

	private void logError( final Throwable e )
	{
		LOG.error( "Problem", e );
	}
}