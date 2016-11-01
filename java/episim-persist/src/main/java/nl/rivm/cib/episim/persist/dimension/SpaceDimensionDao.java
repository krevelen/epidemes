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

import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.dao.BuurtDao;
import nl.rivm.cib.episim.persist.dao.GemeenteDao;
import nl.rivm.cib.episim.persist.dao.GgdDao;
import nl.rivm.cib.episim.persist.dao.LandsdeelDao;
import nl.rivm.cib.episim.persist.dao.PlaceDao;
import nl.rivm.cib.episim.persist.dao.ProvincieDao;
import nl.rivm.cib.episim.persist.dao.WijkDao;

/**
 * {@link SpaceDimensionDao} is a data access object for the location dimension
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@Entity( name = "DIM_LOCATION" )
public class SpaceDimensionDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected long id;

	@Embedded
	protected PlaceDao place;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "WIJK", nullable = false, updatable = false )
	protected WijkDao wijk;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "BRT", nullable = false, updatable = false )
	protected BuurtDao buurt;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "GEM", nullable = false, updatable = false )
	protected GemeenteDao gemeente;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "PROV", nullable = false, updatable = false )
	protected ProvincieDao provincie;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "DEEL", nullable = false, updatable = false )
	protected LandsdeelDao landsdeel;

	@ManyToOne( fetch = FetchType.LAZY, cascade = CascadeType.PERSIST )
	@JoinColumn( name = "GGD", nullable = false, updatable = false )
	protected GgdDao ggd;

	public static SpaceDimensionDao of( final EntityManager em,
		final Place location )
	{
		final SpaceDimensionDao result = new SpaceDimensionDao();
		result.place = PlaceDao.of( em, location );
		result.wijk = WijkDao.of( location.zipCode() );
		result.buurt = BuurtDao.of( location.zipCode() );
		result.gemeente = GemeenteDao.of( location.zipCode() );
		result.provincie = ProvincieDao.of( location.zipCode() );
		result.landsdeel = LandsdeelDao.of( location.zipCode() );
		result.ggd = GgdDao.of( location.zipCode() );
		em.persist( result );
		return result;
	}

}
