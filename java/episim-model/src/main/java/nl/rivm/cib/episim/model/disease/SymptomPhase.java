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

import io.coala.name.Id;

/**
 * {@link SymptomPhase} is an {@link Id} for clinical/symptomatic phases
 * 
 * @version $Id: d5df9621f94e0ecc21038f25d41d1bbde2a398dd $
 * @author Rick van Krevelen
 */
public class SymptomPhase extends Id.Ordinal<String>
{
	/** subclinical: no signs or symptoms (not occult if not exposed or immune) */
	public static final SymptomPhase ASYMPTOMATIC = Util
			.valueOf( "asymptomatic", new SymptomPhase() );

	/**
	 * signs or symptoms of early onset, e.g. lack of appetite,
	 * fever/hyperthermia (measles), rhinorrhea (measles, flu, cold),
	 * conjuctivitis (measles, flu, cold).
	 */
	public static final SymptomPhase PRODROMAL = Util.valueOf( "prodromal",
			new SymptomPhase() );

	/**
	 * signs or symptoms throughout body, e.g. sepsis, cold, flu, mononucleosis
	 * (Pfeiffer due to the Epstein-Barr herpes virus), Streptococcal
	 * pharyngitis
	 */
	public static final SymptomPhase SYSTEMIC = Util.valueOf( "systemic",
			new SymptomPhase() );

	/**
	 * signs or symptoms near recovery, e.g.
	 * <a href="https://en.wikipedia.org/wiki/Reye_syndrome">Reye's syndrome</a>
	 * following influenza recovery
	 */
	public static final SymptomPhase POSTDROMAL = Util.valueOf( "postdromal",
			new SymptomPhase() );

}