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
package nl.rivm.cib.episim.model;

import java.util.Map;

import io.coala.time.x.Duration;
import nl.rivm.cib.episim.model.Individual.Relation;

/**
 * {@link ContactEvent} generated by some {@link Location} represents the
 * {@link Duration} that a primary infective {@link Individual} shared some
 * {@link Medium} at some {@link Location} with secondary susceptible
 * {@link Individual}s
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface ContactEvent
{
	/**
	 * @return the {@link Medium}
	 */
	Medium getMedium();

	/**
	 * @return a {@link Location} if applicable
	 */
	Location getLocation();

	/**
	 * @return a {@link Duration} if applicable
	 */
	Duration getDuration();

	/**
	 * @return a {@link Map mapping} of {@link Disease} to (infective)
	 *         {@link Condition}
	 */
	Map<Disease, Condition> getInfectiveDiseases();

	/**
	 * @return the primary infective {@link Carrier}
	 */
	Carrier getPrimaryInfective();

	/**
	 * @return the secondary susceptible {@link Carrier}s
	 */
	Map<Carrier, Relation> getSecondarySusceptibles();

}