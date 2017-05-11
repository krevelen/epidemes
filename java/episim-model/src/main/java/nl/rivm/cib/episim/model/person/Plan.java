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
package nl.rivm.cib.episim.model.person;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;

/**
 * {@link Plan} provides a (weekly) routine or process, e.g. working_parent
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Plan extends Fact
{
	/** {@link Planner} handles {@link Plan} requests */
	interface Planner extends Actor<Plan>
	{

	}
}