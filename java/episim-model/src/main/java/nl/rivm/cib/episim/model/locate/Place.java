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
import java.util.Collections;
import java.util.List;

import javax.measure.Quantity;

import io.coala.math.LatLong;
import io.coala.math.QuantityUtil;
import nl.rivm.cib.episim.model.ZipCode;
import nl.rivm.cib.episim.model.disease.infection.TransmissionSpace;
import nl.rivm.cib.episim.model.person.Population;
import tec.uom.se.unit.Units;

/**
 * {@link Place} is a stationary {@link TransmissionSpace} located at a
 * geographical position, dimension entity?
 * (wijk/buurt/stad/gemeente/provincie/landsdeel)
 * 
 * @version $Id: 7d10f131de96809298e2af9ae72b548a24a90817 $
 * @author Rick van Krevelen
 */
public interface Place
{

	/** RIVM National Institute for Public Health and the Environment */
	LatLong RIVM_POSITION = LatLong.of( 52.1185272, 5.1868699,
			Units.DEGREE_ANGLE );

	/** the NO_ZIP {@link ZipCode} constant */
	ZipCode NO_ZIP = ZipCode.valueOf( "0000" );

	Region region();

	/** @return the global centroid {@link LatLong} */
	LatLong centroid();

	/** @return the {@link ZipCode} */
	ZipCode zipCode();

	/**
	 * @param position the (centroid) {@link LatLong} position
	 * @param zip the {@link ZipCode}, if any
	 * @param region the {@link Region}
	 * @return a {@link Place}
	 */
	public static Place of( final LatLong position, final ZipCode zip,
		final Region region )
	{
		return new Place()
		{
			@Override
			public LatLong centroid()
			{
				return position;
			}

			@Override
			public ZipCode zipCode()
			{
				return zip;
			}

			@Override
			public Region region()
			{
				return region;
			}
		};
	}

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
			if( p1 == p2 || p1.getCoordinates().equals( p2.getCoordinates() ) )
				return 0;
			return origin.angularDistance( p1 )
					.compareTo( origin.angularDistance( p2 ) );
		} );
		return result;
	}

	interface Inhabited extends Place
	{

		Population<?> population();

		default Quantity<?> populationDensity()
		{
			return QuantityUtil.valueOf( population().members().size() )
					.divide( region().surfaceArea() );
		}

	}
}
