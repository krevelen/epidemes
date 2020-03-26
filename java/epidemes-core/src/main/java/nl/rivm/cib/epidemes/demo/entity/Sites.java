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
package nl.rivm.cib.epidemes.demo.entity;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import nl.rivm.cib.epidemes.data.duo.DuoPedagogy;

@SuppressWarnings( "serial" )
public interface Sites
{

	enum BuiltFunction
	{
		RESIDENCE, LARGE_ENTERPRISE, SMALL_ENTERPRISE, PRIMARY_EDUCATION,;
	}

	class SiteName extends AtomicReference<String> implements Property<String>
	{

	}

	class RegionRef extends AtomicReference<Object> implements Property<Object>
	{
	}

	class Latitude extends AtomicReference<Double> implements Property<Double>
	{
	}

	class Longitude extends AtomicReference<Double> implements Property<Double>
	{
	}

	class SiteFunction extends AtomicReference<Sites.BuiltFunction>
		implements Property<Sites.BuiltFunction>
	{
	}

	class EduCulture extends AtomicReference<DuoPedagogy>
		implements Property<DuoPedagogy>
	{
	}

	class Capacity extends AtomicReference<Integer> implements Property<Integer>
	{
	}

	class Occupancy extends AtomicReference<Integer>
		implements Property<Integer>
	{
	}

	class Pressure extends AtomicReference<Double> implements Property<Double>
	{
	}

	@SuppressWarnings( "rawtypes" )
	List<Class<? extends Property>> PROPERTIES = Arrays.asList(
			Sites.Pressure.class, Sites.Occupancy.class, Sites.SiteName.class,
			Sites.SiteFunction.class, Sites.EduCulture.class,
			Sites.RegionRef.class, Sites.Latitude.class, Sites.Longitude.class,
			Sites.Capacity.class );

	class SiteTuple extends Tuple
	{
		@Override
		@SuppressWarnings( "rawtypes" )
		public List<Class<? extends Property>> properties()
		{
			return PROPERTIES;
		}

	}
}