package nl.rivm.cib.episim.model.locate;

import javax.measure.Quantity;
import javax.measure.quantity.Area;

import io.coala.json.Wrapper;
import io.coala.math.LatLong;
import io.coala.name.Identified;

/**
 * {@link Region}
 * 
 * @version $Id: e6c9b6b9dc29f3775464a9a9e2400236f22ef50c $
 * @author Rick van Krevelen
 */
public interface Region extends Identified<String>
{
	Region parent();

	Quantity<Area> surfaceArea();

	Iterable<Place> places();

	default Iterable<Place> getPlacesByDistance( final LatLong origin )
	{
		return Place.sortByDistance( places(), origin );
	}

	static Region of( final String name, final Region parent,
		final Quantity<Area> surfaceArea, final Iterable<Place> places )
	{
		return new Region()
		{
			@Override
			public String id()
			{
				return name;
			}

			@Override
			public Region parent()
			{
				return parent;
			}

			@Override
			public Quantity<Area> surfaceArea()
			{
				return surfaceArea;
			}

			@Override
			public Iterable<Place> places()
			{
				return places;
			}

			@Override
			public int hashCode()
			{
				return Identified.hashCode( this );
			}

			@Override
			public boolean equals( final Object that )
			{
				return Identified.equals( this, that );
			}
		};
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
