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

	private Extreme<T> maximum;

	private Extreme<T> minimum;

	public Range( final Extreme<T> minimum, final Extreme<T> maximum )
	{
		if( minimum == null || maximum == null
				|| maximum.compareTo( minimum ) < 0 )
			throw ExceptionFactory.createUnchecked(
					"minimum {} greater than maximum {}", minimum, maximum );

		this.minimum = minimum;
		this.maximum = maximum;
	}

	/** @return the minimum value, or {@code null} for (negative) infinity */
	public Extreme<T> getMinimum()
	{
		return this.minimum;
	}

	/** @return the maximum value, or {@code null} for (positive) infinity */
	public Extreme<T> getMaximum()
	{
		return this.maximum;
	}

	/**
	 * @param value the {@link T} to test
	 * @return {@code true} iff this {@link Range} has a finite minimum that is
	 *         greater than specified value, {@code false} otherwise
	 */
	public boolean isGreaterThan( final T value )
	{
		if( this.minimum.isNegativeInfinity() ) return false;
		return getMinimum().isInclusive()
				? Comparison.of( value,
						getMinimum().getValue() ) == Comparison.LESS
				: Comparison.of( value,
						getMinimum().getValue() ) != Comparison.GREATER;
	}

	/**
	 * @param value the {@link T} to test
	 * @return {@code true} iff this {@link Range} has a finite maximum that is
	 *         smaller than specified value, {@code false} otherwise
	 */
	public boolean isLessThan( final T value )
	{
		if( getMaximum() == null ) return false;
		return getMaximum().isInclusive()
				? Comparison.of( value,
						getMaximum().getValue() ) == Comparison.GREATER
				: Comparison.of( value,
						getMaximum().getValue() ) != Comparison.LESS;
	}

	/**
	 * @param value the {@link T} to test
	 * @return {@code true} iff this {@link Range} contains specified value
	 */
	public Boolean contains( final T value )
	{
		return !isGreaterThan( value ) && !isLessThan( value );
	}

	public boolean overlaps( final Range<T> that )
	{
		return intersect( that ) != null;
	}

	public Range<T> intersect( final Range<T> that )
	{
		return of( Comparison.max( this.getMinimum(), that.getMinimum() ),
				Comparison.min( this.getMaximum(), that.getMaximum() ) );
	}

	public static <T extends Comparable<T>> Range<T> infinite()
	{
		return of( null, null, null, null );
	}

	/**
	 * @param minimum
	 * @return a {@link Range} representing <code>[x,&rarr;)</code>
	 */
	public static <T extends Comparable<T>> Range<T>
		upFromAndIncluding( final T minimum )
	{
		return upFrom( minimum, true );
	}

	/**
	 * @param minimum
	 * @param minimumInclusive
	 * @return a {@link Range} representing <code>[x,&rarr;)</code> or
	 *         <code>(x,&rarr;)</code>
	 */
	public static <T extends Comparable<T>> Range<T> upFrom( final T minimum,
		final Boolean minimumInclusive )
	{
		return of( Extreme.lower( minimum, minimumInclusive ),
				Extreme.positiveInfinity() );
	}

	/**
	 * @param maximum
	 * @return a {@link Range} representing <code>(&larr;,x]</code>
	 */
	public static <T extends Comparable<T>> Range<T>
		upToAndIncluding( final T maximum )
	{
		return upFrom( maximum, true );
	}

	/**
	 * @param maximum
	 * @param maximumInclusive
	 * @return a {@link Range} representing <code>(&larr;,x]</code> or
	 *         <code>(&larr;,x)</code>
	 */
	public static <T extends Comparable<T>> Range<T> upTo( final T maximum,
		final Boolean maximumInclusive )
	{
		return of( Extreme.negativeInfinity(),
				Extreme.upper( maximum, maximumInclusive ) );
	}

	/**
	 * @param value the value (inclusive) or {@code null} for infinite range
	 * @return the {@link Range} instance
	 */
	public static <T extends Comparable<T>> Range<T> of( final T value )
	{
		return of( value, true, value, true );
	}

	/**
	 * @param minimum the lower bound (inclusive) or {@code null} for infinite
	 * @param maximum the upper bound (inclusive) or {@code null} for infinite
	 * @return the {@link Range} instance
	 */
	public static <T extends Comparable<T>> Range<T> of( final T minimum,
		final T maximum )
	{
		return of( minimum, true, maximum, true );
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
		return of( Extreme.lower( minimum, minimumInclusive ),
				Extreme.upper( maximum, maximumInclusive ) );
	}

	/**
	 * @param minimum the lower {@link Extreme}
	 * @param maximum the upper {@link Extreme}
	 * @return the {@link Range} instance
	 */
	public static <T extends Comparable<T>> Range<T>
		of( final Extreme<T> minimum, final Extreme<T> maximum )
	{
		return new Range<T>( minimum, maximum );
	}
}
