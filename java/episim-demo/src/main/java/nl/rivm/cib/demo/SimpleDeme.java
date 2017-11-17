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
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.enterprise.Transaction;
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
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.util.InputStreamConverter;
import io.reactivex.Observable;
import io.reactivex.internal.functions.Functions;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.demo.DemoModel.Demical.Deme;
import nl.rivm.cib.demo.DemoModel.Demical.DemeEventType;
import nl.rivm.cib.demo.DemoModel.Demical.DemicFact;
import nl.rivm.cib.demo.DemoModel.HouseholdPosition;
import nl.rivm.cib.demo.DemoModel.Households;
import nl.rivm.cib.demo.DemoModel.Medical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
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

/** organizes survival and reproduction (across households) */
@Singleton
public class SimpleDeme implements Deme
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( SimpleDeme.class );

	public interface DemeConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

		@Key( "population-size" )
		@DefaultValue( "" + 500_000 )
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

	@InjectConfig
	private SimpleDeme.DemeConfig config;

	@Inject
	private LocalBinder binder;

	@Inject
	private Scheduler scheduler;

	@Inject
	private SiteBroker siteBroker;

	@Inject
	private SocietyBroker societyBroker;

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
			// TODO create EO versions
			final Transaction.ID txId = Transaction.ID
					.create( this.binder.id() );
			final Class<? extends Fact> txKind = null;
			final Actor.ID initiatorRef = null;
			final Actor.ID executorRef = null;
			final Fact.Factory factFactory = null;
			final Transaction<?> tx = new Transaction.Simple<>( txId, txKind,
					initiatorRef, executorRef, this.scheduler, factFactory );

			final Fact.ID causeRef = null;
			final Fact.ID id = Fact.ID.create( tx.id() );
			final Instant occurrence = now();
			final FactKind coordKind = null;
			final Instant expiration = null;
			final Map<?, ?>[] properties = null;
			return new Fact.Simple( id, occurrence, tx, coordKind, expiration,
					causeRef, properties );
		} );
	}

	private Table<Persons.PersonTuple> persons;
	private EliminationPicker eliminationPicker;
//			private IndexPartition ppRegAgeIndex;
	private final AtomicLong indSeq = new AtomicLong();
	private Table<Households.HouseholdTuple> households;
	private ExpansionPicker expansionPicker;
	private final Map<Object, Set<Object>> householdMembers = new HashMap<>();
	private EmigrationPicker emigrationPicker;
	private final AtomicLong hhSeq = new AtomicLong();

	private ConditionalDistribution<Cbs71486json.Category, RegionPeriod> hhTypeDist;
	// TODO draw partner age difference from config or CBS dist
	private QuantityDistribution<Time> hhAgeDiffDist = () -> QuantityUtil
			.valueOf(
					Math.max( -3, Math.min( 1,
							this.distFactory.getStream().nextGaussian() * 3 ) ),
					TimeUnits.YEAR );
	private ConditionalDistribution<Comparable<?>, String> regionalCultureDist;
	private ConditionalDistribution<BigDecimal, Object> culturalAttitudeDist;
	private ConditionalDistribution<MSEIRS.Compartment, RegionPeriod> sirStatusDist;
	private ConditionalDistribution<Boolean, RegionPeriod> initialVaxDist;

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

//			private void publishSIR( final long... delta )
//			{
//				final long[] old = this.sirTransitions.getValue(), sir = Arrays
//						.copyOf( delta, old == null ? delta.length : old.length );
//				if( old != null ) for( int i = old.length; --i != -1; )
//					sir[i] += old[i];
//				this.sirTransitions.onNext( sir );
//			}

//			private final BehaviorSubject<long[]> sirTransitions = BehaviorSubject
//					.create();
//
//			public Observable<long[]> sirTransitions()
//			{
//				return this.sirTransitions;
//			}

//			private final Map<String, Comparable<?>> regionKeys = new HashMap<>();
	private Comparable<?> toRegionKey( final String regName )
	{
		return regName;
//				return this.regionKeys.computeIfAbsent( regName, k ->
//				{
////					// TODO look-up in Cbs83287Json; update population
//					final RegionTuple t = this.regions.insertValues(
//							map -> map.put( Regions.RegionName.class, regName )
////							.put( Regions.ParentRef.class, null )
////							.put( Regions.Population.class, 0L ) )
//					);
//					return (Comparable<?>) t.key();
//				} );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public SimpleDeme reset() throws Exception
	{
		this.events.onComplete();

		this.events = PublishSubject.create();
		LOG.trace( "Setting up {} with config: {}", this.config );

		// initialize tables
		this.households = this.data.getTable( Households.HouseholdTuple.class );
		this.persons = this.data.getTable( Persons.PersonTuple.class );

		// initialize context
		this.hhSeq.set( this.households.values( Households.HouseholdSeq.class )
				.mapToLong( seq -> seq ).max().orElse( 0 ) );
		this.indSeq.set( this.persons.values( Persons.PersonSeq.class )
				.mapToLong( seq -> seq ).max().orElse( 0 ) );
		this.dtRange = Range
				.upFromAndIncluding( scheduler().offset().toLocalDate() );
		this.cbsRegionLevel = this.config.cbsRegionLevel();
		this.dtScalingFactor = DecimalUtil // TODO read cbs pop size from cbs data
				.divide( this.config.populationSize(), 17_000_000 );

		final TreeMap<RegionPeriod, Collection<WeightedValue<Cbs71486json.Category>>> values = (TreeMap<RegionPeriod, Collection<WeightedValue<Cbs71486json.Category>>>) Cbs71486json
				.readAsync(
						( /* no :: on OWNER */ ) -> this.config.cbs71486Data(),
						this.dtRange )
				.filter( wv -> wv.getValue()
						.regionType() == this.cbsRegionLevel )
				.toMultimap( wv -> wv.getValue().regionPeriod(),
						Functions.identity(), TreeMap::new )
				.blockingGet();
		this.hhTypeDist = regPer ->
		{
			final RegionPeriod k =
//					regPer.periodRef()
//					.compareTo( values.firstKey().periodRef() ) < 0
//							? RegionPeriod.of( regPer.regionRef(),
//									values.firstKey().periodRef() ) : 
					regPer;
//			if( regPer != k )
//				LOG.trace( "Modified period: {} -> {}", regPer, k );
//			else
//				LOG.trace( "Drawing for reg/per {}", regPer );
			return ConditionalDistribution
					.of( this.distFactory::createCategorical, values )
					.draw( k );
		};

		// TODO from CBS
		this.regionalCultureDist = regName -> DemoModel.NA;
		// TODO from PIENTER2
		this.culturalAttitudeDist = cult -> BigDecimal.ZERO;
		final LocalDate lastOutbreak = LocalDate.of( 2006, 6, 30 );
		final Map<RegionPeriod, ProbabilityDistribution<Boolean>> vaxDegreeDist = new HashMap<>();
		this.initialVaxDist = regPer -> vaxDegreeDist.computeIfAbsent( regPer,
				k -> this.distFactory.createBernoulli( .9 ) ).draw();
		this.sirStatusDist = regPer -> regPer.periodRef()
				.isBefore( lastOutbreak )
						? MSEIRS.Compartment.RECOVERED
						: this.initialVaxDist.draw( regPer )
								? MSEIRS.Compartment.VACCINATED
								: MSEIRS.Compartment.SUSCEPTIBLE;

		// read files and subscribe to all demical events
		Observable
				.fromArray( //
						setupBirths(), //
						setupDeaths(), //
						setupImmigrations(), setupEmigrations() )
				.flatMap( ev -> ev ).subscribe( this.events );

		setupHouseholds( this.config.populationSize() );

		// setup pickers after households to prevent re-indexing
		LOG.trace( "Creating expansion/birth picker..." );
		this.expansionPicker = //this.binder.inject( ExpansionPicker.class );
				new ExpansionPicker( this.scheduler,
						this.distFactory.getStream(), this.households );

		LOG.trace( "Creating emigration picker..." );
		this.emigrationPicker = //this.binder.inject( EmigrationPicker.class );
				new EmigrationPicker( this.scheduler,
						this.distFactory.getStream(), this.households );

		LOG.trace( "Creating elimination/death picker..." );
		this.eliminationPicker = //this.binder.inject( EliminationPicker.class );
				new EliminationPicker( this.scheduler,
						this.distFactory.getStream(), this.persons );

		// TODO RELOCATION, UNION, SEPARATION, DIVISION
		// TODO local partner/divorce rate, age, gender (CBS 37890)
		// TODO age diff (60036ned) 

		// TODO attitude exchange

		// TODO outbreak, vaccination

		LOG.trace( "{} initialized", getClass().getSimpleName() );
		return this;
	}

	private void setupHouseholds( final int n )
	{
		LOG.trace( "Initializing households..." );
		final AtomicLong personCount = new AtomicLong(),
				lastCount = new AtomicLong(),
				t0 = new AtomicLong( System.currentTimeMillis() ),
				lastTime = new AtomicLong( t0.get() );

		final Map<LocalDate, Collection<WeightedValue<Cbs71486json.Category>>> values = Cbs71486json
				.readAsync(
						( /* no :: on OWNER */ ) -> this.config.cbs71486Data(),
						this.dtRange )
				.filter( wv -> wv.getValue()
						.regionType() == this.cbsRegionLevel )
				.toMultimap( wv -> wv.getValue().offset(), Functions.identity(),
						TreeMap::new )
				.blockingGet();
		final ConditionalDistribution<Cbs71486json.Category, LocalDate> hhRegDist = ConditionalDistribution
				.of( this.distFactory::createCategorical, values );

		final CountDownLatch latch = new CountDownLatch( 1 );
		// FIXME find multi-threading RNG; use rxJava Schedulers#comp()?
//		final int nodes = 1;//Runtime.getRuntime().availableProcessors() - 1;
//		new ForkJoinPool( nodes ).submit( () -> 
		new Thread( () ->
		{
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
										DecimalUtil.divide( i * 100, n ), 1 ),
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
		} ).start();
		LongStream.range( 0, n )//
//				.parallel() //
				.forEach( i ->
				{
					try
					{
						if( personCount.get() < n )
						{
							final Cbs71486json.Category hhCat = hhRegDist
									.draw( dt() );
							final Households.HouseholdTuple hh = createHousehold(
									hhCat );
							personCount.addAndGet(
									hh.get( Households.Composition.class )
											.size() );
						} else
							latch.countDown();
					} catch( final Throwable t )
					{
						this.events.onError( t );
						personCount.updateAndGet( n0 -> n + n0 );
						latch.countDown();
					}
				}
//				) 
		);
		final long i = personCount.get(),
				dt = System.currentTimeMillis() - t0.get();
		LOG.trace( "Created {} of {} persons in {}s at {}/s", i, n,
				DecimalUtil.toScale( DecimalUtil.divide( dt, 1000 ), 1 ),
				DecimalUtil.toScale(
						dt == 0 ? 0 : DecimalUtil.divide( i * 1000, dt ), 1 ) );
	}

	private Observable<DemicFact> setupBirths()
	{
		final ConditionalDistribution<Cbs37201json.Category, RegionPeriod> localBirthDist = ConditionalDistribution
				.of( this.distFactory::createCategorical, Cbs37201json
						.readAsync( ( /* no :: on OWNER */ ) -> this.config
								.cbs37201Data(), this.dtRange )
						.filter( wv -> wv.getValue()
								.regionType() == this.cbsRegionLevel )
						// <RegionPeriod, WeightedValue<Cbs37201json.Category>>
						.toMultimap( wv -> wv.getValue().regionPeriod(),
								Functions.identity(), TreeMap::new )
						.blockingGet() );

		// initialize births
		final Cbs37230json.EventProducer births = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.BIRTHS, this.distFactory,
				( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
				this.cbsRegionLevel, this.dtRange, this.dtScalingFactor );
		final AtomicReference<Cbs37201json.Category> pendingBirth = new AtomicReference<>();
		final AtomicReference<DemicFact> lastBirth = new AtomicReference<>();
		return infiniterate( () -> births.nextDelay( dt(), regRef ->
		{
			final Cbs37201json.Category birthCat = localBirthDist
					.draw( RegionPeriod.of( regRef, dt() ) ),
					oldCat = pendingBirth.getAndSet( birthCat );
			if( oldCat != null )
			{
				final Persons.PersonTuple newborn = expandHousehold(
						pendingBirth.get() );
				final Households.HouseholdTuple hh = this.households
						.get( newborn.get( Persons.HouseholdRef.class ) );
				final CBSHousehold hhType = hh
						.get( Households.Composition.class ),
						hhTypeNew = hhType.plusChild();
				hh.updateAndGet( Households.Composition.class,
						prev -> hhTypeNew );
				lastBirth.set( DemeEventType.EXPANSION.create()
						.withContext( null, hh.key(),
								hh.get( Households.HomeSiteRef.class ) )
						.withHouseholdDelta( map -> map.put( hhType, -1 )
								.put( hhTypeNew, +1 ).build() )
						.withMemberDelta( map -> map
								.put( newborn.get(
										Persons.MemberPosition.class ), +1 )
								.build() ) );
			}
			return 1;
		} ) ).map( t -> lastBirth.get() );
	}

	private Observable<DemicFact> setupDeaths()
	{
		final Cbs37230json.EventProducer deaths = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.DEATHS, this.distFactory,
				( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
				this.cbsRegionLevel, this.dtRange, this.dtScalingFactor );
		final AtomicReference<String> pendingReg = new AtomicReference<>();
		return infiniterate( () -> deaths.nextDelay( dt(), nextRegRef ->
		{
			final String regRef = pendingReg.getAndSet( nextRegRef );
			return regRef == null ? 1
					: eliminatePerson( this.hhTypeDist
							.draw( RegionPeriod.of( regRef, dt() ) ) );
		} ) ).map( t -> DemeEventType.ELIMINATION.create() );
	}

	private Observable<DemicFact> setupImmigrations()
	{
		final Cbs37230json.EventProducer immigrations = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.IMMIGRATION, this.distFactory,
				( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
				this.cbsRegionLevel, this.dtRange, this.dtScalingFactor );

		final AtomicReference<String> pendingReg = new AtomicReference<>();
		return infiniterate( () -> immigrations.nextDelay( dt(), nextRegRef ->
		{
			final String regRef = pendingReg.getAndSet( nextRegRef );
			return regRef == null ? 1
					: immigrateHousehold( this.hhTypeDist
							.draw( RegionPeriod.of( regRef, dt() ) ) );
		} ) ).map( t -> DemeEventType.IMMIGRATION.create() );
	}

	private Observable<DemicFact> setupEmigrations()
	{
		final Cbs37230json.EventProducer emigrations = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.EMIGRATION, this.distFactory,
				( /* no :: on OWNER */ ) -> this.config.cbs37230Data(),
				this.cbsRegionLevel, this.dtRange, this.dtScalingFactor );

		final AtomicReference<String> pendingReg = new AtomicReference<>();
		return infiniterate( () -> emigrations.nextDelay( dt(), nextRegRef ->
		{
			final String regRef = pendingReg.getAndSet( nextRegRef );
			return regRef == null ? 1
					: emigrateHousehold( this.hhTypeDist
							.draw( RegionPeriod.of( regRef, dt() ) ) );
		} ) ).map( t -> DemeEventType.EMIGRATION.create() );
	}

	private Households.HouseholdTuple
		createHousehold( final Cbs71486json.Category hhCat )
	{
		final Comparable<?> cultureRef = this.regionalCultureDist
				.draw( hhCat.regionRef() );
		final CBSHousehold hhType = hhCat
				.hhTypeDist( this.distFactory::createCategorical ).draw();
		final Quantity<Time> refAge = hhCat
				.ageDist( this.distFactory::createUniformContinuous ).draw();
		final BigDecimal attitude = this.culturalAttitudeDist
				.draw( cultureRef ); // TODO split into conf/comp
		final Object homeLocationKey = this.siteBroker
				.findHome( hhCat.regionRef() );
		final long hhSeq = this.hhSeq.incrementAndGet();
		final BigDecimal refBirth = now().subtract( Duration.of( refAge ) )
				.decimal();
		final BigDecimal partnerBirth = now()
				.subtract(
						Duration.of( refAge.add( this.hhAgeDiffDist.draw() ) ) )
				.decimal();
		final Households.HouseholdTuple hh = this.households.insertValues(
				map -> map.put( Households.Composition.class, hhType )
						.put( Households.KidRank.class,
								CBSBirthRank.values()[hhType.childCount()] )
						.put( Households.HouseholdSeq.class, hhSeq )
						.put( Households.ReferentBirth.class, refBirth )
						.put( Households.MomBirth.class,
								hhType.couple() ? partnerBirth
										: Households.NO_MOM )
						.put( Households.CultureRef.class, cultureRef )
						.put( Households.HomeSiteRef.class, homeLocationKey )
						.put( Households.HomeRegionRef.class,
								toRegionKey( hhCat.regionRef() ) )
						.put( Households.Confidence.class, attitude )
						.put( Households.Complacency.class, attitude ) );

		// add household's referent
		final boolean refMale = true;
		createPerson( hh, HouseholdPosition.REFERENT, refMale, refBirth,
				homeLocationKey, cultureRef, MSEIRS.Compartment.RECOVERED );

		// add household's partner
		if( hhType.couple() )
		{
			final boolean partnerMale = !refMale; // TODO from CBS dist
			createPerson( hh, HouseholdPosition.PARTNER, partnerMale,
					partnerBirth, homeLocationKey, cultureRef,
					MSEIRS.Compartment.RECOVERED );
		}

		// add household's children
		for( int r = 0, n = hhType.childCount(); r < n; r++ )
		{
			// TODO kid age diff (60036ned)
			final Quantity<Time> refAgeOver15 = refAge
					.subtract( QuantityUtil.valueOf( 15, TimeUnits.YEAR ) );
			// equidistant ages: 0yr < age_1, .., age_n < (ref - 15yr)
			final Quantity<Time> birth = refAgeOver15
					.subtract( refAgeOver15.multiply( (.5 + r) / n ) );
			final boolean childMale = this.distFactory.getStream()
					.nextBoolean();
			final LocalDate dtBirth = now().add( birth )
					.toJava8( this.dtRange.lowerValue() );
			createPerson( hh, HouseholdPosition.ofChildIndex( r ), childMale,
					nowDecimal().subtract( QuantityUtil.decimalValue( birth,
							scheduler().timeUnit() ) ),
					homeLocationKey, cultureRef, this.sirStatusDist.draw(
							RegionPeriod.of( hhCat.regionRef(), dtBirth ) ) );
		}
		// join societies/groups/clubs/...
		this.householdMembers.get( hh.key() ).forEach( ppRef ->
		{
			final Persons.PersonTuple pp = this.persons.get( ppRef );
			final Map<String, Object> roles = this.societyBroker.join( pp );
			if( roles.isEmpty() ) LOG.warn( "No societies joined by {}", pp );
		} );

		return hh;
	}

	private Persons.PersonTuple createPerson(
		final Households.HouseholdTuple hh,
		final DemoModel.HouseholdPosition rank, final boolean male,
		final BigDecimal birth, final Object locationRef,
		final Comparable<?> cultureRef, final MSEIRS.Compartment status )
	{
		final long ppSeq = this.indSeq.incrementAndGet();
		final Persons.PersonTuple pp = this.persons
				.insertValues( map -> map.put( Persons.PersonSeq.class, ppSeq )
						.put( Persons.HouseholdRef.class, hh.key() )
						.put( Persons.MemberPosition.class, rank )
						.put( Persons.EpiCompartment.class, status )
						.put( Persons.CultureRef.class, cultureRef )
						.put( Persons.Birth.class, birth )
						.put( Persons.Male.class, male )
						.put( Persons.HomeSiteRef.class,
								hh.get( Households.HomeSiteRef.class ) )
						.put( Persons.HomeRegionRef.class,
								hh.get( Households.HomeRegionRef.class ) )
						.put( Persons.SiteRef.class, locationRef ) );
		this.householdMembers.computeIfAbsent( hh.key(), k -> new HashSet<>() )
				.add( pp.key() );
		return pp;
	}

	private EnumMap<CBSMotherAgeRange, QuantityDistribution<Time>> momAgeDists = new EnumMap<>(
			CBSMotherAgeRange.class );

	private Persons.PersonTuple
		expandHousehold( final Cbs37201json.Category birthCat )
	{
		// TODO marriage, multiplets (CBS 37422) 
		// TODO kid age diff (60036ned)

		final CBSMotherAgeRange momAge = birthCat
				.ageDist( this.distFactory::createCategorical ).draw();
		final CBSGender gender = birthCat
				.genderDist( this.distFactory::createCategorical ).draw();
		final CBSBirthRank kidRank = birthCat
				.rankDist( this.distFactory::createCategorical ).draw();
		LOG.trace( "{} {}: growing {} hh with {} child ({}), mom {}", dt(),
				DemeEventType.EXPANSION, birthCat.regionRef(), kidRank, gender,
				momAge );
		final Households.HouseholdTuple hh = this.expansionPicker.pick(
				toRegionKey( birthCat.regionRef() ),
				this.momAgeDists
						.computeIfAbsent( momAge,
								k -> k.toDist(
										this.distFactory::createUniformContinuous ) )
						.draw(),
				kidRank );
		final DemoModel.HouseholdPosition rank = HouseholdPosition.ofChildIndex(
				hh.get( Households.Composition.class ).childCount() );
		final Persons.PersonTuple pp = createPerson( hh, rank, gender.isMale(),
				now().decimal(), hh.get( Households.HomeSiteRef.class ),
				hh.get( Households.CultureRef.class ),
				MSEIRS.Compartment.SUSCEPTIBLE );
		hh.updateAndGet( Households.Composition.class,
				CBSHousehold::plusChild );
		hh.updateAndGet( Households.KidRank.class, CBSBirthRank::plusOne );
		return pp;
	}

	private int eliminatePerson( final Cbs71486json.Category hhCat )
	{
		final Persons.PersonTuple pp = this.eliminationPicker.pick(
				toRegionKey( hhCat.regionRef() ),
				hhCat.ageDist( this.distFactory::createUniformContinuous )
						.draw() );
		LOG.warn( "{} {}: TODO shrinking {}, eliminate {}", dt(),
				DemeEventType.ELIMINATION, hhCat.regionRef(), pp );
		// TODO import and sample deaths per agecat/region dist

//				final CBSFamilyRank rank = CBSFamilyRank.CHILD1;
//				final Long hhRef = 0L; // TODO from local hh rank index
//				final HouseholdTuple hh = this.households
//						.get( hhRef );
//				final CBSHousehold hhType = hh
//						.get( Households.Composition.class );

		// TODO handle orphans
//				final Object indKey = removeRandomKey( indKeys );
//				final PersonTuple ind = persons.select( indKey );
//				hh.set( Households.Composition.class,
//						hhType.removeAdult() );
		return 1;
	}

	private int immigrateHousehold( final Cbs71486json.Category hhCat )
	{
		final Households.HouseholdTuple hh = createHousehold( hhCat );
		final CBSHousehold hhType = hh.get( Households.Composition.class );
		LOG.trace( "{} {}: joining {} of {} aged {}", dt(),
				DemeEventType.IMMIGRATION, hhCat.regionRef(), hhType,
				hhCat.ageRange() );
		return this.householdMembers.get( hh.key() ).size();
	}

	private int emigrateHousehold( final Cbs71486json.Category hhCat )
	{
		final CBSHousehold hhType = hhCat
				.hhTypeDist( this.distFactory::createCategorical ).draw();

		final Quantity<Time> refAge = hhCat
				.ageDist( this.distFactory::createUniformContinuous ).draw();
		LOG.trace( "{} {}: leaving {} hh {} aged {}", dt(),
				DemeEventType.EMIGRATION, hhCat.regionRef(), hhType,
				QuantityUtil.pretty( refAge, 3 ) );

		final Households.HouseholdTuple hh = this.emigrationPicker
				.pick( toRegionKey( hhCat.regionRef() ), hhType, refAge );
		this.households.delete( hh.key() );
		return this.householdMembers.remove( hh.key() ).size();
	}
}