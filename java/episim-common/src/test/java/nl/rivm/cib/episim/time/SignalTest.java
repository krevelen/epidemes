package nl.rivm.cib.episim.time;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.dsol3.Dsol3Scheduler;

public class SignalTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( SignalTest.class );

	@Test
	public void test() throws InterruptedException
	{
		final Scheduler scheduler = Dsol3Scheduler.of( "dsol3",
				Instant.of( "0 day" ), Duration.of( "100 day" ), () ->
				{
					LOG.trace( "scheduler initialized" );
				} );

		final BigDecimal decimal = BigDecimal.valueOf( 1.2 );
		final Signal<BigDecimal> signal = Signal.Simple.of( scheduler,
				decimal );

		final CountDownLatch latch = new CountDownLatch( 1 );
		signal.scheduler().time().subscribe( ( Instant t ) ->
		{
			LOG.trace( "new time, t={}", t );
		}, ( Throwable e ) ->
		{
			LOG.trace( new ParameterizedMessage( "problem, t={}",
					scheduler.now() ), e );
		}, () ->
		{
			LOG.trace( "completed, t={}", scheduler.now() );
			latch.countDown();
		} );
		signal.scheduler().resume();

		latch.await( 1, TimeUnit.SECONDS );
		assertEquals( "Scheduler not completed in time", 0, latch.getCount() );
	}

}
