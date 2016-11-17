/* $Id: 40046dd67b92b0aa38bca78cc1eac325825aff1f $
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

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.Converter;

import io.coala.config.ConfigUtil;
import io.coala.persist.JDBCConfig;

@Sources( { "classpath:geodb.properties" } )
public interface GeoDBConfig extends JDBCConfig
{
	String JDBC_DRIVER_KEY = "jdbc.driver"; // TODO move constants to JDBCConfig
	String JDBC_HOST_KEY = "jdbc.host";
	String JDBC_DB_KEY = "jdbc.db";
	String JDBC_URL_KEY = "jdbc.url";
	String JDBC_USERNAME_KEY = "jdbc.username";
	String JDBC_PASSWORD_KEY = "jdbc.password";

	@Key( JDBC_DRIVER_KEY )
	@DefaultValue( "org.postgresql.Driver" )
	String jdbcDriver();

	@Key( JDBC_HOST_KEY )
	@DefaultValue( "geodb.rivm.nl" ) //"pgl04-int-p.rivm.nl";
	String jdbcHost();

	@Key( JDBC_DB_KEY )
	@DefaultValue( "sde_gdbrivm" )
	String jdbcDatabase();

	@Override
	@Key( JDBC_URL_KEY )
	@DefaultValue( "jdbc:postgresql://${" + JDBC_HOST_KEY + "}/${" + JDBC_DB_KEY
			+ "}" )
	String url();

	@Override
	@Key( JDBC_USERNAME_KEY )
	String username();

	@Override
	@Key( JDBC_PASSWORD_KEY )
	@ConverterClass( PasswordPromptConverter.class )
	String password();

	@DefaultValue( "" + true )
	boolean ssl();

	@SuppressWarnings( "unchecked" )
	default Map<String, Object> export()
	{
		return ConfigUtil.export( this,
				map( entry( JDBC_PASSWORD_KEY, "<hidden>" ) ) );
	}

	static void exec( final String sql, final Consumer<ResultSet> consumer )
		throws ClassNotFoundException, SQLException
	{
		getOrCreate().execute( sql, consumer );
	}

	static GeoDBConfig getOrCreate( final Map<?, ?>... imports )
	{
		return ConfigCache.getOrCreate( GeoDBConfig.class, imports );
	}

	/**
	 * TODO migrate to JDBCConfig {@link PasswordPromptConverter}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class PasswordPromptConverter implements Converter<String>
	{
		@Override
		public String convert( final Method method, final String input )
		{
			if( input != null && !input.toLowerCase().contains( "prompt" ) )
				return input;

			final String message = "Enter password (now: " + input + ")";
			if( System.console() != null ) // terminal console
				return new String(
						System.console().readPassword( "%s> ", message ) );

			// inside IDE console
			final JPasswordField pf = new JPasswordField();
			return JOptionPane.showConfirmDialog( null, pf, message,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE ) == JOptionPane.OK_OPTION
							? new String( pf.getPassword() ) : input;
		}
	}
}