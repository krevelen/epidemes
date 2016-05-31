package nl.rivm.cib.episim.model;

import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

/**
 * {@link Units}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Units
{

	public static final Unit<?> PER_KM2 = SI.KILOMETER.pow( -2 );

	/** a {@link Frequency} expressed as an amount per {@link NonSI#DAY} */
	public static final Unit<Frequency> DAILY = NonSI.DAY.inverse()
			.asType( Frequency.class );

	/**
	 * a {@link Frequency} expressed as an amount per
	 * {@link NonSI#YEAR_CALENDAR} (annum = 365 days)
	 */
	public static final Unit<Frequency> ANNUAL = NonSI.YEAR_CALENDAR.inverse()
			.asType( Frequency.class );

	static
	{
		UnitFormat.getInstance().alias( DAILY, "daily" );
		UnitFormat.getInstance().label( DAILY, "daily" );
	}

}
