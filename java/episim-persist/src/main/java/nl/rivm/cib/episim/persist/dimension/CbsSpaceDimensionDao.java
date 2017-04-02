package nl.rivm.cib.episim.persist.dimension;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.coala.bind.BindableDao;
import io.coala.bind.LocalBinder;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.dao.PlaceDao;
import nl.rivm.cib.episim.persist.dao.RegionDao;

/**
 * {@link CbsSpaceDimensionDao} is a data access object for the location
 * dimension
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( { "hibernateLazyInitializer", "handler" } )
@Entity
@Table( name = "DIM_SPACE" )
public class CbsSpaceDimensionDao extends AbstractDao
	implements BindableDao<Place, CbsSpaceDimensionDao>
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected long id;

	@Embedded
	protected PlaceDao place;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "WIJK", updatable = false )
	protected RegionDao wijk; // code vs pc6

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "BRT", updatable = false )
	protected RegionDao buurt; // code vs pc4

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "GEM",  updatable = false )
	protected RegionDao gemeente;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "COROP", updatable = false )
	protected RegionDao corop;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "GGD", updatable = false )
	protected RegionDao ggd;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "PROV", updatable = false )
	protected RegionDao provincie;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "DEEL",  updatable = false )
	protected RegionDao landsdeel;

	public static CbsSpaceDimensionDao of( final EntityManager em,
		final Place location )
	{
		// TODO resolve regional levels recursively
		final CbsSpaceDimensionDao result = new CbsSpaceDimensionDao();
		result.place = null;//PlaceDao.of( em, location.regions().get( Geography );
		result.wijk = null;//WijkDao.of( location.zipCode() );
		result.buurt = null;//BuurtDao.of( location.zipCode() );
		result.gemeente = null;//GemeenteDao.of( location.zipCode() );
		result.corop = null;//GgdDao.of( location.zipCode() );
		result.ggd = null;//GgdDao.of( location.zipCode() );
		result.provincie = null;//ProvincieDao.of( location.zipCode() );
		result.landsdeel = null;//LandsdeelDao.of( location.zipCode() );
		em.persist( result );
		return result;
	}

	@Override
	public Place restore( final LocalBinder binder )
	{
		// TODO Auto-generated method stub
		return null;
	}

}
