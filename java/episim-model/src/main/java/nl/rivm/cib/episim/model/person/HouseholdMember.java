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

import java.util.Arrays;

import io.coala.math.Tuple;
import io.coala.time.Instant;
import io.coala.time.TimeUnits;

/**
 * {@link HouseholdMember} contains information on a Household member such as
 * relation/position and birth, see e.g.
 * https://www.cbs.nl/nl-nl/onze-diensten/methoden/begrippen?tab=p#id=positie-in-het-huishouden
 */
public class HouseholdMember extends Tuple
{
	public static HouseholdMember of( final Instant birth,
		final ConnectionType.Simple relationType )
	{
		return new HouseholdMember( birth, relationType );
	}

	private HouseholdMember( final Instant birth,
		final ConnectionType.Simple relationType )
	{
		super( Arrays.asList( birth.to( TimeUnits.ANNUM ), relationType ) );
	}

	public Instant birth()
	{
		return (Instant) values().get( 0 );
	}

	public ConnectionType.Simple relationType()
	{
		return (ConnectionType.Simple) values().get( 1 );
	}
}