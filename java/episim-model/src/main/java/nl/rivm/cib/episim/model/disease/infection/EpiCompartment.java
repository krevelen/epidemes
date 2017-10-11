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
package nl.rivm.cib.episim.model.disease.infection;

import io.coala.json.Attributed;
import io.coala.name.Identified;
import io.reactivex.Observable;

/**
 * {@link EpiCompartment} is an extensible identifier for compartments in
 * SIR/SIS/SIRS/SEIS/SEIR/MSIR/MSEIR/MSEIRS models following terminology in
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>, particularly
 * <a href="https://en.wikipedia.org/wiki/Compartmental_models_in_epidemiology">
 * compartmental models</a>, see
 * <a href="https://www.volkskrant.nl/kijkverder/2016/vaccinatie/">some examples
 * (Dutch)</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface EpiCompartment extends Identified<String>
{
	/**
	 * @return {@code true} iff this compartment represents a primary or
	 *         INFECTIVE status
	 */
	boolean isInfective();

	/**
	 * @return {@code true} iff this compartment represents a secondary
	 *         SUSCEPTIBLE status
	 */
	boolean isSusceptible();

	/**
	 * {@link Attributable}
	 */
	interface Attributable<THIS extends Attributable<?>>
		extends Attributed.Reactive
	{
		/** propertyName matching bean's getter/setter names */
		String EPIDEMIC_COMPARTMENT_PROPERTY = "compartment";

		default EpiCompartment getCompartment()
		{
			return (EpiCompartment) properties()
					.get( EPIDEMIC_COMPARTMENT_PROPERTY );
		}

		default void setCompartment( final EpiCompartment compartment )
		{
			set( EPIDEMIC_COMPARTMENT_PROPERTY, compartment );
		}

		@SuppressWarnings( "unchecked" )
		default THIS with( final EpiCompartment compartment )
		{
			setCompartment( compartment );
			return (THIS) this;
		}

		default Observable<EpiCompartment> compartmentEmitter()
		{
			return valueEmitter( EPIDEMIC_COMPARTMENT_PROPERTY,
					EpiCompartment.class );
		}
	}
}