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

import java.util.Collections;
import java.util.List;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.json.Wrapper;
import io.coala.time.Instant;

/**
 * {@link VaxRegistrant}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class VaxRegistrant extends Wrapper.SimpleOrdinal<Instant>
{
	private final Actor.ID personRef;

	private final Fact.ID regRef;

	/** upcoming invite/reminder */
	private transient List<VaxDose> vaxStatus = Collections.emptyList();

	public VaxRegistrant( final Advice reg )
	{
		wrap( reg.getBirth() );
		this.personRef = reg.creatorRef().organizationRef();
		this.regRef = reg.id();
	}

	@Override
	public boolean equals( final Object that )
	{
		return this.personRef.equals( ((VaxRegistrant) that).personRef );
	}

	public void update( final Treatment treatment )
	{
		for( VaxDose dose : treatment.getDoses() )
		{
			// TODO remove previous doses in the vaccine series/process
		}
	}
}