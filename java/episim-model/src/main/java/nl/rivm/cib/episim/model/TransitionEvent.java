/* $Id: 2f7141963aaa4c17ee828105bff1527724cab23a $
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
 * {@link TransitionEvent}
 * 
 * @version $Id: 2f7141963aaa4c17ee828105bff1527724cab23a $
 * @author Rick van Krevelen
 */
public class TransitionEvent<T>
{
	/** the {@link Condition} undergoing a {@link TransitionEvent} */
	protected Condition condition;

	/** */
	protected T oldValue;

	/** */
	protected T newValue;

	/**
	 * {@link TransitionEvent} zero-arg bean constructor
	 */
	protected TransitionEvent()
	{
		//
	}

	/** @return the {@link Condition} that is progressing */
	public Condition getCondition()
	{
		return this.condition;
	}

	/** */
	public T getOldValue()
	{
		return this.oldValue;
	}

	/** */
	public T getNewValue()
	{
		return this.newValue;
	}

	@Override
	public String toString()
	{
		return new StringBuffer( getClass().getSimpleName() ).append( '[' )
				.append( getOldValue() ).append( "->" ).append( getNewValue() )
				.append( ']' ).toString();
	}

	public static class CompartmentEvent
		extends TransitionEvent<EpidemicCompartment>
	{

	}

	public static class TreatmentEvent extends TransitionEvent<TreatmentStage>
	{

	}

	public static class SymptomEvent extends TransitionEvent<SymptomPhase>
	{

	}

	public static CompartmentEvent of( final TransmissionEvent transmission,
		final EpidemicCompartment newStage )
	{
		return of( transmission.getContact().getSecondaryCondition(),
				newStage );
	}

	/**
	 * @param condition
	 * @param compartment
	 * @return an {@link CompartmentEvent}
	 */
	public static CompartmentEvent of( final Condition condition,
		final EpidemicCompartment compartment )
	{
		final CompartmentEvent result = new CompartmentEvent();
		result.condition = condition;
		result.oldValue = condition.getCompartment();
		result.newValue = compartment;
		return result;
	}

	/**
	 * @param condition
	 * @param treatment
	 * @return a {@link TreatmentEvent}
	 */
	public static TreatmentEvent of( final Condition condition,
		final TreatmentStage treatment )
	{
		final TreatmentEvent result = new TreatmentEvent();
		result.condition = condition;
		result.oldValue = condition.getTreatmentStage();
		result.newValue = treatment;
		return result;
	}

	/**
	 * @param condition
	 * @param phase
	 * @return a {@link SymptomEvent}
	 */
	public static SymptomEvent of( final Condition condition,
		final SymptomPhase phase )
	{
		final SymptomEvent result = new SymptomEvent();
		result.condition = condition;
		result.oldValue = condition.getSymptomPhase();
		result.newValue = phase;
		return result;
	}
}