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
package nl.rivm.cib.episim.model;

import io.coala.json.x.Wrapper;

/**
 * {@link Relation} is an extensible identifier for relation types
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Relation extends Wrapper<String>
{
	/** */
	Relation PARENT = Util.valueOf( "parent", Relation.class );

	/** */
	Relation CHILD = Util.valueOf( "child", Relation.class );

	/** */
	Relation COLLEAGUE = Util.valueOf( "colleague", Relation.class );

	/** */
	Relation CLASSMATE = Util.valueOf( "classmate", Relation.class );

	/** */
	Relation PARTNER = Util.valueOf( "partner", Relation.class );

	/** */
	Relation FAMILY = Util.valueOf( "family", Relation.class );

	/** */
	Relation FRIEND = Util.valueOf( "friend", Relation.class );
}