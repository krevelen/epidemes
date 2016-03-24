package nl.rivm.cib.episim.time.dsol3;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link Dsol3SchedulerTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Dsol3SchedulerTest
{

	/** */
	private static final Logger LOG = LogManager
			.getLogger( Dsol3SchedulerTest.class );

	private void logTime( final Timed t )
	{
		LOG.trace( "logging, t={}", t.now() );
	}

	@Test
	public void testScheduler() throws InterruptedException
	{
		final Instant h5 = Instant.valueOf( "5 h" );
		LOG.trace( "start t={}", h5 );
		final Scheduler sched = new Dsol3Scheduler( "dsol3Test", h5,
				h5.add( 5 ), ( Scheduler s ) ->
				{
					s.at( h5.add( 1 ) ).call( this::logTime, s );
					s.at( h5.add( 2 ) ).call( this::logTime, s );
					s.at( h5.add( 3 ) ).call( this::logTime, s );
					s.at( h5.add( 4 ) ).call( this::logTime, s );
					LOG.trace( "initialized, t={}", s.now() );
				} );

		final CountDownLatch latch = new CountDownLatch( 1 );
		sched.time().subscribe( ( Instant t ) ->
		{
			LOG.trace( "new time, t={}", t );
		}, ( Throwable e ) ->
		{
			LOG.trace( "problem, t=" + sched.now(), e );
//			fail( e.getMessage() );
		}, () ->
		{
			LOG.trace( "completed, t={}", sched.now() );
			latch.countDown();
		} );
		sched.resume();

		latch.await( 1, TimeUnit.SECONDS );
		assertEquals( "Scheduler never completed", 0, latch.getCount() );
	}

}
