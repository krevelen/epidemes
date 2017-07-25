/* $Id: d5df9621f94e0ecc21038f25d41d1bbde2a398dd $
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

import io.coala.json.Attributed;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.reactivex.Observable;

/**
 * {@link ClinicalPhase} is an {@link Id} for clinical/symptomatic phases
 * 
 * @version $Id: d5df9621f94e0ecc21038f25d41d1bbde2a398dd $
 * @author Rick van Krevelen
 */
public interface ClinicalPhase extends Identified<String>
{

	boolean isClinical();

	enum Simple implements ClinicalPhase
	{
		/**
		 * subclinical: no signs or symptoms (not occult if not exposed or
		 * immune)
		 */
		ASYMPTOMATIC,

		/**
		 * signs or symptoms of early onset, e.g. lack of appetite,
		 * fever/hyperthermia (measles), rhinorrhea (measles, flu, cold),
		 * conjuctivitis (measles, flu, cold).
		 */
		PRODROMAL,

		/**
		 * signs or symptoms throughout body, e.g. sepsis, cold, flu,
		 * mononucleosis (Pfeiffer due to the Epstein-Barr herpes virus),
		 * Streptococcal pharyngitis
		 */
		SYSTEMIC,

		/**
		 * signs or symptoms near recovery, e.g.
		 * <a href="https://en.wikipedia.org/wiki/Reye_syndrome">Reye's
		 * syndrome</a> following influenza recovery
		 */
		POSTDROMAL,

		//
		;

		@Override
		public String id()
		{
			return name();
		}

		@Override
		public boolean isClinical()
		{
			return this != ASYMPTOMATIC;
		}

	}

	public interface Attributable<THIS extends Attributable<?>>
		extends Attributed.Publisher
	{
		/** propertyName matching bean's getter/setter names */
		String CLINICAL_PHASE_PROPERTY = "phase";

		ClinicalPhase getPhase();

		void setPhase( ClinicalPhase phase );

		@SuppressWarnings( "unchecked" )
		default THIS with( final ClinicalPhase phase )
		{
			setPhase( phase );
			return (THIS) this;
		}

		default Observable<ClinicalPhase> emitPhase()
		{
			return emitChanges( CLINICAL_PHASE_PROPERTY, ClinicalPhase.class );
		}
	}

}