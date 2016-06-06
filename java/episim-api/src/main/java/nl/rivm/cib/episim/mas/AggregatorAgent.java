/* $Id: 4c5c9ab3d7cb06790466accd0b398fba7f07c33c $
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
 * {@link AggregatorAgent}
 * 
 * @version $Id: 4c5c9ab3d7cb06790466accd0b398fba7f07c33c $
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public interface AggregatorAgent
{

	/**
	 * add statistical values for aggregation
	 * 
	 * @param statistics the statistical values to aggregate
	 * @return response code
	 */
	JsonNode insert( JsonNode statistics );

	/**
	 * write to the connected sink
	 * 
	 * @param query the aggregation query
	 * @return the requested aggregates
	 */
	JsonNode select( JsonNode query );

}
