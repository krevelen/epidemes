/* $Id: a8bf68b6634dba22075c105a65ea77fc0d8ccf05 $
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
package nl.rivm.cib.episim.model.disease;

import java.util.Map;

import nl.rivm.cib.episim.model.disease.infection.Infection;

/**
 * {@link Afflicted} can be a (secondary or even primary) host to multiple
 * {@link Infection}s, each with their own {@link Condition} dynamics
 * 
 * @version $Id: a8bf68b6634dba22075c105a65ea77fc0d8ccf05 $
 * @author Rick van Krevelen
 */
public interface Afflicted
{

	/**
	 * @param infection the queried {@link Infection}
	 * @return the {@link Condition} of this {@link Carrier} with respect to
	 *         specified {@link Infection}, or {@code null} if susceptible
	 */
	//Condition getCondition( Infection infection );

	/**
	 * @return a {@link Map} of this {@link Afflicted}s {@link Condition}s by
	 *         {@link Disease}
	 */
	Map<Disease, Condition> afflictions();

	default Afflicted with( final Condition condition )
	{
		afflictions().put( condition.disease(), condition );
		return this;
	}

}