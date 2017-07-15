package nl.rivm.cib.pilot;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.InjectConfig;
import io.coala.bind.InjectConfig.Scope;
import io.coala.bind.LocalBinder;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.DecimalUtil;
import io.coala.math.MatrixUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.random.QuantityDistribution;
import io.coala.time.Duration;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.epidemes.cbs.json.CBSGender;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;
import nl.rivm.cib.pilot.dao.HHStatisticsDao;
import nl.rivm.cib.pilot.dao.PilotConfigDao;
import nl.rivm.cib.pilot.hh.HHAttitudeEvaluator;
import nl.rivm.cib.pilot.hh.HHAttitudePropagator;
import nl.rivm.cib.pilot.hh.HHAttractor;
import nl.rivm.cib.pilot.hh.HHAttractor.Broker;
import nl.rivm.cib.pilot.hh.HHAttribute;
import nl.rivm.cib.pilot.hh.HHMemberAttribute;
import nl.rivm.cib.pilot.hh.HHMemberMotor;
import nl.rivm.cib.pilot.hh.HHMemberStatus;
import nl.rivm.cib.pilot.json.HesitancyProfileJson;
import nl.rivm.cib.pilot.json.HesitancyProfileJson.VaccineStatus;
import nl.rivm.cib.pilot.json.RelationFrequencyJson;
import tec.uom.se.ComparableQuantity;

/**
 * {@link PilotScenario} is a simple example {@link Scenario} implementation, of
 * which only one {@link Singleton} instance exists per {@link LocalBinder}
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

	/** */
	private static final HHAttribute[] CHILD_REF_COLUMN_INDICES = {
			// HHAttribute.REFERENT_REF,
			// HHAttribute.PARTNER_REF,
			HHAttribute.CHILD1_REF
//			, HHAttribute.CHILD2_REF
//			, HHAttribute.CHILD3_REF 
	};

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
	private Quantity<Time> hhLeaveHomeAge;

	/** */
	private Matrix ppAttributes;
	/** */
	private final AtomicLong persons = new AtomicLong();

	/** */
	private Range<ComparableQuantity<Time>> vaccinationAge;
	/** */
	private transient ProbabilityDistribution<VaxOccasion> vaxOccasionDist;

	/** */
	private Matrix hhNetwork;
	/** */
	private Map<String, HHAttractor> attractors = Collections.emptyMap();
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

	/** number of top rows (0..n) in {@link #hhNetwork} reserved for oracles */
//	private long attractorCount;
	/** virtual time range of simulation */
//	private transient Range<LocalDate> timeRange = null;
	/** empirical household compositions and referent ages, see CBS 71486 */
//	private transient ConditionalDistribution<Cbs71486json.Category, LocalDate> localHouseholdDist;
	/** zip-codes per borough / ward / municipality / ... */
//	private transient ConditionalDistribution<CbsNeighborhood, Region.ID> hoodDist;

	/** */
	private HHMemberMotor.Broker motorBroker;
	/** */
	private HHMemberMotor[] motors;
	/** */
	private Matrix motorPresence;
	/** */
	private Expectation[] motorAdjourns;

//	private transient ConditionalDistribution<Quantity<Time>, GenderAge> peerPressureIntervalDist;

	@Override
	public void init() throws ParseException, InstantiationException,
		IllegalAccessException, IOException, ClassNotFoundException
	{
		final PseudoRandom rng = this.distFactory.getStream();
		LOG.info( "seed: {}, offset: {}", rng.seed(), scheduler().offset() );

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
//		this.hhNetworkActivity = SparseMatrix.Factory.zeros( edges, edges );

		final JsonNode motorConfig = JsonUtil.getJOM().createObjectNode()
				.set( "work", JsonUtil.getJOM().createObjectNode()
						.put( HHMemberMotor.CONVENE_KEY, "0 0 9 ? * MON-FRI" )
						.put( HHMemberMotor.DURATION_KEY, "const(8 h)" ) );
		LOG.trace( "creating motors from: {}", motorConfig );
		final NavigableMap<String, HHMemberMotor> motors = this.binder
				.inject( HHMemberMotor.Factory.SimpleBinding.class )
				.createAll( motorConfig );
		this.motors = motors.values()
				.toArray( new HHMemberMotor[motors.size()] );
		this.motorAdjourns = new Expectation[motors.size()];
		IntStream.range( 0, this.motors.length ).forEach( m -> this.motors[m]
				.convene().subscribe( dt -> convene( dt, m ) ) );
		this.motorPresence = SparseMatrix.Factory.zeros( ppTotal,
				this.motors.length );
		this.motorBroker = ppIndex -> 0; // TODO expand to local/personal motor

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

		this.hhLeaveHomeAge = this.config.householdLeaveHomeAge();
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

		LOG.info( "Populated: {} pp ({}%) across {} hh in {} attractor/regions",
				this.persons.get(), this.persons.get() * 100 / ppTotal,
				this.hhCount.get() - this.attractors.size(),
				this.attractors.size() );

		final double beta = this.config.hesitancySocialNetworkBeta();
		final HHConnector conn = new HHConnector.WattsStrogatz( rng, beta );
		final long A = attractors.size(), N = this.hhCount.get() - A,
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
			final Matrix m = conn.connect( Na, assortK, x -> inpeerW,
					x -> true );
			return m;
		} ).toArray( Matrix[]::new );

		final Matrix dissorting = conn.connect( N, dissortK,
				x -> this.hhAttributes.getAsBigDecimal( x[0] % A,
						HHAttribute.IMPRESSION_OUTPEER_WEIGHT.ordinal() ),
				x -> this.attractorBroker.next( x[0] ) != this.attractorBroker
						.next( x[1] ) );

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
				final long[] inpeers = HHConnector
						.availablePeers( assorting[aOwn], ia ).map( ja ->
						{
							final BigDecimal w = HHConnector
									.getSymmetric( assorting[aOwn], ia, ja );
							totalW.getAndUpdate( bd -> bd.add( w ) );
							final long j = A + A * ja + aOwn;
							HHConnector.setSymmetric( this.hhNetwork, w, i, j );
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
				final long[] inpeers = HHConnector
						.availablePeers( assorting[aOwn], ia )
						.filter( ja -> ja * A + aOwn < N ) // skip if >N
						.map( ja ->
						{
							final BigDecimal w = HHConnector
									.getSymmetric( assorting[aOwn], ia, ja );
							totalAssortW.getAndUpdate( bd -> bd.add( w ) );
							final long j = A + A * ja + aOwn;
							HHConnector.setSymmetric( this.hhNetwork, w, i, j );
							return j;
						} ).toArray();
				// TODO don't copy, but initialize with outpeers already
				final long[] outpeers = HHConnector
						.availablePeers( dissorting, i ).map( j ->
						{
							final BigDecimal w = HHConnector
									.getSymmetric( dissorting, i, j );
							totalDissortW.getAndUpdate( bd -> bd.add( w ) );
							HHConnector.setSymmetric( this.hhNetwork, w, i, j );
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
								+ "\\" + HHConnector
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
				HHConnector.WattsStrogatz.class.getSimpleName(), K, beta,
				assortativity );

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

		atEach( this.config.attitudePropagatorRecurrence( scheduler() ) )
				.subscribe( this::propagate, this::logError );

		atEach( this.config.vaccinationRecurrence( scheduler() ) )
				.subscribe( this::vaccinate, this::logError );

		// TODO add expressingRefs from own / neighboring / global placeRef dist

		// final Pathogen measles = this.pathogens.create( "MV-1" );

	}

	private Subject<PropertyChangeEvent> networkEvents = PublishSubject
			.create();

	public Observable<PropertyChangeEvent> network()
	{
		return this.networkEvents;
	}

	private void convene( final Duration dt, final int m )
	{
		final int refCol = HHMemberAttribute.MOTOR_REF.ordinal();
		if( this.motorAdjourns[m] != null )
		{
			this.motorAdjourns[m].remove();
			this.motorAdjourns[m] = null;
		}
		final long[] adjourn = adjourn( m );
		final Iterator<long[]> mem = this.ppAttributes
				.selectColumns( Ret.LINK, refCol ).availableCoordinates()
				.iterator();
		if( mem.hasNext() )
		{
			final BigDecimal since = now().decimal();
			for( long[] x = mem.next(); mem.hasNext(); x = mem.next() )
				if( this.ppAttributes.getAsInt( x[0], refCol ) == m )
					this.motorPresence.setAsBigDecimal( since, x[0], m );
		}

		LOG.trace( "t={}, convene motor {}, adjourn @t+{}, kicked {}",
				prettyDate( now() ), m, dt, adjourn );
	}

	private long[] adjourn( final int m )
	{
		return MatrixUtil
				.coordinateStream(
						this.motorPresence.selectColumns( Ret.LINK, m ) )
				.mapToLong( x -> x[0] ).filter( p ->
				{
					final long[] x = { p, m };
					final BigDecimal since = this.motorPresence
							.getAsBigDecimal( x );
					this.motorPresence.setAsObject( null, x );
					if( since.signum() <= 0 ) return false;

					final long[] y = { p,
					HHMemberAttribute.SUSCEPTIBLE_DAYS.ordinal() };
					final BigDecimal days = this.ppAttributes
							.getAsBigDecimal( y )
							.subtract( now().to( TimeUnits.DAYS ).decimal()
									.subtract( since ) );
					this.ppAttributes.setAsBigDecimal( days, y );
					if( days.signum() < 1 )
					{
						LOG.trace( "EXPOSE person: {} at convention: {}", p,
								m );
						// f convening
						this.ppAttributes.setAsInt( -1, p,
								HHMemberAttribute.MOTOR_REF.ordinal() );
						// FIXME verify transition is: susceptible -> exposed
						this.ppAttributes.setAsInt(
								HHMemberStatus.EXPOSED.ordinal(), p,
								HHMemberAttribute.STATUS.ordinal() );
						// FIXME schedule infectious transition
					}
					return true;
				} ).toArray();
	}

	public Observable<HHStatisticsDao> statistics()
	{
//		final UUID contextRef = this.binder.id().contextRef();
		return Observable.create( sub ->
		{
			final PilotConfigDao cfg = PilotConfigDao.create( this.binder.id(),
					this.config );
			scheduler().onReset( scheduler ->
			{
				// TODO copy/move completion trigger to Scheduler
				scheduler.time().lastOrError().subscribe( t -> sub.onComplete(),
						sub::onError );
				final Iterable<Instant> when;
				try
				{
					when = this.config.statisticsRecurrence( scheduler() );
				} catch( final ParseException e )
				{
					sub.onError( e );
					return;
				}
				scheduler.atEach( when ).subscribe( t ->
				{
					final int s = this.statsIteration.getAndIncrement();
					LOG.debug( "t={}, exporting statistics #{}",
							prettyDate( t ), s );
					final Matrix hhAttributes = this.hhAttributes.clone();
					final Matrix ppAttributes = this.ppAttributes.clone();
					LongStream.range( 0, this.hhAttributes.getRowCount() )
							.mapToObj(
									i -> HHStatisticsDao.create( cfg, t, s,
											this.attractorNames,
											hhAttributes.selectRows( Ret.LINK,
													i ),
											ppAttributes ) )
							.forEach( sub::onNext );
				}, sub::onError, sub::onComplete );
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
				.parallel().forEach( i ->
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
							HHConnector.setSymmetric( interactions, HHConnector
									.getSymmetric( this.hhNetwork, x ), x );
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
		// TODO from config: vaccination call age
		final Range<BigDecimal> birthRange = this.vaccinationAge.map(
				age -> t.subtract( age ).to( TimeUnits.ANNUM ).decimal() );
		LOG.debug( "t={}, vaccination occasion: {} for susceptibles born {}",
				prettyDate( t ), occ.asMap().values(),
				birthRange.map(
						age -> prettyDate( Instant.of( age, TimeUnits.ANNUM ) )
								.toString() ) );

//		this.hhNetwork.setAsBigDecimal( BigDecimal.ONE, 0, 0 );
//		this.hhNetwork.zeros( Ret.ORIG );
//		if( this.hhNetwork.getAsBigDecimal( 0, 0 ).signum() != 0 ) Thrower
//				.throwNew( IllegalStateException::new, () -> "reset failed" );

		// for each households evaluated with a positive attitude
		this.attitudeEvaluator.isPositive( occ, this.hhAttributes )

				// for each child position in the positive household
				.forEach( hh ->
				{
					Arrays.stream( CHILD_REF_COLUMN_INDICES )
							.mapToLong( hhAtt -> this.hhAttributes
									.getAsLong( hh, hhAtt.ordinal() ) )

							// if child member: 1. exists
							.filter( ppRef -> ppRef != NA

									// 2. is susceptible
									&& this.ppAttributes.getAsInt( ppRef,
											HHMemberAttribute.STATUS
													.ordinal() ) == HHMemberStatus.SUSCEPTIBLE
															.ordinal()

					// 3. is of vaccination age
									&& birthRange.contains( this.ppAttributes
											.getAsBigDecimal( ppRef,
													HHMemberAttribute.BIRTH
															.ordinal() ) )
					//
					)
							// then vaccinate
							.forEach( ppRef ->
							{
								this.ppAttributes.setAsInt(
										HHMemberStatus.ARTIFICIAL_IMMUNE
												.ordinal(),
										ppRef,
										HHMemberAttribute.STATUS.ordinal() );
								LOG.debug(
										"t={}, Vax! (pos) hh #{} (sus) pp #{} born {}",
										prettyDate( t ), hh, ppRef,
										prettyDate( Instant.of(
												this.ppAttributes
														.getAsBigDecimal( ppRef,
																HHMemberAttribute.BIRTH
																		.ordinal() ),
												TimeUnits.ANNUM ) ) );
							} );
				} );
	}

	private static final long NA = -1L;

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
		final long id = this.hhCount.incrementAndGet();
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

		final boolean hhRefMale = this.hhRefMaleDist.draw();
		final Quantity<Time> hhRefAge =
//		hhCat.ageDist( this.distFactory::createUniformContinuous ).draw();
				this.hhRefAgeDist.draw();
		final CBSHousehold hhType =
//		hhCat.hhTypeDist( this.distFactory::createCategorical ).draw();
				this.hhTypeDist.draw();

		final Quantity<Time> impressDelay = Arrays
				.stream( RelationFrequencyJson.Relation.values() )
				.map( r -> this.hhImpressIntervalDist
						.draw( new RelationFrequencyJson.Category( hhRefMale, r,
								hhRefAge ) )
						.inverse().asType( Frequency.class ) )
				.reduce( ( f1, f2 ) -> f1.add( f2 ) ).get().inverse()
				.asType( Time.class );

//		impressInitial( hhIndex, impressDelay );

//		final long partnerRef = hhType.adultCount() < 2 ? NA
//				: createPerson(
//						hhRefAge.subtract(
//								QuantityUtil.valueOf( 3, TimeUnits.ANNUM ) ),
//						hhStatus );

		final Quantity<Time> child1Age = hhRefAge.subtract(
				// TODO from distribution, e.g. 60036ned, 37201
				QuantityUtil.valueOf( 20, TimeUnits.ANNUM ) );
		final HHMemberStatus hhStatus = oldIndex == NA // newborn
				|| profile.status == VaccineStatus.none
				|| this.vaccinationAge.lowerValue()
						.isGreaterThanOrEqualTo( child1Age )
								? HHMemberStatus.SUSCEPTIBLE
								: HHMemberStatus.ARTIFICIAL_IMMUNE;
		final long referentRef = createPerson(
				oldIndex == NA ? NA
						: this.hhAttributes.getAsLong( hhIndex,
								HHAttribute.REFERENT_REF.ordinal() ),
				hhRefMale, hhRefAge, hhStatus );
		final boolean child1Male = true;
		final long child1Ref = hhType
				.childCount() < 1
						? NA
						: createPerson(
								oldIndex == NA ? NA
										: this.hhAttributes.getAsLong( hhIndex,
												HHAttribute.CHILD1_REF
														.ordinal() ),
								child1Male, child1Age, hhStatus );
//		final long child2Ref = hhType.childCount() < 2 ? NA
//				: createPerson(
//						hhRefAge.subtract(
//								QuantityUtil.valueOf( 22, TimeUnits.ANNUM ) ),
//						hhStatus );
//		final long child3Ref = hhType.childCount() < 3 ? NA
//				: createPerson(
//						hhRefAge.subtract(
//								QuantityUtil.valueOf( 24, TimeUnits.ANNUM ) ),
//						hhStatus );

		final BigDecimal initialCalculation = this.calculationDist.draw();
		final Map<HHAttribute, BigDecimal> initialHesitancy = this.hesitancyDist
				.draw( profile );

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
				QuantityUtil.toBigDecimal( impressDelay, TimeUnits.DAYS ),
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
		this.hhAttributes.setAsLong( referentRef, hhIndex,
				HHAttribute.REFERENT_REF.ordinal() );
//		this.hhAttributes.setAsLong( partnerRef, hhIndex,
//				HHAttribute.PARTNER_REF.ordinal() );
		this.hhAttributes.setAsLong( child1Ref, hhIndex,
				HHAttribute.CHILD1_REF.ordinal() );
//		this.hhAttributes.setAsLong( child2Ref, hhIndex,
//				HHAttribute.CHILD2_REF.ordinal() );
//		this.hhAttributes.setAsLong( child3Ref, hhIndex,
//				HHAttribute.CHILD3_REF.ordinal() );

		after( this.hhLeaveHomeAge.subtract( child1Age ) ).call( t ->
		{
			LOG.debug( "t={}, replace home leaver #{}", prettyDate( t ),
					hhIndex );
			createHousehold( hhIndex );
		} );

		return hhType.size();
	}

	private long[] contacts( final long i )
	{
		return HHConnector.availablePeers( this.hhNetwork, i )//.parallel()
				.toArray();
	}

	private long createPerson( final long oldIndex, final boolean male,
		final Quantity<Time> initialAge, final HHMemberStatus status )
	{
		final long id = this.persons.incrementAndGet();
		final long index;
		if( oldIndex == NA )
		{
			index = this.ppIndex.computeIfAbsent( id,
					key -> (long) this.ppIndex.size() );
		} else
		{
			index = oldIndex;
			this.ppIndex.remove( this.ppAttributes.getAsLong( index,
					HHMemberAttribute.IDENTIFIER.ordinal() ) );
		}
		this.ppAttributes.setAsLong( id, index,
				HHMemberAttribute.IDENTIFIER.ordinal() );
		this.ppAttributes.setAsBigDecimal(
				now().to( TimeUnits.ANNUM ).subtract( initialAge ).decimal(),
				index, HHMemberAttribute.BIRTH.ordinal() );
		this.ppAttributes.setAsBoolean( male, index,
				HHMemberAttribute.MALE.ordinal() );
		this.ppAttributes.setAsInt( status.ordinal(), index,
				HHMemberAttribute.STATUS.ordinal() );
		final int motor = this.motorBroker.next( index );
		this.ppAttributes.setAsInt( motor, index,
				HHMemberAttribute.MOTOR_REF.ordinal() );
		this.ppAttributes.setAsBigDecimal( BigDecimal.TEN, index,
				HHMemberAttribute.SUSCEPTIBLE_DAYS.ordinal() );
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
			return zdt.toLocalDateTime() + "="
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

	private final Map<Long, Long> hhIndex = new HashMap<>();

	private final Map<Long, Long> ppIndex = new HashMap<>();

	private void logError( final Throwable e )
	{
		LOG.error( "Problem", e );
	}
}