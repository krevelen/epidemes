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

import io.coala.enterprise.Fact;
import nl.rivm.cib.episim.model.person.Attitude;
import nl.rivm.cib.episim.model.vaccine.Vaccine;

/**
 * {@link Immunizable} is a decision maker who holds mental {@link Attitude}s
 * about {@link Vaccine}s
 *
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Immunizable
{

//	Attitude attitudeOn( VaccinationOccasion request );
//
//	default void handleOpportunities( Actor<VaccinationOccasion> target )
//	{
//		target.emit( FactKind.REQUESTED )
//				.subscribe( rq ->
//				{
//					if( !attitudeOn( rq ).isPositive() )
//					{
//						target.respond( rq, FactKind.DECLINED ).commit();
//						return;
//					}
////					final VaccinationOccasion pm = 
//							target.respond( rq, FactKind.PROMISED ).commit();
//					// TODO schedule logistics (getting to the occasion, stating success, accepting result, etc)
//				}, e -> LogUtil.getLogger( Immunizable.class ).error( "Problem",
//						e ) );
//	}

	/**
	 * {@link VaccinationOccasion} is a transaction kind for which requests may
	 * be generated for and/or executed by an {@link Immunizable}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	interface VaccinationOccasion //extends MultiCriteriaWeightedAlternative<Criterion>
		extends Fact
	{

		// TODO 3Cs/4Cs property definition, situation values, etc?

	}
}
