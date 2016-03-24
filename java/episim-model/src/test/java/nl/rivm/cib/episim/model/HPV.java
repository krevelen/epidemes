/* $Id: 7d8028f0f722028f7c4fec13582c7fedcd01f8a0 $
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
 * {@link HPV} or Human papillomavirus is transmitted through sexual contact.
 * Its genital infections can cause anogenital cancers such as cervical cancer
 * (baarmoederhalskanker), genital warts (papillomas), and head and neck
 * cancers. It has a <a href="http://www.who.int/immunization/hpv/en/">WHO
 * page</a> and
 * <a href="http://www.who.int/mediacentre/factsheets/fs380/en/">WHO fact
 * sheet</a>, an
 * <a href="http://emedicine.medscape.com/article/219110-overview">eMedicine
 * description</a> and a
 * <a href="http://www.diseasesdatabase.com/ddb6032.htm">DiseaseDB entry</a>,
 * from <a href="https://en.wikipedia.org/wiki/Papillomaviridae">wikipedia</a>:
 * <ul>
 * <li>&ldquo;Papillomas caused by some types ... such as human papillomaviruses
 * 16 and 18, carry a risk of becoming cancerous.&rdquo;</li>
 * <li>&ldquo;Over 170 human papillomavirus types have been completely
 * sequenced.&rdquo;</li>
 * </ul>
 * 
 * @version $Id: 7d8028f0f722028f7c4fec13582c7fedcd01f8a0 $
 * @author Rick van Krevelen
 */
public abstract class HPV implements Infection
{

//	@SuppressWarnings( "unchecked" )
//	private static final Map<TransmissionRoute, Amount<Dimensionless>> ROUTE_LIKELIHOODS = map(
//			entry( TransmissionRoute.SEXUAL, Amount.ONE ) );
//
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
