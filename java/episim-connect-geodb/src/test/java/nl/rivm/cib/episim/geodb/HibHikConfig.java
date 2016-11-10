/* $Id: 84f9132fdef5c600dacf83457e12f878f85ac5b9 $
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
package nl.rivm.cib.episim.geodb;

import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;

import io.coala.persist.HibernateJPAConfig;

/**
	 * {@link HibHikConfig}
	 * 
	 * @version $Id: 84f9132fdef5c600dacf83457e12f878f85ac5b9 $
	 * @author Rick van Krevelen
	 */
	public interface HibHikConfig extends HibernateJPAConfig
	{
		String DATASOURCE_CLASS_KEY = "hibernate.hikari.dataSourceClassName";

		String DATASOURCE_URL_KEY = "hibernate.hikari.dataSource.url";

		String DATASOURCE_USERNAME_KEY = "hibernate.hikari.dataSource.user";

		String DATASOURCE_PASSWORD_KEY = "hibernate.hikari.dataSource.password";

		@DefaultValue( "hibernate_test_pu" )
		String[] persistenceUnitNames();

		@Key( DEFAULT_SCHEMA_KEY )
		@DefaultValue( "PUBLIC" )
		String hibernateDefaultSchema();

		@Key( CONNECTION_PROVIDER_CLASS_KEY )
		@DefaultValue( "org.hibernate.hikaricp.internal.HikariCPConnectionProvider" )
		String hibernateConnectionProviderClass();

		// see https://github.com/brettwooldridge/HikariCP/wiki/Configuration#popular-datasource-class-names
		@Key( DATASOURCE_CLASS_KEY )
		@DefaultValue( "org.postgresql.ds.PGSimpleDataSource" )
		String hikariDataSourceClass();

		@DefaultValue( "" + true )
		boolean ssl();

		@Key( DATASOURCE_URL_KEY )
//		@DefaultValue( "jdbc:hsqldb:file:target/testdb" )
//		@DefaultValue( "jdbc:hsqldb:mem:mymemdb" )
		@DefaultValue( "jdbc:postgresql://geodb.rivm.nl/sde_gdbrivm" ) //"pgl04-int-p.rivm.nl";
		String url();

		/**
		 * @param imports additional {@link EntityManagerFactory} configuration
		 * @return the (expensive) {@link EntityManagerFactory}
		 */
		static HibHikConfig getOrCreate( final Map<?, ?>... imports )
		{
			return ConfigCache.getOrCreate( HibHikConfig.class, imports );
		}
	}