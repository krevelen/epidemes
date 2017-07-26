/* $Id: 3050d23e9fb7b87735fe68325328cd09edfdbfad $
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
package nl.rivm.cib.episim.geard;

import java.util.Collections;

import nl.rivm.cib.episim.geard.GeardHousehold.MoveHouse;

/**
 * {@link HouseholdParticipant} TODO make recursive (Social) Participant
 * 
 * @version $Id: 3050d23e9fb7b87735fe68325328cd09edfdbfad $
 * @author Rick van Krevelen
 */
@Deprecated
public interface HouseholdParticipant extends Participant
{
	GeardHousehold<? extends HouseholdParticipant> household();

	@Override
	default public Population<?> population()
	{
		return household().population();
	}

	@SuppressWarnings( "unchecked" )
	default <T extends HouseholdParticipant> void
		moveHouse( GeardHousehold<T> newHome )
	{
		newHome.members().add( (T) this );
		newHome.emit( DemographicEvent.Builder.of( MoveHouse.class, now() )
				.withDepartures( Collections.singleton( this ) ).build() );
		household().members().remove( this );
		if( household().members().isEmpty() )
		{
//			household().population().households().remove( this );
			household().onAbandoned();
		}
	}
}
