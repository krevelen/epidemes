package nl.rivm.cib.episim.model.person;

import java.util.Collections;
import java.util.Objects;

import io.coala.rx.RxCollection;
import io.coala.time.Scheduler;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Household} adopts elements from
 * <a href= "https://github.com/nlgn/sim-demog">Python code by Nic Geard</a>
 * 
 * @version $Id: 88e3618b2571332669cc3266f9d0836962e886d3 $
 * @author Rick van Krevelen
 */
public interface Household<T extends HouseholdParticipant> extends Population<T>
{

	HouseholdPopulation<T> population();

	@SuppressWarnings( "unchecked" )
	default void onMoveHouse( final T homeLeaver )
	{
		Objects.requireNonNull( homeLeaver );
		emit( DemographicEvent.Builder.of( MoveHouse.class, now() )
				.withDepartures( Collections.singleton( homeLeaver ) )
				.build() );
	}

	@SuppressWarnings( "unchecked" )
	default void onAbandoned()
	{
		emit( DemographicEvent.Builder.of( Abandon.class, now() ).build() );
	}

	/**
	 * {@link MoveHouse}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public class MoveHouse<T extends Participant> extends DemographicEvent<T>
	{
	}

	/**
	 * {@link MoveHouse}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public class Abandon<T extends Participant> extends DemographicEvent<T>
	{
	}

	static <T extends HouseholdParticipant> Household<T> of( final String name,
		final HouseholdPopulation<T> population, final RxCollection<T> members )
	{
		final Household<T> result = new Household<T>()
		{
			private final ID id = ID.of( name );

			private final Subject<DemographicEvent<T>, DemographicEvent<T>> events = PublishSubject
					.create();

			@Override
			public Scheduler scheduler()
			{
				return population.scheduler();
			}

			@Override
			public HouseholdPopulation<T> population()
			{
				return population;
			}

			@Override
			public RxCollection<T> members()
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
		};
		population.households().add( result );
		result.events().subscribe( event ->
		{
			population.emit( event );
		}, error ->
		{
		} );
		return result;
	}
}
