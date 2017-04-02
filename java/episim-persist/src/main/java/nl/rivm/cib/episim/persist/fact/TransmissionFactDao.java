package nl.rivm.cib.episim.persist.fact;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.transaction.Transactional;

import io.coala.bind.BindableDao;
import io.coala.bind.LocalBinder;
import io.coala.time.Instant;
import nl.rivm.cib.episim.model.disease.infection.ContactEvent;
import nl.rivm.cib.episim.model.disease.infection.TransmissionFact;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.dimension.CbsSpaceDimensionDao;
import nl.rivm.cib.episim.persist.dimension.IsoTimeDimensionDao;

/**
 * {@link TransmissionFactDao} is a data access object for the location
 * dimension
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@Entity( name = TransmissionFactDao.ENTITY_NAME )
public class TransmissionFactDao extends AbstractDao
	implements BindableDao<TransmissionFact, TransmissionFactDao>
{
	public static final String ENTITY_NAME = "FACT_TRANSMISSION";

	@Id
	@GeneratedValue
	@Column( name = "PK" )
	protected int pk;

	@ManyToOne
	@JoinColumn( name = "TIME", nullable = false, updatable = false )
	protected IsoTimeDimensionDao time;

	@ManyToOne
	@JoinColumn( name = "SITE", nullable = false, updatable = false )
	protected CbsSpaceDimensionDao site;

	@ManyToOne
	@JoinColumn( name = "CAUSE", nullable = false, updatable = false )
	protected ContactFactDao cause;

	@Transactional // not really
	public static TransmissionFactDao persist( final EntityManager em,
		final TransmissionFact event, final OffsetDateTime offset )
	{
		final TransmissionFactDao result = new TransmissionFactDao();
		result.time = IsoTimeDimensionDao.persist( em, event.now(), offset );
		result.site = CbsSpaceDimensionDao.of( em, event.getPlace() );

		em.persist( result );
		return result;
	}

	@Override
	public TransmissionFact restore( final LocalBinder binder )
	{
		final Instant time = this.time.restore( binder );
		final Place site = this.site.restore( binder );
		final ContactEvent cause = this.cause.restore( binder );
		return TransmissionFact.of( time, site, cause );
	}

}
