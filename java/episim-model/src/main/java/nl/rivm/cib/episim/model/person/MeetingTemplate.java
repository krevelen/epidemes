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
package nl.rivm.cib.episim.model.person;

import java.text.ParseException;

import io.coala.math.Range;
import io.coala.name.Id;
import io.coala.time.Duration;
import io.coala.time.Timing;

/**
 * {@link MeetingTemplate} inspired by Zhang:201:phd
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface MeetingTemplate
{
	/** determines type of venue and participants */
	Purpose purpose();

	/** (daily/weekly/...) recurrence pattern, e.g. "0 0 8 ? * MON-FRI" */
	Timing recurrence();

	/** interruption-completion time span (positive), e.g. "[1 h; 2 h]" */
	Range<Duration> duration();

	static MeetingTemplate of( final String purpose,
		final String recurrence, final String duration ) throws ParseException
	{
		return of( Purpose.of( purpose ), Timing.of( recurrence ),
				Range.parse( duration, Duration.class ) );
	}

	static MeetingTemplate of(
		final Purpose purpose, final Timing recurrence,
		final Range<Duration> duration )
	{
		return new MeetingTemplate()
		{
			@Override
			public Purpose purpose()
			{
				return purpose;
			}

			@Override
			public Timing recurrence()
			{
				return recurrence;
			}

			@Override
			public Range<Duration> duration()
			{
				return duration;
			}
		};
	}

	/**
	 * {@link Routine} categorizes the type of meeting, inspired by
	 * Zhang:2016:phd
	 */
	public static class Purpose extends Id.Ordinal<String>
	{
		/**
		 * career-related, including: (pre/elementary/high)school,
		 * college/university, employment/retirement
		 */
		public static final Purpose OCCUPATIONAL = of(
				"OCCUPATIONAL" );

		/** social events, including: family visits, sports, leisure, ... */
		public static final Purpose SOCIAL = of( "SOCIAL" );

		/** recreational events, including: holidays, vacation, ... */
		public static final Purpose RECESS = of( "RECESS" );

		/** medical events, including: doctor visits, hospitalize, ... */
		public static final Purpose MEDICAL = of( "MEDICAL" );

		/** spiritual events, including: church visit, pilgrimage, ... */
		public static final Purpose SPRITUAL = of(
				"SPRITUAL" );

		public static Purpose of( final String value )
		{
			return Util.of( value, new Purpose() );
		}

	}
}