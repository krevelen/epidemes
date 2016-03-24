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
package nl.rivm.cib.episim.math;

import javax.measure.Measurable;
import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Indicator} decorates an {@link Amount} as {@link Timed}
 * 
 * @param <Q>the concrete type of accumulated {@link Quantity}
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Indicator<Q extends Quantity>
	implements Timed, Comparable<Indicator<Q>>
{

	private Amount<Q> value = null;

	private final transient Scheduler scheduler;

	private final transient Subject<Amount<Q>, Amount<Q>> values = PublishSubject
			.create();

	public Indicator( final Scheduler scheduler, final Amount<Q> initialValue )
	{
		this.scheduler = scheduler;
		setValue( initialValue );
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public String toString()
	{
		return getValue() == null ? null : getValue().toString();
	}

	@Override
	public int compareTo( final Indicator<Q> o )
	{
		return getValue().compareTo( o.getValue() );
	}

	public synchronized void setValue( final Amount<Q> amount )
	{
		this.value = amount;
		if( amount != null ) this.values.onNext( amount );
	}

	public Amount<Q> getValue()
	{
		return this.value;
	}

	public Observable<Amount<Q>> emitValues()
	{
		return this.values.asObservable();
	}

	/**
	 * @param target the {@link Amount} to equate (unit, value, range, etc.)
	 * @return a filtered mapping of {@link #emitValues()} to an
	 *         {@link Observable} stream of all {@link Instant}s when this
	 *         {@link Indicator}'s {@link Amount} equals specified
	 *         {@code target}
	 */
	public Observable<Instant> emitEquals( final Amount<Q> target )
	{
		return emitValues().filter( ( Amount<Q> amount ) ->
		{
			return amount.equals( target );
		} ).map( ( Amount<Q> amount ) ->
		{
			return now();
		} );
	}

	/**
	 * @param target the {@link Measurable} to compare
	 * @return a filtered mapping of {@link #emitValues()} to an
	 *         {@link Observable} stream of all {@link Instant}s when this
	 *         {@link Indicator}'s {@link Amount} equals specified
	 *         {@code target}
	 */
	public Observable<Instant> emitIsSame( final Measurable<Q> target )
	{
		return emitValues().filter( ( Amount<Q> amount ) ->
		{
			return amount.compareTo( target ) == 0;
		} ).map( ( Amount<Q> amount ) ->
		{
			return now();
		} );
	}

	/**
	 * @param minimum the lower bound {@link Measurable} (inclusive)
	 * @param maximum the upper bound {@link Measurable} (inclusive)
	 * @return a filtered mapping of {@link #emitValues()} to an
	 *         {@link Observable} stream of {@link Boolean}s whether
	 *         {@link #getValue(} is currently within specified range
	 */
	public Observable<Boolean> emitIsLessThan( final Measurable<Q> maximum )
	{
		return emitIsWithin( null, null, maximum, false );
	}

	/**
	 * @param minimum the lower bound {@link Measurable} (inclusive)
	 * @param maximum the upper bound {@link Measurable} (inclusive)
	 * @return a filtered mapping of {@link #emitValues()} to an
	 *         {@link Observable} stream of {@link Boolean}s whether
	 *         {@link #getValue(} is currently within specified range
	 */
	public Observable<Boolean> emitIsWithin( final Measurable<Q> minimum,
		final Measurable<Q> maximum )
	{
		return emitIsWithin( minimum, true, maximum, true );
	}

	/**
	 * @param minimum the lower bound {@link Measurable} or {@code null}
	 * @param isMinimumInclusive whether the lower bound is inclusive
	 * @param maximum the upper bound {@link Measurable} or {@code null}
	 * @param isMaximumInclusive whether the upper bound is inclusive
	 * @return a filtered mapping of {@link #emitValues()} to an
	 *         {@link Observable} stream of {@link Boolean}s whether
	 *         {@link #getValue(} is currently within specified range
	 */
	public <T extends Measurable<Q>> Observable<Boolean> emitIsWithin( final Measurable<Q> minimum,
		final Boolean isMinimumInclusive, final Measurable<Q> maximum,
		final Boolean isMaximumInclusive )
	{
		return emitValues().map( ( final Amount<Q> amount ) ->
		{
			final int minimumCompare = amount.compareTo( minimum );
			final boolean isWithinLowerBound = minimum == null
					|| isMinimumInclusive ? minimumCompare >= 0
							: minimumCompare > 0;
			if( !isWithinLowerBound ) return false;
			final int maximumCompare = amount.compareTo( maximum );
			return maximum == null || isMaximumInclusive ? maximumCompare <= 0
					: maximumCompare < 0;
		} );
	}

	public static <Q extends Quantity> Indicator<Q>
		of( final Scheduler scheduler )
	{
		return of( scheduler, null );
	}

	public static <Q extends Quantity> Indicator<Q>
		of( final Scheduler scheduler, final Amount<Q> initialValue )
	{
		return new Indicator<>( scheduler, initialValue );
	}
}