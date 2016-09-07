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

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.guice4.Guice4LocalBinder;
import io.coala.log.LogUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scheduler;
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

		// configure replication 
		ConfigCache.getOrCreate( ReplicateConfig.class, Collections
				.singletonMap( ReplicateConfig.DURATION_KEY, "" + 100 ) );

		// configure tooling
		final LocalConfig config = LocalConfig.builder().withId( "geardSim" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class )
				.withProvider( PseudoRandom.Factory.class,
						Math3PseudoRandom.MersenneTwisterFactory.class )
				.withProvider( ProbabilityDistribution.Factory.class,
						Math3ProbabilityDistribution.Factory.class )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )
				.build();

		LOG.info( "Starting household test, config: {}", config.toYAML() );
		final Scheduler scheduler = Guice4LocalBinder.of( config )
				.inject( Geard2011Scenario.class ).scheduler();

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
