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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
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
import nl.rivm.cib.episim.model.CandidatePicker;
import nl.rivm.cib.episim.model.CandidatePicker.Candidate.Cluster;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public interface MotherPicker<T extends MotherPicker.Mother>
	extends CandidatePicker<T>
{

	interface Mother extends Candidate
	{
		// TODO make birth as abstract Attribute to map (and sort) candidates
		Instant born();

		// TODO make fertility/recovery as abstract (un/re)registration schedule
		Range<Instant> fertilityInterval();

		Quantity<Time> recoveryPeriod();
	}

	default Iterable<Candidate> candidatesOfAge( final Integer ageFilter )
	{
		return candidatesBornIn( ageToBirthInterval( now(), ageFilter ) );
	}

	Iterable<Candidate> candidatesBornIn( final Range<Instant> birthFilter );

	// TODO make age as abstract Filter with birth Attribute-based selection

	default T pick( final Integer ageFilter, final PseudoRandom rng )
	{
		return pick( ageToBirthInterval( now(), Range.of( ageFilter ) ),
				( it, n ) ->
				{
					return rng.nextElement( it, n );
				} );
	}

	default T pick( final Range<Instant> birthFilter,
		final BiFunction<Iterable<T>, Integer, T> picker )
	{
		final List<T> flat = Cluster
				.flatList( candidatesBornIn( birthFilter ) );
		return doPick( flat, flat.size(), picker );
	}

	static Range<Instant> ageToBirthInterval( final Instant now,
		final Integer age )
	{
		return ageToBirthInterval( now, Range.of( age ) );
	}

	static Range<Instant> birthToAgeInterval( final Instant birth,
		final Range<Integer> ageRange )
	{
		final Integer min = ageRange.getMinimum().getValue();
		final Integer max = ageRange.getMaximum().getValue();
		return Range.of(
				min == null ? null
						: birth.add( QuantityUtil
								.valueOf( ageRange.getMinimum().getValue(),
										TimeUnits.ANNUM )
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
		final Integer min = ageRange.getMinimum().getValue();
		final Integer max = ageRange.getMaximum().getValue();
		return Range.of(
				min == null ? null
						: now.subtract( QuantityUtil
								.valueOf( ageRange.getMinimum().getValue(),
										TimeUnits.ANNUM )
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
			private final Map<T, Expectation> candidates = new ConcurrentHashMap<>();

			/** the candidates sorted by birth */
			private final NavigableMap<Instant, Candidate> byBirth = new ConcurrentSkipListMap<>();

			/** the stream of picked mothers */
			private final Subject<T, T> mothers = PublishSubject.create();

			@Override
			public Scheduler scheduler()
			{
				return scheduler;
			}

			@Override
			public Observable<T> emitPicks()
			{
				return this.mothers.asObservable();
			}

			@Override
			public T doPick( final Iterable<T> candidates, final Integer total,
				final BiFunction<Iterable<T>, Integer, T> picker )
			{
				// pick
				final T result = picker.apply( candidates, total );
				if( result == null ) return null;

				// unregister before recoveryPeriod
				if( result.recoveryPeriod() != null ) unregister( result );

				// publish
				this.mothers.onNext( result );

				// re-register after (possibly adjusted) recoveryPeriod
				if( result.recoveryPeriod() != null )
					after( result.recoveryPeriod() ).call( this::register,
							result );

				return result;
			}

			@Override
			public void register( final T candidate )
			{
				if( candidate.fertilityInterval() == null
						|| candidate.fertilityInterval().isLessThan( now() ) )
					return;
				if( candidate.fertilityInterval().isGreaterThan( now() ) )
				{
					at( candidate.fertilityInterval().getMinimum().getValue() )
							.call( this::register, candidate );
					return;
				}
				final Instant end = candidate.fertilityInterval().getMaximum()
						.getValue();
				this.byBirth.compute( candidate.born(), ( birth, current ) ->
				{
					return current == null ? candidate
							: Cluster.of( current, candidate );
				} );
				this.candidates.put( candidate, end == null ? null
						: at( end ).call( this::unregister, candidate ) );
			}

			@Override
			public void unregister( final T candidate )
			{
				final Expectation exp = this.candidates.remove( candidate );
				if( exp != null ) exp.remove();
				this.byBirth.computeIfPresent( candidate.born(),
						( birth, current ) ->
						{
							return current instanceof Cluster
									? ((Cluster) current).without( candidate )
									: null;
						} );
			}

			@Override
			public Iterator<T> iterator()
			{
				return this.candidates.keySet().iterator();
			}

			@Override
			public Integer total()
			{
				return this.candidates.size();
			}

			@Override
			public Iterable<Candidate>
				candidatesBornIn( final Range<Instant> birthFilter )
			{
				return this.byBirth
						.subMap( birthFilter.getMinimum().getValue(),
								birthFilter.getMinimum().isInclusive(),
								birthFilter.getMaximum().getValue(),
								birthFilter.getMaximum().isInclusive() )
						.values();
			}
		};
	}
}