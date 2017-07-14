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

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.aeonbits.owner.Config.Sources;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;

import io.coala.persist.HibernateSchemaPolicy;
import io.coala.persist.HikariHibernateJPAConfig;

/**
 * {@link GeoJPAConfig} provides a default {@link EntityManagerFactory}
 * configuration for the RIVM GeoDB Postgres datasource, allowing values to
 * be replaced/extended in a {@link Properties} file named
 * {@link #CONFIG_PATH}
 */
@Sources( { "classpath:geodb.properties" } )
public interface GeoJPAConfig extends HikariHibernateJPAConfig, GeoDBConfig
{
	@DefaultValue( "geodb_test_pu" )
	String[] jpaUnitNames();

	@Key( AvailableSettings.HBM2DDL_AUTO )
	@DefaultValue( "validate" )
	HibernateSchemaPolicy hibernateSchemaPolicy();

	@Key( AvailableSettings.DEFAULT_SCHEMA )
	@DefaultValue( "nl" )
	String hibernateDefaultSchema();

	@Key( AvailableSettings.DIALECT )
	@DefaultValue( "nl.rivm.cib.episim.geodb.hibernate.PostgisDialectExtended" )
	Class<? extends Dialect> hibernateDialect();

	// see https://github.com/brettwooldridge/HikariCP/wiki/Configuration#popular-datasource-class-names

	@Key( AvailableSettings.DRIVER )
	@DefaultValue( "org.postgresql.ds.PGSimpleDataSource" )
	Class<? extends DataSource> jdbcDataSourceDriver();

}