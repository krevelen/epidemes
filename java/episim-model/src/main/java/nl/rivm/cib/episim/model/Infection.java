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

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import io.coala.random.ProbabilityDistribution;
import io.coala.time.x.Duration;

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
 * typical life cycle (compartment transitions):
 * - passively immune
 * - susceptible
 *   - vaccine-infected : latent (incubation-period), infectious, recovered (1-2y), susceptible
 *   - contact-infected : latent (incubation-period)
 *     - asymptomatic, infectious, recovering, removed (2-7y), susceptible
 *     - symptomatic
 *       - mobile, recovering, removed (2-7y), susceptible
 *       - immobilize
 *         - convalescent, removed (2-7y), susceptible
 *         - hospitalize
 *           - convalescent, removed (2-7y), susceptible
 *           - death, removed
 * 
 * @version $Id$
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
	 * @param routes a {@link Collection} of local {@link TransmissionRoute}s
	 * @param infectionPressure the infection pressure as
	 *            {@link ContactIntensity} for each primary infective currently
	 *            in contact
	 * @return the {@link Frequency} {@link Amount} of infection acquisition
	 */
	Amount<Frequency> getForceOfInfection( Collection<TransmissionRoute> routes,
		Collection<ContactIntensity> infectionPressure );

	/**
	 * @return the (random) period between
	 *         {@link EpidemicCompartment.Simple#EXPOSED} and
	 *         {@link EpidemicCompartment.Simple#INFECTIVE} (i.e. 1 / &epsilon;)
	 */
	Duration drawLatentPeriod();

	/**
	 * @return the (random) period between
	 *         {@link EpidemicCompartment.Simple#INFECTIVE} and
	 *         {@link EpidemicCompartment.Simple#RECOVERED} conditions (i.e. 1 /
	 *         &gamma;)
	 */
	Duration drawRecoverPeriod();

	/**
	 * @return the (random) period between
	 *         {@link EpidemicCompartment.Simple#RECOVERED} and
	 *         {@link EpidemicCompartment.Simple#SUSCEPTIBLE} (i.e. &delta;), or
	 *         {@code null} for infinite (there is no loss of immunity)
	 */
	Duration drawWanePeriod();

	/** @return the (random) period between exposure and first symptoms */
	Duration drawOnsetPeriod();

	Duration drawSymptomPeriod();

	/**
	 * @return the (random) window period between exposure and seropositive
	 *         blood test results: immunoglobulin M (IgM) for recent primary
	 *         infections; immunoglobulin G (IgG) for past infection or
	 *         immunization
	 */
//	Duration drawSeroconversionPeriod();

	/**
	 * {@link Simple} implements an {@link Infection} with a simple force of
	 * infection that is drawn from some {link RandomDistribution} independent
	 * of the relations between infective and susceptible {@link Carrier}s,
	 * their current {@link Condition}s, or the contact {@link Place} (and
	 * {@link TransmissionRoute}s)
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Infection
	{
		private final ProbabilityDistribution<Amount<Frequency>> forceDist;

		private final ProbabilityDistribution<Duration> latentPeriodDist;
		private final ProbabilityDistribution<Duration> recoverPeriodDist;
		private final ProbabilityDistribution<Duration> wanePeriodDist;
		private final ProbabilityDistribution<Duration> onsetPeriodDist;
		private final ProbabilityDistribution<Duration> symptomPeriodDist;

		public Simple( final Amount<Frequency> forceConst,
			final Duration latentPeriodConst, final Duration recoverPeriodConst,
			final Duration wanePeriodConst, final Duration onsetPeriodConst,
			final Duration symptomPeriodConst )
		{
			this( ProbabilityDistribution.createDeterministic( forceConst ),
					ProbabilityDistribution
							.createDeterministic( latentPeriodConst ),
					ProbabilityDistribution
							.createDeterministic( recoverPeriodConst ),
					ProbabilityDistribution
							.createDeterministic( wanePeriodConst ),
					ProbabilityDistribution
							.createDeterministic( onsetPeriodConst ),
					ProbabilityDistribution
							.createDeterministic( symptomPeriodConst ) );
		}

		public Simple(
			final ProbabilityDistribution<Amount<Frequency>> forceDist,
			final ProbabilityDistribution<Duration> latentPeriodDist,
			final ProbabilityDistribution<Duration> recoverPeriodDist,
			final ProbabilityDistribution<Duration> wanePeriodDist,
			final ProbabilityDistribution<Duration> onsetPeriodDist,
			final ProbabilityDistribution<Duration> symptomPeriodDist )
		{
			this.forceDist = forceDist;
			this.latentPeriodDist = latentPeriodDist;
			this.recoverPeriodDist = recoverPeriodDist;
			this.wanePeriodDist = wanePeriodDist;
			this.onsetPeriodDist = onsetPeriodDist;
			this.symptomPeriodDist = symptomPeriodDist;
		}

		@Override
		public Amount<Frequency> getForceOfInfection(
			final Collection<TransmissionRoute> routes,
			final Collection<ContactIntensity> infectionPressure )
		{
			Amount<Frequency> result = Amount.valueOf( 0, Units.DAILY );
			Amount<Frequency> force = this.forceDist.draw();
			for( ContactIntensity intensity : infectionPressure )
				result = result.plus( force.times( intensity.getFactor() ) );
			return result;
		}

		@Override
		public Duration drawLatentPeriod()
		{
			return this.latentPeriodDist.draw();
		}

		@Override
		public Duration drawRecoverPeriod()
		{
			return this.recoverPeriodDist.draw();
		}

		@Override
		public Duration drawWanePeriod()
		{
			return this.wanePeriodDist.draw();
		}

		@Override
		public Duration drawOnsetPeriod()
		{
			return this.onsetPeriodDist.draw();
		}

		@Override
		public Duration drawSymptomPeriod()
		{
			return this.symptomPeriodDist.draw();
		}
	}
}
