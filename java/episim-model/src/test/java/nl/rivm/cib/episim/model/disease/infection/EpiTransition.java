///* $Id$
// * 
// * Part of ZonMW project no. 50-53000-98-156
// * 
// * @license
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy
// * of the License at
// * 
// * http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// * 
// * Copyright (c) 2016 RIVM National Institute for Health and Environment 
// */
//package nl.rivm.cib.episim.model.disease.infection;
//
//import com.fasterxml.jackson.annotation.JsonValue;
//
///**
// * {@link EpiTransition} see
// * <a href="https://en.wikipedia.org/wiki/Compartmental_models_in_epidemiology">
// * compartmental models</a>
// */
//public enum EpiTransition
//{
//	/**
//	 * M &rarr; S or M &rarr; N: passive/maternal immunity waning (frequency =
//	 * &delta;, mean period = <em>&delta;<sup>-1</sup></em>)
//	 */
//	PASSIVE( EpiCompartment.SimpleMSEIRS.SUSCEPTIBLE ),
//
//	/**
//	 * S &rarr; E (or S &rarr; I): pre-exposure contact or infection (frequency
//	 * = &beta;, mean period = <em>&beta;<sup>-1</sup></em>)
//	 */
//	SUSCEPTIBILITY( EpiCompartment.SimpleMSEIRS.EXPOSED ),
//
//	/**
//	 * E &rarr; I: viral loading/incubation (frequency = &sigma;, mean period =
//	 * <em>&sigma;<sup>-1</sup></em>)
//	 */
//	LATENCY( EpiCompartment.SimpleMSEIRS.INFECTIVE ),
//
//	/**
//	 * I &rarr; R: infectious/recovery (frequency = &gamma;, mean period =
//	 * <em>&gamma;<sup>-1</sup></em>)
//	 */
//	INFECTIOUS( EpiCompartment.SimpleMSEIRS.RECOVERED ),
//
//	/**
//	 * N &rarr; V (or S &rarr; V): pre-vaccination (newborn)
//	 * susceptibility/acceptance/hesitancy (frequency = &rho;, mean period =
//	 * <em>&rho;<sup>-1</sup></em>), possibly conditional on beliefs
//	 */
//	ACCEPTANCE( EpiCompartment.SimpleMSEIRS.VACCINATED ),
//
//	/**
//	 * R &rarr; S: wane/decline (frequency = &alpha;, mean period =
//	 * <em>&alpha;<sup>-1</sup></em>), possibly conditional on co-morbidity,
//	 * genetic factors, ...
//	 */
//	WANING( EpiCompartment.SimpleMSEIRS.DORMANT ),
//
//	/**
//	 * E &rarr; &empty;: dying/mortality (frequency = &mu;, mean period =
//	 * <em>&mu;<sup>-1</sup></em>)
//	 */
//	MORTALITY( null ),
//
//	/**
//	 * normal/absent &rarr; apparent/sub-/clinical: symptom latency/incubation
//	 */
////	APPEARANCE,
//
//	/**
//	 * apparent/sub-/clinical &rarr; normal/asymptomatic:
//	 * morbidity/apparent/disabled
//	 */
////	DISAPPEARANCE,
//
//	//
//	;
//
//	private final EpiCompartment.Compartment result;
//
//	private String jsonKey = null;
//
//	private EpiTransition( final EpiCompartment.Compartment result )
//	{
//		this.result = result;
//	}
//
//	public EpiCompartment.Compartment result()
//	{
//		return this.result;
//	}
//
//	@JsonValue
//	public String jsonKey()
//	{
//		return this.jsonKey != null ? this.jsonKey
//				: (this.jsonKey = name().toLowerCase() + "-period");
//	}
//}