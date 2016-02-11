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
package nl.rivm.cib.episim.model.location;

import java.util.Map;

import org.opengis.spatialschema.geometry.geometry.Position;

import io.coala.name.x.Id;
import nl.rivm.cib.episim.model.contagion.CarrierInfectivenessComparator;
import nl.rivm.cib.episim.model.contagion.Disease;

/**
 * {@link Location}
 * 
 * <table>
 * <tr>
 * <th>M</th>
 * <td>Passively immune infants</td>
 * </tr>
 * <tr>
 * <th>S</th>
 * <td>Susceptibles</td>
 * </tr>
 * <tr>
 * <th>E</th>
 * <td>Exposed individuals in the latent period</td>
 * </tr>
 * <tr>
 * <th>I</th>
 * <td>Infectives</td>
 * </tr>
 * <tr>
 * <th>R</th>
 * <td>Recovered with immunity</td>
 * </tr>
 * <tr>
 * <th>&beta;</th>
 * <td>Effective contact rate (= effective contacts per unit time)</td>
 * </tr>
 * <tr>
 * <th>N</th>
 * <td>Total population</td>
 * </tr>
 * </table>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Location //extends AbstractIdentifiable<Location.ID>
{
	public static class ID extends Id.OrdinalChild<String, String>
	{
		ID setValue( final String value )
		{
			wrap( value );
			return this;
		}
	}

	private ID id;

	public Position position;

	public Location parent;

//	public static class Cluster
//	{
//		private final NavigableSet<Carrier> occupants;
//
//		public Cluster( final CarrierInfectivenessComparator<?> comparator )
//		{
//			this.occupants = new ConcurrentSkipListSet<>( comparator );
//		}
//
//		public List<Individual> getSusceptibles()
//		{
//			return new ArrayList<>(
//					this.occupants.headSet( Individual.INFECTIVE, false ) );
//		}
//
//		public List<Individual> getInfectives()
//		{
//			return new ArrayList<>( this.occupants.subSet( Individual.INFECTIVE,
//					true, Individual.REMOVED, false ) );
//		}
//
//		public List<Individual> getRemoveds()
//		{
//			return new ArrayList<>(
//					this.occupants.tailSet( Individual.REMOVED, true ) );
//		}
//	}

	public Map<Disease, CarrierInfectivenessComparator<?>> infectives;

//	public Map<InfectiveState, Integer> infected = new EnumMap<>(
//			InfectiveState.SimpleSIER.class );

	/**
	 * helper
	 * 
	 * @return this {@link Location}
	 */
	public Location setId( final String value )
	{
		return setId( new ID().setValue( value ) );
	}

	/**
	 * @return this {@link Location}
	 */
	public Location setId( final ID id )
	{
		this.id = id;
		return this;
	}

	public ID getId()
	{
		return this.id;
	}

}
