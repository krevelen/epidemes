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
package nl.rivm.cib.demo.module;

import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.log.LogUtil;
import io.coala.random.ConditionalDistribution;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.demo.DemoConfig;
import nl.rivm.cib.demo.DemoModel;
import nl.rivm.cib.demo.DemoModel.EpiFact;
import nl.rivm.cib.demo.DemoModel.Social.PeerBroker;
import nl.rivm.cib.demo.Households;
import nl.rivm.cib.demo.Households.HouseholdTuple;

/**
 * {@link PeerBrokerSimple}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PeerBrokerSimple implements PeerBroker
{

	public interface PeerConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

//		@Key( "invitation-age" )
//		@DefaultValue( "[.5 yr; 4 yr)" )
//		String vaccinationInvitationAge();
//
//		@SuppressWarnings( "unchecked" )
//		default Range<ComparableQuantity<Time>> vaccinationAgeRange()
//			throws ParseException
//		{
//			return Range
//					.parse( vaccinationInvitationAge(), QuantityUtil::valueOf )
//					.map( v -> v.asType( Time.class ) );
//		}
//
//		@Key( "occasion-recurrence" )
//		@DefaultValue( "0 0 0 7 * ? *" )
//		String vaccinationRecurrence();
//
//		default Iterable<Instant> vaccinationRecurrence(
//			final Scheduler scheduler ) throws ParseException
//		{
//			return Timing.of( vaccinationRecurrence() ).iterate( scheduler );
//		}
//
//		/** @see VaxOccasion#utility() */
//		@Key( "occasion-utility-dist" )
//		@DefaultValue( "const(0.5)" )
//		String vaccinationUtilityDist();
//
//		default ProbabilityDistribution<Number> vaccinationUtilityDist(
//			final Parser distParser ) throws ParseException
//		{
//			return distParser.parse( vaccinationUtilityDist() );
//		}
//
//		/** @see VaxOccasion#proximity() */
//		@Key( "occasion-proximity-dist" )
//		@DefaultValue( "const(0.5)" )
//		String vaccinationProximityDist();
//
//		default ProbabilityDistribution<Number> vaccinationProximityDist(
//			final Parser distParser ) throws ParseException
//		{
//			return distParser.parse( vaccinationProximityDist() );
//		}
//
//		/** @see VaxOccasion#clarity() */
//		@Key( "occasion-clarity-dist" )
//		@DefaultValue( "const(0.5)" )
//		String vaccinationClarityDist();
//
//		default ProbabilityDistribution<Number> vaccinationClarityDist(
//			final Parser distParser ) throws ParseException
//		{
//			return distParser.parse( vaccinationClarityDist() );
//		}
//
//		/** @see VaxOccasion#affinity() */
//		@Key( "occasion-affinity-dist" )
//		@DefaultValue( "const(0.5)" )
//		String vaccinationAffinityDist();
//
//		default ProbabilityDistribution<Number> vaccinationAffinityDist(
//			final Parser distParser ) throws ParseException
//		{
//			return distParser.parse( vaccinationAffinityDist() );
//		}
//
//		@Key( "attractor-factory" )
//		@DefaultValue( "nl.rivm.cib.pilot.hh.HHAttractor$Factory$SimpleBinding" )
//		Class<? extends HHAttractor.Factory> hesitancyAttractorFactory();
//
//		default NavigableMap<String, HHAttractor>
//			hesitancyAttractors( final LocalBinder binder )
//		{
//			try
//			{
//				return Collections.unmodifiableNavigableMap(
//						binder.inject( hesitancyAttractorFactory() )
//								.createAll( toJSON( "attractors" ) ) );
//			} catch( final Exception e )
//			{
//				return Thrower.rethrowUnchecked( e );
//			}
//		}
//
//		/** @see RelationFrequencyJson */
//		@Key( "relation-frequencies" )
//		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
//				+ "relation-frequency.json" )
//		@ConverterClass( InputStreamConverter.class )
//		InputStream hesitancyRelationFrequencies();
//
//		default
//			ConditionalDistribution<Quantity<Time>, RelationFrequencyJson.Category>
//			hesitancyRelationFrequencyDist(
//				final ProbabilityDistribution.Factory distFactory )
//		{
//			final List<RelationFrequencyJson> map = JsonUtil
//					.readArrayAsync( () -> hesitancyRelationFrequencies(),
//							RelationFrequencyJson.class )
//					.toList().blockingGet();
//			@SuppressWarnings( "unchecked" )
//			final Quantity<Time> defaultDelay = QuantityUtil.valueOf( "1 yr" );
//			return c ->
//			{
//				return map.stream()
//						.filter( json -> json.relation == c.relation()
//								&& json.gender == c.gender()
//								&& json.ageRange().contains( c.floorAge() ) )
//						.map( json -> json.intervalDist( distFactory ).draw() )
//						.findFirst().orElse( defaultDelay );
//			};
//		}
//
//		@Key( "relation-impact-rate" )
//		@DefaultValue( "1" )
//		BigDecimal hesitancyRelationImpactRate();
//
//		/** @see HesitancyProfileJson */
//		@Key( "profiles" )
//		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
//				+ "hesitancy-univariate.json" )
//		@ConverterClass( InputStreamConverter.class )
//		InputStream hesitancyProfiles();
//
//		default ProbabilityDistribution<HesitancyProfileJson>
//			hesitancyProfileDist( final Factory distFactory )
//		{
//			return distFactory.createCategorical(
//					HesitancyProfileJson.parse( () -> hesitancyProfiles() )
//							.toList().blockingGet() );
//		}
//
//		default <T> ConditionalDistribution<HesitancyProfileJson, T>
//			hesitancyProfilesGrouped( final Factory distFactory,
//				final Function<HesitancyProfileJson, T> keyMapper )
//		{
//			return ConditionalDistribution.of( distFactory::createCategorical,
//					HesitancyProfileJson.parse( () -> hesitancyProfiles() )
//							.toMultimap( wv -> keyMapper.apply( wv.getValue() ),
//									wv -> wv )
//							.blockingGet() );
//		}
//
//		@Key( "profile-sample" )
//		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM + "hesitancy-initial.json" )
//		@ConverterClass( InputStreamConverter.class )
//		InputStream hesitancyProfileSample();
//
//		default
//			ConditionalDistribution<Map<HHAttribute, BigDecimal>, HesitancyProfileJson>
//			hesitancyProfileSample( final PseudoRandom rng ) throws IOException
//		{
//			final BigDecimal[][] sample = JsonUtil
//					.valueOf( hesitancyProfileSample(), BigDecimal[][].class );
//			final Map<HesitancyProfileJson, ProbabilityDistribution<Map<HHAttribute, BigDecimal>>> distCache = new HashMap<>();
//			return ConditionalDistribution
//					.of( hes -> distCache.computeIfAbsent( hes, key -> () ->
//					{
//						final int row = rng.nextInt( sample.length );
//						final int confCol = hes.indices
//								.get( HesitancyDimension.confidence ) - 1;
//						final int compCol = hes.indices
//								.get( HesitancyDimension.complacency ) - 1;
//						return MapBuilder.<HHAttribute, BigDecimal>unordered()
//								.put( HHAttribute.CONFIDENCE,
//										DecimalUtil.valueOf(
//												sample[row][confCol] ) )
//								.put( HHAttribute.COMPLACENCY,
//										DecimalUtil.valueOf(
//												sample[row][compCol] ) )
//								.build();
//					} ) );
//		}
//
//		/**
//		 * TODO from profile-data
//		 * 
//		 * @see HesitancyProfileJson
//		 */
//		@Key( "calculation-dist" )
//		@DefaultValue( "const(0.5)" )
//		String hesitancyCalculationDist();
//
//		default ProbabilityDistribution<BigDecimal> hesitancyCalculationDist(
//			final Parser distParser ) throws ParseException
//		{
//			return distParser.<Number>parse( hesitancyCalculationDist() )
//					.map( DecimalUtil::valueOf );
//		}
//
//		@Key( "social-network-degree" )
//		@DefaultValue( "10" )
//		int hesitancySocialNetworkDegree();
//
//		@Key( "social-network-beta" )
//		@DefaultValue( "0.5" ) // 0 = lattice, 1 = random network
//		double hesitancySocialNetworkBeta();
//
//		@Key( "social-assortativity" )
//		@DefaultValue( "0.75" )
//		double hesitancySocialAssortativity();
//
//		@Key( "school-assortativity-dist" )
//		@DefaultValue( "bernoulli(0.75)" )
//		String hesitancySchoolAssortativity();
//
//		default ProbabilityDistribution<Boolean> hesitancySchoolAssortativity(
//			final Parser distParser ) throws ParseException
//		{
//			return distParser.parse( hesitancySchoolAssortativity() );
//		}
//
//		/** @see HHAttitudeEvaluator */
//		@Key( "evaluator" )
//		@DefaultValue( "nl.rivm.cib.pilot.hh.HHAttitudeEvaluator$Average" )
//		Class<? extends HHAttitudeEvaluator> attitudeEvaluatorType();
//
//		default HHAttitudeEvaluator attitudeEvaluator()
//			throws InstantiationException, IllegalAccessException
//		{
//			return attitudeEvaluatorType().newInstance();
//		}
//
//		/** @see HHAttitudePropagator */
//		@Key( "propagator" )
//		@DefaultValue( "nl.rivm.cib.pilot.hh.HHAttitudePropagator$Shifted" )
//		Class<? extends HHAttitudePropagator> attitudePropagatorType();
//
//		default HHAttitudePropagator attitudePropagator()
//			throws InstantiationException, IllegalAccessException
//		{
//			return attitudePropagatorType().newInstance();
//		}
//
//		/**
//		 * <a
//		 * href=http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html>Cron
//		 * trigger pattern</a> for timing the attitude/hesitancy propagation,
//		 * e.g.
//		 * <ol type=a>
//		 * <li>{@code "0 0 0 ? * MON *"} : <em>midnight on every
//		 * Monday</em></li>
//		 * <li>{@code "0 0 0 1,15 * ? *"} : <em>midnight on every 1st and 15th
//		 * day of the month</em></li>
//		 * </ol>
//		 */
//		@Key( "propagator-recurrence" )
//		@DefaultValue( "0 0 0 ? * MON *" )
//		String attitudePropagatorRecurrence();
//
//		default Iterable<Instant> attitudePropagatorRecurrence(
//			final Scheduler scheduler ) throws ParseException
//		{
//			return Timing.of( attitudePropagatorRecurrence() )
//					.iterate( scheduler );
//		}

	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( PeerBrokerSimple.class );

	@InjectConfig
	private PeerConfig config;

	@Inject
	private Scheduler scheduler;

	@Inject
	private DataLayer data;

//	@Inject
//	private ProbabilityDistribution.Factory distFactory;
//
//	@Inject
//	private ProbabilityDistribution.Parser distParser;

	private final PublishSubject<EpiFact> events = PublishSubject.create();

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public Observable<EpiFact> events()
	{
		return this.events;
	}

	private Table<HouseholdTuple> households;

	private ConditionalDistribution<Comparable<?>, Comparable<?>> regionalCultureDist;

	private ConditionalDistribution<BigDecimal, Object> culturalAttitudeDist;

//	/** */
//	private ConditionalDistribution<Quantity<Time>, RelationFrequencyJson.Category> hhImpressIntervalDist;
//	/** */
//	private Matrix hhNetwork;
//	/** */
//	private NavigableMap<String, HHAttractor> attractors;
//	/** */
//	private String[] attractorNames;
//	/** */
//	private HHAttractor.Broker attractorBroker;
//	/** */
//	private transient ConditionalDistribution<HesitancyProfileJson, HesitancyProfileJson.Category> hesitancyProfileDist;
//	/** */
//	private transient ProbabilityDistribution<BigDecimal> calculationDist;
//	/** */
//	private transient HHAttitudeEvaluator attitudeEvaluator;
//	/** */
//	private transient HHAttitudePropagator attitudePropagator;
//	/** */
//	private transient ConditionalDistribution<Map<HHAttribute, BigDecimal>, HesitancyProfileJson> hesitancyDist;

	@Override
	public PeerBrokerSimple reset() throws Exception
	{
		// TODO from CBS
		this.regionalCultureDist = regName -> DemoModel.NA;
		// TODO from PIENTER2
		this.culturalAttitudeDist = cult -> BigDecimal.ZERO;

		// TODO auto-assign to new persons
		this.households = this.data.getTable( HouseholdTuple.class );
		this.households.onCreate( this::connectHousehold, scheduler()::fail );
		this.households.onUpdate( Households.EduCulture.class,
				( hhKey, prev, edu ) ->
				{
					final HouseholdTuple hh = this.households.get( hhKey );
					switch( edu )
					{
					case ALTERNATIVE:
					case REFORMED:
						hh.set( Households.Confidence.class, BigDecimal.ZERO );
						hh.set( Households.Complacency.class, BigDecimal.ONE );
						LOG.debug( "HH re-positioned NEG: {}", hh );
						break;

					case OTHERS:
					case SPECIAL:
					default:
						hh.set( Households.Confidence.class, BigDecimal.ONE );
						hh.set( Households.Complacency.class, BigDecimal.ZERO );
						LOG.debug( "HH re-positioned POS: {}", hh );
					}
				}, scheduler()::fail );

		// reference to json indices
//		this.hesitancyDist = this.config
//				.hesitancyProfileSample( this.distFactory.getStream() );

//		final double socialBeta = this.config.hesitancySocialNetworkBeta();
//		final SocialConnector conn = new SocialConnector.WattsStrogatz(
//				this.distFactory.getStream(), socialBeta );
//		final long A = this.attractors.size(), N = this.households.size() - A,
//				Na = N / A + 1, // add 1 extra for partition rounding errors
//				K = Math.min( Na - 1,
//						this.config.hesitancySocialNetworkDegree() );

//		this.hhImpressIntervalDist = this.config
//				.hesitancyRelationFrequencyDist( this.distFactory );
//
//		this.hesitancyProfileDist = this.config.hesitancyProfilesGrouped(
//				this.distFactory, HesitancyProfileJson::toCategory );
//		this.calculationDist = this.config
//				.hesitancyCalculationDist( this.distParser );
//
//		this.attitudeEvaluator = this.config.attitudeEvaluatorType()
//				.newInstance();
//		this.attitudePropagator = this.config.attitudePropagatorType()
//				.newInstance();
//
//		atEach( this.config.attitudePropagatorRecurrence( scheduler() ),
//				this::propagate );
//
//		final double assortativity = this.config.hesitancySocialAssortativity(),
//				dissortativity = (1.0 - assortativity);
//		final Supplier<Long> assortK = () -> (long) (assortativity * K * (A - 1)
//				/ A);
//		final Supplier<Long> dissortK =
////				() -> Math.round( (1.0 - assortativity) * K / (A - 1) )
//				this.distFactory.createPoisson( // use poisson for small degree
//						dissortativity * K )::draw;
//
////		final long assortativeK = A < 2 ? K
////				: (long) (K * this.config.hesitancySocialAssortativity()),
////				dissortativeK = A < 2 ? 0
////						: Math.max( 1, (K - assortativeK) / (A - 1) );
//
//		final Matrix[] assorting = LongStream.range( 0, A ).mapToObj( a ->
//		{
//			final BigDecimal inpeerW = this.hhAttributes.getAsBigDecimal( a,
//					HHAttribute.IMPRESSION_INPEER_WEIGHT.ordinal() );
//			if( inpeerW.signum() < 1 ) LOG.warn( "no weight: {}", inpeerW );
//			final Matrix m = conn.connect( Na, assortK, x -> true,
//					x -> inpeerW );
//			return m;
//		} ).toArray( Matrix[]::new );
//
//		final Matrix dissorting = conn.connect( N, dissortK,
//				x -> this.attractorBroker.next( x[0] ) != this.attractorBroker
//						.next( x[1] ),
//				x -> this.hhAttributes.getAsBigDecimal( x[0] % A,
//						HHAttribute.IMPRESSION_OUTPEER_WEIGHT.ordinal() ) );

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	public void connectHousehold( final HouseholdTuple hh )
	{
		final Comparable<?> regRef = hh.get( Households.HomeRegionRef.class );
		final Comparable<?> cultureRef = this.regionalCultureDist
				.draw( regRef );
//		final BigDecimal attitude =
		this.culturalAttitudeDist.draw( cultureRef ); // TODO split into conf/comp
	}

	private Subject<PropertyChangeEvent> networkEvents = PublishSubject
			.create();

	public Observable<PropertyChangeEvent> network()
	{
		return this.networkEvents;
	}

//	private void pushChangedAttributes( final long i )
//	{
//		if( this.networkEvents.hasObservers() ) this.networkEvents.onNext(
//				new PropertyChangeEvent( i, "hh" + i, null, HHAttribute.toMap(
//						k -> this.hhAttributes.getAsBigDecimal( i, k ),
//						HHAttribute.CONFIDENCE, HHAttribute.COMPLACENCY ) ) );
//	}
//
//	private long[] contacts( final long i )
//	{
//		return SocialConnector.availablePeers( this.hhNetwork, i ).parallel()
//				.toArray();
//	}
//
//	private BigDecimal lastPropagationInstantDays = null;
//
//	private void propagate( final Instant t )
//	{
//		LOG.debug( "t={}, propagating...", prettyDate( t ) );
//
//		final BigDecimal nowDays = t.to( TimeUnits.DAYS ).decimal(),
//				propagateDays = this.lastPropagationInstantDays == null
//						? nowDays
//						: nowDays.subtract( this.lastPropagationInstantDays );
//		this.lastPropagationInstantDays = nowDays;
//
//		final Matrix interactions = SparseMatrix.Factory
//				.zeros( this.hhNetwork.getSize() );
//		LongStream.range( this.attractors.size(), this.hhNetwork.getRowCount() )
//				//.parallel() 
//				// NOTE DefaultSparseGenericMatrix (HashMap) not concurrent
//				.forEach( i ->
//				{
//					final long[] J = contacts( i );
//					if( J.length == 0 ) return;
//					final BigDecimal days = this.hhAttributes.getAsBigDecimal(
//							i, HHAttribute.IMPRESSION_PERIOD_DAYS.ordinal() );
//					final ProbabilityDistribution<Long> binom = this.distFactory
//							.createBinomial(
//									DecimalUtil.divide( propagateDays, days ),
//									DecimalUtil.inverse( J.length ) );
//					for( int j = J.length; j-- != 0; )
//						if( binom.draw() > 0 )
//						{
//							final long[] x = { i, J[j] };
//							SocialConnector.setSymmetric( interactions,
//									SocialConnector.getSymmetric(
//											this.hhNetwork, x ),
//									x );
//						}
//				} );
//		final Map<Long, Integer> changed = this.attitudePropagator
//				.propagate( interactions, this.hhAttributes );
//		changed.forEach( ( i, n ) ->
//		{
//			pushChangedAttributes( i );
//			final long[] y = { i, HHAttribute.IMPRESSION_FEEDS.ordinal() };
//			this.hhAttributes.setAsInt( this.hhAttributes.getAsInt( y ) + n,
//					y );
//		} );
//		LongStream.range( this.attractors.size(), this.hhNetwork.getRowCount() )
//				.forEach( i ->
//				{
////					impressInitial( i,
////							QuantityUtil.valueOf(
////									this.hhAttributes.getAsBigDecimal( i,
////											HHAttribute.IMPRESSION_PERIOD_DAYS
////													.ordinal() ),
////									TimeUnits.DAYS ) );
//					final long[] x = { i,
//					HHAttribute.IMPRESSION_ROUNDS.ordinal() };
//					this.hhAttributes
//							.setAsInt( this.hhAttributes.getAsInt( x ) + 1, x );
//				} );
//	}
}
