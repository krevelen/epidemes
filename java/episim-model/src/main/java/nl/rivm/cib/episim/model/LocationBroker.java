package nl.rivm.cib.episim.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.eaio.uuid.UUID;

import io.coala.exception.x.ExceptionBuilder;
import io.coala.name.x.Id;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link LocationBroker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface LocationBroker extends Timed
{
	/**
	 * {@link Registration}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Registration extends Id.Ordinal<UUID>
	{
		public static Registration create()
		{
			return Util.of( new UUID(), new Registration() );
		}
	}

	/**
	 * @param daycare
	 * @param ageCapacities
	 * @return
	 */
	Registration registerDaycare( Location daycare,
		Map<Integer, Integer> ageCapacities );

	Registration findDaycare( Individual individual, Instant expiration,
		Function<Location, Boolean> onSuggestion );

	/**
	 * @param school
	 * @param ageCapacities
	 * @return
	 */
	Registration registerSchool( Location school,
		Map<Integer, Integer> ageCapacities );

	Registration findSchool( Individual individual, Instant expiration,
		Function<Location, Boolean> onSuggestion );

	/**
	 * @param college
	 * @param capacity #students
	 * @return
	 */
	Registration registerCollege( Location college, Integer capacity );

	Registration findCollege( Individual individual, Instant expiration,
		Function<Location, Boolean> onSuggestion );

	/**
	 * @param office the {@link Location} of work
	 * @param capacity #employees
	 * @return
	 */
	Registration registerEmployer( Location office, Integer capacity );

	Registration findEmployment( Individual individual, Instant expiration,
		Function<Location, Boolean> onSuggestion );

	/**
	 * @param hospitality the {@link Location} of logging/park/camping/...
	 * @param capacity #households
	 * @return
	 */
	Registration registerHospitality( Location hospitality, Integer capacity );

	Registration findHospitality( Integer groupSize, Duration stay,
		Instant expiration, Function<Location, Boolean> onSuggestion );

	/**
	 * @param registration
	 */
	void cancel( Registration registration );

	class Simple implements LocationBroker
	{

		private final Scheduler scheduler;

		private Map<Registration, Consumer<Instant>> registrations = new HashMap<>();

		public Simple( final Scheduler scheduler )
		{
			this.scheduler = scheduler;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public void cancel( final Registration registration )
		{
			final Consumer<Instant> deregistrar = this.registrations
					.get( registration );
			if( deregistrar == null ) throw ExceptionBuilder
					.unchecked( "Already deregistered: %s", registration )
					.build();
			deregistrar.accept( now() );
		}

		@Override
		public Registration registerDaycare( final Location daycare,
			final Map<Integer, Integer> ageCapacities )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findDaycare( final Individual individual,
			final Instant expiration,
			final Function<Location, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerSchool( final Location school,
			final Map<Integer, Integer> ageCapacities )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findSchool( final Individual individual,
			final Instant expiration,
			final Function<Location, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerCollege( final Location college,
			final Integer capacity )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findCollege( final Individual individual,
			final Instant expiration,
			final Function<Location, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerEmployer( final Location office,
			final Integer capacity )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findEmployment( final Individual individual,
			final Instant expiration,
			final Function<Location, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerHospitality( final Location hospitality,
			final Integer capacity )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findHospitality( final Integer groupSize,
			final Duration stay, final Instant expiration,
			final Function<Location, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

	}
}
