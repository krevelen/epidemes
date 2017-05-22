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
package nl.rivm.cib.episim.pilot;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Singleton;

import io.coala.enterprise.Actor;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.infection.Pathogen;

/**
	 * {@link Measles}
	 * 
	 * <p>
	 * Disease (Vink et al. 2014) Serial Interval (latent + infectious), days
	 * Anderson and May 1991 (6) Vynnycky and White 2010 (8) <br>
	 * Influenza A 3–6 vs 2–4 <br>
	 * Measles 12–16 vs 7–16 <br>
	 * HPV 100-150?
	 */
	@Singleton
	public class MeaslesFactory extends Actor.Factory.LocalCaching
	{
		// globally unique?
		private transient AtomicInteger mvTypeCounter = new AtomicInteger( 0 );

		public Pathogen createMeasles()
		{
			return super.create( Actor.ID
					.of( "MV-" + this.mvTypeCounter.incrementAndGet(), id() ) )
							.subRole( Pathogen.class );
		}

		/**
		 * {@link Measles} force of infection should result in a mean generation
		 * time (time between primary infecting secondary) of ca 11-12 days,
		 * with st.dev. 2-3 days, see Klinkenberg:2011:jtbi, secondary attack
		 * rate >90% in household/institutional contacts
		 * <p>
		 * for incubation and symptom times, see e.g.
		 * https://wwwnc.cdc.gov/travel/yellowbook/2016/infectious-diseases-related-to-travel/measles-rubeola
		 */
		public MeaslesFactory( final Scheduler scheduler,
			final ProbabilityDistribution.Factory distFact,
			final Condition.Factory conditionFactory )
		{
			super();

			// Pathogen.SimpleSEIR 

			// latent period (E->I) t+0, onset around t+14 but infectious already 4 days before
			distFact.createTriangular( 1, 14 - 4, 21 - 4 )//
					.toQuantities( TimeUnits.DAYS ).map( Duration::of );

			// infectious period (I->R): t+14-4, [onset-4,onset+4] 
			distFact.createTriangular( 0, 8, 10 ).toQuantities( TimeUnits.DAYS )
					.map( Duration::of );

			// wane period (R->S): infinite, forever immune
			distFact.createDeterministic( Duration.of( 100, TimeUnits.YEAR ) );

			// incubation period (E->C, sub->clinical symptoms)
			// : t+7-21 days, rash, fever, Koplik spots, ...
			// : t+9-12 days (https://www.wikiwand.com/en/Incubation_period)
			distFact.createTriangular( 7, 11, 21 )
					.toQuantities( TimeUnits.DAYS ).map( Duration::of );

			// symptom period (C->N, normal/asymptomatic)
			distFact.createTriangular( 4, 5, 7 ).toQuantities( TimeUnits.DAYS )
					.map( Duration::of );

//				force of infection (S->E): t=?, viral shedding -> respiratory/surface contact 
//				if( // TODO if  (infection pressure [=#contacts] > threshold) &&
//				conditionOf( person ).compartment().isSusceptible() )
		}
	}