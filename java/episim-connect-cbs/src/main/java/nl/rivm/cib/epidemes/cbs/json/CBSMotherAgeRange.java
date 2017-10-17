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
package nl.rivm.cib.epidemes.cbs.json;

import java.text.ParseException;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.time.TimeUnits;
import tec.uom.se.ComparableQuantity;

/**
 * {@link CBSMotherAgeRange}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum CBSMotherAgeRange implements CBSJsonProperty
{

	/** */
	Y0_20( "age_0_20", "[0;20)" ),
	/** */
	Y20_25( "age_20_25", "[20;25)" ),
	/** */
	Y25_30( "age_25_30", "[25;30)" ),
	/** */
	Y30_35( "age_30_35", "[30;35)" ),
	/** */
	Y35_40( "age_35_40", "[35;40)" ),
	/** */
	Y40_45( "age_40_45", "[40;45)" ),
	/** */
	Y45_PLUS( "age_45plus", "[45;50]" );// FIXME allow older with same likelihood?

	private final String jsonKey;

	private final Range<Double> cutoffYears;

	private final Range<ComparableQuantity<Time>> cutoff;

	private CBSMotherAgeRange( final String jsonKey, final String value )
	{
		this.jsonKey = jsonKey;
		Range<Double> cutoffYears;
		Range<ComparableQuantity<Time>> cutoff;
		try
		{
			cutoffYears = Range.parse( value, Double.class );
			cutoff = cutoffYears
					.map( v -> QuantityUtil.valueOf( v, TimeUnits.ANNUM ) );
		} catch( final ParseException e )
		{
			e.printStackTrace();
			cutoffYears = Range.infinite();
			cutoff = Range.infinite();
		}
		this.cutoffYears = cutoffYears;
		this.cutoff = cutoff;
	}

	@Override
	public String jsonKey()
	{
		return this.jsonKey;
	}

	public Range<ComparableQuantity<Time>> range()
	{
		return this.cutoff;
	}

	/**
	 * @param age
	 * @return
	 */
	public static CBSMotherAgeRange forAge( final Number age )
	{
		return forAge( age.doubleValue() );
	}

	/**
	 * @param age
	 * @return
	 */
	public static CBSMotherAgeRange forAge( final double age )
	{
		// try shortcut
		final int ordinal = (int) Math.floor( age / 5 );
		if( ordinal >= 0 && ordinal - 3 < values().length )
			return values()[Math.max( 0, ordinal - 3 )];
		if( Y45_PLUS.cutoffYears.contains( age ) ) return Y45_PLUS;
		return null;
	}

	/**
	 * @param age
	 * @return
	 */
	public static CBSMotherAgeRange forAge( final Quantity<Time> age )
	{
		return forAge( age.to( TimeUnits.ANNUM ).getValue().doubleValue() );
	}
}