package nl.rivm.cib.episim.model.scenario;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.eaio.uuid.UUID;

import io.coala.exception.ExceptionBuilder;
import io.coala.name.x.Id;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.Place;
import nl.rivm.cib.episim.time.Scheduler;
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
	
	// TODO health care

	/**
	 * @param daycare
	 * @param ageCapacities
	 * @return
	 */
	Registration registerDaycare( Place daycare,
		Map<Integer, Integer> ageCapacities );

	Registration findDaycare( Individual individual, Instant expiration,
		Function<Place, Boolean> onSuggestion );

	/**
	 * @param school
	 * @param ageCapacities
	 * @return
	 */
	Registration registerSchool( Place school,
		Map<Integer, Integer> ageCapacities );

	Registration findSchool( Individual individual, Instant expiration,
		Function<Place, Boolean> onSuggestion );

	/**
	 * @param college
	 * @param capacity #students
	 * @return
	 */
	Registration registerCollege( Place college, Integer capacity );

	Registration findCollege( Individual individual, Instant expiration,
		Function<Place, Boolean> onSuggestion );

	/**
	 * @param office the {@link Place} of work
	 * @param capacity #employees
	 * @return
	 */
	Registration registerEmployer( Place office, Integer capacity );

	Registration findEmployment( Individual individual, Instant expiration,
		Function<Place, Boolean> onSuggestion );

	/**
	 * @param hospitality the {@link Place} of logging/park/camping/...
	 * @param capacity #households
	 * @return
	 */
	Registration registerHospitality( Place hospitality, Integer capacity );

	Registration findHospitality( Integer groupSize, Duration stay,
		Instant expiration, Function<Place, Boolean> onSuggestion );

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
		public Registration registerDaycare( final Place daycare,
			final Map<Integer, Integer> ageCapacities )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findDaycare( final Individual individual,
			final Instant expiration,
			final Function<Place, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerSchool( final Place school,
			final Map<Integer, Integer> ageCapacities )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findSchool( final Individual individual,
			final Instant expiration,
			final Function<Place, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerCollege( final Place college,
			final Integer capacity )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findCollege( final Individual individual,
			final Instant expiration,
			final Function<Place, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerEmployer( final Place office,
			final Integer capacity )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findEmployment( final Individual individual,
			final Instant expiration,
			final Function<Place, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration registerHospitality( final Place hospitality,
			final Integer capacity )
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Registration findHospitality( final Integer groupSize,
			final Duration stay, final Instant expiration,
			final Function<Place, Boolean> onSuggestion )
		{
			// TODO Auto-generated method stub
			return null;
		}

	}
}
