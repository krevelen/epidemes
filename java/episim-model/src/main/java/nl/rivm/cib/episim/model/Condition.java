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

import io.coala.exception.ExceptionFactory;
import nl.rivm.cib.episim.time.Scheduler;
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

	Individual getIndividual();

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
	//Boolean isSeropositive();

	/**
	 * @param event an {@link Observable} stream of {@link TransitionEvent}s for
	 *            this {@link Infection}
	 */
	Observable<TransitionEvent<?>> emitTransitions();

	/**
	 * initiate infection (infectiousness, symptoms, etc.)
	 */
	void infect();

	// FIXME void treat(TreatmentStage stage);

	/**
	 * {@link Simple} implementation of {@link Condition}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Condition
	{

		/**
		 * @param individual the {@link Individual}
		 * @param infection the {@link Infection}
		 * @return a {@link Simple} instance of {@link Condition}
		 */
		public static Simple of( final Individual individual,
			final Infection infection )
		{
			return of( individual, infection,
					EpidemicCompartment.Simple.SUSCEPTIBLE,
					SymptomPhase.ASYMPTOMATIC, TreatmentStage.UNTREATED );
		}

		/**
		 * @param individual the {@link Individual}
		 * @param infection the {@link Infection}
		 * @param compartment the {@link EpidemicCompartment}
		 * @param symptoms the {@link SymptomPhase}
		 * @param treatment the {@link TreatmentStage}
		 * @return a {@link Simple} instance of {@link Condition}
		 */
		public static Simple of( final Individual individual,
			final Infection infection, final EpidemicCompartment compartment,
			final SymptomPhase symptoms, final TreatmentStage treatment )
		{
			return new Simple( individual, infection, compartment, symptoms,
					treatment );
		}

		private final Individual individual;

		private final Infection infection;

		private EpidemicCompartment compartment;

		private SymptomPhase symptoms;

		private TreatmentStage treatment;

		private final Subject<TransitionEvent<?>, TransitionEvent<?>> transitions = PublishSubject
				.create();

		/**
		 * {@link Simple} constructor
		 * 
		 * @param individual the {@link Individual}
		 * @param infection the {@link Infection}
		 * @param compartment the {@link EpidemicCompartment}
		 * @param symptoms the {@link SymptomPhase}
		 * @param treatment the {@link TreatmentStage}
		 */
		public Simple( final Individual individual, final Infection infection,
			final EpidemicCompartment compartment, final SymptomPhase symptoms,
			final TreatmentStage treatment )
		{
			this.individual = individual;
			this.infection = infection;
			this.compartment = compartment;
			this.symptoms = symptoms;
			this.treatment = treatment;
		}

		protected void set( final EpidemicCompartment compartment )
		{
			this.transitions.onNext( TransitionEvent.of( this, compartment ) );
			this.compartment = compartment;
		}

		protected void set( final TreatmentStage treatment )
		{
			this.transitions.onNext( TransitionEvent.of( this, treatment ) );
			this.treatment = treatment;
		}

		protected void set( final SymptomPhase symptoms )
		{
			this.transitions.onNext( TransitionEvent.of( this, symptoms ) );
			this.symptoms = symptoms;
		}

		@Override
		public Individual getIndividual()
		{
			return this.individual;
		}

		@Override
		public Scheduler scheduler()
		{
			return getIndividual().scheduler();
		}

		@Override
		public Infection getInfection()
		{
			return this.infection;
		}

		@Override
		public EpidemicCompartment getCompartment()
		{
			return this.compartment;
		}

		@Override
		public TreatmentStage getTreatmentStage()
		{
			return this.treatment;
		}

		@Override
		public SymptomPhase getSymptomPhase()
		{
			return this.symptoms;
		}

		@Override
		public Observable<TransitionEvent<?>> emitTransitions()
		{
			return this.transitions.asObservable();
		}

		@Override
		public void infect()
		{
			if( !getCompartment().isSusceptible() )
				throw ExceptionFactory.createUnchecked(
						"Can't become exposed when: {}", getCompartment() );

			set( EpidemicCompartment.Simple.EXPOSED );

			after( getInfection().drawLatentPeriod() )
					.call( this::set, EpidemicCompartment.Simple.INFECTIVE )
					.thenAfter( getInfection().drawRecoverPeriod() )
					.call( this::set, EpidemicCompartment.Simple.RECOVERED )
					.thenAfter( getInfection().drawWanePeriod() )
					.call( this::set, EpidemicCompartment.Simple.SUSCEPTIBLE );
			after( getInfection().drawOnsetPeriod() )
					.call( this::set, SymptomPhase.SYSTEMIC )
					.thenAfter( getInfection().drawSymptomPeriod() )
					.call( this::set, SymptomPhase.ASYMPTOMATIC );
		}
	}
}
