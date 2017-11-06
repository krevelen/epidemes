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

/**
 * {@link HouseholdComposition} the Java contract
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface HouseholdComposition
{

	/** @return {@code true} for aggregates */
	boolean aggregate();

	/** @return the number of adults incl. (step/adoptive) parents */
	int adultCount();

	/** @return {@code true} for a registered (married/cohabiting) couple */
	default boolean couple()
	{
		return adultCount() == 2;
	}

	/** @return {@code true} for a registered (married/cohabiting) couple */
	boolean registered();

	/** @return the number of children, incl. step/adopted, excl. foster */
	int childCount();

	/** @return {@code true} if more than {@link #kids} are allowed */
	boolean more();

	/** @return the new {@link HouseholdComposition} */
	HouseholdComposition plusAdult();

	/** @return the new {@link HouseholdComposition} */
	HouseholdComposition minusAdult();

	/** @return the new {@link HouseholdComposition} */
	HouseholdComposition plusChild();

	/** @return the new {@link HouseholdComposition} */
	HouseholdComposition minusChild();

}