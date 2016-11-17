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

import static org.aeonbits.owner.util.Collections.entry;
import static org.aeonbits.owner.util.Collections.map;

import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigCache;
import org.hibernate.dialect.Dialect;

import io.coala.config.ConfigUtil;
import io.coala.persist.HibernateJPAConfig;

/**
 * {@link HibHikConfig} provides a default {@link EntityManagerFactory}
 * configuration for the RIVM GeoDB Postgres datasource, allowing values to be
 * replaced/extended in a {@link Properties} file named {@link #CONFIG_PATH}
 * 
 * @version $Id: 84f9132fdef5c600dacf83457e12f878f85ac5b9 $
 * @author Rick van Krevelen
 */
@Sources( { "classpath:" + HibHikConfig.CONFIG_PATH } )
public interface HibHikConfig extends HibernateJPAConfig, GeoDBConfig
{
	String CONFIG_PATH = "geodb.properties";
	String DATASOURCE_CLASS_KEY = "hibernate.hikari.dataSourceClassName";
	String DATASOURCE_URL_KEY = "hibernate.hikari.dataSource.url";
	String DATASOURCE_USERNAME_KEY = "hibernate.hikari.dataSource.user";
	String DATASOURCE_PASSWORD_KEY = "hibernate.hikari.dataSource.password";
	String DATASOURCE_DATABASENAME_KEY = "hibernate.hikari.dataSource.databaseName";
	String DATASOURCE_SERVERNAMES_KEY = "hibernate.hikari.dataSource.serverName";

	@DefaultValue( "geodb_test_pu" )
	String[] persistenceUnitNames();

	@Key( DEFAULT_SCHEMA_KEY )
	@DefaultValue( "nl" )
	String hibernateDefaultSchema();

	@Key( CONNECTION_PROVIDER_CLASS_KEY )
	@DefaultValue( "org.hibernate.hikaricp.internal.HikariCPConnectionProvider" )
//	Class<? extends ConnectionProvider>
	String hibernateConnectionProviderClass();

	@Key( "hibernate.dialect" )
	@DefaultValue( "nl.rivm.cib.episim.geodb.PostgisDialectExtended" )
	Class<? extends Dialect> hibernateDialect();

	// see https://github.com/brettwooldridge/HikariCP/wiki/Configuration#popular-datasource-class-names
	@Key( DATASOURCE_CLASS_KEY )
	@DefaultValue( "org.postgresql.ds.PGSimpleDataSource" )
	Class<? extends DataSource> hikariDataSourceClass();

	@Key( DATASOURCE_URL_KEY )
	String hikariDataSourceUrl();

	@Key( DATASOURCE_USERNAME_KEY )
	@DefaultValue( "${" + JDBC_USERNAME_KEY + "}" )
	String hikariDataSourceUsername();

	@Key( DATASOURCE_PASSWORD_KEY )
	@DefaultValue( "${" + JDBC_PASSWORD_KEY + "}" )
	@ConverterClass(PasswordPromptConverter.class)
	String hikariDataSourcePassword();

	@Key( DATASOURCE_DATABASENAME_KEY )
	@DefaultValue( "${" + JDBC_DB_KEY + "}" )
	String hikariDataSourceDatabaseName();

	@Key( DATASOURCE_SERVERNAMES_KEY )
	@DefaultValue( "${" + JDBC_HOST_KEY + "}" )
	String hikariDataSourceServerNames();

	/**
	 * @param imports additional {@link EntityManagerFactory} configuration
	 * @return the (expensive) {@link EntityManagerFactory}
	 */
	static HibHikConfig getOrCreate( final Map<?, ?>... imports )
	{
		return ConfigCache.getOrCreate( HibHikConfig.class, imports );
	}

	@SuppressWarnings( "unchecked" )
	default Map<String, Object> export()
	{
		return ConfigUtil.export( this,
				map( new Map.Entry[]
		{ entry( DATASOURCE_PASSWORD_KEY, "<hidden>" ),
				entry( JDBC_PASSWORD_KEY, "<hidden>" ) } ) );
	}
}