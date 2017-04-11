/* $Id: 7c4f8baff3c6ddc96bb7fadbd6202abb0132f0e9 $
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

import io.coala.name.Id;

/**
 * {@link SerologicState} identifies the condition of the immune system's serum
 * 
 * @version $Id: 7c4f8baff3c6ddc96bb7fadbd6202abb0132f0e9 $
 * @author Rick van Krevelen
 */
public class SerologicState extends Id.Ordinal<String>
{
	/**
	 * immune system serum testing positive (titre > ?) for antibodies against
	 * some antigen
	 */
	public static final SerologicState SEROPOSITIVE = of( "sero+" );

	/**
	 * immune system serum testing negative (titre < ?) for antibodies against
	 * some antigen
	 */
	public static final SerologicState SERONEGATIVE = of( "sero-" );

	public static SerologicState of( final String value )
	{
		return Util.valueOf( value, new SerologicState() );
	}

	public interface Attributable<THIS>
	{
		SerologicState getState();

		void setState( SerologicState stage );

		@SuppressWarnings( "unchecked" )
		default THIS with( final SerologicState stage )
		{
			setState( stage );
			return (THIS) this;
		}
	}

}