package nl.rivm.cib.episim.time.dsol3;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;

import io.coala.exception.ExceptionFactory;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import io.coala.time.x.Timing;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;
import rx.Observable;

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
	public void testCron()
	{
		final String pattern = "0 0 0 14 * ? 2016";
		final Date offset = new Date();
		final CronTrigger trigger = TriggerBuilder.newTrigger()
				.withSchedule( CronScheduleBuilder.cronSchedule( pattern ) )
				.build();
		final Iterator<Instant> it = new Iterator<Instant>()
		{
			private Date current = trigger.getFireTimeAfter( offset );

			@Override
			public boolean hasNext()
			{
				return this.current != null;
			}

			@Override
			public Instant next()
			{
				final Instant next = //this.current == null ? null :
						Instant.of( this.current.getTime() - offset.getTime(),
								SI.MILLI( SI.SECOND ) );
				this.current = trigger.getFireTimeAfter( this.current );
				return next;
			}
		};
//		while( it.hasNext() )
//			LOG.trace( "it: {}", it.next() );
		final Observable<Instant> obs = Observable.from( new Iterable<Instant>()
		{
			@Override
			public Iterator<Instant> iterator()
			{
				return it;
			}
		} );
		obs.subscribe( t ->
		{
			LOG.trace( "obs t: {}", t.prettify( NonSI.HOUR, 2 ) );
		}, e ->
		{
			LOG.trace( "obs t", e );
		}, () ->
		{
			LOG.trace( "obs t: done" );
		} );
	}

	@Test
	public void testScheduler() throws InterruptedException
	{
		final Instant h5 = Instant.of( "5 h" );
		LOG.trace( "start t={}", h5 );
		final Scheduler sched = Dsol3Scheduler.of( "dsol3Test", h5,
				Duration.of( "500 day" ), s ->
				{
					// initialize the model
					s.at( h5.add( 1 ) ).call( this::logTime, s );
					s.at( h5.add( 2 ) ).call( this::logTime, s );
					s.at( h5.add( 2 ) ).call( this::logTime, s );

					s.schedule( Timing.valueOf( "0 0 0 14 * ? *" )// + DateTime.now().getYear() ) // 0 30 9,12,15 * * ?
							.asObservable( new Date() ), () ->
							{
								LOG.trace( "atEach handled, t={}",
										s.now().prettify( NonSI.DAY, 2 ) );
								throw new RuntimeException();
							} ).subscribe( exp ->
							{
								LOG.trace( "atEach next: {}", exp );
							}, e ->
							{
								LOG.trace( "atEach failed, t={}",
										s.now().prettify( NonSI.DAY, 2 ), e );
								throw ExceptionFactory
										.createUnchecked( "<kill app>", e );
							}, () ->
							{
								LOG.trace( "atEach done, t={}",
										s.now().prettify( NonSI.DAY, 2 ) );
							} );

					LOG.trace( "initialized, t={}", s.now() );
				} );

		final CountDownLatch latch = new CountDownLatch( 1 );
		sched.time().subscribe( t ->
		{
			LOG.trace( "t={}", t.prettify( NonSI.DAY, 2 ) );
		}, e ->
		{
			LOG.trace( "problem, t=" + sched.now().prettify( NonSI.DAY, 2 ),
					e );
			latch.countDown();
		}, () ->
		{
			LOG.trace( "completed, t={}", sched.now() );
			latch.countDown();
		} );
		sched.resume();

		latch.await( 1, TimeUnit.SECONDS );
		assertEquals( "Scheduler not completed in time", 0, latch.getCount() );
	}

}
