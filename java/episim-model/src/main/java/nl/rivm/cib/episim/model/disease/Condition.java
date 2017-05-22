/* $Id: 0d19d6801cb9fefc090fe46d46fc23c2d3afc275 $
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

import io.coala.enterprise.Actor;
import nl.rivm.cib.episim.model.disease.infection.EpidemicCompartment;
import nl.rivm.cib.episim.model.disease.infection.Pathogen;
import nl.rivm.cib.episim.model.disease.infection.Serostatus;

/**
 * {@link Condition} actor roles represent an {@link Afflicted} person's
 * {@link Afflict} (co-)morbidity dynamics, i.e. physiological changes and
 * status due to (combined) disorders, allergies, (immune system response) to
 * {@link Pathogen} microbes, or many other (bio)hazards known in the etiology
 * of disease. {@link Condition} may include attributes such as:
 * <li>epidemic compartment:
 * {@link EpidemicCompartment.Attributable#getCompartment()}
 * <li>medical stage: {@link MedicalStage.Attributable#getStage()}
 * <li>clinical phase: {@link ClinicalPhase.Attributable#getPhase()}
 * <li>serologic state: {@link Serostatus.Attributable#getState()}
 * 
 * @version $Id: 0d19d6801cb9fefc090fe46d46fc23c2d3afc275 $
 * @author Rick van Krevelen
 */
public interface Condition extends Actor<Afflict>
{

	/**
	 * {@link Epidemic} adds a {@link #getCompartment()} attribute to the
	 * {@link Condition} actor/role for dynamics related to contagion
	 */
	interface Epidemic
		extends Condition, EpidemicCompartment.Attributable<Epidemic>
	{

	}

	/**
	 * {@link Medical} adds a {@link #getStage()} attribute to the
	 * {@link Condition} actor/role for dynamics related to medical treatment
	 */
	interface Medical extends Condition, MedicalStage.Attributable<Medical>
	{

	}

	/**
	 * {@link Clinical} adds a {@link #getPhase()} attribute to the
	 * {@link Condition} actor/role for dynamics related to clinical symptoms
	 */
	interface Clinical extends Condition, ClinicalPhase.Attributable<Clinical>
	{

	}

	/**
	 * {@link Serologic} adds a {@link #getState()} attribute to the
	 * {@link Condition} actor/role for dynamics related to the immune system
	 */
	interface Serologic
		extends Condition, Serostatus.Attributable<Serologic>
	{

	}
}
