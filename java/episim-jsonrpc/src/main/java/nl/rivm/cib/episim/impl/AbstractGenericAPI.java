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
package nl.rivm.cib.episim.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import io.coala.json.JsonUtil;
import io.coala.name.AbstractIdentifiable;
import nl.rivm.cib.episim.api.GenericAPI;
import nl.rivm.cib.episim.api.GenericAPI.ID;

/**
 * {@link AbstractGenericAPI}
 * 
 * @version $Id$
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 */
@SuppressWarnings( "serial" )
public abstract class AbstractGenericAPI extends AbstractIdentifiable<ID> implements GenericAPI
{

	/** my addresses */
	private final Map<ID, URI> myAddresses = new HashMap<>();

	/** my properties */
	private final Map<String, Object> myProperties = new HashMap<>();

	@Override
	public String toString()
	{
		return JsonUtil.toString( this );
	}

	@Override
	public Map<ID, URI> getAddresses()
	{
		return this.myAddresses;
	}

	@Override
	public URI setAddress( final ID id, final URI uri )
	{
		return this.myAddresses.put( id, uri );
	}

	@Override
	public Object get( final String key )
	{
		return this.myProperties.get( key );
	}

	@Override
	public Object set( final String key, final Object value )
	{
		return this.myProperties.put( key, value );
	}

	/**
	 * {@link Builder}
	 * 
	 * @version $Id$
	 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
	 * 
	 * @param <T> the concrete {@link AbstractGenericAPI} type
	 * @param <THIS> the concrete {@link Builder} type
	 */
	abstract class Builder<T extends AbstractGenericAPI, THIS extends AbstractGenericAPI.Builder<T, THIS>>
		extends AbstractBuilder<T, THIS>
	{

		/**
		 * {@link Builder} constructor
		 * 
		 * @param result the resulting object being built
		 */
		protected Builder( final T result )
		{
			super( result );
		}

	}
}