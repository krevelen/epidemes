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

import rx.Observable;
import rx.Observer;

/**
 * {@link Disease} or contagious illness is an {@link Observer} of
 * {@link ContactEvent}s which in turn may trigger its transmission by
 * generating {@link ContagionEvent}s.
 * <p>
 * Some terminology from
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modelling of infectious disease</a>
 * 
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
 * @version $Date$
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public interface Disease extends Observer<ContactEvent>
{

	/**
	 * @return an {@link Observable} stream of {@link ContagionEvent}s
	 */
	Observable<ContagionEvent> getTransmission();

	/** see http://www.who.int/mediacentre/factsheets/fs286/en/ */
//	Disease MEASLES;

//	/** see http://www.who.int/mediacentre/factsheets/fs211/en/ */
//	SEASONAL_INFLUENZA(),
//	
//	/** see http://www.who.int/mediacentre/factsheets/fs360/en/ */
//	HUMAN_IMMUNODEFICIENCY_VIRUS
//
//	;
}
