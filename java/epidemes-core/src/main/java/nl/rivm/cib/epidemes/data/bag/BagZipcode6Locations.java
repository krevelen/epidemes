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
package nl.rivm.cib.epidemes.data.bag;

import java.util.TreeMap;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.util.FileUtil;

/**
 * {@link BagZipcode6Locations} basic address data:
 * 
 * <pre>
 * {wptot=110, 
 * bagcnt=47, 
 * wpfull=96, 
 * openbareruimtenaam=Spuistraat, 
 * postcode=1012SV, 
 * avgy=487549, 
 * bedrcnt=22, 
 * avgx=121206, 
 * gmpwont=0, 
 * woonplaatsnaam=Amsterdam, 
 * pc6pers=42, 
 * meandist=78, 
 * geometrie={type=Point, coordinates=[4.890940341920423, 52.374772925483896]}, 
 * objectid=156, 
 * pc6won=40}
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class BagZipcode6Locations
{

	@JsonProperty( "postcode" )
	public String zip;

	@JsonProperty( "woonplaatsnaam" )
	public String city;

	@JsonProperty( "openbareruimtenaam" )
	public String street;

	@JsonProperty( "pc6won" )
	public int hh;

	@JsonProperty( "pc6pers" )
	public int ppl;

	@JsonProperty( "bedrcnt" )
	public int org;

	@JsonProperty( "wpfull" )
	public int hr;

	@JsonProperty( "geometrie" )
	public GeoJson geo;

//	public Map<String, Object> values = new HashMap<>();
//
//	@JsonAnySetter
//	public void set( final String key, final Object value )
//	{
//		this.values.put( key, value );
//	}
//
//	@JsonAnyGetter
//	public Object get( final String key )
//	{
//		return this.values.get( key );
//	}

	@Override
	public String toString()
	{
		return JsonUtil.stringify( this );
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( BagZipcode6Locations.class );

	private static final String FILE_NAME = "dist/bag_pc6_2016_01.json";

	public static void main( final String[] args ) throws Exception
	{
		final TreeMap<String, BagZipcode6Locations> result = JsonUtil
				.readArrayAsync( () -> FileUtil.toInputStream( FILE_NAME ),
						BagZipcode6Locations.class )
				.take( 100 )
				.collectInto( new TreeMap<String, BagZipcode6Locations>(),
						( m, v ) -> m.put( v.zip, v ) )
				.blockingGet();
		LOG.info( "Mapping: {}", JsonUtil.toJSON( result ) );
	}
}
