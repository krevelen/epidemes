/* $Id: 7d10f131de96809298e2af9ae72b548a24a90817 $
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
package nl.rivm.cib.episim.model.locate;

import io.coala.json.DynaBean;
import io.coala.math.LatLong;
import io.coala.name.Id;
import io.coala.name.Identified;

/**
 * {@link Place} is a stationary inert space with a {@link ID} reference
 * 
 * 
 * @version $Id: 7d10f131de96809298e2af9ae72b548a24a90817 $
 * @author Rick van Krevelen
 */
public interface Place extends Identified.Ordinal<Place.ID>
{
	class ID extends Id.Ordinal<String>
	{
		public static ID of( final String value )
		{
			return Util.of( value, new ID() );
		}
	}

	/**
	 * {@link Directory} will retrieve or generate specified {@link Place}
	 */
	interface Directory
	{
		Place lookup( Place.ID id );
	}

	/**
	 * @param id the {@link ID}
	 * @return a {@link Place}
	 */
	static Simple of( final ID id )
	{
		return new Simple( id );
	}

	/**
	 * @param position the (centroid) {@link LatLong} position
	 * @param zip the {@link ZipCode}, if any
	 * @param region the {@link Region}
	 * @return a {@link Place}
	 */
	static Geo of( final ID id, final LatLong position, final Region.ID region,
		final Geography geography )
	{
		// FIXME use concrete implementation in stead. e.g. Attributed.Simple
		return DynaBean.proxyOf( Geo.class ).with( id ).with( position )
				.with( geography ).with( region );
	}

	class Simple extends Identified.SimpleOrdinal<ID> implements Place
	{
		public Simple( final ID id )
		{
			this.id = id;
		}
	}

	interface Geo extends Place, Geometric<Geo>, Geographic<Geo>
	{
		default Geo with( final ID id )
		{
			set( Identified.ID_JSON_PROPERTY, id );
			return this;
		}
	}
}
