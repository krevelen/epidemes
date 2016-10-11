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
package nl.rivm.cib.episim.persist.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.persist.AbstractDao;

/**
 * {@link RegionDao}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity( name = "REGION" )
public class RegionDao extends AbstractDao
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected long id;

	@Column( name = "NAME" )
	protected String name;

	@ManyToOne
	@JoinColumn( name = "PARENT_ID", nullable = true, updatable = false )
	protected RegionDao parent;

	/**
	 * @return
	 */
	public Region toRegion()
	{
		return Region.of( this.name, null, null, null );
	}

	/**
	 * @param region
	 * @return
	 */
	public static RegionDao of( final EntityManager em, final Region region )
	{
		final RegionDao result = new RegionDao();
		result.name = region.id();
		if( region.parent() != null )
		{
			result.parent = of( em, region.parent() );
			em.persist( result.parent );
		}
		return result;
	}
}
