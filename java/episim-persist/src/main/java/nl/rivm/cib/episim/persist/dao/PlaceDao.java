package nl.rivm.cib.episim.persist.dao;

import java.time.OffsetDateTime;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;

import io.coala.bind.LocalBinder;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link PlaceDao} is a data access object for a place (e.g. address, location
 * or position)
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@Embeddable
public class PlaceDao extends AbstractDao
{
	@Column( name = "NAME", unique = true, updatable = false )
	protected String name;

	@Embedded
	protected LatLongDao centroid;

	@ManyToMany( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinTable( name = "PLACE_REGIONS",
		joinColumns =
	{ @JoinColumn( name = "GEOGRAPHY_ID" ) },
		inverseJoinColumns =
	{ @JoinColumn( name = "REGION_ID" ) } )
	@MapKeyColumn( name = "REGIONS" )
	protected Map<String, RegionDao> regions;

	public static PlaceDao of( final EntityManager em, final Place location,
		final OffsetDateTime offset )
	{
		final PlaceDao result = new PlaceDao();
		result.name = location.id().unwrap();
//		result.centroid = LatLongDao.of( location );
//		result.regions = location.regions().entrySet().stream()
//				.collect( Collectors.toMap( e -> e.getKey().unwrap(),
//						e -> RegionDao.persist( em, e.getValue(), offset ) ) );
		em.persist( result );
		return result;
	}

	public Place toPlace( final LocalBinder binder,
		final OffsetDateTime offset )
	{
		return Place.of( Place.ID.of( this.name ) );
	}

}
