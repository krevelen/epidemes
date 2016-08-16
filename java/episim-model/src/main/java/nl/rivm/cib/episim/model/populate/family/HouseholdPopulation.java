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
package nl.rivm.cib.episim.model.populate.family;

import java.util.Objects;

import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.populate.DemographicEvent;
import nl.rivm.cib.episim.model.populate.Population;
import nl.rivm.cib.episim.util.Store;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link HouseholdPopulation}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface HouseholdPopulation<T extends HouseholdParticipant>
	extends Population<T>
{

	Store<Household<T>> households();

	default void onImmigration( final Household<T> immigrants )
	{
		Objects.requireNonNull( immigrants );
		immigrate( immigrants.members() );
	}

	default void onEmigration( final Household<T> emigrants )
	{
		Objects.requireNonNull( emigrants );
		immigrate( emigrants.members() );
	}

	static <T extends HouseholdParticipant> HouseholdPopulation<T> of(
		final String name, final Store<T> members,
		final Store<Household<T>> households )
	{
		return new HouseholdPopulation<T>()
		{
			private final ID id = ID.of( name );

			private final Subject<DemographicEvent<T>, DemographicEvent<T>> events = PublishSubject
					.create();

			@Override
			public Scheduler scheduler()
			{
				return members.scheduler();
			}

			@Override
			public Store<T> members()
			{
				return members;
			}

			@Override
			public ID id()
			{
				return this.id;
			}

			@Override
			public Observable<DemographicEvent<T>> events()
			{
				return this.events.asObservable();
			}

			@Override
			public void emit( final DemographicEvent<T> event )
			{
				this.events.onNext( event );
			}

			@Override
			public Store<Household<T>> households()
			{
				return households;
			}
		};
	}
}
