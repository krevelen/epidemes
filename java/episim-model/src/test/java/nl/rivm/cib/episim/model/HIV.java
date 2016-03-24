/* $Id: fd37618aea20f73be13441bd9128542a49bb4c1c $
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

import nl.rivm.cib.episim.model.Infection;

/**
 * {@link HIV} or the Human Immunodeficiency Virus has a
 * <a href="http://www.who.int/mediacentre/factsheets/fs360/en/">WHO fact
 * sheet</a>
 * 
 * @version $Id: fd37618aea20f73be13441bd9128542a49bb4c1c $
 * @author Rick van Krevelen
 */
public abstract class HIV implements Infection
{

//	@SuppressWarnings( "unchecked" )
//	private static final Map<TransmissionRoute, Amount<Dimensionless>> ROUTE_LIKELIHOODS = map(
//			entry( TransmissionRoute.SEXUAL, Amount.ONE ),
//			entry( TransmissionRoute.SEXUAL_ORAL, Amount.ONE ),
//			entry( TransmissionRoute.IATROGENIC, Amount.ONE ),
//			entry( TransmissionRoute.VERTICAL, Amount.ONE ) );

//	@Override
//	public Collection<TransmissionRoute> getTransmissionRoutes()
//	{
//		return ROUTE_LIKELIHOODS.keySet();
//	}
//
//	@Override
//	public Amount<Dimensionless> getTransmissionLikelihood(
//		final TransmissionRoute route, final Amount<Duration> duration,
//		final Relation relation, final Condition condition )
//	{
//		final Amount<Dimensionless> result = ROUTE_LIKELIHOODS.get( route );
//		return result == null ? Amount.ZERO : result;
//	}
}
