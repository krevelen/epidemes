/* $Id: 50e413952a4dbeea9e3a19b9a9fee08f4c586b27 $
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
import java.util.Map;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import io.coala.random.RandomDistribution;

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
 * @version $Id: 50e413952a4dbeea9e3a19b9a9fee08f4c586b27 $
 * @author Rick van Krevelen
 */
public interface Infection
{

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

	Unit<Frequency> DAILY = NonSI.DAY.inverse().asType( Frequency.class );

	/**
	 * The force of infection (denoted &lambda;) is the rate ({@link Amount} of
	 * {@link Frequency}) at which a secondary susceptible individual acquires
	 * this infectious disease from primary infectives. It is directly
	 * proportional to the effective transmission rate &beta; and gives the
	 * number of new infections given a number of infectives and the average
	 * duration of exposures (see
	 * <a href="https://en.wikipedia.org/wiki/Force_of_infection">wikipedia</a>
	 * ), here with calibration to specific circumstances:
	 * 
	 * @param location the {@link Location} of contact
	 * @param infectives the primary infective {@link Carrier}s in contact with
	 *            their respective {@link ContactIntensity}
	 * @param susceptible the secondary susceptible {@link Carrier}
	 * @param duration the {@link Duration} {@link Amount} of contact
	 * @return the {@link Frequency} {@link Amount} of infection acquisition
	 */
	Amount<Frequency> getForceOfInfection( Location location,
		Map<Carrier, ContactIntensity> infectives, Carrier susceptible );

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

	/**
	 * {@link Simple} implements an {@link Infection} with a simple force of
	 * infection that is drawn from some {link RandomDistribution} independent
	 * of the relations between infective and susceptible {@link Carrier}s,
	 * their current {@link Condition}s, or the contact {@link Location} (and
	 * {@link TransmissionRoute}s)
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Infection
	{
		private final RandomDistribution<Amount<Frequency>> forceDist;

		public Simple( final Amount<Frequency> constantForce )
		{
			this.forceDist = RandomDistribution.Util
					.asConstant( constantForce );
		}

		public Simple( final RandomDistribution<Amount<Frequency>> forceDist )
		{
			this.forceDist = forceDist;
		}

		@Override
		public Amount<Frequency> getForceOfInfection( final Location location,
			final Map<Carrier, ContactIntensity> infectives,
			final Carrier susceptible )
		{
			Amount<Frequency> force = this.forceDist.draw();
			Amount<Frequency> result = Amount.valueOf( 0, DAILY );
			for( ContactIntensity intensity : infectives.values() )
				result = result.plus( force.times( intensity.getFactor() ) );
			return result.times( infectives.size() );
		}
	}
}
