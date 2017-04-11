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

import io.coala.enterprise.Fact;
import nl.rivm.cib.episim.model.disease.infection.Pathogen;

/**
 * {@link Disease} interactions represent pathogenesis and disease development
 * between initiator {@link Pathogen} &hArr; executor {@link Afflicted}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Disease extends Fact
{

	/**
	 * infection is transmitted via direct/indirect animal-human route, see
	 * http://www.cdc.gov/onehealth/zoonotic-diseases.html
	 */
	default boolean isZoonotic()
	{
		return false;
	}

	/**
	 * @return {@code true} if this {@link Pathogen} is opportunistic, requiring
	 *         impairment of host defenses, {@code false} otherwise (i.e.
	 *         primary pathogens with intrinsic virulence)
	 */
	default boolean isOpportunistic()
	{
		return false;
	}

	/**
	 * useful in behavior-driven transmission among symptom-avoiding hosts
	 * 
	 * @return {@code true} if this {@link Pathogen} is long-term or chronic,
	 *         {@code false} otherwise (i.e. short-term or acute)
	 */
	default boolean isChronic()
	{
		return false;
	}

}
