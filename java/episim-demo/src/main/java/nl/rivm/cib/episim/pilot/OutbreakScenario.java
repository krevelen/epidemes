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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
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
import io.coala.util.Compare;
import io.coala.util.InputStreamConverter;
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
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.infection.Pathogen;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.locate.Transporter;
import nl.rivm.cib.episim.model.person.DomesticChange;
import nl.rivm.cib.episim.model.person.Gender;
import nl.rivm.cib.episim.model.person.HouseholdComposition;
import nl.rivm.cib.episim.model.person.MeetingTemplate.Purpose;
import nl.rivm.cib.episim.model.person.Redirection;
import nl.rivm.cib.episim.model.person.Redirection.Director;
import nl.rivm.cib.episim.model.person.RelationType;
import nl.rivm.cib.episim.model.person.Residence;
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

//		@DefaultValue( "cbs/37975_2016JJ00.json" )
//		@ConverterClass( InputStreamConverter.class )
//		InputStream cbs37975();

		@DefaultValue( "MUNICIPAL" )
		CBSRegionType cbsRegionLevel();
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
										wv -> wv, () -> new TreeMap<>() )
										.blockingGet() );
					if( g.getKey() == CBSRegionType.COUNTRY ) // top level
						national.accept( metric,
								g.toMultimap( wv -> wv.getValue().offset(),
										wv -> wv, () -> new TreeMap<>() )
										.blockingGet() );
				} );
	}

	// TODO partnership/marriage rate from CBS 37890 (region, age, gender) & 60036ned (age diff) ?

	// TODO separation/divorce rate from CBS 37890 (region, age, gender) ?

	private transient Instant dtInstant = null;
	private transient LocalDate dtCache = null;

	/** synthetic pop size / NL pop size */
	private BigDecimal popSizeFactor = BigDecimal.ONE;

	private final Map<HouseholdComposition, AtomicLong> immigrations = new HashMap<>();

	private final Map<HouseholdComposition, AtomicLong> emigrations = new HashMap<>();

	private AtomicLong households = new AtomicLong( 0 );

	private AtomicLong persons = new AtomicLong( 0 );

	private Actor<Fact> deme;

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

		// populate households
		for( long start = System
				.currentTimeMillis(), size = 0, hh = 0, popSize = this.config
						.popSize(); size < popSize; )
		{
			if( System.currentTimeMillis() - start > 1000 )
			{
				start = System.currentTimeMillis();
				LOG.trace( "size = {} of {}, hh count: {}", size, popSize, hh );
			}
			size += createCBSHousehold( true ).size();
			hh++;
		}

		// set DEME behavior for maintaining demographics
		this.deme = this.actors.create( "DEME" );
		this.deme.specialist( Residence.Deme.class, deme ->
		{
			deme.atEach( timing( CBSPopulationDynamic.BIRTHS,
					() -> createOffspring().size() ) ).subscribe();
			deme.atEach( timing( CBSPopulationDynamic.DEATHS, () -> 1 ) )
					.subscribe( t -> deme.initiate( DomesticChange.Death.class,
							selectDiseased() ).commit() );
			deme.atEach( timing( CBSPopulationDynamic.IMMIGRATION,
					() -> createCBSHousehold( false ).size() ) ).subscribe();
			deme.atEach( timing( CBSPopulationDynamic.EMIGRATION,
					() -> removeCBSHousehold().size() ) ).subscribe();

			deme.emit( DomesticChange.class, FactKind.STATED ).subscribe( st ->
			// TODO update household registry
			LOG.trace( "Confirmed: {}", st )
//			registerHousehold(st.creatorRef(), regRef, hhType, hhRefAge)
			);
		} );

		// TODO initiate Infections: diseases/outbreaks, vaccine/interventions

		LOG.info( "Initialized model" );
	}

	protected Collection<Fact> createOffspring()
	{
		// TODO draw multiplets?
		return Collections.singleton(
				this.deme.initiate( DomesticChange.Birth.class, selectNest() )
						.commit() );
	}

	protected Actor.ID selectNest()
	{
//		final Region.ID regRef = 
		demogSite( CBSPopulationDynamic.BIRTHS );

//		LOG.trace( "t={}, birth occurs in {}", nowPretty(), regRef );
		return this.deme.id(); // FIXME
	}

	protected Actor.ID selectDiseased()
	{
//		final Region.ID regRef = 
		demogSite( CBSPopulationDynamic.DEATHS );

//		LOG.trace( "t={}, death occurs in {}", nowPretty(), regRef );

		// TODO deaths, add 83190ned (age, gender, position)
		return this.deme.id(); // FIXME
	}

//	private final Map<Region.ID, NavigableMap<ZonedDateTime, Actor.ID>> localAgeMothers = new HashMap<>();

	private transient final Map<Region.ID, Map<HouseholdComposition, NavigableMap<Instant, Actor.ID>>> hhReg = new HashMap<>();

	protected NavigableMap<Instant, Actor.ID> householdRegistry(
		final Region.ID regRef, final HouseholdComposition hhType )
	{
		if( regRef == null )
			return this.hhReg.isEmpty() ? Collections.emptyNavigableMap()
					: this.hhReg.entrySet().parallelStream().flatMap(
							e -> e.getValue().entrySet().parallelStream() )
							.flatMap( e -> e.getValue().entrySet()
									.parallelStream() )
							.collect( Collectors.toMap( Entry::getKey,
									Entry::getValue, null, TreeMap::new ) );
		else if( hhType == null ) return this.hhReg.isEmpty()
				? Collections.emptyNavigableMap()
				: this.hhReg.computeIfAbsent( regRef, key -> new HashMap<>() )
						.entrySet().parallelStream()
						.flatMap(
								e -> e.getValue().entrySet().parallelStream() )
						.collect( Collectors.toMap( Entry::getKey,
								Entry::getValue, null, TreeMap::new ) );
		return this.hhReg.computeIfAbsent( regRef, key -> new HashMap<>() )
				.computeIfAbsent( hhType, key -> new TreeMap<>() );
	}

	protected void registerHousehold( final Actor.ID hhRef,
		final Region.ID regRef, final HouseholdComposition hhType,
		final Duration hhRefAge )
	{
		householdRegistry( Objects.requireNonNull( regRef, "missing region" ),
				Objects.requireNonNull( hhType, "missing composition" ) ).put(
						now().subtract( Objects.requireNonNull( hhRefAge,
								"missing referent age" ) ),
						Objects.requireNonNull( hhRef, "missing household" ) );
		LOG.trace( "Registered {} {} {} {}", hhRef, regRef, hhType, hhRefAge );
	}

	protected Actor.ID selectEmigrantHousehold( final Region.ID regRef,
		final HouseholdComposition hhType, final Range<Integer> hhRefAge )
	{
		final NavigableMap<Instant, Actor.ID> category = householdRegistry(
				regRef, hhType );
		if( category.isEmpty() )
		{
			if( regRef == null ) return null;
			if( hhType == null )
				return selectEmigrantHousehold( null, null, hhRefAge );
			return selectEmigrantHousehold( regRef, null, hhRefAge );
		}
		final Actor.ID result;
		if( hhRefAge == null )
			result = this.distFact.getStream().nextElement( category.values() );
		else
		{
			final Range<Instant> ageRange = hhRefAge.map( age -> now()
					.subtract( Duration.of( age, TimeUnits.ANNUM ) ) );
			final NavigableMap<Instant, Actor.ID> ageSelection = ageRange
					.apply( category, false );
			if( ageSelection.isEmpty() )
			{
				// get nearest to age range
				final Entry<Instant, ID> younger = category
						.floorEntry( ageRange.lowerValue() );
				final Entry<Instant, ID> older = category
						.ceilingEntry( ageRange.upperValue() );
				result = older == null || Compare.lt(
						// distance below range
						ageRange.lowerValue().subtract( younger.getKey() ),
						// distance above range
						older.getKey().subtract( ageRange.upperValue() ) )
								? younger.getValue() : older.getValue();
			} else
				result = this.distFact.getStream()
						.nextElement( ageSelection.values() );
		}
		final long nr = this.emigrations
				.computeIfAbsent( hhType, key -> new AtomicLong() )
				.incrementAndGet();
		LOG.trace( "t={}, emigration from {} of {} #{} ({}+{})", nowPretty(),
				regRef, hhType, nr, hhType.adultCount(), hhType.childCount() );
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
//	v

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

	protected CBSHousehold createCBSHousehold( final boolean initial )
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
		if( initial )
		{
//			LOG.trace( "t={}, introduction into {} ({}) of {} ({}+{})",
//					nowPretty(), regRef, zip, hhType, hhType.adultCount(),
//					hhType.childCount() );
		} else
		{
			final long nr = this.immigrations
					.computeIfAbsent( hhType, key -> new AtomicLong() )
					.incrementAndGet();
			LOG.trace( "t={}, immigration into {} ({}) of {} #{} ({}+{})",
					nowPretty(), regRef, zip, hhType, nr, hhType.adultCount(),
					hhType.childCount() );
		}

		final Instant referentBirth = now().subtract( hhRefAge );
		final Gender referentGender = CBSGender.MALE; // TODO draw
		final Actor.ID referentRef = createPerson( referentGender,
				referentBirth );
		final Map<Actor.ID, RelationType> composition = new HashMap<>();
		composition.put( referentRef, hhType.adultRelationType() );
		for( int i = 1; i < hhType.adultCount(); i++ )
			composition.put(
					createPerson( CBSGender.FEMALE, referentBirth.add( 3 ) ),
					hhType.adultRelationType() );
		for( int i = 0; i < hhType.childCount(); i++ )
			composition.put(
					createPerson( CBSGender.FEMALE, referentBirth.add( 3 ) ),
					RelationType.Simple.WARD );
		createHousehold( regRef, zip, null, referentRef, composition );
		return hhType;
	}

	protected Actor.ID createHousehold( final Region.ID regRef,
		final Place.ID placeRef, final LatLong coord,
		final Actor.ID referentRef,
		final Map<Actor.ID, RelationType> composition )
	{
		// create the agent
		final Actor<Fact> actor = this.actors.create(
				String.format( "hh%08d", this.households.incrementAndGet() ) );
		// set household domestic attributes
		final DomesticChange.Household hh = actor
				.subRole( DomesticChange.Household.class ).with( regRef )
				.with( placeRef ).with( coord ).with( referentRef )
				.with( composition == null
						? new HashMap<Actor.ID, RelationType>() : composition );
		// set birth handling
		hh.emit( DomesticChange.Birth.class ).subscribe( rq ->
		{
			final Cbs37201json.Category cat = this.localBirthDist
					.draw( RegionPeriod.of( rq.getRegionRef(), dt() ) );
			final Gender babyGender = cat
					.genderDist( this.distFact::createCategorical ).draw();
			final Actor.ID babyRef = createPerson( babyGender, now() );
			// notify family members FIXME extended kin?
//			hh.getNetwork()
//					.forEach( ( ref, rel ) -> hh
//							.initiate( Redirection.Relation.class, ref )
//							.with( rel ).with( babyRef ).commit() );
			// adopt into family
			hh.getNetwork().put( babyRef, RelationType.Simple.WARD );
			// confirm to registry
			hh.respond( rq, FactKind.STATED ).with( hh.getNetwork() ).commit();
		} );
		// set death handling
		hh.emit( DomesticChange.Death.class ).subscribe( rq ->
		{
			final Actor.ID diseasedRef = rq.diseasedRef();
			hh.getNetwork().remove( diseasedRef );
			hh.initiate( Redirection.Termination.class, diseasedRef ).commit();
			hh.respond( rq, FactKind.STATED ).with( hh.getNetwork() ).commit();
		} );
		// set family merger handling
		hh.emit( DomesticChange.MergeHome.class ).subscribe( rq ->
		{
			hh.getNetwork().putAll( rq.arrivingRefs() );
			hh.respond( rq, FactKind.STATED ).with( hh.getNetwork() ).commit();
		} );
		// set family split handling
		hh.emit( DomesticChange.SplitHome.class ).subscribe( rq ->
		{
			final Map<ID, RelationType> members = hh.getNetwork();
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
			hh.respond( rq, FactKind.STATED ).with( members ).commit();
		} );
		// set new household handling
		hh.emit( DomesticChange.LeaveHome.class ).subscribe( rq ->
		{
			if( hh.getNetwork().remove( rq.departingRef() ) == null )
				LOG.warn( "{} can't leave another home {}", rq.departingRef(),
						hh.id().organizationRef() );
			else
			{
				createHousehold( rq.getRegionRef(), rq.getPlaceRef(),
						rq.getPosition(), rq.departingRef(), null );
				hh.respond( rq, FactKind.STATED ).with( hh.getNetwork() )
						.commit();
			}
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
		return hh.id();
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
			this.deme.initiate( DomesticChange.Emigrate.class, hhRef );
		else
			LOG.warn( "No households registered (yet)?" );

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
}