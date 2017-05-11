/* $Id: 03a6f266df7741da3277d78567f9417b9d1459b7 $
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
package nl.rivm.cib.episim.model;

import io.coala.time.Instant;
import io.coala.time.Timed;

/**
 * {@link TransitionEvent}
 * 
 * @version $Id: 03a6f266df7741da3277d78567f9417b9d1459b7 $
 * @author Rick van Krevelen
 */
@Deprecated
public class TransitionEvent<T> implements Timed
{
	protected Instant time;

	/** */
	protected T oldValue;

	/** */
	protected T newValue;

	/**
	 * {@link TransitionEvent} zero-arg bean constructor
	 */
	protected TransitionEvent()
	{
		//
	}

	@Override
	public Instant now()
	{
		return this.time;
	}

	/** */
	public T getOldValue()
	{
		return this.oldValue;
	}

	/** */
	public T getNewValue()
	{
		return this.newValue;
	}

	@Override
	public String toString()
	{
		return new StringBuffer( getClass().getSimpleName() ).append( '[' )
				.append( getOldValue() ).append( "->" ).append( getNewValue() )
				.append( ']' ).toString();
	}
}