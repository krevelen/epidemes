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
 * {@link Condition} represents the {@link Infection} bookkeeping dynamics for
 * an individual {@link Carrier}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public abstract class Condition
{

	/** the current {@link EpidemicCompartment} of this {@link Condition} */
	private EpidemicCompartment compartment;

	/** the current {@link SymptomPhase} of this {@link Condition} */
	private SymptomPhase symptoms;

	/** the current {@link TreatmentStage} of this {@link Condition} */
	private TreatmentStage treatment;

	/**
	 * @return the current {@link EpidemicCompartment} of this {@link Condition}
	 */
	public EpidemicCompartment getCompartment()
	{
		return this.compartment;
	}

	/** @return the current {@link TreatmentStage} of this {@link Condition} */
	public TreatmentStage getTreatmentStage()
	{
		return this.treatment;
	}

	/** @return the current {@link SymptomPhase} of this {@link Condition} */
	public SymptomPhase getSymptomPhase()
	{
		return this.symptoms;
	}

	public abstract Infection getInfection();

	public abstract Carrier getCarrier();

	/**
	 * @return {@code true} iff this {@link Condition} yields seropositive blood
	 *         tests (i.e. after seroconversion, where antibody &gt;&gt;
	 *         antigen), {@code false} otherwise
	 */
	public abstract boolean isSeropositive();

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
		 *         {@link Condition} in specified {@link EpidemicCompartment}
		 */
		Collection<Carrier> filter( Collection<Carrier> carriers,
			EpidemicCompartment stage );

	}

}
