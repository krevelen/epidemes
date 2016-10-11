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
package nl.rivm.cib.episim.model.vaccine.attitude;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import io.coala.time.Instant;

/**
 * {@link Hesitancy3C}
 * 
 * <style type='text/css'> table.topcenter td { text-align:center;
 * vertical-align:top; } </style>
 * <table class="topcenter">
 * <tr>
 * <th>Pro</th>
 * <th>Vaccination Hesitancy Driver</th>
 * <th>Con</th>
 * </tr>
 * <tr>
 * <td>raise</td>
 * <td>confidence / expected medical utility <br/>
 * (safety/risk framing, comfort, anticipated regret, recommendations)</td>
 * <td>lower</td>
 * </tr>
 * <tr>
 * <td>raise</td>
 * <td>coherence / expected social utility<br/>
 * (social benefit, perceived social norm/defaults)</td>
 * <td>lower</td>
 * </tr>
 * <tr>
 * <td>raise</td>
 * <td>convenience / expected temporal utility <br/>
 * (reminders, proximity, availability, affordability)</td>
 * <td>-</td>
 * </tr>
 * <caption align="bottom"><em>(adapted from
 * <a href="http://dx.doi.org/10.1177/2372732215600716">Betsch et al.,
 * 2015)</a></em></caption>
 * </table>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Hesitancy3C implements Immunizable.Attitude
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

	@Override
	public Instant now()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPositive()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * TODO logarithmic belief combination?
	 * 
	 * @return the willingness to vaccinate aggregates {@link #getConfidence()},
	 *         {@link #getComplacency()}, and {@link #getConvenience()}
	 */
//	Indicator<Dimensionless> getWillingness();

}
