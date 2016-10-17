package nl.rivm.cib.episim.persist.dao;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link PlaceDao} is a data access object for the location dimension
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Embeddable
public class PlaceDao extends AbstractDao
{
	@Column( name = "NAME", unique = true, updatable = false )
	protected String name;

	@Embedded
	protected LatLongDao centroid;

	@Embedded
	protected ZipCodeDao zip;

	@ManyToOne
	@JoinColumn( name = "REG", nullable = false, updatable = false )
	protected RegionDao region;

	public static PlaceDao of( final EntityManager em, final Place location )
	{
		final PlaceDao result = new PlaceDao();
		result.centroid = LatLongDao.of( location.centroid() );
		result.zip = ZipCodeDao.of( location.zipCode() );
		result.region = RegionDao.of( em, location.region() );
		em.persist( result.region );
		return result;
	}

	public Place toPlace()
	{
		return Place.of( this.centroid.toLatLong(), this.zip.toZipCode(),
				this.region.toRegion() );
	}

}
