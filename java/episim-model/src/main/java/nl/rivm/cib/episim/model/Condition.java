/* $Id: 7d050621c6a3d6a11eae377c674e4b6c9903f5ed $
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

import java.util.Map;
import java.util.function.Function;

import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Schedule;
import nl.rivm.cib.episim.time.Timed;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Condition} represents the {@link Infection} bookkeeping dynamics for
 * an individual {@link Carrier}
 * 
 * @version $Id: 7d050621c6a3d6a11eae377c674e4b6c9903f5ed $
 * @author Rick van Krevelen
 */
public interface Condition extends Timed
{

	Infection getInfection();

	/**
	 * @return the current {@link EpidemicCompartment} of this {@link Condition}
	 */
	EpidemicCompartment getCompartment();

	/** @return the current {@link TreatmentStage} of this {@link Condition} */
	TreatmentStage getTreatmentStage();

	/** @return the current {@link SymptomPhase} of this {@link Condition} */
	SymptomPhase getSymptomPhase();

	/**
	 * @return {@code true} iff this {@link Condition} yields seropositive blood
	 *         tests (i.e. after seroconversion, where antibody &gt;&gt;
	 *         antigen), {@code false} otherwise
	 */
	Boolean isSeropositive();

	/**
	 * @param event an {@link Observable} stream of {@link TransitionEvent}s for
	 *            this {@link Infection}
	 */
	Observable<TransitionEvent<?>> emitTransitions();

	class Simple implements Condition
	{
		private final Scheduler scheduler;

		private final Infection infection;

		private final Function<Instant, EpidemicCompartment> compartment;

		private final Function<Instant, SymptomPhase> symptoms;

		private final Function<Instant, TreatmentStage> treatment;

		private final Function<Instant, Boolean> seroconversion;

		private final Subject<TransitionEvent<?>, TransitionEvent<?>> transitions = PublishSubject
				.create();

		public Simple( final Scheduler scheduler, final Infection infection,
			final EpidemicCompartment compartment, final SymptomPhase symptoms,
			final TreatmentStage treatment, final Boolean seroconversion )
		{
			this( scheduler, infection, ( Instant t ) ->
			{
				return compartment;
			}, ( Instant t ) ->
			{
				return symptoms;
			}, ( Instant t ) ->
			{
				return treatment;
			}, ( Instant t ) ->
			{
				return seroconversion;
			} );
		}

		public Simple( final Scheduler scheduler, final Infection infection,
			final Map<Duration, EpidemicCompartment> compartment,
			final Map<Duration, SymptomPhase> symptoms,
			final Map<Duration, TreatmentStage> treatment,
			final Map<Duration, Boolean> seroconversion )
		{
			this( scheduler, infection,
					Schedule.of( scheduler.now(), compartment )::floor,
					Schedule.of( scheduler.now(), symptoms )::floor,
					Schedule.of( scheduler.now(), treatment )::floor,
					Schedule.of( scheduler.now(), seroconversion )::floor );
		}

		public Simple( final Scheduler scheduler, final Infection infection,
			final Function<Instant, EpidemicCompartment> compartment,
			final Function<Instant, SymptomPhase> symptoms,
			final Function<Instant, TreatmentStage> treatment,
			final Function<Instant, Boolean> seroconversion )
		{
			this.scheduler = scheduler;
			this.infection = infection;
			this.compartment = compartment;
			this.symptoms = symptoms;
			this.treatment = treatment;
			this.seroconversion = seroconversion;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Infection getInfection()
		{
			return this.infection;
		}

		@Override
		public EpidemicCompartment getCompartment()
		{
			return this.compartment.apply( now() );
		}

		@Override
		public TreatmentStage getTreatmentStage()
		{
			return this.treatment.apply( now() );
		}

		@Override
		public SymptomPhase getSymptomPhase()
		{
			return this.symptoms.apply( now() );
		}

		@Override
		public Boolean isSeropositive()
		{
			return this.seroconversion.apply( now() );
		}

		@Override
		public Observable<TransitionEvent<?>> emitTransitions()
		{
			return this.transitions.asObservable();
		}

	}

}
