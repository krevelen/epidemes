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
package nl.rivm.cib.episim.model.person;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.random.PseudoRandom;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.episim.model.CandidatePicker;

public interface MotherPicker<T extends MotherPicker.Mother>
	extends CandidatePicker<T>
{

	interface Mother extends Candidate
	{
		// TODO make birth as abstract Attribute to map (and sort) candidates
		Instant born();
	}

	void registerDuring( final T candidate, Range<Instant> fertilityInterval );

	T pickAndRemoveFor( BiFunction<Iterable<T>, Integer, T> picker,
		Quantity<Time> recoveryPeriod );

	Collection<Mother> candidatesBornIn( Range<Instant> birthFilter );

	default Iterable<Mother> candidatesOfAge( final Range<Integer> ageFilter )
	{
		return candidatesBornIn( ageToBirthInterval( now(), ageFilter ) );
	}

	// TODO make age as abstract Filter with birth Attribute-based selection

	default T pick( final Range<Integer> ageFilter, final PseudoRandom rng )
	{
		return pick( ageToBirthInterval( now(), ageFilter ), rng::nextElement );
	}

	default T pick( final Range<Instant> birthFilter,
		final BiFunction<Iterable<T>, Integer, T> picker )
	{
		@SuppressWarnings( "unchecked" )
		final Collection<T> flat = (Collection<T>) candidatesBornIn(
				birthFilter );
		return doPick( flat, flat.size(), picker );
	}

	static Range<Instant> birthToAgeInterval( final Instant birth,
		final Range<Integer> ageRange )
	{
		final Integer min = ageRange.lowerValue();
		final Integer max = ageRange.upperValue();
		return Range.of( min == null ? null
				: birth.add( QuantityUtil
						.valueOf( ageRange.lowerValue(), TimeUnits.ANNUM )
						.add( QuantityUtil.valueOf( BigDecimal.ONE,
								TimeUnits.ANNUM ) ) ),
				true,
				max == null ? null
						: birth.add(
								QuantityUtil.valueOf( max, TimeUnits.ANNUM ) ),
				false );
	}

	static Range<Instant> ageToBirthInterval( final Instant now,
		final Range<Integer> ageRange )
	{
		final Integer min = ageRange.lowerValue();
		final Integer max = ageRange.upperValue();
		return Range.of( min == null ? null
				: now.subtract( QuantityUtil
						.valueOf( ageRange.lowerValue(), TimeUnits.ANNUM )
						.add( QuantityUtil.valueOf( BigDecimal.ONE,
								TimeUnits.ANNUM ) ) ),
				false,
				max == null ? null
						: now.subtract(
								QuantityUtil.valueOf( max, TimeUnits.ANNUM ) ),
				true );
	}

	Logger LOG = LogUtil.getLogger( MotherPicker.class );

	static <T extends MotherPicker.Mother> MotherPicker<T>
		of( final Scheduler scheduler )
	{
		return new MotherPicker<T>()
		{
			/** the candidates */
			private final Map<T, Expectation> candidateRemovals = new ConcurrentHashMap<>();

			/** the candidates sorted by birth */
			private final NavigableSet<Mother> byBirth = new ConcurrentSkipListSet<>(
					( o1, o2 ) -> o1.born().compareTo( o2.born() ) );

			/** the stream of picked mothers */
			private final Subject<T> mothers = PublishSubject.create();

			@Override
			public Scheduler scheduler()
			{
				return scheduler;
			}

			@Override
			public Observable<T> emitPicks()
			{
				return this.mothers;
			}

			@Override
			public T pickAndRemoveFor(
				final BiFunction<Iterable<T>, Integer, T> picker,
				final Quantity<Time> recoveryPeriod )
			{
				// pick
				final T result = pick( picker );
				if( result != null )
				{
					// unregister before recoveryPeriod
					if( recoveryPeriod != null )
					{
						unregister( result );
						// re-register after (possibly adjusted) recoveryPeriod
						if( now().add( recoveryPeriod )
								.compareTo( this.candidateRemovals.get( result )
										.unwrap() ) < 0 )
							after( recoveryPeriod ).call( this::register,
									result );
					}

					// publish
					this.mothers.onNext( result );
				}
				return result;
			}

			@Override
			public void registerDuring( final T candidate,
				final Range<Instant> fertilityInterval )
			{
				if( fertilityInterval == null || fertilityInterval.lt( now() ) )
					return;
				if( fertilityInterval.gt( now() ) )
				{
					at( fertilityInterval.lowerValue() ).call( this::register,
							candidate );
					return;
				}
				final Instant end = fertilityInterval.upperValue();
				this.candidateRemovals.compute( candidate,
						( k, exp ) -> exp != null ? exp
								: end == null ? null
										: at( end ).call( this::unregister,
												candidate ) );
			}

			@Override
			public void register( final T candidate )
			{
				this.byBirth.add( candidate );
			}

			@Override
			public void unregister( final T candidate )
			{
				final Expectation exp = this.candidateRemovals
						.remove( candidate );
				if( exp != null ) exp.remove();
				this.byBirth.remove( candidate );
			}

			@Override
			public Iterator<T> iterator()
			{
				return this.candidateRemovals.keySet().iterator();
			}

			@Override
			public Integer total()
			{
				return this.candidateRemovals.size();
			}

			@Override
			public Collection<Mother>
				candidatesBornIn( final Range<Instant> birthFilter )
			{
				return this.byBirth.subSet( () -> birthFilter.lowerValue(),
						birthFilter.lowerInclusive(),
						() -> birthFilter.upperValue(),
						birthFilter.upperInclusive() );
			}
		};
	}
}