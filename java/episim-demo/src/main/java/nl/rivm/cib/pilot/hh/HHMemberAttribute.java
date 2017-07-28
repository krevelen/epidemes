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
package nl.rivm.cib.pilot.hh;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;

import io.coala.config.Jsonifiable;

/**
 * {@link HHMemberAttribute}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum HHMemberAttribute implements Jsonifiable
{
	/** population-unique identifier (may be replaced upon death/emigration) */
	IDENTIFIER,

	/** {@link BigDecimal} time of birth in years since simulation start (0) */
	BIRTH,

	/** boolean indicating gender is male or not */
	MALE,

	/** the ordinal of a {@link HHMemberStatus} constant */
	STATUS,

	/** the mobility identifier (one per region/behavior combo) */
	MOTOR_REF,

	/** amount of exposure time remaining until invasion/infection occurs */
	SUSCEPTIBLE_DAYS,

	;

	private String json = null;

	@Override
	public String stringify()
	{
		return this.json == null
				? (this.json = name().toLowerCase().replace( '_', '-' ))
				: this.json;
	}
	
	public Object get( final Matrix data, final long hhIndex )
	{
		return data.getAsObject( hhIndex, ordinal() );
	}

	public static Map<HHMemberAttribute, Object> toMap( final Matrix data,
		final long hhIndex, final HHMemberAttribute... attribute )
	{
		return Arrays.stream( Objects.requireNonNull( attribute ) )
				.collect( Collectors.toMap( att -> att,
						att -> att.get( data, hhIndex ), ( att1, att2 ) -> att1,
						() -> new EnumMap<>( HHMemberAttribute.class ) ) );
	}
}
