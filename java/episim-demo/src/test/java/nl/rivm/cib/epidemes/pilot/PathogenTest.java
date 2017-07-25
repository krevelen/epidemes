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
import nl.rivm.cib.pilot.Pathogen;

/**
 * {@link PathogenTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PathogenTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( PathogenTest.class );

	static class TestScenario implements Scenario
	{
		@Inject
		private Scheduler scheduler;

		@Inject
		private Pathogen.Factory pathogens;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		private Subject<Long> pressure = PublishSubject.create();

		@Override
		public void init() throws Exception
		{
			final Pathogen pathogen = this.pathogens.create( JsonUtil.getJOM()
					.createObjectNode().put( "incubate-period", "const(.5 week)" )
//					.put( "wane-period", "const(.5 year)" )
					);
			LOG.trace( "Init pathogen: {}", pathogen );

			pathogen.linearTrajectory( this.pressure )
					.subscribe(
							c -> LOG.trace( "t={}, compartment now: {}",
//									QuantityUtil.toScale(
//											now().toQuantity( TimeUnits.DAYS ),
//											2 ),
									now(),
									c ),
							Exceptions::propagate );

			// add infectious scenario
			final AtomicLong infectious = new AtomicLong();
			atEach( Observable.fromArray( 1, 2, 3, 4, 5, 6, 7 )
					.map( t -> Instant.of( t, TimeUnits.WEEK ) ),
					t -> this.pressure.onNext( infectious.incrementAndGet() ) );

			LOG.trace( "t={}, initialized", now() );
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
				.withProvider( Pathogen.Factory.class,
						Pathogen.Factory.SimpleBinding.class )
				.build();
		LOG.info( "start {} with binder config: {}", getClass().getSimpleName(),
				config );

		config.createBinder().inject( TestScenario.class ).run();

		LOG.info( "completed {}", getClass().getSimpleName() );
	}
}
