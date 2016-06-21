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

import io.coala.json.Wrapper;
import io.coala.json.DynaBean.BeanProxy;

/**
 * {@link TransmissionRoute} used in transmission models of pathogen microbes,
 * see
 * <a href="https://en.wikipedia.org/wiki/Transmission_(medicine)">wikipedia</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@BeanProxy
public interface TransmissionRoute extends Wrapper<String>
{

	/** respiratory, e.g. measles */
	TransmissionRoute AIRBORNE = Util.valueOf( "airborne",
			TransmissionRoute.class );

	/** close proximity spread, e.g. cough, sneeze */
	TransmissionRoute DROPLET = Util.valueOf( "droplet",
			TransmissionRoute.class );

	/**
	 * touch, shared towel, see
	 * <a href="https://en.wikipedia.org/wiki/Contagious_disease">wikipedia</a>
	 */
	TransmissionRoute DIRECT = Util.valueOf( "direct",
			TransmissionRoute.class );

	/**
	 * contaminated objects, soil, waterborne, foodborne (meat, produce), e.g.
	 * E. coli, C. botulinum, Legionnaires', Hookworm. See
	 * <a href="https://en.wikipedia.org/wiki/Fecal-oral_route">wikipedia</a>
	 */
	TransmissionRoute FECAL_ORAL = Util.valueOf( "fecal-oral",
			TransmissionRoute.class );

	/**
	 * vaginal or anal sex, e.g. HIV, Chlamydia, Gonorrhea, Hepatitis B,
	 * Syphilis, Herpes, Trichomoniasis, see
	 * <a href="https://en.wikipedia.org/wiki/Sexually_transmitted_infection">
	 * wikipedia</a>
	 */
	TransmissionRoute SEXUAL = Util.valueOf( "sexual",
			TransmissionRoute.class );

	/** genital-to-mouth contact, e.g. HIV, Herpes simplex */
	TransmissionRoute SEXUAL_ORAL = Util.valueOf( "sexual-oral",
			TransmissionRoute.class );

	/** kissing, sharing drinking glass or cigarette */
	TransmissionRoute ORAL = Util.valueOf( "oral", TransmissionRoute.class );

	/**
	 * contaminated blood, medical procedures like injection, transplantation,
	 * e.g. HIV
	 */
	TransmissionRoute IATROGENIC = Util.valueOf( "iatrogenic",
			TransmissionRoute.class );

	/**
	 * direct animal-to-person contact, e.g. bites, scratches, saliva, pus,
	 * urine, faeces
	 */
	TransmissionRoute ANIMAL = Util.valueOf( "animal",
			TransmissionRoute.class );

	/**
	 * e.g. via placenta/in utero, at birth/perinatal (gonorrhea), or via breast
	 * milk (HIV)
	 */
	TransmissionRoute VERTICAL = Util.valueOf( "vertical",
			TransmissionRoute.class );

	/**
	 * (incl. arthropod vectors causing arthropod-borne arbovirus) insect bites
	 * like e.g. (blood-sucking) mosquitos, fleas, and ticks (tibovirus), e.g.
	 * Malaria, Lyme, West-Nile virus
	 */
	TransmissionRoute VECTOR_BORNE = Util.valueOf( "vector-borne",
			TransmissionRoute.class );

}
