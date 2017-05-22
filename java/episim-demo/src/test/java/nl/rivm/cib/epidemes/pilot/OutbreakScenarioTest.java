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
package nl.rivm.cib.epidemes.pilot;

import java.io.IOException;

import javax.naming.NamingException;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.ogm.cfg.OgmProperties;
import org.hibernate.ogm.datastore.neo4j.Neo4j;
import org.hibernate.ogm.datastore.neo4j.Neo4jProperties;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.Transaction;
import io.coala.log.LogUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.name.JndiUtil;
import io.coala.persist.HibernateJPAConfig;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scheduler;
import io.coala.util.MapBuilder;
import nl.rivm.cib.episim.pilot.DemePersistence;
import nl.rivm.cib.episim.pilot.DemeStats;
import nl.rivm.cib.episim.pilot.OutbreakScenario;

/**
 * {@link OutbreakScenarioTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class OutbreakScenarioTest
{
	/** */
	static final Logger LOG = LogUtil.getLogger( OutbreakScenarioTest.class );

	/**
	 * {@link MyORMConfig} for an embedded relational Hypersonic database
	 */
	public interface MyORMConfig extends HibernateJPAConfig
	{

		/** Tomcat/Catalina typically uses Subcontext/prefix: "java:comp/env" */
		String DATASOURCE_JNDI = "jdbc/testDB";

		@DefaultValue( "rdbms_test_pu" ) // match persistence.xml
		@Key( JPA_UNIT_NAMES_KEY )
		String[] jpaUnitNames();

		@Override
		@DefaultValue( DATASOURCE_JNDI )
		@Key( AvailableSettings.DATASOURCE )
		String jdbcDatasourceJNDI();

	}

	/**
	 * {@link MyOGMConfig} for an object/graph model (OGM/NoSQL) implementation
	 * such as MongoDB or Neo4J, of our object-relation model (ORM/JPA)
	 * entities; requires vendor-specific Hibernate dependency
	 */
	public interface MyOGMConfig extends HibernateJPAConfig
	{
		@DefaultValue( "nosql_test_pu" ) // match persistence.xml
		@Key( JPA_UNIT_NAMES_KEY )
		String[] jpaUnitNames();

		@DefaultValue( "pilot_testdb" )
		@Key( OgmProperties.DATABASE )
		String database();

		// :7474 HTTP REST port, 7687 bolt SSL port
		@DefaultValue( "192.168.99.100:7687" )
		@Key( OgmProperties.HOST )
		String host();

		@DefaultValue( "neo4j" )
		@Key( OgmProperties.USERNAME )
		String jdbcUsername();

		@DefaultValue( "epidemes" )
		@Key( OgmProperties.PASSWORD )
		@ConverterClass( PasswordPromptConverter.class )
		String jdbcPassword();

		@Override
		default String jdbcPasswordKey()
		{
			return OgmProperties.PASSWORD;
		}

		@DefaultValue( Neo4j.EMBEDDED_DATASTORE_PROVIDER_NAME )
		@Key( OgmProperties.DATASTORE_PROVIDER )
		String ogmProvider();

		@DefaultValue( "target/" )
		@Key( Neo4jProperties.DATABASE_PATH )
		String hibernateOgmNeo4jDatabasePath();

	}

	@BeforeClass
	public static void setupJndiDataSource() throws NamingException
	{
		JndiUtil.bindLocally( MyORMConfig.DATASOURCE_JNDI, '/', () ->
		{
			final JDBCDataSource ds = new JDBCDataSource();
//			ds.setUrl( "jdbc:hsqldb:mem:mytestdb" );
			ds.setUrl( "jdbc:hsqldb:file:target/mytestdb" );
			ds.setUser( "SA" );
			ds.setPassword( "" );
			return ds;
		} );
	}

	@Test
	public void measlesTest() throws NamingException, IOException
	{
		LOG.info( "Starting measles test" );

		// configure replication FIXME via LocalConfig?
		ConfigCache.getOrCreate( ReplicateConfig.class, MapBuilder.unordered()
				.put( ReplicateConfig.DURATION_KEY, "" + 1 ) // duration (days)
				.put( ReplicateConfig.OFFSET_KEY, "2012-01-01" ).build() );

		// connect and setup database persistence

		// load scenario
		final LocalBinder binder = LocalConfig.builder().withId( "pop1" )

				// time API (virtual time management)
				.withProvider( Scheduler.class, Dsol3Scheduler.class )

				// math API (pseudo random)
				.withProvider( PseudoRandom.Factory.class,
						Math3PseudoRandom.MersenneTwisterFactory.class )
				.withProvider( ProbabilityDistribution.Factory.class,
						Math3ProbabilityDistribution.Factory.class )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )

				// enterprise API (facts, actors)
				.withProvider( Actor.Factory.class,
						Actor.Factory.LocalCaching.class )
				.withProvider( Transaction.Factory.class,
						Transaction.Factory.LocalCaching.class )
				.withProvider( Fact.Factory.class,
						Fact.Factory.SimpleProxies.class )
				.withProvider( FactBank.class,
//						FactBank.SimpleJPA.class 
//						FactBank.SimpleCache.class
						FactBank.SimpleDrain.class
				//
				).withProvider( FactExchange.class, FactExchange.SimpleBus.class )

				// epidemes API
				.withProvider( DemePersistence.class,
						DemePersistence.MemCache.class )
				.withProvider( DemeStats.class, DemeStats.Simple.class )

				.build().createBinder(
		//
//						Collections.singletonMap( EntityManagerFactory.class, ConfigCache
//						.getOrCreate( MyORMConfig.class ).createEMF() )
		//
		);

		binder.inject( FactExchange.class ).snif()
				.subscribe( f -> LOG.trace( "Sniffed {}", f ) );

		// run scenario & generate output
		binder.inject( OutbreakScenario.class ).run();

		// confirm output
//		JPAUtil.session( EMF, em ->
//		{
//			LOG.info( "Got DB entries: {}", FactDao.class );
//		} );

		LOG.info( "Completed measles test" );
	}

}
