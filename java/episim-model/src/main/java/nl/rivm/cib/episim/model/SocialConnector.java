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

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;

import io.coala.math.MatrixUtil;
import io.coala.random.PseudoRandom;

/**
 * {@link SocialConnector}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface SocialConnector
{

	/**
	 * @param size number of nodes to connect
	 * @param k the average connection degree
	 * @return the connected graph; if symmetric then for for each W(i,j): i>=j
	 */
	default Matrix connect( final long size, final long k )
	{
		return connect( size, () -> k );
	}

	/**
	 * @param size number of nodes to connect
	 * @param initialK the initial degree supplier, e.g. a constant
	 * @return the connected graph; if symmetric then for for each W(i,j): i>=j
	 */
	default Matrix connect( final long size, final Supplier<Long> initialK )
	{
		return connect( size, initialK, x -> true );
	}

	/**
	 * @param size number of nodes to connect
	 * @param initialK the initial degree supplier, e.g. a constant
	 * @param legalJ a link accepter, e.g. some assortativity filter
	 * @return the connected graph; if symmetric then for for each W(i,j): i>=j
	 */
	Matrix connect( long size, Supplier<Long> initialK,
		Predicate<long[]> legalJ );

	/**
	 * @param size number of nodes to connect
	 * @param initialK the initial degree supplier, e.g. a constant
	 * @param legalJ a link accepter, e.g. some assortativity filter
	 * @param initialW the initial weight distribution, e.g. a constant
	 * @return the connected graph; if symmetric then for for each W(i,j): i>=j
	 */
	default Matrix connect( final long size, final Supplier<Long> initialK,
		final Predicate<long[]> legalJ,
		final Function<long[], BigDecimal> initialW )
	{
		final Matrix result = connect( size, initialK, legalJ );
		// NOTE don't use #availableCoordinates() as it misses half the coords
		MatrixUtil.streamAvailableCoordinates( result, true )
				.filter( x -> result.getAsDouble( x ) != 0d ).forEach(
						x -> result.setAsBigDecimal( initialW.apply( x ), x ) );
		return result;
	}

	/**
	 * utility method
	 * 
	 * @param x
	 * @return (min_x,max_x)
	 */
	static long[] rowSmallest( final long... x )
	{
		return x[0] > x[1] ? new long[] { x[1], x[0] } : x;
	}

	/**
	 * utility method
	 * 
	 * @param x
	 * @return (max_x,min_x)
	 */
	static long[] rowLargest( final long... x )
	{
		return x[0] < x[1] ? new long[] { x[1], x[0] } : x;
	}

	static BigDecimal putSymmetric( final Matrix W, final BigDecimal wNew,
		final long... x )
	{
		final long[] x_top = SocialConnector.rowSmallest( x );
		final BigDecimal wOld = getSymmetric( W, x_top );
		setSymmetric( W, wNew, x_top );
		return wOld;
	}

	static void setSymmetric( final Matrix W, final Object w, final long i )
	{
		W.setAsObject( w, i, i );
	}

	static void setSymmetric( final Matrix W, final Object w, final long... x )
	{
		final long[] y = rowSmallest( x );
		if( w != null && w instanceof BigDecimal )
			W.setAsBigDecimal( (BigDecimal) w, y );
		else
			W.setAsObject( w, y );
	}

	static BigDecimal getSymmetric( final Matrix W, final long... x )
	{
		return W.getAsBigDecimal( SocialConnector.rowSmallest( x ) );
	}

	/**
	 * @param W the weight {@link Matrix}
	 * @param x the link (between peers x[0] and x[1])
	 * @return {@code true} iff W_(min(x),max(x)) > 0
	 */
	static boolean isPeer( final Matrix W, final long... x )
	{
		final long[] y = rowSmallest( x );
		return //W.containsCoordinates( y ) && 
		W.getAsDouble( y ) != 0d;
	}

	/**
	 * @param W the weight {@link Matrix}
	 * @param i the focal peer
	 * @return a stream of all peers (j) of i having {@code W_(i,j) > 0}
	 * @see #isPeer
	 */
	static LongStream availablePeers( final Matrix W, final long i )
	{
		return Stream
				.of( LongStream.range( 0, i ).filter( j -> isPeer( W, j, i ) ),
						LongStream.range( i + 1, W.getColumnCount() )
								.filter( j -> isPeer( W, i, j ) ) )
				.flatMapToLong( s -> s );
	}

	/**
	 * {@link WattsStrogatz} implements a <a href=
	 * "https://www.wikiwand.com/en/Watts_and_Strogatz_model">Watts–Strogatz
	 * (WS) model</a> for connecting random graphs with small-world (social)
	 * network properties, which are more "realistic" than random graphs from
	 * the <a href=
	 * "https://www.wikiwand.com/en/Erd%C5%91s%E2%80%93R%C3%A9nyi_model">Erdős-Rényi
	 * (ER) model</a> but lack preferential attachment and respective power-law
	 * distributions as in the random scale-free networks from the <a href=
	 * "https://www.wikiwand.com/en/Barab%C3%A1si%E2%80%93Albert_model">Barabási-Albert
	 * (BA) model</a>.
	 */
	class WattsStrogatz implements SocialConnector
	{
		private final PseudoRandom rng;
		private final double beta;

		public WattsStrogatz( final PseudoRandom rng, final double beta )
		{
			this.rng = rng;
			this.beta = beta;
		}

		@Override
		public Matrix connect( final long size, final Supplier<Long> degree,
			final Predicate<long[]> legalJ )
		{
			final Matrix result = SparseMatrix.Factory.zeros( size, size );

			// step 1: setup lattice: link self + degree-1 lattice 'neighbors'
			// FIXME parallelized rows may contain just 1 value, not thread-safe?
			for( long i = 0; i < size - 1; i++ )
			{
				final long K = Math.min( size - i, // need room to shuffle j's
						degree.get() );

				for( long k = 0; k < K; k++ )
				{
					for( int attempt = 0; attempt < 10; attempt++ )
					{
						final long[] x = { i, (i + 1 + k) % size };
						if( legalJ.test( x ) )
						{
							final long[] y = SocialConnector.rowSmallest( x );
							result.setAsDouble( 1, y );
							break;
						}
					}
				}
			}

			// step 2: perturb lattice
			// FIXME parallelized row selection causes NPE, not Thread safe?
			LongStream.range( 0, size - 1 )// last row in triangle is empty
				.parallel()
					.forEach( i -> LongStream.range( i + 1, size - 1 )
							.filter( j -> SocialConnector.isPeer( result, i, j )
									&& this.rng.nextDouble() < this.beta )
							.forEach( j ->
							{
								// shuffle until : non-self and non-used
								final long[] x = { i, j }, y = { i, j };
								for( int attempt = 0; attempt < 10 && (y[1] == i // skip self
										|| SocialConnector.isPeer( result, y ) // skip used
										|| !legalJ.test( y ) // skip illegal
								); attempt++ )
									y[1] = i + this.rng.nextLong( size - i );

								// weight to move from i,j to i,k
								final double w = result.getAsDouble( x );
								// reset old position
								result.setAsDouble( 0, x );
								// set new position
								result.setAsDouble( w, y );
							} ) );

			return result;
		}
	}
}
