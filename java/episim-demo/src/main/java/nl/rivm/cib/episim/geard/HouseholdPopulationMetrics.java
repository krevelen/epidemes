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
package nl.rivm.cib.episim.geard;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;

import io.coala.math.QuantityUtil;
import io.coala.time.Indicator;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.geard.Population.Birth;
import nl.rivm.cib.episim.geard.Population.Death;
import nl.rivm.cib.episim.geard.Population.Emigration;
import nl.rivm.cib.episim.geard.Population.Immigration;

/**
 * {@link HouseholdPopulationMetrics}
 * 
 * @version $Id: c6080295706e65dec4fef8c59bdb099fff2b1f3a $
 * @author Rick van Krevelen
 */
public interface HouseholdPopulationMetrics extends PopulationMetrics
{

//	Population<?> population();

	Indicator<Dimensionless> householdCount();

	Indicator<Dimensionless> merges();

	Indicator<Dimensionless> splits();

	Indicator<Dimensionless> homeleaves();

	Indicator<Dimensionless> disbands();

	static HouseholdPopulationMetrics
		of( final HouseholdPopulation<?> population )
	{
		return of( population, QuantityUtil.ZERO, QuantityUtil.ZERO,
				QuantityUtil.ZERO, QuantityUtil.ZERO, QuantityUtil.ZERO,
				QuantityUtil.ZERO, QuantityUtil.ZERO, QuantityUtil.ZERO );
	}

	static HouseholdPopulationMetrics of(
		final HouseholdPopulation<?> population,
		final Quantity<Dimensionless> initialBirths,
		final Quantity<Dimensionless> initialDeaths,
		final Quantity<Dimensionless> initialImmigrated,
		final Quantity<Dimensionless> initialEmigrated,
		final Quantity<Dimensionless> initialMerges,
		final Quantity<Dimensionless> initialSplits,
		final Quantity<Dimensionless> initialLeaves,
		final Quantity<Dimensionless> initialEmpties )
	{
		final Indicator<Dimensionless> size = Indicator.of(
				population.scheduler(),
				QuantityUtil.valueOf( population.members().size() ) );
		population.members().onSize().map( i -> QuantityUtil.valueOf( i ) )
				.doOnNext( i -> size.setValue( i ) );

		final Indicator<Dimensionless> hhCount = Indicator.of(
				population.scheduler(),
				QuantityUtil.valueOf( population.households().size() ) );
		population.households().onSize().map( i -> QuantityUtil.valueOf( i ) )
				.doOnNext( i -> hhCount.setValue( i ) );

		final Indicator<Dimensionless> births = Indicator
				.of( population.scheduler(), initialBirths );
		population.on( Birth.class,
				birth -> births.add( birth.arrivals().size() ) );
		final Indicator<Dimensionless> deaths = Indicator
				.of( population.scheduler(), initialDeaths );
		population.on( Death.class,
				death -> deaths.add( death.departures().size() ) );
		final Indicator<Dimensionless> immigrations = Indicator
				.of( population.scheduler(), initialImmigrated );
		population.on( Immigration.class, immigration -> immigrations
				.add( immigration.arrivals().size() ) );
		final Indicator<Dimensionless> emigrations = Indicator
				.of( population.scheduler(), initialEmigrated );
		population.on( Emigration.class, emigration -> emigrations
				.add( emigration.departures().size() ) );
		final Indicator<Dimensionless> merges = Indicator
				.of( population.scheduler(), initialMerges );
		population.on( Merge.class, merge -> emigrations.add( 1 ) );
		final Indicator<Dimensionless> splits = Indicator
				.of( population.scheduler(), initialSplits );
		population.on( Split.class, split -> emigrations.add( 1 ) );
		final Indicator<Dimensionless> leaves = Indicator
				.of( population.scheduler(), initialLeaves );
		population.on( Leave.class, leave -> emigrations.add( 1 ) );
		final Indicator<Dimensionless> disbands = Indicator
				.of( population.scheduler(), initialEmpties );
		population.on( Empty.class, empty -> emigrations.add( 1 ) );

		return new HouseholdPopulationMetrics()
		{
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

			@Override
			public Indicator<Dimensionless> householdCount()
			{
				return hhCount;
			}

			@Override
			public Indicator<Dimensionless> merges()
			{
				return merges;
			}

			@Override
			public Indicator<Dimensionless> splits()
			{
				return splits;
			}

			@Override
			public Indicator<Dimensionless> homeleaves()
			{
				return leaves;
			}

			@Override
			public Indicator<Dimensionless> disbands()
			{
				return disbands;
			}
		};
	}

	public static class Merge<T extends Participant> extends DemographicEvent<T>
	{
	}

	public static class Split<T extends Participant> extends DemographicEvent<T>
	{
	}

	public static class Leave<T extends Participant> extends DemographicEvent<T>
	{
	}

	public static class Empty<T extends Participant> extends DemographicEvent<T>
	{
	}
}
