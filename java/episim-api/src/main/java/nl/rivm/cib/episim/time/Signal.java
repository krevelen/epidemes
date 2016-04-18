package nl.rivm.cib.episim.time;

import java.util.function.Function;

import io.coala.exception.ExceptionFactory;
import io.coala.math.Range;
import io.coala.time.x.Instant;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Signal} evaluates to a value for some (in)finite interval
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Signal<T> extends Timed
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

	<U> Signal<U> transform( Function<T, U> transform );

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
	 * {@link Simple} implementation of {@link Signal}
	 * 
	 * @param <T> the type of value being signaled
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple<T> implements Signal<T>
	{

		public static <T> Signal<T> of( final Scheduler scheduler,
			final T constant )
		{
			return of( scheduler, Range.infinite(),
					new TimeInvariant<T>( constant )::get );
		}

		public static <T> Simple<T> of( final Scheduler scheduler,
			final Range<Instant> domain, final Function<Instant, T> function )
		{
			return new Simple<T>( scheduler, domain, function );
		}

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

		@Override
		public <U> Signal<U> transform( Function<T, U> transform )
		{
			return of( scheduler(), getDomain(),
					partialTransform( getFunction(), transform ) );
		}

		static <T, U> Function<Instant, U> partialTransform(
			final Function<Instant, T> function,
			final Function<T, U> transform )
		{
			return ( instant ) -> transform.apply( function.apply( instant ) );
		}
	}

	/**
	 * {@link SimpleOrdinal} is a {@link Simple} {@link Signal} of
	 * {@link Comparable} values
	 * 
	 * @param <T> the type of {@link Comparable} value being signaled
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class SimpleOrdinal<T extends Comparable<? super T>> extends Simple<T>
		implements Comparable<SimpleOrdinal<T>>
	{

		public SimpleOrdinal( final Scheduler scheduler,
			final Range<Instant> domain, final Function<Instant, T> function )
		{
			super( scheduler, domain, function );
		}

		@Override
		public int compareTo( final SimpleOrdinal<T> o )
		{
			return getValue().compareTo( o.getValue() );
		}
	}
}