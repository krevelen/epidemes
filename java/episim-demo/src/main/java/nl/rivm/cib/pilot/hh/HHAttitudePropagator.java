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
package nl.rivm.cib.pilot.hh;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.apache.logging.log4j.Logger;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation.Ret;

import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.MatrixUtil;
import nl.rivm.cib.episim.model.SocialConnector;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy;

/**
 * {@link HHAttitudePropagator} : [{@link HHAttribute}] &rarr; [0,1]
 */
public interface HHAttitudePropagator
{

	BigDecimal filteredAppreciation( BigDecimal appreciation,
		BigDecimal calculationLevel );

	/**
	 * propagate the new weighted averages of default social attributes:
	 * {@link HHAttribute#CONFIDENCE} and {@link HHAttribute#COMPLACENCY}
	 * 
	 * @param hhPressure an m &times; n {@link Matrix} containing for <em>m</em>
	 *            households (rows) their respective appreciation weight values
	 *            of all <em>n</em> households (columns)
	 * @param hhAttributes an n &times; k {@link Matrix} containing for all
	 *            <em>n</em> households (rows) their respective <em>k</em>
	 *            attribute values (columns)
	 * @return updated indices
	 */
	default Map<Long, Integer> propagate( final Matrix hhPressure,
		final Matrix hhAttributes )
	{
		return propagate( hhPressure, hhAttributes,
				HHAttribute.CONFIDENCE.ordinal(),
				HHAttribute.COMPLACENCY.ordinal() );
	}

	/**
	 * @param hhPressure an m &times; n {@link Matrix} containing for <em>m</em>
	 *            households (rows) their respective appreciation weight values
	 *            of all <em>n</em> households (columns)
	 * @param hhAttributes an n &times; k {@link Matrix} containing for all
	 *            <em>n</em> households (rows) their respective <em>k</em>
	 *            attribute values (columns)
	 * @param attributePressuredCols the indices of {@link HHAttribute} values
	 *            to replace by their respective newly weighted average
	 * @return updated indices mapped to number of peers causing the change
	 */
	default Map<Long, Integer> propagate( final Matrix hhPressure,
		final Matrix hhAttributes, final long... attributePressuredCols )
	{
		Objects.requireNonNull( hhPressure, "network null" );
		Objects.requireNonNull( hhAttributes, "attributes null" );
		final long hhTotal = hhAttributes.getRowCount();

		final AtomicLong duration = new AtomicLong(
				System.currentTimeMillis() ), hhCount = new AtomicLong();
		final Logger LOG = LogUtil.getLogger( getClass() );
		final AtomicBoolean logged = new AtomicBoolean( false );

		// calculate new attributes based on all current (weighted) information
		final Matrix newAttributes = Matrix.Factory.zeros( hhTotal, Objects
				.requireNonNull( attributePressuredCols, "cols null" ).length );

		final Set<Long> attrLogged = new HashSet<>();
		final Matrix colV = hhAttributes.selectColumns( Ret.LINK,
				attributePressuredCols );
		final Map<Long, Integer> changed = LongStream.range( 0, hhTotal )
				.parallel() // !!
				.mapToObj( i ->
				{
					hhCount.incrementAndGet();
					if( System.currentTimeMillis() - duration.get() > 1000 )
					{
						duration.set( System.currentTimeMillis() );
						LOG.trace( "Propagating; {} of {}", hhCount.get(),
								hhTotal );
						logged.set( true );
					}

					final long attr = hhAttributes.getAsLong( i,
							HHAttribute.ATTRACTOR_REF.ordinal() );
					if( attr == i || attr < 0 ) // skip attractor
					{
						MatrixUtil.insertObject( false, newAttributes,
								colV.selectRows( Ret.LINK, i ), i, 0 );
						return (Number[]) null;
					}
					final Matrix rowW = SparseMatrix.Factory.zeros( 1,
							hhTotal );

					final BigDecimal calc = hhAttributes.getAsBigDecimal( i,
							HHAttribute.CALCULATION.ordinal() );
					final AtomicReference<BigDecimal> sumW = new AtomicReference<>(
							BigDecimal.ZERO );
					final AtomicInteger sumJ = new AtomicInteger( 0 );
					SocialConnector.availablePeers( hhPressure, i ).forEach( j ->
					{
						sumJ.incrementAndGet();
						final BigDecimal w = SocialConnector
								.getSymmetric( hhPressure, i, j );
						// apply calculation threshold function to peers
						sumW.getAndUpdate(
								s -> s.add( filteredAppreciation( w, calc ) ) );
						rowW.setAsBigDecimal( w, 0, j );
					} );

					if( sumW.get().signum() < 1 )
					{
						MatrixUtil.insertObject( false, newAttributes,
								colV.selectRows( Ret.LINK, i ), i, 0 );
						return (Number[]) null;
					}

					// determine weights for self and attractor
					final BigDecimal selfW = sumW.get()
							.multiply( hhAttributes.getAsBigDecimal( attr,
									HHAttribute.IMPRESSION_SELF_MULTIPLIER
											.ordinal() ) ),
							attrW = sumW.get().multiply(
									hhAttributes.getAsBigDecimal( attr,
											HHAttribute.IMPRESSION_ATTRACTOR_MULTIPLIER
													.ordinal() ) ),
							totalW = sumW.get().add( selfW ).add( attrW );

					// set own impact/stability
					rowW.setAsBigDecimal( selfW, 0, i );

					// set attractor impact/pressure
					rowW.setAsBigDecimal( attrW, 0, attr );

					// get current hesitancy values and insert transformed
//					final Matrix prod = rowW.mtimes( colV )
//							.divide( totalW.doubleValue() );
//					LOG.trace( "{}:\n{}*\n{}\n / {}\t= {} ", i, rowW,
//							colV.transpose(), totalW, prod );
					final Matrix res = sumW.get().signum() == 0 ? colV
							: rowW.mtimes( colV )
									.divide( totalW.doubleValue() );
					MatrixUtil.insertObject( false, newAttributes, res, i, 0 );

					final int j = sumJ.get();
					if( attrLogged.add( attr ) )
					{
						final int scale = 4;
						final int n = hhAttributes.getAsInt( i,
								HHAttribute.SOCIAL_NETWORK_SIZE.ordinal() );
						LOG.trace(
								"hh #{} [{},{}] ---({}/{}={}%)--> [{},{}] -> [{},{}]",
								i,
								DecimalUtil.toScale(
										hhAttributes.getAsBigDecimal( i,
												attributePressuredCols[0] ),
										scale ),
								DecimalUtil.toScale(
										hhAttributes.getAsBigDecimal( i,
												attributePressuredCols[1] ),
										scale ),
								j, n, DecimalUtil.toScale( 100. * j / n, 1 ),
								DecimalUtil.toScale(
										newAttributes.getAsBigDecimal( i, 0 ),
										scale ),
								DecimalUtil.toScale(
										newAttributes.getAsBigDecimal( i, 1 ),
										scale ),
								DecimalUtil.toScale(
										hhAttributes.getAsBigDecimal( attr,
												attributePressuredCols[0] ),
										1 ),
								DecimalUtil.toScale(
										hhAttributes.getAsBigDecimal( attr,
												attributePressuredCols[1] ),
										1 ) );
					}
					return new Number[] { i, j };
				} ).filter( e -> e != null ).collect( Collectors
						.toMap( e -> (Long) e[0], e -> (Integer) e[1] ) );
		if( logged.get() )
			LOG.trace( "Propagated all {} of {}", hhCount.get(), hhTotal );

		// update all attributes at once afterwards
		IntStream.range( 0, attributePressuredCols.length ).parallel()
				.forEach( col -> MatrixUtil.insertObject( false, hhAttributes,
						newAttributes.selectColumns( Ret.LINK, col ), 0,
						attributePressuredCols[col] ) );

		return changed;
	}

	// examples

	/** {@link Threshold} wraps {@link VaxHesitancy#thresholdAppreciation} */
	class Threshold implements HHAttitudePropagator
	{
		@Override
		public BigDecimal filteredAppreciation( final BigDecimal appreciation,
			final BigDecimal calculationLevel )
		{
			return VaxHesitancy.thresholdAppreciation( appreciation,
					calculationLevel );
		}
	}

	/** {@link Shifted} wraps {@link VaxHesitancy#shiftedAppreciation} */
	class Shifted implements HHAttitudePropagator
	{
		@Override
		public BigDecimal filteredAppreciation( final BigDecimal appreciation,
			final BigDecimal calculationLevel )
		{
			return VaxHesitancy.shiftedAppreciation( appreciation,
					calculationLevel );
		}
	}
}