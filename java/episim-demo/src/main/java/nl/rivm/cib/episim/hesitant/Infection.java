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
package nl.rivm.cib.episim.hesitant;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import nl.rivm.cib.episim.pilot.EcosystemScenario.Contagium;

/**
 * T11 {@link Infection} transactions are initiated by some O02
 * {@link Contagium}'s A15 {@link Transmitter}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Infection extends Fact
{
	/** A11 {@link Afflictor} handles T13 {@link Opinion} execution */
	public interface Afflictor extends Actor<Infection>
	{

	}
}