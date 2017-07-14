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
package nl.rivm.cib.epidemes.geodb.jdbc;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.spatial.dialect.postgis.PostgisDialect;
import org.hibernate.type.StandardBasicTypes;

/**
 * {@link PostgisDialectExtended} follows example from
 * https://gist.github.com/geobabbler/bdc405c5d25ba2b45d81
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PostgisDialectExtended extends PostgisDialect
{
	/** the serialVersionUID */
	private static final long serialVersionUID = 1L;

	public PostgisDialectExtended()
	{
		super();
		registerFunction( "Transform", new StandardSQLFunction( "ST_Transform",
				StandardBasicTypes.STRING ) );
		registerFunction( "AsGeoJson", new StandardSQLFunction( "ST_AsGeoJson",
				StandardBasicTypes.STRING ) );
		registerFunction( "AsGml", new StandardSQLFunction( "ST_AsGml",
				StandardBasicTypes.STRING ) );
		registerFunction( "AsKml", new StandardSQLFunction( "ST_AsKml",
				StandardBasicTypes.STRING ) );
		registerFunction( "Srid", new StandardSQLFunction( "ST_Srid",
				StandardBasicTypes.INTEGER ) );
	}
}