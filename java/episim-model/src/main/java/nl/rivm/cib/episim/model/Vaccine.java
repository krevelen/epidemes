package nl.rivm.cib.episim.model;

import io.coala.name.x.Id;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link Vaccine}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Vaccine extends Timed
{

	Infection getTarget();

	/**
	 * @param scheduler the {@link Scheduler}
	 * @param target the target {@link Infection}
	 * @return a {@link Simple} instance of {@link Vaccine}
	 */
	static Vaccine of( final Scheduler scheduler, final Infection target )
	{
		return new Simple( scheduler, target );
	}

	/**
	 * {@link ID}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class ID extends Id.Ordinal<String>
	{
		public static ID valueOf( final String value )
		{
			return Util.valueOf( value, new ID() );
		}
	}

	/**
	 * {@link Simple} implementation of {@link Vaccine}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Vaccine
	{

		private final Scheduler scheduler;

		private final Infection target;

		/**
		 * {@link Simple} constructor
		 * 
		 * @param scheduler the {@link Scheduler}
		 * @param target the target {@link Infection}
		 */
		public Simple( final Scheduler scheduler, final Infection target )
		{
			this.scheduler = scheduler;
			this.target = target;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Infection getTarget()
		{
			return this.target;
		}

	}
}