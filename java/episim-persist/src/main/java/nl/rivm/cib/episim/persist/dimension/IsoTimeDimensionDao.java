package nl.rivm.cib.episim.persist.dimension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.measure.Quantity;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import io.coala.bind.BindableDao;
import io.coala.bind.LocalBinder;
import io.coala.math.QuantityJPAConverter;
import io.coala.time.Instant;
import io.coala.util.Comparison;
import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link IsoTimeDimensionDao} is a data access object for the dimension of time
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity
@Table( name = "DIM_TIME" )
public class IsoTimeDimensionDao extends AbstractDao implements
	BindableDao<Instant, IsoTimeDimensionDao>, Comparable<IsoTimeDimensionDao>
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@Temporal( TemporalType.TIMESTAMP )
	@Column( name = "POSIX", unique = true )
	protected Date posix;

	@Column( name = "QUANTITY" )
	@Convert( converter = QuantityJPAConverter.class )
	protected Quantity<?> quantity;

	/** the zone offset */
	@Column( name = "OFFSET" )
	protected int offset;

	/** the nano-of-second */
	@Column( name = "NANO" )
	protected int nano;

	@Column( name = "SECOND" )
	protected int second;

	@Column( name = "MINUTE" )
	protected int minute;

	@Column( name = "HOUR" )
	protected int hour;

	@Column( name = "DAY" )
	protected int day;

	@Column( name = "DATE" )
	protected int date;

	@Column( name = "MONTH" )
	protected int month;

	@Column( name = "YEAR" )
	protected int year;

	@Column( name = "WEEKYEAR" )
	protected int weekyear;

	@Column( name = "WEEK" )
	protected int week;

	@Transactional // not really
	public static IsoTimeDimensionDao persist( final EntityManager em,
		final Instant time, final OffsetDateTime offset )
	{
		final IsoTimeDimensionDao result = new IsoTimeDimensionDao();
		final OffsetDateTime dt = offset.plus( time.toNanosLong(),
				ChronoUnit.NANOS );

		result.quantity = time.toQuantity();
		result.posix = Date.from( dt.toInstant() );//dt.toDate();

		result.offset = dt.getOffset().getTotalSeconds();
		result.nano = dt.getNano();//dt.toDate();
		result.second = dt.getSecond();//joda.getSecondOfMinute();
		result.minute = dt.getMinute();//joda.getMinuteOfHour();
		result.hour = dt.getHour();//joda.getHourOfDay();
		result.date = dt.getDayOfMonth();//joda.getDayOfMonth();
		result.month = dt.getMonthValue();//joda.getMonthOfYear();
		result.year = dt.getYear();//joda.getYear();

		// FIXME deprecate Joda
		final DateTime joda = time.toJoda( new DateTime(
				offset.toInstant().toEpochMilli(), DateTimeZone.forOffsetMillis(
						1000 * offset.getOffset().getTotalSeconds() ) ) );
		result.weekyear = joda.getWeekyear();
		result.week = joda.getWeekOfWeekyear();

		em.persist( result ); // FIXME find or create!
		return em.merge( result );
	}

	@Override
	public Instant restore( final LocalBinder binder )
	{
		return Instant.of( this.quantity );
	}

	@Override
	public int compareTo( final IsoTimeDimensionDao o )
	{
		return Comparison.compare( this.posix, o.posix );
	}

}
