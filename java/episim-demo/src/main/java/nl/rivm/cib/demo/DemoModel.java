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

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.LongStream;

import javax.inject.Inject;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import com.eaio.uuid.UUID;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.enterprise.Transaction;
import io.coala.exception.Thrower;
import io.coala.function.ThrowingFunction;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.util.InputStreamConverter;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.epidemes.cbs.json.CBSBirthRank;
import nl.rivm.cib.epidemes.cbs.json.CBSGender;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.epidemes.cbs.json.CBSMotherAgeRange;
import nl.rivm.cib.epidemes.cbs.json.CBSPopulationDynamic;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.Cbs37201json;
import nl.rivm.cib.epidemes.cbs.json.Cbs37230json;
import nl.rivm.cib.epidemes.cbs.json.Cbs71486json;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.person.HouseholdComposition;

/**
 * {@link DemoModel}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface DemoModel
{
	Long NA = -1L;

	enum CBSFamilyRank
	{
		REFERENT, PARTNER, CHILD1, CHILD2, CHILD3, CHILDMORE;

		public boolean isAdult()
		{
			return ordinal() < 2;
		}

		public static CBSFamilyRank ofChildIndex( final int rank )
		{
			if( rank < 3 ) return values()[2 + rank];
//			throw new IllegalStateException( CBSFamilyRank.class.getSimpleName()
//					+ " undefined for child rank " + rank );
			return CHILDMORE;
		}
	}

	interface Cultures
	{
		@SuppressWarnings( "serial" )
		class CultureSeq extends AtomicReference<Object>
			implements Property<Object>
		{
			// track cultures through time (index key is data source dependent)
		}

		@SuppressWarnings( "serial" )
		class NormativeAttitude extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

		List<Class<?>> PROPERTIES = Arrays.asList( Cultures.CultureSeq.class,
				Cultures.NormativeAttitude.class );
	}

	interface Households
	{
		@SuppressWarnings( "serial" )
		class HouseholdSeq extends AtomicReference<Long>
			implements Property<Long>
		{
			// track households through time (index key is data source dependent)
		}

		@SuppressWarnings( "serial" )
		class Attitude extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

		@SuppressWarnings( { "serial", "rawtypes" } )
		class HomeRegionRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		@SuppressWarnings( "serial" )
		class HomeSiteRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		@SuppressWarnings( "serial" )
		class Composition extends AtomicReference<CBSHousehold>
			implements Property<CBSHousehold>
		{
		}

		@SuppressWarnings( "serial" )
		class MotherAgeRange extends AtomicReference<CBSMotherAgeRange>
			implements Property<CBSMotherAgeRange>
		{

		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				HomeRegionRef.class, HomeSiteRef.class, Composition.class,
				MotherAgeRange.class, Attitude.class, Cultures.CultureSeq.class,
				HouseholdSeq.class );

		class HouseholdTuple extends Tuple
		{

		}
	}

	interface Persons
	{
		@SuppressWarnings( "serial" )
		class PersonSeq extends AtomicReference<Long> implements Property<Long>
		{
			// increases monotone at every new initiation/birth/immigration/...
		}

		@SuppressWarnings( "serial" )
		class HouseholdRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		@SuppressWarnings( "serial" )
		class MemberRank extends AtomicReference<CBSFamilyRank>
			implements Property<CBSFamilyRank>
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

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				PersonSeq.class, HouseholdRef.class, MemberRank.class,
				Male.class, BirthTime.class, SiteRef.class );

		class PersonTuple extends Tuple
		{

		}
	}

	interface Regions
	{
		@SuppressWarnings( "serial" )
		class RegionName extends AtomicReference<String>
			implements Property<String>
		{

		}

		@SuppressWarnings( "serial" )
		class ParentRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		@SuppressWarnings( "serial" )
		class Population extends AtomicReference<Long> implements Property<Long>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays
				.asList( RegionName.class, ParentRef.class, Population.class );

		class RegionTuple extends Tuple
		{

		}
	}

	interface Sites
	{
		@SuppressWarnings( "serial" )
		class SiteName extends AtomicReference<String>
			implements Property<String>
		{

		}

		@SuppressWarnings( "serial" )
		class RegionRef extends AtomicReference<Object>
			implements Property<Object>
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

		@SuppressWarnings( "serial" )
		class Capacity extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		@SuppressWarnings( "serial" )
		class Occupancy extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				SiteName.class, RegionRef.class, Latitude.class,
				Longitude.class, Capacity.class, Occupancy.class );

		class SiteTuple extends Tuple
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

		interface Deme extends EpiActor
		{
			@Override
			Deme reset() throws Exception;

			@Override
			Observable<? extends DemicFact> events();

			abstract class DemicFact extends EpiFact
			{
				public Map<? extends HouseholdComposition, Integer> hhDelta = Collections
						.emptyMap();
				public Map<CBSFamilyRank, Integer> memberDelta = Collections
						.emptyMap();
				public UUID txRef = null; // population T30/rq
				public Object hhRef = NA; // inhabitant T30/init=T12/exec
				public Object siteRef = NA;

				public DemicFact withContext( final UUID t30rqRef,
					final Object hhRef, final Object siteRef )
				{
					this.txRef = t30rqRef;
					this.hhRef = hhRef;
					this.siteRef = siteRef;
					return this;
				}

				public DemicFact withHouseholdDelta(
					final Function<MapBuilder<CBSHousehold, Integer, ?>, Map<? extends HouseholdComposition, Integer>> mapBuilder )
				{
					this.hhDelta = mapBuilder.apply( MapBuilder
							.ordered( CBSHousehold.class, Integer.class ) );
					return this;
				}

				public DemicFact withMemberDelta(
					final Function<MapBuilder<CBSFamilyRank, Integer, ?>, Map<CBSFamilyRank, Integer>> mapBuilder )
				{
					this.memberDelta = mapBuilder.apply( MapBuilder
							.ordered( CBSFamilyRank.class, Integer.class ) );
					return this;
				}
			}

			class Preparation extends DemicFact
			{
				// conception, expecting a baby (triggers new behavior)
			}

			class Expansion extends DemicFact
			{
				// child birth, adoption, possibly 0 representing miscarriage etc
			}

			class Elimination extends DemicFact
			{
				// person dies, possibly leaving orphans, abandoning household
			}

			class Union extends DemicFact
			{
				// households merge, e.g. living together or marriage
			}

			class Separation extends DemicFact
			{
				// household splits, e.g. couple divorces
			}

			class Division extends DemicFact
			{
				// household splits, e.g. child reaches adulthood
			}

			class Relocation extends DemicFact
			{
//					HouseholdRef relocatedHHRef;
			}

			class Immigration extends DemicFact
			{
//					HouseholdRef immigratedHHRef;
			}

			class Emigration extends DemicFact
			{
//					HouseholdRef emigratedHHRef;
			}

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

				private final Class<? extends DemicFact> type;

				private DemeEventType( final Class<? extends DemicFact> type )
				{
					this.type = type;
				}

				public DemicFact create()
					throws InstantiationException, IllegalAccessException
				{
					return this.type.newInstance();
				}
			}
		}

		/** organizes survival and reproduction (across households) */
		class SimpleDeme implements Deme, Proactive
		{

			/** */
			private static final Logger LOG = LogUtil
					.getLogger( DemoModel.Demical.SimpleDeme.class );

			public interface DemeConfig extends YamlConfig
			{

				@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
				@Key( DemoConfig.CONFIG_BASE_KEY )
				String configBase();

				@Key( "population-size" )
				@DefaultValue( "" + 50_000 )
				int populationSize();

				@DefaultValue( "MUNICIPAL" )
				CBSRegionType cbsRegionLevel();

				@Key( "hh-type-birth-dist" )
				@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
						+ "37201_TS_2010_2015.json" )
				@ConverterClass( InputStreamConverter.class )
				InputStream cbs37201Data();

				@Key( "hh-type-dist" )
				@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
						+ "37230ned_TS_2012_2017.json" )
				@ConverterClass( InputStreamConverter.class )
				InputStream cbs37230Data();

				@Key( "hh-type-geo-dist" )
				@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
						+ "71486ned-TS-2010-2016.json" )
				@ConverterClass( InputStreamConverter.class )
				InputStream cbs71486Data();

				@Key( "pop-male-freq" )
				@DefaultValue( "0.5" )
				BigDecimal maleFreq();
			}

			/**
			 * historic an local demography event rates (births, deaths,
			 * migrations, ...)
			 */
			public static class CbsDemicEvents
			{
				final BigDecimal scalingFactor;
				final ProbabilityDistribution.Factory distFact;
				ConditionalDistribution<Cbs37230json.Category, LocalDate> periodDist;
				ConditionalDistribution<Cbs37230json.Category, LocalDate> siteDist;

				// <LocalDate, WeightedValue<Cbs37230json.Category>>
				public CbsDemicEvents( final CBSPopulationDynamic metric,
					final ProbabilityDistribution.Factory distFact,
					final Callable<InputStream> data,
					final CBSRegionType cbsRegionLevel,
					final Range<LocalDate> dtRange,
					final BigDecimal scalingFactor )
				{
					this.scalingFactor = scalingFactor;
					this.distFact = distFact;
					Cbs37230json.readAsync( data, dtRange, metric )
							.groupBy( wv -> wv.getValue().regionType() )
							.filter( g -> g.getKey() == cbsRegionLevel
									|| g.getKey() == CBSRegionType.COUNTRY )
							// GroupedObservable<CBSRegionType, WeightedValue<Cbs37230json.Category>>
							.blockingForEach( g ->
							{
								// event locations at configured regional level
								if( g.getKey() == cbsRegionLevel )
									this.siteDist = create( g );

								// event rates at national level, scaled to synth.pop.size?
								if( g.getKey() == CBSRegionType.COUNTRY )
									this.periodDist = create( g );
							} );
				}

				private
					ConditionalDistribution<Cbs37230json.Category, LocalDate>
					create(
						final GroupedObservable<CBSRegionType, WeightedValue<Cbs37230json.Category>> g )
				{
					// Navigable TreeMap to resolve out-of-bounds conditions
					return ConditionalDistribution
							.of( this.distFact::createCategorical,
									g.toMultimap( wv -> wv.getValue().offset(),
											wv -> wv, TreeMap::new )
											.blockingGet() );
				}

				public Quantity<Time> nextDelay( final LocalDate dt,
					final ThrowingFunction<String, Integer, Exception> eventSitePersonCounter )
					throws Exception
				{
					final String regRef = this.siteDist.draw( dt )
							.regionPeriod().regionRef();
					final int n = eventSitePersonCounter.apply( regRef );
					final QuantityDistribution<Time> timeDist = this.periodDist
							.draw( dt )
							.timeDist( freq -> this.distFact.createExponential(
									freq.divide( this.scalingFactor ) ) );
					Quantity<Time> dur = QuantityUtil.zero( Time.class );
					for( int i = 0; i < n; i++ )
						dur = dur.add( timeDist.draw() );
					return dur;
				}
			}

			@InjectConfig
			private DemeConfig config;

			@Inject
			private LocalBinder binder;

			@Inject
			private Scheduler scheduler;

			@Inject
			private DataLayer data;

			@Inject
			private ProbabilityDistribution.Parser distParser;

			@Inject
			private ProbabilityDistribution.Factory distFactory;

			private Subject<DemicFact> events = PublishSubject.create();

			@Override
			public Scheduler scheduler()
			{
				return this.scheduler;
			}

			@Override
			public Observable<DemicFact> events()
			{
				return this.events;
			}

			public Observable<Fact> emitFacts()
			{
				Fact.Simple.checkRegistered( JsonUtil.getJOM() );
				return events().map( ev ->
				{
					final Transaction.ID txId = Transaction.ID
							.create( this.binder.id() );
					final Class<? extends Fact> txKind = null;
					final Actor.ID initiatorRef = null;
					final Actor.ID executorRef = null;
					final Fact.Factory factFactory = null;
					final Transaction<?> tx = new Transaction.Simple<>( txId,
							txKind, initiatorRef, executorRef, this.scheduler,
							factFactory );

					final Fact.ID causeRef = null;
					final Fact.ID id = Fact.ID.create( tx.id() );
					final Instant occurrence = now();
					final FactKind coordKind = null;
					final Instant expiration = null;
					final Map<?, ?>[] properties = null;
					return new Fact.Simple( id, occurrence, tx, coordKind,
							expiration, causeRef, properties );
				} );
			}

			private Table<PersonTuple> persons;
			private final AtomicLong indSeq = new AtomicLong();
			private Table<HouseholdTuple> households;
			private Table.Partition hhRegTypeIndex;
			private Table.Partition hhMomAgeRegRankIndex;
			private final AtomicLong hhSeq = new AtomicLong();
//			private Table<RegionTuple> regions;
//			private final Map<String, Object> regionKeys = new HashMap<>();
			private Table<SiteTuple> sites;

			private ConditionalDistribution<Cbs71486json.Category, RegionPeriod> hhTypeDist;

			private transient CBSRegionType cbsRegionLevel = null;
			private transient Range<LocalDate> dtRange = null;
			private transient Instant dtInstant = null;
			private transient LocalDate dtCache = null;
			private transient BigDecimal dtScalingFactor = BigDecimal.ONE;

			protected LocalDate dt()
			{
				return now().equals( this.dtInstant ) ? this.dtCache
						: (this.dtCache = (this.dtInstant = now())
								.toJava8( this.dtRange.lowerValue() ));
			}

			@SuppressWarnings( { "unchecked", "rawtypes" } )
			@Override
			public SimpleDeme reset() throws Exception
			{
				this.events.onComplete();

				this.events = PublishSubject.create();
				LOG.trace( "Setting up {} with config: {}", this.config );

				// initialize tables
				this.households = this.data.createTable( HouseholdTuple.class );
				this.persons = this.data.createTable( PersonTuple.class );
//				this.regions = this.data.createTable( RegionTuple.class );
//				this.sites = this.data.createTable( SiteTuple.class );

				// initialize indices (used for random picking)
				this.hhRegTypeIndex = new Table.Partition( this.households );
				this.hhRegTypeIndex.groupBy( Households.HomeRegionRef.class );
				this.hhRegTypeIndex.groupBy( Households.Composition.class );
				this.hhMomAgeRegRankIndex = new Table.Partition(
						this.households );
				this.hhMomAgeRegRankIndex.groupBy(
						Households.MotherAgeRange.class,
						CBSMotherAgeRange::compare,
						Arrays.stream( CBSMotherAgeRange.values() ) );
				this.hhMomAgeRegRankIndex
						.groupBy( Households.HomeRegionRef.class );
				this.hhMomAgeRegRankIndex.groupBy( Households.Composition.class,
						( hh1, hh2 ) ->
						{
							final int adultComp = Integer.compare(
									hh1.adultCount(), hh2.adultCount() );
							return adultComp != 0 ? adultComp
									: Integer.compare( hh1.childCount(),
											hh2.childCount() );
						},
						Arrays.asList( CBSHousehold.DUO_NOKIDS,
								CBSHousehold.DUO_1KID, CBSHousehold.DUO_2KIDS,
								CBSHousehold.DUO_3PLUSKIDS ).stream() );

				// initialize context
				this.hhSeq.set(
						this.households.values( Households.HouseholdSeq.class )
								.mapToLong( seq -> seq ).max().orElse( 0 ) );
				this.indSeq.set( this.persons.values( Persons.PersonSeq.class )
						.mapToLong( seq -> seq ).max().orElse( 0 ) );
				this.dtRange = Range.upFromAndIncluding(
						scheduler().offset().toLocalDate() );
				this.cbsRegionLevel = this.config.cbsRegionLevel();
				this.dtScalingFactor = DecimalUtil // TODO read cbs pop size from cbs data
						.divide( this.config.populationSize(), 16_000_000 );

				final Map<RegionPeriod, Collection<WeightedValue<Cbs71486json.Category>>> values = Cbs71486json
						.readAsync( ( /* no :: on OWNER */ ) -> this.config
								.cbs71486Data(), this.dtRange )
						.filter( wv -> wv.getValue()
								.regionType() == this.cbsRegionLevel )
						.toMultimap( wv -> wv.getValue().regionPeriod(),
								Functions.identity(), TreeMap::new )
						.blockingGet();
				this.hhTypeDist = ConditionalDistribution
						.of( this.distFactory::createCategorical, values );

				// TODO from CBS
				final ProbabilityDistribution<Long> cultureDist = ProbabilityDistribution
						.createDeterministic( NA );
				// TODO from PIENTER2
				final ProbabilityDistribution<BigDecimal> attitudeDist = ProbabilityDistribution
						.createDeterministic( BigDecimal.ZERO );

				// read files and subscribe to all demical events
				Observable
						.fromArray( //
								setupBirths(), //
								setupDeaths(), //
								setupImmigrations( cultureDist, attitudeDist ),
								setupEmigrations() )
						.flatMap( ev -> ev ).subscribe( this.events );

				setupHouseholds( this.config.populationSize(), cultureDist,
						attitudeDist );

				// TODO RELOCATION, UNION, SEPARATION, DIVISION
				// TODO local partner/divorce rate, age, gender (CBS 37890)
				// TODO age diff (60036ned) 

				// TODO attitude exchange

				// TODO outbreak, vaccination

//				scheduler().atEnd( t -> this.events.onComplete() );
				return this;
			}

//			private Object regionFor( final String name )
//			{
//				// TODO look-up super-region reference in hierarchy
//				return this.regionKeys.computeIfAbsent( name, k -> this.regions
//						.insertValues(
//								map -> map.put( Regions.ParentRef.class, null )
//										.put( Regions.RegionName.class, name )
//										.put( Regions.Population.class, 0L ) )
//						.key() );
//			}

			private void setupHouseholds( final int n,
				final ProbabilityDistribution<Long> cultureDist,
				final ProbabilityDistribution<BigDecimal> attitudeDist )
			{
				LOG.trace( "Initializing households..." );
				final AtomicLong personCount = new AtomicLong(),
						lastCount = new AtomicLong(),
						t0 = new AtomicLong( System.currentTimeMillis() ),
						lastTime = new AtomicLong( t0.get() );

				final CountDownLatch latch = new CountDownLatch( 1 );
				// FIXME use observable's schedulers?
				final int nodes = Runtime.getRuntime().availableProcessors()
						- 1;
				new ForkJoinPool( nodes ).submit(
						() -> LongStream.range( 0, n ).parallel().forEach( i ->
						{
							try
							{
								if( personCount.get() < n )
								{
									final String regName = "";
									final RegionPeriod regPer = RegionPeriod
											.of( regName, dt() );
									personCount.addAndGet( createHousehold(
											this.hhSeq.incrementAndGet(),
											cultureDist.draw(),
											this.hhTypeDist.draw( regPer )
													.hhTypeDist(
															this.distFactory::createCategorical )
													.draw(),
											attitudeDist.draw(), NA ).size() );
								} else
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
							final long i = personCount.get(),
									i0 = lastCount.getAndSet( i ),
									t = System.currentTimeMillis(),
									ti = lastTime.getAndSet( t );
							LOG.trace(
									"Created {} of {} persons ({}%) in {}s, adding at {}/s...",
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
				final long i = personCount.get(),
						dt = System.currentTimeMillis() - t0.get();
				LOG.trace( "Created {} of {} persons in {}s at {}/s", i, n,
						DecimalUtil.toScale( DecimalUtil.divide( dt, 1000 ),
								1 ),
						DecimalUtil.toScale( DecimalUtil.divide( i * 1000, dt ),
								1 ) );
			}

			private Observable<DemicFact> setupBirths()
			{

				final ConditionalDistribution<Cbs37201json.Category, RegionPeriod> localBirthDist = ConditionalDistribution
						.of( this.distFactory::createCategorical, Cbs37201json
								.readAsync(
										( /* no :: on OWNER */ ) -> this.config
												.cbs37201Data(),
										this.dtRange )
								.filter( wv -> wv.getValue()
										.regionType() == this.cbsRegionLevel )
								// <RegionPeriod, WeightedValue<Cbs37201json.Category>>
								.toMultimap( wv -> wv.getValue().regionPeriod(),
										Functions.identity(), TreeMap::new )
								.blockingGet() );

				final Table.Partition hhMomIndex = new Table.Partition(
						this.households );
				hhMomIndex.groupBy( Households.MotherAgeRange.class );

				// initialize births
				final CbsDemicEvents births = new CbsDemicEvents(
						CBSPopulationDynamic.BIRTHS, this.distFactory,
						( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
						this.cbsRegionLevel, this.dtRange,
						this.dtScalingFactor );
				final AtomicReference<Cbs37201json.Category> pendingBirth = new AtomicReference<>();
				final AtomicReference<DemicFact> lastBirth = new AtomicReference<>();
				return infiniterate( () -> births.nextDelay( dt(), regRef ->
				{
					final Cbs37201json.Category birthCat = localBirthDist
							.draw( RegionPeriod.of( regRef, dt() ) ),
							oldCat = pendingBirth.getAndSet( birthCat );
					if( oldCat != null )
					{
						final PersonTuple newborn = expandFamily(
								pendingBirth.get() );
						LOG.trace( "Effectuated birth: {}", newborn );
						final HouseholdTuple hh = this.households.get(
								newborn.get( Persons.HouseholdRef.class ) );
						final CBSHousehold hhType = hh
								.get( Households.Composition.class ),
								hhTypeNew = hhType.plusChild();
						hh.set( Households.Composition.class, hhTypeNew );
						lastBirth.set( DemeEventType.EXPANSION.create()
								.withContext( null, hh.key(),
										hh.get( Households.HomeSiteRef.class ) )
								.withHouseholdDelta(
										map -> map.put( hhType, -1 )
												.put( hhTypeNew, +1 ).build() )
								.withMemberDelta( map -> map
										.put( newborn.get(
												Persons.MemberRank.class ), +1 )
										.build() ) );
					}
					return 1;
				} ) ).map( t -> lastBirth.get() );
			}

			private PersonTuple
				expandFamily( final Cbs37201json.Category birthCat )
			{
				// TODO marriage, multiplets (CBS 37422) 
				// TODO age diff (60036ned)

				final CBSMotherAgeRange momAge = birthCat
						.ageDist( this.distFactory::createCategorical ).draw();
				final CBSGender gender = birthCat
						.genderDist( this.distFactory::createCategorical )
						.draw();
				final CBSBirthRank rank = birthCat
						.rankDist( this.distFactory::createCategorical ).draw();
				LOG.trace( "{} {}: growing {} hh with {} child ({}), mom {}",
						dt(), DemeEventType.EXPANSION, birthCat.regionRef(),
						rank, gender, momAge );
				final CBSHousehold rankCat = rank == CBSBirthRank.FIRST
						? CBSHousehold.DUO_NOKIDS
						: rank == CBSBirthRank.SECOND ? CBSHousehold.DUO_1KID :
						//rank == CBSBirthRank.THIRD ? 
								CBSHousehold.DUO_2KIDS
				//: CBSHousehold.DUO_3PLUSKIDS
				;
				// pick family from index
				LOG.trace( "{}", this.hhMomAgeRegRankIndex );
				List<Object> keys = this.hhMomAgeRegRankIndex.keys( momAge,
						NA, rankCat );
				if( keys.isEmpty() ) // broaden: any rank
				{
					keys = this.hhMomAgeRegRankIndex.keys( momAge,
							birthCat.regionRef() );
					if( keys.isEmpty() ) // broaden: any age
					{
						keys = this.hhMomAgeRegRankIndex.keys( momAge );
						if( keys.isEmpty() ) // broaden: anywhere
						{
							keys = this.hhMomAgeRegRankIndex.keys();
							if( keys.isEmpty() ) return Thrower.throwNew(
									IllegalStateException::new,
									() -> "No population yet?" );
							LOG.warn(
									"Picking mom outside {} age != {} rank != {}: {}",
									birthCat.regionRef(), momAge, rank, keys );
						} else
							LOG.warn(
									"Picking mom outside {} aged {}, rank != {}: {}",
									birthCat.regionRef(), momAge, rank, keys );
					} else
						LOG.warn(
								"Picking mom inside {} aged: {}, rank != {}: {}",
								birthCat.regionRef(), momAge, rank, keys );
				}

				final Object hhRef = keys.get(
						this.distFactory.getStream().nextInt( keys.size() ) );
				final HouseholdTuple hh = this.households.get( hhRef );
				final CBSHousehold hhType = hh
						.get( Households.Composition.class );
				return createPerson( hhRef, this.indSeq.incrementAndGet(),
						CBSFamilyRank.ofChildIndex( hhType.childCount() ),
						gender.isMale(), now().decimal(),
						hh.get( Households.HomeSiteRef.class ) );
			}

			private Observable<DemicFact> setupDeaths()
			{
				final CbsDemicEvents deaths = new CbsDemicEvents(
						CBSPopulationDynamic.DEATHS, this.distFactory,
						( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
						this.cbsRegionLevel, this.dtRange,
						this.dtScalingFactor );
				final AtomicReference<String> pendingReg = new AtomicReference<>();
				return infiniterate( () -> deaths.nextDelay( dt(), nextRegRef ->
				{
					final String regRef = pendingReg.getAndSet( nextRegRef );

					if( regRef == null ) return 1;

					LOG.trace( "{} {}: shrinking {}", dt(),
							DemeEventType.ELIMINATION, regRef );
//					final CBSFamilyRank rank = CBSFamilyRank.CHILD1;
//					final Long hhRef = 0L; // TODO from local hh rank index
//					final HouseholdTuple hh = this.households
//							.get( hhRef );
//					final CBSHousehold hhType = hh
//							.get( Households.Composition.class );

					// TODO handle orphans
//					final Object indKey = removeRandomKey( indKeys );
//					final PersonTuple ind = persons.select( indKey );
//					hh.set( Households.Composition.class,
//							hhType.removeAdult() );
					return 1;
				} ) ).map( t -> DemeEventType.ELIMINATION.create() );
			}

			private Observable<DemicFact> setupImmigrations(
				// TODO make culture and attribute conditional on location
				final ProbabilityDistribution<Long> cultureDist,
				final ProbabilityDistribution<BigDecimal> attitudeDist )
			{
				final CbsDemicEvents immigrations = new CbsDemicEvents(
						CBSPopulationDynamic.IMMIGRATION, this.distFactory,
						( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
						this.cbsRegionLevel, this.dtRange,
						this.dtScalingFactor );

				final AtomicReference<String> pendingReg = new AtomicReference<>();
				return infiniterate(
						() -> immigrations.nextDelay( dt(), nextRegRef ->
						{
							final String regRef = pendingReg
									.getAndSet( nextRegRef );

							if( regRef == null ) return 1;

							final Cbs71486json.Category hhCat = this.hhTypeDist
									.draw( RegionPeriod.of( regRef, dt() ) );
							final CBSHousehold hhType = hhCat
									.hhTypeDist(
											this.distFactory::createCategorical )
									.draw();
							LOG.trace( "{} {}: joining {} of {} aged {}", dt(),
									DemeEventType.IMMIGRATION, regRef, hhType,
									hhCat.ageRange() );
//							createHousehold( this.hhSeq.incrementAndGet(),
//									cultureDist.draw(), hhType,
//									attitudeDist.draw(), regionFor( regRef ) );
							return hhType.size();
						} ) ).map( t -> DemeEventType.IMMIGRATION.create() );
			}

			private Observable<DemicFact> setupEmigrations()
			{
				final CbsDemicEvents emigrations = new CbsDemicEvents(
						CBSPopulationDynamic.EMIGRATION, this.distFactory,
						( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
						this.cbsRegionLevel, this.dtRange,
						this.dtScalingFactor );

				final AtomicReference<String> pendingReg = new AtomicReference<>();
				return infiniterate(
						() -> emigrations.nextDelay( dt(), nextRegRef ->
						{
							final String regRef = pendingReg
									.getAndSet( nextRegRef );

							if( regRef == null ) return 1;

							// TODO emigrating household type/members (70133NED)

							final Cbs71486json.Category hhCat = this.hhTypeDist
									.draw( RegionPeriod.of( regRef, dt() ) );

							final CBSHousehold hhType = hhCat
									.hhTypeDist(
											this.distFactory::createCategorical )
									.draw();

							// TODO select hh from reg/type index
							LOG.trace( "{} {}: leaving {} hh {} aged {}", dt(),
									DemeEventType.EMIGRATION, regRef, hhType,
									hhCat.ageRange() );
//							final Long hhRef = selectEmigrantHousehold( regRef,
//									hhType, hhCat.ageRange() );
//							final Observable<Entry<Actor.ID, Instant>> category = householdsFor(
//									regRef, hhType ).cache();
//							if( category.isEmpty().blockingGet() )
//							{
//								if( regRef == null ) return null;
//								if( hhType == null )
//									return selectEmigrantHousehold( null, null, hhRefAge );
//								return selectEmigrantHousehold( regRef, null, hhRefAge );
//							}
//							final Actor.ID result;
//							if( hhRefAge == null )
//								result = this.distFact.getStream().nextElement(
//										category.map( Entry::getKey ).toList().blockingGet() );
//							else
//							{
//								final Range<Instant> ageRange = hhRefAge.map( age -> now()
//										.subtract( Duration.of( age, TimeUnits.ANNUM ) ) );
//								final NavigableMap<Instant, Actor.ID> selection = (NavigableMap<Instant, Actor.ID>) category
//										.toMap( Entry::getValue, Entry::getKey, TreeMap::new )
//										.blockingGet();
//								final NavigableMap<Instant, Actor.ID> ageSelection = ageRange
//										.apply( selection, false );
//								if( ageSelection.isEmpty() )
//								{
//									// get nearest to age range
//									final Entry<Instant, ID> earlier = selection
//											.floorEntry( ageRange.lowerValue() );
//									final Entry<Instant, ID> later = selection
//											.ceilingEntry( ageRange.upperValue() );
//
//									result = later != null
//											? (earlier != null
//													? Compare.lt(
//															// distance below range
//															ageRange.lowerValue()
//																	.subtract( earlier.getKey() ),
//															// distance above range
//															later.getKey().subtract(
//																	ageRange.upperValue() ) )
//																			? earlier.getValue()
//																			: later.getValue()
//													: later.getValue())
//											: earlier != null ? earlier.getValue() : null;
//								} else
//									result = this.distFact.getStream()
//											.nextElement( ageSelection.values() );
//							}

//							final int size = this.households.get( hhRef )
//									.get( Households.Composition.class ).size();
//							this.households.delete( hhRef );
//							return size;
							return 1;
						} ) ).map( t -> DemeEventType.EMIGRATION.create() );
			}

			private Map<CBSFamilyRank, Object> createHousehold(
				final long hhSeq, final Object cultureRef,
				final CBSHousehold composition, final BigDecimal attitude,
				final Object homeLocationKey )
			{
				final HouseholdTuple hh = this.households
						.insertValues( map -> map
								.put( Households.Composition.class,
										composition )
								.put( Households.HouseholdSeq.class, hhSeq )
								.put( Cultures.CultureSeq.class, cultureRef )
								.put( Households.HomeRegionRef.class,
										homeLocationKey )
								.put( Households.Attitude.class, attitude ) );

//				while( hh.get( Households.HouseholdSeq.class )
//						.compareTo( hhSeq ) != 0 )
//					LOG.warn( "Retry optimistic setup of household #" + hh.key()
//							+ ": " + hh );

				Map<CBSFamilyRank, Object> result = new EnumMap<>(
						CBSFamilyRank.class );

				// add household's referent
				final boolean refMale = true;
				final double refAge = 18. // TODO
						+ this.distParser.getFactory().getStream().nextDouble()
								* 55;
				final BigDecimal refBirth = QuantityUtil.decimalValue(
						QuantityUtil.valueOf( refAge, TimeUnits.YEAR ),
						scheduler().timeUnit() );
				CBSMotherAgeRange range = refMale ? null
						: CBSMotherAgeRange.forAge( refAge );
				hh.set( Households.MotherAgeRange.class, range );
				final PersonTuple referent = createPerson( hh.key(),
						this.indSeq.incrementAndGet(), CBSFamilyRank.REFERENT,
						refMale, nowDecimal().subtract( refBirth ),
						homeLocationKey );
				result.put( CBSFamilyRank.REFERENT, referent.key() );

				// add household's partner
				if( composition.couple() ) // add partner
				{
					final boolean partnerMale = false; // TODO
					final double partnerAge = 15. + this.distParser.getFactory()
							.getStream().nextDouble() * 35;
					final CBSFamilyRank rank = CBSFamilyRank.PARTNER;
					final BigDecimal birth = QuantityUtil.decimalValue(
							QuantityUtil.valueOf( partnerAge, TimeUnits.YEAR ),
							scheduler().timeUnit() );
					final PersonTuple partner = createPerson( hh.key(),
							this.indSeq.incrementAndGet(), rank, partnerMale,
							nowDecimal().subtract( birth ), homeLocationKey );
					result.put( rank, partner.key() );

					// set fertility
					if( !partnerMale )
						range = CBSMotherAgeRange.forAge( refAge );
				}
				hh.set( Households.MotherAgeRange.class, range );

				// add household's children
				final CBSGender[] childGenders = {}; // composition.childCount()
				final double[] ages = {}; // TODO composition.childCount()
				for( int r = 0; r < ages.length; r++ )
				{
					final CBSFamilyRank rank = CBSFamilyRank.ofChildIndex( r );
					final BigDecimal birth = QuantityUtil.decimalValue(
							QuantityUtil.valueOf( ages[r], TimeUnits.YEAR ),
							scheduler().timeUnit() );
					final boolean male = childGenders[r].isMale();
					final PersonTuple child = createPerson( hh.key(),
							this.indSeq.incrementAndGet(), rank, male,
							nowDecimal().subtract( birth ), homeLocationKey );
					result.put( rank, child.key() );
				}

				return result;
			}

			private PersonTuple createPerson( final Object hhKey,
				final long ppSeq, final CBSFamilyRank rank, final boolean male,
				final BigDecimal birth, final Object locationRef )
			{
				return this.persons.insertValues(
						map -> map.put( Persons.PersonSeq.class, ppSeq )
								.put( Persons.HouseholdRef.class, hhKey )
								.put( Persons.MemberRank.class, rank )
								.put( Persons.BirthTime.class, birth )
								.put( Persons.Male.class, male )
								.put( Persons.SiteRef.class, locationRef ) );
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