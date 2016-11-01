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
package nl.rivm.cib.episim.pilot;

import java.util.Collections;
import java.util.List;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;

import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.Transaction;
import io.coala.eve3.Eve3Exposer;
import io.coala.guice4.Guice4LocalBinder;
import io.coala.inter.Exposer;
import io.coala.log.LogUtil;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scheduler;

/**
 * {@link EcosystemScenarioTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class EcosystemScenarioTest
{

	/** */
	static final Logger LOG = LogUtil.getLogger( EcosystemScenarioTest.class );

	interface FactViewer
	{
		@Access( AccessType.PUBLIC )
		List<Fact> facts();
	}

	public void testEcosystem() throws Exception
	{
		// configure replication FIXME via LocalConfig?
		ConfigCache.getOrCreate( ReplicateConfig.class, Collections
				.singletonMap( ReplicateConfig.DURATION_KEY, "" + 500 ) );

		// configure tooling
		final LocalConfig config = LocalConfig.builder().withId( "ecosysSim" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class )
				.withProvider( Actor.Factory.class,
						Actor.Factory.LocalCaching.class )
				.withProvider( Transaction.Factory.class,
						Transaction.Factory.LocalCaching.class )
				.withProvider( Fact.Factory.class,
						Fact.Factory.SimpleProxies.class )
				.withProvider( FactBank.Factory.class,
						FactBank.Factory.InMemory.class )
				.withProvider( Exposer.class, Eve3Exposer.class )
//				.withProvider( Invoker.class, Eve3Invoker.class )
//				.withProvider( PseudoRandom.Factory.class,
//				Math3PseudoRandom.MersenneTwisterFactory.class )
//				.withProvider( ProbabilityDistribution.Factory.class,
//						Math3ProbabilityDistribution.Factory.class )
//				.withProvider( ProbabilityDistribution.Parser.class,
//						DistributionParser.class )
				.build();

		LOG.info( "Starting Ecosystem test, config: {}", config );
		final EcosystemScenario model = Guice4LocalBinder.of( config )
				.inject( EcosystemScenario.class );

		// FIXME apply outcome-driven event generation pruning

		// person: { activity[stepId], disease[ condition[stepId], attitude[stepId] ] }

		// calculate vaccination degree = #vacc / #non-vacc, given:
		//   disease/vaccine v 
		//   region(s) r
		//   cohort birth range [t1,t2]

		model.scheduler().run();
		LOG.info( "Ecosystem test complete" );
	}

}
