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
 * {@link MedicalStage} is an identifier of treatment stages/regimes
 * 
 * @version $Id: 7c4f8baff3c6ddc96bb7fadbd6202abb0132f0e9 $
 * @author Rick van Krevelen
 */
public class MedicalStage extends Id.Ordinal<String>
{
	/** currently not receiving any treatment */
	public static final MedicalStage UNTREATED = of( "n/a" );

	// DIAGNOSIS

	// PROGNOSIS

	/** currently immunizing using vaccine/antiserum */
	public static final MedicalStage VACCINATION = of( "vax" );

	/** currently suppressing disease using PrEP regime */
	public static final MedicalStage PRE_EXPOSURE_PROPHYLACTIC = of( "prep" );

	/** currently suppressing disease using PEP regime */
	public static final MedicalStage POST_EXPOSURE_PROPHYLACTIC = of( "pep" );

	public static MedicalStage of( final String value )
	{
		return Util.valueOf( value, new MedicalStage() );
	}

	public interface Attributable<THIS>
	{
		MedicalStage getStage();

		void setStage( MedicalStage stage );

		@SuppressWarnings( "unchecked" )
		default THIS with( final MedicalStage stage )
		{
			setStage( stage );
			return (THIS) this;
		}
	}

}