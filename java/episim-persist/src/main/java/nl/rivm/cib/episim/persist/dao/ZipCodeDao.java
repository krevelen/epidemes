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
import javax.persistence.Embeddable;

import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.util.ZipCode;

/**
 * {@link ZipCodeDao}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Embeddable
public class ZipCodeDao extends AbstractDao
{

	@Column( name = "PC6", length = 6, updatable = false )
	protected String pc6;

	public ZipCode toZipCode()
	{
		return ZipCode.valueOf( this.pc6 );
	}

	public static ZipCodeDao of( final ZipCode zip )
	{
		final ZipCodeDao result = new ZipCodeDao();
		result.pc6 = zip.toPostCode6();
		return result;
	}
}
