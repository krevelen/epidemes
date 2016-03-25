package nl.rivm.cib.episim.math;

import java.util.function.BiFunction;
import java.util.function.Function;

import io.coala.exception.ExceptionFactory;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Signal} evaluates to a value for some (in)finite interval
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
interface Signal<T> extends Timed
{

	/** @return the domain interval as {@link Range} of {@link Instant}s */
	Range<Instant> getDomain();

	/** @return the {@link Function} generating the signaled values */
	Function<Instant, T> getFunction();

	/**
	 * @return the evaluated result, or {@code null} if not in
	 *         {@link #getDomain()}
	 */
	T getValue();

	/** @return an {@link Observable} stream of {@link T} evaluations */
	Observable<T> emitValues();

	default <U> Signal<T> aggregate( final Signal<U> secondary,
		final BiFunction<T, U, T> aggregator )
	{
		return new Simple<T>( scheduler(),
				getDomain().intersect( secondary.getDomain() ),
				new Aggregator<T, U>( this, secondary,
						aggregator )::aggregated );
	}

	static <T> Signal<T> of( final Scheduler scheduler, final T constant )
	{
		return of( scheduler, Range.infinite(),
				new TimeInvariant<T>( constant )::get );
	}

	static <T> Signal<T> of( final Scheduler scheduler,
		final Range<Instant> domain, final Function<Instant, T> function )
	{
		return new Simple<T>( scheduler, domain, function );
	}

	/**
	 * {@link TimeInvariant}
	 * 
	 * @param <T>
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public static class TimeInvariant<T>
	{
		private T value;

		public TimeInvariant( final T value )
		{
			set( value );
		}

		public synchronized void set( final T value )
		{
			this.value = value;
		}

		public synchronized T get( final Instant t )
		{
			return this.value;
		}
	}

	/**
	 * {@link Aggregator}
	 * 
	 * @param <T> the type of values from the primary {@link Signal}
	 * @param <U> the type of values from the secondary {@link Signal}
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Aggregator<T, U>
	{
		private final Signal<T> primary;

		private final Signal<U> secondary;

		private final BiFunction<T, U, T> function;

		public Aggregator( final Signal<T> primary, final Signal<U> secondary,
			final BiFunction<T, U, T> aggregator )
		{
			this.primary = primary;
			this.secondary = secondary;
			this.function = aggregator;
		}

		public T aggregated( final Instant t )
		{
			return this.function.apply( this.primary.getFunction().apply( t ),
					this.secondary.getFunction().apply( t ) );
		}
	}

	/**
	 * {@link Simple} implementation of {@link Signal}
	 * 
	 * @param <T> the type of value being signaled
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple<T> implements Signal<T>
	{

		private transient final Subject<T, T> values = PublishSubject.create();

		private transient final Scheduler scheduler;

		private final Range<Instant> domain;

		private final Function<Instant, T> function;

		private volatile Instant now;

		private volatile T value;

		public Simple( final Scheduler scheduler, final Range<Instant> domain,
			final Function<Instant, T> function )
		{
			if( domain.isGreaterThan( scheduler.now() ) ) throw ExceptionFactory
					.createUnchecked( "Currently t={} past domain: {}",
							scheduler.now(), domain );
			this.scheduler = scheduler;
			this.domain = domain;
			this.function = function;
		}

		@Override
		public String toString()
		{
			final T value = getValue();
			return value == null ? null : value.toString();
		}

		@Override
		public int hashCode()
		{
			final T value = getValue();
			return value == null ? 0 : value.hashCode();
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Range<Instant> getDomain()
		{
			return this.domain;
		}

		@Override
		public Function<Instant, T> getFunction()
		{
			return this.function;
		}

		@Override
		public T getValue()
		{
			if( this.now == null || !this.now.equals( now() ) )
			{
				this.now = now();
				if( this.domain.isGreaterThan( this.now ) )
				{
					this.value = null;
				} else if( this.domain.isLessThan( this.now ) )
				{
					if( this.value == null ) this.values.onCompleted();
					this.value = null;
				} else
				{
					final T newValue = this.function.apply( this.now );
					if( (newValue == null && this.value != null)
							|| (newValue != null
									&& !newValue.equals( this.value )) )
					{
						this.value = newValue;
						this.values.onNext( this.value );
					}
				}
			}
			return this.value;
		}

		@Override
		public Observable<T> emitValues()
		{
			return this.values.asObservable();
		}

		/**
		 * @param target the {@link T} to equate
		 * @return a filtered mapping of {@link #emitValues()} to an
		 *         {@link Observable} stream of all {@link Instant}s when this
		 *         {@link Signal}'s {@link #getValue()} equals specified
		 *         {@code target}
		 */
		public Observable<Instant> emitEquals( final T target )
		{
			return emitValues().filter( ( final T value ) ->
			{
				return value.equals( target );
			} ).map( ( final T value ) ->
			{
				return now();
			} );
		}
	}

	/**
	 * {@link Ordinal} is a {@link Simple} {@link Signal} of {@link Comparable}
	 * values
	 * 
	 * @param <T> the type of {@link Comparable} value being signaled
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Ordinal<T extends Comparable<? super T>> extends Simple<T>
		implements Comparable<Ordinal<T>>
	{

		public Ordinal( final Scheduler scheduler, final Range<Instant> domain,
			final Function<Instant, T> function )
		{
			super( scheduler, domain, function );
		}

		@Override
		public int compareTo( final Ordinal<T> o )
		{
			return getValue().compareTo( o.getValue() );
		}

		/**
		 * @param target the {@link Comparable} to equate
		 * @return a filtered mapping of {@link #emitValues()} to an
		 *         {@link Observable} stream of all {@link Instant}s when this
		 *         {@link Indicator}'s {@link #getValue()} is ordinally
		 *         equivalent to specified {@code target}
		 */
		@SuppressWarnings( "unchecked" )
		public <S extends Comparable<S>> Observable<Instant>
			emitEquivalent( final S target )
		{
			return emitValues().filter( ( final T value ) ->
			{
				return Comparison.of( (S) value,
						target ) == Comparison.EQUIVALENT;
			} ).map( ( final T value ) ->
			{
				return now();
			} );
		}

		/**
		 * @param range the {@link Range} of {@link Comparable} values
		 * @return a filtered {@link Observable} stream of {@link #getValue()}s
		 *         contained by specified {@link Range}
		 */
		@SuppressWarnings( "unchecked" )
		public <S extends Comparable<S>> Observable<T>
			emitInside( final Range<S> range )
		{
			return emitValues().filter( ( final T value ) ->
			{
				return range.contains( (S) value );
			} );
		}

		/**
		 * @param range the {@link Range} of {@link Comparable} values
		 * @return a filtered {@link Observable} stream of {@link #getValue()}s
		 *         NOT contained by specified {@link Range}
		 */
		@SuppressWarnings( "unchecked" )
		public <S extends Comparable<S>> Observable<T>
			emitOutside( final Range<S> range )
		{
			return emitValues().filter( ( final T value ) ->
			{
				return !range.contains( (S) value );
			} );
		}

	}

}