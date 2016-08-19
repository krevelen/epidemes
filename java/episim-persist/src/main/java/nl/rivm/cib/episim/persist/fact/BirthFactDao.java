package nl.rivm.cib.episim.persist.fact;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.joda.time.DateTime;

import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.person.DemographicEvent;
import nl.rivm.cib.episim.model.person.Population.Birth;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.PersistenceConfig;
import nl.rivm.cib.episim.persist.dimension.ActorDimensionDao;
import nl.rivm.cib.episim.persist.dimension.SpaceDimensionDao;
import nl.rivm.cib.episim.persist.dimension.TimeDimensionDao;

/**
 * {@link BirthFactDao} is a data access object for the location dimension
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@Entity( name = PersistenceConfig.TRANSMISSION_FACT_ENTITY )
public class BirthFactDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@ManyToOne
	@JoinColumn( name = "TIME", nullable = false, updatable = false )
	protected TimeDimensionDao time;

	@ManyToOne
	@JoinColumn( name = "PLACE", nullable = false, updatable = false )
	protected SpaceDimensionDao place;

	@Column( name = "PERSON" )
	protected ActorDimensionDao person;

	public DemographicEvent<?> toContactEvent()
	{
		// TODO
		return null;
	}

	public static BirthFactDao of( final EntityManager em,
		final DateTime offset, final Birth<?> event )
	{
		final BirthFactDao result = new BirthFactDao();
		result.time = TimeDimensionDao.of( event.now(), offset );
		result.place = null;
		// FIXME use enterprise facts as (dynamic?) extensible event beans
		result.person = ActorDimensionDao
				.of( (Individual) event.arrivals().toArray()[0] );
		em.persist( result.time );
		if( result.place != null ) em.persist( result.place );
		em.persist( result.person );
		return result;
	}

}
