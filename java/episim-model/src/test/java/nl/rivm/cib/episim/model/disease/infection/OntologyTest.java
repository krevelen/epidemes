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
package nl.rivm.cib.episim.model.disease.infection;

import java.util.Collections;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.Transaction;
import io.coala.time.SchedulerConfig;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.person.Redirection;

/**
 * {@link OntologyTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class OntologyTest
{
	/** */
	private static final Logger LOG = LogManager
			.getLogger( OntologyTest.class );

	/*
	 * public interface MyJPAConfig extends HibernateJPAConfig {
	 * 
	 * @DefaultValue( "model_test_pu" ) // match persistence.xml
	 * 
	 * @Key( JPA_UNIT_NAMES_KEY ) String[] jpaUnitNames();
	 * 
	 * // @DefaultValue( "jdbc:mysql://localhost/testdb" ) // @DefaultValue(
	 * "jdbc:neo4j:bolt://192.168.99.100:7687/db/data" ) // @DefaultValue(
	 * "jdbc:hsqldb:file:target/testdb" )
	 * 
	 * @DefaultValue( "jdbc:hsqldb:mem:mymemdb" )
	 * 
	 * @Key( AvailableSettings.URL ) URI jdbcUrl(); }
	 */

	private LocalBinder binder = null;

	@Before
	public void resetBinder()
	{
		// configure replication FIXME via LocalConfig?
		ConfigCache.getOrCreate( SchedulerConfig.class, Collections
				.singletonMap( SchedulerConfig.DURATION_KEY, "" + 200 ) );

		// configure tooling
		this.binder = LocalConfig.builder().withId( "world1" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class )
				.withProvider( Actor.Factory.class,
						Actor.Factory.LocalCaching.class )
				.withProvider( Transaction.Factory.class,
						Transaction.Factory.LocalCaching.class )
				.withProvider( Fact.Factory.class,
						Fact.Factory.SimpleProxies.class )
				.withProvider( FactBank.class, FactBank.SimpleCache.class )
				.withProvider( FactExchange.class,
						FactExchange.SimpleBus.class )
				.build()
				.createBinder( /*
								 * Collections .singletonMap(
								 * EntityManagerFactory.class, ConfigFactory
								 * .create( MyJPAConfig.class ).createEMF() )
								 */ );
	}

	@Test
	public void testDirector()
	{
		LOG.info( "Director" );
		final Actor<Fact> person1 = this.binder.inject( Actor.Factory.class )
				.create( "person1" );
		final Redirection.Director director = person1
				.subRole( Redirection.Director.class );
		// start
		this.binder.inject( Scheduler.class ).onReset( s ->
		{
//			person1.after( Duration.ZERO ).call( () ->
//			{
				final Redirection rq = director.initiate( director.id() )
						.commit();
				LOG.trace( "self-initiated with rq: {}", rq );
//			} );
		} );
		this.binder.inject( Scheduler.class ).run();
		LOG.info( "Director done" );
	}

	@Test
	public void testPlanner()
	{
		LOG.info( "Planner" );

	}

	@Test
	public void testActivator()
	{
		LOG.info( "Activator" );

	}

	@Test
	public void testAfflictor()
	{
		LOG.info( "Afflictor" );

	}

	@Test
	public void testPathogen()
	{
		LOG.info( "Pathogen" );

	}

	@Test
	public void testContagium()
	{
		LOG.info( "Contagium" );

	}
}
