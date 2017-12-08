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
package nl.rivm.cib.demo;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import io.coala.math.Extreme;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.time.TimeUnits;
import io.coala.util.Compare;
import nl.rivm.cib.demo.DemoModel.Medical.VaxDose;
import nl.rivm.cib.demo.DemoModel.Medical.VaxRegimen;
import tec.uom.se.ComparableQuantity;

/**
 * {@link RVP}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class RVP implements VaxRegimen
{
	/**
	 * {@link BMR} doses vaccinate against measles, mumps and rubella (MMR)
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public enum BMR implements VaxDose
	{
		/**
		 * e.g. BMR0-1 (applied 6-12 months old) no invite (eg. holiday,
		 * outbreak)
		 */
		DOSE0( null, 26, 1 * 52 ),

		/**
		 * e.g. BMR1-1 (applied 1-9 years old) invite for 11 months/48 weeks/335
		 * days
		 */
		DOSE1( 48, 48, 9 * 52 ),

		/**
		 * e.g. BMR2-1 (applied 1-9 years old) invite for 14 months/61 weeks/425
		 * days
		 */
		DOSE2( 61, 52, 9 * 52 ),

		/** e.g. BMR2-2 (applied 9-19 years old) no invite (eg. ZRA) */
		DOSE3( null, 9 * 52, 19 * 52 ),

		;

		private final int bit = 1 << ordinal();

		private final Range<ComparableQuantity<Time>> ageRangeSpecial;

		private final Range<ComparableQuantity<Time>> ageRangeNormal;

		private BMR( final Integer defaultAgeWeeks,
			final Integer minimumAgeWeeks, final Integer maximumAgeWeeks )
		{
			this.ageRangeSpecial = Range.of( minimumAgeWeeks, maximumAgeWeeks,
					TimeUnits.WEEK );
			this.ageRangeNormal = defaultAgeWeeks == null ? null
					: Range.of( defaultAgeWeeks, maximumAgeWeeks,
							TimeUnits.WEEK );
		}

		@Override
		public Range<ComparableQuantity<Time>> ageRangeSpecial()
		{
			return this.ageRangeSpecial;
		}

		@Override
		public Range<ComparableQuantity<Time>> ageRangeNormal()
		{
			return this.ageRangeNormal;
		}

		@Override
		public int bit()
		{
			return this.bit;
		}

		@Override
		public VaxRegimen regimen()
		{
			return INSTANCE;
		}
	}

	private static final RVP INSTANCE = new RVP();

	public static final RVP instance()
	{
		return INSTANCE;
	}

	private RVP()
	{
		// singleton
	}

	@Override
	public boolean isCompliant( final int vaxStatus )
	{
		return BMR.DOSE2.isSet( vaxStatus ) || BMR.DOSE3.isSet( vaxStatus );
	}

	@Override
	public BMR nextRegular( final int vaxStatus, final Quantity<Time> age )
	{
		final ComparableQuantity<Time> ageC = QuantityUtil.valueOf( age );
		for( BMR dose : BMR.values() )
			if( dose.ageRangeNormal() != null && !dose.isSet( vaxStatus )
					&& !dose.ageRangeNormal().lt( ageC ) )
				return dose;
		return null;
	}

	@Override
	public BMR nextSpecial( final int vaxStatus, final Quantity<Time> age )
	{
		final ComparableQuantity<Time> ageC = QuantityUtil.valueOf( age );
		for( BMR dose : BMR.values() )
			if( !dose.isSet( vaxStatus ) && !dose.ageRangeSpecial().lt( ageC ) )
				return dose;
		return null;
	}

	private Range<ComparableQuantity<Time>> ageRange = null;

	@Override
	public Range<ComparableQuantity<Time>> decisionAgeRange()
	{
		if( this.ageRange == null )
		{
			Extreme<ComparableQuantity<Time>> lower = Extreme
					.lower( QuantityUtil.valueOf( 3, TimeUnits.WEEK ), true ),
					upper = null;
			for( BMR dose : BMR.values() )
				if( dose.ageRangeNormal() != null )
				{
					upper = upper == null ? dose.ageRangeNormal().getUpper()
							: Compare.max( upper,
									dose.ageRangeNormal().getUpper() );
				}
			this.ageRange = Range.of( lower, upper );
		}
		return this.ageRange;
	}

}
