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
package nl.rivm.cib.episim.model.locate;

import io.coala.name.Id;

/**
 * {@link Geography} refers to a distinct geography of {@link Region regions}
 * with its own topology
 */
public class Geography extends Id.Ordinal<String>
{
	public static Geography of( final String value )
	{
		return Util.of( value, new Geography() );
	}

	/**
	 * the DEFAULT (administrative) geography, e.g. state, territory, zone,
	 * province, municipality, city, neighborhood
	 */
	public static final Geography DEFAULT = of( "admin" );

	/** the HEALTH geography, e.g. GGD */
	public static final Geography HEALTH = of( "health" );

	/** the EMERGENCY (medical, police, military) geography, e.g. COROP, zone */
	public static final Geography EMERGENCY = of( "emergency" );

	/** the RELIGION geography, e.g. synod, diocese, parish */
	public static final Geography RELIGION = of( "reli" );

	/** the ELECTORAL/political geography: constituency, precinct, district */
	public static final Geography ELECTORAL = of( "elect" );

	/** the OPERATIONS geography, e.g. division, territory */
	public static final Geography OPERATIONS = of( "ops" );
}