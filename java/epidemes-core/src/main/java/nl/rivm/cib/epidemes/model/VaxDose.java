package nl.rivm.cib.epidemes.model;

import javax.measure.quantity.Time;

import io.coala.math.Range;
import tec.uom.se.ComparableQuantity;

public interface VaxDose
{
	VaxRegimen regimen();

	int bit();

	/** @return {@code true} iff this dose bit is 1 in given status */
	default boolean isFlippedOn( final int vaxStatus )
	{
		return !isFlippedOff( vaxStatus );
	}

	default boolean isFlippedOff( final int vaxStatus )
	{
		return (vaxStatus & bit()) == 0;
	}

	/** @return the status value after setting bit for this dose to 1 */
	default int flippedOn( final int vaxStatus )
	{
		return vaxStatus | bit();
	}

	default int flippedOff( final int vaxStatus )
	{
		return vaxStatus & ~bit();
	}

	Range<ComparableQuantity<Time>> ageRangeNormal();

	Range<ComparableQuantity<Time>> ageRangeSpecial();
}