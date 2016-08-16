/* $Id: 2596764476bd6170ba5eb0a206706f65aaecc4ee $
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.hsqldb.jdbc.JDBCDriver;

import io.coala.exception.ExceptionFactory;
import io.coala.log.LogUtil;
import io.coala.persist.JDBCConfig;
import io.coala.persist.JDBCUtil;

/**
 * {@link DataSourceHSQL}
 * 
 * @version $Id: 2596764476bd6170ba5eb0a206706f65aaecc4ee $
 * @author Rick van Krevelen
 */
public class DataSourceHSQL
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( DataSourceHSQL.class );

	// HSQLDB guide at: http://hsqldb.org/doc/guide/running-chapt.html

	private static DataSourceHSQL INSTANCE = null;

	public synchronized static DataSourceHSQL getInstance()
	{
		if( INSTANCE == null )
		{
			INSTANCE = new DataSourceHSQL();
			Runtime.getRuntime().addShutdownHook( new Thread( () ->
			{
				try
				{
					INSTANCE.execute( "SHUTDOWN", rs ->
					{
						System.out.println( "Hypersonic shutdown successful: "
								+ JDBCUtil.toString( rs ) );
					} );
				} catch( final Throwable e )
				{
					System.err.println( "Problem shutting down Hypersonic" );
					e.printStackTrace();
				}
			} ) );
		}
		return INSTANCE;
	}

	public void execute( final String sql, final Consumer<ResultSet> consumer )
		throws SQLException
	{
		this.conf.execute( sql, consumer );
	}

	private final JDBCConfig conf;

	private DataSourceHSQL()
	{
		this.conf = ConfigCache.getOrCreate( JDBCConfig.class );
		try
		{
			Class.forName( this.conf.driver() );
		} catch( ClassNotFoundException e )
		{
			throw ExceptionFactory.createUnchecked( "Problem loading driver",
					e );
		}
		LOG.trace( "Loaded Hypersonic JDBC driver: {}", JDBCDriver.class );
	}

}
