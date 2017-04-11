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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.cfg.AvailableSettings;
import org.hsqldb.jdbc.JDBCDataSource;

import io.coala.persist.HibernateJPAConfig;

/**
 * {@link MyORMConfig} JNDI settings inspired by
 * https://blogs.oracle.com/randystuph/entry/injecting_jndi_datasources_for_junit
 */
public interface MyORMConfig extends HibernateJPAConfig
{

	/** Tomcat/Catalina typically uses Subcontext/prefix: "java:comp/env" */
	String DATASOURCE_JNDI = "jdbc/testDB";

	@DefaultValue( "rdbms_test_pu" ) // match persistence.xml
	@Key( JPA_UNIT_NAMES_KEY )
	String[] jpaUnitNames();

	@Override
	@DefaultValue( DATASOURCE_JNDI )
	@Key( AvailableSettings.DATASOURCE )
	String jdbcDatasourceJNDI();

	char NAME_SEPARATOR = '/';

	static void defaultJndiDataSource() throws NamingException
	{
		System.setProperty( Context.INITIAL_CONTEXT_FACTORY,
				org.apache.naming.java.javaURLContextFactory.class.getName() );
		System.setProperty( Context.URL_PKG_PREFIXES,
				org.apache.naming.Constants.Package );

		final Context root = new InitialContext();
		try
		{
			// create required subcontexts
			int lastIndex = MyORMConfig.DATASOURCE_JNDI
					.lastIndexOf( NAME_SEPARATOR );
			if( lastIndex >= 0 )
			{
				Context sub = root;
				for( String name : MyORMConfig.DATASOURCE_JNDI
						.substring( 0, lastIndex )
						.split( "" + NAME_SEPARATOR ) )
					sub = sub.createSubcontext( name );
			}

			// TODO use JDBCDataSourceFactory JNDI object factory?
			final JDBCDataSource ds = new JDBCDataSource();
			ds.setUrl( "jdbc:hsqldb:mem:mytestdb" );
			ds.setUser( "SA" );
			ds.setPassword( "" );
			root.bind( MyORMConfig.DATASOURCE_JNDI, ds );
		} finally
		{
			root.close();
		}
	}

}