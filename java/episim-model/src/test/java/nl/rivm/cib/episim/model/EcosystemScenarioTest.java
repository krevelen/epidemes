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
package nl.rivm.cib.episim.model;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.unit.NonSI;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;

import io.coala.bind.LocalConfig;
import io.coala.config.InjectConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.CompositeActor;
import io.coala.enterprise.CoordinationFact;
import io.coala.enterprise.CoordinationFactType;
import io.coala.enterprise.Organization;
import io.coala.enterprise.Transaction;
import io.coala.eve3.Eve3Exposer;
import io.coala.guice4.Guice4LocalBinder;
import io.coala.inter.Exposer;
import io.coala.log.InjectLogger;
import io.coala.log.LogUtil;
import io.coala.time.Duration;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import net.jodah.concurrentunit.Waiter;
import nl.rivm.cib.episim.model.scenario.Scenario;

/**
 * {@link EcosystemScenarioTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class EcosystemScenarioTest
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( EcosystemScenarioTest.class );

	interface FactBank
	{
		void save( CoordinationFact fact );

		<T extends CoordinationFact> Iterable<T> find( Class<T> factType );
	}

	interface FactViewer
	{
		@Access( AccessType.PUBLIC )
		List<CoordinationFact> facts();
	}

	@Singleton
	public static class EcosystemScenario implements Scenario, FactViewer
	{

		interface BirthFact extends CoordinationFact
		{
			// empty 
		}

		private final Scheduler scheduler;

		@Inject
		private Organization.Factory orgFactory;

		@InjectLogger
		private Logger LOG;

		@InjectConfig
		private ReplicateConfig config;

		@Inject
		public EcosystemScenario( final Scheduler scheduler )
		{
			this.scheduler = scheduler;
			scheduler.onReset( this::init );
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		private void init() throws ParseException
		{
			LOG.info( "initializing..." );
			final Date offset = Date.from( this.config.offset() );

			// create organization
			final Organization org1 = this.orgFactory.create( "org1" );
			final CompositeActor sales = org1.actor( "sales" );

			// add business rule(s)
			sales.on( BirthFact.class, sales.id(), fact ->
			{
				sales.after( Duration.of( 1, NonSI.DAY ) ).call( t ->
				{
					final BirthFact response = sales.createResponse( fact,
							CoordinationFactType.STATED, true, null, Collections
									.singletonMap( "myParam1", "myValue1" ) );
					LOG.trace( "t={}, {} responded: {} for incoming: {}",
							t.prettify( offset ), sales.id(), response, fact );
				} );
			} );

			// consume own outgoing Birth requests
			org1.outgoing( BirthFact.class, CoordinationFactType.REQUESTED )
					.doOnNext( f ->
					{
						org1.consume( f );
					} ).subscribe();

			// spawn initial transactions with self
			org1.atEach(
					Timing.of( "0 0 0 14 * ? *" ).offset( offset ).iterate(),
					t ->
					{
						sales.createRequest( BirthFact.class, sales, null,
								t.add( 1 ), Collections.singletonMap(
										"myParam0", "myValue0" ) );
					} );
		}

		@Override
		public List<CoordinationFact> facts()
		{
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Test
	public void testEcosystem()
		throws TimeoutException, InstantiationException, IllegalAccessException
	{
		// configure replication FIXME via LocalConfig?
		ConfigCache.getOrCreate( ReplicateConfig.class, Collections
				.singletonMap( ReplicateConfig.DURATION_KEY, "" + 500 ) );

		// configure tooling
		final LocalConfig config = LocalConfig.builder().withId( "ecosysSim" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class )
				.withProvider( Organization.Factory.class,
						Organization.Factory.LocalCaching.class )
				.withProvider( Transaction.Factory.class,
						Transaction.Factory.LocalCaching.class )
				.withProvider( CoordinationFact.Factory.class,
						CoordinationFact.Factory.Simple.class )
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

		final Waiter waiter = new Waiter();
		model.scheduler().time().subscribe( time ->
		{
			// virtual time passes...
		}, error ->
		{
			waiter.rethrow( error );
		}, () ->
		{
			waiter.resume();
		} );
		model.scheduler().resume();
		waiter.await( 1, TimeUnit.SECONDS );
		LOG.info( "Ecosystem test complete" );
	}

}
