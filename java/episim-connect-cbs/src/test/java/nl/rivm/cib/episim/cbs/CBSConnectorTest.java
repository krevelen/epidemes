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
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;
import org.apache.olingo.commons.api.edm.Edm;
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

//	@Ignore
	@Test
	public void testOlingo() throws IOException
	{
		final String serviceUrl = "http://opendata.cbs.nl/ODataApi/odata/83225ned";//"http://opendata.cbs.nl/ODataApi/odata/81435ned";
		final Edm edm = ODataUtil.readEdm( serviceUrl );
		edm.getSchemas().forEach( s ->
		{
			s.getEntityTypes().forEach( t ->
			{
				t.getPropertyNames().forEach( p ->
				{
					if(p.equals( "Key" ))
						System.err.println(ODataUtil.readEntities( edm, serviceUrl, t.getName()+"$select=" ));
					LOG.trace( "{}.{} :: {} ({})", t.getNamespace(),
							t.getName(), p, t.getProperty( p ).getType());
				} );
			} );
//			final Map<Object, Object> dims = s.getEntityTypes().stream().filter( e->e.getPropertyNames().contains( "Key" ) )
//					.collect( Collectors.toMap(
//							e -> e.getProperty( "Key" ),
//							e -> e.getProperty( "Title" ) ) );
//			LOG.trace( "{} dims: {}", s.getNamespace(), dims );
			
			final String dim = "Geslacht";
			final Map<Object, Object> keys = StreamSupport
					.stream( Spliterators.spliteratorUnknownSize(
							ODataUtil.readEntities( edm, serviceUrl, dim ),
							Spliterator.CONCURRENT ), false )
					.collect( Collectors.toMap(
							e -> e.getProperty( "Key" ).getPrimitiveValue()
									.toValue(),
							e -> e.getProperty( "Title" ).getPrimitiveValue()
									.toValue() ) );
			LOG.trace( "{} keys: {}", dim, keys );
		} );

	}
}
