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

import io.coala.time.Instant;
import io.coala.time.Proactive;

/**
 * {@link Individual} represents an infective/infectious subject
 * <p>
 * lifecycle:
 * <ol>
 * <li>birth into single/double parent family</li>
 * <li>day care / school / work, *</li>
 * <li>activities / communities, *</li>
 * <li>vaccination, *</li>
 * <li>move home, *</li>
 * <li>partners, *</li>
 * <li>offspring, *</li>
 * <li>travels, *</li>
 * <li>retirement</li>
 * <li>death</li>
 * </ol>
 * <p>
 * household properties:
 * <ol>
 * <li>n x adults</li>
 * <li>m x children</li>
 * </ol>
 *
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Individual extends Proactive
{

	/** @return the birth {@link Instant} */
	Instant born();

	/** @return the {@link Gender} */
	Gender gender();

//	// social network dynamics (incl self): links/rates change due to media
//	// attention/campaigns, active search, medical consultations
//	public Map<Subject, Rate> complianceAuthorities;

}
