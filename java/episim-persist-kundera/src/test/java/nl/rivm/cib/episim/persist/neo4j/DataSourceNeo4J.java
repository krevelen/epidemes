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
package nl.rivm.cib.episim.persist.neo4j;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import io.coala.log.LogUtil;

/**
 * {@link DataSourceNeo4J}
 * 
 * @version $Id: 2596764476bd6170ba5eb0a206706f65aaecc4ee $
 * @author Rick van Krevelen
 */
public class DataSourceNeo4J
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( DataSourceNeo4J.class );

	/** TODO from config */
	private static final File CONFIG_PATH = new File( "neo4j.properties" );

	/** TODO from config */
	private static final File DB_PATH = new File( "db" );

	private static GraphDatabaseService INSTANCE = null;

	public static GraphDatabaseService getInstance()
	{
		if( INSTANCE == null )
		{
			final GraphDatabaseBuilder builder = new GraphDatabaseFactory()
					.newEmbeddedDatabaseBuilder( DB_PATH );
			if( CONFIG_PATH.exists() )
			{
				LOG.trace( "loading config from path: {}", CONFIG_PATH );
				builder.loadPropertiesFromFile( CONFIG_PATH.getAbsolutePath() );
			} else
			{
				LOG.trace( "Not found: {}, skipping...", CONFIG_PATH );
				builder.setConfig( GraphDatabaseSettings.pagecache_memory,
						"512M" )
						.setConfig( GraphDatabaseSettings.string_block_size,
								"60" )
						.setConfig( GraphDatabaseSettings.array_block_size,
								"300" );
			}
			INSTANCE = builder.newGraphDatabase();

			// Registers a shutdown hook for the Neo4j instance so that it
			// shuts down nicely when the VM exits (even if you "Ctrl-C" the
			// running application).
			Runtime.getRuntime().addShutdownHook( new Thread( () ->
			{
				LOG.trace( "Shutting down Neo4J" );
				INSTANCE.shutdown();
			} ) );
		}
		return INSTANCE;
	}
}
