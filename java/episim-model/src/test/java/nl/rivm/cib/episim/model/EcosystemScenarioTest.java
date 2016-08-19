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

import static org.aeonbits.owner.util.Collections.entry;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.unit.NonSI;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.dsol3.Dsol3Config;
import io.coala.enterprise.CompositeActor;
import io.coala.enterprise.CoordinationFact;
import io.coala.enterprise.CoordinationFactType;
import io.coala.enterprise.Organization;
import io.coala.log.LogUtil;
import io.coala.time.Duration;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import io.coala.time.Units;
import net.jodah.concurrentunit.Waiter;
import nl.rivm.cib.episim.model.scenario.Scenario;
import nl.rivm.cib.episim.model.scenario.ScenarioConfig;

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

	public static class EcosystemScenario implements Scenario
	{

		interface BirthFact extends CoordinationFact
		{
			// empty 
		}

		// FIXME @InjectLogger
		private static final Logger LOG = LogUtil
				.getLogger( EcosystemScenario.class );

		// FIXME @Inject
		private Scheduler scheduler;

		// FIXME @Inject
		private CoordinationFact.Factory factFactory = new CoordinationFact.SimpleFactory();

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public void init( final Scheduler scheduler ) throws Exception
		{
			LOG.info( "initializing..." );
			this.scheduler = scheduler;

			final ScenarioConfig config = ConfigCache
					.getOrCreate( ScenarioConfig.class );
			final Date offset = config.offset().toDate();
			scheduler.time().subscribe( t ->
			{
				LOG.trace( "t={}, date: {}", t.prettify( Units.DAYS, 2 ),
						t.prettify( offset ) );
			}, e ->
			{
				LOG.error( "Problem in model", e );
			}, () ->
			{
				LOG.info( "completed, t={}", scheduler.now() );
			} );

			// create organization
			final Organization org1 = Organization.of( scheduler, "ind1",
					this.factFactory );
			final CompositeActor sales = org1.actor( "sales" );

			// add business rule(s)
			sales.on( BirthFact.class, org1.id(), fact ->
			{
				sales.after( Duration.of( 1, NonSI.DAY ) ).call( t ->
				{
					final BirthFact response = sales.createResponse( fact,
							CoordinationFactType.STATED, true, null, Collections
									.singletonMap( "myParam1", "myValue1" ) );
					LOG.trace( "t={}, {} responded: {} for incoming: {}", t,
							sales.id(), response, fact );
//					throw new Exception();
				} );
			} );

			// observe generated facts
			org1.outgoing().subscribe( fact ->
			{
				LOG.trace( "t={}, outgoing: {}", org1.now(), fact );
			} );

			org1.outgoing( BirthFact.class, CoordinationFactType.REQUESTED )
					.doOnNext( f ->
					{
						org1.consume( f );
					} ).subscribe();

			// spawn initial transactions with self
			// FIXME recursive splitting to async stream join
			scheduler.atEach(
					Timing.of( "0 0 0 14 * ? *" ).asObservable( offset ), t ->
					{
						sales.createRequest( BirthFact.class, org1.id(), null,
								t.add( 1 ), Collections.singletonMap(
										"myParam2", "myValue2" ) );
					} );
		}
	}

	@Test
	public void testEcosystem()
		throws TimeoutException, InstantiationException, IllegalAccessException
	{
		final ScenarioConfig config = ConfigCache
				.getOrCreate( ScenarioConfig.class,
						Collections.singletonMap(
								ScenarioConfig.INITIALIZER_KEY,
								EcosystemScenario.class.getName() ) );

		final Dsol3Config dsol = Dsol3Config.of(
				entry( Dsol3Config.ID_KEY, "ecosystemTest" ),
				entry( Dsol3Config.START_TIME_KEY,
						"0 " + config.duration().unwrap().getUnit() ),
				entry( Dsol3Config.RUN_LENGTH_KEY,
						config.duration().unwrap().getValue().toString() ) );
		LOG.info( "Starting ecosystem test, config: {}", config.toYAML() );
		final Scheduler scheduler = dsol.create( config.initializer() );

		final Waiter waiter = new Waiter();
		scheduler.time().subscribe( time ->
		{
			// virtual time passes...
		}, error ->
		{
			waiter.rethrow( error );
		}, () ->
		{
			waiter.resume();
		} );
		scheduler.resume();
		waiter.await( 1, TimeUnit.SECONDS );
		LOG.info( "Ecosystem test complete" );
	}

}
