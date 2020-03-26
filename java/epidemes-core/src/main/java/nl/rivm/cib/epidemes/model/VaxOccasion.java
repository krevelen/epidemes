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
package nl.rivm.cib.epidemes.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.math.DecimalUtil;

/**
 * The convenience of a {@link VaxOccasion} depends on the
 * {@link VaxHesitancy}'s judgment in terms of its (relative) "physical
 * availability, affordability and willingness-to-pay, geographical
 * accessibility, ability to understand (language and health literacy) and
 * appeal of immunization service" (MacDonald et al., 2015)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface VaxOccasion
{

	enum Index
	{
		/** physical availability, affordability and willingness-to-pay */
		UTILITY,

		/** geographical accessibility */
		PROXIMITY,

		/** ability to understand (language and health literacy) */
		CLARITY,

		/** appeal of immunization service */
		AFFINITY;
	}

	Map<Index, BigDecimal> asMap();

	default Collection<BigDecimal> getConvenience()
	{
		return asMap().values();
	}

	@JsonProperty
	default void setConvenience( final List<BigDecimal> list )
	{
		for( Index factor : Index.values() )
			asMap().put( factor, list.get( factor.ordinal() ) );
	}

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of (social)
	 *         affordability (given willingness-to-pay)
	 */
	default BigDecimal utility()
	{
		return asMap().get( Index.UTILITY );
	}

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of physical
	 *         availability and geographical accessibility, given mobility
	 */
	default BigDecimal proximity()
	{
		return asMap().get( Index.PROXIMITY );
	}

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of language
	 *         understandability, given health literacy
	 */
	default BigDecimal clarity()
	{
		return asMap().get( Index.CLARITY );
	}

	/**
	 * @return the {@link BigDecimal level &isin; [0,1]} of vaccination service
	 *         appeal
	 */
	default BigDecimal affinity()
	{
		return asMap().get( Index.AFFINITY );
	}

	public static VaxOccasion of( final Number utility, final Number proximity,
		final Number clarity, final Number affinity )
	{
		final Map<Index, BigDecimal> map = new EnumMap<>( Index.class );
		map.put( Index.UTILITY, DecimalUtil.valueOf( utility ) );
		map.put( Index.PROXIMITY, DecimalUtil.valueOf( proximity ) );
		map.put( Index.CLARITY, DecimalUtil.valueOf( clarity ) );
		map.put( Index.AFFINITY, DecimalUtil.valueOf( affinity ) );
		return () -> map;
	}
}