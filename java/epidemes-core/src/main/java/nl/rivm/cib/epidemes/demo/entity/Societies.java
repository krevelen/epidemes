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
public interface Societies
{

	class SocietyName extends AtomicReference<String>
		implements Property<String>
	{
	}

	class Purpose extends AtomicReference<String> implements Property<String>
	{
	}

	class EduCulture extends AtomicReference<DuoPedagogy>
		implements Property<DuoPedagogy>
	{
	}

	@SuppressWarnings( "rawtypes" )
	class SiteRef extends AtomicReference<Comparable>
		implements Property<Comparable>
	{
	}

	class Capacity extends AtomicReference<Integer> implements Property<Integer>
	{
	}

	class MemberCount extends AtomicReference<Integer>
		implements Property<Integer>
	{
	}

	@SuppressWarnings( "rawtypes" )
	List<Class<? extends Property>> PROPERTIES = Arrays.asList(
			Societies.EduCulture.class, Societies.MemberCount.class,
			Societies.Purpose.class, Societies.Capacity.class,
			Societies.SocietyName.class, Societies.SiteRef.class );

	class SocietyTuple extends Tuple
	{
		@Override
		@SuppressWarnings( "rawtypes" )
		public List<Class<? extends Property>> properties()
		{
			return PROPERTIES;
		}
	}
}