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
package nl.rivm.cib.episim.model.disease.infection;

import io.coala.time.Instant;
import io.coala.time.Timed;
import nl.rivm.cib.episim.model.disease.Afflicted;
import nl.rivm.cib.episim.model.locate.Place;

/**
 * {@link TransmissionEvent} generated by some {@link Infection} represents its
 * successful invasion of a susceptible {@link Afflicted} that was exposed due
 * to some {@link ContactEvent}.
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class TransmissionEvent implements Timed
{

	private Instant time;

	private Place site;

	private ContactEvent contact;

	public TransmissionEvent( final Instant time, final Place site,
		final ContactEvent contact )
	{
		this.time = time;
		this.site = site;
		this.contact = contact;
	}

	@Override
	public Instant now()
	{
		return this.time;
	}

	/** @return the {@link Place} */
	public Place getPlace()
	{
		return this.site;
	}

	/** @return the {@link ContactEvent} */
	public ContactEvent getContact()
	{
		return this.contact;
	}

	public static TransmissionEvent of( final Instant time, final Place site,
		final ContactEvent cause )
	{
		return new TransmissionEvent( time, site, cause );
	}
}