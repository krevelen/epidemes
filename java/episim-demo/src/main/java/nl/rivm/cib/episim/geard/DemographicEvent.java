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
package nl.rivm.cib.episim.geard;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import io.coala.time.Instant;
import io.coala.time.Timed;
import io.coala.util.Instantiator;

/**
 * {@link DemographicEvent} represents {@link Population} changes due to birth,
 * death, migration, couple formation or separation, or people leaving home
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Deprecated
public abstract class DemographicEvent<T extends Participant> implements Timed
{

	Instant time = null;

	int deltaSize = 0;

	/** the {@link Collection} of arrived {@link Participant}s */
	Collection<T> arrivals = Collections.emptySet();

	/** the {@link Collection} of departed {@link Participant}s */
	Collection<T> departures = Collections.emptySet();

//		protected DemographicEvent( final Instant time,
//			final Collection<T> arrivals, final Collection<T> departures )
//		{
//			this.time = time;
//			this.arrivals = arrivals == null ? Collections.emptySet()
//					: arrivals;
//			this.departures = departures == null ? Collections.emptySet()
//					: departures;
//			this.deltaSize = this.arrivals.size() - this.departures.size();
//		}

	@Override
	public Instant now()
	{
		return this.time;
	}

	public int deltaSize()
	{
		return this.deltaSize;
	}

	/** @return a {@link Collection} of {@link Participant}s who arrived */
	public Collection<T> arrivals()
	{
		return this.arrivals;
	}

	/** @return a {@link Collection} of {@link Participant}s who departed */
	public Collection<T> departures()
	{
		return this.departures;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + '(' + deltaSize() + ')' + '+'
				+ arrivals() + '-' + departures();
	}

	public static class Builder<R extends DemographicEvent<T>, T extends Participant>
	{
		private R event;

		public static <R extends DemographicEvent<T>, T extends Participant>
			DemographicEvent.Builder<R, T>
			of( final Class<R> type, final Instant time )
		{
			final DemographicEvent.Builder<R, T> result = new DemographicEvent.Builder<R, T>();
			result.event = Instantiator.instantiate( type );
			result.event.time = time;
			return result;
		}

		public DemographicEvent.Builder<R, T> withUpdatedDelta()
		{
			this.event.deltaSize = this.event.arrivals.size()
					- this.event.departures.size();
			return this;
		}

		public DemographicEvent.Builder<R, T>
			withArrivals( final Collection<T> arrivals )
		{
			Objects.requireNonNull( arrivals );
			this.event.arrivals = arrivals;
			return withUpdatedDelta();
		}

		public DemographicEvent.Builder<R, T>
			withDepartures( final Collection<T> departures )
		{
			Objects.requireNonNull( departures );
			this.event.departures = departures;
			return withUpdatedDelta();
		}

		public R build()
		{
			return this.event;
		}
	}
}