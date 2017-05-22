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
package nl.rivm.cib.morphine;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.json.JsonUtil;
import io.coala.time.Duration;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;

/**
 * {@link MorphineScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class MorphineScenario implements Scenario
{

	public static class ScenarioConfig
	{
		public String test;
	}

	/** */
	private static final Logger LOG = LogManager
			.getLogger( MorphineScenario.class );

	@Inject
	private Scheduler scheduler;

	@InjectConfig
	private ScenarioConfig config;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public void init()
	{
		final Date offset = new Date();
		LOG.trace( "Scenario config: {}", JsonUtil.stringify( this.config ) );
		after( Duration.of( 1, TimeUnits.DAYS ) )
				.call( t -> LOG.trace( "t={}", t.prettify( offset ) ) );
	}

	public static final void main( final String[] args ) throws IOException
	{
		final String scenName = "scen1";
		final Map<String, Object> export = new HashMap<>();//ConfigUtil.export( this );
		export.put( ReplicateConfig.ID_KEY, scenName );
		export.put( LocalConfig.ID_KEY, scenName );

		// configure replication FIXME via LocalConfig?
//		ReplicateConfig.getOrCreate( export );

//		ConfigFactory.create( LocalConfig.class, export ).createBinder();
		final LocalBinder binder = LocalConfig
				.openYAML( "hesitant.yaml", scenName, export ).createBinder();

		LOG.info( "Starting Morphine scenario, config: {}", binder );
		binder.inject( Scenario.class ).run();

	}

}
