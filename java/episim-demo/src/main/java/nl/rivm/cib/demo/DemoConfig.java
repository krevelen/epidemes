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

import java.time.LocalDate;
import java.time.Period;

import org.aeonbits.owner.Config.Sources;

import io.coala.config.ConfigUtil;
import io.coala.config.LocalDateConverter;
import io.coala.config.PeriodConverter;
import io.coala.config.YamlConfig;
import nl.rivm.cib.pilot.PilotConfig.RandomSeedConverter;

/**
 * {@link DemoConfig} used in {@link DemoModel}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Sources( {
		"file:" + DemoConfig.CONFIG_BASE_PARAM + DemoConfig.CONFIG_YAML_FILE,
		"file:" + DemoConfig.CONFIG_BASE_DIR + DemoConfig.CONFIG_YAML_FILE,
		"classpath:" + DemoConfig.CONFIG_YAML_FILE } )
public interface DemoConfig extends YamlConfig
{

	/** configuration file name system property */
	String CONFIG_BASE_KEY = "config.base";

	/** configuration file name system property */
	String CONFIG_BASE_PARAM = "${" + CONFIG_BASE_KEY + "}";

	/** configuration and data file base directory */
	String CONFIG_BASE_DIR = "dist/";

	String CONFIG_YAML_FILE = "demo.yaml";

	String DATASOURCE_JNDI = "jdbc/pilotDB";

	/** configuration key separator */
	String KEY_SEP = ConfigUtil.CONFIG_KEY_SEP;

	/** configuration key */
	String SCENARIO_BASE = "scenario";

	/** configuration key */
	String REPLICATION_PREFIX = SCENARIO_BASE + KEY_SEP + "replication"
			+ KEY_SEP;

	/** configuration key */
	String POPULATION_BASE = "demography";

	/** configuration key */
	String LOCATION_BASE = "geography";

	/** configuration key */
	String MOTION_BASE = "mobility";
	
	/** configuration key */
	String VACCINATION_BASE = "vaccination"; 
	
	/** configuration key */
	String HESITANCY_BASE = "hesitancy"; 

	@Key( REPLICATION_PREFIX + "setup-name" )
	@DefaultValue( "pilot" )
	String setupName();

	@Key( REPLICATION_PREFIX + "random-seed" )
	@DefaultValue( "NaN" )
	@ConverterClass( RandomSeedConverter.class )
	Long randomSeed();

	@Key( REPLICATION_PREFIX + "duration-period" )
	@DefaultValue( "P1Y" )
	@ConverterClass( PeriodConverter.class )
	Period duration();

	@Key( REPLICATION_PREFIX + "offset-date" )
	@DefaultValue( "2012-01-01" )
	@ConverterClass( LocalDateConverter.class )
	LocalDate offset();
}