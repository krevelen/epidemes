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
package nl.rivm.cib.episim.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.measure.Quantity;

import io.coala.math.LatLong;
import io.coala.math.WeightedValue;
import io.coala.random.PseudoRandom;
import io.reactivex.Observable;

/**
 * {@link OptionPicker} is a simple data-source with (attribute-based) sorting
 * and filtering as well as (size-based) random access for picking an entry
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface OptionPicker<T>
{

	Iterable<T> all();

	long total();

	boolean register( T option );

	boolean unregister( T option );

	default T pick( final PseudoRandom rng )
	{
		return rng.nextElement( all(), total() );
	}

	Map<Function<T, Number>, Supplier<T>> preferences();

	/**
	 * @param evaluator provides weights for values, including {@code null} if a
	 *            value is not an option (e.g. removed)
	 * @param distGen returns a supplier that picks based on an
	 *            {@link Observable} stream of {@link WeightedValue}
	 * @return a pick according to specified evaluator
	 */
	default T pick( final Function<T, Number> evaluator,
		final Function<Observable<WeightedValue<T>>, Supplier<T>> distGen )
	{
		return preferences()
				.computeIfAbsent( evaluator, key -> distGen.apply( Observable
						.fromIterable( all() )
						.map( t -> WeightedValue.of( t, key.apply( t ) ) )
						.filter( wv -> wv.getWeight() != null ) ) )
				.get();
	}

	static <T, Q extends Quantity<Q>> Function<T, Number> preferLargest(
		final T origin, final Function<T, Quantity<Q>> valueFetcher )
	{
		return t -> valueFetcher.apply( origin )
				.subtract( valueFetcher.apply( t ) ).getValue();
	}

	static <T, Q extends Quantity<Q>> Function<T, Number> preferSmallest(
		final T origin, final Function<T, Quantity<Q>> valueFetcher )
	{
		return t -> valueFetcher.apply( origin )
				.subtract( valueFetcher.apply( t ) ).inverse().getValue();
	}

	static <T> Function<T, Number> preferFarthest( final T origin,
		final Function<T, LatLong> coordFetcher )
	{
		return t -> coordFetcher.apply( origin )
				.angularDistance( coordFetcher.apply( t ) ).getValue();
	}

	static <T> Function<T, Number> preferNearest( final T origin,
		final Function<T, LatLong> coordFetcher )
	{
		return t -> coordFetcher.apply( origin )
				.angularDistance( coordFetcher.apply( t ) ).inverse()
				.getValue();
	}

	class Simple<T> implements OptionPicker<T>
	{
		private final Collection<T> coll;
		private final Map<Function<T, Number>, Supplier<T>> preferences = new HashMap<>();

		public Simple( final Collection<T> coll )
		{
			this.coll = coll;
		}

		@Override
		public Iterable<T> all()
		{
			return this.coll;
		}

		@Override
		public long total()
		{
			return this.coll.size();
		}

		@Override
		public boolean register( final T option )
		{
			this.preferences.clear(); // TODO add caching for statistics
			return this.coll.add( option );
		}

		@Override
		public boolean unregister( final T option )
		{
			this.preferences.clear(); // TODO add caching for statistics
			return this.coll.remove( option );
		}

		@Override
		public Map<Function<T, Number>, Supplier<T>> preferences()
		{
			return this.preferences;
		}

		// TODO implement for never-ending observable, e.g. BehaviorSubject ? 
//		@Override
//		public T pick( final Function<T, Number> evaluator,
//			final Function<Observable<WeightedValue<T>>, Supplier<T>> distGen )
//		{
//			return preferences()
//					.computeIfAbsent( evaluator,
//							key -> distGen.apply( all().map(
//									t -> WeightedValue.of( t, key.apply( t ) ) )
//									.filter( wv -> wv.getWeight() != null ) ) )
//					.get();
//		}

	}
}