/* $Id: 14a474927d6edfc812c1107ca6057531008db526 $
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

import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.time.Duration;
import io.coala.time.Instant;
import nl.rivm.cib.episim.model.disease.Afflict;
import nl.rivm.cib.episim.model.disease.Afflicted;
import nl.rivm.cib.episim.model.disease.infection.Contagion.Contagium;

/**
 * {@link Occupancy} is initiated by a person to a {@link Contagium},
 * possibly containing or introducing infectious {@link Afflicted} host(s) or
 * infested sites, to produce a mapping of <b>time-at-risk per disease</b>, each
 * accumulating to the likelihood of this or another susceptible visitor's
 * {@link Transmission} to the respective {@link Afflict}(s)
 * 
 * <li>{@link #id()}: fact identifier
 * <li>{@link #transaction()}: links {@link Afflicted} &hArr;
 * {@link Contagium}
 * <li>{@link #kind()}: enter = {@link FactKind#REQUESTED rq}, leave =
 * {@link FactKind#STATED st}
 * <li>{@link #occurrence()}: [rq] visit start {@link Instant}
 * <li>{@link #expiration()}: [rq] visit end {@link Instant}
 * <li>{@link #causeRef()}: reference to cause, e.g. {@link ActivityFact}
 * <li>{@link #timeAtRisk(}: (stated) {@link Duration time-at-risk} per
 * {@link Afflict.ID}
 * 
 * @version $Id: 14a474927d6edfc812c1107ca6057531008db526 $
 * @author Rick van Krevelen
 */
public interface Occupancy extends Fact
{

	/** @return fraction of (average) no. infectious per total occupancy */
	Number getForce();

//	Map<Afflict.ID, Duration> timeAtRisk();
//
//	void setTimeAtRisk( Map<Afflict.ID, Duration> value );
//
//	default Visit withTimeAtRisk( final Map<Afflict.ID, Duration> value )
//	{
//		setTimeAtRisk( value );
//		return this;
//	}

//	/**
//	 * @param start
//	 * @param duration
//	 * @param space
//	 * @param route
//	 * @param primary
//	 * @param secondary
//	 * @return
//	 */
//	static Visit of( Instant start, Duration duration, TransmissionSpace space,
//		TransmissionRoute route, Condition primary, Condition secondary )
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}

}