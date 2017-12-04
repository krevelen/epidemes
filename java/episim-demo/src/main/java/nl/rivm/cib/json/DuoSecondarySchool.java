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
package nl.rivm.cib.json;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.util.FileUtil;

/**
 * {@link DuoSecondarySchool}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class DuoSecondarySchool
{

	@JsonProperty( "jaar_1" )
	public int y1;
	@JsonProperty( "jaar_2" )
	public int y2;
	@JsonProperty( "jaar_3" )
	public int y3;
	@JsonProperty( "jaar_4" )
	public int y4;
	@JsonProperty( "jaar_5" )
	public int y5;
	@JsonProperty( "jaar_6" )
	public int y6;
	@JsonProperty( "leerlingen" )
	public int n;

	@JsonProperty( "gem_code" )
	public int municipality;
	@JsonProperty( "pc6" )
	public String pc6;
	@JsonProperty( "denominatie" )
	public String denom;
	@JsonProperty( "brin_nummer" )
	public String brin;
	@JsonProperty( "vestigingsnummer" )
	public String dep;
	@JsonProperty( "shape" )
	public GeoJson geo;

	@Override
	public String toString()
	{
		return JsonUtil.stringify( this );
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( DuoSecondarySchool.class );

	private static final String FILE_NAME = "dist/voorz_duo_vo_vestigingen_20130103.json";

	public static void main( final String[] args ) throws Exception
	{
		JsonUtil.readArrayAsync( () -> FileUtil.toInputStream( FILE_NAME ),
				DuoSecondarySchool.class )
				.subscribe( row -> LOG.info( "Row: {}", row ) );
	}
}
