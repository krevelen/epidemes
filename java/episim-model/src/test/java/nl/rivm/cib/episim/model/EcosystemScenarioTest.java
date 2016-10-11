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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;

import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.EnterpriseTest.World.Sale;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.Transaction;
import io.coala.eve3.Eve3Exposer;
import io.coala.guice4.Guice4LocalBinder;
import io.coala.inter.Exposer;
import io.coala.log.LogUtil;
import io.coala.time.Proactive;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scheduler;
import io.coala.util.DecimalUtil;
import net.jodah.concurrentunit.Waiter;

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

	interface FactViewer
	{
		@Access( AccessType.PUBLIC )
		List<Fact> facts();
	}

	@Singleton
	public static class EcosystemScenario implements Proactive
	{

		interface Mother extends Actor<Birth>
		{

		}

		/**
		 * {@link Birth} initiator: population, executor: mother
		 */
		interface Birth extends Fact
		{
			/**
			 * implemented by proxy, using {@link #properties()}
			 * 
			 * @return the {@link java.time.Instant posix-time} ETA
			 */
			java.time.Instant getPosixETA();

			/**
			 * implemented by proxy, using {@link #properties()}
			 * 
			 * @param value the {@link java.time.Instant posix-time} ETA
			 */
			void setPosixETA( java.time.Instant value );

			/**
			 * @param value the {@link java.time.Instant posix-time} ETA
			 * @return this {@link Sale} object, to allow chaining
			 */
			default Birth withPosixETA( java.time.Instant value )
			{
				setPosixETA( value );
				return this;
			}

			/**
			 * @param offset the {@link java.time.Instant posix-time} offset to
			 *            convert from
			 * @return a virtual {@link io.coala.time.Instant} conversion
			 */
			default io.coala.time.Instant
				getVirtualETA( java.time.Instant offset )
			{
				return io.coala.time.Instant.of( getPosixETA(), offset );
			}
		}

		/**
		 * {@link Death} initiator: population, executor: person
		 */
		interface Death extends Fact
		{

		}

		private final Scheduler scheduler;

		@Inject
		private Actor.Factory actors;

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

			// create organization
			int total = 160000;
			long clock = System.currentTimeMillis();
			for( int i = 0; i < total; i++ )
			{
				if( System.currentTimeMillis()-clock>1000 )
				{
					LOG.trace( LogUtil.messageOf(
							"{0} ({1,number,#.##%}) persons added, "
							+ "jvm free: ~{2,number,#,#00.#}MB (~{3,number,#.#%})",
							i, DecimalUtil.divide( i, total ),
							DecimalUtil.divide(Runtime.getRuntime().freeMemory(),1024*1024),
							DecimalUtil.divide(
									Runtime.getRuntime().freeMemory(), Runtime
											.getRuntime().totalMemory() ) ) );
					clock = System.currentTimeMillis();
				}
				final Actor<Fact> person = this.actors.create( "person" + i );
//				final Mother mother = person.asExecutor( //Birth.class,
//						Mother.class );
//
//				// add business rule(s)
//				mother.emit( FactKind.REQUESTED ).subscribe( fact ->
//				{
//					after( Duration.of( 1, Units.DAYS ) ).call( t ->
//					{
//						final Birth st = mother.respond( fact, FactKind.STATED )
//								.with( "myParam1", "myValue1" ).commit( true );
//						LOG.trace( "t={}, {} responded: {} for incoming: {}",
//								t.prettify( this.actors.offset() ), mother.id(),
//								st, fact );
//					} );
//				} );
//
//				// initiate transactions with self
//				atEach( Timing.of( "0 0 0 14 * ? *" )
//						.offset( this.actors.offset() ).iterate(), t ->
//						{
//							mother.initiate( Birth.class, mother.id(), null,
//									t.add( 1 ), Collections.singletonMap(
//											"myParam0", "myValue0" ) );
//						} );
			}
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

		final Waiter waiter = new Waiter();
		model.scheduler().time().subscribe( time ->
		{
			// virtual time passes...
		}, waiter::rethrow, waiter::resume );
		model.scheduler().resume();
		waiter.await( 1, TimeUnit.HOURS );
		LOG.info( "Ecosystem test complete" );
	}

}
