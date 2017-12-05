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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.util.FileUtil;
import nl.rivm.cib.epidemes.cbs.json.CbsRegionHierarchy;

/**
 * {@link CbsRegionCentroidDensity} basic address data:
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
public class CbsRegionCentroidDensity
{

	// pc6;huisnummer;gwb2016_code;wijkcode;gemeentecode
	enum Col
	{
		pc6, huisnummer, gwb2016_code, wijkcode, gemeentecode;
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( CbsRegionCentroidDensity.class );

	private static final String FILE_NAME = "pc6hnr20160801_gwb.csv";

	public static void main( final String[] args ) throws Exception
	{
		// landsdeel/nuts1 -> prov/nuts2 (12) -> corop/nuts3 (25) -> corop_sub -> corop_plus -> gm (400)
		// ggd (25x)
		// jeugdzorg (42x)
		// ressort (4) -> district (11) -> safety (25) (!= police reg eenh)
		// politie reg eenh (10x) -> districten (43x)
		// agri_group -> agr

		final ObjectNode gmBoroZipPos = JsonUtil.getJOM().createObjectNode();
//		LOG.info( "Got: {}", JsonUtil.toJSON( ldPvCrCsCpGm ) );

		final String pc6GeoFile = "dist/bag_pc6_2016_01.json";
		LOG.info( "Reading geographic positions of zipcodes from {}",
				pc6GeoFile );
		final TreeMap<String, BagZipcode6Locations> pc6stat = JsonUtil
				.readArrayAsync( () -> FileUtil.toInputStream( pc6GeoFile ),
						BagZipcode6Locations.class )
//				.take( 100 )
				.collectInto( new TreeMap<String, BagZipcode6Locations>(),
						( m, v ) -> m.put( v.zip, v ) )
				.blockingGet();

		// read CSV columns into tree: gm -> ward/boro -> zip4 -> zip6 -> count
		final List<String> missing = new ArrayList<>();
		try( final Stream<String> stream = Files.lines(
				Paths.get( "dist", FILE_NAME ),
				Charset.forName( "ISO-8859-1" ) ) )
		{
			stream.skip( 1 ).map( line -> line.split( ";" ) ).forEach( v ->
			{
				final BagZipcode6Locations stat = pc6stat.get( v[0] );
				if( stat == null )
				{
					missing.add( v[0] );
					return;
				}
				if( stat.hr == 0 && stat.hh == 0 ) return;

				final String gwb = v[Col.gwb2016_code.ordinal()],
						gmc = v[Col.gemeentecode.ordinal()], gmc4 = String
								.format( "GM%04d", Integer.valueOf( gmc ) );
				final ArrayNode gm = gmBoroZipPos.with( gmc4 )
//								.put( "woonplaats", stat.city ).with( "buurt" )
						.with( gwb.substring( gwb.length() - 4 ) )
						.with( v[Col.pc6.ordinal()].substring( 0, 4 ) )
//								.with( stat.street )
						.withArray( v[Col.pc6.ordinal()].substring( 4 ) );
				if( gm.size() == 0 )
					gm.add( stat.geo.coords[1] ).add( stat.geo.coords[0] )
							.add( stat.hh ).add( stat.hr );
			} );
		}
		
		// put in administrative hierarchy...
		final String gmRegionFile = "dist/83287NED.json";
		final String outFileName = "dist/region-centroid-density.json";
		try( final InputStream is = FileUtil.toInputStream( gmRegionFile );
				final OutputStream os = FileUtil.toOutputStream( outFileName,
						false ); )
		{
			final CbsRegionHierarchy gmRegs = JsonUtil.getJOM().readValue( is,
					CbsRegionHierarchy.class );

			final ObjectNode ldPvCrCsCpGm = gmRegs
					.addAdminHierarchy( gmBoroZipPos );
			JsonUtil.getJOM().writer().withDefaultPrettyPrinter()
					.writeValue( os, ldPvCrCsCpGm );
		}
		LOG.warn( "Missing stats for PC6 addresses: {}", missing );
	}
}
