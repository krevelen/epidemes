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
package nl.rivm.cib.episim.model.person;

import java.util.HashMap;
import java.util.Map;

import io.coala.enterprise.Actor;
import io.coala.json.Attributed;

/**
 * {@link Domestic}
 * 
 * @version $Id$
 * @author Rick van KrevelenHouseholdMember
 */
public interface Domestic<THIS> extends Attributed
{

	HouseholdComposition getComposition();

	void setComposition( HouseholdComposition composition );

	@SuppressWarnings( "unchecked" )
	default THIS with( HouseholdComposition composition )
	{
		setComposition( composition );
		return (THIS) this;
	}

	Map<Actor.ID, HouseholdMember> getMembers();

	void setMembers( Map<Actor.ID, HouseholdMember> composition );

	@SuppressWarnings( "unchecked" )
	default THIS withMembers( final Map<Actor.ID, HouseholdMember> composition )
	{
		setMembers( composition );
		return (THIS) this;
	}

	@SuppressWarnings( "unchecked" )
	default <T> THIS with( final HouseholdMember member )
	{
		Map<Actor.ID, HouseholdMember> composition = getMembers();
		if( composition == null )
		{
			composition = new HashMap<>();
			setMembers( composition );
		}
		composition.put( member.ref(), member );
		return (THIS) this;
	}

	default HouseholdMember member( final Actor.ID memberRef )
	{
		return getMembers().get( memberRef );
	}

	@SuppressWarnings( "unchecked" )
	default THIS with( final Domestic<?> domestic )
	{
		setComposition( domestic.getComposition() );
		setMembers( domestic.getMembers() );
		return (THIS) this;
	}
}
