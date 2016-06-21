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
package nl.rivm.cib.episim.model.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Dimensionless;

import org.jscience.physics.amount.Amount;

import nl.rivm.cib.episim.model.Infection;
import nl.rivm.cib.episim.model.Population;
import nl.rivm.cib.episim.time.Indicator;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link PopulationMetrics} follows common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>
 * 
 * <table>
 * <tr>
 * <th>&mu;</th>
 * <td>Average death rate</td>
 * </tr>
 * <tr>
 * <th>B</th>
 * <td>Average birth rate</td>
 * </tr>
 * <tr>
 * <th>N</th>
 * <td>Total population</td>
 * </tr>
 * </table>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface PopulationMetrics extends Timed
{

	Population getPopulation();

	InfectionMetrics metricsOf( Infection infection );

	Indicator<Dimensionless> getSize();

	Indicator<Dimensionless> getBirths();

	Indicator<Dimensionless> getDeaths();

	Indicator<Dimensionless> getImmigrations();

	Indicator<Dimensionless> getEmigrations();

	class Simple implements PopulationMetrics
	{

		/** */
		private final Population population;

		/** */
		private final Indicator<Dimensionless> size;

		/** */
		private final Indicator<Dimensionless> births;

		/** */
		private final Indicator<Dimensionless> deaths;

		/** */
		private final Indicator<Dimensionless> immigrations;

		/** */
		private final Indicator<Dimensionless> emigrations;

		public Simple( final Population population )
		{
			this.population = population;
			this.size = Indicator.of( population.scheduler(), Amount.ZERO );
			this.births = Indicator.of( population.scheduler(), Amount.ZERO );
			this.deaths = Indicator.of( population.scheduler(), Amount.ZERO );
			this.immigrations = Indicator.of( population.scheduler(),
					Amount.ZERO );
			this.emigrations = Indicator.of( population.scheduler(),
					Amount.ZERO );
		}

		@Override
		public Population getPopulation()
		{
			return this.population;
		}

		@Override
		public Scheduler scheduler()
		{
			return getPopulation().scheduler();
		}

		private final Map<Infection, InfectionMetrics> infections = Collections
				.synchronizedMap( new HashMap<>() );

		@Override
		public InfectionMetrics metricsOf( final Infection infection )
		{
			synchronized( this.infections )
			{
				InfectionMetrics result = this.infections.get( infection );
				if(result == null)
				{
					//result = InfectionMetrics.
				}
				return result;
			}
		}

		@Override
		public Indicator<Dimensionless> getSize()
		{
			return this.size;
		}

		@Override
		public Indicator<Dimensionless> getBirths()
		{
			return this.births;
		}

		@Override
		public Indicator<Dimensionless> getDeaths()
		{
			return this.deaths;
		}

		@Override
		public Indicator<Dimensionless> getImmigrations()
		{
			return this.immigrations;
		}

		@Override
		public Indicator<Dimensionless> getEmigrations()
		{
			return this.emigrations;
		}

	}
}
