package nl.rivm.cib.episim.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;

import org.jscience.geography.coordinates.LatLong;
import org.jscience.physics.amount.Amount;

import io.coala.json.Wrapper;
import static io.coala.math.MeasureUtil.angularDistance;

/**
 * {@link Region}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Region
{
	Map<RegionType, Region> getParents();

	Population getPopulation();

	Amount<Area> getArea();

	Iterable<Place> getPlaces();

	default Iterable<Place> getPlacesByDistance( final LatLong pos )
	{
		final List<Place> result = new ArrayList<>();
		for( Place place : getPlaces() )
			result.add( place );

		Collections.sort( result, ( final Place o1, final Place o2 ) ->
		{
			final LatLong p1 = o1.getCentroid();
			final LatLong p2 = o2.getCentroid();
			if( p1 == p2 || Arrays.equals( p1.getCoordinates(),
					p2.getCoordinates() ) )
				return 0;
			final Amount<Angle> d1 = angularDistance( pos, p1 );
			final Amount<Angle> d2 = angularDistance( pos, p2 );
			return d1.approximates( d2 ) ? 0 : d1.compareTo( d2 );
		} );
		return result;
	}

	default Amount<?> getPopulationDensity()
	{
		return getPopulation().getSize().divide( getArea() )
				.to( Units.PER_KM2 );
	}

	/**
	 * {@link RegionType}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	interface RegionType extends Wrapper<String>
	{

		/** territory, zone, province, municipality, city, neighborhood */
		RegionType STATE = Util.valueOf( "state", RegionType.class );

		/** GGD/COROP-regions */
		RegionType HEALTH = Util.valueOf( "health", RegionType.class );

		//	RegionType PARTY = Util.valueOf( "party", RegionType.class );

		//	RegionType RELIGION = Util.valueOf( "religion", RegionType.class );

	}
}
