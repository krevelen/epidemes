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
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.random.PseudoRandom;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.util.Compare;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.episim.model.OptionPicker;

public interface MotherPicker<T extends MotherPicker.Mother>
	extends OptionPicker<T>, Proactive
{

	Observable<T> emitPicks();

	@SuppressWarnings( "rawtypes" )
	@FunctionalInterface
	interface Mother extends Comparable//Candidate
	{
		// TODO make birth as abstract Preference to map (and sort) candidates
		Instant born();

		@Override
		default int compareTo( final Object o )
		{
			return born().compareTo( ((Mother) o).born() );
		}
	}

	void registerDuring( final T candidate, Range<Instant> fertilityInterval );

	T pickAndRemoveFor( BiFunction<Iterable<T>, Long, T> picker,
		Quantity<Time> recoveryPeriod );

	Collection<Mother> candidatesBornIn( Range<Instant> birthFilter );

	default Iterable<Mother> candidatesOfAge( final Range<Integer> ageFilter )
	{
		return candidatesBornIn( ageToBirthInterval( now(), ageFilter ) );
	}

	// TODO make age as abstract Filter with birth Attribute-based selection

	default T doPick( final BiFunction<Iterable<T>, Long, T> picker )
	{
		return picker.apply( all(), total() );
	}

	default T pick( final Range<Integer> ageFilter, final PseudoRandom rng )
	{
		return pick( ageToBirthInterval( now(), ageFilter ), rng::nextElement );
	}

	default T pick( final Range<Instant> birthFilter,
		final BiFunction<Iterable<T>, Long, T> picker )
	{
		@SuppressWarnings( "unchecked" )
		final Collection<T> flat = (Collection<T>) candidatesBornIn(
				birthFilter );
		return picker.apply( flat, (long) flat.size() );
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
			private final NavigableSet<Mother> byBirth = new ConcurrentSkipListSet<>();

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
				final BiFunction<Iterable<T>, Long, T> picker,
				final Quantity<Time> recoveryPeriod )
			{
				// pick
				final T result = doPick( picker );
				if( result != null )
				{
					// unregister and start recoveryPeriod
					if( recoveryPeriod != null )
					{
						unregister( result );
						// re-register after (possibly adjusted) recoveryPeriod
						if( Compare.lt( now().add( recoveryPeriod ),
								this.candidateRemovals.get( result )
										.unwrap() ) )
							after( recoveryPeriod ).call( t->register(
									result ));
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
					at( fertilityInterval.lowerValue() )
							.call( t -> register( candidate ) );
					return;
				}
				final Instant end = fertilityInterval.upperValue();
				this.candidateRemovals.compute( candidate, ( k,
					exp ) -> exp != null ? exp
							: end == null ? null
									: at( end ).call(
											t -> unregister( candidate ) ) );
			}

			@Override
			public boolean register( final T candidate )
			{
				return this.byBirth.add( candidate );
			}

			@Override
			public boolean unregister( final T candidate )
			{
				final Expectation exp = this.candidateRemovals
						.remove( candidate );
				if( exp != null ) exp.remove();
				return this.byBirth.remove( candidate );
			}

			@Override
			public Iterable<T> all()
			{
				return this.candidateRemovals.keySet();
			}

			@Override
			public long total()
			{
				return this.candidateRemovals.size();
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public Collection<Mother>
				candidatesBornIn( final Range<Instant> birthFilter )
			{
				return birthFilter.<Mother>map( t -> () -> t )
						.apply( this.byBirth, true );
			}

			private final Map<Function<T, Number>, Supplier<T>> prefs = new HashMap<>();

			@Override
			public Map<Function<T, Number>, Supplier<T>> preferences()
			{
				return this.prefs;
			}
		};
	}
}