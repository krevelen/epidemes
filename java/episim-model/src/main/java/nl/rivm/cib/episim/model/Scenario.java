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

import java.net.URI;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import nl.rivm.cib.episim.model.location.Location;
import nl.rivm.cib.episim.model.population.Individual;

/**
 * {@link Scenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Scenario
{

	/** the locationsURIs */
	protected final NavigableMap<Location.ID, URI> locationsURIs = new ConcurrentSkipListMap<>();

	/** the householdURIs */
	protected final NavigableMap<Individual.ID, URI> householdURIs = new ConcurrentSkipListMap<>();

	public Scenario withLocations( final Iterable<Location> locations )
	{
		for( Location location : locations )
			this.locationsURIs.put( location.getId(),
					URI.create( location.getId().unwrap() ) );
		return this;
	}

//	public Scenario withPopulation( final Iterable<Individual> individuals )
//	{
//		for( Individual individual : individuals )
//			this.householdURIs.put( individual.getId(),
//					URI.create( "ind" + individual.getId().unwrap() ) );
//		return this;
//	}

}
