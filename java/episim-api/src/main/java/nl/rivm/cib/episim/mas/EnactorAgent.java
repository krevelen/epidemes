/* $Id: cabf939af9c7987d68e13813516e6456562b64cd $
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
package nl.rivm.cib.episim.mas;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link EnactorAgent}
 * 
 * @version $Id: cabf939af9c7987d68e13813516e6456562b64cd $
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public interface EnactorAgent
{

	/**
	 * resume local simulator until granted time
	 * 
	 * @param until the time to proceed to locally
	 * @return response code
	 */
	JsonNode grant( JsonNode until );

	/**
	 * write to the connected sink
	 * 
	 * @param query the aggregation query
	 * @return the requested aggregates
	 */
	JsonNode select( JsonNode query );

}
