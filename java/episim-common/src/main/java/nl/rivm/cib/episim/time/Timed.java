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

import java.util.concurrent.Callable;

import javax.measure.quantity.Dimensionless;

//import javax.measure.quantity.Dimensionless;
//import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import io.coala.exception.ExceptionFactory;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import io.coala.time.x.TimeSpan;
import io.coala.util.Comparison;
import nl.rivm.cib.episim.util.Caller;
import nl.rivm.cib.episim.util.Caller.ThrowingBiConsumer;
import nl.rivm.cib.episim.util.Caller.ThrowingBiFunction;
import nl.rivm.cib.episim.util.Caller.ThrowingConsumer;
import nl.rivm.cib.episim.util.Caller.ThrowingFunction;
import rx.Observable;
import rx.Observer;

/**
 * {@link Timed}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Timed
{

	/** @return the {@link Scheduler} of this {@link Timed} object */
	Scheduler scheduler();

	/** @return the current {@link Instant} */
	default Instant now()
	{
		return scheduler().now();
	}

	/**
	 * @param delay the {@link Amount} of delay, in ({@link Duration} or
	 *            {@link Dimensionless} units
	 * @return the {@link FutureSelf}
	 */
	default FutureSelf after( final Duration delay )
	{
		return FutureSelf.of( this, now().add( delay ) );
	}

	/**
	 * @param delay the {@link Amount} of delay, in ({@link Duration} or
	 *            {@link Dimensionless} units
	 * @return the {@link FutureSelf}
	 */
	default FutureSelf after( final TimeSpan delay )
	{
		return FutureSelf.of( this, now().add( delay ) );
	}

	/**
	 * @param delay the future {@link Instant}
	 * @return the {@link FutureSelf}
	 */
	default FutureSelf at( final Instant when )
	{
		return FutureSelf.of( this, when );
	}

	default Observable<FutureSelf> atEach( final Observable<Instant> when )
	{
		final Timed self = this;
		return Observable.create( sub ->
		{
			scheduler().schedule( when, new Observer<Instant>()
			{
				@Override
				public void onCompleted()
				{
					sub.onCompleted();
				}

				@Override
				public void onError( final Throwable e )
				{
					sub.onError( e );
				}

				@Override
				public void onNext( final Instant t )
				{
					sub.onNext( FutureSelf.of( self, t ) );
				}
			} );
		} );
	}

	/**
	 * {@link FutureSelf} is a decorator of a {@link Timed} object that is
	 * itself {@link Timed} but with its {@link #now()} at a fixed (future)
	 * {@link Instant} and additional scheduling helper methods
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	interface FutureSelf extends Timed
	{
		Timed self();

		@Override
		default Scheduler scheduler()
		{
			return self().scheduler();
		}

		/**
		 * @param runner the {@link Runnable} (method) to call when time comes
		 * @return the {@link Expectation} for potential cancellation
		 */
		default Expectation call( final Runnable runner )
		{
			return scheduler().schedule( now(), runner );
		}

		/**
		 * @param call the {@link Callable} (method) to call when time comes
		 * @return the {@link Expectation} for potential cancellation
		 */
		default <R> Observable<R> call( final Callable<R> call )
		{
			return scheduler().schedule( Observable.just( now() ), call );
		}

		/**
		 * @param call the {@link Callable} (method) to call when time comes
		 * @param t arg0
		 * @return the {@link Expectation} for potential cancellation
		 */
		default <T, E extends Exception> Expectation
			call( final ThrowingConsumer<T, E> call, final T t )
		{
			return call( Caller.of( call, t )::run );
		}

		/**
		 * @param call the {@link Callable} (method) to call when time comes
		 * @param t arg0
		 * @param u arg1
		 * @return the {@link Expectation} for potential cancellation
		 */
		default <T, U, E extends Exception> Expectation
			call( final ThrowingBiConsumer<T, U, E> call, final T t, final U u )
		{
			return call( Caller.of( call, t, u )::run );
		}

		/**
		 * @param call the {@link Callable} (method) to call when time comes
		 * @param t arg0
		 * @return the {@link Expectation} for potential cancellation
		 */
		// FIXME make result R observable?
		default <T, R, E extends Exception> Expectation
			call( final ThrowingFunction<T, R, E> call, final T t )
		{
			return call( Caller.of( call, t )::run );
		}

		/**
		 * @param call the {@link Callable} (method) to call when time comes
		 * @param t arg0
		 * @param u arg1
		 * @return the {@link Expectation} for potential cancellation
		 */
		// FIXME make result R observable?
		default <T, U, R, E extends Exception> Expectation call(
			final ThrowingBiFunction<T, U, R, E> call, final T t, final U u )
		{
			return call( Caller.of( call, t, u )::run );
		}

		/**
		 * {@link FutureSelf} factory method
		 * 
		 * @param self the {@link Timed} to project forward
		 * @param when the {@link Instant} to project onto
		 * @return a {@link FutureSelf} wrapper of the {@link Timed} self at
		 *         specified {@link Instant}
		 */
		static FutureSelf of( final Timed self, final Instant when )
		{
			if( Comparison.is( when ).lt( self.now() ) ) throw ExceptionFactory
					.createUnchecked( "Can't schedule in past: {} < now({})",
							when, self.now() );
			return new FutureSelf()
			{
				@Override
				public Timed self()
				{
					return self;
				}

				@Override
				public Instant now()
				{
					return when;
				}
			};
		}
	}
}
