package nl.rivm.cib.epidemes.model;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import io.coala.math.Range;
import tec.uom.se.ComparableQuantity;

public interface VaxRegimen
{
	boolean isCompliant( int vaxStatus );

	/** @return the NIP (default) schedule's next dose */
	VaxDose nextRegular( int vaxStatus, Quantity<Time> age );

	/** @return the alternative (outbreak, ZRA) schedule's next dose */
	VaxDose nextSpecial( int vaxStatus, Quantity<Time> age );

	Range<ComparableQuantity<Time>> decisionAgeRange();
}