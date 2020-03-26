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
package nl.rivm.cib.epidemes.data.cbs;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import io.coala.math.Range;

/**
 * {@link TimeUtil}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class TimeUtil
{

	private TimeUtil()
	{
		// singleton constructor
	}

	/** time zone used for converting local dates in the Dutch census data */
	public static final ZoneId NL_TZ = ZoneId.of( "CET" );

	/**
	 * @param offset
	 * @param dayCounts
	 * @param range the offset range, or {@code null} for all available
	 * @return
	 */
	public static NavigableMap<LocalDate, Integer> indicesFor(
		final LocalDate offset, final List<Integer> dayCounts,
		final Range<LocalDate> range )
	{
		final NavigableMap<LocalDate, Integer> result = new TreeMap<>();
		if( range.lt( offset ) ) // range ends before data offset
			result.put( offset, 0 );
		else
		{
			LocalDate current = offset;
			for( int i = 0; i < dayCounts.size(); current = current
					.plusDays( dayCounts.get( i++ ) ) )
				if( range.contains( current ) ) result.put( current, i );
			if( result.isEmpty() ) // range starts after last 
				result.put( current, dayCounts.size() - 1 );
		}
		return result;
	}

	/**
	 * @param offsets
	 * @param range the offset range, or {@code null} for all available
	 * @return
	 */
	public static NavigableMap<LocalDate, Integer> indicesFor(
		final List<LocalDate> offsets, final Range<LocalDate> range )
	{
		if( offsets.isEmpty() ) return Collections.emptyNavigableMap();
		final NavigableMap<LocalDate, Integer> result = new TreeMap<>();

		final int last = offsets.size() - 1;
		if( range.lt( offsets.get( 0 ) ) ) // range ends before data offset
			result.put( offsets.get( 0 ), 0 );
		else if( range.gt( offsets.get( last ) ) ) // range starts after last 
			result.put( offsets.get( last ), last );
		else
			for( int i = 0, n = offsets.size() - 1; i < n; i++ )
				if( range.overlaps(
						Range.of( offsets.get( i ), offsets.get( i + 1 ) ) ) )
					result.put( offsets.get( i ), i );

		return result;
	}
}
