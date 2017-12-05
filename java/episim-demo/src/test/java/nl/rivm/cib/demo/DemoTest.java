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
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.config.ConfigUtil;
import io.coala.config.YamlUtil;
import io.coala.data.DataLayer;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.log.LogUtil;
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
import nl.rivm.cib.demo.DemoModel.Cultural.PeerBroker;
import nl.rivm.cib.demo.DemoModel.Cultural.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Demical.Deme;
import nl.rivm.cib.demo.DemoModel.Epidemical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.episim.cbs.TimeUtil;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;

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

	private static final String CONF_ARG = "conf";

	@Test
	public void doTest() throws InterruptedException, IOException
	{
		LOG.info( "Demo test" );
//		+ DemoConfig.CONFIG_YAML_FILE
		final Map<String, String> argMap = ConfigUtil.cliArgMap( "test1=a",
				"test=b" );
		final String confFile = argMap.computeIfAbsent( CONF_ARG,
				confArg -> System.getProperty( CONF_ARG,
						ConfigUtil.cliConfBase( argMap,
								DemoConfig.CONFIG_BASE_KEY,
								DemoConfig.CONFIG_BASE_DIR, "sim.yaml" ) ) );

		final DemoConfig config = ConfigFactory.create( DemoConfig.class,
				// CLI args added first: override config resource and defaults 
				argMap,
				YamlUtil.flattenYaml( FileUtil.toInputStream( confFile ) ) );
		final ZonedDateTime offset = config.offset()
				.atStartOfDay( TimeUtil.NL_TZ );
		final long durationDays = Duration
				.between( offset, offset.plus( config.duration() ) ).toDays();

		final JsonNode demeConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.POPULATION_BASE );
		LOG.debug( "Deme config: {}", demeConfig );

		final JsonNode healthConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.VACCINATION_BASE );
		LOG.debug( "Health config: {}", demeConfig );

		final JsonNode peerConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.HESITANCY_BASE );
		LOG.debug( "Peer config: {}", demeConfig );

		final JsonNode siteConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.LOCATION_BASE );
		LOG.debug( "Site config: {}", siteConfig );

		final JsonNode societyConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.MOTION_BASE );
		LOG.debug( "Society config: {}", societyConfig );

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
				.withProvider( Deme.class, SimplePersonBroker.class,
						demeConfig )
				// add site broker for regions/sites/transmission
				.withProvider( SiteBroker.class, SimpleSiteBroker.class,
						siteConfig )
				// add society broker for groups/gatherings
				.withProvider( SocietyBroker.class, SimpleSocietyBroker.class,
						societyConfig )
				.withProvider( PeerBroker.class, SimplePeerBroker.class,
						peerConfig )
				.withProvider( HealthBroker.class, SimpleHealthBroker.class,
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
				binderConfig.toJSON() );
		final SimpleDemoScenario model = binder
				.inject( SimpleDemoScenario.class );

		final TreeSet<String> regNames = new TreeSet<>();
		final List<MSEIRS.Compartment> cols = Arrays.asList(
				MSEIRS.Compartment.SUSCEPTIBLE, MSEIRS.Compartment.INFECTIVE,
				MSEIRS.Compartment.RECOVERED, MSEIRS.Compartment.VACCINATED );
		final String sep = ";", eol = "\r\n",
				sirFile = "daily-" + System.currentTimeMillis() + ".csv";
		Observable.using( () -> new FileWriter( sirFile, false ),
				fw -> model.emitEpidemicStats( "0 0 12 ? * *" ).map( homeSIR ->
				{
					if( regNames.isEmpty() )
					{
						regNames.addAll( homeSIR.keySet() );
						fw.write( "ActualTime" + sep + "VirtualTime" + sep
								+ String.join( sep + " ",
										regNames.stream()
												.flatMap( reg -> cols.stream()
														.map( c -> reg + '_'
																+ c.name()
																		.charAt( 0 ) ) )
												.toArray( String[]::new ) )
								+ eol );
						fw.flush();
					}
					fw.write( DateTimeFormatter.ISO_LOCAL_DATE_TIME
							.format( ZonedDateTime.now() )
							+ sep
							+ model.scheduler()
									.now( DateTimeFormatter.ISO_LOCAL_DATE_TIME )
							+ sep
							+ String.join( sep + " ", regNames.stream().flatMap(
									reg -> cols.stream().map( c -> homeSIR
											.computeIfAbsent( reg,
													k -> new HashMap<>() )
											.computeIfAbsent( c,
													k -> Long.valueOf( 0 ) )
											.toString() ) )
									.toArray( String[]::new ) )
							+ eol );
					fw.flush();
					return homeSIR;
				} ), fw ->
				{
					fw.close();
					LOG.debug( "SIR stats written to {}", sirFile );
				} )
				.subscribe( homeSIR -> LOG.debug( "t={} SIR:{ {} }",
						model.scheduler()
								.now( DateTimeFormatter.ISO_LOCAL_DATE_TIME ),
						String.join( ", ", regNames.stream()
								.map( reg -> reg + ":[" + String.join( ",",
										cols.stream().map( c -> homeSIR
												.computeIfAbsent( reg,
														k -> new HashMap<>() )
												.computeIfAbsent( c,
														k -> Long.valueOf( 0 ) )
												.toString() )
												.toArray( String[]::new ) )
										+ "]" )
								.toArray( String[]::new ) ) ),
						Throwable::printStackTrace );

		LOG.debug( "Starting..." );
		model.run();

		LOG.info( "Demo test done" );
	}

}
