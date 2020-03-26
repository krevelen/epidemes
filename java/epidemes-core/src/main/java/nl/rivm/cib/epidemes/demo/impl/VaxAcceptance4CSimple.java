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
package nl.rivm.cib.epidemes.demo.impl;

import java.math.BigDecimal;

import io.coala.util.Compare;
import nl.rivm.cib.epidemes.demo.DemoScenario;
import nl.rivm.cib.epidemes.demo.DemoScenario.Medical;
import nl.rivm.cib.epidemes.demo.DemoScenario.Medical.VaxAcceptanceEvaluator;
import nl.rivm.cib.epidemes.demo.entity.Households;
import nl.rivm.cib.epidemes.model.VaxHesitancy;
import nl.rivm.cib.epidemes.model.VaxOccasion;

/**
 * applied {@link VaxHesitancy#minimumConvenience} and
 * {@link VaxHesitancy#averageBarrier}
 */
public class VaxAcceptance4CSimple implements Medical.VaxAcceptanceEvaluator
{
	@Override
	public boolean test( final Households.HouseholdTuple hh,
		final VaxOccasion occasion )
	{
		final BigDecimal confidence = hh
				.get( Households.Confidence.class ),
				complacency = hh.get( Households.Complacency.class );

		// no occasion, just return general attitude
		if( occasion == null )
			return Compare.gt( confidence, complacency );

		final BigDecimal convenience = VaxHesitancy
				.minimumConvenience( occasion );
		final BigDecimal barrier = VaxHesitancy
				.averageBarrier( confidence, complacency );
		return Compare.ge( convenience, barrier );
	}
}