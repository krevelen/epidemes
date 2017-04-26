/* $Id: c7fde65ca5b752da73a2422971867884017a49fb $
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
import nl.rivm.cib.episim.model.disease.Afflicted;
import nl.rivm.cib.episim.model.disease.Condition;

/**
 * {@link EpidemicCompartment} is an extensible identifier for compartments in
 * SIR/SIS/SIRS/SEIS/SEIR/MSIR/MSEIR/MSEIRS models following terminology in
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>, particularly
 * <a href="https://en.wikipedia.org/wiki/Compartmental_models_in_epidemiology">
 * compartmental models</a>
 * 
 * @version $Id: c7fde65ca5b752da73a2422971867884017a49fb $
 * @author Rick van Krevelen
 */
public interface EpidemicCompartment extends Identified<String>
{
	/**
	 * @return {@code true} iff this compartment represents an INFECTIVE
	 *         {@link Condition}
	 */
	boolean isInfective();

	/**
	 * @return {@code true} iff this compartment represents a SUSCEPTIBLE
	 *         {@link Condition}
	 */
	boolean isSusceptible();

	/**
	 * @return {@code true} iff this compartment represents an IMMUNE
	 *         {@link Condition}
	 */
	boolean isImmune();

	interface Attributable<THIS extends Attributable<?>> extends Attributed
	{
		/** propertyName matching bean's getter/setter names */
		String EPIDEMIC_COMPARTMENT_PROPERTY = "compartment";

		EpidemicCompartment getCompartment();

		void setCompartment( EpidemicCompartment compartment );

		@SuppressWarnings( "unchecked" )
		default THIS with( final EpidemicCompartment compartment )
		{
			setCompartment( compartment );
			return (THIS) this;
		}

		default Observable<EpidemicCompartment> emitCompartment()
		{
			return emitChanges( EPIDEMIC_COMPARTMENT_PROPERTY,
					EpidemicCompartment.class );
		}
	}

	/**
	 * {@link Simple}
	 */
	enum Simple implements EpidemicCompartment
	{
		/**
		 * a {@link Condition} where a {@link Afflicted} infant is MATERNALLY
		 * DERIVED or PASSIVELY IMMUNE to some {@link Pathogen} (e.g. naturally
		 * due to maternal antibodies in placenta and colostrum, or artificially
		 * induced via antibody-transfer). See
		 * https://www.wikiwand.com/en/Passive_immunity
		 */
		PASSIVE_IMMUNE,

		/**
		 * a {@link Condition} where a {@link Afflicted} is SUSCEPTIBLE to some
		 * {@link Pathogen}
		 */
		SUSCEPTIBLE,

		/**
		 * a {@link Condition} where a {@link Afflicted} is EXPOSED to, LATENT
		 * INFECTED by, or PRE-INFECTIVE of some {@link Pathogen}
		 */
		EXPOSED,

		/**
		 * a {@link Condition} where a {@link Afflicted} is INFECTIVE of some
		 * {@link Pathogen}
		 */
		INFECTIVE,

		/**
		 * a {@link Condition} where a {@link Afflicted} is RECOVERED from, and
		 * IMMUNE to, some {@link Pathogen} or otherwise REMOVED (i.e. dead)
		 */
		RECOVERED,

		/**
		 * a {@link Condition} where a {@link Afflicted} remains non-recovered
		 * CARRIER of some {@link Pathogen}
		 */
//		CARRIER,

		;

		@Override
		public boolean isInfective()
		{
			return equals( INFECTIVE );
		}

		@Override
		public boolean isSusceptible()
		{
			return equals( SUSCEPTIBLE );
		}

		@Override
		public String id()
		{
			return name();
		}

		@Override
		public boolean isImmune()
		{
			return equals( PASSIVE_IMMUNE ) || equals( RECOVERED );
		}
	}
}