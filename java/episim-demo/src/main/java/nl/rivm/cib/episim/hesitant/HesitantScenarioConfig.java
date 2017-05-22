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
package nl.rivm.cib.episim.hesitant;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.config.ConfigUtil;
import io.coala.config.YamlUtil;
import io.coala.exception.Thrower;
import io.coala.log.LogUtil;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scenario;
import io.coala.util.FileUtil;

/**
 * {@link HesitantScenarioConfig}
 */
public interface HesitantScenarioConfig extends ReplicateConfig//, LocalConfig
{

	String HESITANT_YAML_FILE = "hesitant.yaml";

	String SCENARIO_NAME = "scenario";

	String SCENARIO_TYPE_KEY = SCENARIO_NAME + KEY_SEP + "init";

	String SCENARIO_TYPE_DEFAULT = "nl.rivm.cib.episim.hesitant.HesitantScenario";

	@Key( SCENARIO_TYPE_KEY )
	@DefaultValue( SCENARIO_TYPE_DEFAULT )
	Class<? extends Scenario> scenarioType();

	@Key( DURATION_KEY )
	@DefaultValue( "" + 100 )
	@Override // add new default value
	BigDecimal rawDuration();

	default LocalBinder createBinder()
	{
		final Map<String, Object> export = ConfigUtil.export( this );
		export.put( ReplicateConfig.ID_KEY, SCENARIO_NAME );
		export.put( LocalConfig.ID_KEY, SCENARIO_NAME );

		// configure replication FIXME via LocalConfig?
		ReplicateConfig.getOrCreate( export );

		return ConfigFactory.create( LocalConfig.class, export ).createBinder();
	}

	default Scenario createScenario()
	{
		return createBinder().inject( scenarioType() );
	}

	default HealthConfig healthOrg()
	{
		return subConfig( HealthConfig.BASE_KEY, HealthConfig.class );
	}

	default PersonConfig personOrg()
	{
		return subConfig( PersonConfig.BASE_KEY, PersonConfig.class );
	}

	/**
	 * @param imports optional extra configuration settings
	 * @return the imported values
	 * @throws IOException if reading from {@link #HESITANT_YAML_FILE} fails
	 */
	static HesitantScenarioConfig getOrFromYaml( final Map<?, ?>... imports )
	{
		try
		{
			return ConfigCache.getOrCreate( SCENARIO_NAME,
					HesitantScenarioConfig.class,
					ConfigUtil.join(
							YamlUtil.flattenYaml( FileUtil
									.toInputStream( HESITANT_YAML_FILE ) ),
							imports ) );
		} catch( final IOException e )
		{
			return Thrower.rethrowUnchecked( e );
		}
	}

	/**
	 * @param args the command line arguments
	 * @throws IOException if reading from {@link #HESITANT_YAML_FILE} fails
	 */
	static void main( final String[] args ) throws IOException
	{
		final Map<?, ?> argMap = Arrays.asList( args ).stream()
				.filter( arg -> arg.contains( "=" ) )
				.map( arg -> arg.split( "=" ) ).collect( Collectors
						.toMap( parts -> parts[0], parts -> parts[1] ) );
		final HesitantScenarioConfig config = getOrFromYaml( argMap );
		LogUtil.getLogger( HesitantScenarioConfig.class ).info(
				"HESITANT scenario starting, config: {}", config.toYAML() );
		config.createScenario().run();
		LogUtil.getLogger( HesitantScenarioConfig.class )
				.info( "HESITANT completed" );
	}
}