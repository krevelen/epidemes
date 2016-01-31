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
package nl.rivm.cib.episim.model.location;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.coala.model.ModelComponentID;
import io.coala.name.AbstractIdentifiable;
import nl.rivm.cib.episim.model.disease.Individual;
import nl.rivm.cib.episim.model.disease.InfectiveState;

/**
 * {@link Location}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@SuppressWarnings( "serial" )
public class Location extends AbstractIdentifiable<Location.ID>
{
	public static class ID extends ModelComponentID<String>
	{

	}

	public static class TypeID extends ModelComponentID<String>
	{

	}

	public ID id;

	public TypeID type;

	public List<Individual> vectors = new ArrayList<>();

	public Map<InfectiveState, Integer> infected = new EnumMap<>( InfectiveState.class );

}
