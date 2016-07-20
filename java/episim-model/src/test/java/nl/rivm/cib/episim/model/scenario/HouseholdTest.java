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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.guice4.Guice4LocalBinder;
import io.coala.log.LogUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.Units;

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
	public void householdCompositionTest() throws Throwable
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
		final Scheduler scheduler = Dsol3Scheduler.of( "householdTest",
				Instant.of( "0 days" ), Duration.of( "100 days" ),
				binder.inject( Scenario.class )::init );

		LOG.trace( "Starting household composition scenario..." );
		final CountDownLatch latch = new CountDownLatch( 1 );
		scheduler.time().subscribe( t ->
		{
			LOG.trace( "t = {}", t.prettify( 1 ) );
		}, e ->
		{
			LOG.warn( "Problem in scheduler", e );
		}, () ->
		{
			latch.countDown();
		} );
		scheduler.resume();
		latch.await( 20, TimeUnit.SECONDS );
		assertEquals( "Should have completed", 0, latch.getCount() );
	}

}
