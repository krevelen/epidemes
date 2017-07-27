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
package nl.rivm.cib.epidemes.pilot;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.pilot.IllnessTrajectory;

/**
 * {@link IllnessTrajectoryTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class IllnessTrajectoryTest
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( IllnessTrajectoryTest.class );

	static class TestScenario implements Scenario
	{
		@Inject
		private Scheduler scheduler;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}
	}

	@Test
	public void testPathogen() throws Exception
	{

		final LocalConfig config = new LocalConfig.JsonBuilder().withId( "v1" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class )
				.withProvider( PseudoRandom.Factory.class,
						Math3PseudoRandom.MersenneTwisterFactory.class )
				.withProvider( ProbabilityDistribution.Factory.class,
						Math3ProbabilityDistribution.Factory.class )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )
				.build();
		LOG.info( "start {} with binder config: {}", getClass().getSimpleName(),
				config );

		final LocalBinder binder = config.createBinder();
		final Scheduler scheduler = binder.inject( Scheduler.class );
		final Subject<Long> pressure = PublishSubject.create();
		final Observable<Instant> T_p = Observable
				.fromArray( 1, 2, 3, 4, 5, 6, 7 )
				.map( t -> Instant.of( t, TimeUnits.WEEK ) );

		scheduler.onReset( s ->
		{
			final IllnessTrajectory.Factory pathogens = binder
					.inject( IllnessTrajectory.Factory.SimpleBinding.class );
			final IllnessTrajectory pathogen = pathogens
					.create( JsonUtil.getJOM().createObjectNode()
							.put( "incubate-period", "const(.5 week)" )
//					.put( "wane-period", "const(.5 year)" )
			);
			LOG.trace( "t={}, init pathogen: {}", s.now(), pathogen );

			// have pathogen subscribe to an individual's infection pressure
			pathogen.linear( pressure ).subscribe( c -> LOG
					.trace( "t={}, linear trajectory @ {}", s.now(), c ),
					Exceptions::propagate );

			// add infectious scenario
			final AtomicLong p = new AtomicLong();
			s.atEach( T_p, t -> pressure.onNext( p.incrementAndGet() ) );

			LOG.trace( "t={}, initialized", s.now() );
		} );

		pressure.subscribe( p ->
		{

		} );

		scheduler.run();

		LOG.info( "completed {}", getClass().getSimpleName() );
	}
}
