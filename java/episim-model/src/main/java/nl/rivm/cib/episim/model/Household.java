package nl.rivm.cib.episim.model;

import java.util.Collection;
import java.util.HashSet;

import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link Household} includes elements adapted from
 * <a href= "https://github.com/nlgn/sim-demog">Python code by Nic Geard</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Household extends Timed
{
	Collection<Individual> getMembers();

	/** @return the home {@link Place} */
	Place getHome();

	void move( Place newHome );

	default boolean join( Individual member )
	{
		return getMembers().add( member );
	}

	default boolean leave( Individual member )
	{
		return getMembers().remove( member );
	}

	/**
	 * @param scheduler the {@link Scheduler}
	 * @param home the initial {@link Place} of residence
	 * @return a {@link Simple} instance of {@link Household}
	 */
	static Household of( final Scheduler scheduler, final Place home )
	{
		return new Simple( scheduler, home );
	}

	/**
	 * {@link Simple} implementation of {@link Household}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Household
	{
		private final Scheduler scheduler;

		private Place home;

		private final Collection<Individual> members = new HashSet<>();

		public Simple( final Scheduler scheduler, final Place home )
		{
			this.scheduler = scheduler;
			this.home = home;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Place getHome()
		{
			return this.home;
		}

		@Override
		public void move( final Place newHome )
		{
			this.home = newHome;
			// TODO change daily routine, schools, commute, etc.
		}

		@Override
		public Collection<Individual> getMembers()
		{
			return this.members;
		}

	}
}
