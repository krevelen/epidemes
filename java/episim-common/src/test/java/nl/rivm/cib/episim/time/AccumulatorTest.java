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
package nl.rivm.cib.episim.time;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.DataAmount;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import io.coala.time.x.TimeSpan;
import nl.rivm.cib.episim.time.Accumulator;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Accumulator.Integrator;
import nl.rivm.cib.episim.time.dsol3.Dsol3Scheduler;
import rx.Observer;

/**
 * {@link AccumulatorTest} tests the {@link Accumulator}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class AccumulatorTest
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( AccumulatorTest.class );

	private void logPoint( final Accumulator<?> acc, final TimeSpan interval )
	{
		LOG.trace( "{},{}", acc.now(),
				BigDecimal.valueOf( acc.getAmount().getEstimatedValue() )
						.setScale( 4, RoundingMode.HALF_UP ) );
		acc.after( interval ).call( this::logPoint, acc, interval );
	}

	/**
	 * Test method for {@link Accumulator#setIntegrator(Accumulator.Integrator)}
	 * , {@link Accumulator#at(Amount, Observer)},
	 * {@link Accumulator#emitAmounts()} and {@link Accumulator#getAmount()}.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void test() throws InterruptedException
	{

		final Unit<?> bps = SI.BIT.divide( SI.SECOND );
		final Scheduler scheduler = Dsol3Scheduler.of( "dsol3Test",
				Instant.of( "5 s" ), Duration.of( "100 s" ), s ->
				{
					LOG.trace( "initialized, t={}", s.now() );
				} );
		final Accumulator<DataAmount> acc = Accumulator.of( scheduler,
				Amount.valueOf( 120.4, SI.BIT ), Amount.valueOf( 2, bps ) );

		final TimeSpan delay = TimeSpan.valueOf( "1 s" );
		scheduler.after( delay ).call( this::logPoint, acc, delay );

		// schedule event at target level
		final Amount<DataAmount> target = Amount.valueOf( 40, SI.BIT );
		acc.at( target, t ->
		{
			LOG.trace( "reached a={} at t={}", target, t );
		} );

		// double the rate
		acc.setIntegrator( Integrator.ofRate( Amount.valueOf( 4, bps ) ) );
		assertThat( "Can't be null", acc, not( nullValue() ) );

		final CountDownLatch latch = new CountDownLatch( 1 );
		scheduler.time().subscribe( t ->
		{
			LOG.trace( "new time, t={}", t );
		}, e ->
		{
			LOG.trace( "problem, t=" + scheduler.now(), e );
		}, () ->
		{
			LOG.trace( "completed, t={}", scheduler.now() );
			latch.countDown();
		} );
		scheduler.resume();

		latch.await( 1, TimeUnit.SECONDS );
		assertEquals( "Scheduler never completed", 0, latch.getCount() );

		LOG.trace( "Got total: " + acc );
	}

}
