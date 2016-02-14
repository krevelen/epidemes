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

import io.coala.json.x.Wrapper;

/**
 * {@link Route} used in transmission models of pathogen microbes, see
 * <a href="https://en.wikipedia.org/wiki/Transmission_(medicine)">wikipedia</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Route extends Wrapper<String>
{

	/** respiratory, e.g. measles */
	Route AIRBORNE = Util.valueOf( "airborne", Route.class );

	/** close proximity spread, e.g. cough, sneeze */
	Route DROPLET = Util.valueOf( "droplet", Route.class );

	/**
	 * touch, shared towel, see
	 * <a href="https://en.wikipedia.org/wiki/Contagious_disease">wikipedia</a>
	 */
	Route DIRECT = Util.valueOf( "direct", Route.class );

	/**
	 * contaminated objects, soil, waterborne, foodborne (meat, produce), e.g.
	 * E. coli, C. botulinum, Legionnaires', Hookworm. See
	 * <a href="https://en.wikipedia.org/wiki/Fecal-oral_route">wikipedia</a>
	 */
	Route FECAL_ORAL = Util.valueOf( "fecal-oral", Route.class );

	/**
	 * vaginal or anal sex, e.g. HIV, Chlamydia, Gonorrhea, Hepatitis B,
	 * Syphilis, Herpes, Trichomoniasis, see
	 * <a href="https://en.wikipedia.org/wiki/Sexually_transmitted_infection">
	 * wikipedia</a>
	 */
	Route SEXUAL = Util.valueOf( "sexual", Route.class );

	/** genital-to-mouth contact, e.g. HIV, Herpes simplex */
	Route SEXUAL_ORAL = Util.valueOf( "sexual-oral", Route.class );

	/** kissing, sharing drinking glass or cigarette */
	Route ORAL = Util.valueOf( "oral", Route.class );

	/**
	 * contaminated blood, medical procedures like injection, transplantation,
	 * e.g. HIV
	 */
	Route IATROGENIC = Util.valueOf( "iatrogenic", Route.class );

	/**
	 * direct animal-to-person contact, e.g. bites, scratches, saliva, pus,
	 * urine, faeces
	 */
	Route ANIMAL = Util.valueOf( "animal", Route.class );

	/**
	 * e.g. via placenta/in utero, at birth/perinatal (gonorrhea), or via breast
	 * milk (HIV)
	 */
	Route VERTICAL = Util.valueOf( "vertical", Route.class );

	/**
	 * insect bites like e.g. (blood-sucking) mosquitos, fleas, and ticks, e.g.
	 * Malaria, Lyme, West-Nile virus
	 */
	Route VECTOR_BORNE = Util.valueOf( "vector-borne", Route.class );

}
