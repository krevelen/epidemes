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
package nl.rivm.cib.episim.model.disease;

import java.util.NavigableMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.exception.Thrower;
import io.coala.json.Attributed;
import io.coala.json.JsonUtil;
import io.coala.time.Proactive;
import io.reactivex.Observable;

/**
 * {@link IllnessTrajectory} specifies the API for illness trajectories as state
 * machines that transition between conditions
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface IllnessTrajectory extends Proactive
{
	String PERIOD_DEFAULT = "const( 1 week )";

	Observable<? extends Condition> emitCondition();

	interface Condition
	{
		class SimpleClinical
			extends Attributed.Publisher.SimpleOrdinal<SimpleClinical>
			implements Condition, ClinicalPhase.Attributable<SimpleClinical>
		{
		}
	}

	/**
	 * {@link Factory} used for {@link IllnessTrajectory}
	 */
	interface Factory
	{
		String TYPE_KEY = "type";

		IllnessTrajectory create( JsonNode config ) throws Exception;

		default NavigableMap<String, IllnessTrajectory>
			createAll( final JsonNode config )
		{
			// array: generate default numbered name
			if( config.isArray() ) return JsonUtil.toMap( (ArrayNode) config,
					i -> String.format( "pathogen%02d", i ), this::create );

			// object: use field names to identify
			if( config.isObject() ) return JsonUtil.toMap( (ObjectNode) config,
					( key, val ) -> create( val ) );

			// unexpected
			return Thrower.throwNew( IllegalArgumentException::new,
					() -> "Invalid pathogen config: " + config );
		}
	}
}
