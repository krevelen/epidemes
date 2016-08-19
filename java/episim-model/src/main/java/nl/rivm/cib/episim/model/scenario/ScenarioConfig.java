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
package nl.rivm.cib.episim.model.scenario;

import org.joda.time.DateTime;

import io.coala.config.GlobalConfig;
import io.coala.exception.Thrower;
import io.coala.function.ThrowingConsumer;
import io.coala.time.Duration;
import io.coala.time.Scheduler;

/**
 * {@link ScenarioConfig}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface ScenarioConfig extends GlobalConfig
{
	String DURATION_KEY = "scenario.duration";

	String DURATION_DEFAULT = "500 day";

	String OFFSET_KEY = "scenario.offset";

	String OFFSET_DEFAULT = "2016-01-01T00:00:00";

	String INITIALIZER_KEY = "scenario.initializer";

//	String INITIALIZER_DEFAULT = "nl.rivm.cib.episim.model.EcosystemScenarioTest$EcosystemScenario";

	@Key( DURATION_KEY )
	@DefaultValue( DURATION_DEFAULT )
	Duration duration();

	@Key( OFFSET_KEY )
	@DefaultValue( OFFSET_DEFAULT )
	DateTime offset();

	@Key( INITIALIZER_KEY )
//	@DefaultValue( INITIALIZER_DEFAULT )
	Class<? extends Scenario> scenarioType();

	default ThrowingConsumer<Scheduler, ?> initializer()
	{
		try
		{
			return scenarioType().newInstance()::init;
		} catch( final Exception e )
		{
			return Thrower.rethrowUnchecked( e );
		}
	}
}