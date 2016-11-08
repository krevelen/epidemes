/* $Id: c6080295706e65dec4fef8c59bdb099fff2b1f3a $
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
package nl.rivm.cib.episim.model.person;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;

import io.coala.math.QuantityUtil;
import io.coala.time.Indicator;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.person.Population.Birth;
import nl.rivm.cib.episim.model.person.Population.Death;
import nl.rivm.cib.episim.model.person.Population.Emigration;
import nl.rivm.cib.episim.model.person.Population.Immigration;

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
 * @version $Id: c6080295706e65dec4fef8c59bdb099fff2b1f3a $
 * @author Rick van Krevelen
 */
public interface PopulationMetrics extends Proactive
{

//	Population<?> population();

	Indicator<Dimensionless> size();

	Indicator<Dimensionless> births();

	Indicator<Dimensionless> deaths();

	Indicator<Dimensionless> immigrated();

	Indicator<Dimensionless> emigrated();

	static PopulationMetrics of( final Population<?> population )
	{
		return of( population, QuantityUtil.ZERO, QuantityUtil.ZERO,
				QuantityUtil.ZERO, QuantityUtil.ZERO );
	}

	static PopulationMetrics of( final Population<?> population,
		final Quantity<Dimensionless> initialBirths,
		final Quantity<Dimensionless> initialDeaths,
		final Quantity<Dimensionless> initialImmigrated,
		final Quantity<Dimensionless> initialEmigrated )
	{
		final Indicator<Dimensionless> size = Indicator.of(
				population.scheduler(),
				QuantityUtil.valueOf( population.members().size() ) );
		final Indicator<Dimensionless> births = Indicator
				.of( population.scheduler(), initialBirths );
		final Indicator<Dimensionless> deaths = Indicator
				.of( population.scheduler(), initialDeaths );
		final Indicator<Dimensionless> immigrations = Indicator
				.of( population.scheduler(), initialImmigrated );
		final Indicator<Dimensionless> emigrations = Indicator
				.of( population.scheduler(), initialEmigrated );
		population.on( Birth.class )
				.doOnNext( birth -> births.add( birth.arrivals().size() ) );
		population.on( Death.class )
				.doOnNext( death -> deaths.add( death.departures().size() ) );
		population.on( Immigration.class ).doOnNext( immigration -> immigrations
				.add( immigration.arrivals().size() ) );
		population.on( Emigration.class ).doOnNext( emigration -> emigrations
				.add( emigration.departures().size() ) );
		population.members().onSize().map( QuantityUtil::valueOf )
				.doOnNext( i -> size.setValue( i ) );

		return new PopulationMetrics()
		{
//			@Override
//			public Population<?> population()
//			{
//				return population;
//			}

			@Override
			public Scheduler scheduler()
			{
				return population.scheduler();
			}

			@Override
			public Indicator<Dimensionless> size()
			{
				return size;
			}

			@Override
			public Indicator<Dimensionless> births()
			{
				return births;
			}

			@Override
			public Indicator<Dimensionless> deaths()
			{
				return deaths;
			}

			@Override
			public Indicator<Dimensionless> immigrated()
			{
				return immigrations;
			}

			@Override
			public Indicator<Dimensionless> emigrated()
			{
				return emigrations;
			}
		};
	}
}
