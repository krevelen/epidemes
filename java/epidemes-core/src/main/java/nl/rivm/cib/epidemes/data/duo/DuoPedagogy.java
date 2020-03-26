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
package nl.rivm.cib.epidemes.data.duo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import nl.rivm.cib.epidemes.data.duo.DuoPrimarySchool.EduCol;

/**
 * @author gebruiker
 *
 */
public enum DuoPedagogy
{
	/**
	 * Protestant (Luther), Gereformeerd/Vrijgemaakt/Evangelistisch (Calvin)
	 */
	REFORMED,

	/** Antroposofisch (Steiner/Waldorf) */
	ALTERNATIVE,

	/** speciaal onderwijs (special needs) */
	SPECIAL,

	/** public, catholic, islamic, mixed */
	OTHERS,

	/** unknown/all */
	ALL,

	;

	private static final Map<String, DuoPedagogy> DUO_CACHE = new HashMap<>();

	public static DuoPedagogy resolveDuo( final EnumMap<EduCol, JsonNode> school )
	{
		final String type = school.get( EduCol.PO_SOORT ).asText();
		final String denom = school.get( EduCol.DENOMINATIE ).asText();
		final String key = type + denom;
		final DuoPedagogy result = DUO_CACHE.computeIfAbsent( key, k ->
		{
			if( //denom.startsWith( "Prot" ) || // 23.1%
			denom.startsWith( "Geref" ) // 1.2%
					|| denom.startsWith( "Evan" ) // 0.1%
			) return REFORMED;

			if( denom.startsWith( "Antro" ) ) // 0.9%
				return ALTERNATIVE;

			if( type.startsWith( "S" ) || type.contains( "s" ) ) return SPECIAL;

			return OTHERS;
		} );
//				System.err.println( type + " :: " + denom + " -> " + result );
		return result;
	}
}