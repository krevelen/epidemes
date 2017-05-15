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

import java.util.Map;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import nl.rivm.cib.episim.model.locate.Geographic;
import nl.rivm.cib.episim.model.locate.Geometric;
import nl.rivm.cib.episim.model.locate.Locatable;
import nl.rivm.cib.episim.model.person.Redirection.Relation;

/**
 * {@link DomesticChange}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface DomesticChange extends Fact, Domestic<DomesticChange>
{
	// friendly name
	default Map<Actor.ID, HouseholdMember> resultRefs()
	{
		return getMembers();
	}

	interface Household extends Actor<DomesticChange>, Geographic<Household>,
		Geometric<Household>, Locatable<Household>, Personal<Household>,
		Domestic<Household>
	{

		// friendly name
		default Actor.ID referentRef()
		{
			return getActorRef();
		}

	}

	interface Immigrate extends DomesticChange
	{

	}

	interface Emigrate extends DomesticChange
	{

	}

	interface Birth extends DomesticChange, Gender.Attributable<Birth>//, Geographic<Birth>
	{
//		interface Mother extends Actor<Birth>
//		{
//
//		}
	}

	interface Death extends DomesticChange, Personal<Death>
	{
		// friendly name
		default Actor.ID diseasedRef()
		{
			return getActorRef();
		}
	}

	interface MergeHome extends DomesticChange
	{
		// friendly name
		default Map<Actor.ID, HouseholdMember> arrivingRefs()
		{
			return getMembers();
		}
	}

	interface SplitHome extends DomesticChange
	{
		// friendly name
		default Map<Actor.ID, HouseholdMember> departingRefs()
		{
			return getMembers();
		}
	}

	/**
	 * {@link LeaveHome} special case of {@link SplitHome} where a
	 * {@link ConnectionType.Simple#WARD} becomes a
	 * {@link ConnectionType.Simple#SINGLE} adult without children
	 */
	interface LeaveHome extends DomesticChange, Geographic<Household>,
		Geometric<Household>, Locatable<Household>, Personal<Relation>
	{
		// friendly name
		default Actor.ID departingRef()
		{
			return getActorRef();
		}
	}
}
