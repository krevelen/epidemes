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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.coala.util.FileUtil;

/**
 * {@link CbsWardBoroughPolygons} basic address data:
 * 
 * <pre>
 * {
"postcode":9901
"gem_nr":3
"gm_code":"GM0003"
"gm_naam":"Appingedam"
"wk_code":"WK000300"
"wk_naam":"Wijk 00"
"bu_code":"BU00030000"
"bu_naam":"Appingedam-Centrum"
"shape":{"type":"Polygon"
"coordinates":[ [ [6.867724344776397, 53.327068736084165] ... [.., 53.327068736084165]]]}
"indeling":"Buurt"

"opp_land":84
"opp_water":5
"opp_tot":90,
"auto_land":1205
"auto_tot":1015
"dek_perc":1
"bev_dichth":2739

"af_overst":25.2
"af_treinst":0.6
"af_ziek_i":3.7
"af_ziek_e":3.7
"af_biblio":0.4
"af_brandw":0.9
"af_kdv":1.3
"af_bso":1.1
"af_oprith":0.5
"af_ijsbaan":22.3

"aant_inw":2305
"aant_man":1075
"aant_vrouw":1230
"a_lftj6j":300
"a_lfto6j":720
"a_bst_nb":145
"a_bst_b":875
"auto_hh":0.8
"bedr_auto":80

"aantal_hh":1275
"gem_hh_gr":1.8

"p_hh_z_k":29,
"p_hh_m_k":19
"p_gescheid":9
"p_ongehuwd":38
"p_verweduw":14
"p_gehuwd":39
"p_eenp_hh":52

"p_ant_aru":0
"p_west_al":6,
"p_surinam":0
"p_turkije":0
"p_marokko":0
"p_n_w_al":3
"p_over_nw":2

"p_00_14_jr":10
"p_15_24_jr":10
"p_25_44_jr":21
"p_45_64_jr":29
"p_65_eo_jr":30

"av1_bso":0.4
"av1_kdv":0.1
"av3_bso":2.5
"av3_kdv":1
"av5_bso":5
"av5_kdv":4
"av5_ziek_i":1
"av5_ziek_e":1
"av10ziek_i":1
"av10ziek_e":1,
"av20ziek_i":1
"av20ziek_e":1,

"oad":1139
"sted":3
"motor_2w":80
"objectid":1}
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class CbsWardBoroughPolygons
{

	@JsonProperty( "gm_code" )
	public String city;

	@JsonProperty( "wk_code" )
	public String ward;

	@JsonProperty( "bu_code" )
	public String boro;

	@JsonProperty( "postcode" )
	public String zip4;

	@JsonProperty( "aantal_hh" )
	public int hh;

//	@JsonProperty("p_eenp_hh")
//	public int hh_solo_perc;
//
//	@JsonProperty("p_hh_z_k")
//	public int hh_duo_nokids_perc;
//
//	@JsonProperty("p_hh_m_k")
//	public int hh_kids_perc;

//	@JsonProperty("p_ongehuwd")
//	public int hh_unmar_perc;
//
//	@JsonProperty("p_gehuwd")
//	public int hh_mar_perc;
//
//	@JsonProperty("p_verweduw")
//	public int hh_wid_perc;
//
//	@JsonProperty("p_gescheid")
//	public int hh_div_perc;

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
			.getLogger( CbsWardBoroughPolygons.class );

	private static final String FILE_NAME = "dist/adm_cbs_wijkbuurt_2013_v1.json";

	public static Map<String, ProbabilityDistribution<String>> parse(
		final InputStream is, final ProbabilityDistribution.Factory distFact )
		throws IOException
	{
		return JsonUtil.stream( JsonUtil.getJOM().readTree( is ) )
				.collect( Collectors.toMap(
						// municipal
						gmZipBo -> gmZipBo.getKey(),
						// zip distribution
						gmZipBo -> distFact.createCategorical( JsonUtil
								.stream( gmZipBo.getValue() ).map(
										zipBo -> WeightedValue.of(
												// pc4
												zipBo.getKey(),
												// sum weights from all boroughs
												JsonUtil.stream(
														zipBo.getValue() )
														.mapToInt(
																bo -> bo.getValue()
																		.asInt() )
														.sum() ) ) ) ) );
	}

	private static final String outFile = "dist/gm_pc4_boro_hh.json";

	public static void main( final String[] args ) throws Exception
	{
		final TreeMap<String, Map<String, Map<String, Integer>>> result = JsonUtil
				.readArrayAsync( () -> FileUtil.toInputStream( FILE_NAME ),
						CbsWardBoroughPolygons.class )
//				.take( 100 )
				.filter( v -> v.hh > 0 && v.boro != null )
				.collectInto(
						new TreeMap<String, Map<String, Map<String, Integer>>>(),
						( m, v ) -> m
								.computeIfAbsent( v.city, k -> new TreeMap<>() )
								.computeIfAbsent( v.zip4, k -> new TreeMap<>() )
								.put( v.boro, v.hh ) )
				.blockingGet();

		JsonUtil.getJOM().writerWithDefaultPrettyPrinter().writeValue(
				FileUtil.toOutputStream( outFile, false ), result );

		LOG.info( "Written to: {}", outFile );
	}
}
