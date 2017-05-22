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
import io.coala.time.Timing;

/**
 * O05 {@link HealthConfig}, eg
 * <ul>
 * <li>RIVM/RVP (National Vaccination Program)</li>
 * <li>DVP (Vaccine and Prevention Service)</li>
 * <li>JGZ (Youth Health Service)</li>
 * <ul>
 * <li>CB-MZ (Maternal and Child Health [MCH] centers)</li>
 * <li>CJG (Youth and Family Center [YFC])</li>
 * <li>GGD (Municipal Health Service [MHS])</li>
 * </ul>
 * </ul>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface HealthConfig extends GlobalConfig
{
	String BASE_KEY = "health";

	String RIVM_NAME = "rivm";

	// sec min hour date month weekday[0=Sun] year 
	@DefaultValue( "0 0 0 L-3 * ? *" )
	Timing campaignTiming();

	@DefaultValue( " Normal ( -0.5 day;1 h )" )
	String adviceDelay();

	/** @return advisor confidence in vaccine efficacy and safety */
	@DefaultValue( "const(1)" )
	String advisorConfidence();

	/** healthOrg inherently urgent (non-complacent) about herd immunity */
	@DefaultValue( "const(0)" )
	String advisorComplacency();

	/** @return (regional) GGD-to-person proximity, eg thuisvaccinatie.nl */
	@DefaultValue( "const(1)" )
	String occasionProximity();

	/** @return personal understandability of vaccination activity */
	@DefaultValue( "const(1)" )
	String occasionClarity();

	/** @return personal (financial/social) gain of vaccination activity */
	@DefaultValue( "const(1)" )
	String occasionUtility();

	/** @return perceived appeal of vaccination event */
	@DefaultValue( "const(1)" )
	String occasionAffinity();
}