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
package nl.rivm.cib.epidemes.rest;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import io.coala.json.JsonUtil;
import io.coala.name.JndiUtil;

/**
 * {@link JsonServiceTest} tests {@link JsonService}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class JsonServiceTest
{

	/** */
	private static final Logger LOG = LogManager
			.getLogger( JsonServiceTest.class );

	@BeforeClass
	public static void setupDatasource() throws NamingException
	{
		JndiUtil.bindLocally( JsonService.DATASOURCE_JNDI, '/', () ->
		{
			final JDBCDataSource ds = new JDBCDataSource();
			ds.setUrl( "jdbc:hsqldb:mem:testdb" );
			ds.setUser( "SA" );
			ds.setPassword( "" );
			return ds;
		} );
	}

	@Test
	public void initTest()
	{
		LOG.info( "start {}", getClass().getSimpleName() );
		final JsonService svc = new JsonService();
		LOG.trace( "Municipal data: {}",
				JsonUtil.toJSON( svc.getMunicipalValues() ) );
		LOG.info( "done {}", getClass().getSimpleName() );
	}

}
