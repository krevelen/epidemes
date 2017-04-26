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
import java.util.function.BiFunction;

import io.coala.random.PseudoRandom;
import io.coala.time.Proactive;
import io.reactivex.Observable;

/**
 * {@link CandidatePicker} is a simple data-source with (attribute-based)
 * sorting and filtering as well as (size-based) random access for picking an
 * entry
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface CandidatePicker<T extends CandidatePicker.Candidate>
	extends Proactive, Iterable<T>
{

	void register( T candidate );

	default <S extends T> void register( final Collection<S> candidates )
	{
		for( S candidate : candidates )
			register( candidate );
	}

	@SuppressWarnings( "unchecked" )
	default <S extends T> void register( final S... candidates )
	{
		if( candidates != null && candidates.length != 0 )
			for( S candidate : candidates )
			register( candidate );
	}

	void unregister( T candidate );

	default <S extends T> void unregister( final Collection<S> candidates )
	{
		for( S candidate : candidates )
			unregister( candidate );
	}

	@SuppressWarnings( "unchecked" )
	default <S extends T> void unregister( final S... candidates )
	{
		if( candidates != null && candidates.length != 0 )
			for( S candidate : candidates )
			unregister( candidate );
	}

	Integer total();

	default T pick( final PseudoRandom rng )
	{
		return pick( ( it, n ) ->
		{
			return rng.nextElement( it, n - 1 );
		} );
	}

	default T pick( final BiFunction<Iterable<T>, Integer, T> picker )
	{
		return doPick( this, total(), picker );
	}

	default T doPick( final Iterable<T> candidates, final Integer total,
		final BiFunction<Iterable<T>, Integer, T> picker )
	{
		return picker.apply( candidates, total );
	}

	Observable<T> emitPicks();

	interface Attribute
	{
		// TODO use Attribute to map (and sort) entries
	}

	interface Filter
	{
		// TODO use abstract Filter for Attribute-based selection
	}

	interface Candidate
	{

//		class Cluster implements Candidate
//		{
//			public static Cluster of( final Candidate candidate1,
//				final Candidate candidate2 )
//			{
//				if( candidate1 instanceof Cluster )
//					return ((Cluster) candidate1).with( candidate2 );
//				if( candidate2 instanceof Cluster )
//					return ((Cluster) candidate2).with( candidate1 );
//				return new Cluster().with( candidate1 ).with( candidate2 );
//			}
//
//			private List<Candidate> candidates = new ArrayList<>();
//
//			public Cluster with( final Candidate mother )
//			{
//				if( mother instanceof Cluster )
//					this.candidates.addAll( ((Cluster) mother).candidates );
//				else
//					this.candidates.add( mother );
//				return this;
//			}
//
//			/**
//			 * @param candidate
//			 * @return
//			 */
//			public Cluster without( final Candidate candidate )
//			{
//				this.candidates.remove( candidate );
//				return this.candidates.isEmpty() ? null : this;
//			}
//
//			/**
//			 * @param candidates to flatten recursively
//			 * @return a flat {@link List} of {@link Candidate}s
//			 */
//			@SuppressWarnings( "unchecked" )
//			public static <T extends Candidate> List<T>
//				flatList( final Iterable<Candidate> candidates )
//			{
//				final List<T> result = new ArrayList<>();
//				for( Candidate candidate : candidates )
//					if( candidate instanceof Cluster )
//						result.addAll( Cluster
//								.flatList( ((Cluster) candidate).candidates ) );
//					else
//						result.add( (T) candidate );
////				if( result.isEmpty() )
////					throw new IllegalArgumentException( "Empty" );
//				return result;
//			}
//		}
	}
}