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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityConfigConverter;
import io.coala.math.QuantityRangeConfigConverter;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.demo.DemoModel.Demical.SimpleDeme.SimpleConfig.DemeEventType;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.person.HouseholdComposition;
import tec.uom.se.ComparableQuantity;

/**
 * {@link DemoModel}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface DemoModel
{
	Long NA = -1L;

	enum FamilyRank
	{
		REFERENT, PARTNER, CHILD1, CHILD2, CHILD3;
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

		List<Class<?>> PROPERTIES = Arrays.asList( Cultures.CultureRef.class,
				Cultures.AttractorAttitude.class );
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

		List<Class<?>> PROPERTIES = Arrays.asList( Cultures.CultureRef.class,
				Households.HouseholdSeq.class, Households.Attitude.class,
				Households.Composition.class,
				Households.HomeLocationRef.class );

		/**
		 * {@link PersonTuple} binds concretely
		 */
		class HouseholdTuple extends Tuple
		{

		}

	}

	interface Persons
	{
		@SuppressWarnings( "serial" )
		class PersonSeq extends AtomicReference<Long> implements Property<Long>
		{
		}

		@SuppressWarnings( "serial" )
		class HouseholdRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		@SuppressWarnings( "serial" )
		class MemberRank extends AtomicReference<FamilyRank>
			implements Property<FamilyRank>
		{
		}

		@SuppressWarnings( "serial" )
		class Male extends AtomicReference<Boolean> implements Property<Boolean>
		{
		}

		@SuppressWarnings( "serial" )
		class BirthTime extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

		@SuppressWarnings( "serial" )
		class SiteRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		List<Class<?>> PROPERTIES = Arrays.asList( Persons.PersonSeq.class,
				Persons.HouseholdRef.class, Persons.MemberRank.class,
				Persons.Male.class, Persons.BirthTime.class,
				Persons.SiteRef.class );

		class PersonTuple extends Tuple
		{

		}
	}

	interface Regions
	{
		@SuppressWarnings( "serial" )
		class RegionRef extends AtomicReference<Long> implements Property<Long>
		{
		}

		@SuppressWarnings( "serial" )
		class ParentRef extends AtomicReference<Long> implements Property<Long>
		{
		}

		@SuppressWarnings( "serial" )
		class Population extends AtomicReference<Long> implements Property<Long>
		{
		}
	}

	interface Sites
	{
		@SuppressWarnings( "serial" )
		class SiteSeq extends AtomicReference<Long> implements Property<Long>
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

		Observable<? extends EpiFact> events();
	}

	interface Cultural
	{
		// group-related variance/subcultures, in social and medical behaviors 
	}

	interface Social
	{
		// peer pressure networks, dynamics
	}

	interface Medical
	{
		// transmission, intervention, information, decisions, vaccination

		abstract class MedicalEvent extends EpiFact
		{
			public Map<MSEIRS.Compartment, Integer> compartmentDelta = Collections
					.emptyMap();
			public Object siteRef = NA;
		}

	}

	/**
	 * {@link Demical} provides mixing and (in-/epi-/pan-)demic transmission
	 */
	interface Demical
	{

		abstract class DemicalEvent extends EpiFact
		{
			public Map<? extends HouseholdComposition, Integer> hhDelta = Collections
					.emptyMap();
			public Map<FamilyRank, Integer> memberDelta = Collections
					.emptyMap();
			public Object siteRef = NA;

			public DemicalEvent withHouseholdDelta(
				final Function<MapBuilder<CBSHousehold, Integer, ?>, Map<? extends HouseholdComposition, Integer>> mapBuilder )
			{
				this.hhDelta = mapBuilder.apply( MapBuilder
						.ordered( CBSHousehold.class, Integer.class ) );
				return this;
			}

			public DemicalEvent withMemberDelta(
				final Function<MapBuilder<FamilyRank, Integer, ?>, Map<FamilyRank, Integer>> mapBuilder )
			{
				this.memberDelta = mapBuilder.apply(
						MapBuilder.ordered( FamilyRank.class, Integer.class ) );
				return this;
			}
		}

		interface Deme extends EpiActor
		{
			@Override
			Deme reset() throws Exception;

			@Override
			Observable<? extends DemicalEvent> events();
		}

		class Preparation extends DemicalEvent
		{
			// conception, expecting a baby (triggers new behavior)
		}

		class Expansion extends DemicalEvent
		{
			// child birth, adoption, possibly 0 representing miscarriage etc
		}

		class Elimination extends DemicalEvent
		{
			// person dies, possibly leaving orphans, abandoning household
		}

		class Union extends DemicalEvent
		{
			// households merge, e.g. living together or marriage
		}

		class Separation extends DemicalEvent
		{
			// household splits, e.g. couple divorces
		}

		class Division extends DemicalEvent
		{
			// household splits, e.g. child reaches adulthood
		}

		class Relocation extends DemicalEvent
		{
//				HouseholdRef relocatedHHRef;
		}

		class Immigration extends DemicalEvent
		{
//				HouseholdRef immigratedHHRef;
		}

		class Emigration extends DemicalEvent
		{
//				HouseholdRef emigratedHHRef;
		}

		/** organizes survival and reproduction (across households) */
		class SimpleDeme implements EpiActor, Proactive
		{

			/** */
			private static final Logger LOG = LogUtil
					.getLogger( DemoModel.Demical.SimpleDeme.class );

			public interface SimpleConfig extends YamlConfig
			{

				enum DemeEventType
				{
					/** */
					EXPANSION( Expansion.class ),
					/** */
					ELIMINATION( Elimination.class ),
					/** */
					UNION( Union.class ),
					/** */
					SEPARATION( Separation.class ),
					/** */
					DIVISION( Division.class ),
					/** */
					RELOCATION( Relocation.class ),
					/** */
					IMMIGRATION( Immigration.class ),
					/** */
					EMIGRATION( Emigration.class );

					private final Class<? extends DemicalEvent> type;

					private DemeEventType(
						final Class<? extends DemicalEvent> type )
					{
						this.type = type;
					}

					public DemicalEvent create()
						throws InstantiationException, IllegalAccessException
					{
						return this.type.newInstance();
					}
				}

				@Key( "population-size" )
				@DefaultValue( "" + 3_000_000 )
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
			private SimpleDeme.SimpleConfig config;

			@Inject
			private Scheduler scheduler;

			@Inject
			private DataLayer data;

			@Inject
			private ProbabilityDistribution.Parser distParser;

			private Subject<DemoModel.EpiFact> events = PublishSubject.create();

			private final AtomicLong indSeq = new AtomicLong(),
					hhSeq = new AtomicLong();//, regSeq = new AtomicLong();

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
				LOG.trace( "Setting up {} with config: {}", this.config );

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
				indSeq.set( persons.stream()
						.mapToLong( pp -> pp.get( Persons.PersonSeq.class ) )
						.max().orElse( -1 ) );
				indSeq.incrementAndGet(); // advance to new sequence value
				hhSeq.set( households.keys()
						// store current hh keys for random picking
						.filter( householdKeys::add ).map( households::select )
						.mapToLong(
								hh -> hh.get( Households.HouseholdSeq.class ) )
						.max().orElse( -1 ) );
				hhSeq.incrementAndGet(); // advance to new sequence value

				final ProbabilityDistribution<Long> cultureDist = ProbabilityDistribution
						.createDeterministic( NA );
				final ProbabilityDistribution<BigDecimal> attitudeDist = ProbabilityDistribution
						.createDeterministic( BigDecimal.ZERO );
				final ProbabilityDistribution<Long> locationDist = ProbabilityDistribution
						.createDeterministic( NA );
				final TreeMap<BigDecimal, Object> fertileFemKeys = new TreeMap<>();

				LOG.trace( "Initializing households..." );
				households.creation().subscribe( hh ->
				{
					final Map<FamilyRank, Object> memberKeys =
							// setup household and member parameters
							setupHousehold( indSeq::getAndIncrement,
									persons::create, hh,
									hhSeq.getAndIncrement(), cultureDist.draw(),
									attitudeDist.draw(), locationDist.draw() );

					householdKeys.add( hh.key() );

					// index fertile female members
					final Object partnerKey = memberKeys
							.get( FamilyRank.PARTNER );
					if( partnerKey != null && !NA.equals( partnerKey ) )
					{
						final PersonTuple partner = persons
								.select( partnerKey );
						final BigDecimal partnerAge = nowDecimal().subtract(
								partner.get( Persons.BirthTime.class ) );
						if( fertilityAgeRange.contains( partnerAge ) )
							synchronized( fertileFemKeys )
						{
							fertileFemKeys.put( partnerAge, partnerKey );
						}
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
						households.create( new Households.Composition()
								.with( composition ) );
						return composition.size();
					} );

					scheduleDemeEvents().subscribe( eventType ->
					{
						switch( eventType )
						{
						case ELIMINATION: // TODO handle orphans
						case EMIGRATION:
						case UNION:
							final Object key = removeRandomKey( householdKeys );
							if( key != null )
							{
								households.remove( key );
								this.events.onNext( eventType.create() );
							} else
								LOG.warn( "No household to remove??" );
							break;

						case SEPARATION:
						case IMMIGRATION:
						case DIVISION:
							households.create( new Households.Composition()
									.with( hhCompositionDist.draw() ) );
							this.events.onNext( eventType.create() );
							break;

						case EXPANSION: // TODO update referent/partner status
						case RELOCATION: // TODO update home location
							this.events.onNext( eventType.create() );
							break;

						default:
							LOG.warn( "Unhandled event type: {}", eventType );
						}
					}, this.events::onError );
				} catch( final ParseException e )
				{
					this.events.onError( e );
				}

				synchronized( fertileFemKeys )
				{
					LOG.trace( "Fertile: {}", fertileFemKeys
							.size()/*
									 * .values().stream() .map( Object::toString
									 * ) .reduce( ( s1, s2 ) -> String .join(
									 * ", ", s1, s2 ) ) .orElse( "[]" )
									 */ );
				}

				scheduler().atEnd( t -> this.events.onComplete() );
				return this;
			}

			private Observable<DemeEventType> scheduleDemeEvents()
			{
				LOG.trace( "Scheduling deme-events à la Gillespie" );

				@SuppressWarnings( "unchecked" )
				// mean event interval per household = inverse event rate
				final ComparableQuantity<?> dt = this.config
						.householdEventDuration().to( scheduler().timeUnit() );
				// mean event interval across all households
				final BigDecimal mean = DecimalUtil.divide( dt.getValue(),
						this.hhSeq.get() );
				// event rate: i.i.d. exponential distribution
				final QuantityDistribution<?> hhEvDurDist = this.distParser
						.getFactory().createExponential( mean )
						.toQuantities( scheduler().timeUnit() );
				try
				{
					final ProbabilityDistribution<DemeEventType> nextEvent = this.distParser
							.<DemeEventType, DemeEventType>parse(
									this.config.householdEventTypeDist(),
									DemeEventType.class );
					return atEach( () -> (Infiniterator) () -> now()
							.add( hhEvDurDist.draw() ) )
									.map( t -> nextEvent.draw() );
				} catch( final ParseException e )
				{
					return Observable.error( e );
				}
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
				this.forkJoinPool.submit(
						() -> LongStream.range( 0, n ).parallel().forEach( i ->
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
									DecimalUtil.toScale(
											DecimalUtil.divide( i * 100, n ),
											1 ),
									DecimalUtil.toScale( DecimalUtil
											.divide( t - t0.get(), 1000 ), 1 ),
									DecimalUtil.toScale( DecimalUtil.divide(
											(i - i0) * 1000, t - ti ), 1 ) );
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
						DecimalUtil.toScale( DecimalUtil.divide( i * 1000, dt ),
								1 ) );
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

			private Map<FamilyRank, Object> setupHousehold(
				final Supplier<Long> ppSeq,
				final Supplier<PersonTuple> personFactory,
				final HouseholdTuple hh, final Long hhSeq,
				final Object cultureRef, final BigDecimal attitude,
				final Object homeLocationKey )
			{
				final CBSHousehold composition = hh
						.get( Households.Composition.class );
				hh.with( Households.HouseholdSeq.class, hhSeq )
						.with( Cultures.CultureRef.class, cultureRef )
//							.with( Households.Composition.class, composition )
						.with( Households.HomeLocationRef.class,
								homeLocationKey )
						.with( Households.Attitude.class, attitude );
				if( hh.get( Households.HouseholdSeq.class )
						.compareTo( hhSeq ) != 0 )
				{
					LOG.trace( "Retry optimistic setup of household #{} -> {}",
							hh.key(), hh );
					return setupHousehold( ppSeq, personFactory, hh, hhSeq,
							cultureRef, attitude, homeLocationKey );
				}
				final Map<FamilyRank, Object> result = new EnumMap<>(
						FamilyRank.class );
				final boolean refMale = true;
				final BigDecimal refBirth = BigDecimal.ZERO;
				result.put( FamilyRank.REFERENT,
						setupPerson( personFactory.get(), hh.key(), ppSeq.get(),
								FamilyRank.REFERENT, refMale, refBirth,
								homeLocationKey ).key() );

				if( composition.couple() ) // add partner
				{
					final boolean partnerMale = false;
					final BigDecimal partnerAge = QuantityUtil.decimalValue(
							QuantityUtil.valueOf(
									15. + this.distParser.getFactory()
											.getStream().nextDouble() * 35,
									TimeUnits.YEAR ),
							scheduler().timeUnit() );
					result.put( FamilyRank.PARTNER,
							setupPerson( personFactory.get(), hh.key(),
									ppSeq.get(), FamilyRank.PARTNER,
									partnerMale,
									nowDecimal().subtract( partnerAge ),
									homeLocationKey ).key() );
				}

				if( composition.childCount() > 0 ) // add first child
				{
					final boolean child1Male = false;
					final BigDecimal child1Birth = BigDecimal.ZERO;
					result.put( FamilyRank.CHILD1,
							setupPerson( personFactory.get(), hh.key(),
									ppSeq.get(), FamilyRank.CHILD1, child1Male,
									child1Birth, homeLocationKey ).key() );
				}

				if( composition.childCount() > 1 ) // add second child
				{
					final boolean child2Male = false;
					final BigDecimal child2Birth = BigDecimal.ZERO;
					result.put( FamilyRank.CHILD2,
							setupPerson( personFactory.get(), hh.key(),
									ppSeq.get(), FamilyRank.CHILD2, child2Male,
									child2Birth, homeLocationKey ).key() );
				}

				if( composition.childCount() > 2 ) // add third child
				{
					final boolean child3Male = false;
					final BigDecimal child3Birth = BigDecimal.ZERO;
					final PersonTuple child3 = setupPerson( personFactory.get(),
							hh.key(), ppSeq.get(), FamilyRank.CHILD3,
							child3Male, child3Birth, homeLocationKey );
					result.put( FamilyRank.CHILD3, child3.key() );
				}

				return result;
			}

			private PersonTuple setupPerson( final PersonTuple pp,
				final Object hhKey, final long ppSeq, final FamilyRank rank,
				final boolean male, final BigDecimal birthDay,
				final Object locationRef )
			{
				pp.with( Persons.PersonSeq.class, ppSeq )
						.with( Persons.HouseholdRef.class, hhKey )
						.with( Persons.MemberRank.class, rank )
						.with( Persons.BirthTime.class, birthDay )
						.with( Persons.Male.class, male )
						.with( Persons.SiteRef.class, locationRef );
				if( pp.get( Persons.PersonSeq.class ).compareTo( ppSeq ) != 0 )
				{
					LOG.trace( "Retry optimistic setup of person #{} -> {}",
							pp.key(), pp );
					return setupPerson( pp, hhKey, ppSeq, rank, male, birthDay,
							locationRef );
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