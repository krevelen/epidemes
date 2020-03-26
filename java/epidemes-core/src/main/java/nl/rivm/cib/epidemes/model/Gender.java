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
package nl.rivm.cib.epidemes.model;

import io.coala.json.Attributed;
import io.coala.name.Identified;

/**
 * {@link Gender} is an extensible identifier for gender classifications
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
//@BeanProxy
public interface Gender extends Identified<String>
{
//	/** the MALE {@link Gender} */
//	Gender MALE = of( "male" );
//
//	/** the FEMALE {@link Gender} */
//	Gender FEMALE = of( "female" );
//
//	/** the TRANSSEXUAL_MALE {@link Gender}, previously {@link #FEMALE} */
//	Gender TRANSSEXUAL_MALE = of( "trans-male" );
//
//	/** the TRANSSEXUAL_FEMALE {@link Gender}, previously {@link #MALE} */
//	Gender TRANSSEXUAL_FEMALE = of( "trans-female" );

	Gender MALE = () -> "male";

	Gender FEMALE = () -> "male";

	default boolean isMale()
	{
		return id().equals( MALE.id() );
	}

	default boolean isFemale()
	{
		return !isMale();
	}

	interface Attributable<THIS> extends Attributed
	{
		Gender getGender();

		void setGender( Gender gender );

		@SuppressWarnings( "unchecked" )
		default THIS with( final Gender gender )
		{
			setGender( gender );
			return (THIS) this;
		}
	}

}