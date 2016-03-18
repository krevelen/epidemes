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

import io.coala.exception.x.ExceptionBuilder;
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
	//Boolean isSeropositive();
	
	void infect();

	/**
	 * @param event an {@link Observable} stream of {@link TransitionEvent}s for
	 *            this {@link Infection}
	 */
	Observable<TransitionEvent<?>> emitTransitions();

	class Simple implements Condition
	{
		private final Scheduler scheduler;

		private final Infection infection;

		private EpidemicCompartment compartment;

		private SymptomPhase symptoms;

		private TreatmentStage treatment;

		private final Subject<TransitionEvent<?>, TransitionEvent<?>> transitions = PublishSubject
				.create();

		public Simple( final Scheduler scheduler, final Infection infection )
		{
			this( scheduler, infection, EpidemicCompartment.Simple.SUSCEPTIBLE,
					SymptomPhase.ASYMPTOMATIC, TreatmentStage.UNTREATED );
		}

		public Simple( final Scheduler scheduler, final Infection infection,
			final EpidemicCompartment compartment, final SymptomPhase symptoms,
			final TreatmentStage treatment )
		{
			this.scheduler = scheduler;
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

		public void infect()
		{
			if( !getCompartment().isSusceptible() ) throw ExceptionBuilder
					.unchecked( "Can't be exposed when: %s", getCompartment() )
					.build();

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
	}
}
