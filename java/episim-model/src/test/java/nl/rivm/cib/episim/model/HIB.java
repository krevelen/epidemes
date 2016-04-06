/* $Id: c497843c72fe02d3fb4c20067d0832b27514147b $
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

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import io.coala.random.ProbabilityDistribution;
import io.coala.time.x.Duration;
import nl.rivm.cib.episim.model.Infection;

/**
 * {@link HIB} is an invasive disease caused by the Hib bacteria residing in
 * nose-throat cavity. It occasionally causes blood poisoning (sepsis), septic
 * arthritis, pneumonia, epiglottis and meningitis. It has a
 * <a href="http://rijksvaccinatieprogramma.nl/De_ziekten/HIb">RVP page</a>, a
 * <a href="http://www.rivm.nl/Onderwerpen/H/Haemophilus_influenzae_type_b">RIVM
 * page</a>, a <a href="http://www.cdc.gov/hi-disease/">US CDC page</a>, and a
 * <a href="http://www.who.int/immunization/topics/hib/en/">WHO immunization
 * page</a>
 * 
 * @version $Id: c497843c72fe02d3fb4c20067d0832b27514147b $
 * @author Rick van Krevelen
 */
public class HIB extends Infection.Simple
{

	private static final ProbabilityDistribution<Amount<Frequency>> forceDist = null;

	private static final ProbabilityDistribution<Duration> latentPeriodDist = null;

	private static final ProbabilityDistribution<Duration> recoverPeriodDist = null;

	private static final ProbabilityDistribution<Duration> wanePeriodDist = null;

	private static final ProbabilityDistribution<Duration> onsetPeriodDist = null;

	private static final ProbabilityDistribution<Duration> symptomPeriodDist = null;

	public HIB()
	{
		super( forceDist, latentPeriodDist, recoverPeriodDist, wanePeriodDist,
				onsetPeriodDist, symptomPeriodDist );
	}

}