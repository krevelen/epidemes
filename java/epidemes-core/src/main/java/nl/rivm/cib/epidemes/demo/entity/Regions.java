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

@SuppressWarnings( "serial" )
public interface Regions
{
	class RegionName extends AtomicReference<String> implements Property<String>
	{

	}

	class ParentRef extends AtomicReference<Object> implements Property<Object>
	{
	}

	class Population extends AtomicReference<Long> implements Property<Long>
	{
	}

	@SuppressWarnings( "rawtypes" )
	List<Class<? extends Property>> PROPERTIES = Arrays.asList(
			Regions.RegionName.class, Regions.ParentRef.class,
			Regions.Population.class );

	class RegionTuple extends Tuple
	{
		@Override
		@SuppressWarnings( "rawtypes" )
		public List<Class<? extends Property>> properties()
		{
			return PROPERTIES;
		}
	}
}