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

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;

import io.coala.random.x.RandomAmountDistribution;

/**
 * {@link Disease} follows common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modelling of infectious disease</a>
 * 
 * <table>
 * <tr>
 * <th>R<sub>0</sub></th>
 * <td>Basic reproduction number</td>
 * </tr>
 * <tr>
 * <th>1 / &epsilon;</th>
 * <td>Average latent period</td>
 * </tr>
 * <tr>
 * <th>1 / &gamma;</th>
 * <td>Average infectious period</td>
 * </tr>
 * <tr>
 * <th>f</th>
 * <td>Average loss of immunity rate of recovered individuals</td>
 * </tr>
 * <tr>
 * <th>&delta;</th>
 * <td>Average temporary immunity period</td>
 * </tr>
 * </table>
 * 
 * @version $Date$
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public interface Disease
{
	/**
	 * The basic reproduction number, <em>R</em><sub>0</sub>, is defined as the
	 * expected number of secondary cases produced by a single (typical)
	 * infection in a completely susceptible population. It is important to note
	 * that <em>R</em><sub>0</sub> is a dimensionless number and not a rate,
	 * which would have units of time<sup>−1</sup> . Some authors incorrectly
	 * call <em>R</em><sub>0</sub> the &ldquo;basic reproductive rate.&rdquo;
	 * <p>
	 * <em>R</em><sub>0</sub> = <em>&tau;</em> &middot; <em>c</em> &middot;
	 * <em>d</em>
	 */
	RandomAmountDistribution<Dimensionless> getBasicReproductionNumber();

	/**
	 * @return <em>&tau;</em> = infection / contact
	 */
	RandomAmountDistribution<Dimensionless> getTransmissibility();

	/**
	 * @return <em>d</em> = time / infection = duration of infectiousness
	 */
	RandomAmountDistribution<Duration> getInfectiousDuration();

	/**
	 * @return &delta; = (average) temporary immunity period
	 */
	RandomAmountDistribution<Duration> getTemporaryImmunityPeriodDist();

//	/** see http://www.who.int/mediacentre/factsheets/fs286/en/ */
//	MEASLES(),
//
//	/** see http://www.who.int/mediacentre/factsheets/fs211/en/ */
//	SEASONAL_INFLUENZA(),
//
//	/** see http://www.who.int/mediacentre/factsheets/fs380/en/ */
//	HUMAN_PAPILLOMA_VIRUS(),
//	
//	/** see http://www.who.int/mediacentre/factsheets/fs360/en/ */
//	HUMAN_IMMUNODEFICIENCY_VIRUS
//
//	;
//
//	private final SimRate infectionRate;
//
//	private Disease()
//	{
//		this(null);
//	}
//
//	private Disease(final SimRate infectionRate)
//	{
//		this.infectionRate = infectionRate;
//	}
//
//	public SimRate getInfectionRate()
//	{
//		return this.infectionRate;
//	}
}
