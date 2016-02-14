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
 * {@link Condition} represents the {@link Infection} dynamics of an individual
 * {@link Carrier}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Condition
{

	/**
	 * @param event a observed {@link ContactEvent} that may change this
	 *            {@link Condition}
	 */
	void on( ContactEvent event );

	/**
	 * @param stage the {@link Stage} to test
	 * @return {@code true} if this {@link Condition} currently matches
	 *         specified {@link Stage}, {@code false} otherwise
	 */
	boolean is( Stage stage );

	/**
	 * @return {@code true} iff this {@link Condition} is during prophylactic
	 *         treatment (e.g. PrEP or PEP), {@code false} otherwise
	 */
	boolean isProphylactic();

	/**
	 * @return {@code true} iff this {@link Condition} yield seropositive blood
	 *         tests (i.e. after seroconversion, where antibody &gt;&gt;
	 *         antigen), {@code false} otherwise
	 */
	boolean isSeropositive();

	/**
	 * useful in behavior-driven transmission among symptom-observing humans
	 * 
	 * @return {@code true} if this {@link Condition} shows apparent noticeable
	 *         symptoms, {@code false} otherwise (i.e. inapparent, silent,
	 *         subclinical, occult)
	 */
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
		 * @return the subset of specified {@link Carrier}s with
		 *         {@link Condition} in specified {@link Stage}
		 */
		Collection<Carrier> filter( Collection<Carrier> carriers, Stage stage );

	}

}
