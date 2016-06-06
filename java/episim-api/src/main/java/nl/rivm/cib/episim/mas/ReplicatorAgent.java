/* $Id: 3932e2a6cc6b2b9db954ea3d10c7caad0f4a9104 $
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
 * {@link ReplicatorAgent}
 * 
 * @version $Id: 3932e2a6cc6b2b9db954ea3d10c7caad0f4a9104 $
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public interface ReplicatorAgent
{

	/**
	 * start/resume the simulation
	 * 
	 * @param config replication parameters, e.g. scenario or experiment
	 *            settings
	 * @return response code
	 */
	JsonNode resume( JsonNode config );

	/**
	 * pause the simulation
	 * 
	 * @return response code
	 */
	JsonNode status();

	/**
	 * pause the simulation
	 * 
	 * @return response code
	 */
	JsonNode pause();

}
