package nl.rivm.cib.episim.model;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.measure.quantity.Dimensionless;

import org.jscience.physics.amount.Amount;

import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link VaccineBelief}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface VaccineBelief extends Timed
{

	Infection getInfection();

	/**
	 * @param factor the perceived safety/risk factor as a function of time
	 *            (e.g. news/opinion contact from media, pastor, family,
	 *            doctors)
	 */
	void onPerception( Function<Instant, Amount<Dimensionless>> factor );

	/**
	 * @return vaccination willingness decision (positive [.5-1.] or negative
	 *         [.0-.5) )
	 */
	Amount<Dimensionless> getWillingness();

	class Simple implements VaccineBelief
	{

		private final Scheduler scheduler;

		private final Infection infection;

		private final Set<Function<Instant, Amount<Dimensionless>>> factors = new HashSet<>();

		public Simple( final Scheduler scheduler, final Infection infection )
		{
			this.scheduler = scheduler;
			this.infection = infection;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Infection getInfection()
		{
			return this.infection;
		}

		@Override
		public void onPerception(
			final Function<Instant, Amount<Dimensionless>> factor )
		{
			this.factors.add( factor );
		}

		@Override
		public Amount<Dimensionless> getWillingness()
		{
			Amount<Dimensionless> result = Amount.ZERO;
			for( Function<Instant, Amount<Dimensionless>> factor : this.factors )
				result = result.plus( factor.apply( now() ) );
			return result;
		}

	}
}