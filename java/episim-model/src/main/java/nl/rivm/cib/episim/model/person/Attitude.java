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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import io.coala.exception.Thrower;
import io.coala.json.Attributed;

/**
 * {@link Attitude} or stance toward some type of phenomenon is similar to a
 * {@link Predicate} and may be dynamic, reflecting e.g. cumulative experiences
 * such as perceived safety/risk or influence from opinions observed from a
 * (dynamic) social network
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@FunctionalInterface
public interface Attitude<T> //extends Timed
{

	/**
	 * @return {@code true} if this {@link Attitude} is (currently) positive
	 */
	boolean isPositive( T phenomenon );

	default Attitude<T> and( final Attitude<T> that )
	{
		return t -> this.isPositive( t ) && that.isPositive( t );
	}

	default Attitude<T> or( final Attitude<T> that )
	{
		return t -> this.isPositive( t ) || that.isPositive( t );
	}

	/**
	 * @param positive
	 * @return a fixed attitude
	 */
	static <T> Attitude<T> of( final boolean positive )
	{
		return t -> positive;
	}

	/**
	 * @param values an n-length {@link List}
	 * @param index 0 <= index < n (values.size())
	 * @param evaluator a {@link BiFunction} that takes into account both the
	 *            phenomenon and a value vector at specified row index
	 * @return an {@link Attitude} function
	 */
	static <T, S> Attitude<T> of( final S index,
		final BiFunction<T, S, Boolean> evaluator )
	{
		return t -> evaluator.apply( t, index );
	}

	/**
	 * @param values an n-length {@link List}
	 * @param index 0 <= index < n (values.size())
	 * @param evaluator a {@link BiFunction} that takes into account both the
	 *            phenomenon and a value vector at specified row index
	 * @return an {@link Attitude} function
	 */
	static <T, S> Attitude<T> of( final List<S> values, final int index,
		final BiFunction<T, S, Boolean> evaluator )
	{
		if( index >= values.size() )
			Thrower.throwNew( IllegalArgumentException::new,
					() -> "index exceeds values size" );

		return t -> evaluator.apply( t, values.get( index ) );
	}

	/**
	 * @param values an n x m {@link Matrix}
	 * @param row 0 <= row < n (values.getSize(0))
	 * @param evaluator a {@link BiFunction} that takes into account both the
	 *            phenomenon and a value vector (1 x m)
	 * @return an {@link Attitude} function for specified row index
	 */
	static <T> Attitude<T> of( final Matrix values, final long row,
		final BiFunction<T, Matrix, Boolean> evaluator )
	{
		if( row >= values.getSize( 0 ) )
			Thrower.throwNew( IndexOutOfBoundsException::new,
					() -> row + " >= " + values.getSize( 0 ) );

		return t -> evaluator.apply( t, values.selectRows( Ret.NEW, row ) );
	}

	/**
	 * @param values an n x m {@link Matrix}
	 * @param weights an n x n {@link Matrix} (e.g. connection strength)
	 * @param row 0 <= row < n (values.getSize(0))
	 * @param evaluator a {@link BiFunction} that takes into account both the
	 *            phenomenon and a weighted value vector (1 x m)
	 * @return an {@link Attitude} function for specified row index
	 */
	static <T> Attitude<T> of( final Matrix values, final Matrix weights,
		final long row, final BiFunction<T, Matrix, Boolean> evaluator )
	{
		if( row >= values.getSize( 0 ) )
			Thrower.throwNew( IndexOutOfBoundsException::new,
					() -> row + " >= " + values.getSize( 0 ) );
		if( row >= weights.getSize( 0 ) )
			Thrower.throwNew( IndexOutOfBoundsException::new,
					() -> row + " >= " + weights.getSize( 0 ) );
		if( weights.getSize( 0 ) != weights.getSize( 1 ) )
			Thrower.throwNew( IllegalArgumentException::new,
					() -> "Weights dimensions are unequal, should be (n x n)" );
		if( weights.getSize( 1 ) != values.getSize( 0 ) )
			Thrower.throwNew( IllegalArgumentException::new,
					() -> "Weights dimensions != values row count" );

		return t -> evaluator.apply( t,
				weights.selectRows( Ret.NEW, row ).mtimes( values ) );
	}

	/**
	 * {@link Attributable}
	 * 
	 * @param <THIS> the concrete sub-type
	 */
	interface Attributable<THIS> extends Attributed
	{
		Map<Class<?>, Attitude<?>> getAttitudes();

		void setAttitudes( Map<Class<?>, Attitude<?>> attitudes );

		@SuppressWarnings( "unchecked" )
		default THIS withAttitudes( final Map<Class<?>, Attitude<?>> attitudes )
		{
			setAttitudes( attitudes );
			return (THIS) this;
		}

		@SuppressWarnings( "unchecked" )
		default <T> THIS with( final Class<T> type, final Attitude<T> attitude )
		{
			Map<Class<?>, Attitude<?>> attitudes;
			try
			{
				attitudes = getAttitudes();
			} catch( final NullPointerException e )
			{
				attitudes = new HashMap<>();
				setAttitudes( attitudes );
			}
			// FIXME TypeArguments type loops infinitely
//			final Class<T> type = (Class<T>) TypeArguments
//					.of( Attitude.class, attitude.getClass() ).get( 0 );
			attitudes.put( type, attitude );
			return (THIS) this;
		}
	}
}