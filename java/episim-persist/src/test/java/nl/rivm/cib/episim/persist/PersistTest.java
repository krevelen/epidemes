/* $Id: 92e4d555d6552345f6f63dc1262d87bbed2e4669 $
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
package nl.rivm.cib.episim.persist;

import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.hibernate.ogm.cfg.OgmProperties;
import org.hibernate.ogm.datastore.neo4j.Neo4j;
import org.hibernate.ogm.datastore.neo4j.Neo4jProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import io.coala.enterprise.persist.FactDao;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.LatLong;
import io.coala.persist.HibernateJPAConfig;
import io.coala.persist.JPAUtil;
import nl.rivm.cib.episim.model.disease.infection.Transmission;
import tec.uom.se.unit.Units;

/**
 * {@link PersistTest}
 * 
 * @version $Id: 92e4d555d6552345f6f63dc1262d87bbed2e4669 $
 * @author Rick van Krevelen
 */
public class PersistTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( PersistTest.class );

	/** RIVM National Institute for Public Health and the Environment */
	public LatLong RIVM_POSITION = LatLong.of( 52.1185272, 5.1868699,
			Units.DEGREE_ANGLE );

	/**
	 * {@link MyOGMConfig} for an object-graph model (OGM/NoSQL) implementation
	 * such as MongoDB or Neo4J of our object-relation model (ORM/JPA) entities,
	 * requires vendor-specific hibernate dependency
	 */
	interface MyOGMConfig extends HibernateJPAConfig
	{
		@DefaultValue( "hibernate_test_pu" ) // match persistence.xml
		@Key( JPA_UNIT_NAMES_KEY )
		String[] jpaUnitNames();

//		@DefaultValue( "jdbc:mysql://localhost/testdb" )
//		@DefaultValue( "jdbc:hsqldb:mem:mymemdb" )
//		@DefaultValue( "jdbc:neo4j:bolt://192.168.99.100:7687/db/data" )
//		@DefaultValue( "jdbc:hsqldb:file:target/testdb" )
//		@Key( HIKARI_DATASOURCE_URL_KEY )
//		URI jdbcUrl();

		@DefaultValue( "pilot_testdb" )
		@Key( OgmProperties.DATABASE )
		String database();

		@DefaultValue( "192.168.99.100:7687" ) // :7474 HTTP port, 7687 bolt SSL port
		@Key( OgmProperties.HOST )
		String host();

		@DefaultValue( "neo4j" )
		@Key( OgmProperties.USERNAME )
		String jdbcUsername();

		@DefaultValue( "epidemes" /* PASSWORD_PROMPT_VALUE */ )
		@Key( OgmProperties.PASSWORD )
		@ConverterClass( PasswordPromptConverter.class )
		String jdbcPassword();

		@DefaultValue( Neo4j.EMBEDDED_DATASTORE_PROVIDER_NAME )
		@Key( OgmProperties.DATASTORE_PROVIDER )
		String ogmProvider();

		// hibernate.ogm.neo4j.database_path
		@DefaultValue( "target/" )
		@Key( Neo4jProperties.DATABASE_PATH )
		String hibernateOgmNeo4jDatabasePath();

	}

	private static EntityManagerFactory EMF;

//	private static GraphDatabaseService GRAPH_DB;

//	@SuppressWarnings( "unchecked" )
	@BeforeClass
	public static void createEMF()
	{
//	    graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
//		final GraphDatabaseBuilder builder = new TestGraphDatabaseFactory()
//				.newImpermanentDatabaseBuilder()
//				.setConfig( GraphDatabaseSettings.pagecache_memory, "512M" )
//				.setConfig( GraphDatabaseSettings.string_block_size, "60" )
//				.setConfig( GraphDatabaseSettings.array_block_size, "300" );
//		LOG.trace( "Starting in-memory Neo4J graph database, config: {}",
//				builder );
//		GRAPH_DB = builder.newGraphDatabase();

		// jdbc:neo4j:bolt://localhost:7687

		EMF = ConfigCache.getOrCreate( MyOGMConfig.class ).createEMF();
	}

	@AfterClass
	public static void closeEMF()
	{
		if( EMF != null ) EMF.close();
//		GRAPH_DB.shutdown();
	}

	@Test
	public void testNeo4J() //throws Exception
	{
		// from http://neo4j.com/docs/stable/tutorials-java-unit-testing.html
//		Node n = null;
//		try( final Transaction tx = this.GRAPH_DB.beginTx() )
//		{
//			n = this.GRAPH_DB.createNode();
//			n.setProperty( "name", "Nancy" );
//			tx.success();
//		}
//
//		// The node should have a valid id
//		assertThat( n.getId(), is( greaterThan( -1L ) ) );
//		LOG.trace( "Stored node: {}", n );
//
//		// Retrieve a node by using the id of the created node. The id's and
//		// property should match.
//		try( final Transaction tx = this.GRAPH_DB.beginTx() )
//		{
//			final Node foundNode = this.GRAPH_DB.getNodeById( n.getId() );
//			assertThat( foundNode.getId(), is( n.getId() ) );
//			assertThat( (String) foundNode.getProperty( "name" ),
//					is( "Nancy" ) );
//			LOG.trace( "Found id: {}, name: {} in: {}", foundNode.getId(),
//					foundNode.getProperty( "name" ), foundNode );
//		}
	}

//	@Ignore
	@Test
	public void testDAOs() throws Exception
	{
		final Hibernate5Module hbm = new Hibernate5Module();
		hbm.enable( Hibernate5Module.Feature.FORCE_LAZY_LOADING );
		JsonUtil.getJOM().registerModule( hbm );

//		final OffsetDateTime offset = OffsetDateTime.now();
//		final Region region = new Region.Simple( Region.ID.of( "Netherlands" ),
//				"NL01", null, null );
//		final Place site = Place.of( Place.ID.of( "rivm" ), RIVM_POSITION,
//				region, Geography.DEFAULT );
//		final Instant start = null;
//		final Duration duration = null;
//		final TransmissionSpace space = null;
//		final TransmissionRoute route = null;
//		final Condition primary = null;
//		final Condition secondary = null;
//		final Occupancy cause = /*
//								 * TODO Occupancy.of( start, duration, space,
//								 * route, primary, secondary )
//								 */ null;
//		final Instant time = Instant.of( 3.456, TimeUnits.ANNUM );
		final Transmission event = /*
									 * TODO Transmission.of( time, site, cause )
									 */ null;
		LOG.trace( "Getting EM to store/retrieve event: {}", event );
		JPAUtil.session( EMF, em ->
		{
			final FactDao fact = FactDao.create( em, event );
			LOG.trace( "Persisting: {}", fact );
			em.persist( fact );
		} );
		JPAUtil.session( EMF, em ->
		{
			// TODO use criteria builder
			LOG.trace( "Read table, result: {}",
					em.createQuery( "SELECT f FROM "
							+ FactDao.class.getSimpleName() + " f" )
							.getResultList() );
		} );
	}
}
