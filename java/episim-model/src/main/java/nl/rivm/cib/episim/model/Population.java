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

import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import io.coala.time.x.Instant;
import io.coala.time.x.TimeSpan;
import rx.Observable;
import rx.Subscription;

/**
 * {@link Population} follows common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modelling of infectious disease</a>
 * 
 * <table>
 * <tr>
 * <th>&mu;</th>
 * <td>Average death rate</td>
 * </tr>
 * <tr>
 * <th>B</th>
 * <td>Average birth rate</td>
 * </tr>
 * <tr>
 * <th>M</th>
 * <td>Passively immune infants</td>
 * </tr>
 * <tr>
 * <th>S</th>
 * <td>Susceptibles</td>
 * </tr>
 * <tr>
 * <th>E</th>
 * <td>Exposed individuals in the latent period</td>
 * </tr>
 * <tr>
 * <th>I</th>
 * <td>Infectives</td>
 * </tr>
 * <tr>
 * <th>R</th>
 * <td>Recovered with immunity</td>
 * </tr>
 * <tr>
 * <th>N</th>
 * <td>Total population</td>
 * </tr>
 * </table>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Population
{
	// TODO apply yearly birth rates (per mother age category?)

	// TODO apply yearly survival rates (per age category?)

	Subscription subscribeTo( Observable<TransitionEvent> stageStream );

	Map<Infection, InfectionMetrics> getDiseaseMetrics();

	Amount<Dimensionless> getTotalNumber();

	interface InfectionMetrics
	{
		Infection getInfection();

		Instant getTime();

		Occurrence getOccurrence();

		Amount<Dimensionless> getSusceptiblesNumber();

		Amount<Dimensionless> getInfectivesNumber();

		Amount<Dimensionless> getRemovedsNumber();

		/**
		 * @return the number of {@link TransmissionEvent}s
		 */
		Amount<Dimensionless> getEffectiveContactsNumber();

		/**
		 * @return &beta; = Effective contact rate (=
		 *         {@link #getEffectiveContactsNumber()} per {@link TimeSpan})
		 */
		Amount<Frequency> getEffectiveContactRate();
	}
}
