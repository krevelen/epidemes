package nl.rivm.cib.episim.time;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import io.coala.exception.ExceptionFactory;
import io.coala.log.LogUtil;
import io.coala.time.x.Instant;
import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * {@link Scheduler}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Scheduler extends Timed
{

	@Override
	default Scheduler scheduler()
	{
		return this;
	}

	/** @return an {@link Observable} stream of {@link Instant}s */
	Observable<Instant> time();

	/** */
	void resume();

	/**
	 * @param when the {@link Instant} of execution
	 * @param what the {@link Runnable}
	 * @return the occurrence {@link Expectation}, for optional cancellation
	 */
	Expectation schedule( Instant when, Runnable what );

	/**
	 * Schedule a stream of {@link Expectation}s for execution of {@code what}
	 * 
	 * @param when the {@link Observable} stream of {@link Instant}s
	 * @param what the {@link Runnable} to execute upon each {@link Instant}
	 * @return an {@link Observable} stream of {@link Expectation}s for each
	 *         next {@link Instant}, until completion of simulation time or
	 *         observed instants or an error occurs
	 */
	default <T> Observable<Expectation>
		schedule( final Observable<Instant> when, final Runnable what )
	{
		return schedule( when, new Observer<Instant>()
		{
			@Override
			public void onNext( final Instant t )
			{
				what.run();
			}

			@Override
			public void onError( final Throwable e )
			{
				// ignore errors, already passed to result Observable
			}

			@Override
			public void onCompleted()
			{
				// ignore complete, result Observable also completes
			}
		} );
	}

	/**
	 * Schedule a stream of values resulting from executing a {@link Callable}
	 * 
	 * @param when the {@link Observable} stream of {@link Instant}s
	 * @param what the {@link Callable} to execute upon each {@link Instant}
	 * @return an {@link Observable} stream of results, until completion of
	 *         simulation time or observed instants or an error occurs
	 */
	default <T> Observable<T> schedule( final Observable<Instant> when,
		final Callable<T> what )
	{
		final Subject<T, T> result = BehaviorSubject.create();
		schedule( when, new Observer<Instant>()
		{
			@Override
			public void onNext( final Instant t )
			{
				try
				{
					what.call();
				} catch( final RuntimeException e )
				{
					throw e;
				} catch( final Throwable e )
				{
					throw ExceptionFactory.createUnchecked( e,
							"Problem calling " + what );
				}
			}

			@Override
			public void onError( final Throwable e )
			{
				result.onError( e );
			}

			@Override
			public void onCompleted()
			{
				result.onCompleted();
			}
		} );
		return result.asObservable();
	}

	/**
	 * Schedule a stream of {@link Instant}s and their {@link Expectation}s
	 * 
	 * @param when the {@link Observable} stream of {@link Instant}s, to be
	 *            scheduled immediately
	 * @param what the {@link Observer} of the same {@link Instant}s but delayed
	 *            until they occur in simulation time
	 * @return an {@link Observable} stream of {@link Expectation}s, until
	 *         completion of simulation time or observed instants or an error
	 *         occursF
	 */
	default <T> Observable<Expectation>
		schedule( final Observable<Instant> when, final Observer<Instant> what )
	{
		final Subject<Expectation, Expectation> result = BehaviorSubject
				.create();

		time().subscribe( t ->
		{
			// ignore passage of time
		}, e ->
		{
			result.onError( e );
		}, () ->
		{
			result.onCompleted();
		} );
		when.first().subscribe( t ->
		{
			final Expectation exp = scheduler().schedule( t, () ->
			{
				try
				{
					what.onNext( t );
					// completed first() Instant, recurse remaining: skip(1)
					schedule( when.skip( 1 ), what ).subscribe( result );
				} catch( final Throwable e )
				{
					// failed first() Instant, interrupt recursion
					try
					{
						result.onError( e );
//						what.onError( e );
					} catch( final Throwable e1 )
					{
						LogUtil.getLogger( getClass() ).error(
								"Problem in error propagation/handling", e1 );
					}
					// FIXME kill sim
//					time().doOnNext( t1 ->
//					{
//						throw e instanceof RuntimeException
//								? (RuntimeException) e
//								: ExceptionFactory
//										.createUnchecked( "<kill sim>", e );
//					} );
				}
			} );
			result.onNext( exp );
		}, e ->
		{
			// recursion complete
			if( e instanceof NoSuchElementException )
			{
				// no elements remain
				result.onCompleted();
				what.onCompleted();
			} else
			{
				// problem observing Instants
				result.onError( e );
				what.onError( e );
			}
		} );
		return result.asObservable();
	}

}