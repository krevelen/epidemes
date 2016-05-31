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
	Population getPopulation();

	Collection<Individual> getMembers();

	/** @return the home {@link Place} */
	Place getHome();

	// TODO change daily routines: schools, commute, etc.
	void move( Place newHome );

	default Household join( Individual member )
	{
		getMembers().add( member );
		return this;
	}

	default Household join( Household extra )
	{
		getMembers().addAll( extra.getMembers() );
		return this;
	}

	default Household part( Individual member )
	{
		getMembers().remove( member );
		return this;
	}

	default Household part( Household leavers )
	{
		getMembers().removeAll( leavers.getMembers() );
		return this;
	}

	default Household abandon()
	{
		getMembers().clear();
		return this;
	}

	/**
	 * {@link Simple} implementation of {@link Household}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Household
	{

		/**
		 * @param population the {@link Population}
		 * @return a {@link Simple} instance of a homeless {@link Household}
		 */
		public static Household of( final Population population )
		{
			return of( population, null );
		}

		/**
		 * @param population the {@link Population}
		 * @param home the initial {@link Place} of residence
		 * @return a {@link Simple} instance of {@link Household}
		 */
		public static Household of( final Population population,
			final Place home )
		{
			return new Simple( population, home );
		}

		private final Population population;

		private final Collection<Individual> members = new HashSet<>();

		private Place home;

		public Simple( final Population population, final Place home )
		{
			this.population = population;
			this.home = home;
		}

		@Override
		public Population getPopulation()
		{
			return this.population;
		}

		@Override
		public Scheduler scheduler()
		{
			return getPopulation().scheduler();
		}

		@Override
		public Place getHome()
		{
			return this.home;
		}

		@Override
		public void move( final Place newHome )
		{
			getHome().vacate( this );
			this.home = newHome;
			getHome().occupy( this );
		}

		@Override
		public Collection<Individual> getMembers()
		{
			return this.members;
		}

	}
}
