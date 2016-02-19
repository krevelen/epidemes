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
package nl.rivm.cib.episim.model;

import io.coala.json.x.Wrapper;

/**
 * {@link TreatmentStage} is an extensible identifier for stages of treatment
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface TreatmentStage extends Wrapper<String>
{
	/** currently not receiving any treatment */
	TreatmentStage UNTREATED = Util.valueOf( "untreated",
			TreatmentStage.class );

	/** currently immunizing using vaccine/antiserum */
	TreatmentStage VACCINATION = Util.valueOf( "vaccination",
			TreatmentStage.class );

	/** currently suppressing disease using PrEP treatment */
	TreatmentStage PRE_EXPOSURE_PROPHYLACTIC = Util.valueOf( "prep",
			TreatmentStage.class );

	/** currently suppressing disease using PEP treatment */
	TreatmentStage POST_EXPOSURE_PROPHYLACTIC = Util.valueOf( "pep",
			TreatmentStage.class );
}