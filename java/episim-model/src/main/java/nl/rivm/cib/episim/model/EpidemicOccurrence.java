/* $Id: 4a50c88b1b873be037db78d0b76d4d4ab29ec731 $
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

import io.coala.json.Wrapper;
import nl.rivm.cib.episim.model.metrics.PopulationMetrics;

/**
 * {@link EpidemicOccurrence} is an extensible classifier to characterize incidence of
 * an {@link Infection} among a {@link PopulationMetrics} (= <em>demos</em>) over time
 * and space, related but not similar to
 * <a href="http://www.ncbi.nlm.nih.gov/books/NBK143061/">WHO pandemic
 * phases</a>
 * 
 * @version $Id: 4a50c88b1b873be037db78d0b76d4d4ab29ec731 $
 * @author Rick van Krevelen
 */
public interface EpidemicOccurrence extends Wrapper<String>
{

	/**
	 * occasional occurrence, e.g.
	 * <a href="https://en.wikipedia.org/wiki/Hospital-acquired_infection">
	 * hospital-acquired or nosocomial infections</a>
	 */
	EpidemicOccurrence SPORADIC = Util.valueOf( "sporadic", EpidemicOccurrence.class );

	/**
	 * regular cases often occurring in a region, e.g. community-acquired
	 * infections and livestock-acquired (zoonotic) infections (eg MRSA)
	 */
	EpidemicOccurrence ENDEMIC = Util.valueOf( "endemic", EpidemicOccurrence.class );

	/**
	 * an unusually high number of cases in a (possibly international) region
	 * (regional outbreak), see
	 * <a href="https://en.wikipedia.org/wiki/Epidemic">wikipedia</a>
	 */
	EpidemicOccurrence EPIDEMIC = Util.valueOf( "epidemic", EpidemicOccurrence.class );

	/**
	 * a global epidemic: an unusually high number of cases globally (global
	 * outbreak), see
	 * <a href="https://en.wikipedia.org/wiki/Pandemic">wikipedia</a>
	 */
	EpidemicOccurrence PANDEMIC = Util.valueOf( "pandemic", EpidemicOccurrence.class );

	/** immunization threshold reached, disease practically eliminated */
	EpidemicOccurrence HERD_IMMUNITY = Util.valueOf( "herd-immunity",
			EpidemicOccurrence.class );

	/** no more occurrence, also in the ild worldwide */
	EpidemicOccurrence ERADICATED = Util.valueOf( "eradicated", EpidemicOccurrence.class );
}