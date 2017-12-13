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

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.exception.Thrower;
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

	private static final String FILE_BASE = "../episim-demo/dist/";

	private static final String FILE_NAME = "83287NED.json";

	public static void main( final String[] args ) throws IOException
	{

		if( System.getProperty(
				ConfigurationFactory.CONFIGURATION_FILE_PROPERTY ) == null )
			try( final InputStream is = FileUtil
					.toInputStream( FILE_BASE + "log4j2.yaml" ) )
			{
			// see https://stackoverflow.com/a/42524443
			final LoggerContext ctx = LoggerContext.getContext( false );
			ctx.start( new YamlConfiguration( ctx, new ConfigurationSource( is ) ) );
			}

		try( final InputStream is = FileUtil
				.toInputStream( FILE_BASE + FILE_NAME ) )
		{
			final CbsRegionHierarchy hier = JsonUtil.getJOM().readValue( is,
					CbsRegionHierarchy.class );
			LOG.info( "GM regions: {}", hier.cityRegionsByType() );

			LOG.info( "Hierarchy: {}",
					JsonUtil.toJSON( hier.addAdminHierarchy( null ) ) );
		}
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

	// landsdeel/nuts1 -> prov/nuts2 (12) -> corop/nuts3 (25) -> corop_sub -> corop_plus -> gm (400)
	// ggd (25x)
	// jeugdzorg (42x)
	// ressort (4) -> district (11) -> safety (25) (!= police reg eenh)
	// politie reg eenh (10x) -> districten (43x)
	// agri_group -> agr

	public ObjectNode addAdminHierarchy( final JsonNode gmMap )
	{
		final TreeMap<String, EnumMap<CBSRegionType, String>> gmRegs = cityRegionsByType();

		final ObjectNode result = JsonUtil.getJOM().createObjectNode();

		if( gmMap != null && gmMap.isObject() && gmMap.size() != 0 )
			// only place given keys in a hierarchy
			JsonUtil.forEach( (ObjectNode) gmMap,
					( k, v ) -> insertHierarchy( result,
							gmRegs.get( k.toUpperCase() ), k, v ) );
		else if( gmMap == null || gmMap.isNull()
				|| (gmMap.isContainerNode() && gmMap.size() == 0) )
			// create empty nodes for all known keys
			gmRegs.forEach( ( k, v ) -> insertHierarchy( result, v, k,
					JsonUtil.getJOM().createObjectNode() ) );
		else
			Thrower.throwNew( IllegalArgumentException::new,
					() -> "Illegal filter: " + gmMap );

		return result;
	}

	public static final String REG_TAGS_KEY = "reg_tags";

	private void insertHierarchy( final ObjectNode container,
		final EnumMap<CBSRegionType, String> v, final String gm,
		final JsonNode gmNode )
	{
		final ObjectMapper om = JsonUtil.getJOM();

		final ObjectNode ld = container
				.with( v.get( CBSRegionType.TERRITORY ) );
		ld.replace( REG_TAGS_KEY,
				om.createArrayNode().add( v.get( CBSRegionType.TERRITORY ) )
						.add( v.get( CBSRegionType.NUTS1 ) ) );

		final ObjectNode pv = ld.with( CBSRegionType.PROVINCE.getPrefix() )
				.with( v.get( CBSRegionType.PROVINCE ) );
		pv.replace( REG_TAGS_KEY,
				om.createArrayNode().add( v.get( CBSRegionType.TERRITORY ) )
						.add( v.get( CBSRegionType.NUTS1 ) )
						.add( v.get( CBSRegionType.PROVINCE ) )
						.add( v.get( CBSRegionType.NUTS2 ) ) );

		final ObjectNode cr = pv.with( CBSRegionType.COROP.getPrefix() )
				.with( v.get( CBSRegionType.COROP ) );
		cr.replace( REG_TAGS_KEY,
				om.createArrayNode().add( v.get( CBSRegionType.TERRITORY ) )
						.add( v.get( CBSRegionType.NUTS1 ) )
						.add( v.get( CBSRegionType.PROVINCE ) )
						.add( v.get( CBSRegionType.NUTS2 ) )
						.add( v.get( CBSRegionType.COROP ) )
						.add( v.get( CBSRegionType.NUTS3 ) ) );

		cr.with( CBSRegionType.MUNICIPAL.getPrefix() )
				.replace( gm,
						gmNode.isObject()
								? ((ObjectNode) gmNode).set( REG_TAGS_KEY,
										om.valueToTree( v.values() ) )
								: gmNode );
	}
}
