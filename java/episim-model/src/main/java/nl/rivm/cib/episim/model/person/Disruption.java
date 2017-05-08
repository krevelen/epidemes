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
package nl.rivm.cib.episim.model.person;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import nl.rivm.cib.episim.model.person.Residence.Deme;

/**
 * {@link Disruption} regards e.g. change or switch in Vitae (development,
 * amusement, employment, retirement), Partner (romance, marriage, divorce),
 * Guardianship (child birth, adoption, nest flight), Residence (migration)
 */
public interface Disruption extends Fact
{

	/**
	 * {@link Disruptor} responds to various {@link Disruption}s, initiated by a
	 * {@link Deme} based on {@link Residence} status
	 */
	interface Disruptor extends Actor<Disruption>
	{
	}

	interface Guardianship extends Disruption
	{

		interface Guardian extends Actor<Disruption.Guardianship>
		{

		}

	}

	interface Birth extends Guardianship
	{

		interface Mother extends Actor<Birth>
		{

		}
	}

	interface Death extends Disruption
	{
	}

	interface Migration extends Disruption
	{
	}

	interface Partnership extends Disruption
	{
		interface Partner extends Actor<Disruption.Partnership>
		{

		}

		interface Marriage extends Disruption.Partnership
		{

		}
	}

	interface Commitment extends Disruption
	{
		interface Education extends Disruption.Commitment
		{
		}

		interface Profession extends Disruption.Commitment
		{
		}

		interface Avocation extends Disruption.Commitment
		{
		}
	}

}