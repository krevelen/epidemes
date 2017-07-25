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
package nl.rivm.cib.episim.model.disease.infection;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import io.coala.exception.Thrower;
import io.coala.math.DecimalUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

/**
 * {@link InfectionPressure} calculates the infection speed or pressure for a
 * susceptible individual given some composition of persons in epidemic states
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@FunctionalInterface
public interface InfectionPressure
{
	/**
	 * @param infectives the number of infective occupants
	 * @param others the number of non-infective occupants
	 * @return the infection pressure (speed factor)
	 */
	BigDecimal calculate( long infectives, long others );

	/** @see Binary */
	InfectionPressure BINARY = new Binary();

	/** @see Proportional */
	InfectionPressure PROPORTIONAL = new Proportional();

	default Observable<BigDecimal> map( final Observable<long[]> args )
	{
		return args.map( v -> calculate( v[0], v[1] ) ).distinctUntilChanged();
	}

	default Contagium map( final Consumer<BigDecimal> consumer )
	{
		final Contagium result = new Contagium();
		result.emit( this ).subscribe( consumer, Throwable::printStackTrace );
		return result;
	}

	default Contagium map( final Observer<BigDecimal> observer )
	{
		final Contagium result = new Contagium();
		result.emit( this ).subscribe( observer );
		return result;
	}

	/**
	 * @param counts the number of occupants per epidemic compartment
	 * @return the infection pressure (speed factor)
	 */
	default BigDecimal calculate( final Map<EpidemicCompartment, Long> counts )
	{
		long infectives = 0L, others = 0L;
		for( Map.Entry<EpidemicCompartment, Long> entry : Objects
				.requireNonNull( counts, "No counts" ).entrySet() )
			if( entry.getValue() < 0L )
				return Thrower.throwNew( IllegalArgumentException::new,
						() -> "Negative amount: " + entry );
			else if( entry.getKey().isInfective() )
				infectives += entry.getValue();
			else
				others += entry.getValue();
		return calculate( infectives, others );
	}

	/**
	 * {@link Binary} is defined as {@code 1} (normal speed/pressure) for any
	 * {@code infectives > 0 AND non-infectives > 0}; {@code 0} otherwise
	 */
	class Binary implements InfectionPressure
	{
		@Override
		public BigDecimal calculate( final long infectives, final long others )
		{
			return infectives < 1L || others < 1L ? BigDecimal.ZERO
					: BigDecimal.ONE;
		}
	}

	/**
	 * {@link Proportional} is defined as {@code |N=I|/|N\I|} (speed/pressure
	 * proportional to number of infectives per non-infective) for
	 * {@code infectives > 0 AND non-infectives > 0}; {@code 0} otherwise
	 */
	class Proportional implements InfectionPressure
	{
		@Override
		public BigDecimal calculate( final long infectives, final long others )
		{
			return infectives < 1L || others < 1L ? BigDecimal.ZERO
					: DecimalUtil.divide( infectives, others );
		}
	}

	/**
	 * {@link Contagium}
	 */
	class Contagium
	{
		private final BehaviorSubject<long[]> occupancy = BehaviorSubject
				.createDefault( new long[]
		{ 0L, 0L } );

		public Contagium change( final long deltaInfectives,
			final long deltaOther )
		{
			if( deltaInfectives == 0 && deltaOther == 0 ) return this;
			final long[] v = this.occupancy.getValue();
			this.occupancy.onNext(
					new long[]
			{ v[0] + deltaInfectives, v[1] + deltaOther } );
			return this;
		}

		public Observable<BigDecimal> emit( final InfectionPressure pressure )
		{
			return pressure.map( this.occupancy );
		}
	}
}