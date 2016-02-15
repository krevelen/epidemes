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

import java.util.Collection;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

/**
 * {@link Infection} results in infectious/transmissible/communicable/contagious
 * disease and is caused by pathogen microbes like viruses, bacteria, fungi, and
 * parasites (bacterial, viral, fungal, parasitic) for which respective
 * medications may exist (antibiotics, antivirals, antifungals, and
 * antiprotozoals/antihelminthics)
 * 
 * <p>
 * Some terminology from
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>:
 * <table>
 * <tr>
 * <th>R<sub>0</sub></th>
 * <td>Basic reproduction number</td>
 * </tr>
 * <tr>
 * <th>1 / &epsilon;</th>
 * <td>Average latent period</td>
 * </tr>
 * <tr>
 * <th>1 / &gamma;</th>
 * <td>Average infectious period</td>
 * </tr>
 * <tr>
 * <th>f</th>
 * <td>Average loss of immunity rate of recovered individuals</td>
 * </tr>
 * <tr>
 * <th>&delta;</th>
 * <td>Average temporary immunity period</td>
 * </tr>
 * </table>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Infection
{

//	Unit<Frequency> HOURLY = NonSI.HOUR.inverse().asType( Frequency.class );
//
//	Unit<Frequency> DAILY = NonSI.DAY.inverse().asType( Frequency.class );
//
//	Unit<Frequency> WEEKLY = NonSI.WEEK.inverse().asType( Frequency.class );
//
//	Unit<Frequency> ANNUALLY = NonSI.YEAR.inverse().asType( Frequency.class );

	/**
	 * infection is transmitted via direct/indirect animal-human route, see
	 * http://www.cdc.gov/onehealth/zoonotic-diseases.html
	 */
	//boolean isZoonotic();

	/**
	 * @return {@code true} if this {@link Infection} is opportunistic,
	 *         requiring impairment of host defenses, {@code false} otherwise
	 *         (i.e. primary pathogens with intrinsic virulence)
	 */
	//boolean isOpportunistic();

	/**
	 * useful in behavior-driven transmission among symptom-avoiding hosts
	 * 
	 * @return {@code true} if this {@link Infection} is long-term or chronic,
	 *         {@code false} otherwise (i.e. short-term or acute)
	 */
	//boolean isChronic();

	/**
	 * useful in behavior-driven transmission among symptom-avoiding hosts
	 * 
	 * @return {@code true} if this {@link Condition} is systemic, causing
	 *         sepsis, {@code false} otherwise (i.e. short-term or acute)
	 */
	//boolean isSystemic();

	/**
	 * @return a {@link Collection} of transmission {@link TransmissionRoute}s of this
	 *         {@link Infection}
	 */
	Collection<TransmissionRoute> getRoutes();

	/**
	 * The empirical fraction {@link Amount} of transmissions occurring between
	 * some susceptible and some infective {@link Carrier} of this
	 * {@link Infection} under specific circumstances:
	 * 
	 * @param route the {@link TransmissionRoute} of contact
	 * @param duration the {@link Duration} of contact
	 * @param relation the {@link Relation} between {@link Carrier}s
	 * @param condition the {@link Condition} of the susceptible {@link Carrier}
	 * @return the likelihood or fraction {@link Amount} of transmission
	 */
	Amount<Dimensionless> getTransmissionLikelihood( TransmissionRoute route,
		Amount<Duration> duration, Relation relation, Condition condition );

	/**
	 * @return the (random) period between {@link Stage#EXPOSED} and
	 *         {@link Stage#INFECTIVE} (i.e. 1 / &epsilon;)
	 */
	Amount<Duration> drawLatentPeriod();

	/**
	 * @return the (random) period between {@link Stage#INFECTIVE} and
	 *         {@link Stage#RECOVERED} conditions (i.e. 1 / &gamma;)
	 */
	Amount<Duration> drawInfectiousPeriod();

	/**
	 * @return the (random) period between {@link Stage#RECOVERED} and
	 *         {@link Stage#SUSCEPTIBLE} (i.e. &delta;), or {@code null} for
	 *         infinite (there is no loss of immunity)
	 */
	Amount<Duration> drawImmunizationPeriod();

	/** @return the (random) period between exposure and first symptoms */
	Amount<Duration> drawOnsetPeriod();

	/**
	 * @return the (random) window period between exposure and seropositive
	 *         blood test results: immunoglobulin M (IgM) for recent primary
	 *         infections; immunoglobulin G (IgG) for past infection or
	 *         immunization
	 */
	Amount<Duration> drawSeroconversionPeriod();
}
