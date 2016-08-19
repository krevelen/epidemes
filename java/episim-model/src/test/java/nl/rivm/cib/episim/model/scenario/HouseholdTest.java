/* $Id: 276c926515013cd391932010126debe6aec6ac89 $
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
package nl.rivm.cib.episim.model.scenario;

import static org.aeonbits.owner.util.Collections.entry;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Config;
import io.coala.guice4.Guice4LocalBinder;
import io.coala.log.LogUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Scheduler;
import io.coala.time.Units;
import net.jodah.concurrentunit.Waiter;

/**
 * {@link HouseholdTest}
 * 
 * @version $Id: 276c926515013cd391932010126debe6aec6ac89 $
 * @author Rick van Krevelen
 */
public class HouseholdTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( HouseholdTest.class );

	/**
	 * This test should:
	 * <ol>
	 * </ol>
	 * 
	 * @throws Throwable
	 */
	@Test
	public void householdCompositionTest() throws TimeoutException
	{
		LOG.trace( "Initializing household composition scenario..." );

		Units.DAILY.toString(); // FIXME initialize Units.class more elegantly
		final long seed = 1234L; // FIXME put in some replicator config somehow

		@SuppressWarnings( "serial" )
		final LocalBinder binder = Guice4LocalBinder.of( LocalConfig.builder()
				.withProvider( Scenario.class, Geard2011Scenario.class )
				.withProvider( ProbabilityDistribution.Factory.class,
						Math3ProbabilityDistribution.Factory.class )
				.build(), new HashMap<Class<?>, Object>()
				{
					{
						put( PseudoRandom.class, Math3PseudoRandom.Factory
								.ofMersenneTwister().create( "rng", seed ) );
					}
				} );

		// TODO initiate scheduler through (replication-specific) binder
		final Dsol3Config config = Dsol3Config.of(
				entry( Dsol3Config.ID_KEY, "geardTest" ),
				entry( Dsol3Config.START_TIME_KEY, "0 day" ),
				entry( Dsol3Config.RUN_LENGTH_KEY, "100" ) );
		LOG.info( "Starting household test, config: {}", config.toYAML() );
		final Scheduler scheduler = config
				.create( binder.inject( Scenario.class )::init );

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
		waiter.await( 20, TimeUnit.SECONDS );

		LOG.info( "completed, t={}", scheduler.now() );
	}

}
