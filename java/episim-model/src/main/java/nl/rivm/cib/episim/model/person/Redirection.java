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
 * {@link Redirection} regards e.g. change or switch in Vitae (development,
 * amusement, employment, retirement), Partner (romance, marriage, divorce),
 * Guardianship (child birth, adoption, nest flight), Residence (migration)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Redirection extends Fact
{
	/**
	 * {@link Director} responds to various {@link Redirection}s, initiated by
	 * e.g. a {@link Deme} based on {@link Residence} status, or a
	 * {@link Motivator} based on successful {@link Motivation} execution
	 */
	interface Director
		extends Actor<Redirection>, Gender.Attributable<Director>,
		Ageing<Director>, Routine.Attributable<Director>
	{

	}

	interface Relation extends Redirection, Personal<Relation>,
		RelationType.Attributable<Relation>
	{
		// friendly name
		default Actor.ID relationRef()
		{
			return getActorRef();
		}
	}

	interface Termination extends Redirection
	{
		// notify social network of own death
	}

	interface Commitment extends Redirection, Routine.Attributable<Redirection>
	{
//		interface Education extends Redirection.Commitment
//		{
//		}
//
//		interface Profession extends Redirection.Commitment
//		{
//		}
//
//		interface Avocation extends Redirection.Commitment
//		{
//		}
	}
}
