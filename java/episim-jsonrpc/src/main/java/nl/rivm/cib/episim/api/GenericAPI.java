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
package nl.rivm.cib.episim.api;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.coala.json.JsonUtil;
import io.coala.name.AbstractIdentifier;
import io.coala.name.Identifiable;

/**
 * {@link GenericAPI} is the generic API for all exposed components
 * 
 * @version $Id$
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 */
public interface GenericAPI extends Identifiable<GenericAPI.ID>
{

	@SuppressWarnings( "serial" )
	class ID extends AbstractIdentifier<String>
	{

		public static ID valueOf( final String value )
		{
			final ID result = new ID();
			result.setValue( value );
			return result;
		}

		{
			JsonUtil.getJOM().registerModule( new SimpleModule()
			{
				{
					addDeserializer( ID.class, new JsonDeserializer<ID>()
					{

						@Override
						public ID deserialize( final JsonParser p, final DeserializationContext ctxt )
							throws IOException, JsonProcessingException
						{
							return ID.valueOf( p.getValueAsString() );
						}
					} );
					addKeyDeserializer( ID.class, new KeyDeserializer()
					{
						@Override
						public Object deserializeKey( final String key, final DeserializationContext ctxt )
							throws IOException, JsonProcessingException
						{
							return ID.valueOf( key );
						}
					} );
				}
			} );
		}
	}

	/**
	 * @param key the slot or property
	 * @return the current value
	 */
	@JsonAnyGetter
		Object get( String key );

	/**
	 * @param key the key to update
	 * @param value the new value
	 * @return the previous value
	 */
	@JsonAnySetter
		Object set( String key, Object value );

	/** @return known peer addresses (parent, children, services, ...) */
	Map<ID, URI> getAddresses();

	/**
	 * @param id the address owner's {@link ID}
	 * @param uri the new {@link URI}
	 * @return the previous {@link URI}
	 */
	URI setAddress( ID id, URI uri );

}
