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
import java.util.Arrays;
import java.util.stream.LongStream;

import org.ujmp.core.Matrix;

import io.coala.util.Compare;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;

/**
 * {@link HHAttitudeEvaluator} : [{@link HHAttribute}] &rarr; [0,1]
 */
public interface HHAttitudeEvaluator
{
	/**
	 * @param occ the {@link VaxOccasion} to evaluate, or {@code null} if only a
	 *            general attitude (e.g. mental barrier) is requested
	 * @param hhAttributes an n &times; m {@link Matrix} containing for
	 *            <em>n</em> households (rows) their respective <em>m</em> (=
	 *            {@link HHAttribute#values() HHAttribute.values().length})
	 *            values (columns)
	 * @param hhIndex the household row index/indices to update, or {@code null}
	 *            for all
	 */
	boolean isPositive( VaxOccasion occ, Matrix hhAttributes, long hhIndex );

	/**
	 * @param occ the {@link VaxOccasion} to evaluate, or {@code null} if only a
	 *            general attitude (e.g. mental barrier) is requested
	 * @param hhAttributes an n &times; m {@link Matrix} containing for
	 *            <em>n</em> households (rows) their respective <em>m</em> (=
	 *            {@link HHAttribute#values() HHAttribute.values().length})
	 *            values (columns)
	 * @param hhFilter the household row index/indices to update, or
	 *            {@code null} for all
	 * @return a {@link LongStream} of indices for all households that are
	 *         positive about the {@link VaxOccasion} (or in general if
	 *         {@code null})
	 */
	default LongStream isPositive( final VaxOccasion occ,
		final Matrix hhAttributes, final long... hhFilter )
	{
		return (hhFilter == null || hhFilter.length == 0
				? LongStream.range( 0, hhAttributes.getRowCount() )
				: Arrays.stream( hhFilter ))
						.filter( i -> i != hhAttributes.getAsInt( i,
								HHAttribute.ATTRACTOR_REF.ordinal() ) )
						.filter( i -> isPositive( occ, hhAttributes, i ) );
	}

	// examples

	/** {@link Average} wraps {@link VaxHesitancy#averageBarrier} */
	class Average implements HHAttitudeEvaluator
	{
		@Override
		public boolean isPositive( final VaxOccasion occ,
			final Matrix hhAttributes, final long i )
		{
			final BigDecimal conf = hhAttributes.getAsBigDecimal( i,
					HHAttribute.CONFIDENCE.ordinal() );
			final BigDecimal comp = hhAttributes.getAsBigDecimal( i,
					HHAttribute.COMPLACENCY.ordinal() );

			// no occasion, just return general attitude
			if( occ == null ) return Compare.gt( conf, comp );

			final BigDecimal conv = VaxHesitancy.minimumConvenience( occ );
			final BigDecimal barrier = VaxHesitancy.averageBarrier( conf,
					comp );
			return Compare.ge( conv, barrier );
		}
	}

	/** {@link Difference} wraps {@link VaxHesitancy#differenceBarrier} */
	class Difference implements HHAttitudeEvaluator
	{

		@Override
		public boolean isPositive( final VaxOccasion occ,
			final Matrix hhAttributes, final long hhRow )
		{
			final BigDecimal conf = hhAttributes.getAsBigDecimal( hhRow,
					HHAttribute.CONFIDENCE.ordinal() );
			final BigDecimal comp = hhAttributes.getAsBigDecimal( hhRow,
					HHAttribute.COMPLACENCY.ordinal() );
			if( occ == null ) return Compare.gt( conf, comp );

			final BigDecimal conv = VaxHesitancy.minimumConvenience( occ );
			final BigDecimal barrier = VaxHesitancy.differenceBarrier( conf,
					comp );
			return Compare.ge( conv, barrier );
		}
	}
}