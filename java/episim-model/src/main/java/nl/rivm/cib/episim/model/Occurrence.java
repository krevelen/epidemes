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

/**
 * {@link Occurrence} characterizes incidence of an {@link Infection} among a
 * {@link Population} (= <em>demos</em>) over time and space, similar to
 * <a href="http://www.ncbi.nlm.nih.gov/books/NBK143061/">WHO pandemic
 * phases</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum Occurrence
{
	/** no more occurrence */
	ERADICATED,

	/**
	 * occasional occurrence, e.g.
	 * <a href="https://en.wikipedia.org/wiki/Hospital-acquired_infection">
	 * hospital-acquired or nosocomial infections</a>
	 */
	SPORADIC,

	/**
	 * regular cases often occurring in a region, e.g. community-acquired
	 * infections and livestock-acquired (zoonotic) infections (eg MRSA)
	 */
	ENDEMIC,

	/**
	 * an unusually high number of cases in a region (regional outbreak), see
	 * <a href="https://en.wikipedia.org/wiki/Epidemic">wikipedia</a>
	 */
	EPIDEMIC,

	/**
	 * a global epidemic: an unusually high number of cases globally (global
	 * outbreak), see
	 * <a href="https://en.wikipedia.org/wiki/Pandemic">wikipedia</a>
	 */
	PANDEMIC,;
}