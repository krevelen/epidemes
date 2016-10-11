/* $Id: c9d52251bdef7fc0d03cb53098fce3c300e50566 $
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
package nl.rivm.cib.episim.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.rivm.cib.episim.model.disease.infection.EpidemicCompartment;

/**
 * {@link EpidemicCompartmentTest}
 * 
 * @version $Id: c9d52251bdef7fc0d03cb53098fce3c300e50566 $
 * @author Rick van Krevelen
 */
public class EpidemicCompartmentTest
{

	/**
	 * Test method for {@link EpidemicCompartment#isInfective()}.
	 */
	@Test
	public void testIsInfective()
	{
		assertTrue( EpidemicCompartment.Simple.INFECTIVE.isInfective() );
		assertFalse( EpidemicCompartment.Simple.SUSCEPTIBLE.isInfective() );
		assertFalse( EpidemicCompartment.Simple.EXPOSED.isInfective() );
		assertFalse( EpidemicCompartment.Simple.RECOVERED.isInfective() );
		assertFalse( EpidemicCompartment.Simple.PASSIVE_IMMUNE.isInfective() );
	}

}
