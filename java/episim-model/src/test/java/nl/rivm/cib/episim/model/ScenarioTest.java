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

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.log.LogUtil;

/**
 * {@link ScenarioTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ScenarioTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( ScenarioTest.class );

	/**
	 * This test should:
	 * <ol>
	 * <li>init (contact, transport, care) location networks</li>
	 * <li>init (age, sex, opinion, birth/death) population networks</li>
	 * <li>init (travel, contact, risk) behaviors</li>
	 * <li>init disease conditions/compartments</li>
	 * <li>run opinion-immunization\\contact-transmission events</li>
	 * </ol>
	 * 
	 * @throws Throwable
	 */
	@Test
	public void scenarioTest() throws Throwable
	{
		LOG.trace( "Starting scenario..." );

	}

}