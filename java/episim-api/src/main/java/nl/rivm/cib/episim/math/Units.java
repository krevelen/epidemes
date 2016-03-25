package nl.rivm.cib.episim.math;

import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;
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

	/** a {@link Frequency} expressed as an amount per {@link NonSI#DAY} */
	public static final Unit<Frequency> DAILY = NonSI.DAY.pow( -1 )
			.asType( Frequency.class );

	static
	{
		UnitFormat.getInstance().alias( DAILY, "daily" );
		UnitFormat.getInstance().label( DAILY, "daily" );
	}

}
