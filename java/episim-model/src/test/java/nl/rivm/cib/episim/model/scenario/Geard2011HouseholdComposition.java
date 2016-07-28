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
package nl.rivm.cib.episim.model.scenario;

import java.util.Arrays;
import java.util.function.Function;

/**
 * {@link Geard2011HouseholdComposition}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Geard2011HouseholdComposition
{
	/**
	 * "i j k": a household with i adults (18+), j school-age children (5-17)
	 * and k preschool-age children (<5).
	 */
	Number[] counts = null;

	public static Geard2011HouseholdComposition valueOf( final String values )
	{
		return of( Integer::valueOf,
				Geard2011Config.VALUE_SEP.split( values ) );
	}

	public static <T extends Number> Geard2011HouseholdComposition
		of( final Function<String, T> parser, final String... values )
	{
		final Geard2011HouseholdComposition result = new Geard2011HouseholdComposition();
		if( values != null )
		{
			result.counts = new Number[values.length];
			for( int i = 0; i < values.length; i++ )
				result.counts[i] = parser.apply( values[i] );
		}
		return result;
	}

	@Override
	public String toString()
	{
		return "hh" + Arrays.asList( this.counts );
	}
}