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

import java.util.Collection;
import java.util.Collections;

import io.coala.time.x.Instant;

/**
 * {@link DemographicEvent} represents {@link Population} changes due to birth,
 * death, migration, couple formation or separation, or people leaving home
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public abstract class DemographicEvent
{

	private Instant time;

	/** the {@link Collection} of {@link Individual}s involved */
	private Collection<Individual> individuals;

	/** the previous {@link Household}, e.g. empty nest or single shack */
	private Household previous;

	protected DemographicEvent( final Instant time,
		final Collection<Individual> individuals, final Household previous )
	{
		this.time = time;
		this.individuals = individuals;
		this.previous = previous;
	}

	/** @return the {@link Instant} */
	public Instant getTime()
	{
		return this.time;
	}

	/** @return a {@link Collection} of {@link Individual}s involved */
	public Collection<Individual> getIndividuals()
	{
		return this.individuals;
	}

	/**
	 * @return the previous {@link Household}, e.g. empty nest or single shack
	 */
	public Household getPrevious()
	{
		return this.previous;
	}

	public static Birth birth( final Individual newborn )
	{
		return new Birth( newborn );
	}

	public static Death death( final Individual diseased )
	{
		return new Death( diseased );
	}

	public static NestDeparture depart( final Individual child,
		final Household nest )
	{
		return new NestDeparture( child, nest );
	}

	public static Immigration immigration( final Household immigrants )
	{
		return new Immigration( immigrants );
	}

	public static Emigration emigration( final Household emigrants )
	{
		return new Emigration( emigrants );
	}

	public static CoupleFormation coupleFormation( final Household merging,
		final Household abandoning )
	{
		return new CoupleFormation( merging, abandoning );
	}

	public static CoupleDissolution coupleDissolution( final Household parting,
		final Household dissolving )
	{
		return new CoupleDissolution( parting, dissolving );
	}

	public static class Birth extends DemographicEvent
	{
		public Birth( final Individual newborn )
		{
			super( newborn.now(), Collections.singleton( newborn ), null );
		}
	}

	public static class Death extends DemographicEvent
	{
		public Death( final Individual diseased )
		{
			super( diseased.now(), Collections.singleton( diseased ), null );
		}
	}

	public static class NestDeparture extends DemographicEvent
	{
		public NestDeparture( final Individual parting,
			final Household nest )
		{
			super( parting.now(), Collections.singleton( parting ), nest );
		}
	}

	public static class Immigration extends DemographicEvent
	{
		public Immigration( final Household immigrants )
		{
			super( immigrants.now(), immigrants.getMembers(), immigrants );
		}
	}

	public static class Emigration extends DemographicEvent
	{
		public Emigration( final Household emigrants )
		{
			super( emigrants.now(), emigrants.getMembers(), emigrants );
		}
	}

	public static class CoupleFormation extends DemographicEvent
	{
		public CoupleFormation( final Household merging,
			final Household abandoning )
		{
			super( merging.now(), merging.join( abandoning ).getMembers(),
					abandoning.abandon() );
		}
	}

	public static class CoupleDissolution extends DemographicEvent
	{
		public CoupleDissolution( final Household parting,
			final Household dissolving )
		{
			super( parting.now(), parting.getMembers(),
					dissolving.part( parting ) );
		}
	}
}