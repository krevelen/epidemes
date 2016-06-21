package nl.rivm.cib.episim.time;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

import io.coala.exception.ExceptionFactory;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.util.Caller;

/**
 * {@link Schedule} is a mapping of values occurring from some {@link Instant}s
 * 
 * @param <T>
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Schedule<T>
{

	/** the mapping function of values occurring from some {@link Instant}s */
	private NavigableMap<Instant, T> function;

	public static <T> Schedule<T> of( final Instant offset,
		final Map<Duration, T> function )
	{
		final NavigableMap<Instant, T> absolute = new ConcurrentSkipListMap<>();
		for( Entry<Duration, T> entry : function.entrySet() )
			absolute.put( offset.add( entry.getKey() ), entry.getValue() );
		return of( absolute );
	}

	public static <T> Schedule<T> of( final Map<Instant, T> function )
	{
		return of( function instanceof NavigableMap
				? (NavigableMap<Instant, T>) function
				: new ConcurrentSkipListMap<>( function ) );
	}

	public static <T> Schedule<T> of( final NavigableMap<Instant, T> function )
	{
		if( function.isEmpty() )
			throw ExceptionFactory.createUnchecked( "Can't be empty" );
		final Schedule<T> result = new Schedule<T>();
		result.function = function;
		return result;
	}

	/**
	 * @param t the {@link Instant} to check in this {@link Schedule}
	 * @return the value occurring at specified {@link Instant}
	 */
	public T floor( final Instant t )
	{
		final Entry<Instant, T> lastBefore = this.function.floorEntry( t );
		return lastBefore == null ? null : lastBefore.getValue();
	}

	/**
	 * @param scheduler the {@link Scheduler} for the timing of calls
	 * @param handler the {@link Consumer} of new values to call
	 */
	// FIXME make Throwable's observable, e.g. using scheduler::atEach?
	public void handle( final Scheduler scheduler, final Consumer<T> handler )
	{
		for( Entry<Instant, T> entry : this.function.tailMap( scheduler.now() )
				.entrySet() )
			scheduler.at( entry.getKey() )
					.call( Caller.of( handler, entry.getValue() )::run );
	}

	public void put( final Instant when, final T value )
	{
		this.function.put( when, value );
	}
}