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

/**
 * {@link ContactType} used in transmission models of a {@link Disease}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
/**
 * {@link Medium}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum Medium
{
	/** animal-to-person contact (animal bites, waste, ...), e.g. */
	ANIMAL,

	/**  */
	PLACENTA,

	/** e.g. gonorrhea */
	BIRTH,

	/** contact with contaminated blood */
	BLOOD,

	/** close proximity, e.g. cough, sneeze */
	DROPLET_SPREAD,

	/** intimate contact, e.g. HIV */
	GENITAL,

	/** respiratory, e.g. measles */
	AIR,

	/**
	 * insect bites like e.g. (blood-sucking) mosquitos, fleas, and ticks, e.g.
	 * Malaria, Lyme, West-Nile virus
	 */
	VECTOR,

	/** contaminated objects (door knobs etc) */
	OBJECT,

	/** contaminated food and drink (meat, produce, drinking water), e.g. E. coli, C. botulinum, */
	FOOD,

	/** contaminated soil and water, e.g. Hookworm, Legionnaires', */
	WATER,

	/** contaminated soil and water, e.g. Hookworm, Legionnaires', */
	SOIL,

	;

}
