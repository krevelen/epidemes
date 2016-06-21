/* $Id: 1430209d4f5807f0cad4fd42c34f5d75bf83b9cf $
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
 * {@link ConnectorAgent}
 * 
 * @version $Id: 1430209d4f5807f0cad4fd42c34f5d75bf83b9cf $
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public interface ConnectorAgent
{

	/**
	 * open connection to source and return available (meta)data
	 * 
	 * @param config connect parameters
	 * @return response code, e.g. the available (meta)data
	 */
	//JsonNode open(JsonNode config);

	/**
	 * read from the connected source
	 * 
	 * @param query the import query, e.g. scenario specific ranges
	 * @return the queried data
	 */
	JsonNode read( JsonNode query );

}
