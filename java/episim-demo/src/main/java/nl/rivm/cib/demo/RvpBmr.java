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

import javax.measure.quantity.Time;

import io.coala.math.Extreme;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.time.TimeUnits;
import io.coala.util.Compare;
import nl.rivm.cib.demo.DemoModel.Medical.VaxDose;
import tec.uom.se.ComparableQuantity;

/**
 * {@link RvpBmr}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum RvpBmr implements VaxDose
{
	/**
	 * e.g. BMR0-1 (applied 6-12 months old) no invite (eg. holiday, outbreak)
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

	private final Range<ComparableQuantity<Time>> ageRangeOptional;

	private final Range<ComparableQuantity<Time>> ageRangeDefault;

	private RvpBmr( final Integer defaultAgeWeeks,
		final Integer minimumAgeWeeks, final Integer maximumAgeWeeks )
	{
		this.ageRangeOptional = Range.of( minimumAgeWeeks, maximumAgeWeeks,
				TimeUnits.WEEK );
		this.ageRangeDefault = defaultAgeWeeks == null ? null
				: Range.of( defaultAgeWeeks, maximumAgeWeeks, TimeUnits.WEEK );
	}

	@Override
	public Range<ComparableQuantity<Time>> ageRangeOptional()
	{
		return this.ageRangeOptional;
	}

	@Override
	public Range<ComparableQuantity<Time>> ageRangeDefault()
	{
		return this.ageRangeDefault;
	}

	public int bit()
	{
		return this.bit;
	}

	public boolean isSet( final int v )
	{
		return !isUnset( v );
	}

	public boolean isUnset( final int v )
	{
		return (v & bit()) == 0;
	}

	public int set( final int v )
	{
		return v | bit();
	}

	public int unset( final int v )
	{
		return v & ~bit();
	}

	public static boolean isCompliant( final int vaxStatus )
	{
		return DOSE2.isSet( vaxStatus ) || DOSE3.isSet( vaxStatus );
	}

	/** @return the NIP (default) schedule's next dose */
	public static VaxDose nextDefault( final int vaxStatus,
		final ComparableQuantity<Time> age )
	{
		for( VaxDose dose : values() )
			if( dose.ageRangeDefault() != null && !dose.isSet( vaxStatus )
					&& !dose.ageRangeDefault().lt( age ) )
				return dose;
		return null;
	}

	/** @return the alternative (outbreak, ZRA) schedule's next dose */
	public static VaxDose nextOption( final int vaxStatus,
		final ComparableQuantity<Time> age )
	{
		for( VaxDose dose : values() )
			if( !dose.isSet( vaxStatus ) && !dose.ageRangeOptional().lt( age ) )
				return dose;
		return null;
	}

	private static Range<ComparableQuantity<Time>> OVERALL_AGE_RANGE = null;

	public static Range<ComparableQuantity<Time>> decisionAgeRange()
	{
		if( OVERALL_AGE_RANGE == null )
		{
			Extreme<ComparableQuantity<Time>> lower = Extreme
					.lower( QuantityUtil.valueOf( 3, TimeUnits.WEEK ), true ),
					upper = null;
			for( VaxDose dose : values() )
				if( dose.ageRangeDefault() != null )
				{
					upper = upper == null ? dose.ageRangeDefault().getUpper()
							: Compare.max( upper,
									dose.ageRangeDefault().getUpper() );
				}
			OVERALL_AGE_RANGE = Range.of( lower, upper );
		}
		return OVERALL_AGE_RANGE;
	}
}