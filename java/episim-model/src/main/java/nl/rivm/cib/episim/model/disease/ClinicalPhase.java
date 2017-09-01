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
package nl.rivm.cib.episim.model.disease;

import io.coala.json.Attributed;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.reactivex.Observable;

/**
 * {@link ClinicalPhase} is an {@link Id} for clinical/symptomatic phases
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface ClinicalPhase extends Identified<String>
{

	/**
	 * @return symptoms can be observed, by self and/or others, laymen and/or
	 *         specialists
	 */
	boolean isApparent();

	/**
	 * @return clinical attention or treatment may be appropriate
	 */
	boolean isClinical();

	enum Simple implements ClinicalPhase
	{
		/**
		 * no disease, pathogen, signs or symptoms (not if never
		 * exposed/invaded)
		 */
		ABSENT( false, false ),

		/**
		 * affected/exposed but non-apparent, possibly 1. dormant (inactive, non
		 * growing); 2. asymptomatic/subclinical (not clinically observable); or
		 * 3. latent/occult (no visual signs or symptoms)
		 */
		LATENT( false, false ),

		/**
		 * (sub)clinical signs or symptoms, possibly still obscure/non-apparent
		 */
		SYMPTOMATIC( true, false ),

		/**
		 * visually apparent signs or symptoms, possibly still subclinical
		 */
		SYMPTOMATIC_APPARENT( true, false ),

		/**
		 * apparent signs or symptoms of early onset, e.g. lack of appetite,
		 * fever/hyperthermia (measles), rhinorrhea (measles, flu, cold),
		 * conjuctivitis (measles, flu, cold).
		 */
		SYMTPOMATIC_PRODROMAL( true, true ),

		/**
		 * apparent signs or symptoms throughout body, e.g. sepsis, cold, flu,
		 * mononucleosis (Pfeiffer due to the Epstein-Barr herpes virus),
		 * Streptococcal pharyngitis
		 */
		SYMTPOMATIC_SYSTEMIC( true, true ),

		/**
		 * apparent signs or symptoms near recovery, e.g.
		 * <a href="https://en.wikipedia.org/wiki/Reye_syndrome">Reye's
		 * syndrome</a> following influenza recovery
		 */
		SYMPTOMATIC_POSTDROMAL( true, true ),

		//
		;

		private final boolean apparent;
		private final boolean clinical;

		private Simple( final boolean apparent, final boolean clinical )
		{
			this.apparent = apparent;
			this.clinical = clinical;
		}

		@Override
		public String id()
		{
			return name();
		}

		@Override
		public boolean isApparent()
		{
			return this.apparent;
		}

		@Override
		public boolean isClinical()
		{
			return this.clinical;
		}

	}

	public interface Attributable<THIS extends Attributable<?>>
		extends Attributed.Publisher
	{
		/** propertyName matching bean's getter/setter names */
		String CLINICAL_PHASE_PROPERTY = "clinical-phase";

		default ClinicalPhase getPhase()
		{
			return (ClinicalPhase) properties().get( CLINICAL_PHASE_PROPERTY );
		}

		default void setPhase( final ClinicalPhase phase )
		{
			set( CLINICAL_PHASE_PROPERTY, phase );
		}

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