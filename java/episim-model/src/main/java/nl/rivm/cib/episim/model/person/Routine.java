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
package nl.rivm.cib.episim.model.person;

import io.coala.json.Attributed;
import io.coala.name.Id;

/**
 * {@link Routine} categorizes the type of plans, inspired by Zhang:2016:phd
 */
public class Routine extends Id.Ordinal<String>
{
	/** day-care/school-child, working class hero, retiree */
	public static final Routine STANDARD = of(
			"STANDARD" );

	// for special-care, adjusted workplaces, elderly homes, ...
	// TODO public static final Routine SPECIAL = of( "SPECIAL" );

	public static Routine of( final String value )
	{
		return Util.of( value, new Routine() );
	}

	public interface Attributable<THIS> extends Attributed
	{
		void setRoutine( Routine routine );

		Routine getRoutine();

		@SuppressWarnings( "unchecked" )
		default THIS with( final Routine routine )
		{
			setRoutine( routine );
			return (THIS) this;
		};
	}
}