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
import javax.persistence.Id;

import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.CBSUtil;
import nl.rivm.cib.episim.util.ZipCode;

/**
 * {@link ProvincieDao}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity( name = "PROVINCIE" )
public class ProvincieDao extends AbstractDao
{
	@Id
	@Column( name = "CODE", unique = true )
	protected int code;

	@Column( name = "NAME", length = 100 )
	protected String name;

	/**
	 * @param zipCode
	 * @return
	 */
	public static ProvincieDao of( final ZipCode zipCode )
	{
		final ProvincieDao result = new ProvincieDao();
		result.code = CBSUtil.toProvincieCode( zipCode );
		result.name = CBSUtil.toProvincieNaam( zipCode );
		return result;
	}

}
