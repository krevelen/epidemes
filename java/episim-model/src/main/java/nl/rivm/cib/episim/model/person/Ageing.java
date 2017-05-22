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

import io.coala.json.Attributed;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Timed;

/**
 * {@link Ageing}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Ageing<THIS> extends Timed, Attributed
{

	Instant getBirth();

	void setBirth( Instant birth );

	default Duration age()
	{
		return now().subtract( getBirth() );
	}

	@SuppressWarnings( "unchecked" )
	default THIS with( final Instant birth )
	{
		setBirth( birth );
		return (THIS) this;
	}

}
