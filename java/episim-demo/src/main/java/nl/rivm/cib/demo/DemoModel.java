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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import io.coala.data.IndexPartition;
import io.coala.data.Table;
import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.enterprise.Transaction;
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
import io.coala.time.Duration;
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
import nl.rivm.cib.demo.DemoModel.Regions.RegionTuple;
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
			return rank < 3 ? values()[2 + rank] : CHILDMORE;
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
		BigDecimal NO_MOM = BigDecimal.TEN.pow( 6 );

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
		class KidRank extends AtomicReference<CBSBirthRank>
			implements Property<CBSBirthRank>
		{
		}

		@SuppressWarnings( "serial" )
		class ReferentBirth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{

		}

		@SuppressWarnings( "serial" )
		class MomBirth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{

		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				HomeRegionRef.class, HomeSiteRef.class, Composition.class,
				KidRank.class, ReferentBirth.class, MomBirth.class,
				Attitude.class, Cultures.CultureSeq.class, HouseholdSeq.class );

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
		class Birth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

		@SuppressWarnings( "serial" )
		class SiteRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		@SuppressWarnings( { "serial", "rawtypes" } )
		class HomeRegionRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				PersonSeq.class, HouseholdRef.class, MemberRank.class,
				Male.class, Birth.class, HomeRegionRef.class,
				SiteRef.class );

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
				@DefaultValue( "" + 5_000 )
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

//			@Inject
//			private ProbabilityDistribution.Parser distParser;

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
			private IndexPartition ppRegAgeIndex;
			private final AtomicLong indSeq = new AtomicLong();
			private Table<HouseholdTuple> households;
			private final Map<Object, Set<Object>> householdMembers = new HashMap<>();
			private IndexPartition hhRegTypeBirthIndex;
			private IndexPartition hhMomRegRankBirthIndex;
			private final AtomicLong hhSeq = new AtomicLong();
			private Table<RegionTuple> regions;
			private final Map<String, Comparable<?>> regionKeys = new HashMap<>();
			private Table<SiteTuple> sites;
			private final Map<String, NavigableSet<Comparable<?>>> regionSites = new HashMap<>();

			private ConditionalDistribution<Object, String> regionalSiteDist;
			private ConditionalDistribution<Cbs71486json.Category, RegionPeriod> hhTypeDist;
			// TODO draw partner age difference from config or CBS dist
			private QuantityDistribution<Time> hhAgeDiffDist = () -> QuantityUtil
					.valueOf(
							Math.max( -3,
									Math.min( 1,
											this.distFactory.getStream()
													.nextGaussian() * 3 ) ),
							TimeUnits.YEAR );
			private ConditionalDistribution<Object, String> regionalCultureDist;
			private ConditionalDistribution<BigDecimal, Object> culturalAttitudeDist;

			private transient CBSRegionType cbsRegionLevel = null;
			private transient Range<LocalDate> dtRange = null;
			private transient Instant dtInstant = null;
			private transient LocalDate dtCache = null;
			private transient BigDecimal dtScalingFactor = BigDecimal.ONE;

			private LocalDate dt()
			{
				return now().equals( this.dtInstant ) ? this.dtCache
						: (this.dtCache = (this.dtInstant = now())
								.toJava8( this.dtRange.lowerValue() ));
			}

			private Comparable<?> toRegionKey( final String regName )
			{
				return this.regionKeys.computeIfAbsent( regName, k ->
				{
//					// TODO look-up in Cbs83287Json; update population
					final RegionTuple t = this.regions.insertValues(
							map -> map.put( Regions.RegionName.class, regName )
//							.put( Regions.ParentRef.class, null )
//							.put( Regions.Population.class, 0L ) )
					);
					return (Comparable<?>) t.key();
				} );
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
				this.regions = this.data.createTable( RegionTuple.class );
				this.sites = this.data.createTable( SiteTuple.class );
				this.regionalSiteDist = regName ->
				{
					// TODO get lat,long,zip from CBS distribution
					final SiteTuple t = this.sites.insertValues(
							map -> map.put( Sites.RegionRef.class, regName ) );
					this.regionSites
							.computeIfAbsent( regName, k -> new TreeSet<>() )
							.add( (Comparable<?>) t.key() );
					return t.key();
				};

				// initialize indices (used for random picking)
				this.hhRegTypeBirthIndex = new IndexPartition(
						this.households );
				this.hhRegTypeBirthIndex
						.groupBy( Households.HomeRegionRef.class );
				this.hhRegTypeBirthIndex
						.groupBy( Households.Composition.class );
				this.hhRegTypeBirthIndex
						.groupBy( Households.ReferentBirth.class,
								IntStream.range( 15,
										100 /* TODO plus sim length (yr) */ )
										.mapToObj( dt -> now()
												.subtract( Duration.of( dt,
														TimeUnits.YEAR ) )
												.decimal() )
										.collect( Collectors.toList() ) );

				this.ppRegAgeIndex = new IndexPartition( this.persons );
				this.ppRegAgeIndex.groupBy( Persons.HomeRegionRef.class );
				this.ppRegAgeIndex.groupBy( Persons.Birth.class, IntStream
						.range( 0, 100 /* TODO plus sim length (yr) */ )
						.mapToObj( dt -> now()
								.subtract( Duration.of( dt, TimeUnits.YEAR ) )
								.decimal() )
						.collect( Collectors.toList() ) );

				this.hhMomRegRankBirthIndex = new IndexPartition(
						this.households );
				this.hhMomRegRankBirthIndex
						.groupBy( Households.HomeRegionRef.class );
				this.hhMomRegRankBirthIndex.groupBy( Households.KidRank.class );
				this.hhMomRegRankBirthIndex.groupBy( Households.MomBirth.class,
						IntStream
								.range( 15, 50 /* TODO plus sim length (yr) */ )
								.mapToObj(
										dt -> now()
												.subtract( Duration.of( dt,
														TimeUnits.YEAR ) )
												.decimal() )
								.collect( Collectors.toList() ) );

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
				this.regionalCultureDist = regName -> NA;
				// TODO from PIENTER2
				this.culturalAttitudeDist = cult -> BigDecimal.ZERO;

				// read files and subscribe to all demical events
				Observable
						.fromArray( //
								setupBirths(), //
								setupDeaths(), //
								setupImmigrations(), setupEmigrations() )
						.flatMap( ev -> ev ).subscribe( this.events );

				setupHouseholds( this.config.populationSize() );
				LOG.trace( "household region-type-refBirth index: {}",
						this.hhRegTypeBirthIndex );
				LOG.trace( "fertility region-rank-birth index: {}",
						this.hhMomRegRankBirthIndex );
				LOG.trace( "persons birth index: {}", this.ppRegAgeIndex );

				// TODO RELOCATION, UNION, SEPARATION, DIVISION
				// TODO local partner/divorce rate, age, gender (CBS 37890)
				// TODO age diff (60036ned) 

				// TODO attitude exchange

				// TODO outbreak, vaccination
				return this;
			}

			private void setupHouseholds( final int n )
			{
				LOG.trace( "Initializing households..." );
				final AtomicLong personCount = new AtomicLong(),
						lastCount = new AtomicLong(),
						t0 = new AtomicLong( System.currentTimeMillis() ),
						lastTime = new AtomicLong( t0.get() );

				final CountDownLatch latch = new CountDownLatch( 1 );
				// FIXME find multi-threading RNG; use rxJava Schedulers#comp()?
//				final int nodes = Runtime.getRuntime().availableProcessors()
//						- 1;
//				new ForkJoinPool( nodes ).submit( () -> 
				LongStream.range( 0, n ) //.parallel()
						.forEach( i ->
						{
							try
							{
								if( personCount.get() < n )
								{
									final String regName = "";
									final RegionPeriod regPer = RegionPeriod
											.of( regName, dt() );
									final Cbs71486json.Category hhCat = this.hhTypeDist
											.draw( regPer );
									final HouseholdTuple hh = createHousehold(
											hhCat );
									personCount.addAndGet(
											hh.get( Households.Composition.class )
													.size() );
								} else
									latch.countDown();
							} catch( final Throwable t )
							{
								this.events.onError( t );
								latch.countDown();
							}
						} //) 
				);

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
						final PersonTuple newborn = expandHousehold(
								pendingBirth.get() );
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

					LOG.trace( "{} {}: TODO shrinking {}/{}", dt(),
							DemeEventType.ELIMINATION, regRef,
							toRegionKey( regRef ) );
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

			private Observable<DemicFact> setupImmigrations()
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
							return regRef == null ? 1
									: immigrateHousehold( this.hhTypeDist.draw(
											RegionPeriod.of( regRef, dt() ) ) );
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
							return regRef == null ? 1
									: emigrateHousehold( this.hhTypeDist.draw(
											RegionPeriod.of( regRef, dt() ) ) );
						} ) ).map( t -> DemeEventType.EMIGRATION.create() );
			}

			private HouseholdTuple
				createHousehold( final Cbs71486json.Category hhCat )
			{
				final Object cultureRef = this.regionalCultureDist
						.draw( hhCat.regionRef() );
				final CBSHousehold hhType = hhCat
						.hhTypeDist( this.distFactory::createCategorical )
						.draw();
				final Quantity<Time> refAge = hhCat
						.ageDist( this.distFactory::createUniformContinuous )
						.draw();
				final BigDecimal attitude = this.culturalAttitudeDist
						.draw( cultureRef );
				final Object homeLocationKey = this.regionalSiteDist
						.draw( hhCat.regionRef() );
				final long hhSeq = this.hhSeq.incrementAndGet();
				final BigDecimal refBirth = now()
						.subtract( Duration.of( refAge ) ).decimal();
				final BigDecimal partnerBirth = now()
						.subtract( Duration
								.of( refAge.add( this.hhAgeDiffDist.draw() ) ) )
						.decimal();
				final HouseholdTuple hh = this.households.insertValues(
						map -> map.put( Households.Composition.class, hhType )
								.put( Households.KidRank.class,
										CBSBirthRank.values()[hhType
												.childCount()] )
								.put( Households.HouseholdSeq.class, hhSeq )
								.put( Households.ReferentBirth.class, refBirth )
								.put( Households.MomBirth.class,
										hhType.couple() ? partnerBirth
												: Households.NO_MOM )
								.put( Cultures.CultureSeq.class, cultureRef )
								.put( Households.HomeSiteRef.class,
										homeLocationKey )
								.put( Households.HomeRegionRef.class,
										toRegionKey( hhCat.regionRef() ) )
								.put( Households.Attitude.class, attitude ) );

				// add household's referent
				final boolean refMale = true;
				createPerson( hh, CBSFamilyRank.REFERENT, refMale,
						nowDecimal().subtract( refBirth ), homeLocationKey );

				// add household's partner
				if( hhType.couple() )
				{
					final boolean partnerMale = !refMale; // TODO from CBS dist
					createPerson( hh, CBSFamilyRank.PARTNER, partnerMale,
							partnerBirth, homeLocationKey );
				}

				// add household's children
				for( int r = 0, n = hhType.childCount(); r < n; r++ )
				{
					// TODO kid age diff (60036ned)
					final Quantity<Time> refAgeOver15 = refAge.subtract(
							QuantityUtil.valueOf( 15, TimeUnits.YEAR ) );
					// equidistant ages: 0yr < age_1, .., age_n < (ref - 15yr)
					final BigDecimal birth = QuantityUtil.decimalValue(
							refAgeOver15.subtract(
									refAgeOver15.multiply( (.5 + r) / n ) ),
							scheduler().timeUnit() );
					final boolean childMale = this.distFactory.getStream()
							.nextBoolean();
					createPerson( hh, CBSFamilyRank.ofChildIndex( r ),
							childMale, nowDecimal().subtract( birth ),
							homeLocationKey );
				}
				return hh;
			}

			private PersonTuple createPerson( final HouseholdTuple hh,
				final CBSFamilyRank rank, final boolean male,
				final BigDecimal birth, final Object locationRef )
			{
				final long ppSeq = this.indSeq.incrementAndGet();
				final PersonTuple pp = this.persons.insertValues(
						map -> map.put( Persons.PersonSeq.class, ppSeq )
								.put( Persons.HouseholdRef.class, hh.key() )
								.put( Persons.MemberRank.class, rank )
								.put( Persons.Birth.class, birth )
								.put( Persons.Male.class, male )
								.put( Persons.HomeRegionRef.class,
										hh.get( Households.HomeRegionRef.class ) )
								.put( Persons.SiteRef.class, locationRef ) );
				this.householdMembers
						.computeIfAbsent( hh.key(), k -> new HashSet<>() )
						.add( pp.key() );
				return pp;
			}

			private PersonTuple
				expandHousehold( final Cbs37201json.Category birthCat )
			{
				// TODO marriage, multiplets (CBS 37422) 
				// TODO kid age diff (60036ned)

				final CBSMotherAgeRange momAge = birthCat
						.ageDist( this.distFactory::createCategorical ).draw();
				final CBSGender gender = birthCat
						.genderDist( this.distFactory::createCategorical )
						.draw();
				final CBSBirthRank kidRank = birthCat
						.rankDist( this.distFactory::createCategorical ).draw();
				LOG.trace( "{} {}: growing {}/{} hh with {} child ({}), mom {}",
						dt(), DemeEventType.EXPANSION, birthCat.regionRef(),
						toRegionKey( birthCat.regionRef() ), kidRank, gender,
						momAge );
				// pick family from index
				final Range<Instant> momBirth = momAge.range()
						.map( Duration::of ).map( dt -> now().subtract( dt ) );
				final List<Object> keys = this.hhMomRegRankBirthIndex
						.nearestKeys( ( k, r ) ->
						{
							LOG.trace( "Mom [{};{};{}={}] deviate: {} in {}",
									birthCat.regionRef(), kidRank, momAge,
									momBirth.map( t -> QuantityUtil.toScale(
											t.toQuantity(), TimeUnits.YEAR,
											1 ) ),

									k.getSimpleName(), r );
							return true;
						}, toRegionKey( birthCat.regionRef() ), kidRank,
								momBirth.map( Instant::decimal ) );

				final Object hhRef = keys.get(
						this.distFactory.getStream().nextInt( keys.size() ) );
				final HouseholdTuple hh = this.households.get( hhRef );
				final CBSFamilyRank rank = CBSFamilyRank.ofChildIndex(
						hh.get( Households.Composition.class ).childCount() );

				final PersonTuple pp = createPerson( hh, rank, gender.isMale(),
						now().decimal(),
						hh.get( Households.HomeSiteRef.class ) );
				hh.updateAndGet( Households.Composition.class,
						CBSHousehold::plusChild );
				hh.updateAndGet( Households.KidRank.class,
						CBSBirthRank::plusOne );
				return pp;
			}

			private int immigrateHousehold( final Cbs71486json.Category hhCat )
			{
				final HouseholdTuple hh = createHousehold( hhCat );
				final CBSHousehold hhType = hh
						.get( Households.Composition.class );
				LOG.trace( "{} {}: joining {}/{} of {} aged {}", dt(),
						DemeEventType.IMMIGRATION, hhCat.regionRef(),
						toRegionKey( hhCat.regionRef() ), hhType,
						hhCat.ageRange() );
				return this.householdMembers.get( hh.key() ).size();
			}

			private int emigrateHousehold( final Cbs71486json.Category hhCat )
			{
				final CBSHousehold hhType = hhCat
						.hhTypeDist( this.distFactory::createCategorical )
						.draw();

				final Range<BigDecimal> refAge = hhCat.ageRange()
						.map( BigDecimal::valueOf );
				LOG.trace( "{} {}: leaving {}/{} hh {} aged {}", dt(),
						DemeEventType.EMIGRATION, hhCat.regionRef(),
						toRegionKey( hhCat.regionRef() ), hhType, refAge );

				final List<Object> keys = this.hhRegTypeBirthIndex
						.nearestKeys( ( p, r ) ->
						{
							LOG.trace( "Emigrant [{};{};{}] deviate: {} in {}",
									hhCat.regionRef(), hhType, refAge,
									p.getSimpleName(), r );
							return true;
						}, toRegionKey( hhCat.regionRef() ), hhType, refAge );
				final Object hhRef = keys.get(
						this.distFactory.getStream().nextInt( keys.size() ) );
//				LOG.info( "Removing hh: " + hhRef );
				this.households.delete( hhRef );
				return this.householdMembers.remove( hhRef ).size();
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