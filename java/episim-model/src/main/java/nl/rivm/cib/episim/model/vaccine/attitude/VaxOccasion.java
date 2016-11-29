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
package nl.rivm.cib.episim.model.vaccine.attitude;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;

/**
 * The convenience of a {@link VaxOccasion} depends on the {@link VaxHesitancy}'s
 * judgment in terms of its (relative) "physical availability, affordability and
 * willingness-to-pay, geographical accessibility, ability to understand
 * (language and health literacy) and appeal of immunization service" (MacDonald
 * et al., 2015)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class VaxOccasion
{

	enum Index
	{
		PROXIMITY, CLARITY, UTILITY, AFFINITY;
	}

	public static VaxOccasion of( final Number proximity, final Number clarity,
		final Number utility, final Number affinity )
	{
		final VaxOccasion result = new VaxOccasion();
		result.values[Index.PROXIMITY.ordinal()] = DecimalUtil
				.valueOf( proximity );
		result.values[Index.CLARITY.ordinal()] = DecimalUtil.valueOf( clarity );
		result.values[Index.UTILITY.ordinal()] = DecimalUtil.valueOf( utility );
		result.values[Index.AFFINITY.ordinal()] = DecimalUtil
				.valueOf( affinity );
		return result;
	}

	@JsonProperty
	private BigDecimal[] values = new BigDecimal[Index.values().length];

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of physical
	 *         availability and geographical accessibility, given mobility
	 */
	@JsonIgnore
	public BigDecimal getProximity()
	{
		return this.values[Index.PROXIMITY.ordinal()];
	}

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of language
	 *         understandability, given health literacy
	 */
	@JsonIgnore
	public BigDecimal getClarity()
	{
		return this.values[Index.CLARITY.ordinal()];
	}

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of (social)
	 *         affordability (given willingness-to-pay)
	 */
	@JsonIgnore
	public BigDecimal getUtility()
	{
		return this.values[Index.UTILITY.ordinal()];
	}

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of vaccination service
	 *         appeal
	 */
	@JsonIgnore
	public BigDecimal getAffinity()
	{
		return this.values[Index.AFFINITY.ordinal()];
	}

	@Override
	public String toString()
	{
		return JsonUtil.stringify( this );
	}
}