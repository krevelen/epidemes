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
package nl.rivm.cib.episim.model;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.Test;

import io.coala.log.LogUtil;
import nl.rivm.cib.episim.model.disease.Individual;
import nl.rivm.cib.episim.model.location.Location;

/**
 * {@link ScenarioTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ScenarioTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( ScenarioTest.class );

	@Test
	public void scenarioTest()
	{
		try
		{
			final Scenario test = new Scenario()
					.withLocations(
							Arrays.asList( new Location().setId( "myLoc" ) ) )
					.withPopulation( Arrays.asList( new Individual() ) );
			assertTrue( "no locations", 0 < test.locationsURIs.size() );
			LOG.trace( "Added locations: " + test.locationsURIs );
			assertTrue( "no individuals", 0 < test.householdURIs.size() );
			LOG.trace( "Added individuals: " + test.householdURIs );
		} catch( final Throwable t )
		{
			LOG.error( "Problem", t );
		}
	}

}
