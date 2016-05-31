package nl.rivm.cib.episim.model;

import java.util.Collection;

import javax.measure.quantity.Dimensionless;

import io.coala.random.ProbabilityDistribution;
import io.coala.random.ProbabilityDistribution.ArithmeticDistribution;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link Vaccine}s trigger the immune system to protect against some
 * {@link Infection}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Vaccine extends Timed
{

	Collection<Infection> getTarget();

	/**
	 * @param person the {@link Individual} to vaccinate
	 * @return an {@link ArithmeticDistribution} {@link ProbabilityDistribution} of the
	 *         <em>actual</em> efficacy for specified {@link Individual}'s
	 *         traits (age, sex, &hellip;)
	 */
	ArithmeticDistribution<Dimensionless> getEfficacy( Individual person );

	/**
	 * @param person the {@link Individual} to vaccinate
	 * @return an {@link ArithmeticDistribution} {@link ProbabilityDistribution} of the
	 *         <em>actual</em> delivery method comfort (e.g. intravenous,
	 *         needle-free patch, inhaled, oral, micro-needle arrays, stratum
	 *         corneum disruption) for specified {@link Individual}'s traits
	 *         (age, sex, &hellip;)
	 */
	ArithmeticDistribution<Dimensionless> getComfort( Individual person );

	/**
	 * {@link Simple} implementation of {@link Vaccine}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Vaccine
	{

		/**
		 * @param scheduler the {@link Scheduler}
		 * @param target the target {@link Infection}
		 * @param efficacy <em>actual</em> efficacy
		 * @param comfort <em>actual</em> delivery method comfort
		 * @return a {@link Simple} instance of {@link Vaccine}
		 */
		public static Simple of( final Scheduler scheduler,
			final Infection target, final ArithmeticDistribution<Dimensionless> efficacy,
			final ArithmeticDistribution<Dimensionless> comfort )
		{
			return new Simple( scheduler, target, efficacy, comfort );
		}

		private final Scheduler scheduler;

		private final Infection target;

		private final ArithmeticDistribution<Dimensionless> efficacy;

		private final ArithmeticDistribution<Dimensionless> comfort;

		/**
		 * {@link Simple} constructor
		 * 
		 * @param scheduler the {@link Scheduler}
		 * @param target the target {@link Infection}
		 */
		public Simple( final Scheduler scheduler, final Infection target,
			final ArithmeticDistribution<Dimensionless> efficacy,
			final ArithmeticDistribution<Dimensionless> comfort )
		{
			this.scheduler = scheduler;
			this.target = target;
			this.efficacy = efficacy;
			this.comfort = comfort;
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

		@Override
		public ArithmeticDistribution<Dimensionless> getEfficacy( final Individual person )
		{
			return this.efficacy;
		}

		@Override
		public ArithmeticDistribution<Dimensionless> getComfort( final Individual person )
		{
			return this.comfort;
		}

	}
}