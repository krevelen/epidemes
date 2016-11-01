/* $Id: bb24fc6585f8a2b1817cf8d2bf16b9fcefadd13f $
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
package nl.rivm.cib.episim.model.locate.travel;

import io.coala.time.Duration;
import io.coala.time.Instant;
import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.locate.Place;

/**
 * {@link TravelEvent} generated by some {@link Individual} tracks the
 * {@link Instant} of its departure from its origin {@link Place} and the
 * {@link Instant} of its arrival to its destination {@link Place}
 * 
 * @version $Id: bb24fc6585f8a2b1817cf8d2bf16b9fcefadd13f $
 * @author Rick van Krevelen
 */
public class TravelEvent
{

	private Individual traveler;

	private Place origin;

	private Instant departure;

	private Vehicle transport;

	private Place destination;

	private Instant arrival;

	/**
	 * {@link TravelEvent} constructor
	 * 
	 * @param traveler the {@link Individual}
	 * @param origin the {@link Place} of origin
	 * @param transport the transport {@link Vehicle}
	 * @param destination the {@link Place} of destination
	 * @param arrivalDelay the {@link Duration} of travel
	 */
	public TravelEvent( final Individual traveler, final Place origin,
		final Vehicle transport, final Place destination,
		final Duration arrivalDelay )
	{
		this.traveler = traveler;
		this.origin = origin;
		this.departure = traveler.now();
		this.destination = destination;
		this.arrival = traveler.now().add( arrivalDelay );
	}

	public Individual getTraveler()
	{
		return this.traveler;
	}

	public Place getOrigin()
	{
		return this.origin;
	}

	public Instant getDeparture()
	{
		return this.departure;
	}

	public Vehicle getTransport()
	{
		return this.transport;
	}

	public Place getDestination()
	{
		return this.destination;
	}

	public Instant getArrival()
	{
		return this.arrival;
	}

}