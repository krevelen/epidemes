package nl.rivm.cib.episim.persist.dimension;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import io.coala.time.Instant;
import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link TimeDimensionDao} is a data access object for the dimension of time
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@Entity( name = "DIM_TIME" )
public class TimeDimensionDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@Column( name = "SECOND" )
	protected int second;

	@Column( name = "MINUTE" )
	protected int minute;

	@Column( name = "HOUR" )
	protected int hour;

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

	@Column( name = "DAY" )
	protected int day;

	@Temporal( TemporalType.TIMESTAMP )
	@Column( name = "MILLIS", unique = true )
	protected Date millis;

	public Instant toInstant( final DateTime offset )
	{
		return Instant.of( new DateTime( this.millis ), offset );
	}

	public static TimeDimensionDao of( final Instant time,
		final ZonedDateTime offset )
	{
		final DateTime dt = time.toJoda( new DateTime(
				offset.toInstant().toEpochMilli(), DateTimeZone.forTimeZone(
						TimeZone.getTimeZone( offset.getZone() ) ) ) );
		final TimeDimensionDao result = new TimeDimensionDao();
		result.second = dt.getSecondOfMinute();
		result.minute = dt.getMinuteOfHour();
		result.hour = dt.getHourOfDay();
		result.date = dt.getDayOfMonth();
		result.month = dt.getMonthOfYear();
		result.year = dt.getYear();
		result.weekyear = dt.getWeekyear();
		result.week = dt.getWeekOfWeekyear();
		result.millis = dt.toDate();
		return result;
	}

}
