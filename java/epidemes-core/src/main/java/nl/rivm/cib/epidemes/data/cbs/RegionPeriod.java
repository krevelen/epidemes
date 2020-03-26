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
import java.util.Arrays;

import io.coala.math.Tuple;
import nl.rivm.cib.epidemes.model.Region;

/**
 * {@link RegionPeriod} is a two-dimensional key used to localize/actualize
 * distributions. It is a {@link Comparable} {@link Tuple} so the "nearest" key
 * can be determined, such that region {@link String} is compared
 * (alphabetically) before the period {@link LocalDate}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class RegionPeriod extends Tuple
{
	public static RegionPeriod of( final Region.ID regionRef,
		final LocalDate periodRef )
	{
		return new RegionPeriod( regionRef.unwrap(), periodRef );
	}

	public static RegionPeriod of( final String regionRef,
		final LocalDate periodRef )
	{
		return new RegionPeriod( regionRef, periodRef );
	}

	public RegionPeriod( final String regionRef, final LocalDate periodRef )
	{
		super( Arrays.asList( regionRef, periodRef ) );
	}

	public String regionRef()
	{
		return (String) values().get( 0 );
	}

	public LocalDate periodRef()
	{
		return (LocalDate) values().get( 1 );
	}
}