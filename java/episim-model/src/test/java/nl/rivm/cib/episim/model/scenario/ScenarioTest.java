/* $Id: 42f0b51d22ab98c493fc5694961aa589b7cb0e9c $
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import io.coala.dsol3.Dsol3Scheduler;
import io.coala.log.LogUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.Units;
import nl.rivm.cib.episim.model.Condition;
import nl.rivm.cib.episim.model.ContactIntensity;
import nl.rivm.cib.episim.model.Gender;
import nl.rivm.cib.episim.model.Household;
import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.Infection;
import nl.rivm.cib.episim.model.Place;
import nl.rivm.cib.episim.model.Population;
import nl.rivm.cib.episim.model.TransmissionRoute;
import nl.rivm.cib.episim.model.TransmissionSpace;

/**
 * {@link ScenarioTest}
 * 
 * @version $Id: 42f0b51d22ab98c493fc5694961aa589b7cb0e9c $
 * @author Rick van Krevelen
 */
public class ScenarioTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( ScenarioTest.class );

	/**
	 * Traffic intensity
	 * (http://opendata.cbs.nl/dataportaal/portal.html?_catalog=CBS&_la=nl&
	 * tableId=81435ned&_theme=364) Traffic participants
	 * (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&
	 * tableId=81125ned&_theme=361) Mobility - vehicle posession
	 * (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&
	 * tableId=37856&_theme=837) Mobility - traveler characteristics
	 * (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&
	 * tableId=81128ned&_theme=494) Mobility - traffic characteristics
	 * (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&
	 * tableId=81127ned&_theme=494) Mobility - motives
	 * (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&
	 * tableId=81124ned&_theme=494)
	 */
	public void testTraffic()
	{
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

		Units.DAILY.toString(); // init new unit
		final Scheduler scheduler = Dsol3Scheduler.of( "scenarioTest",
				Instant.of( "0 days" ), Duration.of( "100 days" ), s ->
				{
					LOG.trace( "initialized, t={}",
							s.now().prettify( NonSI.DAY, 1 ) );
				} );

		//final Set<Individual> pop = new HashSet<>();
		final int n_pop = 10;
//		final Set<Location> homes = new HashSet<>();
//		final int n_homes = 6000000;
//		final Set<Location> offices = new HashSet<>();
//		final int n_offices = 3000000;
		final Infection measles = new Infection.Simple(
				Amount.valueOf( 1, Units.DAILY ), Duration.of( "2 days" ),
				Duration.of( "5 days" ), Duration.of( "9999 days" ),
				Duration.of( "3 days" ), Duration.of( "7 days" ) );

		final TransmissionRoute route = TransmissionRoute.AIRBORNE;
		final TransmissionSpace space = TransmissionSpace.of( scheduler,
				route );
		final Place rivm = Place.Simple.of( Place.RIVM_POSITION, Place.NO_ZIP,
				space );

		final Collection<ContactIntensity> contactTypes = Collections
				.singleton( ContactIntensity.FAMILY );
		final Amount<Frequency> force = measles.getForceOfInfection(
				rivm.getSpace().getTransmissionRoutes(), contactTypes );
		final Duration contactPeriod = Duration.of( "10 h" );
		final double infectLikelihood = force.times( contactPeriod.toAmount() )
				.to( Unit.ONE ).getEstimatedValue();
		LOG.trace( "Infection likelihood: {} * {} * {} = {}", force,
				contactPeriod, Arrays.asList( contactTypes ),
				infectLikelihood );

		final DistributionParser distParser = new DistributionParser(
				Math3ProbabilityDistribution.Factory
						.of( Math3PseudoRandom.Factory
								.of( MersenneTwister.class )
								.create( "MAIN", 1234L ) ) );
		final ProbabilityDistribution<Gender> genderDist = distParser
				.getFactory()
				.createUniformCategorical( Gender.MALE, Gender.FEMALE );
		/*
		 * FIXME RandomDistribution. Util .valueOf( "uniform(male;female)",
		 * distParser, Gender.class );
		 */
		final ProbabilityDistribution<Instant> birthDist = Instant
				.of( /* distFactory.getUniformInteger( rng, -5, 0 ) */
						distParser.parse( "uniform-discrete(-5;0)",
								Integer.class ),
						NonSI.DAY );
		final CountDownLatch latch = new CountDownLatch( 1 );
		final Population pop = Population.Simple.of( scheduler );
		for( int i = 1; i < n_pop; i++ )
		{
			final Gender gender = genderDist.draw();
			final Instant birth = birthDist.draw();
			LOG.trace( "#{} - gender: {}, birth: {}", i, gender,
					birth.prettify( NonSI.DAY, 1 ) );
			final Individual ind = Individual.Simple.of(
					Household.Simple.of( pop, rivm ), birth, gender,
					rivm.getSpace(), false );
			ind.with( Condition.Simple.of( ind, measles ) );
//			pop.add( ind );
			final int nr = i;
			ind.getConditions().get( measles ).emitTransitions()
					.subscribe( ( t ) ->
					{
						LOG.trace( "Transition for #{} at t={}: {}", nr,
								scheduler.now().prettify( NonSI.HOUR, 1 ), t );
					}, ( e ) ->
					{
						LOG.warn( "Problem in transition", e );
					}, () ->
					{
						latch.countDown();
					} );
			if( distParser.getFactory().getStream()
					.nextDouble() < infectLikelihood )
			{
				LOG.trace( "INFECTED #{}", i );
				ind.after( Duration.of( "30 min" ) )
						.call( ind.getConditions().get( measles )::infect );
			}
		}
		scheduler.time().subscribe( t ->
		{
			LOG.trace( "t = {}", t.prettify( NonSI.DAY, 1 ) );
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

		// CBS overledenen in huishouden per leeftijd: 83190ned
		// http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T
	}

}
