package nl.rivm.cib.episim.persist.fact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.joda.time.DateTime;

import nl.rivm.cib.episim.model.disease.infection.TransmissionEvent;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.PersistenceConfig;
import nl.rivm.cib.episim.persist.dimension.SpaceDimensionDao;
import nl.rivm.cib.episim.persist.dimension.TimeDimensionDao;

/**
 * {@link TransmissionFactDao} is a data access object for the location
 * dimension
 * 
 * @version $Id$
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
	@JoinColumn( name = "TIME", nullable = false, updatable = false )
	protected TimeDimensionDao time;

	@ManyToOne
	@JoinColumn( name = "SITE", nullable = false, updatable = false )
	protected SpaceDimensionDao site;

	public static TransmissionFactDao of( final EntityManager em,
		final DateTime offset, final TransmissionEvent event )
	{
		final TransmissionFactDao result = new TransmissionFactDao();
		result.time = TimeDimensionDao.of( event.now(), offset );
		result.site = SpaceDimensionDao.of( em, event.getPlace() );
		em.persist( result.time );
		em.persist( result.site );
		return result;
	}

}
