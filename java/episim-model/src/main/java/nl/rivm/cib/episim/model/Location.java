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

import org.opengis.spatialschema.geometry.geometry.Position;

import rx.Observable;

/**
 * {@link Location}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Location //extends Observer<TravelEvent>
{

	/** @return the global {@link Position} of this {@link Location} */
	Position getPosition();

	/** @return the current {@link Collection} of transmission {@link Route}s */
	Collection<Route> getRoutes();

	/** @return the current {@link Collection} of {@link Carrier} occupants */
	Collection<Carrier> getOccupants();

	/**
	 * @return an {@link Observable} stream of {@link ContactEvent}s generated
	 *         by {@link Carrier} occupants of this {@link Location}
	 */
	Observable<ContactEvent> getContacts();

	/**
	 * {@link Carrier}s arriving at this {@link Location} may cause it to
	 * generate {@link ContactEvent}s or add/remove {@link Route}s (e.g.
	 * contaminated objects, food, water, blood, ...)
	 * 
	 * @param event a {@link TravelEvent} with
	 *            {@link TravelEvent#getDestination()} == this {@link Location}
	 *            and {@link TravelEvent#getArrival()} == now
	 */
	void onArrival( TravelEvent event );

	/**
	 * {@link Carrier}s departing from this {@link Location} may cause it to
	 * generate {@link ContactEvent}s or add/remove {@link Route}s (e.g.
	 * contaminated objects, food, water, blood, ...)
	 * 
	 * @param event a {@link TravelEvent} with {@link TravelEvent#getOrigin()}
	 *            == this {@link Location} and
	 *            {@link TravelEvent#getDeparture()} == now
	 */
	void onDeparture( TravelEvent event );

}
