/* $Id: 03628f399dba5f1c6bbb754d920e7a548cb38201 $
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
package nl.rivm.cib.episim.model.populate;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Timed;
import nl.rivm.cib.episim.model.populate.family.Participant;
import rx.Observable;

/**
 * {@link Population} provides macro-level characteristics, following common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>, e.g. by
 * <a href="http://jasss.soc.surrey.ac.uk/16/1/8.html">Geard et al. (2013)</a>
 * 
 * <table>
 * <tr>
 * <th>&mu;</th>
 * <td>Average death rate</td>
 * </tr>
 * <tr>
 * <th>B</th>
 * <td>Average birth rate</td>
 * </tr>
 * </table>
 * 
 * @version $Id: 03628f399dba5f1c6bbb754d920e7a548cb38201 $
 * @author Rick van Krevelen
 */
public interface Population<T> extends Proactive, Identified<Population.ID>
{

	//Signal<Integer> size();

	Observable<DemographicEvent> emitEvents();

	/** @return the {@link Participant}s */
	Map<T, Instant> members();

	class ID extends Id.Ordinal<String>
	{
		public static ID of( final String value )
		{
			return Util.of( value, new ID() );
		}
	}

	/**
	 * {@link DemographicEvent} represents {@link Population} changes due to
	 * birth, death, migration, couple formation or separation, or people
	 * leaving home
	 * 
	 * @version $Id: cc8c7dbaad3f0f805c5fec7ed16af65acf3e1a9d $
	 * @author Rick van Krevelen
	 */
	abstract class DemographicEvent implements Timed
	{

		private Instant time;

		private int deltaSize;

		/** the {@link Collection} of arrived {@link Participant}s */
		private Collection<Participant> arrivals;

		/** the {@link Collection} of departed {@link Participant}s */
		private Collection<Participant> departures;

		protected DemographicEvent( final Instant time,
			final Collection<Participant> arrivals,
			final Collection<Participant> departures )
		{
			this.time = time;
			this.arrivals = arrivals == null ? Collections.emptySet()
					: arrivals;
			this.departures = departures == null ? Collections.emptySet()
					: departures;
			this.deltaSize = this.arrivals.size() - this.departures.size();
		}

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
		public Collection<Participant> arrivals()
		{
			return this.arrivals;
		}

		/** @return a {@link Collection} of {@link Participant}s who departed */
		public Collection<Participant> departures()
		{
			return this.departures;
		}
	}

	public static class Birth extends DemographicEvent
	{
		public Birth( final Participant newborn )
		{
			super( newborn.now(), Collections.singleton( newborn ), null );
		}
	}

	public static class Death extends DemographicEvent
	{
		public Death( final Participant diseased )
		{
			super( diseased.now(), null, Collections.singleton( diseased ) );
		}
	}

	public static class Immigration extends DemographicEvent
	{
		public Immigration( final Instant time,
			final Collection<Participant> immigrants )
		{
			super( time, immigrants, null );
		}
	}

	public static class Emigration extends DemographicEvent
	{
		public Emigration( final Instant time,
			final Collection<Participant> emigrants )
		{
			super( time, null, emigrants );
		}
	}
//	class Simple implements Population
//	{
//		public static Simple of( final Scheduler scheduler )
//		{
//			return new Simple( scheduler );
//		}
//
//		private final Subject<DemographicEvent, DemographicEvent> changes = PublishSubject
//				.create();
//
//		private final Scheduler scheduler;
//
//		private long size;
//
//		public Simple( final Scheduler scheduler )
//		{
//			this.scheduler = scheduler;
//		}
//
//		@Override
//		public Scheduler scheduler()
//		{
//			return this.scheduler;
//		}
//
//		// TODO apply sorting?
//		private Set<Household> households = new HashSet<>();
//
//		@Override
//		public Iterable<Household> households()
//		{
//			return this.households;
//		}
//
//		@Override
//		public void reset( final Iterable<Household> households )
//		{
//			this.households.clear();
//			int size = 0;
//			for( Household hh : households )
//			{
//				size += hh.members().size();
//				this.households.add( hh );
//			}
//			// FIXME persist?
//			this.size = size;
//		}
//
//		public Observable<DemographicEvent> emitDemographicEvents()
//		{
//			return this.changes.asObservable();
//		}
//
//		@Override
//		public long size()
//		{
//			return this.size;
//		}
//
//		protected Population addSize( final long delta )
//		{
//			this.size += delta;
//			return this;
//		}
//
//		@Override
//		public Population birth( final Individual newborn )
//		{
//			this.changes.onNext( new Birth( newborn ) );
//			return addSize( 1 );
//		}
//
//		@Override
//		public Population death( final Individual diseased )
//		{
//			this.changes.onNext( new Death( diseased ) );
//			return addSize( -1 );
//		}
//
//		@Override
//		public Population depart( final Individual child, final Household nest )
//		{
//			this.changes.onNext( new NestDeparture( child, nest ) );
//			return this;
//		}
//
//		@Override
//		public Population immigrate( final Household immigrants )
//		{
//			this.changes.onNext( new Immigration( immigrants ) );
//			return addSize( immigrants.members().size() );
//		}
//
//		@Override
//		public Population emigrate( final Household emigrants )
//		{
//			this.changes.onNext( new Emigration( emigrants ) );
//			return addSize( -emigrants.members().size() );
//		}
//
//		@Override
//		public Population formCouple( final Household merging,
//			final Household abandoning )
//		{
//			this.changes.onNext( new CoupleFormation( merging, abandoning ) );
//			return this;
//		}
//
//		@Override
//		public Population dissolveCouple( final Household parting,
//			final Household dissolving )
//		{
//			this.changes.onNext( new CoupleDissolution( parting, dissolving ) );
//			return this;
//		}
//	}

}
