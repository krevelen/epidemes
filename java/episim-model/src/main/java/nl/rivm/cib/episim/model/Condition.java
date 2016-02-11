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

import java.util.Collection;
import java.util.Comparator;

/**
 * {@link Condition}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Condition
{

	boolean isSusceptible();

	boolean isInfective();

	boolean isRemoved();
	
	boolean isSymptomatic();

	/**
	 * {@link ConditionComparator}
	 * 
	 * @param <C> the concrete type of {@link Carrier} being compared
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	interface ConditionComparator extends Comparator<Carrier>
	{

		/**
		 * @param carriers the {@link Collection} of {@link Carrier}s to filter
		 * @return the subset of {@link Carrier}s with susceptible
		 *         {@link Condition}
		 */
		Collection<Carrier> getSusceptibles( Collection<Carrier> carriers );

		/**
		 * @param carriers the {@link Collection} of {@link Carrier}s to filter
		 * @return the subset of {@link Carrier}s with infective
		 *         {@link Condition}
		 */
		Collection<Carrier> getInfectives( Collection<Carrier> carriers );

		/**
		 * @param carriers the {@link Collection} of {@link Carrier}s to filter
		 * @return the subset of {@link Carrier}s with immune {@link Condition}
		 */
		Collection<Carrier> getRemoveds( Collection<Carrier> carriers );

	}

}
