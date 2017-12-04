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
package nl.rivm.cib.epidemes.cbs.json;

import java.util.EnumMap;
import java.util.List;
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
import io.coala.util.FileUtil;
import io.reactivex.Observable;

/**
 * {@link CbsRegionHierarchy} basic address data:
 * 
 * <pre>
 * {
"reg":["GM...", ...],
"Arbeidsmarktregio's":["AM..", ...],
"Arrondissementen (rechtsgebieden)":["AR..", ...],
"COROP-gebieden":["CR..", ...],
"COROP-subgebieden":["CS..", ...],
"COROP-plusgebieden":["CP..", ...],
"GGD-regio's":["GG..", ...],
"Jeugdzorgregios":["JZ..", ...],
"Kamer van Koophandel":["KK..", ...],
"Landbouwgebieden":["LB..", ...],
"Landbouwgebieden (groepen)":["LG..", ...],
"Landsdelen":["LD", ...],
"NUTS1-gebieden":["NL.", ...],
"NUTS2-gebieden":["NL..", ...],
"NUTS3-gebieden":["NL...", ...],
"Politie Regionale eenheden":["RE..", ...],
"Provincies":["PV..", ...],
"Ressorten (rechtsgebieden)":["RT", ...],
"RPA-gebieden":["RP..", ...],
"Toeristengebieden":["TR", ...],
"Veiligheidsregio's":["VR..", ...],
"Wgr-samenwerkingsgebieden":["WG..", ...],
"Zorgkantoorregio's":["ZK..", ...]
}
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class CbsRegionHierarchy
{

	@JsonProperty( "reg" )
	public String[] gm;

//	@JsonProperty( "Arbeidsmarktregio's" )
//	public String[] am;
//
//	@JsonProperty( "Arrondissementen (rechtsgebieden)" )
//	public String[] ar;
//
//	@JsonProperty( "COROP-gebieden" )
//	public String[] cr;
//
//	@JsonProperty( "COROP-subgebieden" )
//	public String[] cs;
//
//	@JsonProperty( "COROP-plusgebieden" )
//	public String[] cp;
//
//	@JsonProperty( "GGD-regio's" )
//	public String[] gg;
//
//	@JsonProperty( "Jeugdzorgregios" )
//	public String[] jz;
//
//	@JsonProperty( "Kamer van Koophandel" )
//	public String[] kk;
//
//	@JsonProperty( "Landbouwgebieden" )
//	public String[] lb;
//
//	@JsonProperty( "Landbouwgebieden (groepen)" )
//	public String[] lg;
//
//	@JsonProperty( "Landsdelen" )
//	public String[] ld;
//
//	@JsonProperty( "NUTS1-gebieden" )
//	public String[] nl1;
//
//	@JsonProperty( "NUTS2-gebieden" )
//	public String[] nl2;
//
//	@JsonProperty( "NUTS3-gebieden" )
//	public String[] nl3;
//
//	@JsonProperty( "Politie Regionale eenheden" )
//	public String[] re;
//
//	@JsonProperty( "Provincies" )
//	public String[] pv;
//
//	@JsonProperty( "Ressorten (rechtsgebieden)" )
//	public String[] rt;
//
//	@JsonProperty( "RPA-gebieden" )
//	public String[] rp;
//
//	@JsonProperty( "Toeristengebieden" )
//	public String[] tr;
//
//	@JsonProperty( "Veiligheidsregio's" )
//	public String[] vr;
//
//	@JsonProperty( "Wgr-samenwerkingsgebieden" )
//	public String[] wg;
//
//	@JsonProperty( "Zorgkantoorregio's" )
//	public String[] zk;

	public Map<String, Object> values = new TreeMap<>();

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
			.getLogger( CbsRegionHierarchy.class );

	private static final String FILE_NAME = "dist/83287NED.json";

	public static void main( final String[] args ) throws Exception
	{
		final CbsRegionHierarchy hier = JsonUtil.getJOM().readValue(
				FileUtil.toInputStream( FILE_NAME ), CbsRegionHierarchy.class );

		LOG.info( "Got: {}", JsonUtil.toJSON( hier.cityRegionsByType() ) );
	}

	@SuppressWarnings( "unchecked" )
	public TreeMap<String, EnumMap<CBSRegionType, String>> cityRegionsByType()
	{
		return Observable.range( 0, this.gm.length ).collectInto(
				new TreeMap<String, EnumMap<CBSRegionType, String>>(),
				( m, i ) -> m.put( this.gm[i],
						this.values.values().stream()
								.map( v -> ((List<String>) v).get( i ) )
								.filter( v -> v != null && !v.isEmpty() )
								.collect( Collectors.toMap(
										v -> CBSRegionType.parse( v ), v -> v,
										( v1, v2 ) -> v1,
										() -> new EnumMap<>(
												CBSRegionType.class ) ) ) ) )
				.blockingGet();
	}
}
