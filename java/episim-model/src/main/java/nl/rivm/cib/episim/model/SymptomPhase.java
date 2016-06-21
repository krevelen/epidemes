/* $Id: e09ef6f5bdddf867d6f60848ae7ca3d5ccb0a485 $
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

import io.coala.json.Wrapper;
import io.coala.json.DynaBean.BeanProxy;

/**
 * {@link SymptomPhase} is an extensible identifier for stages of treatment
 * 
 * @version $Id: e09ef6f5bdddf867d6f60848ae7ca3d5ccb0a485 $
 * @author Rick van Krevelen
 */
@BeanProxy
public interface SymptomPhase extends Wrapper<String>
{
	/** no signs or symptoms (not occult if not exposed or immune) */
	SymptomPhase ASYMPTOMATIC = Util.valueOf( "asymptomatic",
			SymptomPhase.class );

	/**
	 * signs or symptoms of early onset, e.g. lack of appetite,
	 * fever/hyperthermia (measles), rhinorrhea (measles, flu, cold),
	 * conjuctivitis (measles, flu, cold).
	 */
	SymptomPhase PRODROMAL = Util.valueOf( "prodromal", SymptomPhase.class );

	/**
	 * signs or symptoms throughout body, e.g. sepsis, cold, flu, mononucleosis
	 * (Pfeiffer due to the Epstein-Barr herpes virus), Streptococcal
	 * pharyngitis
	 */
	SymptomPhase SYSTEMIC = Util.valueOf( "systemic", SymptomPhase.class );

	/**
	 * signs or symptoms near recovery, e.g.
	 * <a href="https://en.wikipedia.org/wiki/Reye_syndrome">Reye's syndrome</a>
	 * following influenza recovery
	 */
	SymptomPhase POSTDROMAL = Util.valueOf( "postdromal", SymptomPhase.class );
}