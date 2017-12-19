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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

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
import nl.rivm.cib.csv.CbsRegionHistory;
import nl.rivm.cib.demo.DemoModel.Demical.PersonBroker;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.demo.DemoModel.Regional.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Social.PeerBroker;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.demo.module.HealthBrokerSimple;
import nl.rivm.cib.demo.module.PeerBrokerSimple;
import nl.rivm.cib.demo.module.PersonBrokerSimple;
import nl.rivm.cib.demo.module.SiteBrokerSimple;
import nl.rivm.cib.demo.module.SocietyBrokerSimple;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.CbsRegionHierarchy;
import nl.rivm.cib.episim.cbs.TimeUtil;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Compartment;

/**
 * {@link DemoTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class DemoTest
{
	/** */
	static final Logger LOG = LogUtil.getLogger( DemoTest.class );

	@SuppressWarnings( "unchecked" )
	@Test
	public void doTest() throws InterruptedException, IOException
	{
		LOG.info( "Demo test" );

		final Map<String, String> argMap = ConfigUtil.cliArgMap( "test1=a",
				"test=b" );
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
		final ZonedDateTime offset = config.offset()
				.atStartOfDay( TimeUtil.NL_TZ );
		final long durationDays = Duration
				.between( offset, offset.plus( config.duration() ) ).toDays();

		final JsonNode demeConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.DEMOGRAPHY_BASE );
//		LOG.debug( "Deme config: {}", JsonUtil.toJSON( demeConfig ) );

		final JsonNode healthConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.EPIDEMIOLOGY_BASE );
//		LOG.debug( "Health config: {}", JsonUtil.toJSON( healthConfig ) );

		final JsonNode peerConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.HESITANCY_BASE );
//		LOG.debug( "Peer config: {}", JsonUtil.toJSON( peerConfig ) );

		final JsonNode siteConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.GEOGRAPHY_BASE );
//		LOG.debug( "Site config: {}", JsonUtil.toJSON( siteConfig ) );

		final JsonNode societyConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.MOTION_BASE );
//		LOG.debug( "Society config: {}", JsonUtil.toJSON( societyConfig ) );

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
				.withProvider( PersonBroker.class, PersonBrokerSimple.class,
						demeConfig )
				// add site broker for regions/sites/transmission
				.withProvider( SiteBroker.class, SiteBrokerSimple.class,
						siteConfig )
				// add society broker for groups/gatherings
				.withProvider( SocietyBroker.class, SocietyBrokerSimple.class,
						societyConfig )
				.withProvider( PeerBroker.class, PeerBrokerSimple.class,
						peerConfig )
				.withProvider( HealthBroker.class, HealthBrokerSimple.class,
						healthConfig )

				.build();

		// FIXME workaround until seed becomes configurable from coala
		final long seed = config.randomSeed();
		final LocalBinder binder = binderConfig
				.createBinder( MapBuilder.<Class<?>, Object>unordered()
						.put( ProbabilityDistribution.Factory.class,
								new Math3ProbabilityDistribution.Factory(
										new Math3PseudoRandom.MersenneTwisterFactory()
												.create( PseudoRandom.Config.NAME_DEFAULT,
														seed ) ) )
						.build() );

		LOG.debug( "Constructing model (seed: {}, config: {})...", seed,
				JsonUtil.toJSON( binderConfig.toJSON() ) );
		final DemoScenarioMeasles1M model = binder
				.inject( DemoScenarioMeasles1M.class );

		final CbsRegionHierarchy hier;
		try( final InputStream is = FileUtil
				.toInputStream( config.configBase() + "83287NED.json" ) )
		{
			hier = JsonUtil.getJOM().readValue( is, CbsRegionHierarchy.class );
		}
		final TreeMap<String, EnumMap<CBSRegionType, String>> gmRegions = hier
				.cityRegionsByType();

		final long timestamp = System.currentTimeMillis();
		final String totalsFile = "daily-" + timestamp + "-sir-total.csv";
		final String deltasFile = "daily-" + timestamp + "-sir-delta.csv";
		final String timing = "0 0 12 ? * *";
		final List<Compartment> sirCols = Arrays.asList(
				Compartment.SUSCEPTIBLE, Compartment.INFECTIVE,
				Compartment.RECOVERED, Compartment.VACCINATED );
		final Compartment logSortReg = Compartment.INFECTIVE;
		final int n = 10;
		final CBSRegionType aggregationLevel = CBSRegionType.COROP;

		final Map<String, String> gmChanges = CbsRegionHistory.allChangesAsPer(
				LocalDate.of( 2016, 1, 1 ), CbsRegionHistory.parse(
						config.configBase(), "gm_changes_before_2018.csv" ) );
		// TODO pick neighbor within region(s)
		final String gmFallback = "GM0363";

		final ObjectNode configTree = (ObjectNode) config
				.toJSON( DemoConfig.SCENARIO_BASE );
		configTree.with( DemoConfig.REPLICATION_BASE ).put(
				DemoConfig.RANDOM_SEED_KEY,
				binder.inject( ProbabilityDistribution.Factory.class )
						.getStream().seed().longValue() );

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
										TreeMap::new,
										Collectors.mapping( Function.identity(),
												Collectors.toCollection(
														TreeSet::new ) ) ) ) );
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
										homeSIR, n, logSortReg ) ) ),
						Throwable::printStackTrace,
						() -> LOG.debug( "SIR totals written to {}",
								totalsFile ) );

		regNames.clear();
		Observable.using( () -> new FileWriter( deltasFile, false ),
				fw -> model.atEach( timing ).map( self ->
				{
					final Map<String, EnumMap<Compartment, Long>> deltas = self
							.exportRegionalSIRDelta();
					if( regNames.isEmpty() )
					{
						regNames.putAll( deltas.keySet().stream()
								.collect( Collectors.groupingBy(
										gmName -> gmRegions
												.computeIfAbsent( gmName,
														k -> gmRegions // FIXME
																.get( "GM0363" ) )
												.get( aggregationLevel ),
										TreeMap::new,
										Collectors.mapping( Function.identity(),
												Collectors.toCollection(
														TreeSet::new ) ) ) ) );
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
										homeSIR, n, logSortReg ) ) ),
						Throwable::printStackTrace,
						() -> LOG.debug( "SIR deltas written to {}",
								deltasFile ) );

		LOG.debug( "Starting..." );
		model.run();

		LOG.info( "Demo test done" );
	}
}
