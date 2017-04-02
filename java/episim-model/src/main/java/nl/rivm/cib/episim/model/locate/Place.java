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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.coala.math.LatLong;
import io.coala.name.Id;
import io.coala.name.Identified;
import nl.rivm.cib.episim.model.ZipCode;
import nl.rivm.cib.episim.model.disease.infection.TransmissionSpace;

/**
 * {@link Place} is a stationary {@link TransmissionSpace} located in a
 * geographical region and position
 * 
 * 
 * @version $Id: 7d10f131de96809298e2af9ae72b548a24a90817 $
 * @author Rick van Krevelen
 */
public interface Place extends Identified<Place.ID>
{
	class ID extends Id.Ordinal<String>
	{
		public static ID of( final String value )
		{
			return Util.of( value, new ID() );
		}
	}
	
	TransmissionSpace space();

	/** @return the global centroid {@link LatLong} */
	LatLong centroid();

	Map<Geography, Region> regions();

	/**
	 * @return the default/admin region, or first known
	 */
	default Region region()
	{
		return regions() == null || regions().isEmpty() ? null
				: regions().containsKey( Geography.DEFAULT )
						? regions().get( Geography.DEFAULT )
						: regions().values().iterator().next();
	}

	/**
	 * {@link Factory} will retrieve or generate specified {@link Place}
	 */
	interface Factory
	{
		Place get( ID id );
	}

	/**
	 * @param position the (centroid) {@link LatLong} position
	 * @param zip the {@link ZipCode}, if any
	 * @param region the {@link Region}
	 * @return a {@link Place}
	 */
	public static Place of( final ID id, final LatLong position,
		final Region region, final Geography... geographies )
	{
		return of( id, position,
				geographies == null || geographies.length == 0
						? Collections.singletonMap( Geography.DEFAULT, region )
						: Arrays.stream( geographies ).collect(
								Collectors.toMap( g -> g, g -> region ) ) );
	}

	/**
	 * @param id the {@link ID}
	 * @param position the (centroid) {@link LatLong} position
	 * @param region the {@link Region}
	 * @return a {@link Place}
	 */
	public static Place of( final ID id, final LatLong position,
		final Map<Geography, Region> regions )
	{
		return new Simple( id, position, regions );
	}

	class Simple extends Identified.SimpleOrdinal<ID> implements Place
	{
		private LatLong position;
		private Map<Geography, Region> regions;

		public Simple( final ID id, final LatLong position,
			final Map<Geography, Region> regions )
		{
			this.id = id;
			this.position = position;
			this.regions = regions;
		}

		@Override
		public LatLong centroid()
		{
			return this.position;
		}

		@Override
		public Map<Geography, Region> regions()
		{
			return this.regions;
		}

		@Override
		public TransmissionSpace space()
		{
			// TODO Auto-generated method stub
			return null;
		}
	};

	static Iterable<Place> sortByDistance( final Iterable<Place> places,
		final LatLong origin )
	{
		final List<Place> result = new ArrayList<>();
		for( Place place : places )
			result.add( place );

		Collections.sort( result, ( place1, place2 ) ->
		{
			final LatLong p1 = place1.centroid();
			final LatLong p2 = place2.centroid();
			if( p1.getCoordinates().equals( p2.getCoordinates() ) ) return 0;
			return origin.angularDistance( p1 )
					.compareTo( origin.angularDistance( p2 ) );
		} );
		return result;
	}
}
