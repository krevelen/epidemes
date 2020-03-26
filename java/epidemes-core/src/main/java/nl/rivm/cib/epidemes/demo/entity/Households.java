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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import nl.rivm.cib.epidemes.data.cbs.CBSBirthRank;
import nl.rivm.cib.epidemes.data.cbs.CBSHousehold;
import nl.rivm.cib.epidemes.data.duo.DuoPedagogy;

@SuppressWarnings( "serial" )
public
interface Households
{
	BigDecimal NO_MOM = BigDecimal.TEN.pow( 6 );

	class HouseholdSeq extends AtomicReference<Long>
		implements Property<Long>
	{
		// track households through time (index key is data source dependent)
	}

	class EduCulture extends AtomicReference<DuoPedagogy>
		implements Property<DuoPedagogy>
	{
	}

	class Complacency extends AtomicReference<BigDecimal>
		implements Property<BigDecimal>
	{
	}

	class Confidence extends AtomicReference<BigDecimal>
		implements Property<BigDecimal>
	{
	}

	@SuppressWarnings( "rawtypes" )
	class HomeRegionRef extends AtomicReference<Comparable>
		implements Property<Comparable>
	{
	}

	@SuppressWarnings( "rawtypes" )
	class HomeSiteRef extends AtomicReference<Comparable>
		implements Property<Comparable>
	{
	}

	class Composition extends AtomicReference<CBSHousehold>
		implements Property<CBSHousehold>
	{
	}

	class KidRank extends AtomicReference<CBSBirthRank>
		implements Property<CBSBirthRank>
	{
	}

	class ReferentBirth extends AtomicReference<BigDecimal>
		implements Property<BigDecimal>
	{

	}

	class MomBirth extends AtomicReference<BigDecimal>
		implements Property<BigDecimal>
	{

	}

	@SuppressWarnings( "rawtypes" )
	List<Class<? extends Property>> PROPERTIES = Arrays.asList(
			Households.HomeRegionRef.class, Households.HomeSiteRef.class, Households.Composition.class,
			Households.KidRank.class, Households.ReferentBirth.class, Households.MomBirth.class,
			Households.EduCulture.class, Households.HouseholdSeq.class, Households.Complacency.class,
			Households.Confidence.class );

	class HouseholdTuple extends Tuple
	{
		@Override
		@SuppressWarnings( "rawtypes" )
		public List<Class<? extends Property>> properties()
		{
			return PROPERTIES;
		}
	}
}