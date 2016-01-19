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
package nl.rivm.epi.vacsim.connect;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.log.LogUtil;

/**
 * {@link CBSConnectorTest}
 * 
 * @version $Id$
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public class CBSConnectorTest {

	/** */
	private static final Logger LOG = LogUtil.getLogger(CBSConnectorTest.class);

	@Test
	public void testCBSOpenDataPortal() throws ClientProtocolException, IOException {

		final String testURI = "http://opendata.cbs.nl/ODataApi/odata/81435ned";
		final JsonNode result = CBSConnector.getJSON(testURI);
		Assert.assertNotNull("Got null response for " + testURI, result);
		LOG.trace("Got result: " + result);
	}
}
