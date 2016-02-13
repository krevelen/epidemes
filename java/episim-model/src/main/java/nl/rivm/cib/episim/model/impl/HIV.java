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
package nl.rivm.cib.episim.model.impl;

import io.coala.time.x.Duration;
import nl.rivm.cib.episim.model.Infection.SimpleInfection;
import nl.rivm.cib.episim.model.Location;
import nl.rivm.cib.episim.model.Relation;
import nl.rivm.cib.episim.model.Route;

/**
 * {@link HIV} or the Human Immunodeficiency Virus has a
 * <a href="http://www.who.int/mediacentre/factsheets/fs360/en/">WHO fact
 * sheet</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class HIV extends SimpleInfection
{

	@Override
	public boolean transmit( final Location location, final Route route,
		final Duration duration, final Relation relation )
	{
		if(Route.SEXUAL.equals( route ))
			return true;
		
		if(Route.SEXUAL_ORAL.equals( route ))
			return true;
		
		if(Route.IATROGENIC.equals( route ))
			return true;
		
		if(Route.VERTICAL.equals( route ))
			return true;
		
		return false;
	}

}
