/* $Id: a8bf68b6634dba22075c105a65ea77fc0d8ccf05 $
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

import java.util.Map;

import io.coala.name.Id;
import io.coala.name.Identified;

/**
 * {@link Afflicted} can be a carrier of multiple (infectious, chronic, ...)
 * {@link Disease}s, each with their own {@link Condition} dynamics
 * 
 * @version $Id: a8bf68b6634dba22075c105a65ea77fc0d8ccf05 $
 * @author Rick van Krevelen
 */
public interface Afflicted extends Identified<Afflicted.ID>
{
	@SuppressWarnings( "rawtypes" )
	class ID extends Id.Ordinal
	{
		@SuppressWarnings( "unchecked" )
		public static <T extends Comparable<? super T>> ID of( final T value )
		{
			return Util.of( value, new ID() );
		}
	}

	/**
	 * @return a {@link Map} of this {@link Afflicted}s {@link Condition}s per
	 *         {@link Disease.ID}
	 */
	Map<Disease.ID, Condition> afflictions();

	default Condition condition( Disease.ID diseaseRef )
	{
		return afflictions().get( diseaseRef );
	}

	default Afflicted with( final Condition condition )
	{
		afflictions().put( condition.diseaseRef(), condition );
		return this;
	}

}