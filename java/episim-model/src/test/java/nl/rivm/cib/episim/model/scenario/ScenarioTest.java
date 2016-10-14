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

import java.util.Comparator;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.enterprise.Fact;
import io.coala.log.LogUtil;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.time.Duration;

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

	interface Step<THIS extends Step<?>> extends Identified<Step.ID>
	{
		Duration duration(); // e.g. drawn from random distribution

		THIS next(); // e.g. drawn from random distribution

		class ID extends Id<String>
		{

		}
	}

	interface Activity extends Step<Activity>
	{
	}

	interface Meeting extends Activity
	{
		String destination();

		String deadline();

		String purpose();

		String relations(); // e.g. decided by broker

		String persons(); // e.g. decided by broker
	}

	interface Transit extends Activity
	{
		String destination();

		String modality(); // e.g. decided by broker

		String vehicle(); // e.g. decided by broker
	}

	interface Condition extends Step<Condition>
	{
		String symptoms();

		String stage( String diseaseId );

		String compartment( String diseaseId ); // SIR
	}

	interface Broker<T extends Comparable<? super T>>
	{
		void register( T registrant );

		void unregister( T registrant );

		Iterable<T> rank( Comparator<T> comparator ); // prioritize by some distance metric

		default T first( Comparator<T> comparator )
		{
			for( T t : rank( comparator ) )
				return t;
			return null;
		}

		/**
		 * Apply the objects' {@link T#compareTo(Object) natural ordering}
		 * @param ascend {@code true} to order ascending, {@code false} for descending
		 * @return an {@link Iterable} object, e.g. a sorted list or db query
		 */
		Iterable<T> rank( boolean ascend );

		default T smallest()
		{
			for( T t : rank( true ) )
				return t;
			return null;
		}

		default T largest()
		{
			for( T t : rank( false ) )
				return t;
			return null;
		}
	}

	interface VaccineAttitude extends Step<VaccineAttitude>
	{
		interface Persuasion extends Fact // e.g. media, peer, experience
		{

		}

		void absorb( String mediaEvent );

		void consume( String opinionEvent );

		void handle( String experienceEvent );

		Boolean isConfident( String vaccinationId ); // e.g. based on "life experience"

		Boolean isComplacent( String vaccinationId ); // e.g. based on "life experience"

		Boolean isConvenient( String vaccinationId ); // e.g. based on "life experience"

		Boolean isCalculated( String vaccinationId ); // e.g. based on "life experience"
	}

	interface Person extends Identified<Person.ID>
	{
		String birth();

		String gender();

		String lifephase(); // automaton based on age

		Condition condition(); // default fall-back?

		VaccineAttitude attitude(); // default fall-back?

		Activity activity(); // belongs to a (common) routine pattern instance

		String location(); // should be consistent with activity site/vehicle

		class ID extends Id<String>
		{

		}
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

		// person: { activity[stepId], disease[ condition[stepId], attitude[stepId] ] }

		// calculate vaccination degree = #vacc / #non-vacc, given:
		//   disease/vaccine v 
		//   region(s) r
		//   cohort birth range [t1,t2]

//
//		Units.DAILY.toString(); // init new unit
//		final Scheduler scheduler = Dsol3Scheduler.of( "scenarioTest",
//				Instant.of( "0 days" ), Duration.of( "100 days" ), s ->
//				{
//					LOG.trace( "initialized, t={}",
//							s.now().prettify( NonSI.DAY, 1 ) );
//				} );
//
//		//final Set<Individual> pop = new HashSet<>();
//		final int n_pop = 10;
////		final Set<Location> homes = new HashSet<>();
////		final int n_homes = 6000000;
////		final Set<Location> offices = new HashSet<>();
////		final int n_offices = 3000000;
//		final Infection measles = new Infection.Simple(
//				Amount.valueOf( 1, Units.DAILY ), Duration.of( "2 days" ),
//				Duration.of( "5 days" ), Duration.of( "9999 days" ),
//				Duration.of( "3 days" ), Duration.of( "7 days" ) );
//
//		final TransmissionRoute route = TransmissionRoute.AIRBORNE;
//		final TransmissionSpace space = TransmissionSpace.of( scheduler,
//				route );
//		final Place rivm = Place.Simple.of( Place.RIVM_POSITION, Place.NO_ZIP,
//				space );
//
//		final Collection<ContactIntensity> contactTypes = Collections
//				.singleton( ContactIntensity.FAMILY );
//		final Amount<Frequency> force = measles.getForceOfInfection(
//				rivm.getTransmissionSpace().getTransmissionRoutes(), contactTypes );
//		final Duration contactPeriod = Duration.of( "10 h" );
//		final double infectLikelihood = force.times( contactPeriod.toAmount() )
//				.to( Unit.ONE ).getEstimatedValue();
//		LOG.trace( "Infection likelihood: {} * {} * {} = {}", force,
//				contactPeriod, Arrays.asList( contactTypes ),
//				infectLikelihood );
//
//		final DistributionParser distParser = new DistributionParser(
//				Math3ProbabilityDistribution.Factory
//						.of( Math3PseudoRandom.Factory
//								.of( MersenneTwister.class )
//								.create( "MAIN", 1234L ) ) );
//		final ProbabilityDistribution<Gender> genderDist = distParser
//				.getFactory()
//				.createUniformCategorical( Gender.MALE, Gender.FEMALE );
//		/*
//		 * FIXME RandomDistribution. Util .valueOf( "uniform(male;female)",
//		 * distParser, Gender.class );
//		 */
//		final ProbabilityDistribution<Instant> birthDist = Instant
//				.of( /* distFactory.getUniformInteger( rng, -5, 0 ) */
//						distParser.parse( "uniform-discrete(-5;0)",
//								Integer.class ),
//						NonSI.DAY );
//		final CountDownLatch latch = new CountDownLatch( 1 );
//		final Population pop = Population.Simple.of( scheduler );
//		for( int i = 1; i < n_pop; i++ )
//		{
//			final Gender gender = genderDist.draw();
//			final Instant birth = birthDist.draw();
//			LOG.trace( "#{} - gender: {}, birth: {}", i, gender,
//					birth.prettify( NonSI.DAY, 1 ) );
//			final Individual ind = Individual.Simple.of(
//					Household.Simple.of( pop, rivm ), birth, gender,
//					rivm.getTransmissionSpace(), false );
//			ind.with( Condition.Simple.of( ind, measles ) );
////			pop.add( ind );
//			final int nr = i;
//			ind.afflictions().get( measles ).emitTransitions()
//					.subscribe( ( t ) ->
//					{
//						LOG.trace( "Transition for #{} at t={}: {}", nr,
//								scheduler.now().prettify( Units.HOURS, 1 ), t );
//					}, ( e ) ->
//					{
//						LOG.warn( "Problem in transition", e );
//					}, () ->
//					{
//						latch.countDown();
//					} );
//			if( distParser.getFactory().getStream()
//					.nextDouble() < infectLikelihood )
//			{
//				LOG.trace( "INFECTED #{}", i );
//				ind.after( Duration.of( "30 min" ) )
//						.call( ind.afflictions().get( measles )::infect );
//			}
//		}
//		scheduler.time().subscribe( t ->
//		{
//			LOG.trace( "t = {}", t.prettify( NonSI.DAY, 1 ) );
//		}, e -> LOG.warn( "Problem in scheduler", e ), latch::countDown );
//		scheduler.resume();
//		latch.await( 3, TimeUnit.SECONDS );
//		assertEquals( "Should have completed", 0, latch.getCount() );
//
	}

}
