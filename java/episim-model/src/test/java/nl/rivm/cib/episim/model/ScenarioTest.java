/* $Id: 4cd06794959ca7bd59432990ae8f934d02b9e26d $
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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.math3.Math3RandomNumberStream;
import io.coala.random.RandomDistribution;
import io.coala.random.RandomNumberStream;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Timed.Scheduler;
import nl.rivm.cib.episim.time.dsol3.Dsol3Scheduler;

/**
 * {@link ScenarioTest}
 * 
 * @version $Id: 4cd06794959ca7bd59432990ae8f934d02b9e26d $
 * @author Rick van Krevelen
 */
public class ScenarioTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( ScenarioTest.class );
	{
		UnitFormat.getInstance().alias( NonSI.DAY, "days" );
		//UnitFormat.getInstance().label(DAILY, "daily");
	}

	/**
	 * This test should:
	 * <ol>
	 * <li>init (contact, transport, care) location networks</li>
	 * <li>init (age, sex, opinion, birth/death) population networks</li>
	 * <li>init (travel, contact, risk) behaviors</li>
	 * <li>init disease conditions/compartments</li>
	 * <li>run opinion-immunization\\contact-transmission events</li>
	 * </ol>
	 * 
	 * @throws Throwable
	 */
	@Test
	public void scenarioTest() throws Throwable
	{
		LOG.trace( "Starting scenario..." );

		final Scheduler scheduler = new Dsol3Scheduler( "dsol3Test",
				Instant.of( "0 days" ), Instant.of( "100 days" ),
				( Scheduler s ) ->
				{
					LOG.trace( "initialized, t={}", s.now() );
				} );

		final Set<Individual> pop = new HashSet<>();
		final int n_pop = 10;
//		final Set<Location> homes = new HashSet<>();
//		final int n_homes = 6000000;
//		final Set<Location> offices = new HashSet<>();
//		final int n_offices = 3000000;
		final Infection measles = new Infection.Simple(
				Amount.valueOf( 1, Infection.DAILY ), Duration.of( "2 days" ),
				Duration.of( "5 days" ), Duration.of( "9999 days" ),
				Duration.of( "3 days" ), Duration.of( "7 days" ) );

		final TransmissionRoute route = TransmissionRoute.AIRBORNE;
		final Location rivm = new Location.Simple( scheduler,
				Location.RIVM_POSITION, Location.NO_ZIP, route );

		final Duration contactPeriod = Duration.of( "1 h" );
		final ContactIntensity[] contactTypes = { ContactIntensity.FAMILY };
		final Amount<Frequency> force = measles.getForceOfInfection( null, null,
				contactTypes );
		final double infectLikelihood = force.times( contactPeriod.toAmount() )
				.to( Unit.ONE ).getEstimatedValue();
		LOG.trace( "Infection likelihood: {} x {} x {} = {}", force,
				contactPeriod, Arrays.asList( contactTypes ),
				infectLikelihood );

		final RandomDistribution<Gender> genderDist = RandomDistribution.Util
				.asConstant( Gender.MALE );
		final CountDownLatch latch = new CountDownLatch( 1 );
		final RandomNumberStream rng = new Math3RandomNumberStream.MersenneFactory()
				.create( "MAIN", 1234L );
		for( int i = 1; i < n_pop; i++ )
		{
			final Individual ind = new Individual.Simple( scheduler,
					Instant.of( "0 h" ), genderDist.draw(), rivm, rivm,
					new Condition.Simple( scheduler, measles ) );
			pop.add( ind );
			if( rng.nextDouble() > infectLikelihood ) continue;
			final int nr = i;
			ind.getConditions().get( measles ).emitTransitions()
					.subscribe( ( TransitionEvent<?> t ) ->
					{
						LOG.trace( "Transition for {} at t={}: {}", nr,
								scheduler.now(), t );
					}, ( Throwable e ) ->
					{
						LOG.warn( "Problem in transition", e );
					}, () ->
					{
						latch.countDown();
					} );
			ind.getConditions().get( measles ).infect();
			LOG.trace( "INFECTED" );
		}
		scheduler.time().subscribe( ( Instant t ) ->
		{
			LOG.trace( "t = {}", t );
		}, ( Throwable e ) ->
		{
			LOG.warn( "Problem in scheduler", e );
		}, () ->
		{
			latch.countDown();
		} );
		scheduler.resume();
		latch.await( 3, TimeUnit.SECONDS );
		assertEquals( "Should have completed", 0, latch.getCount() );
	}

}
