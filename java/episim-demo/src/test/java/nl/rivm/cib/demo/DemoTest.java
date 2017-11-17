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

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

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
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.SchedulerConfig;
import io.coala.util.FileUtil;
import io.coala.util.MapBuilder;
import nl.rivm.cib.demo.DemoModel.Demical.Deme;
import nl.rivm.cib.demo.DemoModel.Medical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.episim.cbs.TimeUtil;

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
		final String fileName = argMap.computeIfAbsent( CONF_ARG,
				confArg -> System.getProperty( CONF_ARG,
						ConfigUtil.cliConfBase( argMap,
								DemoConfig.CONFIG_BASE_KEY,
								DemoConfig.CONFIG_BASE_DIR,
								"sim.yaml" ) ) );

		final DemoConfig config = ConfigFactory.create( DemoConfig.class,
				// CLI args added first: override config resource and defaults 
				argMap,
				YamlUtil.flattenYaml( FileUtil.toInputStream( fileName ) ) );
		final ZonedDateTime offset = config.offset()
				.atStartOfDay( TimeUtil.NL_TZ );
		final long durationDays = Duration
				.between( offset, offset.plus( config.duration() ) ).toDays();

		final JsonNode demeConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.POPULATION_BASE );
		LOG.trace( "Deme config: {}", demeConfig );
		final JsonNode siteConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.LOCATION_BASE );
		LOG.trace( "Site config: {}", siteConfig );
		final JsonNode societyConfig = config.toJSON( DemoConfig.SCENARIO_BASE,
				DemoConfig.MOTION_BASE );
		LOG.trace( "Society config: {}", societyConfig );
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
				.withProvider( Deme.class, SimpleDeme.class, demeConfig )
				// add site broker for regions/sites/transmission
				.withProvider( SiteBroker.class, SimpleSiteBroker.class,
						siteConfig )
				// add society broker for groups/gatherings
				.withProvider( SocietyBroker.class, SimpleSocietyBroker.class,
						societyConfig )
				// TODO impl social/mental influences

				.build();

		// FIXME workaround until seed becomes configurable in coala
		final long seed = config.randomSeed();
		final LocalBinder binder = binderConfig
				.createBinder( MapBuilder.<Class<?>, Object>unordered()
						.put( ProbabilityDistribution.Factory.class,
								new Math3ProbabilityDistribution.Factory(
										new Math3PseudoRandom.MersenneTwisterFactory()
												.create( PseudoRandom.Config.NAME_DEFAULT,
														seed ) ) )
						.build() );

		LOG.trace( "Constructing model (seed: {}, config: {})...", seed,
				binderConfig.toJSON() );
		final Scenario model = binder.inject( SimpleScenario.class );

		LOG.trace( "Starting..." );
		model.run();

		LOG.info( "Demo test done" );
	}
}
