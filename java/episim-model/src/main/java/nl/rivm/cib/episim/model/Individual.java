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
package nl.rivm.cib.episim.model;

import javax.measure.Measurable;
import javax.measure.quantity.Duration;

import rx.Observable;

/**
 * {@link Individual} represents an infective/infectious subject
 * 
 * @version $Date$
 * @author Rick van Krevelen
 */
public interface Individual extends Carrier

{

	Gender getGender();

	/**
	 * @return the current age or {@link Duration} of this {@link Individual}
	 */
	Measurable<Duration> getAge();

	/**
	 * @return the current {@link Location} of this {@link Individual}
	 */
	Location getLocation();

	/**
	 * @return the {@link Observable} stream of {@link TravelEvent}s generated
	 *         by this {@link Individual} based on some travel behavior
	 */
	Observable<TravelEvent> getTravels();

//	public Instant birth;
//
//	public Location currentLocation;
//
//	public TravelBehavior travelBehavior;
//
//	// social network dynamics (incl self): links/rates change due to media
//	// attention/campaigns, active search, medical consultations
//	public Map<Subject, Rate> complianceAuthorities;
//
//	// social network dynamics: links/rates change due to movements
//	public Map<Subject, Rate> directContacts;
//
//	// social network dynamics: links/rates change due to movements
//	public Map<Subject, Rate> indirectContacts;
//
//	// social network dynamics: links change due to behavior types (e.g. risky)
//	public Map<Subject, Rate> sexualContacts;

}
