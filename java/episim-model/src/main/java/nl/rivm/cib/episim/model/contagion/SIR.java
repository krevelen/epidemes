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
package nl.rivm.cib.episim.model.contagion;

/**
 * {@link SimpleSIR} follows common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic
 * models</a> and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modelling of infectious disease</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum SIR implements Carrier
{

	/** S */
	SUSCEPTIBLE,

	/** I */
	INFECTIVE,

	/** R (removed, recovered) */
	RECOVERED,

	;

	@Override
	public boolean isSusceptible()
	{
		return this == SUSCEPTIBLE;
	}

	@Override
	public boolean isExposed()
	{
		return this == INFECTIVE;
	}

	@Override
	public boolean isInfective()
	{
		return this == INFECTIVE;
	}

	@Override
	public boolean isAsymptomaticInfective()
	{
		return this == INFECTIVE;
	}

	@Override
	public boolean isSymptomaticInfective()
	{
		return this == INFECTIVE;
	}

	@Override
	public boolean isRecoveredImmune()
	{
		return this == RECOVERED;
	}

	@Override
	public boolean isImmune()
	{
		return this == RECOVERED;
	}

	@Override
	public boolean isPassivelyMaternalImmune()
	{
		return this == RECOVERED;
	}
}