/* $Id: 29f8beca0d938bde40a0178d73456e1827c03856 $
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
package nl.rivm.cib.episim.model;

import io.coala.json.Wrapper;
import io.coala.json.DynaBean.BeanProxy;

/**
 * {@link Gender} is an extensible identifier for gender classifications
 * 
 * @version $Id: 29f8beca0d938bde40a0178d73456e1827c03856 $
 * @author Rick van Krevelen
 */
@BeanProxy
public interface Gender extends Wrapper<String>
{
	/** the MALE {@link Gender} */
	Gender MALE = Util.valueOf( "male", Gender.class );

	/** the FEMALE {@link Gender} */
	Gender FEMALE = Util.valueOf( "female", Gender.class );

	/** the TRANSSEXUAL_MALE {@link Gender}, previously {@link #FEMALE} */
	Gender TRANSSEXUAL_MALE = Util.valueOf( "trans-male", Gender.class );

	/** the TRANSSEXUAL_FEMALE {@link Gender}, previously {@link #MALE} */
	Gender TRANSSEXUAL_FEMALE = Util.valueOf( "trans-female", Gender.class );
}