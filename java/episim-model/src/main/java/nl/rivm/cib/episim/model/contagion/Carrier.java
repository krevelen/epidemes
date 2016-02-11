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
package nl.rivm.cib.episim.model.contagion;

/**
 * {@link Carrier} follows common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic
 * models</a> and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modelling of infectious disease</a>
 * 
 * <table>
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
public interface Carrier
{
	boolean isSusceptible();

	boolean isExposed();

	boolean isInfective();

	boolean isAsymptomaticInfective();

	boolean isSymptomaticInfective();

	boolean isImmune();

	boolean isPassivelyMaternalImmune();

	boolean isRecoveredImmune();
}