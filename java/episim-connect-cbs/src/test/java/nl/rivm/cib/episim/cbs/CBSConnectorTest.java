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
import java.net.URI;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;

/**
 * {@link CBSConnectorTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class CBSConnectorTest
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( CBSConnectorTest.class );

	public static class CBSMetaURL
	{
		private String name;
		private URI url;

		@Override
		public String toString()
		{
			return this.name + ":" + this.url;
		}
	}

	public static class CBSMetadata
	{
		@JsonProperty( "odata.metadata" )
		private URI metadata;
		private List<CBSMetaURL> value;

		@Override
		public String toString()
		{
			return this.metadata + ":" + this.value;
		}
	}

	@Test
	public void testCBSOpenDataPortal()
		throws ClientProtocolException, IOException
	{
		final String testURI = "http://opendata.cbs.nl/ODataApi/odata/70072NED";//"http://opendata.cbs.nl/ODataApi/odata/81435ned";
		final JsonNode result = CBSConnector.getJSON( testURI );
		Assert.assertNotNull( "Got null response for " + testURI, result );
//		final CBSMetadata data = JsonUtil.valueOf(result, CBSMetadata.class);
		LOG.trace( "Got result: " + JsonUtil.toJSON( result ) );
	}

	@Test
	public void testOlingo() throws IOException
	{
		final String serviceUrl = "http://opendata.cbs.nl/ODataApi/odata/37422ned";//"http://opendata.cbs.nl/ODataApi/odata/81435ned";
		final Edm edm = ODataUtil.readEdm( serviceUrl );
		edm.getSchemas().forEach( ( EdmSchema s ) ->
		{
			s.getEntityTypes().forEach( ( EdmEntityType t ) ->
			{

				LOG.trace( "{} :: {}", t.getFullQualifiedName(),
						t.getPropertyNames()
				/*
				 * ,ODataUtil .readEntities( edm, serviceUrl, t.getName() )
				 */ );
			} );
		} );

	}
}
