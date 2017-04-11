/* $Id: 0d19d6801cb9fefc090fe46d46fc23c2d3afc275 $
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
package nl.rivm.cib.episim.model.disease;

import io.coala.enterprise.Actor;
import nl.rivm.cib.episim.model.disease.infection.EpidemicCompartment;

/**
 * {@link Condition} represents the {@link Disease} bookkeeping dynamics for
 * e.g. an {@link Afflicted} person
 * 
 * measles, see e.g. https://en.wikipedia.org/wiki/File:Measles.webm; influenza
 * see e.g. Figure 5 in https://doi.org/10.1093/aje/kwm375
 * 
 * @version $Id: 0d19d6801cb9fefc090fe46d46fc23c2d3afc275 $
 * @author Rick van Krevelen
 */
public interface Condition
	extends Actor<Disease>, EpidemicCompartment.Attributable<Condition>,
	MedicalStage.Attributable<Condition>,
	ClinicalPhase.Attributable<Condition>, SerologicState.Attributable<Condition>
{

	
	
	// TODO get a single stream of all bean property/attribute changes

//	/** @return the {@link Observable} stream of {@link EpidemicCompartment}s */
//	Observable<EpidemicCompartment> compartmentStream();

//	/** @return the {@link Observable} stream of {@link TreatmentStage}s */
//	Observable<TreatmentStage> treatmentStream();

//	/** @return the {@link Observable} stream of {@link SymptomPhase}s */
//	Observable<SymptomPhase> symptomsStream();

//	/**
//	 * @return {@code true} iff this {@link Condition} yields seropositive blood
//	 *         tests (i.e. after seroconversion, where antibody &gt;&gt;
//	 *         antigen), {@code false} otherwise
//	 */
//	Observable<Boolean> seropositive();

//	@Singleton
//	interface Factory
//	{
//		default Condition create( final Afflicted individual,
//			final Disease infection )
//		{
//			return create( individual, infection,
//					EpidemicCompartment.Simple.SUSCEPTIBLE,
//					SymptomPhase.ASYMPTOMATIC, TreatmentStage.UNTREATED );
//		}
//
//		/**
//		 * @param individual the {@link Individual}
//		 * @param infection the {@link Pathogen}
//		 * @param compartment the {@link EpidemicCompartment}
//		 * @param symptoms the {@link SymptomPhase}
//		 * @param treatment the {@link TreatmentStage}
//		 * @return a {@link Simple} instance of {@link Condition}
//		 */
//		default Condition create( final Afflicted individual,
//			final Disease infection, final EpidemicCompartment compartment,
//			final SymptomPhase symptoms, final TreatmentStage treatment )
//		{
//			return Simple.of( individual, infection, compartment, symptoms,
//					treatment );
//		}
//	}
//
//	/**
//	 * {@link Simple} implementation of {@link Condition}
//	 */
//	class Simple implements Condition
//	{
//
//		/**
//		 * @param individual the {@link Individual}
//		 * @param infection the {@link Pathogen}
//		 * @param compartment the {@link EpidemicCompartment}
//		 * @param symptoms the {@link SymptomPhase}
//		 * @param treatment the {@link TreatmentStage}
//		 * @return a {@link Simple} instance of {@link Condition}
//		 */
//		public static Simple of( final Afflicted individual,
//			final Disease infection, final EpidemicCompartment compartment,
//			final SymptomPhase symptoms, final TreatmentStage treatment )
//		{
//			return new Simple( individual, infection, compartment, symptoms,
//					treatment );
//		}
//
//		private Actor.ID personRef;
//
//		private Disease.ID diseaseRef;
//
//		private BehaviorSubject<EpidemicCompartment> compartment;
//
//		private BehaviorSubject<SymptomPhase> symptoms;
//
//		private BehaviorSubject<TreatmentStage> treatment;
//
//		private BehaviorSubject<Boolean> seropositive;
//
//		/**
//		 * {@link Simple} constructor
//		 * 
//		 * @param person the {@link Afflicted}
//		 * @param disease the {@link Disease}
//		 * @param compartment the {@link EpidemicCompartment}
//		 * @param symptoms the {@link SymptomPhase}
//		 * @param treatment the {@link TreatmentStage}
//		 */
//		public Simple( final Afflicted person, final Disease disease,
//			final EpidemicCompartment compartment, final SymptomPhase symptoms,
//			final TreatmentStage treatment )
//		{
//			this.personRef = person.id();
//			this.diseaseRef = disease.id();
//			this.compartment = BehaviorSubject.create( compartment );
//			this.symptoms = BehaviorSubject.create( symptoms );
//			this.treatment = BehaviorSubject.create( treatment );
//		}
//
//		@Override
//		public Actor.ID personRef()
//		{
//			return this.personRef;
//		}
//
//		@Override
//		public Disease.ID diseaseRef()
//		{
//			return this.diseaseRef;
//		}
//
//		@Override
//		public Observable<EpidemicCompartment> compartmentStream()
//		{
//			return this.compartment;
//		}
//
//		@Override
//		public Observable<TreatmentStage> treatmentStream()
//		{
//			return this.treatment;
//		}
//
//		@Override
//		public Observable<SymptomPhase> symptomsStream()
//		{
//			return this.symptoms;
//		}
//
//		@Override
//		public Observable<Boolean> seropositive()
//		{
//			return this.seropositive;
//		}
//
//		@Override
//		public EpidemicCompartment compartment()
//		{
//			return this.compartment.getValue();
//		}
//
//		@Override
//		public TreatmentStage treatment()
//		{
//			return this.treatment.getValue();
//		}
//
//		@Override
//		public SymptomPhase symptoms()
//		{
//			return this.symptoms.getValue();
//		}
//
//		@Override
//		public void set( final EpidemicCompartment current )
//		{
//			this.compartment.onNext( current );
//		}
//
//		@Override
//		public void set( final TreatmentStage current )
//		{
//			this.treatment.onNext( current );
//		}
//
//		@Override
//		public void set( final SymptomPhase current )
//		{
//			this.symptoms.onNext( current );
//		}
//	}
}
