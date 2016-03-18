/* $Id: 02d50a7203fcffdd147257460aa6196283b4d936 $
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

import io.coala.json.x.JsonUtil;

/**
 * {@link CBSConnector}
 * 
 * @version $Id: 02d50a7203fcffdd147257460aa6196283b4d936 $
 * @author Rick van Krevelen
 */
public class CBSConnector
{

	static final int connectTimeoutMS = 1000;

	static final int socketTimeoutMS = 1000;

	public static JsonNode getJSON( final String url )
		throws ClientProtocolException, IOException
	{
		return getJSON( Request.Get( url ) );
	}

	public static JsonNode getJSON( final URI url )
		throws ClientProtocolException, IOException
	{
		return getJSON( Request.Get( url ) );
	}

	public static JsonNode getJSON( final Request request )
		throws ClientProtocolException, IOException
	{
		try( final InputStream stream = request
				.connectTimeout( connectTimeoutMS )
				.socketTimeout( socketTimeoutMS ).execute().returnContent()
				.asStream() )
		{
			return JsonUtil.getJOM().readTree( stream );
		}
	}

	public static void olingoTest()
	{

	}

}
