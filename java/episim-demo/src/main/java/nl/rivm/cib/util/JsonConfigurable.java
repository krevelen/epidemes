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
package nl.rivm.cib.util;

import java.text.ParseException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link JsonConfigurable}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface JsonConfigurable<THIS extends JsonConfigurable<?>>
{

	/**
	 * @param config a {@link JsonNode} configuration
	 * @return this {@link JsonConfigurable} for chaining
	 */
	THIS reset( JsonNode config ) throws ParseException;

	JsonNode config();

	default String stringify()
	{
		return getClass().getSimpleName() + config();
	}

	default boolean fromConfig( final String key, final boolean defaultValue )
	{
		if( config() == null ) return defaultValue;
		final JsonNode node = config().get( key );
		return node.isNumber() ? node.asDouble() > 0
				: node.asBoolean( defaultValue );
	}

	default String fromConfig( final String key, final String defaultValue )
	{
		if( config() == null ) return defaultValue;
		final JsonNode node = config().get( key );
		return node == null || node.isNull() ? defaultValue : node.asText();
	}

}
