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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Picker;
import io.coala.data.Table;
import io.coala.data.Picker.Groups;
import io.coala.data.Picker.Root;
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
import nl.rivm.cib.demo.DemoModel.Demical.DemicFact;
import nl.rivm.cib.demo.DemoModel.Demical.PersonBroker;
import nl.rivm.cib.demo.DemoModel.Households;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.HouseholdPosition;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
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
import tec.uom.se.ComparableQuantity;

/** organizes survival and reproduction (across households) */
@Singleton
public class SimplePersonBroker implements PersonBroker
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimplePersonBroker.class );

	public interface PersonConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

		@Key( "population-size" )
		@DefaultValue( "" + 500_000 )
		int populationSize();

		// TODO read referent pop size from cbs data?
		@Key( "population-size-ref" )
		@DefaultValue( "" + 17_000_000 )
		int referentPopulationSize();

		// TODO draw partner age difference from CBS 37422, 37890, 60036ned?
		@Key( "hh-partner-age-delta-range" )
		@DefaultValue( "[-5 year; 1 year]" )
		String hhPartnerAgeDeltaRange();

		@Key( "hh-partner-age-delta-dist" )
		@DefaultValue( "normal(-2 year; 2 year)" )
		String hhPartnerAgeDeltaDist();

		@Key( "pop-male-freq" )
		@DefaultValue( "0.5" )
		BigDecimal maleFreq();

		// for demographic congruence accuracy
		@Key( "regional-resolution" )
		@DefaultValue( "MUNICIPAL" ) //"MUNICIPAL" //"PROVINCE" //"COROP" //"TERRITORY"
		CBSRegionType regionalResolution();

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

		@Key( "hh-type-region-dist" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
				+ "71486ned-TS-2010-2016.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs71486Data();
	}

	@InjectConfig
	private SimplePersonBroker.PersonConfig config;

	@Inject
	private LocalBinder binder;

	@Inject
	private Scheduler scheduler;

	@Inject
	private DataLayer data;

	@Inject
	private ProbabilityDistribution.Factory distFactory;

	@Inject
	private ProbabilityDistribution.Parser distParser;

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

	private Table<PersonTuple> persons;
	private Table<HouseholdTuple> households;
	private final AtomicLong hhSeq = new AtomicLong();
	private final AtomicLong indSeq = new AtomicLong();

	@SuppressWarnings( "rawtypes" )
	private Groups<BigDecimal, Groups<CBSBirthRank, Groups<Comparable, Root<HouseholdTuple>>>> expansionPicker;

	@SuppressWarnings( "rawtypes" )
	private Groups<BigDecimal, Groups<CBSHousehold, Groups<Comparable, Root<HouseholdTuple>>>> emigrationPicker;

	@SuppressWarnings( "rawtypes" )
	private Groups<BigDecimal, Groups<Comparable, Root<PersonTuple>>> eliminationPicker;

	private ConditionalDistribution<Cbs71486json.Category, RegionPeriod> hhTypeDist;
	private QuantityDistribution<Time> hhPartnerAgeDiffDist;
	private final Map<Object, List<Object>> hhMembers = new HashMap<>();
	private EnumMap<CBSMotherAgeRange, QuantityDistribution<Time>> momAgeDists = new EnumMap<>(
			CBSMotherAgeRange.class );

	private final Set<Object> sizeMismatches = new HashSet<>();

	private transient CBSRegionType regionalResolution = null;
	private transient BigDecimal dtScalingFactor = BigDecimal.ONE;
	private transient Range<LocalDate> dtRange = null;

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public SimplePersonBroker reset() throws Exception
	{
		this.events.onComplete();

		this.events = PublishSubject.create();
		LOG.info( "Setting up {} with config: {}", this.config );

		// fetch tables
		this.households = this.data.getTable( HouseholdTuple.class );
		this.households.onUpdate( Households.Composition.class,
				( hhKey, prev, hhType ) ->
				{
					final List<Object> members = this.hhMembers.get( hhKey );
					if( hhType == CBSHousehold.OTHER
							|| hhType.size() == members.size()
							|| !this.sizeMismatches.add( hhKey ) )
						return;

					LOG.warn( LogUtil.messageOf(
							"t={} HH size mismatch {} for '{}'"
									+ " {}={}+{} <> {}",
							scheduler().nowDT(), members.size(), hhKey, hhType,
							hhType.adultCount(), hhType.childCount(),
							members.stream()
									.map( ppKey -> this.persons.selectValue(
											ppKey, Persons.HouseholdRank.class ) )
									.toArray() ) );
				}, scheduler()::fail );
		this.persons = this.data.getTable( PersonTuple.class );
		this.persons.onCreate( this::registerPerson, scheduler()::fail );
		this.persons.onDelete( this::unregisterPerson, scheduler()::fail );

		// initialize context
		this.hhSeq.set( this.households.values( Households.HouseholdSeq.class )
				.mapToLong( seq -> seq ).max().orElse( 0 ) );
		this.indSeq.set( this.persons.values( Persons.PersonSeq.class )
				.mapToLong( seq -> seq ).max().orElse( 0 ) );
		this.dtRange = Range
				.upFromAndIncluding( scheduler().offset().toLocalDate() );
		this.regionalResolution = this.config.regionalResolution();
		this.dtScalingFactor = DecimalUtil.divide( this.config.populationSize(),
				this.config.referentPopulationSize() );

		final TreeMap<RegionPeriod, Collection<WeightedValue<Cbs71486json.Category>>> values = (TreeMap<RegionPeriod, Collection<WeightedValue<Cbs71486json.Category>>>) Cbs71486json
				.readAsync( () -> this.config.cbs71486Data(), this.dtRange )
				.filter( wv -> wv.getValue()
						.regionType() == this.regionalResolution )
				.toMultimap( wv -> wv.getValue().regionPeriod(),
						Functions.identity(), TreeMap::new )
				.blockingGet();
		this.hhTypeDist = ConditionalDistribution
				.of( this.distFactory::createCategorical, values );

		final Range<ComparableQuantity<Time>> ageDiffRange = Range
				.parseQuantity( this.config.hhPartnerAgeDeltaRange(),
						Time.class );
		this.hhPartnerAgeDiffDist = this.distParser
				.parseQuantity( this.config.hhPartnerAgeDeltaDist(),
						Time.class )
				.transform( v -> ageDiffRange
						.crop( (ComparableQuantity<Time>) v ) );

		// read files to generate dists and subscribe to their demical events
		Observable
				.fromArray( setupBirths(), //
						setupDeaths(), //
						setupImmigrations(), //
						setupEmigrations() )
				.flatMap( ev -> ev ).subscribe( this.events );

		setupHouseholds( this.config.populationSize() );

		// setup pickers AFTER households to prevent re-indexing
		LOG.info( "Creating expansion/birth picker..." );
		final Stream<BigDecimal> momAgeCats = IntStream
				// prefix 5 years for those newly eligible during the simulation
				.range( 15 - 5, 50 )
				.mapToObj( dt -> now()
						.subtract( Duration.of( dt, TimeUnits.YEAR ) )
						.decimal() );
		this.expansionPicker = //this.binder.inject( ExpansionPicker.class );
//				new ExpansionPicker( this.scheduler,
//						this.distFactory.getStream(), this.households );
				Picker.of( this.households, this.distFactory.getStream(),
						scheduler()::fail )
						.splitBy( Households.HomeRegionRef.class )
						.thenBy( Households.KidRank.class )
						.thenBy( Households.MomBirth.class, momAgeCats );

		LOG.info( "Creating emigration picker..." );
		final Stream<BigDecimal> refAgeCats = IntStream
				.range( 15 - 5, // minus newly eligible
						100 )
				.mapToObj( dt -> now()
						.subtract( Duration.of( dt, TimeUnits.YEAR ) )
						.decimal() );
		this.emigrationPicker = //this.binder.inject( EmigrationPicker.class );
//				new EmigrationPicker( this.scheduler,
//						this.distFactory.getStream(), this.households );
				Picker.of( this.households, this.distFactory.getStream(),
						scheduler()::fail )
						.splitBy( Households.HomeRegionRef.class )
						.thenBy( Households.Composition.class )
						.thenBy( Households.ReferentBirth.class, refAgeCats );

		LOG.info( "Creating elimination/death picker..." );
		final Stream<BigDecimal> birthAgeCats = IntStream
				.range(
						// minus newly eligible
						-20 - 2, 0 )
				.mapToObj(
						n -> now().add( Duration.of( 5 * n, TimeUnits.YEAR ) )
								.decimal() );
		this.eliminationPicker = //this.binder.inject( EliminationPicker.class );
//				new EliminationPicker( this.scheduler,
//						this.distFactory.getStream(), this.persons );
				Picker.of( this.persons, this.distFactory.getStream(),
						scheduler()::fail )
						.splitBy( Persons.HomeRegionRef.class )
						.thenBy( Persons.Birth.class, birthAgeCats );

		// TODO RELOCATION, UNION, SEPARATION, DIVISION

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	private void registerPerson( final PersonTuple pp )
	{
		this.hhMembers.computeIfAbsent( pp.get( Persons.HouseholdRef.class ),
				k -> new ArrayList<>() ).add( pp.key() );
	}

	private void unregisterPerson( final PersonTuple pp )
	{
		final List<Object> otherMembers = this.hhMembers
				.compute( pp.get( Persons.HouseholdRef.class ),
						( k, members ) -> members != null
								&& members.remove( pp.key() )
								&& !members.isEmpty() ? members : null );
		final HouseholdTuple hh = this.households
				.get( pp.get( Persons.HouseholdRef.class ) );
		if( otherMembers == null )
		{
			this.households.delete( hh.key() );
			return;
		}

		final HouseholdPosition ppPos = pp.get( Persons.HouseholdRank.class );
		otherMembers.forEach( ppRef -> this.persons.select( ppRef )
				.updateAndGet( Persons.HouseholdRank.class,
						memPos -> memPos.shift( ppPos ) ) );

		CBSHousehold hhType = hh.get( Households.Composition.class );
		switch( ppPos )
		{
		case REFERENT:
		case PARTNER:
			if( hhType.adultCount() == 1 && hhType.childCount() == 0 )
				this.households.delete( hh.key() );
			else
				hh.updateAndGet( Households.Composition.class,
						hhComp -> hhComp.couple() ? hhComp.minusAdult() :
						// TODO improve orphans handling
								CBSHousehold.OTHER );
			break;

		default:
			if( hhType.childCount() == 0 )
				LOG.warn( LogUtil.messageOf(
						"Unable to unregister child {} from {}", pp, hh ),
						new IllegalStateException( "Orphaned child?" ) );
			else
			{
				hh.updateAndGet( Households.Composition.class,
						CBSHousehold::minusChild );
				hh.updateAndGet( Households.KidRank.class,
						CBSBirthRank::minusOne );
			}
			break;
		}

//		LOG.debug( "eliminated {} from {} members {}", pp, hh,
//				otherMembers.stream()
//						.map( ppKey -> this.persons.selectValue( ppKey,
//								Persons.MemberPosition.class ) )
//						.toArray() );	
	}

	private void setupHouseholds( final int n )
	{
		LOG.info( "Creating households..." );
		final AtomicLong personCount = new AtomicLong(),
				lastCount = new AtomicLong(),
				t0 = new AtomicLong( System.currentTimeMillis() ),
				lastTime = new AtomicLong( t0.get() );

		final TreeMap<LocalDate, Collection<WeightedValue<Cbs71486json.Category>>> values = (TreeMap<LocalDate, Collection<WeightedValue<Cbs71486json.Category>>>) Cbs71486json
				.readAsync( () -> this.config.cbs71486Data(), this.dtRange )
				.filter( wv -> wv.getValue()
						.regionType() == this.regionalResolution )
				.toMultimap( wv -> wv.getValue().regionPeriod().periodRef(),
						Functions.identity(), TreeMap::new,
						k -> new ArrayList<>() )
				.blockingGet();
		final LocalDate startDT = dt();
		final ConditionalDistribution<Cbs71486json.Category, LocalDate> hhRegDist = ConditionalDistribution
				.of( this.distFactory::createCategorical, values );
		final CountDownLatch latch = new CountDownLatch( 1 );
		new Thread( () ->
		{
			Thread.currentThread().setName( "hhMon" );
			while( latch.getCount() > 0 )
			{
				try
				{
					latch.await( 5, TimeUnit.SECONDS );
				} catch( final InterruptedException e )
				{
					return;
				}
				if( latch.getCount() == 0 ) return;

				final long i = personCount.get(), i0 = lastCount.getAndSet( i ),
						t = System.currentTimeMillis(),
						ti = lastTime.getAndSet( t );
				LOG.info(
						"Created {} of {} persons ({}%) in {}s, adding at {}/s...",
						i, n,
						DecimalUtil.toScale( DecimalUtil.divide( i * 100, n ),
								1 ),
						DecimalUtil.toScale(
								DecimalUtil.divide( t - t0.get(), 1000 ), 1 ),
						DecimalUtil.toScale(
								DecimalUtil.divide( (i - i0) * 1000, t - ti ),
								1 ) );
			}
		} ).start();
//		final int nodes = 1;//Runtime.getRuntime().availableProcessors() - 1;
//		new ForkJoinPool( nodes ).submit( () -> 
		LongStream.range( 0, n ).forEach( i ->
		{
			try
			{
				if( personCount.get() >= n )
				{
					latch.countDown();
					return;
				}
				final Cbs71486json.Category hhCat = hhRegDist.draw( startDT );
				final HouseholdTuple hh = createHousehold( hhCat );
				personCount.addAndGet(
						hh.get( Households.Composition.class ).size() );
			} catch( final Throwable t )
			{
				this.events.onError( t );
				personCount.updateAndGet( n0 -> n + n0 );
				latch.countDown();
			}
		} );
		final long i = personCount.get(),
				dt = System.currentTimeMillis() - t0.get();
		LOG.info( "Created {} of {} persons in {}s at {}/s", i, n,
				DecimalUtil.toScale( DecimalUtil.divide( dt, 1000 ), 1 ),
				DecimalUtil.toScale(
						dt == 0 ? 0 : DecimalUtil.divide( i * 1000, dt ), 1 ) );
	}

	private Observable<DemicFact> setupBirths()
	{
		// initialize birth family type dist
		final ConditionalDistribution<Cbs37201json.Category, RegionPeriod> localBirthDist = ConditionalDistribution
				.of( this.distFactory::createCategorical, Cbs37201json
						.readAsync( () -> this.config.cbs37201Data(),
								this.dtRange )
						.filter( wv -> wv.getValue()
								.regionType() == this.regionalResolution )
						// <RegionPeriod, WeightedValue<Cbs37201json.Category>>
						.toMultimap( wv -> wv.getValue().regionPeriod(),
								Functions.identity(), TreeMap::new )
						.blockingGet() );

		// initialize birth space-time dist
		final Cbs37230json.EventProducer births = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.BIRTHS, this.distFactory,
				() -> this.config.cbs37230Data(), this.regionalResolution,
				this.dtRange, this.dtScalingFactor );
		final AtomicReference<DemicFact> pendingEvent = new AtomicReference<>();
		return infiniterate( () -> births.nextDelay( dt(),
				regRef -> expandHousehold(
						localBirthDist.draw( RegionPeriod.of( regRef, dt() ) ),
						pendingEvent ) ) ).map( t -> pendingEvent.get() );
	}

	private Observable<DemicFact> setupDeaths()
	{
		final Cbs37230json.EventProducer deaths = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.DEATHS, this.distFactory,
				() -> this.config.cbs37230Data(), this.regionalResolution,
				this.dtRange, this.dtScalingFactor );
		final AtomicReference<String> pendingReg = new AtomicReference<>();
		return infiniterate( () -> deaths.nextDelay( dt(), nextRegRef ->
		{
			final String regRef = pendingReg.getAndSet( nextRegRef );
			return regRef == null ? 1
					: eliminatePerson( this.hhTypeDist
							.draw( RegionPeriod.of( regRef, dt() ) ) );
		} ) ).map( t -> new Elimination() );
	}

	private Observable<DemicFact> setupImmigrations()
	{
		final Cbs37230json.EventProducer immigrations = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.IMMIGRATION, this.distFactory,
				() -> this.config.cbs37230Data(), this.regionalResolution,
				this.dtRange, this.dtScalingFactor );

		final AtomicReference<String> pendingReg = new AtomicReference<>();
		return infiniterate( () -> immigrations.nextDelay( dt(), nextRegRef ->
		{
			final String regRef = pendingReg.getAndSet( nextRegRef );
			return regRef == null ? 1
					: immigrateHousehold( this.hhTypeDist
							.draw( RegionPeriod.of( regRef, dt() ) ) );
		} ) ).map( t -> new Immigration() );
	}

	private Observable<DemicFact> setupEmigrations()
	{
		final Cbs37230json.EventProducer emigrations = new Cbs37230json.EventProducer(
				CBSPopulationDynamic.EMIGRATION, this.distFactory,
				() -> this.config.cbs37230Data(), this.regionalResolution,
				this.dtRange, this.dtScalingFactor );

		final AtomicReference<String> pendingReg = new AtomicReference<>();
		return infiniterate( () -> emigrations.nextDelay( dt(), nextRegRef ->
		{
			final String regRef = pendingReg.getAndSet( nextRegRef );
			return regRef == null ? 1
					: emigrateHousehold( this.hhTypeDist
							.draw( RegionPeriod.of( regRef, dt() ) ) );
		} ) ).map( t -> new Emigration() );
	}

	private HouseholdTuple createHousehold( final Cbs71486json.Category hhCat )
	{
		final CBSHousehold hhType = hhCat
				.hhTypeDist( this.distFactory::createCategorical ).draw();
		final Quantity<Time> refAge = hhCat
				.ageDist( this.distFactory::createUniformContinuous ).draw();
		final long hhSeq = this.hhSeq.incrementAndGet();
		final Instant refBirth = now().subtract( Duration.of( refAge ) );
		final Instant partnerBirth = now().subtract( Duration
				.of( refAge.subtract( this.hhPartnerAgeDiffDist.draw() ) ) );
		final HouseholdTuple hh = this.households.insertValues( map -> map
				.put( Households.Composition.class, hhType )
				.put( Households.KidRank.class,
						CBSBirthRank.values()[hhType.childCount()] )
				.put( Households.HouseholdSeq.class, hhSeq )
				.put( Households.ReferentBirth.class, refBirth.decimal() )
				.put( Households.MomBirth.class,
						hhType.couple() ? partnerBirth.decimal()
								: Households.NO_MOM )
				.put( Households.HomeRegionRef.class, hhCat.regionRef() ) );

		// add household's referent
		final boolean refMale = true;
		createPerson( hh, HouseholdPosition.REFERENT, refMale,
				refBirth.decimal() );

		// add household's partner
		if( hhType.couple() )
		{
			final boolean partnerMale = !refMale; // TODO from CBS dist
			createPerson( hh, HouseholdPosition.PARTNER, partnerMale,
					partnerBirth.decimal() );
		}

		// add household's children
		for( int r = 0, n = hhType.childCount(); r < n; r++ )
		{
			final Quantity<Time> refAgeOver15 = refAge
					.subtract( QuantityUtil.valueOf( 15, TimeUnits.YEAR ) );
			// equidistant ages: 0yr < age_1, .., age_n < (ref - 15yr)
			// TODO kid age diff from cbs (60036ned)?
			final Instant birth = now()
					.subtract( refAgeOver15.subtract( refAgeOver15.multiply(
							(1 - this.distFactory.getStream().nextDouble() * .5
									+ r) / n ) ) );
			final boolean childMale = this.distFactory.getStream()
					.nextBoolean();
			createPerson( hh, HouseholdPosition.ofChildIndex( r ), childMale,
					birth.decimal() );
		}
		return hh;
	}

	private PersonTuple createPerson( final HouseholdTuple hh,
		final HouseholdPosition rank, final boolean male,
		final BigDecimal birth )
	{
		return this.persons.insertValues( map -> map
				.put( Persons.PersonSeq.class, this.indSeq.incrementAndGet() )
				.put( Persons.HouseholdRef.class, hh.key() )
				.put( Persons.HouseholdRank.class, rank )
				.put( Persons.Birth.class, birth )
				.put( Persons.Male.class, male ) );
	}

	private final AtomicReference<Cbs37201json.Category> pendingBirthCat = new AtomicReference<>();

	private int expandHousehold( final Cbs37201json.Category birthCat,
		final AtomicReference<DemicFact> pendingEvent )
		throws InstantiationException, IllegalAccessException
	{
		final Cbs37201json.Category oldCat = this.pendingBirthCat
				.getAndSet( birthCat );
		if( oldCat == null ) return 1; // initial execution

		// pick family
		final CBSGender gender = birthCat
				.genderDist( this.distFactory::createCategorical ).draw();
		final CBSBirthRank kidRank = birthCat
				.rankDist( this.distFactory::createCategorical ).draw();
		final Quantity<Time> momAge = this.momAgeDists.computeIfAbsent(
				birthCat.ageDist( this.distFactory::createCategorical ).draw(),
				k -> k.toDist( this.distFactory::createUniformContinuous ) )
				.draw();
		final BigDecimal momBirth = now().subtract( momAge ).decimal();
		final HouseholdTuple hh = this.expansionPicker.match( momBirth )
				.match( kidRank ).match( birthCat.regionRef() )
				.draw( ( args, k, v ) ->
				{
					LOG.trace( "Expanders {} deviate: {} in {}", args,
							k.getSimpleName(), v );
					return true;
				} );

		// sanity check
		final CBSHousehold hhType = hh.get( Households.Composition.class );
		if( hhType.adultCount() < 1 )
		{
			LOG.trace( "No parents available in {}, trying new region...",
					birthCat.regionRef() );
			return 0;
		}

		LOG.trace( "{} EXPANSION: growing {} hh with {} child ({}), mom {}",
				dt(), birthCat.regionRef(), kidRank, gender, momAge );
		final HouseholdPosition rank = HouseholdPosition.ofChildIndex(
				hh.get( Households.Composition.class ).childCount() );
		final PersonTuple newborn = createPerson( hh, rank, gender.isMale(),
				now().decimal() );
		final CBSHousehold hhTypeNew = hh.updateAndGet(
				Households.Composition.class, CBSHousehold::plusChild );
		hh.updateAndGet( Households.KidRank.class, CBSBirthRank::plusOne );

		pendingEvent.set( new Expansion()
				.withContext( null, hh.key(),
						hh.get( Households.HomeSiteRef.class ) )
				.withHouseholdDelta( map -> map.put( hhType, -1 )
						.put( hhTypeNew, +1 ).build() )
				.withMemberDelta( map -> map
						.put( newborn.get( Persons.HouseholdRank.class ), +1 )
						.build() ) );
		return 1;

	}

	private int eliminatePerson( final Cbs71486json.Category hhCat )
	{
		final Quantity<Time> age = hhCat
				.ageDist( this.distFactory::createUniformContinuous ).draw();
		final PersonTuple pp = this.eliminationPicker
				.match( now().subtract( age ).decimal() )
				.match( hhCat.regionRef() ).draw(( args, k, v ) ->
				{
					LOG.trace( "Death {} deviates: {} in {}", args,
							k.getSimpleName(), v );
					return true;
				} );

		// TODO import and sample deaths per agecat/region dist

		final Object hhRef = pp.get( Persons.HouseholdRef.class );
		final HouseholdTuple hh = this.households.select( hhRef );

		if( hh == null )
		{
			LOG.warn( "Missing household ref {} for pp {} ?", hhRef, pp );
			return 0;
		}
		CBSHousehold hhType = hh.get( Households.Composition.class );
		if( hhType == CBSHousehold.OTHER )
		{
			// TODO handle OTHER type (remove pp, and remove hh if empty)
			return 0;
		}

		if( this.sizeMismatches.contains( hh.key() ) ) return 0;

		LOG.trace( "{} ELIMINATION: shrinking {} person aged ~{}", dt(),
				hhCat.regionRef(), age );

		this.persons.delete( pp.key() );

		return 1;
	}

	private int immigrateHousehold( final Cbs71486json.Category hhCat )
	{
		final HouseholdTuple hh = createHousehold( hhCat );
		final CBSHousehold hhType = hh.get( Households.Composition.class );
		LOG.trace( "{} IMMIGRATION: joining {} of {} aged {}", dt(),
				hhCat.regionRef(), hhType, hhCat.ageRange() );
		return hhType.size();
	}

	private int emigrateHousehold( final Cbs71486json.Category hhCat )
	{
		final CBSHousehold hhType = hhCat
				.hhTypeDist( this.distFactory::createCategorical ).draw();

		final Quantity<Time> refAge = hhCat
				.ageDist( this.distFactory::createUniformContinuous ).draw();
		LOG.trace( "{} EMIGRATION: leaving {} hh {} aged {}", dt(),
				hhCat.regionRef(), hhType, QuantityUtil.pretty( refAge, 3 ) );

		final HouseholdTuple hh = this.emigrationPicker
				.match( now().subtract( refAge ).decimal() ).match( hhType )
				.match( hhCat.regionRef() ).draw( ( args, k, v ) ->
				{
					LOG.trace( "Emigrants {} deviate: {} in {}", args,
							k.getSimpleName(), v );
					return true;
				} );

		final CBSHousehold pickedType = hh.get( Households.Composition.class );
		this.households.delete( hh.key() );
		return pickedType.size();
	}
}