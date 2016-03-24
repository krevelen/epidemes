/* $Id: 18fd2b4cf3b6654714b5baa115e415bb0e851b5d $
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

import io.coala.time.x.Instant;

/**
 * {@link TransmissionEvent} generated by some {@link Infection} represents its
 * successful invasion of a susceptible {@link Carrier} that was exposed due to
 * some {@link ContactEvent}.
 * 
 * @version $Id: 18fd2b4cf3b6654714b5baa115e415bb0e851b5d $
 * @author Rick van Krevelen
 */
public class TransmissionEvent
{

	private Instant time;

	private ContactEvent contact;

	public TransmissionEvent( final Instant time, final ContactEvent contact )
	{
		this.time = time;
		this.contact = contact;
	}

	/** @return the transmission {@link Instant} */
	public Instant getTime()
	{
		return this.time;
	}

	/** @return the {@link ContactEvent} */
	public ContactEvent getContact()
	{
		return this.contact;
	}

	public static TransmissionEvent of( final Instant time,
		final ContactEvent cause )
	{
		return new TransmissionEvent( time, cause );
	}
}