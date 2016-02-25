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

import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import nl.rivm.cib.episim.time.Timed;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

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
public interface Infection extends Timed
{

	// FIXME have a Subject mapped separately for each Infection ?
	@SuppressWarnings( "rawtypes" )
	Subject<TransitionEvent, TransitionEvent> transitions = PublishSubject
			.create();

	/**
	 * @param event an {@link Observable} stream of {@link TransitionEvent}s for
	 *            this {@link Infection}
	 */
	@SuppressWarnings( "rawtypes" )
	default Observable<TransitionEvent> emitTransitions()
	{
		final Infection self = this;
		return transitions.filter( new Func1<TransitionEvent, Boolean>()
		{
			@Override
			public Boolean call( final TransitionEvent event )
			{
				return self.equals( event.getCondition().getInfection() );
			}
		} );
	}

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
	 * @return a {@link Collection} of transmission {@link TransmissionRoute}s
	 *         of this {@link Infection}
	 */
	//Collection<TransmissionRoute> getTransmissionRoutes();

	/**
	 * The force of infection (denoted &lambda;) is the rate ({@link Frequency}
	 * {@link Amount}) at which a secondary susceptible individual acquires this
	 * infectious disease from primary infectives. It is directly proportional
	 * to the effective transmission rate &beta; and gives the number of new
	 * infections given a number of infectives and the average duration of
	 * exposures (see
	 * <a href="https://en.wikipedia.org/wiki/Force_of_infection">wikipedia</a>
	 * ), here with calibration to specific circumstances:
	 * 
	 * @param location the {@link Location} of contact
	 * @param infectives the primary infective {@link Carrier}s
	 * @param susceptible the secondary susceptible {@link Carrier}
	 * @param duration the {@link Duration} {@link Amount} of contact
	 * @return the {@link Frequency} {@link Amount} of infection acquisition
	 */
	default Amount<Frequency> getForceOfInfection( Location location,
		Collection<? extends Carrier> infectives, Carrier susceptible,
		Amount<Duration> duration )
	{
		return Amount.valueOf( 0, SI.HERTZ );
	}

	/**
	 * @return the (random) period between {@link Stage#EXPOSED} and
	 *         {@link Stage#INFECTIVE} (i.e. 1 / &epsilon;)
	 */
//	Amount<Duration> drawLatentPeriod();

	/**
	 * @return the (random) period between {@link Stage#INFECTIVE} and
	 *         {@link Stage#RECOVERED} conditions (i.e. 1 / &gamma;)
	 */
//	Amount<Duration> drawInfectiousPeriod();

	/**
	 * @return the (random) period between {@link Stage#RECOVERED} and
	 *         {@link Stage#SUSCEPTIBLE} (i.e. &delta;), or {@code null} for
	 *         infinite (there is no loss of immunity)
	 */
//	Amount<Duration> drawImmunizationPeriod();

	/** @return the (random) period between exposure and first symptoms */
//	Amount<Duration> drawOnsetPeriod();

	/**
	 * @return the (random) window period between exposure and seropositive
	 *         blood test results: immunoglobulin M (IgM) for recent primary
	 *         infections; immunoglobulin G (IgG) for past infection or
	 *         immunization
	 */
//	Amount<Duration> drawSeroconversionPeriod();
}
