/* $Id: 5d9e7c1ed3ccfced243cecfbff29647d70388ac7 $
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
package nl.rivm.cib.episim.mas.eve;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import com.almende.eve.capabilities.Config;
import com.almende.eve.config.YamlReader;
import com.almende.eve.deploy.Boot;
import com.almende.eve.transport.http.embed.JettyLauncher;
import com.thetransactioncompany.cors.CORSFilter;

import io.coala.log.LogUtil;
import io.coala.util.FileUtil;

/**
 * {@link EpisimMasServer}
 * 
 * @version $Id: 5d9e7c1ed3ccfced243cecfbff29647d70388ac7 $
 * @author Rick van Krevelen
 * 
 * TODO try RxNetty server?
 */
public class EpisimMasServer
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( EpisimMasServer.class );

	/**
	 * The main method.
	 *
	 * @param args the command line arguments
	 * @throws IOException e.g. file not found
	 */
	public static void main( final String[] args ) throws IOException
	{

		final Config configfile = YamlReader
				.load( FileUtil.toInputStream( args.length==0?"eve.yaml":args[0] ) ).expand();

		Boot.boot( configfile );

		final JettyLauncher launcher = new JettyLauncher();

		// Cross-Origin Request Blocked filter for Browser JSON HTTP Requests
		launcher.addFilter( CORSFilter.class.getName(), "/*" );

		/*
		 * // Init the wink REST servlet. final ObjectNode winkCfg =
		 * JOM.createObjectNode(); winkCfg.withArray( "initParams" ) .add(
		 * JOM.createObjectNode() .put( "key", "javax.ws.rs.Application" ) .put(
		 * "value", WinkApplication.class.getName() ) ); launcher.add( new
		 * RestServlet(), URI.create( "/rest/v1/" ), winkCfg );
		 */

		LOG.trace( "STARTED" );
	}

}