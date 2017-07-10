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
package nl.rivm.cib.pilot;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;

import io.coala.random.PseudoRandom;

/**
 * {@link HHConnector}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface HHConnector
{

	Matrix connect( long size, Supplier<Long> degree,
		Function<long[], BigDecimal> initialW, Predicate<long[]> legalJ );

	/**
	 * utility method
	 * 
	 * @param x
	 * @return
	 */
	static long[] top( final long... x )
	{
		return x[0] > x[1] ? new long[] { x[1], x[0] } : x;
	}

	static BigDecimal putSymmetric( final Matrix W, final BigDecimal wNew,
		final long... x )
	{
		final long[] x_top = HHConnector.top( x );
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
		final long[] y = top( x );
		if( w != null && w instanceof BigDecimal )
			W.setAsBigDecimal( (BigDecimal) w, y );
		else
			W.setAsObject( w, y );
	}

	static BigDecimal getSymmetric( final Matrix W, final long... x )
	{
		return W.getAsBigDecimal( HHConnector.top( x ) );
	}

	/**
	 * <B>NOTE</b> JVM/UJMP-BUG: don't parallelize, coord array reused by JVM?
	 * 
	 * @param W
	 * @return
	 */
	static Stream<long[]> availableCoordinates( final Matrix W )
	{
		return W == null ? Stream.empty()
				: StreamSupport.stream( W.availableCoordinates().spliterator(),
						false ); // 
	}

	static boolean isPeer( final Matrix W, final long... x )
	{
		final long[] y = top( x );
		return W.containsCoordinates( y )
				&& W.getAsBigDecimal( y ).signum() > 0;
	}

	static LongStream availablePeers( final Matrix W, final long i )
	{
		// bug 1: W.selectColumns( Ret.LINK, i ).availableCoordinates() does not LINK but creates new
		// bug 2: W.selectRows( Ret.LINK, i ).transpose() fails

		return Stream
				.of( LongStream.range( 0, i ).filter( j -> isPeer( W, i, j ) ),
						LongStream.range( i + 1, W.getColumnCount() )
								.filter( j -> isPeer( W, i, j ) ) )
				.flatMapToLong( s -> s );
	}

	class WattsStrogatz implements HHConnector
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
			final Function<long[], BigDecimal> initialW,
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
							final long[] y = HHConnector.top( x );
							result.setAsBigDecimal( initialW.apply( y ), y );
							continue;
						}
					}
				}
			}

			// step 2: perturb lattice
			// FIXME parallelized row selection causes NPE, not Thread safe?
			LongStream.range( 0, size - 1 )// last row in triangle is empty
					.forEach( i -> LongStream.range( i + 1, size - 1 )
							.filter( j -> HHConnector.isPeer( result, i, j )
									&& this.rng.nextDouble() < this.beta )
							.forEach( j ->
							{
								// shuffle until : non-self and non-used
								final long[] x = { i, j }, y = { i, j };
								for( int attempt = 0; attempt < 10 && (y[1] == i // skip self
										|| HHConnector.isPeer( result, y ) // skip used
										|| !legalJ.test( y ) // skip illegal
								); attempt++ )
									y[1] = i + this.rng.nextLong( size - i );

								// weight to move from i,j to i,k
								final BigDecimal w = HHConnector
										.getSymmetric( result, x );
								// reset old position
								HHConnector.setSymmetric( result, null, x );
								// set new position
								HHConnector.setSymmetric( result, w, y );
							} ) );

			return result;
		}

	}

}
