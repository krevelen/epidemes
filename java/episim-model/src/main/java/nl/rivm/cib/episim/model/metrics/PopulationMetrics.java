/* $Id: 9dade484b159c8240410d99c1b9d08c6f16e1340 $
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
package nl.rivm.cib.episim.model.metrics;

import javax.measure.quantity.Dimensionless;

import nl.rivm.cib.episim.math.Indicator;
import nl.rivm.cib.episim.model.Infection;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link PopulationMetrics} follows common
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
 * <th>N</th>
 * <td>Total population</td>
 * </tr>
 * </table>
 * 
 * @version $Id: 9dade484b159c8240410d99c1b9d08c6f16e1340 $
 * @author Rick van Krevelen
 */
public interface PopulationMetrics extends Timed
{

	InfectionMetrics metricsOf(Infection infection);

	Indicator<Dimensionless> getTotalPopulationSize();

	Indicator<Dimensionless> getTotalBirths();

	Indicator<Dimensionless> getTotalDeaths();

	// disease burden analysis
	
	// N: deaths per 10^n individuals
	// Indicator<Dimensionless> getTotalMortality()
	// I: incidence (new cases fraction of population per interval)
	// P: prevalence (total cases fraction of population per instant/interval/lifetime)
	// Indicator<Dimensionless> getTotalMorbidity()
	// YLLs = N x L (standard life expectancy at age of death in years)
	// Indicator<Dimensionless> getTotalYearsOfLifeLost()
	// YLDs = I x DW x L or P x DW (almost equivalent when not discounting age etc)
	// Indicator<Dimensionless> getTotalYearsOfHealthyLifeLostDueToDisability()

	// Indicator<Dimensionless> getTotalLifeYears();
	// DALYs = YLL + YLD
	// Indicator<Dimensionless> getTotalDisabilityAdjustedLifeYears();
}
