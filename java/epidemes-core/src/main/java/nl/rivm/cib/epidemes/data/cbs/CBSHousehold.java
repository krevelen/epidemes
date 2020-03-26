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
package nl.rivm.cib.epidemes.data.cbs;

import nl.rivm.cib.epidemes.model.ConnectionType;
import nl.rivm.cib.epidemes.model.HouseholdComposition;

/**
 * {@link CBSHousehold} the JSON 'implementation' of a CBS household composition
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum CBSHousehold implements HouseholdComposition, CBSJsonProperty
{
	/**
	 * total households == 'hh_l' + 'hh_p_0' + 'hh_p_1+' == 'hh_l' + 'hh_b' +
	 * 'hh_m' + 'hh_s' + 'hh_o'
	 */
	TOTAL( "hh", true, 0, false, 0, true, null ),

	/** total multiple adults with no kids == 'hh_b_0' + 'hh_m_0' + 'hh_o' */
	POLY_NOKIDS( "hh_p_0", true, 2, false, 0, false, ConnectionType.Simple.PARTNER ),

	/**
	 * total multiple adults with 1+ kids ==
	 * 'hh_b_1'+'hh_b_2'+'hh_b_3+'+'hh_m_1'+'hh_m_2'+'hh_m_3+'+'hh_s_1'+'hh_s_2'+'hh_s_3+'
	 */
	POLY_1PLUSKIDS( "hh_p_1p", true, 2, false, 1, true, ConnectionType.Simple.PARTNER ),

	/** single adult, no kids */
	SOLO_NOKIDS( "hh_l", false, 1, false, 0, false, ConnectionType.Simple.SINGLE ),

	/** total single parent with 1+ [step/adopted] kids */
	SOLO_1PLUSKIDS( "hh_s", true, 1, false, 1, true, ConnectionType.Simple.SINGLE ),

	/** single parent with 1 [step/adopted] kid */
	SOLO_1KID( "hh_s_1", false, 1, false, 1, false, ConnectionType.Simple.SINGLE ),

	/** single parent with 2 [step/adopted] kids */
	SOLO_2KIDS( "hh_s_2", false, 1, false, 2, false, ConnectionType.Simple.SINGLE ),

	/** single parent with >=3 [step/adopted] kids */
	SOLO_3PLUSKIDS( "hh_s_3p", false, 1, false, 3, true, ConnectionType.Simple.SINGLE ),

	/** total unregistered couples == 'hh_b_0'+'hh_b_1'+'hh_b_2'+'hh_b_3+' */
	DUO_0PLUSKIDS( "hh_b", true, 2, false, 0, true, ConnectionType.Simple.PARTNER ),

	/** unregistered couple with no kids */
	DUO_NOKIDS( "hh_b_0", false, 2, false, 0, false, ConnectionType.Simple.PARTNER ),

	/** unregistered couple with one [step/adopted] kid */
	DUO_1KID( "hh_b_1", false, 2, false, 1, false, ConnectionType.Simple.PARTNER ),

	/** unregistered couple with 2 [step/adopted] kids */
	DUO_2KIDS( "hh_b_2", false, 2, false, 2, false, ConnectionType.Simple.PARTNER ),

	/** unregistered couple with >=3 [step/adopted] kids */
	DUO_3PLUSKIDS( "hh_b_3p", false, 2, false, 3, true, ConnectionType.Simple.PARTNER ),

	/**
	 * total registered (married/cohabiting) couples ==
	 * 'hh_m_0'+'hh_m_1'+'hh_m_2'+'hh_m_3+'
	 */
	REGDUO_0PLUS( "hh_m", true, 2, true, 0, true, ConnectionType.Simple.PARTNER ),

	/** registered (married/cohabiting) couple with no kids */
	REGDUO_NOKIDS( "hh_m_0", false, 2, true, 0, false, ConnectionType.Simple.PARTNER ),

	/** registered (married/cohabiting) couple with 1 [step/adopted] kid */
	REGDUO_1KID( "hh_m_1", false, 2, true, 1, false, ConnectionType.Simple.PARTNER ),

	/** registered (married/cohabiting) couple with 2 [step/adopted] kids */
	REGDUO_2KIDS( "hh_m_2", false, 2, true, 2, false, ConnectionType.Simple.PARTNER ),

	/** registered (married/cohabiting) couple and 3+ [step/adopted] kids */
	REGDUO_3PLUSKIDS( "hh_m_3p", false, 2, true, 3, true, ConnectionType.Simple.PARTNER ),

	/** total other private compositions (siblings, boarder, fosters, ...) */
	OTHER( "hh_o", true, 0, false, 0, true, ConnectionType.Simple.WARD ),

	;

	private final String jsonKey;
	private final boolean aggregate;
	private final int adultCount;
	private final boolean registered;
	private final int childCount; // incl. adopted/step, excl. foster
	private final boolean orMoreKids;
	private final ConnectionType.Simple relationType;

	private CBSHousehold( final String jsonKey, final boolean aggregate,
		final int adultCount, final boolean registered, final int kidCount,
		final boolean orMoreKids, final ConnectionType.Simple relationType )
	{
		this.jsonKey = jsonKey;
		this.aggregate = aggregate;
		this.adultCount = adultCount;
		this.registered = registered;
		this.childCount = kidCount;
		this.orMoreKids = orMoreKids;
		this.relationType = relationType;
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

	/** @return the (minimum) household size */
	public int size()
	{
		return adultCount() + childCount();
	}

	/** @return the referent relation type */
	public ConnectionType.Simple partnerRelationType()
	{
		return this.relationType;
	}

//	public static CBSHousehold of( final Domestic<?> household )
//	{
//		final long adultCount = household.getMembers().values().stream()
//				.filter( m -> m.relationType().isInformative() ).count(),
//				childCount = household.getMembers().size() - adultCount;
//		// check if registered couple is still together
//		final boolean registered = household.getComposition().registered()
//				&& adultCount == 2;
//		for( CBSHousehold hhType : values() )
//			if(
//			// skip aggregates
//			!hhType.aggregate()
//					// match registered
//					&& hhType.registered() == registered
//					// match adult count
//					&& hhType.adultCount() == adultCount
//					// match child count exactly or range
//					&& (hhType.childCount() == childCount || (hhType.more()
//							&& hhType.childCount() < childCount)) )
//				return hhType;
//		return Thrower.throwNew( IllegalArgumentException::new,
//				() -> "Unknown composition for " + household.getMembers()
//						+ ", adults: " + adultCount + ", children: "
//						+ childCount + ", registered: " + registered );
//	}

	@Override
	public CBSHousehold plusAdult()
	{
		switch( this )
		{
		case SOLO_NOKIDS:
			return DUO_NOKIDS;
		case SOLO_1KID:
			return DUO_1KID;
		case SOLO_2KIDS:
			return DUO_2KIDS;
		case SOLO_3PLUSKIDS:
			return DUO_3PLUSKIDS;

//		case SOLO_1PLUSKIDS:
//		case POLY_1PLUSKIDS:
//		case POLY_NOKIDS:
//		case OTHER:
		default:
		}
		throw new IllegalStateException( "Can't add adult to " + name() );
	}

	@Override
	public CBSHousehold minusAdult()
	{
		switch( this )
		{
		case REGDUO_NOKIDS:
		case DUO_NOKIDS:
			return SOLO_NOKIDS;
		case REGDUO_1KID:
		case DUO_1KID:
			return SOLO_1KID;
		case REGDUO_2KIDS:
		case DUO_2KIDS:
			return SOLO_2KIDS;
		case REGDUO_3PLUSKIDS:
		case DUO_3PLUSKIDS:
			return SOLO_3PLUSKIDS;

//		case REGDUO_0PLUS: how many kids remain?
//		case DUO_0PLUSKIDS: how many kids remain?
//		case POLY_1PLUSKIDS: how many adults/kids remain?
//		case POLY_NOKIDS: how many adults remain?
//		case OTHER:
		default:
		}
		throw new IllegalStateException( "Can't remove adult from " + name() );
	}

	@Override
	public CBSHousehold plusChild()
	{
		switch( this )
		{
		case SOLO_NOKIDS:
			return SOLO_1KID;
		case SOLO_1KID:
			return SOLO_2KIDS;
		case SOLO_2KIDS:
		case SOLO_3PLUSKIDS:
			return SOLO_3PLUSKIDS;
//		case SOLO_1PLUSKIDS:

		case DUO_NOKIDS:
			return DUO_1KID;
		case DUO_1KID:
			return DUO_2KIDS;
		case DUO_2KIDS:
		case DUO_3PLUSKIDS:
			return DUO_3PLUSKIDS;
//		case DUO_0PLUSKIDS:

		case REGDUO_NOKIDS:
			return REGDUO_1KID;
		case REGDUO_1KID:
			return REGDUO_2KIDS;
		case REGDUO_2KIDS:
		case REGDUO_3PLUSKIDS:
			return REGDUO_3PLUSKIDS;
//		case REGDUO_0PLUS:

		case POLY_NOKIDS:
			return POLY_1PLUSKIDS;

//		case POLY_1PLUSKIDS:
//		case OTHER:
		default:
		}
		throw new IllegalStateException( "Can't add child to " + name() );
	}

	@Override
	public CBSHousehold minusChild()
	{
		switch( this )
		{
		case SOLO_1KID:
			return SOLO_NOKIDS;
		case SOLO_2KIDS:
			return SOLO_1KID;
		case SOLO_3PLUSKIDS:
			return SOLO_2KIDS;

		case DUO_1KID:
			return DUO_NOKIDS;
		case DUO_2KIDS:
			return DUO_1KID;
		case DUO_3PLUSKIDS:
			return DUO_2KIDS;

		case REGDUO_1KID:
			return REGDUO_NOKIDS;
		case REGDUO_2KIDS:
			return REGDUO_1KID;
		case REGDUO_3PLUSKIDS:
			return REGDUO_2KIDS;

		case SOLO_NOKIDS:
		case DUO_NOKIDS:
		case REGDUO_NOKIDS:
		case POLY_NOKIDS:
			throw new IllegalStateException(
					"No child to remove from " + name() );

//		case SOLO_1PLUSKIDS: not symmetric
//		case POLY_1PLUSKIDS: SOLO or POLY?
//		case DUO_0PLUSKIDS:
//		case REGDUO_0PLUS:
//		case TOTAL:
//		case OTHER:
		default:
			throw new IllegalStateException(
					"Undefined composition in " + name() );
		}
	}
}