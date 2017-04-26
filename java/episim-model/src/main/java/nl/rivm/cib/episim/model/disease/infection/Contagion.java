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
package nl.rivm.cib.episim.model.disease.infection;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import nl.rivm.cib.episim.model.disease.Afflicted;
import nl.rivm.cib.episim.model.disease.infection.Pathogen.Transmission;

/**
 * {@link Contagion} is self-initiated by a {@link Contagium} (space, route,
 * locus, vector, ...) triggered by environment changes (occupancy, weather,
 * ...) to attempt {@link Transmission} of its {@link Pathogen}(s) on to some
 * potential {@link Afflicted} host
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Contagion extends Fact
{
	Actor.ID getSusceptibleRef();

	void setSusceptibleRef( Actor.ID ref );

	default Contagion withSusceptibleRef( final Actor.ID ref )
	{
		setSusceptibleRef( ref );
		return this;
	}

	interface Contagium extends Actor<Contagion>
	{
		@Override
		default void onInit()
		{
			emit( Contagion.class, FactKind.REQUESTED )
					.subscribe( this::onContact, this::onError );
		}

		default void onContact( final Contagion rq )
		{
			initiate( Transmission.class, rq.getSusceptibleRef() );
		}
	}
}