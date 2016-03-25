package nl.rivm.cib.episim.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import io.coala.decide.DecisionAnalyzer.MultiCriteriaWeightedAlternative;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link VaccineAttitude} reflects perceived safety/risk due to e.g. opinions
 * observed from media, pastor, family, doctors, etc.
 * 
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface VaccineAttitude extends Timed
{

	Vaccine getVaccine();

	/**
	 * @return the confidence (vaccine effectiveness vs risk perception) due to
	 *         e.g. success reports, side-effect reports, etc.
	 */
	Amount<Dimensionless> getConfidence();

	/**
	 * @return the complacency (inverse urgency or disease risk perception)
	 */
	Amount<Dimensionless> getComplacency();

	/**
	 * @return the comfort factor {@link Amount} due to delivery method (e.g.
	 *         intravenous, needle-free patch, inhaled, oral, micro-needle
	 *         arrays, stratum corneum disruption)
	 */
//	Amount<Dimensionless> getDeliveryComfort();

//	Amount<Dimensionless> getEffectiveness();
	/**
	 * @return the convenience (inverse delivery effort required) due to time
	 *         and/or distance required, the delivery method used (e.g.
	 *         intravenous, needle-free patch, inhaled, oral, micro-needle
	 *         arrays, stratum corneum disruption), etc.
	 */
	Amount<Dimensionless> getConvenience( Amount<Length> distance );

	/**
	 * TODO logarithmic belief combination?
	 * 
	 * @return the willingness to vaccinate aggregates {@link #getConfidence()},
	 *         {@link #getComplacency()}, and {@link #getConvenience()}
	 */
//	Indicator<Dimensionless> getWillingness();

	/**
	 * @return {@code true} iff this attitude is willing to vaccinate given
	 *         {@link #getConfidence()}, {@link #getComplacency()}, and
	 *         {@link #getConvenience()}
	 */
	Set<Vaccine> isWilling( Map<Vaccine, VaccineOpportunity> opportunities );

	/**
	 * {@link AttitudeCriterion}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	enum AttitudeCriterion
	{

		/**
		 * the confidence (inverse <b>vaccine</b> risk perception, e.g.
		 * side-effects)
		 */
		CONFIDENCE,

		/**
		 * the complacency (inverse urgency or <b>disease</b> risk perception)
		 */
		COMPLACENCY,

		/** the convenience (inverse delivery <b>effort</b> required) */
		CONVENIENCE,

		;
	}

	/**
	 * {@link VaccineOpportunity}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	interface VaccineOpportunity
		extends MultiCriteriaWeightedAlternative<AttitudeCriterion>
	{

		Vaccine getVaccine();

		/**
		 * @param attitude
		 * @param distance the
		 * @return
		 */
		static VaccineOpportunity of( final VaccineAttitude attitude,
			final Amount<Length> distance )
		{
			return new Simple( attitude, distance );
		}

		/**
		 * {@link Simple}
		 * 
		 * @version $Id$
		 * @author Rick van Krevelen
		 */
		class Simple implements VaccineOpportunity
		{

			private final Vaccine vaccine;

			private final Map<AttitudeCriterion, Number> values = new EnumMap<>(
					AttitudeCriterion.class );

			public Simple( final VaccineAttitude attitude,
				final Amount<Length> distance )
			{
				this.vaccine = attitude.getVaccine();
				with( AttitudeCriterion.CONFIDENCE, attitude.getConfidence() );
				with( AttitudeCriterion.COMPLACENCY,
						attitude.getComplacency() );
				with( AttitudeCriterion.CONVENIENCE,
						attitude.getConvenience( distance ) );
			}

			public Simple with( final AttitudeCriterion criterion,
				final Amount<Dimensionless> value )
			{
				return with( criterion, value.getEstimatedValue() );
			}

			public Simple with( final AttitudeCriterion criterion,
				final Number value )
			{
				this.values.put( criterion, value );
				return this;
			}

			@Override
			public Number evaluate( final AttitudeCriterion criterion )
			{
				return this.values.get( criterion );
			}

			@Override
			public Vaccine getVaccine()
			{
				return this.vaccine;
			}
		}
	}
}