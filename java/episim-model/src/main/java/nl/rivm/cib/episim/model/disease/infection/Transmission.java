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

import io.coala.enterprise.FactKind;
import io.coala.time.TimingMap;
import nl.rivm.cib.episim.model.disease.Afflict;
import nl.rivm.cib.episim.model.disease.Afflicted;
import nl.rivm.cib.episim.model.disease.ClinicalPhase;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.infection.Contagion.Contagium;

/**
 * {@link Transmission} initiated by some {@link Pathogen} represents its
 * attempt to invade a (secondary) {@link Afflicted} host, e.g. during their
 * {@link Occupancy} of a {@link Contagium}, having:
 * 
 * <li>{@link #id()}: fact identifier
 * <li>{@link #transaction()}: links initiator {@link Pathogen} &hArr;
 * executor {@link Afflicted}
 * <li>{@link #causeRef()}: reference to cause, e.g. some {@link Occupancy}
 * <li>{@link #kind()}
 * <ul>
 * <li>pressure ({@link FactKind#REQUESTED rq}) &rArr; invasion
 * ({@link FactKind#STATED st}) or escape (either explicit
 * {@link FactKind#DECLINED dc} or implicit {@link #expiration() expire})
 * </ul>
 * <li>{@link #getForce()} or <em>force of infection</em> to quantify the
 * {@link Pathogen}'s <em>infection pressure</em> upon the executing
 * {@link Afflicted} host's immune system {@link Condition}
 * 
 * @version $Id: 0cf1c75df00a9cefc122aaec1f98d86596665550 $
 * @author Rick van Krevelen
 */
public interface Transmission extends Afflict
{

	/**
	 * the <em>force of infection</em> expressed in a (dimensionless)
	 * likelihood of invasion (assuming susceptible immune system), e.g.:
	 * <li>{@code 1} for deterministic/unavoidable, or
	 * <li>a fraction of (average) no. infectious per total occupancy
	 * 
	 * @return a dimensionless force of infection &isin; &lang;0,1]
	 */
	Number getForce();

	/**
	 * @return the {@link TimingMap} of {@link EpiCompartment} executed
	 *         upon invasion
	 */
	TimingMap<EpiCompartment> getEpidemiology();

	/**
	 * @return the {@link TimingMap} of {@link ClinicalPhase} executed upon
	 *         invasion
	 */
	TimingMap<ClinicalPhase> getPathology();

	/**
	 * @return the {@link TimingMap} of {@link Serostatus} executed upon
	 *         invasion
	 */
	TimingMap<Serostatus> getSerology();
}