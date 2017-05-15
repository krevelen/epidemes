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
package nl.rivm.cib.episim.pilot;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Actor.ID;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.LatLong;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import io.coala.util.Compare;
import io.coala.util.InputStreamConverter;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.observables.GroupedObservable;
import nl.rivm.cib.epidemes.cbs.json.CBSGender;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.epidemes.cbs.json.CBSPopulationDynamic;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.Cbs37201json;
import nl.rivm.cib.epidemes.cbs.json.Cbs37230json;
import nl.rivm.cib.epidemes.cbs.json.Cbs71486json;
import nl.rivm.cib.epidemes.cbs.json.CbsBoroughPC6json;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.cbs.TimeUtil;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.infection.Pathogen;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.locate.Transporter;
import nl.rivm.cib.episim.model.person.ConnectionType;
import nl.rivm.cib.episim.model.person.Domestic;
import nl.rivm.cib.episim.model.person.DomesticChange;
import nl.rivm.cib.episim.model.person.Gender;
import nl.rivm.cib.episim.model.person.HouseholdComposition;
import nl.rivm.cib.episim.model.person.HouseholdMember;
import nl.rivm.cib.episim.model.person.MeetingTemplate.Purpose;
import nl.rivm.cib.episim.model.person.Redirection;
import nl.rivm.cib.episim.model.person.Redirection.Director;
import nl.rivm.cib.episim.model.person.Residence;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.unit.Units;

/**
 * {@link OutbreakScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class OutbreakScenario implements Scenario
{

	public interface Config extends YamlConfig
	{
		@DefaultValue( "" + 1000 )
		int popSize();

//		@DefaultValue( "[18;50]" )
//		String refParentAge();

		@DefaultValue( "MUNICIPAL" )
		CBSRegionType cbsRegionLevel();

		@DefaultValue( "cbs/pc6_buurt.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbsBoroughPC6();

		@DefaultValue( "cbs/37201_TS_2010_2015.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs37201();

		@DefaultValue( "cbs/37713_2016JJ00_AllOrigins.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs37713();

		@DefaultValue( "cbs/71486ned-TS-2010-2016.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs71486();

		@DefaultValue( "cbs/37230ned_TS_2012_2017.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs37230();

		/**
		 * @return
		 */
		@DefaultValue( "[15:50]" )
		String momAgeRange();

//		@DefaultValue( "cbs/37975_2016JJ00.json" )
//		@ConverterClass( InputStreamConverter.class )
//		InputStream cbs37975();
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( OutbreakScenario.class );

	@InjectConfig
	private transient Config config;

	@Inject
	private transient Scheduler scheduler;

	@Inject
	private transient Actor.Factory actors;

	@Inject
	private transient ProbabilityDistribution.Factory distFact;

//		@Inject
//		private LocalBinder binder;

	/** spatial scope of analysis */
	private transient CBSRegionType cbsRegionLevel = null;

	/** the fallbackRegionRef, e.g. 'GM0363' = Amsterdam */
	private transient Region.ID fallbackRegionRef = Region.ID.of( "GM0363" );

	/** temporal scope of analysis */
	private transient Range<LocalDate> timeRange = null;

//	FIXME private Map<Region.ID, Region> regions = new TreeMap<>();

	/** population initialization and immigration */
//	private transient ConditionalDistribution<Cbs37713json.Category, ZonedDateTime> genderOriginDist = null;

	/** national monthly demographic rates (births, deaths, migrations, ...) */
	private transient Map<CBSPopulationDynamic, ConditionalDistribution<Cbs37230json.Category, LocalDate>> demogDelayDist = new EnumMap<>(
			CBSPopulationDynamic.class );

	/** regional monthly demographic places (births, deaths, migrations, ...) */
	private transient Map<CBSPopulationDynamic, ConditionalDistribution<Cbs37230json.Category, LocalDate>> demogRegionDist = new EnumMap<>(
			CBSPopulationDynamic.class );

	private transient ConditionalDistribution<CbsBoroughPC6json, Region.ID> hoodDist;
	/**
	 * household types and referent ages, e.g. based on empirical CBS 71486
	 * (composition) & 60036ned (age diff) & 37201 (mom age) data
	 */
	private transient ConditionalDistribution<Cbs71486json.Category, RegionPeriod> localHouseholdDist = null;

	private transient ConditionalDistribution<Cbs37201json.Category, RegionPeriod> localBirthDist = null;

	private void readMonthlyRates( final CBSPopulationDynamic metric,
		final BiConsumer<CBSPopulationDynamic, Map<LocalDate, Collection<WeightedValue<Cbs37230json.Category>>>> national,
		final BiConsumer<CBSPopulationDynamic, Map<LocalDate, Collection<WeightedValue<Cbs37230json.Category>>>> regional )
	{
		// FIXME parse and group by all metrics in a single parsing run
		Cbs37230json.readAsync( this.config::cbs37230, this.timeRange, metric )
				.groupBy( wv -> wv.getValue().regionType() ).blockingForEach( (
					GroupedObservable<CBSRegionType, WeightedValue<Cbs37230json.Category>> g ) ->
				{
					if( g.getKey() == this.cbsRegionLevel )
						regional.accept( metric,
								g.toMultimap( wv -> wv.getValue().offset(),
										wv -> wv, TreeMap::new )
										.blockingGet() );
					if( g.getKey() == CBSRegionType.COUNTRY ) // top level
						national.accept( metric,
								g.toMultimap( wv -> wv.getValue().offset(),
										wv -> wv, TreeMap::new )
										.blockingGet() );
				} );
	}

	// TODO partnership/marriage rate from CBS 37890 (region, age, gender) & 60036ned (age diff) ?

	// TODO separation/divorce rate from CBS 37890 (region, age, gender) ?

	private transient Instant dtInstant = null;
	private transient LocalDate dtCache = null;

	/** synthetic pop size / NL pop size */
	private BigDecimal popSizeFactor = BigDecimal.ONE;

	/** stats */
	private final AtomicLong immigrations = new AtomicLong();
	private final AtomicLong emigrations = new AtomicLong();
	private final AtomicLong households = new AtomicLong();
	private final AtomicLong persons = new AtomicLong();
	private final AtomicLong births = new AtomicLong();
	private final AtomicLong deaths = new AtomicLong();

	private Actor<Fact> deme;

	private Range<ComparableQuantity<Time>> momAgeRange;

	/**
	 * social network (household/opinions):
	 * <li>topology: households (marry/split, birth/death, migrate)
	 * <ul>
	 * <li>37230: #persons-births-deaths-migrations, per municipality / month
	 * '02-'17
	 * <li>71091: #splits: Scheiden; naar belangrijkste redenen, 2003
	 * <li>71488: #single-couple-married-kids-institute, per age 0:5:95+ /
	 * municipality / year '97-'16
	 * <li>37973: #single-couple-married-kids-institute, per
	 * ethnicity-generation / year '00-'16
	 * <li>37713: #persons, per ethnicity / age 0:5:95+ / municipality /
	 * annually '96-'16
	 * </ul>
	 * <li>pro/con, weight; parent, partner, sibling, work/classmate
	 * <li>contact network (locations): home, work, school, vehicle, shop,
	 * sports (mobility routines)
	 * <ul>
	 * <li>82249: Social contacts
	 * <li>81124: Mobility - motives
	 * <li>81125: Traffic participants
	 * <li>37856: Mobility vehicle possession
	 * </ul>
	 * <li>disease/vaccine
	 * <ul>
	 * <li>measles: SEIRS (Vink et al)
	 * <li>cost-effectiveness: per case
	 */
	@Override
	public void init() throws Exception
	{
		this.timeRange = Range
				.upFromAndIncluding( scheduler().offset().toLocalDate() );
		this.momAgeRange = Range.parse( this.config.momAgeRange() )
				.map( yr -> QuantityUtil.valueOf( yr, TimeUnits.ANNUM ) );
		LOG.trace( "Init; range: {}", timeRange );

		this.cbsRegionLevel = this.config.cbsRegionLevel();

		Arrays.asList( CBSPopulationDynamic.BIRTHS, CBSPopulationDynamic.DEATHS,
				CBSPopulationDynamic.IMMIGRATION,
				CBSPopulationDynamic.EMIGRATION )
				.forEach( metric -> readMonthlyRates( metric, ( key,
					map ) -> this.demogDelayDist.put( key,
							ConditionalDistribution.of(
									this.distFact::createCategorical, map ) ),
						( key, map ) -> this.demogRegionDist.put( key,
								ConditionalDistribution.of(
										this.distFact::createCategorical, map ) ) ) );

		this.localHouseholdDist = ConditionalDistribution.of(
				this.distFact::createCategorical,
				Cbs71486json.readAsync( this.config::cbs71486, this.timeRange )
						.filter( wv -> wv.getValue()
								.regionType() == this.cbsRegionLevel )
						.toMultimap( wv -> wv.getValue().regionPeriod(),
								Functions.identity(), () -> new TreeMap<>() )
						.blockingGet() );

		this.localBirthDist = ConditionalDistribution.of(
				this.distFact::createCategorical,
				Cbs37201json.readAsync( this.config::cbs37201, this.timeRange )
						.filter( wv -> wv.getValue()
								.regionType() == this.cbsRegionLevel )
						.toMultimap( wv -> wv.getValue().regionPeriod(),
								Functions.identity(),
								// Navigable Map for out-of-bounds conditions
								TreeMap::new )
						.blockingGet() );

		final Map<Region.ID, ProbabilityDistribution<CbsBoroughPC6json>> async = CbsBoroughPC6json
				.readAsync( this.config::cbsBoroughPC6 )
				.toMultimap( bu -> bu.regionRef( this.cbsRegionLevel ),
						CbsBoroughPC6json::toWeightedValue )
				.blockingGet().entrySet().parallelStream() // blocking
				.collect( Collectors.toMap( e -> e.getKey(), e -> this.distFact
						.createCategorical( e.getValue() ) ) );

		this.hoodDist = ConditionalDistribution
				.of( id -> async.computeIfAbsent( id, key ->
				{
					LOG.warn( "Missing zipcodes for {}, falling back to {}",
							key, this.fallbackRegionRef );
					return async.get( this.fallbackRegionRef );
				} ) );

//		this.genderOriginDist = ConditionalDistribution.of(
//				this.distFact::createCategorical,
//				Cbs37713json.readAsync( this.config::cbs37713, this.timeRange )
//						.filter( wv -> wv.getValue()
//								.regionType() == this.cbsRegionLevel )
//						.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
//								() -> new TreeMap<>() )
//						.blockingGet() );

//		Cbs37975json
//				.readAsync( this.config::cbs37975 )
//				.toList().blockingGet();

		// set DEME behavior for maintaining demographics
		this.deme = this.actors.create( "DEME" );
		this.deme.specialist( Residence.Deme.class, deme ->
		{
			// trigger births
			deme.atEach( timing( CBSPopulationDynamic.BIRTHS,
					() -> createOffspring().size() ) ).subscribe( t ->
					{
						// empty
					}, this::error );
			// trigger deaths
			deme.atEach( timing( CBSPopulationDynamic.DEATHS, () -> 1 ) )
					.subscribe( t -> deme.initiate( DomesticChange.Death.class,
							selectDiseased() ).commit() );
			// trigger immigrations
			deme.atEach( timing( CBSPopulationDynamic.IMMIGRATION,
					() -> createCBSHousehold( false ).size() ) ).subscribe( t ->
					{
						// empty
					}, this::error );
			// trigger emigrations
			deme.atEach( timing( CBSPopulationDynamic.EMIGRATION,
					() -> removeCBSHousehold().size() ) ).subscribe( t ->
					{
						// empty
					}, this::error );
			// register new households in residence
			deme.emit( Residence.class, FactKind.REQUESTED ).subscribe( rq ->
			{
				registerHousehold( rq.getRegionRef(), rq.getComposition() ).put(
						rq.creatorRef(),
						rq.member( rq.referentRef() ).birth() );
			}, this::error );
			// update household registry
			deme.emit( DomesticChange.class, FactKind.STATED ).subscribe( st ->
			{
				// TODO re-registerHousehold(st.creatorRef(), regRef, hhType, hhRefAge)
				if( st instanceof DomesticChange.Immigrate )
				{
					this.immigrations.incrementAndGet();
				} else if( st instanceof DomesticChange.Emigrate )
				{
					this.emigrations.incrementAndGet();
				} else if( st instanceof DomesticChange.Birth )
//					this.persons.computeIfAbsent( ConnectionType.Simple.WARD,
//							key -> new AtomicLong() ).incrementAndGet();
					this.births.incrementAndGet();
				else if( st instanceof DomesticChange.Death )
//					this.persons.computeIfAbsent( st.getComposition(),
//							key -> new AtomicLong() ).decrementAndGet();
					this.deaths.incrementAndGet();
//				else if( st instanceof DomesticChange.LeaveHome )
//				else if( st instanceof DomesticChange.SplitHome )
//				else if( st instanceof DomesticChange.MergeHome )
			}, this::error );
		} );

		// populate households
		for( long start = System
				.currentTimeMillis(), size = 0, popSize = this.config
						.popSize(); size < popSize; size += createCBSHousehold(
								true ).size() )
		{
			if( System.currentTimeMillis() - start > 1000 )
			{
				start = System.currentTimeMillis();
				LOG.trace( "size = {} of {}, hh count: {}", this.persons.get(),
						popSize, this.households.get() );
			}
			;
		}

		final LocalDateTime offset = this.timeRange.lowerValue().atStartOfDay();
		atEach( Timing.of( "0 0 * * * ?" )
				.offset( dt().atStartOfDay( TimeUtil.NL_TZ ) ).iterate() )
						.subscribe( t ->
						{
							LOG.info( "t={}, hh[{}+{}-{}] pp[{}+{}-{}]",
									t.prettify( offset ), this.households.get(),
									this.immigrations.getAndSet( 0L ),
									this.emigrations.getAndSet( 0L ),
									this.persons.get(),
									this.births.getAndSet( 0L ),
									this.deaths.getAndSet( 0L ) );
						}, this::error );

		// TODO initiate Infections: diseases/outbreaks, vaccine/interventions

		LOG.info( "Initialized model" );
	}

	private void error( final Throwable e )
	{
		LOG.error( "Problem occurred", e );
	}

//	private final Map<Region.ID, NavigableMap<ZonedDateTime, Actor.ID>> localAgeMothers = new HashMap<>();

	private transient final Map<Region.ID, Map<HouseholdComposition, NavigableMap<Actor.ID, Instant>>> hhReg = new HashMap<>();

	protected NavigableMap<Actor.ID, Instant> registerHousehold(
		final Region.ID regRef, final HouseholdComposition hhType )
	{
		// registry of all households of given composition in given region
		return this.hhReg.computeIfAbsent( regRef, key -> new HashMap<>() )
				.computeIfAbsent( hhType, key -> new TreeMap<>() );
	}

	protected Observable<Entry<Actor.ID, Instant>> householdRegistry(
		final Region.ID regFilter, final HouseholdComposition... hhFilter )
	{
		if( this.hhReg.isEmpty() ) return Observable.empty();

		if( regFilter == null )
			// copy from all regions and compositions
			return Observable.fromIterable( this.hhReg.entrySet() )
					.flatMap( byReg -> Observable
							.fromIterable( byReg.getValue().entrySet() ) )
					.flatMap( byType -> Observable
							.fromIterable( byType.getValue().entrySet() ) );

		if( hhFilter == null || hhFilter.length == 0
				|| (hhFilter.length == 1 && hhFilter[0] == null) )
			// copy from all compositions in given region
			// FIXME toMultiMap a la guava: http://stackoverflow.com/a/23003630/1418999
			return Observable
					.fromIterable(
							this.hhReg
									.computeIfAbsent( regFilter,
											key -> new HashMap<>() )
									.entrySet() )
					.flatMap( byType -> Observable
							.fromIterable( byType.getValue().entrySet() ) );

		// copy from given compositions in given region
		return Observable.fromArray( hhFilter )
				.flatMap( hhType -> Observable.fromIterable( this.hhReg
						.computeIfAbsent( regFilter, key -> new HashMap<>() )
						.computeIfAbsent( hhType, key -> new TreeMap<>() )
						.entrySet() ) );
	}

	private static final HouseholdComposition[] COMPOSITIONS_WITH_MOTHERS = Arrays
			.stream( CBSHousehold.values() )
			.filter( hh -> hh.partnerRelationType() != null
					&& hh.partnerRelationType().isSexual() )
			.toArray( n -> new HouseholdComposition[n] );

	protected Actor.ID selectNest( final Region.ID regRef,
		final Range<ComparableQuantity<Time>> momAgeRange )
	// TODO , final int siblingCount, final int multiplets 
	{

		final Observable<Entry<ID, Instant>> localMothers = householdRegistry(
				regRef, COMPOSITIONS_WITH_MOTHERS ).cache();
		final Range<Instant> birthRange = momAgeRange
				// TODO subtract average referent age difference?
				.map( age -> now().subtract( age ) );
		final List<Actor.ID> ageFilter = (localMothers.isEmpty().blockingGet()
				? householdRegistry( null, COMPOSITIONS_WITH_MOTHERS )
				: localMothers)
						.filter( e -> birthRange.contains( e.getValue() ) )
						.map( Entry::getKey ).toList().blockingGet();

		return ageFilter.isEmpty() ? null
				: this.distFact.getStream().nextElement( ageFilter );
	}

	protected Actor.ID selectDiseased()
	{
//		final Region.ID regRef = 
		demogSite( CBSPopulationDynamic.DEATHS );

//		LOG.trace( "t={}, death occurs in {}", nowPretty(), regRef );

		// TODO deaths, add 83190ned (age, gender, position)
		return this.deme.id(); // FIXME
	}

	protected Actor.ID selectEmigrantHousehold( final Region.ID regRef,
		final HouseholdComposition hhType, final Range<Integer> hhRefAge )
	{
		final Observable<Entry<Actor.ID, Instant>> category = householdRegistry(
				regRef, hhType ).cache();
		if( category.isEmpty().blockingGet() )
		{
			if( regRef == null ) return null;
			if( hhType == null )
				return selectEmigrantHousehold( null, null, hhRefAge );
			return selectEmigrantHousehold( regRef, null, hhRefAge );
		}
		final Actor.ID result;
		if( hhRefAge == null )
			result = this.distFact.getStream().nextElement(
					category.map( Entry::getKey ).toList().blockingGet() );
		else
		{
			final Range<Instant> ageRange = hhRefAge.map( age -> now()
					.subtract( Duration.of( age, TimeUnits.ANNUM ) ) );
			final NavigableMap<Instant, Actor.ID> selection = (NavigableMap<Instant, Actor.ID>) category
					.toMap( Entry::getValue, Entry::getKey, TreeMap::new )
					.blockingGet();
			final NavigableMap<Instant, Actor.ID> ageSelection = ageRange
					.apply( selection, false );
			if( ageSelection.isEmpty() )
			{
				// get nearest to age range
				final Entry<Instant, ID> earlier = selection
						.floorEntry( ageRange.lowerValue() );
				final Entry<Instant, ID> later = selection
						.ceilingEntry( ageRange.upperValue() );

				result = later != null
						? (earlier != null
								? Compare.lt(
										// distance below range
										ageRange.lowerValue()
												.subtract( earlier.getKey() ),
										// distance above range
										later.getKey().subtract(
												ageRange.upperValue() ) )
														? earlier.getValue()
														: later.getValue()
								: later.getValue())
						: earlier != null ? earlier.getValue() : null;
			} else
				result = this.distFact.getStream()
						.nextElement( ageSelection.values() );
		}
		return result;
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	protected LocalDate dt()
	{
		return now().equals( this.dtInstant ) ? this.dtCache
				: (this.dtCache = (this.dtInstant = now())
						.toJava8( this.timeRange.lowerValue() ));
	}

	protected Pretty minutes( final Quantity<Time> qty, final int scale )
	{
		return QuantityUtil.pretty( qty, TimeUnits.MINUTE, scale );
	}

	protected Pretty nowPretty()
	{
		return now().prettify( this.timeRange.lowerValue() );
	}

	private static Map<String, Region.ID> REGION_ID_CACHE = new TreeMap<>();

	protected Region.ID demogSite( final CBSPopulationDynamic metric )
	{
		return REGION_ID_CACHE.computeIfAbsent(
				this.demogRegionDist.get( metric ).draw( dt() ).regionRef(),
				key -> Region.ID.of( key.trim() ) );
	}

	protected Iterable<Instant> timing( final CBSPopulationDynamic metric,
		final IntSupplier hhSize )
	{
		return () -> (Infiniterator) () ->
		{
			final QuantityDistribution<Time> timeDist = this.demogDelayDist
					.get( metric ).draw( dt() )
					.timeDist( freq -> this.distFact.createExponential(
							freq.multiply( this.popSizeFactor ) ) );

			Quantity<Time> dur = timeDist.draw();
			for( int n = hhSize.getAsInt(); n > 1; n-- )
				dur = dur.add( timeDist.draw() );

			return now().add( dur );
		};
	}

	protected Map<ID, HouseholdMember>
		createCBSHousehold( final boolean initial )
	{
		final LocalDate dt = dt();
		// TODO immigration, add 37713 (gender, origin)

		Region.ID regRef = demogSite( CBSPopulationDynamic.IMMIGRATION );
		final CbsBoroughPC6json buurt = this.hoodDist.draw( regRef );
		// regRef may not exist in 2016 zipcode data, update to fallback value
		regRef = buurt.regionRef( this.cbsRegionLevel );
		final Place.ID zip = buurt.zipDist( this.distFact::createCategorical )
				.draw();

		final Cbs71486json.Category hhCat = this.localHouseholdDist
				.draw( RegionPeriod.of( regRef, dt ) );
		final Quantity<Time> hhRefAge = hhCat
				.ageDist( this.distFact::createUniformContinuous ).draw();
		// TODO create household members with ages relative to hhRefAge
		final CBSHousehold hhType = hhCat
				.hhTypeDist( this.distFact::createCategorical ).draw();

		final Instant referentBirth = now().subtract( hhRefAge );
		final Gender referentGender = CBSGender.MALE; // TODO draw ?
		final Actor.ID referentRef = createPerson( referentGender,
				referentBirth );
		final Map<Actor.ID, HouseholdMember> members = new HashMap<>();
		members.put( referentRef, HouseholdMember.of( referentBirth,
				hhType.partnerRelationType() ) );
		for( int i = 1; i < hhType.adultCount(); i++ )
		{
			final Instant birth = referentBirth.add( 3 * i ); // TODO draw ?
			members.put( createPerson( CBSGender.FEMALE, birth ),
					HouseholdMember.of( birth, hhType.partnerRelationType() ) );
		}
		for( int i = 0; i < hhType.childCount(); i++ )
		{
			final Instant birth = referentBirth.add( 25 + i * 2 ); // TODO draw ?
			final Gender gender = CBSGender.FEMALE;
			members.put( createPerson( gender, birth ),
					HouseholdMember.of( birth, ConnectionType.Simple.WARD ) );
		}
		final Actor.ID hhRef = createHousehold( regRef, zip, null, referentRef,
				hhType, members );

		if( !initial ) this.deme
				.initiate( DomesticChange.Immigrate.class, hhRef ).commit();

		return members;
	}

	protected Actor.ID createHousehold( final Region.ID regRef,
		final Place.ID placeRef, final LatLong coord,
		final Actor.ID referentRef, final HouseholdComposition composition,
		final Map<Actor.ID, HouseholdMember> members )
	{
		// create the agent
		final Actor<Fact> actor = this.actors.create(
				String.format( "hh%08d", this.households.incrementAndGet() ) );
		// set household domestic attributes
		final DomesticChange.Household hh = actor
				.subRole( DomesticChange.Household.class ).with( regRef )
				.with( placeRef ).with( coord ).with( referentRef )
				.with( members ).with( composition );
		// set birth handling
		hh.emit( DomesticChange.Birth.class ).subscribe( rq ->
		{
			final Actor.ID babyRef = createPerson( rq.getGender(), now() );
			// notify family members FIXME extended kin?
//			hh.getNetwork()
//					.forEach( ( ref, rel ) -> hh
//							.initiate( Redirection.Relation.class, ref )
//							.with( rel ).with( babyRef ).commit() );
			// adopt into family
			hh.getMembers().put( babyRef,
					HouseholdMember.of( now(), ConnectionType.Simple.WARD ) );
			// confirm to registry
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit();
		} );
		// set death handling
		hh.emit( DomesticChange.Death.class ).subscribe( rq ->
		{
			final Actor.ID diseasedRef = rq.diseasedRef();
			members.remove( diseasedRef );
			hh.initiate( Redirection.Termination.class, diseasedRef ).commit();
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit();
		} );
		// set family merger handling
		hh.emit( DomesticChange.MergeHome.class ).subscribe( rq ->
		{
			members.putAll( rq.arrivingRefs() );
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit();
		} );
		// set family split handling
		hh.emit( DomesticChange.SplitHome.class ).subscribe( rq ->
		{
//			final Map<ID, HouseholdMember> members = hh.getMembers();
			rq.departingRefs().keySet().forEach(
					ref -> members.computeIfPresent( ref, ( key, pos ) ->
					{
						// if a sexual member departs, all become non-sexual
//						if( pos.isSexual() )
//							members.forEach( ( otherRef, otherPos ) ->
//							{
//								if( !otherRef.equals( ref )
//										&& otherPos.isSexual() )
//									hh.initiate( Redirection.Relation.class,
//											ref ).with( otherRef )
//											.with( RelationType.Simple.SOCIAL )
//											.commit();
//							} );
						return null; // remove
					} ) );
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit();
		} );
		// set new household handling
		hh.emit( DomesticChange.LeaveHome.class ).subscribe( rq ->
		{
			if( members.remove( rq.departingRef() ) == null )
			{
				LOG.warn( "{} can't leave another home {}", rq.departingRef(),
						hh.id().organizationRef() );
				return;
			}
			createHousehold( rq.getRegionRef(), rq.getPlaceRef(),
					rq.getPosition(), rq.departingRef(), rq.getComposition(),
					MapBuilder.<Actor.ID, HouseholdMember>unordered()
							.put( rq.departingRef(),
									HouseholdMember.of(
											members.get( rq.departingRef() )
													.birth(),
											ConnectionType.Simple.SINGLE ) )
							.build() );
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit();
		} );
		// set added household handling
		hh.emit( DomesticChange.Immigrate.class ).subscribe( rq ->
		{
			hh.respond( rq, FactKind.STATED ).with( hh ).commit();
		} );
		// set removed household handling
		hh.emit( DomesticChange.Emigrate.class ).subscribe( rq ->
		{
			members.keySet().forEach( this::removePerson );
			hh.respond( rq, FactKind.STATED ).with( hh ).commit();
		} );
//		if( composition != null ) composition.forEach(
//				( ref1, rel1 ) -> composition.forEach( ( ref2, rel2 ) ->
//				{
//					if( !ref1.equals( ref2 ) )
//					{
//						final RelationType rel = rel1.isSexual()
//								&& rel2.isSexual() ? RelationType.Simple.SEXUAL
//										: RelationType.Simple.SOCIAL;
//						hh.initiate( Redirection.Relation.class, ref1 )
//								.with( ref2 ).with( rel ).commit();
//						hh.initiate( Redirection.Relation.class, ref2 )
//								.with( ref1 ).with( rel ).commit();
//					}
//				} ) );
		hh.initiate( Residence.class, this.deme.id() ).with( regRef )
				.with( placeRef ).with( coord ).with( referentRef )
				.with( composition ).with( members ).commit();
		return hh.id();
	}

	/**
	 * @param members
	 * @return
	 */
	private <T extends Domestic<T>> T updateComposition( final T household )
	{
		return household.with( CBSHousehold.of( household ) );
	}

	protected Actor.ID createPerson( final Gender gender, final Instant birth )
	{
		final Actor<Fact> person = this.actors
				.create( this.persons.incrementAndGet() );

//		final Motivator motiv = person.specialist( Motivator.class )
//				.with( rq.getAttitude() );

		final Director dir = person.subRole( Director.class ).with( gender )
				.with( birth );
		dir.emit( Redirection.Relation.class ).subscribe( rq ->
		{
			// TODO join(rq)/split(rj), ...
		} );
		dir.emit( Redirection.Commitment.class ).subscribe( rq ->
		{
			// TODO routine ...
		} );

//		final Category profile = this.genderOriginDist
//				.draw( genderDist.draw() );
//		person.with( "male", profile.gender() );
//		person.with( "age", profile
//				.ageDist( this.distFact::createUniformContinuous ).draw() );
//		person.with( "regionRef", profile.regionId() );
//		person.specialist( Redirection.Director.class )
//				// add Redirection/request handler
//				.emit( Actor.RQ_FILTER, ( role, rq ) ->
//				{
//					LOG.trace( "role {} rq {} routine {}", role, rq,
//							rq.getRoutine() );
//
//					if( Routine.STANDARD.equals( rq.getRoutine() ) )
//					{
//						// FIXME use factory to schedule this persons life
//
//						role.initiate( Plan.class, person.id() ).commit();
//					}
//					// cancel prior engagements
//				} )
//				// self-initiate behavior redirection
//				.initiate( person.id() ).with( Routine.STANDARD ).commit();
//		atEach( Timing.of( "0 0 8 ? * MON-FRI" ).offset( dt ).iterate(), t ->
//		{
//			LOG.trace( "t={}, person ", nowPretty() );
//			person.specialist( Activity.Activator.class ).initiate( Visit.class,
//					home.id(),
//					now().add( Duration.of( 10, TimeUnits.HOURS ) ) );
//		} );

		// TODO initiate Mixing: spaces/transports, mobility

		return person.id();
	}

	protected void removePerson( final Actor.ID personRef )
	{
		// TODO unregister contacts etc
	}

	protected Collection<Fact> createOffspring()
	{
		// TODO draw multiplets?
		final Region.ID regRef = demogSite( CBSPopulationDynamic.BIRTHS );
		final Cbs37201json.Category cat = this.localBirthDist
				.draw( RegionPeriod.of( regRef, dt() ) );
		final Range<ComparableQuantity<Time>> momAgeRange = cat
				.ageDist( this.distFact::createCategorical ).draw().range();
		// TODO mom age, siblings, marriage (CBS 37201), multiplets (CBS 37422) 
//		final int siblingRank = cat.rankDist( this.distFact::createCategorical )
//				.draw().rank();
		Actor.ID hhRef = selectNest( regRef, momAgeRange );
		// retry for full mother age range
		if( hhRef == null ) hhRef = selectNest( regRef, this.momAgeRange );
		if( hhRef == null )
		{
			LOG.trace( "t={}, no mothers (yet) in {} aged {}, fall back to {}",
					nowPretty(), regRef, this.momAgeRange,
					this.fallbackRegionRef );
			hhRef = selectNest( this.fallbackRegionRef, momAgeRange );
			if( hhRef == null )
				hhRef = selectNest( this.fallbackRegionRef, this.momAgeRange );
		}
		if( hhRef == null )
		{
			LOG.warn( "t={}, no fall-back mothers (yet) in {}, referent age {}",
					nowPretty(), this.fallbackRegionRef, this.momAgeRange );
			return Collections.emptySet();
		}

		final Gender babyGender = cat
				.genderDist( this.distFact::createCategorical ).draw();

		// may be multiple facts later...
		return Collections.singleton(
				this.deme.initiate( DomesticChange.Birth.class, hhRef )
						.with( babyGender ).commit() );
	}

	protected CBSHousehold removeCBSHousehold() // emigration
	{
		final LocalDate dt = dt();

		final Region.ID regRef = demogSite( CBSPopulationDynamic.EMIGRATION );

		// TODO draw household type/members from emigration dist, e.g. 70133NED
		final Cbs71486json.Category hhCat = this.localHouseholdDist
				.draw( RegionPeriod.of( regRef, dt ) );

		final CBSHousehold hhType = hhCat
				.hhTypeDist( this.distFact::createCategorical ).draw();
		final Actor.ID hhRef = selectEmigrantHousehold( regRef, hhType,
				hhCat.ageRange() );
		if( hhRef != null )
		{
			DomesticChange.Emigrate removed = null;
			regLoop: for( Map<HouseholdComposition, NavigableMap<Actor.ID, Instant>> byReg : this.hhReg
					.values() )
				for( NavigableMap<Actor.ID, Instant> byType : byReg.values() )
					if( byType.remove( hhRef ) != null )
					{
						removed = this.deme.initiate(
								DomesticChange.Emigrate.class, hhRef ).commit();
						break regLoop;
					}
			if( removed == null )
				LOG.warn( "Can't remove {}: not registered", hhRef.unwrap() );
		} else
			LOG.warn( "No population (yet), current: {}", this.hhReg );

		return hhType;
	}

	/**
	 * {@link Measles}
	 * 
	 * <p>
	 * Disease (Vink et al. 2014) Serial Interval (latent + infectious), days
	 * Anderson and May 1991 (6) Vynnycky and White 2010 (8) <br>
	 * Influenza A 3–6 vs 2–4 <br>
	 * Measles 12–16 vs 7–16 <br>
	 * HPV 100-150?
	 */
	@Singleton
	public static class MeaslesFactory extends Actor.Factory.LocalCaching
	{
		// globally unique?
		private transient AtomicInteger mvTypeCounter = new AtomicInteger( 0 );

		public Pathogen createMeasles()
		{
			return super.create( Actor.ID
					.of( "MV-" + this.mvTypeCounter.incrementAndGet(), id() ) )
							.subRole( Pathogen.class );
		}

		/**
		 * {@link Measles} force of infection should result in a mean generation
		 * time (time between primary infecting secondary) of ca 11-12 days,
		 * with st.dev. 2-3 days, see Klinkenberg:2011:jtbi, secondary attack
		 * rate >90% in household/institutional contacts
		 * <p>
		 * for incubation and symptom times, see e.g.
		 * https://wwwnc.cdc.gov/travel/yellowbook/2016/infectious-diseases-related-to-travel/measles-rubeola
		 */
		public MeaslesFactory( final Scheduler scheduler,
			final ProbabilityDistribution.Factory distFact,
			final Condition.Factory conditionFactory )
		{
			super();

			// Pathogen.SimpleSEIR 

			// latent period (E->I) t+0, onset around t+14 but infectious already 4 days before
			distFact.createTriangular( 1, 14 - 4, 21 - 4 )//
					.toQuantities( TimeUnits.DAYS ).map( Duration::of );

			// infectious period (I->R): t+14-4, [onset-4,onset+4] 
			distFact.createTriangular( 0, 8, 10 ).toQuantities( TimeUnits.DAYS )
					.map( Duration::of );

			// wane period (R->S): infinite, forever immune
			distFact.createDeterministic( Duration.of( 100, TimeUnits.YEAR ) );

			// incubation period (E->C, sub->clinical symptoms)
			// : t+7-21 days, rash, fever, Koplik spots, ...
			// : t+9-12 days (https://www.wikiwand.com/en/Incubation_period)
			distFact.createTriangular( 7, 11, 21 )
					.toQuantities( TimeUnits.DAYS ).map( Duration::of );

			// symptom period (C->N, normal/asymptomatic)
			distFact.createTriangular( 4, 5, 7 ).toQuantities( TimeUnits.DAYS )
					.map( Duration::of );

//				force of infection (S->E): t=?, viral shedding -> respiratory/surface contact 
//				if( // TODO if  (infection pressure [=#contacts] > threshold) &&
//				conditionOf( person ).compartment().isSusceptible() )
		}
	}

	@Singleton
	public static class MyDirectory
		implements Region.Directory, Place.Directory, Transporter.Directory
	{
		/** the regions: geographic areas, for analytics on clustering, ... */
		private NavigableMap<Region.ID, Region> regions = new TreeMap<>();

		/**
		 * the places: geographic locations, to broker by type
		 * ({@link Purpose}), proximity ({@link LatLong})
		 */
		private NavigableMap<Place.ID, Place> places = new TreeMap<>();

		/** the spaces: rooms, vehicles */
		private NavigableMap<Actor.ID, Transporter> vehicles = new TreeMap<>();

		/** RIVM National Institute for Public Health and the Environment */
		public LatLong RIVM_POSITION = LatLong.of( 52.1185272, 5.1868699,
				Units.DEGREE_ANGLE );

		@Inject
		private Actor.Factory actorFactory;

		@Override
		public Region lookup( final Region.ID id )
		{
			return this.regions.computeIfAbsent( id, key ->
			{
				final String name = null;// TODO lookup
				final Region parent = null;// TODO lookup
				final Collection<Region> children = null;// TODO lookup

				return new Region.Simple( id, name, parent, children );
			} );
		}

		@Override
		public Place lookup( final Place.ID id )
		{
			return this.places.computeIfAbsent( id, key -> Place.of( id ) );
		}

		@Override
		public Transporter lookup( final Actor.ID id )
		{
			return this.vehicles.computeIfAbsent( id, key ->
			{
				return this.actorFactory.create( id )
						.subRole( Transporter.class );
			} );
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public Number getRegionalValue( final Region.ID key )
	{
		// TODO Auto-generated method stub
		return this.hhReg.computeIfAbsent( key, k -> new HashMap<>() ).size();
	}
}