package nl.rivm.cib.episim.persist.fact;

import java.time.OffsetDateTime;

import javax.measure.Quantity;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.coala.bind.BindableDao;
import io.coala.bind.LocalBinder;
import io.coala.math.QuantityJPAConverter;
import io.coala.time.Duration;
import nl.rivm.cib.episim.model.disease.infection.ContactEvent;
import nl.rivm.cib.episim.model.disease.infection.TransmissionRoute;
import nl.rivm.cib.episim.model.disease.infection.TransmissionSpace;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.dimension.ActorDimensionDao;
import nl.rivm.cib.episim.persist.dimension.IsoTimeDimensionDao;
import nl.rivm.cib.episim.persist.dimension.PathogenDimensionDao;

/**
 * {@link ContactFactDao} is a data access object for the location dimension
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity
@Table( name = "FACT_CONTACT" )
public class ContactFactDao extends AbstractDao
	implements BindableDao<ContactEvent, ContactFactDao>
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@ManyToOne
	@JoinColumn( name = "BEGIN", nullable = false, updatable = false )
	protected IsoTimeDimensionDao begin;

	// redundant, but useful in SQL queries
	@ManyToOne
	@JoinColumn( name = "END", nullable = false, updatable = false )
	protected IsoTimeDimensionDao end;

	@Column( name = "DURATION" )
	@Convert( converter = QuantityJPAConverter.class )
	protected Quantity<?> duration;

	@Column( name = "ROUTE" )
	protected String route;

	@Column( name = "SPACE_ID" )
	protected String space;

	@Column( name = "INFECTION" )
	protected PathogenDimensionDao infection;

	@Column( name = "PRIMARY" )
	protected ActorDimensionDao primary;

	@Column( name = "SECONDARY" )
	protected ActorDimensionDao secondary;

	@Override
	public ContactEvent restore( final LocalBinder binder )
	{
		return ContactEvent.of( this.begin.restore( binder ),
				Duration.of( this.duration ),
				binder.inject( TransmissionSpace.Factory.class ).create(
						this.space ),
				TransmissionRoute.of( this.route ), null, // this.primary.toIndividual(),
				null // this.secondary.toIndividual()
		);
	}

	public static ContactFactDao of( final EntityManager em,
		final ContactEvent event, final OffsetDateTime offset )
	{
		final ContactFactDao result = new ContactFactDao();
		result.begin = IsoTimeDimensionDao.persist( em, event.getStart(),
				offset );
		result.end = IsoTimeDimensionDao.persist( em,
				event.getStart().add( event.getDuration() ), offset );
		result.duration = event.getDuration().toQuantity();
		result.route = event.getRoute().unwrap();
		result.space = event.getSpace().id().unwrap();
//		result.primary = ActorDimensionDao
//				.of( event.getPrimaryCondition().host().id() );
//		result.secondary = ActorDimensionDao
//				.of( event.getSecondaryCondition().host() );
		return result;
	}

}
