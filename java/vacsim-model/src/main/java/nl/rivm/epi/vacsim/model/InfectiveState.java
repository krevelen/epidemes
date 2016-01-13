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
package nl.rivm.epi.vacsim.model;

/**
 * {@link InfectiveState}
 * 
 * <table>
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
 * <th>&beta;</th>
 * <td>Effective contact rate (= effective contacts per unit time)</td>
 * </tr>
 * <tr>
 * <th>&mu;</th>
 * <td>Average death rate</td>
 * </tr>
 * <tr>
 * <th>B</th>
 * <td>Average birth rate</td>
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
 * <th>R<sub>0</sub></th>
 * <td>Basic reproduction number</td>
 * </tr>
 * <tr>
 * <th>N</th>
 * <td>Total population</td>
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
 * @version $Date$
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public enum InfectiveState
{

	// see https://en.wikipedia.org/wiki/Epidemic_model
	// see https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease

	/** M */
	PASSIVELY_IMMUNE_INFANT,
	
	/** S */
	SUSCEPTIBLE,
	
	/** E (exposed, latent) */
	EXPOSED,
	
	/** I */
	INFECTIVE,
	
	/** R (removed, recovered) */
	RECOVERED_IMMUNE,
	
	;

}
