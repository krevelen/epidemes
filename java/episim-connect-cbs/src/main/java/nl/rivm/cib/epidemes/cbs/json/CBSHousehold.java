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
package nl.rivm.cib.epidemes.cbs.json;

/**
 * {@link CBSHousehold} the JSON 'implementation' of a CBS household composition
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum CBSHousehold implements CBSHouseholdCompositionType
{
	/** total households == 'hh_l' + 'hh_p_0' + 'hh_p_1+' == 'hh_l' + 'hh_b' + 'hh_m' + 'hh_s' + 'hh_o' */
	TOTAL( "hh", true, 0, false, 0, true ),
	
	/**  total multiple adults with no kids == 'hh_b_0' + 'hh_m_0' + 'hh_o' */
	POLY_NOKIDS( "hh_p_0", true, 2, false, 0, false ),

	/** total multiple adults with 1+ kids == 'hh_b_1'+'hh_b_2'+'hh_b_3+'+'hh_m_1'+'hh_m_2'+'hh_m_3+'+'hh_s_1'+'hh_s_2'+'hh_s_3+' */
	POLY_1PLUSKIDS( "hh_p_1p", true, 2, false, 1, true ),

	/** single adult, no kids */
	SOLO_NOKIDS( "hh_l", false, 1, false, 0, false ),

	/** total single parent with 1+ [step/adopted] kids */
	SOLO_1PLUSKIDS( "hh_s", true, 1, false, 1, true ),

	/** single parent with 1 [step/adopted] kid */
	SOLO_1KID( "hh_s_1", false, 1, false, 1, false ),

	/** single parent with 2 [step/adopted] kids */
	SOLO_2KIDS( "hh_s_2", false, 1, false, 2, false ),

	/** single parent with >=3 [step/adopted] kids */
	SOLO_3PLUSKIDS( "hh_s_3p", false, 1, false, 3, true ),

	/** total unregistered couples == 'hh_b_0'+'hh_b_1'+'hh_b_2'+'hh_b_3+' */
	DUO_0PLUSKIDS( "hh_b", true, 2, false, 0, true ),

	/** unregistered couple with no kids */
	DUO_NOKIDS( "hh_b_0", false, 2, false, 0, false ),

	/** unregistered couple with one [step/adopted] kid */
	DUO_1KID( "hh_b_1", false, 2, false, 1, false ),

	/** unregistered couple with 2 [step/adopted] kids */
	DUO_2KIDS( "hh_b_2", false, 2, false, 2, false ),

	/** unregistered couple with >=3 [step/adopted] kids */
	DUO_3PLUSKIDS( "hh_b_3p", false, 2, false, 3, true ),

	/** total registered (married/cohabiting) couples == 'hh_m_0'+'hh_m_1'+'hh_m_2'+'hh_m_3+' */
	REGDUO_0PLUS( "hh_m", true, 2, true, 0, true ),

	/** registered (married/cohabiting) couple with no kids */
	REGDUO_NOKIDS( "hh_m_0", false, 2, true, 0, false ),

	/** registered (married/cohabiting) couple with 1 [step/adopted] kid */
	REGDUO_1KID( "hh_m_1", false, 2, true, 1, false ),

	/** registered (married/cohabiting) couple with 2 [step/adopted] kids */
	REGDUO_2KIDS( "hh_m_2", false, 2, true, 2, false ),

	/** registered (married/cohabiting) couple and 3+ [step/adopted] kids */
	REGDUO_3PLUSKIDS( "hh_m_3p", false, 2, false, 3, true ),

	/** total other private compositions (siblings, boarder, fosters, ...) */
	OTHER( "hh_o", true, 0, false, 0, true ),

	;

	final String jsonKey;
	private final boolean aggregate;
	private final int adultCount;
	private final boolean registered;
	private final int childCount; // incl. adopted/step, excl. foster
	private final boolean orMoreKids;

	private CBSHousehold( final String jsonKey,
		final boolean aggregate, final int adultCount, final boolean registered,
		final int kidCount, final boolean orMoreKids )
	{
		this.jsonKey = jsonKey;
		this.aggregate = aggregate;
		this.adultCount = adultCount;
		this.registered = registered;
		this.childCount = kidCount;
		this.orMoreKids = orMoreKids;
	}

	@Override
	public String jsonKey()
	{
		return this.jsonKey;
	}

	@Override
	public boolean aggregate()
	{
		return this.aggregate;
	}

	@Override
	public int adultCount()
	{
		return this.adultCount;
	}

	@Override
	public boolean registered()
	{
		return this.registered;
	}

	@Override
	public int childCount()
	{
		return this.childCount;
	}

	@Override
	public boolean more()
	{
		return this.orMoreKids;
	}
}