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
package nl.rivm.cib.episim.model.disease;

import io.coala.json.x.Wrapper;
import io.coala.name.x.Id;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.model.location.Location;

/**
 * {@link Individual} represents an infective/infectious subject
 * 
 * @version $Date$
 * @author Rick van Krevelen
 */
public class Individual extends Wrapper.SimpleComparable<Individual.ID>
{
	public static class ID extends Id<Long>
	{
		private static long COUNTER = 0L;

		public static ID create()
		{
			return Id.valueOf( "" + COUNTER++, ID.class );
		}
	}

	public ID id;

//	public Instant birth;
//
//	public Location currentLocation;
//
//	public TravelBehavior travelBehavior;
//
//	public Map<Disease, InfectiveState> diseaseStates;
//
//	// social network dynamics (incl self): links/rates change due to media
//	// attention/campaigns, active search, medical consultations
//	public Map<Subject, Rate> complianceAuthorities;
//
//	// social network dynamics: links/rates change due to movements
//	public Map<Subject, Rate> directContacts;
//
//	// social network dynamics: links/rates change due to movements
//	public Map<Subject, Rate> indirectContacts;
//
//	// social network dynamics: links change due to behavior types (e.g. risky)
//	public Map<Subject, Rate> sexualContacts;

	public Individual( final Instant birth, final InfectiveState state, final Location location )
	{
		this.id = ID.create();
//		this.birth = birth;
//		this.currentLocation = location;
//		this.diseaseStates = new HashMap<>();
//		this.complianceAuthorities = new HashMap<>();
//		this.directContacts = new HashMap<>();
//		this.indirectContacts = new HashMap<>();
//		this.sexualContacts = new HashMap<>();
	}
}
