/* $Id: 73665230298d3d60135602e8402f9f4afc981805 $
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
package nl.rivm.cib.episim.model.scenario;

import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link Scenario}
 * 
 * @version $Id: 73665230298d3d60135602e8402f9f4afc981805 $
 * @author Rick van Krevelen
 */
public interface Scenario extends Timed
{

	class Simple implements Scenario
	{

		public static Simple of( final String id, final Scheduler s )
		{
			return new Simple( s );
		}

		private final Scheduler scheduler;

		public Simple( final Scheduler scheduler )
		{
			this.scheduler = scheduler;
			
			// TODO create/load model components
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}
	}
}
