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
package nl.rivm.cib.episim.cbs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link CBSConnector}
 * 
 * @version $Id$
 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
 *
 */
public class CBSConnector {

	static final ObjectMapper om = new ObjectMapper();

	static final int connectTimeoutMS = 1000;

	static final int socketTimeoutMS = 1000;

	public static JsonNode getJSON(final String url) throws ClientProtocolException, IOException {
		return getJSON(Request.Get(url));
	}

	public static JsonNode getJSON(final URI url) throws ClientProtocolException, IOException {
		return getJSON(Request.Get(url));
	}

	public static JsonNode getJSON(final Request request) throws ClientProtocolException, IOException {

		try (final InputStream stream = request.connectTimeout(connectTimeoutMS).socketTimeout(socketTimeoutMS)
				.execute().returnContent().asStream()) {
			return om.readTree(stream);
		}
	}
	
	// TODO try using Apache Olingo (https://olingo.apache.org/doc/odata2/tutorials/OlingoV2BasicClientSample.html)

}
