/* $Id$
 * 
 * Part of ZonMW project no. 50-53000-98-156
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2016 RIVM National Institute for Health and Environment 
 */
package nl.rivm.cib.episim.hesitant;

import java.math.BigDecimal;

import io.coala.math.DecimalUtil;

/**
 * The {@link Level} represents ordinal preferences on a three-point Likert
 * scale with declared (crisp) {@link #toNumber()} values:
 * <p>
 * {@link #LO} = 0, {@link #MID} = .5, and {@link #HI} = 1.
 * <p>
 * The {@link #MID} range boundaries used during conversion by
 * {@link #valueOf(Number)}, i.e. {@link #MID_LOWER}=0.25 and
 * {@link #MID_UPPER}=0.75, are both <em>exclusive</em> in order to maintain
 * inversion symmetry:
 * <p>
 * <code>{@link #LO} &le; {@link #MID_LOWER} &lt; {@link #MID} &lt; {@link #MID_UPPER} &le; {@link #HI}</code>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum Level
{
	/** {@link #toNumber()}=0 */
	LO,

	/** {@link #toNumber()}=0.5 */
	MID,

	/** {@link #toNumber()}=1 */
	HI;

	public static final BigDecimal MID_LOWER = BigDecimal.valueOf( 25, 2 );

	public static final BigDecimal MID_UPPER = BigDecimal.valueOf( 75, 2 );

	/**
	 * The {@link #MID} range boundaries used during conversion by
	 * {@link #valueOf(Number)}, i.e. {@link #MID_LOWER}=0.25 and
	 * {@link #MID_UPPER}=0.75, are both <em>exclusive</em> in order to maintain
	 * inversion symmetry:
	 * <p>
	 * <code>{@link #LO} &le; {@link #MID_LOWER} &lt; {@link #MID} &lt; {@link #MID_UPPER} &le; {@link #HI}</code>
	 * 
	 * @param value the {@link Number} to map
	 * @return the respective {@link Level}
	 */
	public static Level valueOf( final Number value )
	{
		final BigDecimal d = DecimalUtil.valueOf( value );
		final Level result = d.compareTo( MID_LOWER ) > 0
				? d.compareTo( MID_UPPER ) < 0 ? MID : HI : LO;
		return result;
	}

	/**
	 * @return {@link #HI}&harr;{@link #LO}
	 */
	public Level invert()
	{
		return values()[values().length - 1 - ordinal()];
	}

	private static final BigDecimal MAX = BigDecimal
			.valueOf( values().length - 1 );

	private BigDecimal number = null;

	public BigDecimal toNumber()
	{
		return this.number != null ? this.number
				: (this.number = DecimalUtil.divide( ordinal(), MAX ));
	}
}