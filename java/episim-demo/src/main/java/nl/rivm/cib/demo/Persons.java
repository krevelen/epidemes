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
package nl.rivm.cib.demo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;

@SuppressWarnings( "serial" )
	public
	interface Persons
	{
		enum HouseholdPosition
		{
			REFERENT, PARTNER, CHILD1, CHILD2, CHILD3, CHILDMORE;

			public boolean isAdult()
			{
				return ordinal() < 2;
			}

			public static Persons.HouseholdPosition ofChildIndex( final int rank )
			{
				return rank < 3 ? values()[2 + rank] : CHILDMORE;
			}

			/**
			 * @param ppPos
			 * @return
			 */
			public Persons.HouseholdPosition shift( final Persons.HouseholdPosition missing )
			{
				return missing == REFERENT ? (this == PARTNER ? REFERENT : this)
						: (!isAdult() && ordinal() > missing.ordinal()
								? values()[ordinal() - 1] : this);
			}
		}

		/** person's sequence/serial number */
		class PersonSeq extends AtomicReference<Long> implements Property<Long>
		{
			// increases monotone at every new initiation/birth/immigration/...
		}

		/** reference of person's culture */
		@SuppressWarnings( "rawtypes" )
		class CultureRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		/** reference of person's household */
		class HouseholdRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		/** person's current household rank */
		class HouseholdRank extends AtomicReference<Persons.HouseholdPosition>
			implements Property<Persons.HouseholdPosition>
		{
		}

		/** person's gender */
		class Male extends AtomicReference<Boolean> implements Property<Boolean>
		{
		}

		/** virtual absolute time of person's birth */
		class Birth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

//		@SuppressWarnings( "rawtypes" )
//		class SiteRef extends AtomicReference<Comparable>
//			implements Property<Comparable>
//		{
//		}

		/** reference of person's current home region */
		@SuppressWarnings( "rawtypes" )
		class HomeRegionRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		/** reference of person's current home site */
		@SuppressWarnings( "rawtypes" )
		class HomeSiteRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		/** person's current epidemiological compartment for some pathogen */
		class PathogenCompartment extends AtomicReference<MSEIRS.Compartment>
			implements Property<MSEIRS.Compartment>
		{
		}

		/** person's current remaining 'resistance' against some pathogen */
		class PathogenResistance extends AtomicReference<Double>
			implements Property<Double>
		{
		}

		/**
		 * person's current vaccination compliance (bitwise, see
		 * https://stackoverflow.com/a/31921748)
		 */
		class VaxCompliance extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				// list frequently accessed fields first
				Persons.PathogenCompartment.class, Persons.HouseholdRef.class,
				Persons.HouseholdRank.class, Persons.Birth.class, Persons.HomeRegionRef.class,
				Persons.HomeSiteRef.class, Persons.PathogenResistance.class, Persons.Male.class,
				Persons.CultureRef.class, Persons.VaxCompliance.class, Persons.PersonSeq.class );

		class PersonTuple extends Tuple
		{
			@Override
			@SuppressWarnings( "rawtypes" )
			public List<Class<? extends Property>> properties()
			{
				return PROPERTIES;
			}
		}
	}