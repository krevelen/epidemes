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
package nl.rivm.cib.pilot.hh;

import nl.rivm.cib.pilot.json.HHJsonifiable;

/**
 * {@link HHMemberStatus} are SEIR-like compartments differentiating between
 * removed and immunized
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum HHMemberStatus implements HHJsonifiable
{
	SUSCEPTIBLE,

	INFECTIOUS,

	ARTIFICIAL_IMMUNE,

	NATURAL_IMMUNE,

	PASSIVE_IMMUNE,

	REMOVED,

	;

	private String json = null;

	@Override
	public String jsonValue()
	{
		return this.json == null
				? (this.json = name().toLowerCase().replace( '_', '-' ))
				: this.json;
	}
}