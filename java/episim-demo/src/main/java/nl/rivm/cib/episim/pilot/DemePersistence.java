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
package nl.rivm.cib.episim.pilot;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.coala.enterprise.Actor;
import io.coala.math.LatLong;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.locate.Transporter;
import nl.rivm.cib.episim.model.person.MeetingTemplate.Purpose;
import tec.uom.se.unit.Units;

/**
 * {@link DemePersistence}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface DemePersistence
	extends Region.Directory, Place.Directory, Transporter.Directory
{

	@Singleton
	public class MemCache implements DemePersistence
	{
		/** the regions: geographic areas, for analytics on clustering, ... */
		private NavigableMap<Region.ID, Region> regions = new TreeMap<>();

		/**
		 * the places: geographic locations, to broker by type
		 * ({@link Purpose}), proximity ({@link LatLong})
		 */
		private NavigableMap<Place.ID, Place> places = new TreeMap<>();

		/** the spaces: rooms, vehicles */
		private NavigableMap<Actor.ID, Transporter> vehicles = new TreeMap<>();

		/** RIVM National Institute for Public Health and the Environment */
		public LatLong RIVM_POSITION = LatLong.of( 52.1185272, 5.1868699,
				Units.DEGREE_ANGLE );

		@Inject
		private Actor.Factory actorFactory;

		@Override
		public Region lookup( final Region.ID id )
		{
			return this.regions.computeIfAbsent( id, key ->
			{
				final String name = null;// TODO lookup
				final Region parent = null;// TODO lookup
				final Collection<Region> children = null;// TODO lookup

				return new Region.Simple( id, name, parent, children );
			} );
		}

		@Override
		public Place lookup( final Place.ID id )
		{
			return this.places.computeIfAbsent( id, key -> Place.of( id ) );
		}

		@Override
		public Transporter lookup( final Actor.ID id )
		{
			return this.vehicles.computeIfAbsent( id, key ->
			{
				return this.actorFactory.create( id )
						.subRole( Transporter.class );
			} );
		}
	}
}
