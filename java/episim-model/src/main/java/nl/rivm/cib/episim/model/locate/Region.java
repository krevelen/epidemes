package nl.rivm.cib.episim.model.locate;

import java.util.Map;

import javax.measure.quantity.Area;

import org.jscience.geography.coordinates.LatLong;
import org.jscience.physics.amount.Amount;

import io.coala.json.Wrapper;

/**
 * {@link Region}
 * 
 * @version $Id: e6c9b6b9dc29f3775464a9a9e2400236f22ef50c $
 * @author Rick van Krevelen
 */
public interface Region
{
	Map<RegionType, Region> partOf();

	Amount<Area> surfaceArea();

	Iterable<Place> places();

	default Iterable<Place> getPlacesByDistance( final LatLong origin )
	{
		return Place.sortByDistance( places(), origin );
	}

	/**
	 * {@link RegionType}
	 * 
	 * @version $Id: e6c9b6b9dc29f3775464a9a9e2400236f22ef50c $
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
