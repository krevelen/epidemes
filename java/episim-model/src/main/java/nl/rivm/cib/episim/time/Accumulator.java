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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.measure.quantity.Quantity;

import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import io.coala.exception.x.ExceptionBuilder;
import io.coala.log.LogUtil;
import io.coala.time.x.Instant;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Accumulator}
 * 
 * @param <Q>the concrete type of accumulated {@link Quantity}
 * @param <R> the concrete type of accumulation rate {@link Quantity}
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Accumulator<Q extends Quantity> implements Timed
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( Accumulator.class );

	private final transient Subject<Amount<Q>, Amount<Q>> amounts = PublishSubject
			.create();

	private final transient Map<TargetAmount<Q>, Expectation> intercepts = new HashMap<>();

	private Integrator<Q> integrator;

	private Amount<Q> amount;

	private Scheduler scheduler;

	private Instant t;

	public Accumulator( final Scheduler scheduler )
	{
		this.scheduler = scheduler;
		this.t = now();
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	public synchronized void setIntegrator( final Integrator<Q> integrator )
	{
		final Instant t0 = this.t, t1 = now();
		this.integrator = integrator;
		this.t = t1;
		final Amount<Q> delta = this.integrator.delta( t0, t1 );
		final Amount<Q> amount = this.amount == null ? delta
				: this.amount.plus( delta );
		setAmount( amount );
	}

	public synchronized void setAmount( final Amount<Q> amount )
	{
		this.amount = amount;
		this.amounts.onNext( amount );

		this.intercepts.keySet().forEach( this::reschedule );
	}

	protected void reschedule( final TargetAmount<Q> target )
	{
		final Expectation e = this.intercepts.put( target, null );
		e.remove(); // unschedule target
		LOG.trace( "unscheduled a={} at t={}, total={}", target.amount, e,
				this.intercepts.size() );
		scheduleReached( target ); // reschedule target, if any
	}

	protected void onReached( final TargetAmount<Q> target )
	{
		setAmount( target.amount );
		target.consumer.accept( now() );
		scheduleReached( target );
	}

	protected void scheduleReached( final TargetAmount<Q> target )
	{
		final Instant t1 = now();
		final Instant t2 = this.integrator.when( t1,
				target.amount.minus( this.amount ) );
		if( t2 == null || t2.compareTo( t1 ) <= 0 ) return; // no repeats, push onCompleted()?
		
		// schedule repeat
		/*
		 * if( t2.compareTo( t1 ) <= 0 ) throw ExceptionBuilder .unchecked(
		 * "Got time in past: %s =< %s", t2, t1 ).build();
		 */

		this.intercepts.put( target, at( t2 ).call( this::onReached, target ) );
		LOG.trace( "scheduled a={} at t={}, total={}", target.amount, t2,
				this.intercepts.size() );
	}

	public void at( final Amount<Q> amount, final Consumer<Instant> observer )
	{
		scheduleReached( TargetAmount.of( amount, observer ) );
	}

	public Amount<Q> getAmount()
	{
		return this.amount;
	}

	public Observable<Amount<Q>> emitAmounts()
	{
		return this.amounts.asObservable();
	}

	public static <Q extends Quantity> Accumulator<Q>
		of( final Scheduler scheduler, final Integrator<Q> integrator )
	{
		return of( scheduler, null, integrator );
	}

	public static <Q extends Quantity> Accumulator<Q> of(
		final Scheduler scheduler, final Amount<Q> initialAmount,
		final Amount<?> initialRate )
	{
		return of( scheduler, initialAmount, Integrator.ofRate( initialRate ) );
	}

	public static <Q extends Quantity> Accumulator<Q> of(
		final Scheduler scheduler, final Amount<Q> initialAmount,
		final Integrator<Q> integrator )
	{
		final Accumulator<Q> result = new Accumulator<>( scheduler );
		result.setAmount( initialAmount );
		result.setIntegrator( integrator );
		return result;
	}

	static class TargetAmount<Q extends Quantity>
	{
		Amount<Q> amount;
		Consumer<Instant> consumer;

		public static <Q extends Quantity> TargetAmount<Q>
			of( final Amount<Q> amount, final Consumer<Instant> consumer )
		{
			final TargetAmount<Q> result = new TargetAmount<Q>();
			result.amount = amount;
			result.consumer = consumer;
			return result;
		}
	}

	public interface Integrator<Q extends Quantity>
	{
		/**
		 * @param start the interval start {@link Instant}
		 * @param end the interval end {@link Instant}
		 * @return the integral {@link Amount} of change
		 */
		Amount<Q> delta( Instant start, Instant end );

		/**
		 * @param now the current {@link Instant}
		 * @param delta the target {@link Amount} in/decrease
		 * @return the next {@link Instant} (after {@code now}) when given
		 *         {@link Amount} occurs again, or {@code null} if never
		 */
		Instant when( Instant now, Amount<Q> delta );

		static <Q extends Quantity, R extends Quantity> Integrator<Q>
			ofRate( final Amount<R> rate )
		{
			return new Integrator<Q>()
			{
				@SuppressWarnings( "unchecked" )
				@Override
				public Amount<Q> delta( final Instant start, final Instant end )
				{
					return (Amount<Q>) rate
							.times( end.subtract( start ).toAmount() );
				}

				@Override
				public Instant when( final Instant now, final Amount<Q> delta )
				{
					final Amount<?> duration = delta.divide( rate );
					return duration.getEstimatedValue() < 0 ? null
							: now.add( duration );
				}
			};
		}
	}
}