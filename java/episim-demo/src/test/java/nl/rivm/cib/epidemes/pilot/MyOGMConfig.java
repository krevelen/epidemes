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
package nl.rivm.cib.epidemes.pilot;

import org.hibernate.ogm.cfg.OgmProperties;
import org.hibernate.ogm.datastore.neo4j.Neo4j;
import org.hibernate.ogm.datastore.neo4j.Neo4jProperties;

import io.coala.persist.HibernateJPAConfig;

/**
 * {@link MyOGMConfig} for an object-graph model (OGM/NoSQL) implementation
 * such as MongoDB or Neo4J of our object-relation model (ORM/JPA) entities,
 * requires vendor-specific hibernate dependency
 */
public interface MyOGMConfig extends HibernateJPAConfig
{
	@DefaultValue( "nosql_test_pu" ) // match persistence.xml
	@Key( JPA_UNIT_NAMES_KEY )
	String[] jpaUnitNames();

	@DefaultValue( "pilot_testdb" )
	@Key( OgmProperties.DATABASE )
	String database();

	// :7474 HTTP REST port, 7687 bolt SSL port
	@DefaultValue( "192.168.99.100:7687" )
	@Key( OgmProperties.HOST )
	String host();

	@DefaultValue( "neo4j" )
	@Key( OgmProperties.USERNAME )
	String jdbcUsername();

	@DefaultValue( "epidemes" /* PASSWORD_PROMPT_VALUE */ )
	@Key( OgmProperties.PASSWORD )
	@ConverterClass( PasswordPromptConverter.class )
	String jdbcPassword();

	@Override
	default String jdbcPasswordKey()
	{
		return OgmProperties.PASSWORD;
	}

	@DefaultValue( Neo4j.EMBEDDED_DATASTORE_PROVIDER_NAME )
	@Key( OgmProperties.DATASTORE_PROVIDER )
	String ogmProvider();

	@DefaultValue( "target/" )
	@Key( Neo4jProperties.DATABASE_PATH )
	String hibernateOgmNeo4jDatabasePath();

}