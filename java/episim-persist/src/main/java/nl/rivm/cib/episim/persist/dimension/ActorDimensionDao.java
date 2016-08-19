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
package nl.rivm.cib.episim.persist.dimension;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.person.HouseholdParticipant;
import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link ActorDimensionDao}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Embeddable
public class ActorDimensionDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected int id;

	@Column( name = "NAME", nullable = false, updatable = false )
	protected String name;

	@Column( name = "ROLE", nullable = true, updatable = false )
	protected String role;

	@Column( name = "ORGANIZATION", nullable = true, updatable = false )
	protected String organization;

	@Column( name = "ORGANIZATION_TYPE", nullable = true, updatable = false )
	protected String organizationType;

	@Column( name = "WORLD", nullable = true, updatable = false )
	protected String world;

	/**
	 * @param secondaryCondition
	 * @return
	 */
	public static ActorDimensionDao of( final Individual individual )
	{
		final ActorDimensionDao result = new ActorDimensionDao();
		if( individual instanceof HouseholdParticipant )
		{
			((HouseholdParticipant) individual).household().id().unwrap();
		}
		return result;
	}

}
