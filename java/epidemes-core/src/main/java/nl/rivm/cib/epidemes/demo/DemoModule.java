package nl.rivm.cib.epidemes.demo;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.measure.quantity.Time;

import io.coala.log.LogUtil.Pretty;
import io.coala.math.QuantityUtil;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;
import nl.rivm.cib.epidemes.demo.entity.Persons;
import nl.rivm.cib.epidemes.demo.entity.Persons.PersonTuple;
import tec.uom.se.ComparableQuantity;

public interface DemoModule extends Proactive
{
	DemoModule reset() throws Exception;

	Observable<? extends DemoEvent> events();

	default LocalDate dt()
	{
		return dt( now() );
	}

	default LocalDate dt( final Instant t )
	{
		return t.toJava8( scheduler().offset().toLocalDate() );
	}

	default Pretty prettyDate( final Instant t )
	{
		return Pretty.of( () ->
		{
			final ZonedDateTime zdt = scheduler().offset().plus(
					t.to( TimeUnits.MINUTE ).value().longValue(),
					ChronoUnit.MINUTES );
			return QuantityUtil.toScale( t.toQuantity( TimeUnits.DAYS ), 1 )
					+ ";" + zdt.toLocalDateTime() + ";"
					+ zdt.format( DateTimeFormatter.ISO_WEEK_DATE );
		} );
	}

	default ComparableQuantity<Time> ageOf( final PersonTuple pp )
	{
		return QuantityUtil.valueOf(
				now().decimal().subtract( pp.get( Persons.Birth.class ) ),
				scheduler().timeUnit().asType( Time.class ) );
	}
}