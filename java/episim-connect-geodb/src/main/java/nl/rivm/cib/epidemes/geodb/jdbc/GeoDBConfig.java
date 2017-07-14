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
package nl.rivm.cib.epidemes.geodb.jdbc;

import java.net.URI;
import java.sql.Driver;

import org.aeonbits.owner.Config.Sources;

import io.coala.persist.JDBCConfig;

/**
 * {@link GeoDBConfig}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Sources( { "classpath:geodb.properties" } )
public interface GeoDBConfig extends JDBCConfig
{
	@Key( JDBC_DRIVER_KEY )
	@DefaultValue( "org.postgresql.Driver" )
	Class<? extends Driver> jdbcDriver();

	@Key( JDBC_URL_KEY )
	@DefaultValue( "jdbc:postgresql://geodb.rivm.nl/sde_gdbrivm" )
	URI jdbcUrl();

	@DefaultValue( PASSWORD_PROMPT_VALUE )
	@Key( JDBC_PASSWORD_KEY )
	@ConverterClass( PasswordPromptConverter.class )
	String jdbcPassword();

//	@DefaultValue( "" + true )
//	boolean ssl();

}