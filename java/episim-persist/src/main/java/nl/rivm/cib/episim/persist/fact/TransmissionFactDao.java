package nl.rivm.cib.episim.persist.fact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.PersistenceConfig;
import nl.rivm.cib.episim.persist.dimension.LocationDimensionDao;

/**
 * {@link TransmissionFactDao} is a data access object for the location
 * dimension
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@Entity( name = PersistenceConfig.TRANSMISSION_FACT_ENTITY )
public class TransmissionFactDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@ManyToOne
	@JoinColumn( name = "SITE", nullable = false, updatable = false )
	protected LocationDimensionDao site;

	public static TransmissionFactDao of( final EntityManager em,
		final Place site )
	{
		final TransmissionFactDao result = new TransmissionFactDao();
		result.site = LocationDimensionDao.of( em, site );
		em.persist( result.site );
		return result;
	}

}
