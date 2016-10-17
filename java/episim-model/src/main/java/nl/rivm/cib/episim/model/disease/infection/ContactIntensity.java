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
package nl.rivm.cib.episim.model.disease.infection;

import javax.measure.quantity.Dimensionless;

import org.jscience.physics.amount.Amount;

import io.coala.json.Wrapper;
import io.coala.json.DynaBean.BeanProxy;

/**
 * {@link ContactIntensity} is an extensible identifier for relation types
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@BeanProxy
public interface ContactIntensity extends Wrapper<String>
{

	default public Amount<Dimensionless> getFactor()
	{
		return Amount.ONE;
	}

	/** */
	ContactIntensity PARENT = Util.valueOf( "parent", ContactIntensity.class );

	/** */
	ContactIntensity CHILD = Util.valueOf( "child", ContactIntensity.class );

	/** */
	ContactIntensity COLLEAGUE = Util.valueOf( "colleague",
			ContactIntensity.class );

	/** */
	ContactIntensity CLASSMATE = Util.valueOf( "classmate",
			ContactIntensity.class );

	/** */
	ContactIntensity PARTNER = Util.valueOf( "partner",
			ContactIntensity.class );

	/** */
	ContactIntensity FAMILY = Util.valueOf( "family", ContactIntensity.class );

	/** */
	ContactIntensity FRIEND = Util.valueOf( "friend", ContactIntensity.class );
}