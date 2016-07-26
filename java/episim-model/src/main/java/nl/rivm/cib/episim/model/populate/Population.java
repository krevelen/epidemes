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
import java.util.Objects;

import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.Store;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

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
public interface Population<T extends Participant>
	extends Proactive, Identified<Population.ID>
{

	class ID extends Id.Ordinal<String>
	{
		public static ID of( final String value )
		{
			return Util.of( value, new ID() );
		}
	}

	Observable<DemographicEvent<T>> emitEvents();

	Store<T> members();

	void on( final DemographicEvent<T> event );

	@SuppressWarnings( "unchecked" )
	default void onBirth( final T newborn )
	{
		Objects.requireNonNull( newborn );
		on( DemographicEvent.Builder.of( Birth.class, now() )
				.withArrivals( Collections.singleton( newborn ) ).build() );
	}

	@SuppressWarnings( "unchecked" )
	default void onDeath( final T diseased )
	{
		Objects.requireNonNull( diseased );
		members().remove( diseased );
		on( DemographicEvent.Builder.of( Death.class, now() )
				.withDepartures( Collections.singleton( diseased ) ).build() );
	}

	@SuppressWarnings( "unchecked" )
	default void onImmigration( final Collection<T> immigrants )
	{
		Objects.requireNonNull( immigrants );
		if( immigrants.isEmpty() ) throw new IllegalArgumentException();
		on( DemographicEvent.Builder.of( Immigration.class, now() )
				.withArrivals( immigrants ).build() );
	}

	@SuppressWarnings( "unchecked" )
	default void onEmigration( final Collection<T> emigrants )
	{
		Objects.requireNonNull( emigrants );
		if( emigrants.isEmpty() ) throw new IllegalArgumentException();
		on( DemographicEvent.Builder.of( Emigration.class, now() )
				.withDepartures( emigrants ).build() );
	}

	static <T extends Participant> Population<T> of( final String name,
		final Store<T> members )
	{
		return new Population<T>()
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
			public Observable<DemographicEvent<T>> emitEvents()
			{
				return this.events.asObservable();
			}

			@Override
			public void on( final DemographicEvent<T> event )
			{
				at( now() ).call( t ->
				{
					this.events.onNext( event );
				} );
			}
		};
	}

	public static class Birth<T extends Participant> extends DemographicEvent<T>
	{
	}

	public static class Death<T extends Participant> extends DemographicEvent<T>
	{
	}

	public static class Immigration<T extends Participant>
		extends DemographicEvent<T>
	{
	}

	public static class Emigration<T extends Participant>
		extends DemographicEvent<T>
	{
	}
}
