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
package nl.rivm.cib.epidemes.pilot;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import javax.inject.Inject;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.ujmp.core.Matrix;
import org.ujmp.core.enums.ValueType;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.config.ConfigUtil;
import io.coala.config.LocalDateConverter;
import io.coala.config.PeriodConverter;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityConfigConverter;
import io.coala.math.QuantityRangeConfigConverter;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.random.QuantityDistribution;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.SchedulerConfig;
import io.coala.time.TimeUnits;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.epidemes.pilot.BrokerTest.DemoModel.DemeTag.DemeEvent;
import nl.rivm.cib.epidemes.pilot.BrokerTest.DemoModel.DemeTag.DemeEventType;
import nl.rivm.cib.epidemes.pilot.BrokerTest.DemoModel.DemeTag.HHRank;
import nl.rivm.cib.epidemes.pilot.BrokerTest.DemoModel.DemeTag.HouseholdTuple;
import nl.rivm.cib.epidemes.pilot.BrokerTest.DemoModel.DemeTag.PersonTuple;
import nl.rivm.cib.epidemes.pilot.BrokerTest.DemoModel.DemeTag.SimpleDeme;
import nl.rivm.cib.epidemes.pilot.BrokerTest.DemoModel.DemoConfig;
import nl.rivm.cib.episim.cbs.TimeUtil;
import nl.rivm.cib.pilot.PilotConfig.RandomSeedConverter;
import tec.uom.se.ComparableQuantity;

/**
 * {@link BrokerTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class BrokerTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( BrokerTest.class );

//	public interface Manager<ID, T extends Tuple>
//	{
//		DataAccessor<ID, T> data();
//
//		class Simple<ID, T extends Tuple> implements Manager<ID, T>
//		{
//			private final DataAccessor<ID, T> data;
//
//			public Simple()
//			{
//				this( new DataAccessor.Factory.MapAccessor() );
//			}
//
//			@SuppressWarnings( "unchecked" )
//			@Inject
//			public Simple( final DataAccessor.Factory factory )
//			{
//				final List<Class<?>> typeArgs = TypeArguments.of( Manager.class,
//						getClass() );
//				final Class<ID> keyType = (Class<ID>) typeArgs.get( 0 );
//				final Class<T> tupleType = (Class<T>) typeArgs.get( 1 );
//				this.data = factory.create( keyType, tupleType );
//			}
//
//			@Override
//			public DataAccessor<ID, T> data()
//			{
//				return this.data;
//			}
//		}
//	}

	public interface DemoModel
	{
		Long NA = -1L;

		@Sources( {
				"file:${" + DemoConfig.CONFIG_BASE_KEY + "}"
						+ DemoConfig.CONFIG_YAML_FILE,
				"file:" + DemoConfig.CONFIG_BASE_DIR
						+ DemoConfig.CONFIG_YAML_FILE,
				"classpath:" + DemoConfig.CONFIG_YAML_FILE } )
		interface DemoConfig extends YamlConfig
		{

			/** configuration file name system property */
			String CONFIG_BASE_KEY = "config.base";

			/** configuration and data file base directory */
			String CONFIG_BASE_DIR = "dist/";

			String CONFIG_YAML_FILE = "demo.yaml";

			String DATASOURCE_JNDI = "jdbc/pilotDB";

			/** configuration key separator */
			String KEY_SEP = ConfigUtil.CONFIG_KEY_SEP;

			/** configuration key */
			String SCENARIO_BASE = "scenario";

			/** configuration key */
			String REPLICATION_PREFIX = SCENARIO_BASE + KEY_SEP + "replication"
					+ KEY_SEP;

			@Key( REPLICATION_PREFIX + "setup-name" )
			@DefaultValue( "pilot" )
			String setupName();

			@Key( REPLICATION_PREFIX + "random-seed" )
			@DefaultValue( "NAN" )
			@ConverterClass( RandomSeedConverter.class )
			Long randomSeed();

			@Key( REPLICATION_PREFIX + "duration-period" )
			@DefaultValue( "P1Y" )
			@ConverterClass( PeriodConverter.class )
			Period duration();

			@Key( REPLICATION_PREFIX + "offset-date" )
			@DefaultValue( "2012-01-01" )
			@ConverterClass( LocalDateConverter.class )
			LocalDate offset();

			/** configuration key */
			String POPULATION_BASE = "demography";
		}

		interface Cultures
		{
			@SuppressWarnings( "serial" )
			class CultureRef extends AtomicReference<Object>
				implements Property<Object>
			{
			}

			@SuppressWarnings( "serial" )
			class AttractorAttitude extends AtomicReference<BigDecimal>
				implements Property<BigDecimal>
			{
			}

			List<Class<?>> PROPERTIES = Arrays.asList( CultureRef.class,
					AttractorAttitude.class );
		}

		interface Households
		{
			@SuppressWarnings( "serial" )
			class HouseholdSeq extends AtomicReference<Long>
				implements Property<Long>
			{
			}

			@SuppressWarnings( "serial" )
			class Attitude extends AtomicReference<BigDecimal>
				implements Property<BigDecimal>
			{
			}

			@SuppressWarnings( "serial" )
			class HomeLocationRef extends AtomicReference<Object>
				implements Property<Object>
			{
			}

			@SuppressWarnings( "serial" )
			class Composition extends AtomicReference<CBSHousehold>
				implements Property<CBSHousehold>
			{
			}

			List<Class<?>> PROPERTIES = Arrays.asList(
					Cultures.CultureRef.class, HouseholdSeq.class,
					Attitude.class, Composition.class, HomeLocationRef.class );

		}

		interface Persons
		{
			@SuppressWarnings( "serial" )
			class PersonSeq extends AtomicReference<Long>
				implements Property<Long>
			{
			}

			@SuppressWarnings( "serial" )
			class CurrentHouseholdRef extends AtomicReference<Object>
				implements Property<Object>
			{
			}

			@SuppressWarnings( "serial" )
			class HouseholdRank extends AtomicReference<HHRank>
				implements Property<HHRank>
			{
			}

			@SuppressWarnings( "serial" )
			class Male extends AtomicReference<Boolean>
				implements Property<Boolean>
			{
			}

			@SuppressWarnings( "serial" )
			class BirthDay extends AtomicReference<BigDecimal>
				implements Property<BigDecimal>
			{
			}

			@SuppressWarnings( "serial" )
			class CurrentLocationRef extends AtomicReference<Object>
				implements Property<Object>
			{
			}

			List<Class<?>> PROPERTIES = Arrays.asList( PersonSeq.class,
					CurrentHouseholdRef.class, HouseholdRank.class, Male.class,
					BirthDay.class, CurrentLocationRef.class );

		}

		interface Region
		{
			@SuppressWarnings( "serial" )
			class RegionRef extends AtomicReference<Long>
				implements Property<Long>
			{
			}

			@SuppressWarnings( "serial" )
			class ParentRef extends AtomicReference<Long>
				implements Property<Long>
			{
			}
		}

		interface Locations
		{
			@SuppressWarnings( "serial" )
			class LocationRef extends AtomicReference<Long>
				implements Property<Long>
			{
			}

			@SuppressWarnings( "serial" )
			class Latitude extends AtomicReference<BigDecimal>
				implements Property<BigDecimal>
			{
			}

			@SuppressWarnings( "serial" )
			class Longitude extends AtomicReference<BigDecimal>
				implements Property<BigDecimal>
			{
			}
		}

		class EpiFact
		{
			Instant time;
		}

		interface EpiActor
		{
			EpiActor reset() throws Exception;

			Observable<EpiFact> events();
		}

		interface DemeTag
		{
			// tag

//			class Preparation extends EpiFact implements DemeTag
//			{
//				PersonRef referentRef;
//				PersonRef partnerRef;
//				HouseholdRef expectingHHRef;
//			}
//
//			class Expansion extends EpiFact implements DemeTag
//			{
//				HouseholdRef expandedHHRef;
//				PersonRef childRef; // possibly null, representing miscarriage etc
//			}
//
//			class Reduction extends EpiFact implements DemeTag
//			{
//				PersonRef personRef;
//				HouseholdRef reducedHHRef;
//			}
//
//			class Union extends EpiFact implements DemeTag
//			{
//				PersonRef referentRef;
//				HouseholdRef referentHHRef;
//				PersonRef partnerRef;
//				HouseholdRef abandonedHHRef;
//			}
//
//			class Separation extends EpiFact implements DemeTag
//			{
//				PersonRef referentRef;
//				HouseholdRef referentHHRef;
//				PersonRef expartnerRef;
//				HouseholdRef expartnerHHRef;
//			}
//
//			class Division extends EpiFact implements DemeTag
//			{
//				HouseholdRef abandonedHHRef;
//				PersonRef childRef;
//				HouseholdRef childHHRef;
//			}
//
//			class Relocation extends EpiFact implements DemeTag
//			{
//				HouseholdRef relocatedHHRef;
//			}
//
//			class Immigration extends EpiFact implements DemeTag
//			{
//				HouseholdRef immigratedHHRef;
//			}
//
//			class Emigration extends EpiFact implements DemeTag
//			{
//				HouseholdRef emigratedHHRef;
//			}

			class DemeEvent extends EpiFact implements DemeTag
			{
				/**
				 * {@link DemeEvent} constructor
				 * 
				 * @param eventType
				 */
				public DemeEvent( DemeEventType eventType )
				{
					this.type = eventType;
				}

				public final DemeEventType type;
			}

			enum DemeEventType
			{
				PREPARATION, EXPANSION, ELIMINATION, UNION, SEPARATION, DIVISION, RELOCATION, IMMIGRATION, EMIGRATION;
			}

			enum HHRank
			{
				REFERENT, PARTNER, CHILD1, CHILD2, CHILD3;
			}

			/**
			 * {@link PersonTuple} binds concretely
			 */
			class HouseholdTuple extends Tuple implements DemeTag
			{

			}

			/**
			 * {@link PersonTuple} binds concretely
			 */
			class PersonTuple extends Tuple implements DemeTag //, Comparable<PersonTuple>
			{
//				private BigDecimal birthDay = null;
//
//				public PersonTuple cacheBirth()
//				{
//					this.birthDay = super.get( Persons.BirthDay.class );
//					return this;
//				}
//
//				@SuppressWarnings( "unchecked" )
//				public <K extends Property<V>, V> V get( final Class<K> key )
//				{
//					return key == Persons.BirthDay.class ? (V) this.birthDay
//							: super.get( key );
//				}
//
//				@Override
//				public int compareTo( final PersonTuple o )
//				{
//					return this.birthDay.compareTo( o.birthDay );
//				}
			}

			/** organizes survival and reproduction (across households) */
			class SimpleDeme implements EpiActor, Proactive
			{

				public interface SimpleConfig extends YamlConfig
				{

					@Key( "population-size" )
					@DefaultValue( "" + 30_000 )
					int populationSize();

					@Key( "hh-type-dist" )
					@DefaultValue( "const(DUO_1KID)" )
					String householdTypeDist();

					/**
					 * mean event interval per household = inverse event rate
					 */
					@SuppressWarnings( "rawtypes" )
					@Key( "hh-event-duration" )
					@DefaultValue( "1 year" )
					@ConverterClass( QuantityConfigConverter.class )
					ComparableQuantity householdEventDuration();

					@Key( "hh-event-dist" )
					@DefaultValue( "enum(EXPANSION:1; ELIMINATION:1; "
							+ "UNION:1; SEPARATION:1; DIVISION:1;"
							+ " RELOCATION:1; IMMIGRATION:1; EMIGRATION:1)" )
					String householdEventTypeDist();

					@SuppressWarnings( "rawtypes" )
					@Key( "fertility-age-range" )
					@DefaultValue( "[15 yr; 50 yr]" )
					@ConverterClass( QuantityRangeConfigConverter.class )
					Range<ComparableQuantity> fertilityAgeRange();
				}

				@InjectConfig
				private SimpleConfig config;

				@Inject
				private Scheduler scheduler;

				@Inject
				private DataLayer data;

				@Inject
				private ProbabilityDistribution.Parser distParser;

				private Subject<EpiFact> events = PublishSubject.create();

				private final AtomicLong ppSeq = new AtomicLong( -1 ),
						hhSeq = new AtomicLong( -1 );

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

				@SuppressWarnings( { "unchecked", "rawtypes" } )
				@Override
				public SimpleDeme reset()
				{
					this.events.onComplete();

					this.events = PublishSubject.create();
					LOG.trace( "Setting up {} with config: {}",
							getClass().getSimpleName(), this.config );

					final Table<PersonTuple> persons = this.data
							.createTable( PersonTuple.class );
					final Table<HouseholdTuple> households = this.data
							.createTable( HouseholdTuple.class );
					final Range<BigDecimal> fertilityAgeRange = this.config
							.fertilityAgeRange()
							.<ComparableQuantity>map(
									v -> v.to( scheduler().timeUnit() ) )
							.map( QuantityUtil::decimalValue );

					final int n = this.config.populationSize();
					final List<Object> householdKeys = Collections
							.synchronizedList( new ArrayList<>( n ) );
					persons.forEach( pp -> ppSeq.updateAndGet( seq -> Math
							.max( seq, pp.get( Persons.PersonSeq.class ) ) ) );
					ppSeq.incrementAndGet(); // advance to new sequence value
					households.keys().filter( householdKeys::add )
							.forEach( hh -> hhSeq.updateAndGet( seq -> Math
									.max( seq, households.select( hh ).get(
											Households.HouseholdSeq.class ) ) ) );
					hhSeq.incrementAndGet(); // advance to new sequence value

					final ProbabilityDistribution<Long> cultureDist = ProbabilityDistribution
							.createDeterministic( NA );
					final ProbabilityDistribution<BigDecimal> attitudeDist = ProbabilityDistribution
							.createDeterministic( BigDecimal.ZERO );
					final ProbabilityDistribution<Long> locationDist = ProbabilityDistribution
							.createDeterministic( NA );
					final TreeMap<BigDecimal, Object> fertileFemKeys = new TreeMap<>();

					LOG.trace( "Initializing households..." );
					final BigDecimal now = nowDecimal();
					households.creation().subscribe( hh ->
					{
						final Map<HHRank, Object> memberKeys = setupHousehold(
								ppSeq::getAndIncrement, persons::create, hh,
								hhSeq.getAndIncrement(), cultureDist.draw(),
								hh.get( Households.Composition.class ),
								attitudeDist.draw(), locationDist.draw() );

						// index fertile females
						final Object partnerKey = memberKeys
								.get( HHRank.PARTNER );
						if( !NA.equals( partnerKey ) )
						{
							final PersonTuple partner = persons
									.select( partnerKey );
							final BigDecimal partnerAge = now.subtract(
									partner.get( Persons.BirthDay.class ) );
							if( fertilityAgeRange.contains( partnerAge ) )
								synchronized( fertileFemKeys )
								{
									fertileFemKeys.put( partnerAge,
											partnerKey );
								}
							else
								LOG.trace( "not fertile: {} -> {}", partnerAge,
										partner );
						}
					}, scheduler()::fail );
					try
					{
						final ProbabilityDistribution<CBSHousehold> hhCompositionDist = this.distParser
								.parse( this.config.householdTypeDist(),
										CBSHousehold.class );

						parallellize( n, () ->
						{
							final CBSHousehold composition = hhCompositionDist
									.draw();
							households.create().with(
									Households.Composition.class, composition );
							return composition.size();
						} );

						scheduleDemeEvents().subscribe( eventType ->
						{
							switch( eventType )
							{
							case ELIMINATION: // TODO handle orphans
							case EMIGRATION:
							case UNION:
								final Object key = removeRandomKey(
										householdKeys );
								if( key != null )
								{
									households.remove( key );
									this.events.onNext(
											new DemeEvent( eventType ) );
								}
								break;

							case SEPARATION:
							case IMMIGRATION:
							case DIVISION:
								final HouseholdTuple hh = households.create();
								householdKeys.add( hh.key() );
								setupHousehold( ppSeq::getAndIncrement,
										persons::create, hh,
										hhSeq.getAndIncrement(),
										cultureDist.draw(),
										hhCompositionDist.draw(),
										attitudeDist.draw(),
										locationDist.draw() );
								this.events
										.onNext( new DemeEvent( eventType ) );
								break;

							case PREPARATION: // TODO add child createPerson();
							case EXPANSION: // TODO update referent/partner status
							case RELOCATION: // TODO update home location
								this.events
										.onNext( new DemeEvent( eventType ) );
								break;

							default:
								LOG.warn( "Unhandled event type: {}",
										eventType );
							}
						}, this.events::onError );
					} catch( final ParseException e )
					{
						this.events.onError( e );
					}

					synchronized( fertileFemKeys )
					{
						LOG.trace( "Fertile: {}",
								fertileFemKeys.values().stream()
										.map( Object::toString )
										.reduce( ( s1, s2 ) -> String
												.join( ", ", s1, s2 ) )
										.orElse( "[]" ) );
					}

					scheduler().atEnd( t -> this.events.onComplete() );
					return this;
				}

				private Observable<DemeEventType> scheduleDemeEvents()
				{
					return Observable.create( sub ->
					{
						LOG.trace( "Scheduling deme-events" );

						@SuppressWarnings( "unchecked" )
						// mean event interval per household = inverse event rate
						final ComparableQuantity<?> dt = this.config
								.householdEventDuration()
								.to( scheduler().timeUnit() );
						// mean event interval across all households
						final BigDecimal mean = DecimalUtil
								.divide( dt.getValue(), this.hhSeq.get() );
						// event rate: i.i.d. exponential distribution
						final QuantityDistribution<?> hhEvDurDist = this.distParser
								.getFactory().createExponential( mean )
								.toQuantities( scheduler().timeUnit() );
						try
						{
							final ProbabilityDistribution<DemeEventType> nextEvent = this.distParser
									.<DemeEventType, DemeEventType>parse(
											this.config
													.householdEventTypeDist(),
											DemeEventType.class );
							final Disposable dis = atEach(
									() -> (Infiniterator) () -> now()
											.add( hhEvDurDist.draw() ) ).map(
													t -> nextEvent.draw() )
													.subscribe( sub::onNext,
															sub::onError,
															sub::onComplete );
							sub.setDisposable( dis );
						} catch( final ParseException e )
						{
							sub.onError( e );
						}
					} );
				}

				private final ForkJoinPool forkJoinPool = new ForkJoinPool(
						Runtime.getRuntime().availableProcessors() - 1 );

				private void parallellize( final long n,
					final Supplier<Integer> handler )
				{
					final AtomicLong count = new AtomicLong(),
							lastCount = new AtomicLong(),
							t0 = new AtomicLong( System.currentTimeMillis() ),
							lastTime = new AtomicLong( t0.get() );

					final CountDownLatch latch = new CountDownLatch( 1 );
					// FIXME use observable schedulers
					this.forkJoinPool.submit( () -> LongStream.range( 0, n )
							.parallel().forEach( i ->
							{
								try
								{
									if( count.get() < n )
										count.addAndGet( handler.get() );
									else
										latch.countDown();
								} catch( final Throwable t )
								{
									this.events.onError( t );
									latch.countDown();
								}
							} ) );

					while( latch.getCount() > 0 )
					{
						try
						{
							latch.await( 5, TimeUnit.SECONDS );
							if( latch.getCount() > 0 )
							{
								final long i = count.get(),
										i0 = lastCount.getAndSet( i ),
										t = System.currentTimeMillis(),
										ti = lastTime.getAndSet( t );
								LOG.trace(
										"Handled {} of {} persons ({}%) in {}s, adding at {}/s...",
										i, n,
										DecimalUtil.toScale( DecimalUtil
												.divide( i * 100, n ), 1 ),
										DecimalUtil.toScale(
												DecimalUtil.divide(
														t - t0.get(), 1000 ),
												1 ),
										DecimalUtil.toScale( DecimalUtil.divide(
												(i - i0) * 1000, t - ti ),
												1 ) );
							}
						} catch( final InterruptedException e )
						{
							this.events.onError( e );
						}
					}
					final long i = count.get(),
							dt = System.currentTimeMillis() - t0.get();
					LOG.trace( "Handled {} of {} persons in {}s at {}/s", i, n,
							DecimalUtil.toScale( DecimalUtil.divide( dt, 1000 ),
									1 ),
							DecimalUtil.toScale(
									DecimalUtil.divide( i * 1000, dt ), 1 ) );
				}

				private Object removeRandomKey( final List<Object> keys )
				{
					if( keys.isEmpty() ) return null;

					synchronized( keys )
					{
						final int last = keys.size() - 1, i = this.distParser
								.getFactory().getStream().nextInt( last + 1 );
						final Object result = keys.get( i );
						// swap with last, so ArrayList#remove() needs not shift
						if( i != last ) keys.set( i, keys.get( last ) );
						keys.remove( last );
						return result;
					}
				}

				private final Map<HHRank, Object> setupHousehold(
					final Supplier<Long> ppSeq,
					final Supplier<PersonTuple> personFactory,
					final HouseholdTuple hh, final Long hhSeq,
					final Object cultureRef, final CBSHousehold composition,
					final BigDecimal attitude, final Object homeLocationKey )
				{
					hh.with( Households.HouseholdSeq.class, hhSeq )
							.with( Cultures.CultureRef.class, cultureRef )
//							.with( Households.Composition.class, composition )
							.with( Households.HomeLocationRef.class,
									homeLocationKey )
							.with( Households.Attitude.class, attitude );
					if( hh.get( Households.HouseholdSeq.class )
							.compareTo( hhSeq ) != 0 )
					{
						LOG.trace(
								"Retry optimistic lock on household #{} -> {}",
								hh.key(), hh );
						return setupHousehold( ppSeq, personFactory, hh, hhSeq,
								cultureRef, composition, attitude,
								homeLocationKey );
					}
					final Map<HHRank, Object> result = new EnumMap<>(
							HHRank.class );
					final boolean refMale = true;
					final BigDecimal refBirth = BigDecimal.ZERO;
					result.put( HHRank.REFERENT,
							setupPerson( personFactory.get(), hh.key(),
									ppSeq.get(), HHRank.REFERENT, refMale,
									refBirth, homeLocationKey ).key() );

					if( composition.couple() ) // add partner
					{
						final boolean partnerMale = false;
						final BigDecimal partnerAge = QuantityUtil.decimalValue(
								QuantityUtil.valueOf(
										15. + this.distParser.getFactory()
												.getStream().nextDouble() * 35,
										TimeUnits.YEAR ),
								scheduler().timeUnit() );
						result.put( HHRank.PARTNER,
								setupPerson( personFactory.get(), hh.key(),
										ppSeq.get(), HHRank.PARTNER,
										partnerMale,
										nowDecimal().subtract( partnerAge ),
										homeLocationKey ).key() );
					} else
						result.put( HHRank.PARTNER, NA );
					if( composition.childCount() > 0 ) // add first child
					{
						final boolean child1Male = false;
						final BigDecimal child1Birth = BigDecimal.ZERO;
						result.put( HHRank.CHILD1,
								setupPerson( personFactory.get(), hh.key(),
										ppSeq.get(), HHRank.CHILD1, child1Male,
										child1Birth, homeLocationKey ).key() );
					} else
						result.put( HHRank.CHILD1, NA );

					if( composition.childCount() > 1 ) // add second child
					{
						final boolean child2Male = false;
						final BigDecimal child2Birth = BigDecimal.ZERO;
						result.put( HHRank.CHILD2,
								setupPerson( personFactory.get(), hh.key(),
										ppSeq.get(), HHRank.CHILD2, child2Male,
										child2Birth, homeLocationKey ).key() );
					} else
						result.put( HHRank.CHILD2, NA );

					if( composition.childCount() > 2 ) // add third child
					{
						final boolean child3Male = false;
						final BigDecimal child3Birth = BigDecimal.ZERO;
						final PersonTuple child3 = setupPerson(
								personFactory.get(), hh.key(), ppSeq.get(),
								HHRank.CHILD3, child3Male, child3Birth,
								homeLocationKey );
						result.put( HHRank.CHILD3, child3.key() );
					} else
						result.put( HHRank.CHILD3, NA );

					return result;
				}

				private PersonTuple setupPerson( final PersonTuple pp,
					final Object hhKey, final long ppSeq, final HHRank rank,
					final boolean male, final BigDecimal birthDay,
					final Object locationRef )
				{
					pp.with( Persons.PersonSeq.class, ppSeq )
							.with( Persons.CurrentHouseholdRef.class, hhKey )
							.with( Persons.HouseholdRank.class, rank )
							.with( Persons.BirthDay.class, birthDay )
							.with( Persons.Male.class, male )
							.with( Persons.CurrentLocationRef.class,
									locationRef );
					if( pp.get( Persons.PersonSeq.class )
							.compareTo( ppSeq ) != 0 )
					{
						LOG.trace( "Retry optimistic lock on person #{} -> {}",
								pp.key(), pp );
						return setupPerson( pp, hhKey, ppSeq, rank, male,
								birthDay, locationRef );
					}
					return pp;
				}
			}
		}

//		class MixingStatus extends DataView.Simple
//		{
//
//			public MixingStatus( final Map<Class<?>, Object> ownerData )
//			{
//				super( ownerData );
//			}
//
//			public MixingStatus( final Matrix ownerData,
//				final Function<Class<?>, long[]> coords )
//			{
//				super( ownerData, coords );
//			}
//
//			public Long personRef()
//			{
//				return (Long) get( PersonRef.class );
//			}
//
//			public Long locationRef()
//			{
//				return (Long) get( LocationRef.class );
//			}
//
//		}
//
//		/** organizes meetings at a location */
//		class MobilizationManager extends Manager.Simple<Long, MixingStatus>
//		{
//			public MobilizationManager( final Map<Long, MixingStatus> dataStore,
//				final Function<Long, MixingStatus> generator )
//			{
//				super( dataStore.keySet()::stream,
//						ref -> dataStore.computeIfAbsent( ref, generator ) );
//			}
//
//			public MobilizationManager( final Matrix dataSource,
//				final Function<Class<?>, Long> coords )
//			{
//				super( () -> LongStream.range( 0, dataSource.getRowCount() )
//						.mapToObj( i -> i ),
//						i -> new MixingStatus( dataSource, type -> new long[]
//				{ i, coords.apply( type ) } ) );
//			}
//		}
	}

	public static class DemoScenario implements DemoModel, Scenario
	{
		@Inject
		private LocalBinder binder;

		@Inject
		private DataLayer data;

		@Inject
		private Scheduler scheduler;

		private Matrix persons, households;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		private final Map<DemeEventType, AtomicLong> demeEventStats = new EnumMap<>(
				DemeEventType.class );

		private final Map<Class<?>, AtomicLong> dataEventStats = new HashMap<>();

		@Override
		public void init() throws Exception
		{
			final DemoConfig config = ConfigFactory.create( DemoConfig.class );
			LOG.trace( "cfg: {}", config );

			this.persons = Matrix.Factory.sparse( ValueType.OBJECT, 20_000_000,
					Persons.PROPERTIES.size() );
			this.households = Matrix.Factory.sparse( ValueType.OBJECT,
					10_000_000, Households.PROPERTIES.size() );

			this.data
					.withSource( this.persons,
							MapBuilder.<Class<?>, List<Class<?>>>unordered()
									.put( PersonTuple.class,
											Persons.PROPERTIES )
									.build() )
					.withSource( this.households, MapBuilder
							.<Class<?>, List<Class<?>>>unordered()
							.put( HouseholdTuple.class, Households.PROPERTIES )
							.build() );
			this.data.changes().filter( chg -> chg.crud() != Table.CRUD.UPDATE )
					.subscribe(
							chg -> this.dataEventStats
									.computeIfAbsent( chg.changedType(),
											k -> new AtomicLong() )
									.addAndGet( chg.crud() == Table.CRUD.CREATE
											? 1 : -1 ),
							ex -> LOG.error( "Problem", ex ) );

			// populate and run deme
			this.binder
					.inject( SimpleDeme.class,
							config.toJSON( DemoConfig.SCENARIO_BASE,
									DemoConfig.POPULATION_BASE ) )
					.reset().events().ofType( DemeEvent.class ).subscribe(
							ev -> this.demeEventStats
									.computeIfAbsent( ev.type,
											k -> new AtomicLong() )
									.incrementAndGet(),
							ex -> LOG.error( "Problem", ex ),
							() -> LOG.trace( "stats: {}, sum: {} data: {}",
									this.demeEventStats,
									this.demeEventStats.values().stream()
											.mapToLong( AtomicLong::get ).sum(),
									this.dataEventStats ) );
		}
	}

	@Test
	public void doTest() throws InterruptedException
	{
		LOG.info( "Test started" );

		final DemoConfig config = ConfigFactory.create( DemoConfig.class );
		final ZonedDateTime offset = config.offset()
				.atStartOfDay( TimeUtil.NL_TZ );
		final long durationDays = Duration
				.between( offset, offset.plus( config.duration() ) ).toDays();
		final LocalConfig binderConfig = LocalConfig.builder().withProvider(
				Scheduler.class, Dsol3Scheduler.class,
				MapBuilder.unordered()
						.put( SchedulerConfig.ID_KEY, "" + config.setupName() )
						.put( SchedulerConfig.OFFSET_KEY, "" + config.offset() )
						.put( SchedulerConfig.DURATION_KEY, "" + durationDays )
						.build() )
				.withProvider( DataLayer.class, DataLayer.Simple.class )
				.withProvider( Scenario.class, DemoScenario.class )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )
				.build();

		// FIXME workaround until seed becomes configurable in coala
		final LocalBinder binder = binderConfig
				.createBinder( MapBuilder.<Class<?>, Object>unordered()
						.put( ProbabilityDistribution.Factory.class,
								new Math3ProbabilityDistribution.Factory(
										new Math3PseudoRandom.MersenneTwisterFactory()
												.create( PseudoRandom.Config.NAME_DEFAULT,
														config.randomSeed() ) ) )
						.build() );

		final Scenario model = binder.inject( Scenario.class );
		model.run();

		LOG.info( "Test done" );
	}
}
