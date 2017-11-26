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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;

import io.coala.config.Jsonifiable;
import nl.rivm.cib.json.RelationFrequencyJson;

/**
 * {@link HHAttribute}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum HHAttribute implements Jsonifiable
{
	/** population-unique identifier (may be replaced upon death/emigration) */
	IDENTIFIER,

	/** simulation time of household creation */
	SINCE_DAYS,

	/** in-group identifier determines group/attractor/authority */
	ATTRACTOR_REF,

	/** {@link Matrix} hh indices {@link Matrix#getAsLong} */
	SOCIAL_NETWORK_SIZE,

	/** number of propagations */
	IMPRESSION_ROUNDS,

	/** drawn from CBS social contact profile {@link RelationFrequencyJson} */
	IMPRESSION_PERIOD_DAYS,

	/** cumulative impression peers per propagation round */
	IMPRESSION_FEEDS,

	/** in-group peer pressure */
	IMPRESSION_INPEER_WEIGHT,

	/** out-group peer pressure */
	IMPRESSION_OUTPEER_WEIGHT,

	/** own resolve */
	IMPRESSION_SELF_MULTIPLIER,

	/** coherence */
	IMPRESSION_ATTRACTOR_MULTIPLIER,

	/**
	 * social <a
	 * href=https://www.wikiwand.com/en/Assortativity>assortativity</a> &isin;
	 * [0,1] representing <a
	 * href=https://www.wikiwand.com/en/Homophily>homophily</a> in peer pressure
	 */
	SOCIAL_ASSORTATIVITY,

	/**
	 * school <a
	 * href=https://www.wikiwand.com/en/Assortativity>assortativity</a> &isin;
	 * [0,1] representing <a
	 * href=https://www.wikiwand.com/en/Homophily>homophily</a> in transmission
	 */
	SCHOOL_ASSORTATIVITY,

	/** {@link BigDecimal} &isin; [0,1] */
	CALCULATION,

	/** {@link BigDecimal} &isin; [0,1] */
	CONFIDENCE,

	/** {@link BigDecimal} &isin; [0,1] */
	COMPLACENCY,

	/** {@link Long} member row-index */
	REFERENT_REF,

	/** {@link Long} member row-index, or -1 for N/A */
	PARTNER_REF,

	/** {@link Long} member row-index, or -1 for N/A */
	CHILD1_REF,

	/** {@link Long} member row-index, or -1 for N/A */
	CHILD2_REF,

	/** {@link Long} member row-index, or -1 for N/A */
	CHILD3_REF,

	;

	public static <T> Map<HHAttribute, T> toMap(
		final Function<Integer, T> data, final HHAttribute... attributeFilter )
	{
		return Arrays
				.stream( attributeFilter == null || attributeFilter.length == 0
						? values() : attributeFilter )
				.collect( Collectors.toMap( att -> att,
						att -> data.apply( att.ordinal() ),
						( att1, att2 ) -> att1,
						() -> new EnumMap<>( HHAttribute.class ) ) );
	}

	private String json = null;

	@Override
	public String stringify()
	{
		return this.json == null
				? (this.json = name().toLowerCase().replace( '_', '-' ))
				: this.json;
	}
}
