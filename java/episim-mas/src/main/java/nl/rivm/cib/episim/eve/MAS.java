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
package nl.rivm.cib.episim.eve;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.apache.wink.server.internal.servlet.RestServlet;

import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.eve.deploy.Boot;
import com.almende.eve.transport.http.embed.JettyLauncher;
import com.almende.util.jackson.JOM;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thetransactioncompany.cors.CORSFilter;

import nl.rivm.cib.episim.ws.AggregatorWS;

/**
 * {@link MAS}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class MAS
{

	/**
	 * {@link WinkApplication}
	 */
	public class WinkApplication extends Application
	{

		/** */
		private final Set<Class<?>> classes = new HashSet<Class<?>>(Arrays.asList(
				AggregatorWS.class));

		@Override
		public Set<Class<?>> getClasses()
		{
			return this.classes;
		}
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws FileNotFoundException the file not found exception
	 * @throws ServletException
	 */
	public static void main( final String[] args )
		throws FileNotFoundException, ServletException
	{
		final Config configfile = YamlReader
				.load( new FileInputStream( new File( args[0] ) ) ).expand();

		Boot.boot( configfile );

		// final WebAppContext aggregator = new WebAppContext("aggregator.war",
		// "/aggregator");
		// aggregator.setServer(server);

		final JettyLauncher launcher = new JettyLauncher();

		// Cross-Origin Request Blocked filter for Browser JSON HTTP Requests
		launcher.addFilter( CORSFilter.class.getName(), "/*" );

		// Init the wink REST servlet.
		final ObjectNode winkCfg = JOM.createObjectNode();
		winkCfg.withArray( "initParams" )
				.add( JOM.createObjectNode()
						.put( "key", "javax.ws.rs.Application" )
						.put( "value", WinkApplication.class.getName() ) );
		launcher.add( new RestServlet(), URI.create( "/rs/" ), winkCfg );
	}

}