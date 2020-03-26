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
package nl.rivm.cib.epidemes.demo.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.config.ConfigUtil;
import io.coala.config.YamlUtil;
import io.coala.data.DataLayer;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Scheduler;
import io.coala.time.SchedulerConfig;
import io.coala.util.FileUtil;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import nl.rivm.cib.epidemes.data.cbs.CBSRegionType;
import nl.rivm.cib.epidemes.data.cbs.CbsRegionHierarchy;
import nl.rivm.cib.epidemes.data.cbs.CbsRegionHistory;
import nl.rivm.cib.epidemes.data.cbs.TimeUtil;
import nl.rivm.cib.epidemes.demo.DemoConfig;
import nl.rivm.cib.epidemes.demo.DemoScenario.Demical.PersonBroker;
import nl.rivm.cib.epidemes.demo.DemoScenario.Medical.HealthBroker;
import nl.rivm.cib.epidemes.demo.DemoScenario.Regional.SiteBroker;
import nl.rivm.cib.epidemes.demo.DemoScenario.Social.PeerBroker;
import nl.rivm.cib.epidemes.demo.DemoScenario.Social.SocietyBroker;
import nl.rivm.cib.epidemes.model.MSEIRS.Compartment;

/**
 * {@link Main}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Main 
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( Main.class );

	public static void main( final String[] args )
		throws InterruptedException, IOException
	{
		LOG.info( "Starting {}", Main.class.getSimpleName() );

		final Map<String, String> argMap = ConfigUtil.cliArgMap( args );
		final String confBase = 
				argMap.computeIfAbsent( DemoConfig.CONFIG_BASE_KEY,
						k -> System.getProperty( DemoConfig.CONFIG_BASE_KEY, 
								DemoConfig.CONFIG_BASE_DIR ) );
		final String confFile = argMap.computeIfAbsent( DemoConfig.CONF_ARG,
				confArg -> System.getProperty( DemoConfig.CONF_ARG,
						confBase + DemoConfig.CONFIG_YAML_FILE ) );

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
		final JsonNode demeConfig = ((ObjectNode)config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.DEMOGRAPHY_BASE )).put(DemoConfig.CONFIG_BASE_KEY, confBase);
//		LOG.debug( "Deme config: {}", JsonUtil.toJSON( demeConfig ) );

		final Class<? extends HealthBroker> healthModule = config
				.healthModule();
		final JsonNode healthConfig = ((ObjectNode)config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.EPIDEMIOLOGY_BASE )).put(DemoConfig.CONFIG_BASE_KEY, confBase);
//		LOG.debug( "Health config: {}", JsonUtil.toJSON( healthConfig ) );

		final Class<? extends PeerBroker> peerModule = config.peerModule();
		final JsonNode peerConfig = ((ObjectNode)config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.HESITANCY_BASE )).put(DemoConfig.CONFIG_BASE_KEY, confBase);
//		LOG.debug( "Peer config: {}", JsonUtil.toJSON( peerConfig ) );

		final Class<? extends SiteBroker> siteModule = config.siteModule();
		final JsonNode siteConfig = ((ObjectNode)config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.GEOGRAPHY_BASE )).put(DemoConfig.CONFIG_BASE_KEY, confBase);
//		LOG.debug( "Site config: {}", JsonUtil.toJSON( siteConfig ) );

		final Class<? extends SocietyBroker> societyModule = config
				.societyModule();
		final JsonNode societyConfig = ((ObjectNode)config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.MOTION_BASE )).put(DemoConfig.CONFIG_BASE_KEY, confBase);
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
		final DemoScenarioSimple model = binder
				.inject( DemoScenarioSimple.class );

		final CbsRegionHierarchy hier;
		try( final InputStream is = FileUtil
				.toInputStream( confBase + "data/83287NED.json" // 2016
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
						confBase, "data/gm_changes_before_2018.csv" ) );
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

		LOG.info( "{} done", Main.class.getSimpleName() );
	}

}