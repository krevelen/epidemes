package nl.rivm.cib.episim.persist.fact;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.joda.time.DateTime;

import io.coala.time.Duration;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.disease.infection.ContactEvent;
import nl.rivm.cib.episim.model.disease.infection.TransmissionRoute;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.dao.TransmissionSpaceDao;
import nl.rivm.cib.episim.persist.dimension.ActorDimensionDao;
import nl.rivm.cib.episim.persist.dimension.PathogenDimensionDao;
import nl.rivm.cib.episim.persist.dimension.TimeDimensionDao;

/**
 * {@link ContactFactDao} is a data access object for the location dimension
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity //( name = PersistenceConfig.TRANSMISSION_FACT_ENTITY )
public class ContactFactDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@ManyToOne
	@JoinColumn( name = "BEGIN", nullable = false, updatable = false )
	protected TimeDimensionDao begin;

	@ManyToOne
	@JoinColumn( name = "END", nullable = false, updatable = false )
	protected TimeDimensionDao end;

	@Column( name = "DURATION" )
	protected Long duration;

	@Column( name = "ROUTE" )
	protected String route;

	@Column( name = "SPACE" )
	protected TransmissionSpaceDao space;

	@Column( name = "INFECTION" )
	protected PathogenDimensionDao infection;

	@Column( name = "PRIMARY" )
	protected ActorDimensionDao primary;

	@Column( name = "SECONDARY" )
	protected ActorDimensionDao secondary;

	public ContactEvent toContactEvent( final Scheduler scheduler,
		final DateTime offset )
	{
		return ContactEvent.of( this.begin.toInstant( offset ),
				Duration.of( this.duration, TimeUnits.MILLIS ),
				this.space.toSpace( scheduler ),
				TransmissionRoute.of( this.route ), null, // this.primary.toIndividual(),
				null // this.secondary.toIndividual()
		);
	}

	public static ContactFactDao of( final EntityManager em,
		final ZonedDateTime offset, final ContactEvent event )
	{
		final ContactFactDao result = new ContactFactDao();
		result.begin = TimeDimensionDao.of( event.getStart(), offset );
		result.end = TimeDimensionDao
				.of( event.getStart().add( event.getDuration() ), offset );
		result.duration = event.getDuration().toMillisLong();
		result.route = event.getRoute().unwrap();
		result.space = TransmissionSpaceDao.of( event.getSpace() );
		result.primary = ActorDimensionDao
				.of( event.getPrimaryCondition().host() );
		result.secondary = ActorDimensionDao
				.of( event.getSecondaryCondition().host() );
		em.persist( result.begin );
		em.persist( result.end );
		em.persist( result.space );
		em.persist( result.primary );
		em.persist( result.secondary );
		return result;
	}

}
