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
import java.util.Collections;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
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
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.locate.travel.Vehicle;
import nl.rivm.cib.episim.pilot.OutbreakScenario;
import nl.rivm.cib.episim.pilot.OutbreakScenario.MyDirectory;

/**
 * {@link PilotTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PilotTest
{
	/** */
	static final Logger LOG = LogUtil.getLogger( PilotTest.class );

	@BeforeClass
	public static void setupJndiDataSource() throws NamingException
	{
		MyORMConfig.defaultJndiDataSource();
	}

	@Test
	public void measlesTest() throws NamingException, IOException
	{
		LOG.info( "Starting measles test" );

		// connect and setup database persistence
		final EntityManagerFactory EMF = ConfigCache
				.getOrCreate( MyORMConfig.class ).createEMF();

		// load scenario
		final LocalBinder binder = LocalConfig.builder().withId( "outbreak1" )

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
				.withProvider( FactBank.class, FactBank.SimpleJPA.class )
				.withProvider( FactExchange.class,
						FactExchange.SimpleBus.class )

				// epidemes API
				.withProvider( Region.Directory.class, MyDirectory.class )
				.withProvider( Place.Directory.class, MyDirectory.class )
				.withProvider( Vehicle.Directory.class, MyDirectory.class )

				.build().createBinder( Collections
						.singletonMap( EntityManagerFactory.class, EMF ) );

		// run scenario & generate output
		binder.inject( OutbreakScenario.class ).run();

		// confirm output
//		JPAUtil.session( EMF, em ->
//		{
//			LOG.info( "Got DB entries: {}", FactDao.class );
//		} );

		EMF.close();
		LOG.info( "Completed measles test" );
	}

}
