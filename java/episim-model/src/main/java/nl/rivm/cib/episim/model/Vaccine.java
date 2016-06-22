package nl.rivm.cib.episim.model;

import java.util.Collection;
import java.util.Collections;

import javax.measure.quantity.Dimensionless;

import io.coala.random.ProbabilityDistribution;
import io.coala.random.ProbabilityDistribution.ArithmeticDistribution;
import io.coala.time.Scheduler;
import io.coala.time.Timed;

/**
 * {@link Vaccine}s trigger the immune system to protect against some
 * {@link Infection}
 * 
 * @version $Id: 61e2466aa6bbc87325a9001452de624ce2ac198e $
 * @author Rick van Krevelen
 */
public interface Vaccine extends Timed
{

	Collection<Infection> getTargets();

	/**
	 * @param condition the {@link Condition} to improve
	 * @return a {@link ProbabilityDistribution} of the <em>actual</em> efficacy
	 *         for specified {@link Individual} 's traits (age, sex, &hellip;)
	 */
	ProbabilityDistribution<Boolean> getEfficacy( Condition condition );

	/**
	 * @param person the {@link Individual} to vaccinate
	 * @return a {@link ProbabilityDistribution} of the <em>actual</em> delivery
	 *         method comfort (e.g. intravenous, needle-free patch, inhaled,
	 *         oral, micro-needle arrays, stratum corneum disruption) for
	 *         specified {@link Individual}'s traits (age, sex, &hellip;)
	 */
	ArithmeticDistribution<Dimensionless> getComfort( Individual person );

	/**
	 * {@link Simple} implementation of {@link Vaccine}
	 * 
	 * @version $Id: 61e2466aa6bbc87325a9001452de624ce2ac198e $
	 * @author Rick van Krevelen
	 */
	class Simple implements Vaccine
	{

		/**
		 * @param scheduler the {@link Scheduler}
		 * @param target the target {@link Infection}
		 * @param efficacy <em>actual</em> efficacy distribution for everyone
		 * @param comfort <em>actual</em> delivery method comfort distribution
		 *            for everyone
		 * @return a {@link Simple} instance of {@link Vaccine}
		 */
		public static Simple of( final Scheduler scheduler,
			final Infection target,
			final ProbabilityDistribution<Boolean> efficacy,
			final ArithmeticDistribution<Dimensionless> comfort )
		{
			return new Simple( scheduler, Collections.singleton( target ),
					efficacy, comfort );
		}

		private final Scheduler scheduler;

		private final Collection<Infection> targets;

		private final ProbabilityDistribution<Boolean> efficacy;

		private final ArithmeticDistribution<Dimensionless> comfort;

		/**
		 * {@link Simple} constructor
		 * 
		 * @param scheduler the {@link Scheduler}
		 * @param targets the target {@link Infection}s
		 */
		public Simple( final Scheduler scheduler,
			final Collection<Infection> targets,
			final ProbabilityDistribution<Boolean> efficacy,
			final ArithmeticDistribution<Dimensionless> comfort )
		{
			this.scheduler = scheduler;
			this.targets = targets;
			this.efficacy = efficacy;
			this.comfort = comfort;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Collection<Infection> getTargets()
		{
			return this.targets;
		}

		@Override
		public ProbabilityDistribution<Boolean>
			getEfficacy( final Condition condition )
		{
			return this.efficacy;
		}

		@Override
		public ArithmeticDistribution<Dimensionless>
			getComfort( final Individual person )
		{
			return this.comfort;
		}

	}
}