package nl.rivm.cib.episim.math;

import io.coala.exception.ExceptionFactory;

/**
 * {@link Range}
 * 
 * @param <T>
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Range<T extends Comparable<T>>
{

	private T minimum;

	private Boolean minimumInclusive;

	private T maximum;

	private Boolean maximumInclusive;

	public Range( final T minimum, final Boolean minimumInclusive,
		final T maximum, final Boolean maximumInclusive )
	{
		if( minimum != null && maximum != null
				&& maximum.compareTo( minimum ) < 0 )
			throw ExceptionFactory.createUnchecked(
					"minimum {} greater than maximum {}", minimum, maximum );

		this.minimum = minimum;
		this.minimumInclusive = minimumInclusive;
		this.maximum = maximum;
		this.maximumInclusive = maximumInclusive;
	}

	public T getMinimum()
	{
		return this.minimum;
	}

	public Boolean isMinimumInclusive()
	{
		return this.minimumInclusive;
	}

	public T getMaximum()
	{
		return this.maximum;
	}

	public Boolean isMaximumInclusive()
	{
		return this.maximumInclusive;
	}

	/**
	 * @param value the {@link Comparable} to test
	 * @return {@code true} iff this {@link Range} has a finite minimum that is
	 *         greater than specified value, {@code false} otherwise
	 */
	public boolean isGreaterThan( final T value )
	{
		if( getMinimum() == null ) return false;
		final int minimumCompare = value.compareTo( getMinimum() );
		return isMinimumInclusive() ? minimumCompare < 0 : minimumCompare <= 0;
	}

	/**
	 * @param value the {@link Comparable} to test
	 * @return {@code true} iff this {@link Range} has a finite maximum that is
	 *         smaller than specified value, {@code false} otherwise
	 */
	public boolean isLessThan( final T value )
	{
		if( getMaximum() == null ) return false;
		final int maximumCompare = value.compareTo( getMaximum() );
		return isMaximumInclusive() ? maximumCompare > 0 : maximumCompare >= 0;
	}

	/**
	 * @param value the {@link Comparable} to test
	 * @return {@code true} iff this {@link Range} contains specified value
	 */
	public Boolean contains( final T value )
	{
		return !isGreaterThan( value ) && !isLessThan( value );
	}

	/**
	 * @param value the value (inclusive) or {@code null} for infinite range
	 * @return the {@link Range} instance
	 */
	public static <T extends Comparable<T>> Range<T> of( final T value )
	{
		return of( value, Boolean.TRUE, value, Boolean.TRUE );
	}

	/**
	 * @param minimum the lower bound (inclusive) or {@code null} for infinite
	 * @param maximum the upper bound (inclusive) or {@code null} for infinite
	 * @return the {@link Range} instance
	 */
	public static <T extends Comparable<T>> Range<T> of( final T minimum,
		final T maximum )
	{
		return of( minimum, Boolean.TRUE, maximum, Boolean.TRUE );
	}

	/**
	 * @param minimum the lower bound or {@code null} for infinite
	 * @param minimumInclusive whether the lower bound is inclusive
	 * @param maximum the upper bound or {@code null} for infinite
	 * @param maximumInclusive whether the upper bound is inclusive
	 * @return the {@link Range} instance
	 */
	public static <T extends Comparable<T>> Range<T> of( final T minimum,
		final Boolean minimumInclusive, final T maximum,
		final Boolean maximumInclusive )
	{
		return new Range<T>( minimum, minimumInclusive, maximum,
				maximumInclusive );
	}
}
