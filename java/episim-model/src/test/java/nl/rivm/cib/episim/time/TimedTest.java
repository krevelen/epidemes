/* $Id: dfc79e6adc947b0e970d847b546cafdde5f6dd26 $
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
package nl.rivm.cib.episim.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.time.x.Instant;
import io.coala.time.x.TimeSpan;
import nl.rivm.cib.episim.time.Timed.Scheduler;
import nl.rivm.cib.episim.time.dsol3.Dsol3Scheduler;

/**
 * {@link TimedTest}
 * 
 * @version $Id: dfc79e6adc947b0e970d847b546cafdde5f6dd26 $
 * @author Rick van Krevelen
 */
public class TimedTest
{

	/** */
	static final Logger LOG = LogManager.getLogger( TimedTest.class );

	@Test
	public void test() throws InterruptedException
	{
		final Scheduler sched = new Dsol3Scheduler( "dsol3Test",
				Instant.of( 5 ), Instant.of( 10 ), ( Timed.Scheduler s ) ->
				{
					LOG.trace( "initialized, t={}", s.now() );
					
				} );
		final Timed model = new Timed()
		{
			@Override
			public Scheduler scheduler()
			{
				return sched;
			}
		};

		LOG.trace( "testing t=0: " + Instant.ZERO );
		assertThat( "default start time is zero", model.now(),
				equalTo( Instant.ZERO ) );

		LOG.trace( "testing t+1 == " + Instant.ONE );
		assertThat( "FutureSelf#after(t) time is added to Timed#now()",
				model.after( TimeSpan.ONE ).now(), equalTo( Instant.ONE ) );

		LOG.trace( "testing t+3 != " + Instant.of( 2 ) );
		assertThat( "FutureSelf#after(t) time is added to Timed#now()",
				model.after( TimeSpan.of( 3 ) ).now(),
				not( equalTo( Instant.of( 2 ) ) ) );
/*
 * final CountDownLatch latch = new CountDownLatch( 1 ); model.after(
 * TimeSpan.of( 3 ) ).call( new Runnable() {
 * 
 * @Override public void run() { latch.countDown(); } } );
 * model.scheduler().resume(); latch.await( 1, TimeUnit.SECONDS ); assertThat(
 * "testing Callable executed and counted down in <1000ms", latch.getCount(),
 * equalTo( 0 ) );
 */ }

}
