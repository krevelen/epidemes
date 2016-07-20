/* $Id$
 * 
 * Part of ZonMW project no. 50-53000-98-156
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2016 RIVM National Institute for Health and Environment 
 */
package nl.rivm.cib.episim.model.attitude;

import java.util.EnumMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import io.coala.decide.DecisionAnalyzer.MultiCriteriaWeightedAlternative;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.Vaccine;
import nl.rivm.cib.episim.model.VaccineAttitude;

/**
 * {@link Hesitancy3C}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Hesitancy3C implements VaccineAttitude
{

	/**
	 * {@link AttitudeCriterion}
	 * 
	 * @version $Id: f5d8e89c5356937430b94f625ecfe4fbd38401f8 $
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
	 * @version $Id: f5d8e89c5356937430b94f625ecfe4fbd38401f8 $
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
		static VaccineOpportunity of( final Hesitancy3C attitude,
			final Amount<Length> distance )
		{
			return new Simple( attitude, distance );
		}

		/**
		 * {@link Simple}
		 * 
		 * @version $Id: f5d8e89c5356937430b94f625ecfe4fbd38401f8 $
		 * @author Rick van Krevelen
		 */
		class Simple implements VaccineOpportunity
		{

			private final Vaccine vaccine;

			private final Map<AttitudeCriterion, Number> values = new EnumMap<>(
					AttitudeCriterion.class );

			public Simple( final Hesitancy3C attitude,
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

	@Override
	public Scheduler scheduler()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vaccine getVaccine()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWilling()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the confidence (vaccine effectiveness vs risk perception) due to
	 *         e.g. success reports, side-effect reports, etc.
	 */
	public Amount<Dimensionless> getConfidence()
	{
		return null; // FIXME
	}

	/**
	 * @return the complacency (inverse urgency or disease risk perception)
	 */
	public Amount<Dimensionless> getComplacency()
	{
		return null; // FIXME
	}

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
	public Amount<Dimensionless> getConvenience( Amount<Length> distance )
	{
		return null; // FIXME
	}

	/**
	 * TODO logarithmic belief combination?
	 * 
	 * @return the willingness to vaccinate aggregates {@link #getConfidence()},
	 *         {@link #getComplacency()}, and {@link #getConvenience()}
	 */
//	Indicator<Dimensionless> getWillingness();

}
