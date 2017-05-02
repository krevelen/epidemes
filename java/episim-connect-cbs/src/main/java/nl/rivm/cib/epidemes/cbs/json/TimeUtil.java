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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.SortedMap;
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

	/** time zone used for timestamps of local dates in the Dutch census data */
	public static final ZoneId NL_TZ = ZoneId.of( "CET" );

	/**
	 * @param offset
	 * @param dayCounts
	 * @param range the offset range, or {@code null} for all available
	 * @return
	 */
	public static SortedMap<ZonedDateTime, Integer> indicesFor(
		final LocalDate offset, final List<Integer> dayCounts,
		final Range<ZonedDateTime> range )
	{
		return indicesFor( () -> new Iterator<LocalDate>()
		{
			private LocalDate current = offset;
			private int i = 0;

			@Override
			public boolean hasNext()
			{
				return i < dayCounts.size();
			}

			@Override
			public LocalDate next()
			{
				final LocalDate result = this.current;
				this.current = this.current.plusDays( dayCounts.get( i++ ) );
				return result;
			}
		}, range );
	}

	/**
	 * @param offset
	 * @param dayCounts
	 * @param range the offset range, or {@code null} for all available
	 * @return
	 */
	public static SortedMap<ZonedDateTime, Integer> indicesFor(
		final Iterable<LocalDate> offsets, //final List<Integer> dayCounts,
		final Range<ZonedDateTime> range )
	{
		// Java8 Time conversions: http://stackoverflow.com/a/23197731/1418999
		final NavigableMap<ZonedDateTime, Integer> indices = new TreeMap<>();
		int i = 0;
		for( Iterator<LocalDate> it = offsets.iterator(); it.hasNext(); )
			indices.put( it.next().atStartOfDay( NL_TZ ), i++ );

		return range == null ? indices : range.subMap( indices, true );
	}
}
