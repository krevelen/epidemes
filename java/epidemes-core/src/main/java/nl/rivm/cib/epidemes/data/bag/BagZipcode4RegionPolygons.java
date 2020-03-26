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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.util.FileUtil;

/**
 * {@link BagZipcode4RegionPolygons} basic address data:
 * 
 * <pre>
 * {
"pc4":1023,
"vr_code":"VR13",
"gg_code":"GG3406",
"shape":{"type":"Polygon",
"coordinates":[[[4.968445637092872, 52.38812065054079], ... [..., ...]]]},
"ggd":"GGD Amsterdam",
"pc4_naam":"Nieuwendammerdijk/Buiksloterdijk",
"provincie":"Noord-Holland",
"veiligheidsregio":"Amsterdam-Amstelland",
"woonplaats_2":"AMSTERDAM",
"gem_nr":363,
"bev_2015":4610,
"bev_2016":4630,
"pv_code":"PV27",
"woonplaats_nen":"AMSTERDAM",
"gemeente":"Amsterdam",
"objectid":12
}
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class BagZipcode4RegionPolygons
{

	@JsonProperty("pc4")
	public int zip;

	@JsonProperty("gem_nr")
	public int gm;

	public Map<String, Object> values = new HashMap<>();

	@JsonAnySetter
	public void set( final String key, final Object value )
	{
		this.values.put( key, value );
	}

	@JsonAnyGetter
	public Object get( final String key )
	{
		return this.values.get( key );
	}

	@Override
	public String toString()
	{
		return JsonUtil.stringify( this );
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( BagZipcode4RegionPolygons.class );

	private static final String FILE_NAME = "dist/adm_pc4_2016_basis.json";

	public static void main( final String[] args ) throws Exception
	{
//		JsonUtil.getJOM().getDeserializationContext().cr
		LOG.info( "Row: {}",
				JsonUtil.readArrayAsync(
						() -> FileUtil.toInputStream( FILE_NAME ),
						BagZipcode4RegionPolygons.class ).blockingFirst() );
	}
}
