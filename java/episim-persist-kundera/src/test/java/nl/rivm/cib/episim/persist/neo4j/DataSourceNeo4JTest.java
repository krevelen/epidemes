/* $Id: 4e7b078aa1022e524014203b7fe3e7af3140f96c $
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

import static org.junit.Assert.assertNotNull;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import io.coala.log.LogUtil;

/**
 * {@link DataSourceNeo4JTest}
 * 
 * @version $Id: 4e7b078aa1022e524014203b7fe3e7af3140f96c $
 * @author Rick van Krevelen
 */
public class DataSourceNeo4JTest
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( DataSourceNeo4JTest.class );

	@Test
	public void test()
	{
		LOG.trace( "starting {}", DataSourceNeo4JTest.class.getSimpleName() );
		final GraphDatabaseService neo4j = DataSourceNeo4J.getInstance();
		assertNotNull( "Neo4J not instantiated", neo4j );
		try( Transaction tx = neo4j.beginTx() )
		{
			final Iterable<RelationshipType> relTypes = GlobalGraphOperations
					.at( neo4j ).getAllRelationshipTypes();
			for( RelationshipType relType : relTypes )
				LOG.trace( "Found relationship type: {}", relType );
			tx.success();
		}
	}
}
