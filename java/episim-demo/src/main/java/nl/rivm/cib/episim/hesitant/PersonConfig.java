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

import io.coala.config.GlobalConfig;

/**
 * O01 {@link PersonConfig}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface PersonConfig extends GlobalConfig
{
	String BASE_KEY = "person";

	@DefaultValue( "const(1)" )
	String myConfidence();

	@DefaultValue( "const(0)" )
	String myComplacencyDist();

	@DefaultValue( "const(0.5)" )
	String myCalculationDist();

	// DKTP-1 ~ cauchy(x_0=59;gamma=3.5), subtract 14 days invitation delay
	@DefaultValue( "cauchy(45 day;3.5 day)" )
	String myVaccinationDelayDist();

	@DefaultValue( "[25 day;125 day>" )
	String myVaccinationDelayRange();
}