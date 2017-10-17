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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;

import io.coala.bind.InjectConfig;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Actor.ID;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.LatLong;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Duration;
import io.coala.time.Infiniterator;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import io.coala.util.Compare;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import nl.rivm.cib.epidemes.cbs.json.CBSGender;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.epidemes.cbs.json.CBSPopulationDynamic;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.Cbs37201json;
import nl.rivm.cib.epidemes.cbs.json.Cbs37230json;
import nl.rivm.cib.epidemes.cbs.json.Cbs71486json;
import nl.rivm.cib.epidemes.cbs.json.CbsNeighborhood;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.cbs.TimeUtil;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.person.Attitude;
import nl.rivm.cib.episim.model.person.ConnectionType;
import nl.rivm.cib.episim.model.person.Domestic;
import nl.rivm.cib.episim.model.person.DomesticChange;
import nl.rivm.cib.episim.model.person.Gender;
import nl.rivm.cib.episim.model.person.HouseholdComposition;
import nl.rivm.cib.episim.model.person.HouseholdMember;
import nl.rivm.cib.episim.model.person.Redirection;
import nl.rivm.cib.episim.model.person.Redirection.Director;
import nl.rivm.cib.episim.model.person.Residence;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;
import tec.uom.se.ComparableQuantity;

/**
 * {@link OutbreakScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class OutbreakScenario implements Scenario
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( OutbreakScenario.class );

	@InjectConfig
	private transient OutbreakConfig config;

	@Inject
	private transient DemeStats stats;

//	@Inject
//	private transient DemePersistence directory;

	@Inject
	private transient Scheduler scheduler;

	@Inject
	private transient Actor.Factory actors;

	@Inject
	private transient ProbabilityDistribution.Factory distFact;

	/** spatial scope of analysis */
	private transient CBSRegionType cbsRegionLevel = null;

	/** the fallbackRegionRef, e.g. 'GM0363' = Amsterdam */
	private transient Region.ID fallbackRegionRef;

	private Range<ComparableQuantity<Time>> fallbackMomAge;

	private Matrix hesitancy;
	private Matrix pressure;

	/** synthetic pop size / NL pop size */
	private BigDecimal popSizeFactor = BigDecimal.ONE;
	private final AtomicLong households = new AtomicLong();
	private final AtomicLong persons = new AtomicLong();

	/** population initialization and immigration */
//	private transient ConditionalDistribution<Cbs37713json.Category, ZonedDateTime> genderOriginDist = null;

	/** national monthly demographic rates (births, deaths, migrations, ...) */
	private transient Map<CBSPopulationDynamic, ConditionalDistribution<Cbs37230json.Category, LocalDate>> demogDelayDist = new EnumMap<>(
			CBSPopulationDynamic.class );

	/** regional monthly demographic places (births, deaths, migrations, ...) */
	private transient Map<CBSPopulationDynamic, ConditionalDistribution<Cbs37230json.Category, LocalDate>> demogRegionDist = new EnumMap<>(
			CBSPopulationDynamic.class );

	// TODO partnership/marriage rate from CBS 37890 (region, age, gender) & 60036ned (age diff) ?

	// TODO separation/divorce rate from CBS 37890 (region, age, gender) ?

	private transient ConditionalDistribution<CbsNeighborhood, Region.ID> hoodDist;
	/**
	 * household types and referent ages, e.g. based on empirical CBS 71486
	 * (composition) & 60036ned (age diff) & 37201 (mom age) data
	 */
	private transient ConditionalDistribution<Cbs71486json.Category, RegionPeriod> localHouseholdDist = null;

	private transient ConditionalDistribution<Cbs37201json.Category, RegionPeriod> localBirthDist = null;

	/** temporal scope of analysis */
	private transient Range<LocalDate> timeRange = null;

	private transient Instant dtInstant = null;
	private transient LocalDate dtCache = null;

	private Actor<Fact> deme;

	private final Map<Region.ID, Object> fallbacks = new HashMap<>();

	private Region.ID fallbackFor( final Region.ID missing,
		final Object message )
	{
		this.fallbacks.computeIfAbsent( missing, key ->
		{
			LOG.trace( "t={}, falling back to {} <- {}: {}", nowPretty(),
					this.fallbackRegionRef, missing,
					message == null ? "n/a" : message );
			return message;
		} );
		return this.fallbackRegionRef;
	}

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
		// TODO from single config
		this.timeRange = Range
				.upFromAndIncluding( scheduler().offset().toLocalDate() );
		this.cbsRegionLevel = this.config.cbsRegionLevel();
		this.fallbackRegionRef = this.config.fallbackRegionRef();
		this.fallbackMomAge = this.config.momAgeRange();
		LOG.trace( "Init; range: {}", timeRange );

		this.config.cbs37230( this.timeRange,
				( metric, regionType ) -> timeSeries ->
				{
					if( regionType == this.cbsRegionLevel )
						this.demogRegionDist.put( metric,
								ConditionalDistribution.of(
										this.distFact::createCategorical,
										timeSeries ) );
					else if( regionType == CBSRegionType.COUNTRY ) // top level
						this.demogDelayDist.put( metric,
								ConditionalDistribution.of(
										this.distFact::createCategorical,
										timeSeries ) );
				} );

		this.localHouseholdDist = ConditionalDistribution.of(
				this.distFact::createCategorical,
				this.config.cbs71486( this.timeRange ) );

		this.localBirthDist = ConditionalDistribution.of(
				this.distFact::createCategorical,
				this.config.cbs37201( this.timeRange ) );

		final Map<Region.ID, ProbabilityDistribution<CbsNeighborhood>> async = this.config
				.cbsNeighborhoods().entrySet().parallelStream()
				.collect( Collectors.toMap( Entry::getKey, e -> this.distFact
						.createCategorical( e.getValue() ) ) );

		this.hoodDist = ConditionalDistribution.of( id -> async
				.computeIfAbsent( id, key -> async.get( this.fallbackFor( key,
						Pretty.of( () -> "no zipcodes" ) ) ) ) );

		final long popSize = this.config.popSize();
		this.hesitancy = SparseMatrix.Factory.zeros( popSize,
				VaxHesitancy.SocialFactors.values().length );
		this.pressure = SparseMatrix.Factory.zeros( popSize, popSize );

		// set DEME behavior for maintaining demographics
		this.deme = this.actors.create( "DEME" );
		this.deme.specialist( Residence.Deme.class, deme ->
		{
			// register new households in residence
			deme.emit( FactKind.REQUESTED ).subscribe( rq ->
			{
				householdRegistryFor( rq.getRegionRef(), rq.getComposition() )
						.put( rq.creatorRef().organizationRef(),
								rq.member( rq.referentRef() ).birth() );
//				after( Duration.ZERO )
//						.call( deme.respond( rq, FactKind.STATED )::commit );
			}, this::error );

			// trigger births
			atEach( timing( CBSPopulationDynamic.BIRTHS,
					() -> createOffspring().size() ) ).subscribe( t ->
					{
						// empty
					}, this::error );
			// trigger deaths
			atEach( timing( CBSPopulationDynamic.DEATHS, () -> 1 ),
					t -> at( t ).call(
							t1 -> deme.initiate( DomesticChange.Death.class,
									selectDiseased() ).commit() ) );
			// trigger immigrations
			atEach( timing( CBSPopulationDynamic.IMMIGRATION,
					() -> createCBSHousehold( false ) ) ).subscribe( t ->
					{
						// empty
					}, this::error );
			// trigger emigrations
			atEach( timing( CBSPopulationDynamic.EMIGRATION,
					() -> removeCBSHousehold() ) ).subscribe( t ->
					{
						// empty
					}, this::error );
			// unregister diseased persons
			deme.emit( DomesticChange.Death.class, FactKind.STATED )
					.subscribe( st ->
					{
						if( st.getMembers().isEmpty() ) unregisterHousehold(
								st.creatorRef().organizationRef() );
						this.unregisterPerson( st.diseasedRef() );
					}, this::error );
			// unregister emigrated persons
			deme.emit( DomesticChange.Emigrate.class, FactKind.STATED )
					.subscribe( st ->
					{
						st.getMembers().keySet()
								.forEach( this::unregisterPerson );
					}, this::error );

			this.stats
					.reportDeme(
							Timing.of( this.config.statisticsRecurrence() )
									.offset( dt()
											.atStartOfDay( TimeUtil.NL_TZ ) ),
							deme );
		} );

		// populate households
		for( long time = 0, size = 0, hhSize = 0, agPrev = 0; size < popSize; size += hhSize )
		{
			hhSize = createCBSHousehold( true );
			if( System.currentTimeMillis() - time > 1000 )
			{
				time = System.currentTimeMillis();
				long agNow = this.persons.get() + this.households.get();
				LOG.trace( "#pp: {} (of {}) over {} hh (= {} ag/s) in {} regs",
						this.persons.get(), popSize, this.hhIndex.size(),
						agNow - agPrev, this.hhReg.size() );
				agPrev = agNow;
			}
		}

		// TODO initiate Infections: diseases/outbreaks, vaccine/interventions

		LOG.info( "Initialized model, households: {}", this.hhReg );
	}

	private void error( final Throwable e )
	{
		LOG.error( "Problem occurred", e );
	}

//	private final Map<Region.ID, NavigableMap<ZonedDateTime, Actor.ID>> localAgeMothers = new HashMap<>();

	private static final HouseholdComposition[] COMPOSITIONS_WITH_MOTHERS = Arrays
			.stream( CBSHousehold.values() )
			.filter( hh -> hh.partnerRelationType() != null
					&& hh.partnerRelationType().isSexual() )
			.toArray( n -> new HouseholdComposition[n] );

	protected Actor.ID selectNest( final Region.ID regRef,
		final Range<ComparableQuantity<Time>> momAgeRange )
	// TODO , final int siblingCount, final int multiplets 
	{

		final Observable<Entry<ID, Instant>> localMothers = householdsFor(
				regRef, COMPOSITIONS_WITH_MOTHERS ).cache();
		final Range<Instant> birthRange = momAgeRange
				// TODO subtract average referent age difference?
				.map( age -> now().subtract( age ) );
		final List<Actor.ID> ageFilter = (localMothers.isEmpty().blockingGet()
				? householdsFor( null, COMPOSITIONS_WITH_MOTHERS )
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
//		this.householdRegistry( regRef).

		// TODO remove from actors factory cache
		return this.deme.id(); // FIXME
	}

	protected Actor.ID selectEmigrantHousehold( final Region.ID regRef,
		final HouseholdComposition hhType, final Range<Integer> hhRefAge )
	{
		final Observable<Entry<Actor.ID, Instant>> category = householdsFor(
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
		return () -> (Infiniterator<Instant>) () ->
		{
			final QuantityDistribution<Time> timeDist = this.demogDelayDist
					.get( metric ).draw( dt() )
					.timeDist( freq -> this.distFact.createExponential(
							freq.multiply( this.popSizeFactor ) ) );

			Quantity<Time> dur = timeDist.draw();

			// skip for each household member separately
			for( int i = 1, n = hhSize.getAsInt(); i < n; i++ )
				dur = dur.add( timeDist.draw() );

			return now().add( dur );
		};
	}

	protected int createCBSHousehold( final boolean initial )
	{
		final LocalDate dt = dt();
		// TODO immigration, add 37713 (gender, origin)

		Region.ID regRef = demogSite( CBSPopulationDynamic.IMMIGRATION );
		final CbsNeighborhood buurt = this.hoodDist.draw( regRef );
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
				hhType.partnerRelationType(), referentRef ) );
		for( int i = 1; i < hhType.adultCount(); i++ )
		{
			final Instant birth = referentBirth.add( 3 * i ); // TODO draw ?
			final Gender gender = CBSGender.FEMALE;
			final Actor.ID partnerRef = createPerson( gender, birth );
			members.put( partnerRef, HouseholdMember.of( birth,
					hhType.partnerRelationType(), partnerRef ) );
		}
		for( int i = 0; i < hhType.childCount(); i++ )
		{
			final Instant birth = referentBirth.add( 25 + i * 2 ); // TODO draw ?
			final Gender gender = CBSGender.FEMALE;
			final Actor.ID wardRef = createPerson( gender, birth );
			members.put( wardRef, HouseholdMember.of( birth,
					ConnectionType.Simple.WARD, wardRef ) );
		}

		final Actor.ID hhRef = createHousehold( regRef, zip, null, referentRef,
				hhType, members );

		if( !initial ) this.deme
				.initiate( DomesticChange.Immigrate.class, hhRef ).commit();

		return members.size();
	}

	private final Map<Actor.ID, Long> hhIndex = new HashMap<>();

	private final List<Long> hhIndexRecycler = new ArrayList<>();

	private long toIndex( final Actor.ID hhRef )
	{
		return this.hhIndex.computeIfAbsent( hhRef,
				key -> this.hhIndexRecycler.isEmpty()
						? (long) this.hhIndex.size()
						: this.hhIndexRecycler.remove( 0 ) );
	}

	protected Actor.ID createHousehold( final Region.ID regRef,
		final Place.ID placeRef, final LatLong coord,
		final Actor.ID referentRef, final HouseholdComposition composition,
		final Map<Actor.ID, HouseholdMember> members )
	{
		// create the agent
		final Actor<Fact> actor = this.actors.create(
				String.format( "hh%08d", this.households.incrementAndGet() ) );

		// TODO from config
		final Attitude<VaxOccasion> att = VaxHesitancy.MatrixWeightedAverager
				.of( this.hesitancy, this.pressure, this::toIndex, actor.id() );

		// set household domestic attributes
		final DomesticChange.Household hh = actor
				.subRole( DomesticChange.Household.class ).with( regRef )
				.with( placeRef ).with( coord ).with( referentRef )
				.withMembers( members ).with( composition )
				.with( VaxOccasion.class, att );
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
			hh.getMembers().put( babyRef, HouseholdMember.of( now(),
					ConnectionType.Simple.WARD, babyRef ) );
			// confirm to registry
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit( true );
		} );
		// set death handling
		hh.emit( DomesticChange.Death.class ).subscribe( rq ->
		{
			final Actor.ID diseasedRef = rq.diseasedRef();
			members.remove( diseasedRef );
			hh.initiate( Redirection.Termination.class, diseasedRef ).commit();
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit( true );
		} );
		// set family merger handling
		hh.emit( DomesticChange.MergeHome.class ).subscribe( rq ->
		{
			members.putAll( rq.arrivingRefs() );
			after( Duration.ZERO ).call( () -> hh.respond( rq, FactKind.STATED )
					.with( updateComposition( hh ) ).commit( true ) );
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
					.commit( true );
		} );
		// set new household handling
		hh.emit( DomesticChange.LeaveHome.class ).subscribe( rq ->
		{
			final Actor.ID memberRef = rq.departingRef();
			final HouseholdMember memberEntry = members.remove( memberRef );
			if( memberEntry == null )
			{
				LOG.warn( "{} can't leave another home {}", memberRef,
						hh.id().organizationRef() );
				return;
			}
			createHousehold( rq.getRegionRef(), rq.getPlaceRef(),
					rq.getPosition(), memberRef, rq.getComposition(),
					MapBuilder.<Actor.ID, HouseholdMember>unordered()
							.put( memberRef,
									memberEntry
											.with( ConnectionType.Simple.SINGLE ) )
							.build() );
			hh.respond( rq, FactKind.STATED ).with( updateComposition( hh ) )
					.commit( true );
		} );
		// set added household handling
		hh.emit( DomesticChange.Immigrate.class ).subscribe( rq ->
		{
			hh.respond( rq, FactKind.STATED ).with( hh ).commit( true );
		} );
		// set removed household handling
		hh.emit( DomesticChange.Emigrate.class ).subscribe( rq ->
		{
			hh.respond( rq, FactKind.STATED ).with( hh ).commit( true );
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

		// all handlers are set up, now we can safely initiate deme residency
		final Residence rq = hh.initiate( Residence.class, this.deme.id() )
				.with( regRef ).with( placeRef ).with( coord )
				.with( referentRef ).with( composition ).withMembers( members )
				.commit();

		return rq.creatorRef();
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
		// TODO re-use removed ID's first
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

	protected void unregisterPerson( final Actor.ID personRef )
	{
		// TODO unregister contacts, remove from actors factory cache, etc...
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
		if( hhRef == null ) hhRef = selectNest( regRef, this.fallbackMomAge );
		if( hhRef == null )
		{
			final Region.ID fallbackReg = this.fallbackFor( regRef,
					Pretty.of( () -> "no mothers" ) );
			hhRef = selectNest( fallbackReg, momAgeRange );
			if( hhRef == null )
				hhRef = selectNest( fallbackReg, this.fallbackMomAge );
		}
		if( hhRef == null )
		{
			LOG.warn( "t={}, no fall-back mothers (yet)", nowPretty() );
			return Collections.emptySet();
		}

		final Gender babyGender = cat
				.genderDist( this.distFact::createCategorical ).draw();

		// may be multiple facts later...
		return Collections.singleton(
				this.deme.initiate( DomesticChange.Birth.class, hhRef )
						.with( babyGender ).commit() );
	}

	protected int removeCBSHousehold() // emigration
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
		if( hhRef == null )
		{
			LOG.warn( "No population (yet), current: {}", this.hhReg );
			return 0;
		}
		if( unregisterHousehold( hhRef ) )
		{
			this.deme.initiate( DomesticChange.Emigrate.class, hhRef ).commit();
			return hhType.size(); // FIXME use actual
		}

		LOG.warn( "Can't remove {}: not registered", hhRef.unwrap() );
		return 0;
	}

	private transient final Map<Region.ID, Map<HouseholdComposition, NavigableMap<Actor.ID, Instant>>> hhReg = new HashMap<>();

	protected NavigableMap<Actor.ID, Instant> householdRegistryFor(
		final Region.ID regRef, final HouseholdComposition hhType )
	{
		// registry of all households of given composition in given region
		return this.hhReg.computeIfAbsent( regRef, key -> new HashMap<>() )
				.computeIfAbsent( hhType, key -> new TreeMap<>() );
	}

	protected Observable<Entry<Actor.ID, Instant>> householdsFor(
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

	protected boolean unregisterHousehold( final Actor.ID hhRef ) // emigration
	{
		final Long index = this.hhIndex.remove( hhRef );
		if( index != null ) this.hhIndexRecycler.add( index );

		for( Map<HouseholdComposition, NavigableMap<Actor.ID, Instant>> byReg : this.hhReg
				.values() )
			for( NavigableMap<Actor.ID, Instant> byType : byReg.values() )
				if( byType.remove( hhRef ) != null ) return true;
		return false;
	}

	/**
	 * @param key
	 * @return
	 */
	public Number getRegionalValue( final Region.ID key )
	{
		return this.hhReg.computeIfAbsent( key, k -> new HashMap<>() ).size();
	}
}