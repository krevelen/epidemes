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
package nl.rivm.cib.episim.persist.hsql;

import java.sql.SQLException;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.persist.JDBCConfig;
import io.coala.persist.JDBCUtil;

/**
 * {@link DataSourceHSQLTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class DataSourceHSQLTest
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( DataSourceHSQLTest.class );

	@Test
	public void testJDBC() throws SQLException
	{
//		final DataSourceHSQL jdbc = DataSourceHSQL.getInstance();
		final JDBCConfig jdbc = ConfigCache.getOrCreate( JDBCConfig.class );
		jdbc.execute( "CREATE TABLE IF NOT EXISTS "//
				+ "REGISTRATION " //
				+ "(id INTEGER not NULL, " //
				+ " first VARCHAR(255), " //
				+ " last VARCHAR(255), " //
				+ " PRIMARY KEY ( id )" //
				+ ")", rs ->
				{
					LOG.trace( "Created table, result: {}",
							JDBCUtil.toString( rs ) );
				} );

		jdbc.execute( "TRUNCATE TABLE REGISTRATION", rs ->
		{
			LOG.trace( "Cleared table, result: {}", JDBCUtil.toString( rs ) );
		} );

		jdbc.execute( "INSERT INTO REGISTRATION " //
				+ "(id, first, last) " //
				+ "VALUES " //
				+ "(0,'Rick','van Krevelen')" //
				+ ", (1,'Joram','Hoogink')", rs ->
				{
					LOG.trace( "Filled table, result: {}",
							JDBCUtil.toString( rs ) );
				} );

		jdbc.execute( "SELECT * FROM REGISTRATION ", rs ->
		{
			LOG.trace( "Read table, result: {}", JDBCUtil.toString( rs ) );
		} );
	}

}
