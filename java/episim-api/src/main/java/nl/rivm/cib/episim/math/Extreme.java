package nl.rivm.cib.episim.math;

/**
 * {@link Extreme}
 * 
 * @param <T>
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Extreme<T extends Comparable<T>> implements Comparable<Extreme<T>>
{

	public enum Inclusiveness
	{
		/** */
		INCLUSIVE,

		/** */
		EXCLUSIVE;

		public static Inclusiveness of( final Boolean inclusive )
		{
			return inclusive == null ? null : inclusive ? INCLUSIVE : EXCLUSIVE;
		}
	}

	/**
	 * {@link BoundaryPosition} refers to an extreme of some linear
	 * {@link Range}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public enum BoundaryPosition
	{

		/** also has lower {@link #ordinal()} for natural ordering */
		LOWER( Comparison.LESS ),

		/** also has higher {@link #ordinal()} for natural ordering */
		UPPER( Comparison.GREATER );

		/** the infinity comparison value */
		private final Comparison limitComparison;

		private BoundaryPosition( final Comparison infinity )
		{
			this.limitComparison = infinity;
		}

		/**
		 * @return the {@link Comparison} for finite values at the (infinite)
		 *         limit of this {@link BoundaryPosition} position
		 */
		public Comparison compareLimit()
		{
			return this.limitComparison;
		}
	}

	private final T value;

	private final Inclusiveness inclusive;

	private final BoundaryPosition boundary;

	public Extreme( final T value, final Inclusiveness inclusiveness,
		final BoundaryPosition position )
	{
		this.value = value;
		this.inclusive = value == null ? null : inclusiveness;
		this.boundary = position;
	}

	public T getValue()
	{
		return this.value;
	}

	/** @return {@code true} iff this value represents INFINITY */
	public boolean isInfinity()
	{
		return this.value == null;
	}

	/** @return {@code true} iff this value represents POSITIVE INFINITY */
	public boolean isPositiveInfinity()
	{
		return isInfinity() && isUpperBoundary();
	}

	/** @return {@code true} iff this value represents NEGATIVE INFINITY */
	public boolean isNegativeInfinity()
	{
		return isInfinity() && isLowerBoundary();
	}

	public boolean isInclusive()
	{
		return this.inclusive == Inclusiveness.INCLUSIVE;
	}

	public boolean isExclusive()
	{
		return this.inclusive == Inclusiveness.EXCLUSIVE;
	}

	public boolean isUpperBoundary()
	{
		return this.boundary == BoundaryPosition.UPPER;
	}

	public boolean isLowerBoundary()
	{
		return this.boundary == BoundaryPosition.LOWER;
	}

	public Comparison compareLimit()
	{
		return this.boundary.compareLimit();
	}

	@Override
	public int compareTo( final Extreme<T> that )
	{
		return compareWith( that ).toInt();
	}

	/**
	 * @param that
	 * @return
	 */
	public Comparison compareWith( final Extreme<T> that )
	{
		if( isInfinity() ) return that.isInfinity()
				? Comparison.of( this.boundary, that.boundary )
				: this.compareLimit();

		if( that.isInfinity() ) return that.compareLimit();

		final Comparison valueCmp = Comparison.of( this.value, that.value );
		if( valueCmp != Comparison.EQUIVALENT ) return valueCmp;

		// equivalent values, check inclusiveness
		if( isInclusive() && !that.isInclusive() ) return compareLimit();

		if( !isInclusive() && that.isInclusive() ) return that.compareLimit();

		return Comparison.EQUIVALENT;
	}

	public static <T extends Comparable<T>> Extreme<T> negativeInfinity()
	{
		return of( null, null, BoundaryPosition.LOWER );
	}

	public static <T extends Comparable<T>> Extreme<T> positiveInfinity()
	{
		return of( null, null, BoundaryPosition.UPPER );
	}

	public static <T extends Comparable<T>> Extreme<T> lower( final T value,
		final Boolean inclusive )
	{
		return of( value, Inclusiveness.of( inclusive ),
				BoundaryPosition.LOWER );
	}

	public static <T extends Comparable<T>> Extreme<T> upper( final T value,
		final Boolean inclusive )
	{
		return of( value, Inclusiveness.of( inclusive ),
				BoundaryPosition.UPPER );
	}

	public static <T extends Comparable<T>> Extreme<T> of( final T value,
		final Inclusiveness inclusiveness, final BoundaryPosition position )
	{
		return new Extreme<T>( value, inclusiveness, position );
	}
}