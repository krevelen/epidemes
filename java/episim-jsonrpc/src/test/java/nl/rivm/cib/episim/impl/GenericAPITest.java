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
package nl.rivm.cib.episim.impl;

import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.apache.log4j.Logger;
import org.junit.Test;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import nl.rivm.cib.episim.api.GenericAPI;
import nl.rivm.cib.episim.api.GenericAPI.ID;

/**
 * {@link GenericAPITest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 *
 */
public class GenericAPITest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( GenericAPITest.class );

	/**
	 * {@link TestGenericAPI} implements {@link GenericAPI}
	 * 
	 * @version $Id$
	 * @author <a href="mailto:rick.van.krevelen@rivm.nl">Rick van Krevelen</a>
	 */
	@SuppressWarnings( "serial" )
	public static class TestGenericAPI extends AbstractGenericAPI //implements ConnectorAPI
	{

//		public TestGenericAPI()
//		{
//			
//		}
//		@Override
//		public JsonNode open( final JsonNode config )
//		{
//			return config;
//		}
//
//		@Override
//		public JsonNode read( final JsonNode query )
//		{
//			return query;
//		}
	}

	@Test
	public void testJSONRPC()
	{
		// TODO use mock version?
		LOG.trace( "Starting test" );
		final TestGenericAPI conn = JsonUtil.valueOf( "{\"class\":\"" + TestGenericAPI.class.getName()
				+ "\",\"id\":{\"class\":\"" + ID.class.getName() + "\",\"value\":\"my-id\"}}", TestGenericAPI.class );
		conn.setAddress( conn.getID(), URI.create( "testuri" ) );
		assertNotNull( "Parsed connector as null", conn );
		LOG.trace( "Parsed conn: " + conn );
		LOG.trace( "Completed test" );
	}
}
