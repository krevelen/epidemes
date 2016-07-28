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
package nl.rivm.cib.episim.persist.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;

/**
 * {@link JDBCUtil}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class JDBCUtil
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( JDBCUtil.class );

	private JDBCUtil()
	{
		// singleton
	}

	public static void execute( final JDBCConfig conf, final String sql,
		final Consumer<ResultSet> consumer ) throws SQLException
	{
		// FIXME use connection pool with time-outs?
		try( final Connection conn = DriverManager.getConnection( conf.url(),
				conf.username(), conf.password() );
				final Statement stmt = conn.createStatement();
				final ResultSet rs = stmt.executeQuery( sql ); )
		{
			consumer.accept( rs );
		}
	}

	/**
	 * @param rs
	 * @return
	 */
	public static CharSequence toString( final ResultSet rs )
	{
		final StringBuilder out = new StringBuilder(
				rs.getClass().getSimpleName() + "[" );
		try
		{
			boolean colNames = false;
			while( rs.next() )
			{
				out.append( "\r\n" );
				if( !colNames )
				{
					for( int i = 1, n = rs.getMetaData()
							.getColumnCount(); i <= n; i++ )
						out.append(
								" | " + rs.getMetaData().getColumnLabel( i ) );
					out.append( " |\r\n" );
					colNames = true;
				}
				for( int i = 1, n = rs.getMetaData()
						.getColumnCount(); i <= n; i++ )
					out.append( " | " + rs.getString( i ) );
				out.append( " |" );
			}
			out.append( " ]" );
			if( rs.getWarnings() != null )
				out.append( " warning: " + rs.getWarnings() );
		} catch( final SQLException e )
		{
			LOG.error( "Problem describing result set", e );
		}
		return out;
	}
}
