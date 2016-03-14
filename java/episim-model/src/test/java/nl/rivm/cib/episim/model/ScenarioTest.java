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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.SI;

import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.random.RandomDistribution;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Timed.Scheduler;
import nl.rivm.cib.episim.time.dsol3.Dsol3Scheduler;

import static org.aeonbits.owner.util.Collections.map;
import static org.aeonbits.owner.util.Collections.entry;

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
	@SuppressWarnings( "unchecked" )
	public void scenarioTest() throws Throwable
	{
		LOG.trace( "Starting scenario..." );

		final Scheduler scheduler = new Dsol3Scheduler( "dsol3Test",
				Instant.of( "5 s" ), Instant.of( "100 s" ), ( Scheduler s ) ->
				{
					LOG.trace( "initialized, t={}", s.now() );
				} );

		final Set<Location> homes = new HashSet<>();
		final Set<Individual> pop = new HashSet<>();
		final Set<Location> offices = new HashSet<>();

		final int n_pop = 17000000;
		final int n_homes = 6000000;
		final int n_offices = 3000000;
		final RandomDistribution<Gender> genderDist = RandomDistribution.Util
				.asConstant( Gender.MALE );
		final Location rivm = new Location.Simple( scheduler,
				Location.RIVM_POSITION, Location.NO_ZIP,
				TransmissionRoute.AIRBORNE );
		final Infection measles = new Infection.Simple(
				Amount.valueOf( 0L, SI.HERTZ ) );
		final Map<Duration, EpidemicCompartment> compartment = map( entry(
				Duration.ZERO, EpidemicCompartment.Simple.PASSIVE_IMMUNE ) );
		final Map<Duration, SymptomPhase> symptoms = map(
				entry( Duration.ZERO, SymptomPhase.ASYMPTOMATIC ) );
		final Map<Duration, TreatmentStage> treatment = map(
				entry( Duration.ZERO, TreatmentStage.UNTREATED ) );
		final Map<Duration, Boolean> seroconversion = map(
				entry( Duration.ZERO, Boolean.FALSE ) );
		final Condition healthy = new Condition.Simple( scheduler, measles,
				compartment, symptoms, treatment, seroconversion );
		for( int i = 1; i < n_pop; i++ )
		{
			final Individual ind = new Individual.Simple( scheduler,
					Instant.of( "2013-01-01" ), genderDist.draw(), rivm, rivm,
					healthy );
			pop.add( ind );
		}
	}

}
