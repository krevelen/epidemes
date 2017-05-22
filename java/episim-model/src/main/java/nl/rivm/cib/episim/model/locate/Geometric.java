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
package nl.rivm.cib.episim.model.locate;

import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.coala.json.Attributed;
import io.coala.math.LatLong;

/**
 * {@link Geometric}
 * 
 * @param <THIS>
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Geometric<THIS> extends Attributed
{
	/** @return the position as {@link LatLong} coordinates */
	LatLong getPosition();

	void setPosition( LatLong position );

	@SuppressWarnings( "unchecked" )
	default THIS with( final LatLong position )
	{
		setPosition( position );
		return (THIS) this;
	}

	@SuppressWarnings( "unchecked" )
	default <T extends Geometric<?>> T nearestTo( final LatLong origin,
		final T other )
	{
		return comparatorFrom( origin ).compare( this, other ) > 0 ? other
				: (T) this;
	}

	@SuppressWarnings( "unchecked" )
	default <T extends Geometric<?>> T farthestFrom( final LatLong origin,
		final T other )
	{
		return comparatorFrom( origin ).compare( this, other ) < 0 ? other
				: (T) this;
	}

	static Comparator<Geometric<?>> comparatorFrom( final LatLong origin )
	{
		return ( place1, place2 ) ->
		{
			final LatLong p1 = place1.getPosition();
			final LatLong p2 = place2.getPosition();
			if( p1.getCoordinates().equals( p2.getCoordinates() ) ) return 0;
			return origin.angularDistance( p1 )
					.compareTo( origin.angularDistance( p2 ) );
		};
	}

	static <T extends Geometric<?>> Stream<T>
		sortByDistance( final Iterable<T> places, final LatLong origin )
	{
		return StreamSupport.stream( places.spliterator(), true )
				.sorted( comparatorFrom( origin ) );
	}
}