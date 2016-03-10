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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import io.coala.exception.x.ExceptionBuilder;
import io.coala.json.x.Wrapper;
import io.coala.time.x.Instant;
import io.coala.time.x.TimeSpan;
import rx.Observable;
import rx.Subscription;

/**
 * {@link Timed}
 * 
 * {@link List#stream()}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Timed
{

	/**
	 * @return the {@link Scheduler} of this {@link Timed} object
	 */
	//default 
	Scheduler scheduler();
//	{
//		return SCHEDULER_INSTANCE;
//	}

	/** @return the current {@link Instant} */
	default Instant now()
	{
		return scheduler().now();
	}

	/**
	 * @param delay the future {@link Instant}
	 * @return the {@link FutureSelf}
	 */
	default FutureSelf at( final Instant when )
	{
		return FutureSelf.of( this, when );
	}

//	default FutureSelf at( final Amount<?> when )
//	{
//		return at( Instant.of( when ) );
//	}
//
//	default FutureSelf at( final Measure<?, ?> when )
//	{
//		return at( Instant.of( when ) );
//	}
//
//	default FutureSelf at( final ReadableInstant when )
//	{
//		return at( Instant.of( when ) );
//	}
//
//	default FutureSelf at( final Number when )
//	{
//		return at( Instant.of( when ) );
//	}

	default Observable<FutureSelf> atEach( final Observable<Instant> stream )
	{
		return stream.map( ( Instant t ) ->
		{
			return FutureSelf.of( this, t );
		} );
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

	interface Scheduler extends Timed
	{

		//Instant now();

		@Override
		default Scheduler scheduler()
		{
			return this;
		}

		Observable<Instant> time();

		/** */
		void resume();

		/**
		 * @param when the {@link Instant} of execution
		 * @param call the {@link Callable}
		 * @return the occurrence {@link Expectation}, for optional cancellation
		 */
		Expectation schedule( Instant when, Callable<?> call );

	}

	/**
	 * {@link Expectation} or anticipation confirms that an event is scheduled
	 * to occur at some future {@link Instant}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Expectation extends Wrapper.SimpleOrdinal<Instant>
	{
		Subscription subscription;

		public Expectation()
		{

		}

		public Expectation( final Instant when,
			final Subscription subscription )
		{
			wrap( when );
			this.subscription = subscription;
		}

		/** cancels the scheduled event */
		public void remove()
		{
			this.subscription.unsubscribe();
		}

		/** @return {@code true} iff the event was cancelled or has occurred */
		public boolean isRemoved()
		{
			return this.subscription.isUnsubscribed();
		}

		public static Expectation of( final Instant when,
			final Subscription subscription )
		{
			return new Expectation( when, subscription );
		}
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

		default <R> Expectation call( final Callable<R> call )
		{
			return self().scheduler().schedule( now(), call );
		}

		default Expectation call( final Runnable runnable )
		{
			return call( new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					runnable.run();
					return null;
				}
			} );
		}

		default Expectation call( final Method method, final Object obj,
			final Object... args )
		{
			return call( new Callable<Object>()
			{
				@Override
				public Object call() throws Exception
				{
					method.setAccessible( true );
					return method.invoke( obj, args );
				}
			} );
		}

		default <T> Expectation call( final Constructor<T> constructor,
			final Object... initargs )
		{
			return call( new Callable<T>()
			{
				@Override
				public T call() throws Exception
				{
					return constructor.newInstance( initargs );
				}
			} );
		}

		default <R> Expectation call( final Supplier<R> s )
		{
			return call( new Callable<R>()
			{
				@Override
				public R call() throws Exception
				{
					return s.get();
				}
			} );
		}

		default <T> Expectation call( final Consumer<T> c, final T t )
		{
			return call( new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					c.accept( t );
					return null;
				}
			} );
		}

		default <T, R> Expectation call( final Function<T, R> f, final T t )
		{
			return call( new Callable<R>()
			{
				@Override
				public R call() throws Exception
				{
					return f.apply( t );
				}
			} );
		}

		default <T, U> Expectation call( final BiConsumer<T, U> c, final T t,
			final U u )
		{
			return call( new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					c.accept( t, u );
					return null;
				}
			} );
		}

		default <T, U, R> Expectation call( final BiFunction<T, U, R> f,
			final T t, final U u )
		{
			return call( new Callable<R>()
			{
				@Override
				public R call() throws Exception
				{
					return f.apply( t, u );
				}
			} );
		}

		static FutureSelf of( final Timed self, final Instant when )
		{
			if( self.now().compareTo( when ) > 0 ) throw ExceptionBuilder
					.unchecked( "Can't schedule in past: %s < now(%s)", when,
							self.now() )
					.build();
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
