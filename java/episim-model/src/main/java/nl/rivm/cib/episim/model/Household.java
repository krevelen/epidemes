package nl.rivm.cib.episim.model;

import java.util.Collection;
import java.util.HashSet;

import io.coala.time.Scheduler;
import io.coala.time.Timed;

/**
 * {@link Household} adopts elements from
 * <a href= "https://github.com/nlgn/sim-demog">Python code by Nic Geard</a>
 * 
 * @version $Id: 88e3618b2571332669cc3266f9d0836962e886d3 $
 * @author Rick van Krevelen
 */
public interface Household extends Timed
{
	/** @return the overall {@link Population} */
	Population getPopulation();

	/** @return the member {@link Individual}s */
	Collection<Individual> members();

	/** @return the (main) {@link Place} of residence */
	Place getHome();

	// TODO change daily routines: schools, commute, etc.
	void move( Place newHome );

	/**
	 * @param member the {@link Individual} to add
	 * @return this {@link Household} to allow chaining
	 */
	default Household join( Individual member )
	{
		members().add( member );
		return this;
	}

	default Household join( Household extra )
	{
		members().addAll( extra.members() );
		return this;
	}

	/**
	 * @param member the {@link Individual} to remove
	 * @return this {@link Household} to allow chaining
	 */
	default Household part( Individual member )
	{
		members().remove( member );
		return this;
	}

	/**
	 * @param member the {@link Individual} to remove
	 * @return this {@link Household} to allow chaining
	 */
	default Household part( Household leavers )
	{
		members().removeAll( leavers.members() );
		return this;
	}

	/**
	 * @return this {@link Household} (after clearing) to allow chaining
	 */
	default Household abandon()
	{
		members().clear();
		return this;
	}

	/**
	 * {@link Simple} implementation of {@link Household}
	 * 
	 * @version $Id: 88e3618b2571332669cc3266f9d0836962e886d3 $
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
		public Collection<Individual> members()
		{
			return this.members;
		}

	}
}
