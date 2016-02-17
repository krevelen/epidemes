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

/**
 * {@link Carrier} can be a secondary or even a primary host to multiple
 * (possibly asymptomatic) {@link Infection}s, each with their own
 * {@link Condition}, once invaded/exposed in {@link TransmissionEvent}s
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Carrier
{

	/**
	 * @param transmission the observed exposure {@link TransmissionEvent} where
	 *            {@link TransmissionEvent#getSecondaryExposed()} == this
	 *            {@link Carrier}
	 */
	void invade( TransmissionEvent transmission );

	/**
	 * @param infection the queried {@link Infection}
	 * @return the {@link Condition} of this {@link Carrier} with respect to
	 *         specified {@link Infection}
	 */
	Condition getCondition( Infection infection );

}