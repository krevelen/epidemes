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
import javax.persistence.Entity;
import javax.persistence.Table;

import io.coala.bind.persist.LocalIdDao;
import io.coala.enterprise.Actor;

/**
 * {@link ActorDimensionDao}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity
@Table( name = "DIM_ACTOR" )
public class ActorDimensionDao extends LocalIdDao
{
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
	 * @param individual
	 * @return
	 */
	public static ActorDimensionDao of( final Actor.ID personRef )
	{
		// TODO resolve attributes recursively from LocalId
		final ActorDimensionDao result = new ActorDimensionDao();
		return result;
	}

}
