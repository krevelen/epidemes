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
import java.util.List;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.time.Instant;

/**
 * {@link Advice}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Advice extends Fact
{
	/**
	 * {@link Advisor}
	 */
	interface Advisor extends Actor<Advice>
	{
	}

	/** @return the requester/registrant's time of birth */
	Instant getBirth();

	/** @param birth the requester/registrant's time of birth */
	void setBirth( Instant birth );

	/** @param birth the requester/registrant's time of birth */
	default Advice withBirth( final Instant birth )
	{
		setBirth( birth );
		return this;
	}

	/** @return the requester/registrant's vaccination status */
	List<VaxDose> getVaxStatus();

	/** @param status the requester/registrant's vaccination status */
	void setVaxStatus( List<VaxDose> status );

	/** @param status the requester/registrant's vaccination status */
	default Advice with( final VaxDose... status )
	{
		setVaxStatus( Arrays.asList( status ) );
		return this;
	}
}