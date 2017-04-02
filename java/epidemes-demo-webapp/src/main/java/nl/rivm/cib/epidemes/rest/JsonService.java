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
package nl.rivm.cib.epidemes.rest;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;

/**
 * {@link JsonService} see e.g. http://cxf.apache.org/docs/jax-rs.html
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Path( "/json/" )
//@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
public class JsonService
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( JsonService.class );

	/**
	 * https://jsfiddle.net/gh/get/library/pure/highslide-software/highcharts.com/tree/master/samples/mapdata/countries/nl/nl-all
	 */
	private static final String[] NL_PROVINCE_KEYS = { "nl-fr", "nl-gr",
			"nl-fl", "nl-ze", "nl-nh", "nl-zh", "nl-dr", "nl-ge", "nl-li",
			"nl-ov", "nl-nb", "nl-ut" };

	public static class HighChartEntry
	{
		@JsonProperty( "hc-key" )
		public String key;

		@JsonProperty( "value" )
		public Number value;

		public HighChartEntry( final String key, final Number value )
		{
			this.key = key;
			this.value = value;
		}
	}

	public JsonService()
	{
		LOG.info( "Created {}", getClass().getSimpleName() );
	}

	@GET
	@Produces( MediaType.APPLICATION_JSON )
	public String getProvinceValues()
	{
		final Random rnd = new Random();
		return JsonUtil.toJSON( Arrays.stream( NL_PROVINCE_KEYS )
				.map( k -> new HighChartEntry( k, rnd.nextInt( 12 ) ) )
				.collect( Collectors.toList() ) );
	}

//	@GET
//	@Path( "/{id}" )
//	@Produces( MediaType.APPLICATION_JSON )
//	public Customer getCustomer( @PathParam( "id" ) String id )
//	{
//		return new Customer( id );
//	}

}
