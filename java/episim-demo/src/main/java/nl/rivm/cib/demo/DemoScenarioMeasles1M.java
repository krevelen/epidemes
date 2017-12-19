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

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.enums.ValueType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.config.ConfigUtil;
import io.coala.config.YamlUtil;
import io.coala.data.DataLayer;
import io.coala.data.DataLayer.MapFactory;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.MatrixUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.SchedulerConfig;
import io.coala.time.Timing;
import io.coala.util.FileUtil;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import nl.rivm.cib.csv.CbsRegionHistory;
import nl.rivm.cib.demo.DemoModel.Demical.DemicFact;
import nl.rivm.cib.demo.DemoModel.Demical.PersonBroker;
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.demo.DemoModel.Regional.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Social.PeerBroker;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.demo.Households.HouseholdTuple;
import nl.rivm.cib.demo.Persons.PersonTuple;
import nl.rivm.cib.demo.Regions.RegionTuple;
import nl.rivm.cib.demo.Sites.SiteTuple;
import nl.rivm.cib.demo.Societies.SocietyTuple;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.CbsRegionHierarchy;
import nl.rivm.cib.episim.cbs.TimeUtil;
//import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Compartment;

/**
 * {@link DemoScenarioMeasles1M}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class DemoScenarioMeasles1M implements DemoModel, Scenario
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( DemoScenarioMeasles1M.class );

	@InjectConfig
	private DemoConfig config;

	@Inject
	private DataLayer data;

	@Inject
	private Scheduler scheduler;

	@Inject
	private ProbabilityDistribution.Factory distFactory;

	@Inject
	private PersonBroker personBroker;

	@Inject
	private SiteBroker siteBroker;

	@Inject
	private SocietyBroker societyBroker;

	@Inject
	private PeerBroker peerBroker;

	@Inject
	private HealthBroker healthBroker;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	/** the data source */
	private Matrix persons, households;

	/** demographic event aggregates */
	private final Map<String, AtomicLong> demicEventStats = new TreeMap<>();

	/** epidemic event aggregates */
	private final Map<String, EnumMap<Compartment, AtomicLong>> sirEventStats = new TreeMap<>();

	@Override
	public void init() throws Exception
	{
		LOG.info( "Initializing {}, config: {}", getClass().getSimpleName(),
				this.config );
		LOG.info( "RNG seed: {}, scheduler offset: {}",
				this.distFactory.getStream().seed(), scheduler().offset() );

		this.persons = Matrix.Factory.sparse( ValueType.OBJECT, 1_100_000,
				Persons.PROPERTIES.size() );
		this.households = Matrix.Factory.sparse( ValueType.OBJECT, 1_000_000,
				Households.PROPERTIES.size() );

		// register data sources BEFORE initializing the brokers
		this.data
				.withSource(
						map -> map.put( PersonTuple.class, Persons.PROPERTIES ),
						this.persons )
				.withSource( map -> map.put( HouseholdTuple.class,
						Households.PROPERTIES ), this.households )
				.withSource(
						map -> map.put( RegionTuple.class, Regions.PROPERTIES ),
						(MapFactory<Long>) HashMap::new )
				.withSource(
						map -> map.put( SocietyTuple.class,
								Societies.PROPERTIES ),
						(MapFactory<Long>) HashMap::new )
				.withSource(
						map -> map.put( SiteTuple.class, Sites.PROPERTIES ),
						(MapFactory<Long>) HashMap::new );

		// reset brokers only AFTER data sources have been initialized
		this.siteBroker.reset();
		this.societyBroker.reset();
		this.peerBroker.reset();
		this.healthBroker.reset();

		// aggregate statistics
		this.healthBroker.events().observeOn( Schedulers.io() )
				.ofType( EpidemicFact.class ).subscribe( this::onEpidemicFact,
						scheduler()::fail, this::logStats );

		this.personBroker.reset().events().observeOn( Schedulers.io() )
				.ofType( Demical.DemicFact.class ).subscribe( this::onDemicFact,
						scheduler()::fail, this::logStats );
	}

	private void onEpidemicFact( final EpidemicFact ev )
	{
		ev.sirDelta.forEach( ( sir, delta ) -> //
		this.sirEventStats
				.computeIfAbsent(
						ev.pp.get( Persons.HomeRegionRef.class ).toString(),
						k -> new EnumMap<>( Compartment.class ) )
				.computeIfAbsent( sir, k -> new AtomicLong() )
				.addAndGet( Math.max( 0, delta ) ) );
	}

	private final AtomicLong logWalltime = new AtomicLong();

	private void onDemicFact( final DemicFact ev )
	{
		this.demicEventStats.computeIfAbsent( ev.getClass().getSimpleName(),
				k -> new AtomicLong() ).incrementAndGet();
		this.logWalltime.updateAndGet( tPrev ->
		{
			final long tWall = System.currentTimeMillis();
			if( tWall - tPrev < 5000 ) return tPrev;
			logStats();
			return tWall;
		} );
	}

	private void logStats()
	{
		LOG.info( "t={} sir transitions overall x {}; recent demic (x {}): {}",
				scheduler().now( DateTimeFormatter.ISO_WEEK_DATE ),
				this.sirEventStats.values().stream()
						.flatMap( e -> e.values().stream() )
						.mapToLong( AtomicLong::get ).map( Math::abs ).sum()
						/ 2,
				this.demicEventStats.values().stream()
						.mapToLong( AtomicLong::get ).sum(),
				this.demicEventStats );
		this.demicEventStats.clear();
	}

	@Override
	public Observable<DemoModel> atEach( final String timing )
	{
		return Observable.create( sub ->
		{
			if( scheduler().now() == null )
				scheduler().onReset(
						s -> s.atOnce( t -> scheduleAtEach( timing, sub ) ) );
			else
				scheduleAtEach( timing, sub );
		} );
	}

	private void scheduleAtEach( final String timing,
		final ObservableEmitter<DemoModel> sub ) throws ParseException
	{
		atOnce( t -> sub.onNext( this ) );
		scheduler().atEnd( t -> sub.onComplete() );
		try
		{
			final Iterable<Instant> it = Timing.of( timing )
					.iterate( scheduler() );
			atEach( it, t -> sub.onNext( this ) );
		} catch( final ParseException e )
		{
			sub.onError( e );
			return;
		}
	}

	@Override
	public Map<String, EnumMap<Compartment, Long>> exportRegionalSIRDelta()
	{
		return this.sirEventStats.entrySet().stream().collect( Collectors.toMap(
				Map.Entry::getKey,
				e -> e.getValue().entrySet().stream().collect( Collectors.toMap(
						Map.Entry::getKey, f -> f.getValue().get(),
						( l, r ) -> l + r,
						() -> new EnumMap<>( Compartment.class ) ) ) ) );
	}

	@Override
	public Map<String, EnumMap<Compartment, Long>> exportRegionalSIRTotal()
	{
		final Matrix epicol = this.persons.selectColumns( Ret.LINK,
				Persons.PROPERTIES
						.indexOf( Persons.PathogenCompartment.class ) );
		final Matrix homecol = this.persons.selectColumns( Ret.LINK,
				Persons.PROPERTIES.indexOf( Persons.HomeRegionRef.class ) );

		return MatrixUtil.streamAvailableCoordinates( epicol, true )
				// sequential? called from another thread
				.filter( x -> homecol.getAsObject( x ) != null )
				.collect( Collectors.groupingBy( homecol::getAsString,
						Collectors.groupingBy(
								x -> Compartment.values()[epicol.getAsInt( x )
										- 1],
								() -> new EnumMap<>( Compartment.class ),
								Collectors.counting() ) ) );
	}

//	@Override
//	public Map<String, EnumMap<Compartment, Long>> exportRegionalSIRTotal()
//	{
//		final Matrix epicol = this.persons.selectColumns( Ret.LINK,
//				Persons.PROPERTIES
//						.indexOf( Persons.PathogenCompartment.class ) );
//		final Matrix homecol = this.persons.selectColumns( Ret.LINK,
//				Persons.PROPERTIES.indexOf( Persons.HomeRegionRef.class ) );
//
//		return MatrixUtil.streamAvailableCoordinates( epicol, true )
//				// sequential? called from another thread
//				.filter( x -> homecol.getAsObject( x ) != null )
//				.collect( Collectors.groupingBy( homecol::getAsString,
//						Collectors.groupingBy(
//								x -> Compartment.values()[epicol.getAsInt( x )
//										- 1],
//								() -> new EnumMap<>( Compartment.class ),
//								Collectors.counting() ) ) );
//	}

//	public Observable<? extends DemicFact> emitDemicEvents()
//	{
//		return this.deme.events();
//	}
//
//	public Observable<FactDao> emitFactDaos( final EntityManager em )
//	{
//		return ((SimplePersonBroker) this.deme).emitFacts()
//				.map( fact -> FactDao.create( em, fact ) );
//	}
	public static void main( final String[] args )
		throws InterruptedException, IOException
	{
		LOG.info( "Starting {}", DemoScenarioMeasles1M.class.getSimpleName() );

		final Map<String, String> argMap = ConfigUtil.cliArgMap( args );
		final String confFile = argMap.computeIfAbsent( DemoConfig.CONF_ARG,
				confArg -> System.getProperty( DemoConfig.CONF_ARG,
						ConfigUtil.cliConfBase( argMap,
								DemoConfig.CONFIG_BASE_KEY,
								DemoConfig.CONFIG_BASE_DIR,
								DemoConfig.CONFIG_YAML_FILE ) ) );

		final DemoConfig config = ConfigFactory.create( DemoConfig.class,
				// CLI args added first: override config resource and defaults 
				argMap,
				YamlUtil.flattenYaml( FileUtil.toInputStream( confFile ) ) );

		if( System.getProperty(
				ConfigurationFactory.CONFIGURATION_FILE_PROPERTY ) == null )
			try( final InputStream is = FileUtil
					.toInputStream( config.configBase() + "log4j2.yaml" ) )
			{
			// see https://stackoverflow.com/a/42524443
			final LoggerContext ctx = LoggerContext.getContext( false );
			ctx.start( new YamlConfiguration( ctx, new ConfigurationSource( is ) ) );
			} catch( final IOException ignore )
			{
			}

		final Class<? extends PersonBroker> demeModule = config.demeModule();
		final JsonNode demeConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.DEMOGRAPHY_BASE );
//		LOG.debug( "Deme config: {}", JsonUtil.toJSON( demeConfig ) );

		final Class<? extends HealthBroker> healthModule = config
				.healthModule();
		final JsonNode healthConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.EPIDEMIOLOGY_BASE );
//		LOG.debug( "Health config: {}", JsonUtil.toJSON( healthConfig ) );

		final Class<? extends PeerBroker> peerModule = config.peerModule();
		final JsonNode peerConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.HESITANCY_BASE );
//		LOG.debug( "Peer config: {}", JsonUtil.toJSON( peerConfig ) );

		final Class<? extends SiteBroker> siteModule = config.siteModule();
		final JsonNode siteConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.GEOGRAPHY_BASE );
//		LOG.debug( "Site config: {}", JsonUtil.toJSON( siteConfig ) );

		final Class<? extends SocietyBroker> societyModule = config
				.societyModule();
		final JsonNode societyConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.MOTION_BASE );
//		LOG.debug( "Society config: {}", JsonUtil.toJSON( societyConfig ) );

		final ZonedDateTime offset = config.offset()
				.atStartOfDay( TimeUtil.NL_TZ );
		final long durationDays = Duration
				.between( offset, offset.plus( config.duration() ) ).toDays();
		final LocalConfig binderConfig = LocalConfig.builder().withProvider(
				Scheduler.class, Dsol3Scheduler.class,
				MapBuilder.unordered()
						.put( SchedulerConfig.ID_KEY, "" + config.setupName() )
						.put( SchedulerConfig.OFFSET_KEY, "" + offset )
						.put( SchedulerConfig.DURATION_KEY, "" + durationDays )
						.build() )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )
				// add data layer: static caching
				.withProvider( DataLayer.class, DataLayer.StaticCaching.class )
				// add deme to create households/persons
				.withProvider( PersonBroker.class, demeModule, demeConfig )
				// add site broker for regions/sites/transmission
				.withProvider( SiteBroker.class, siteModule, siteConfig )
				// add society broker for groups/gatherings
				.withProvider( SocietyBroker.class, societyModule,
						societyConfig )
				.withProvider( PeerBroker.class, peerModule, peerConfig )
				.withProvider( HealthBroker.class, healthModule, healthConfig )

				.build();

		// FIXME workaround until seed becomes configurable from coala
		final PseudoRandom rng = new Math3PseudoRandom.MersenneTwisterFactory()
				.create( PseudoRandom.Config.NAME_DEFAULT,
						config.randomSeed() );
		final LocalBinder binder = binderConfig.createBinder( MapBuilder
				.<Class<?>, Object>unordered()
				.put( ProbabilityDistribution.Factory.class,
						new Math3ProbabilityDistribution.Factory( rng ) )
				.build() );

		LOG.debug( "Constructing model, seed: {}, config: {}", rng.seed(),
				JsonUtil.toJSON( binderConfig.toJSON() ) );
		final DemoScenarioMeasles1M model = binder
				.inject( DemoScenarioMeasles1M.class );

		final CbsRegionHierarchy hier;
		try( final InputStream is = FileUtil
				.toInputStream( config.configBase() + "83287NED.json" // 2016
		) )
		{
			hier = JsonUtil.getJOM().readValue( is, CbsRegionHierarchy.class );
		}
		final TreeMap<String, EnumMap<CBSRegionType, String>> gmRegions = hier
				.cityRegionsByType();

		// TODO from config
		final long seed = rng.seed().longValue();
//		final long timestamp = System.currentTimeMillis();
		final String totalsFile = "daily-" + seed + "-sir-total.csv";
		final String deltasFile = "daily-" + seed + "-sir-delta.csv";
		final String timing = "0 0 12 ? * *";
		final int n = 10;
		final List<Compartment> sirCols = Arrays.asList(
				Compartment.SUSCEPTIBLE, Compartment.INFECTIVE,
				Compartment.RECOVERED, Compartment.VACCINATED );
		final Compartment sortLogCol = Compartment.INFECTIVE;
		final CBSRegionType aggregationLevel = CBSRegionType.HEALTH_SERVICES;

		final Map<String, String> gmChanges = CbsRegionHistory.allChangesAsPer(
				LocalDate.of( 2016, 1, 1 ), CbsRegionHistory.parse(
						config.configBase(), "gm_changes_before_2018.csv" ) );
		// TODO pick neighbor within region(s)
		final String gmFallback = "GM0363";

		final ObjectNode configTree = (ObjectNode) config
				.toJSON( DemoConfig.SCENARIO_BASE );
		configTree.with( DemoConfig.REPLICATION_BASE )
				.put( DemoConfig.RANDOM_SEED_KEY, seed );

		final TreeMap<String, Set<String>> regNames = new TreeMap<>();
		Observable.using( () -> new FileWriter( totalsFile, false ),
				fw -> model.atEach( timing ).map( self ->
				{
					final Map<String, EnumMap<Compartment, Long>> totals = self
							.exportRegionalSIRTotal();
					if( regNames.isEmpty() )
					{
						regNames.putAll( totals.keySet().stream().collect(
								Collectors.groupingBy( gmName -> gmRegions
										.computeIfAbsent( gmName, k ->
										{
											if( gmChanges.containsKey( k ) )
												return gmRegions.get(
														gmChanges.get( k ) );

											LOG.warn( "Aggregating {} as {}",
													gmName, gmFallback );
											return gmRegions.get( gmFallback );
										} ).get( aggregationLevel ),
										() -> new TreeMap<>(),
										Collectors.toCollection(
												() -> new TreeSet<>() ) ) ) );
						fw.write( DemoConfig.toHeader( configTree, sirCols,
								regNames ) );
					}
					fw.write( DemoConfig.toLine( sirCols,
							model.scheduler().nowDT().toLocalDate().toString(),
							regNames, totals ) );
					fw.flush();
					return totals;
				} ), FileWriter::close ).subscribe(
						homeSIR -> LOG.debug( "t={} TOTAL-top{}:{ {} }",
								model.scheduler().nowDT(), n,
								Pretty.of( () -> DemoConfig.toLog( sirCols,
										homeSIR, n, sortLogCol ) ) ),
						e ->
						{
							LOG.error( "Problem writing " + totalsFile, e );
							System.exit( 1 );
						}, () -> LOG.debug( "SIR totals written to {}",
								totalsFile ) );

		final AtomicBoolean first = new AtomicBoolean( true );
		Observable.using( () -> new FileWriter( deltasFile, false ),
				fw -> model.atEach( timing ).map( self ->
				{
					final Map<String, EnumMap<Compartment, Long>> deltas = self
							.exportRegionalSIRDelta();
					if( first.get() )
					{
						first.set( false );
						fw.write( DemoConfig.toHeader( configTree, sirCols,
								regNames ) );
					}
					fw.write( DemoConfig.toLine( sirCols,
							model.scheduler().nowDT().toLocalDate().toString(),
							regNames, deltas ) );
					fw.flush();
					return deltas;
				} ), FileWriter::close ).subscribe(
						homeSIR -> LOG.debug( "t={} DELTA-top{}:{ {} }",
								model.scheduler().nowDT(), n,
								Pretty.of( () -> DemoConfig.toLog( sirCols,
										homeSIR, n, sortLogCol ) ) ),
						e ->
						{
							LOG.error( "Problem writing " + deltasFile, e );
							System.exit( 1 );
						}, () -> LOG.debug( "SIR deltas written to {}",
								deltasFile ) );

		LOG.debug( "Starting..." );
		model.run();

		LOG.info( "{} done", DemoScenarioMeasles1M.class.getSimpleName() );
	}

}