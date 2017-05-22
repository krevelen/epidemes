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
package nl.rivm.cib.episim.hesitant;

import java.util.Arrays;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;

/**
 * T15 {@link Redirection} transactions are initiated by:
 * <ul>
 * <li>the {@link PersonConfig}'s own A12 {@link Disruptor} behavior;</li>
 * <li>the {@link PersonConfig}'s own A14 {@link Motivator}; or</li>
 * <li>the {@link PersonConfig}'s own A15 {@link Redirector}.</li>
 * </ul>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Redirection extends Fact
{
	/** A15 {@link Redirector} handles T15 {@link Redirection} execution */
	public interface Redirector extends Actor<Redirection>
	{
		/** @param ids */
		default Redirector consultPersons( final Actor.ID... ids )
		{
			return ids == null || ids.length == 0 ? this
					: consultPersons( Arrays.asList( ids ) );
		}

		/** @param ids */
		default Redirector consultPersons( final Iterable<Actor.ID> ids )
		{
			ids.forEach( id -> initiate( Opinion.class, id ).commit() );
			return this;
		}

		/** @param ids */
		default Redirector consultAdvisors( final Actor.ID... ids )
		{
			return ids == null || ids.length == 0 ? this
					: consultAdvisors( Arrays.asList( ids ) );
		}

		/** @param ids */
		default Redirector consultAdvisors( final Iterable<Actor.ID> ids )
		{
			ids.forEach( id -> initiate( Advice.class, id ).commit() );
			return this;
		}

		/** @param ids */
		default Redirector consultMedia( final Actor.ID... ids )
		{
			return ids == null || ids.length == 0 ? this
					: consultMedia( Arrays.asList( ids ) );
		}

		/** @param ids */
		default Redirector consultMedia( final Iterable<Actor.ID> ids )
		{
			ids.forEach( id -> initiate( Information.class, id ).commit() );
			return this;
		}
	}
	
	enum GoalType
	{
		VACCINATE;
	}

	GoalType getGoalType();

	void setGoalType( GoalType goalType );

	default Redirection with( final GoalType goalType )
	{
		setGoalType( goalType );
		return this;
	}
	
}