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
package nl.rivm.cib.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.WeightedValue;
import io.coala.random.ConditionalDistribution;
import io.coala.random.DistributionFactory;
import io.coala.random.ProbabilityDistribution;
import io.coala.util.FileUtil;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.CbsRegionHierarchy;
import nl.rivm.cib.json.BagZipcode4RegionPolygons;
import nl.rivm.cib.json.BagZipcode6Locations;

/**
 * {@link DuoPrimarySchool}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class DuoPrimarySchool
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( DuoPrimarySchool.class );

	public enum Col
	{
		PEILDATUM, GEMEENTENUMMER, GEMEENTENAAM, POSTCODE_LEERLING,

		GEMEENTENAAM_LEERLING, BRIN_NUMMER, VESTIGINGSNUMMER,

		INSTELLINGSNAAM_VESTIGING, POSTCODE_VESTIGING, PLAATSNAAM,

		PROVINCIE, SOORT_PO, DENOMINATIE_VESTIGING, BEVOEGD_GEZAG_NUMMER,

		L3_MIN, L4, L5, L6, L7, L8, L9, L10, L11, L12, L13, L14, L15, L16, L17,

		L18, L19, L20, L21, L22, L23, L24, L25_PLUS, TOTAAL

	}

	public static final String ZIPDIST_KEY = "zip_dist";

	public static final String SCHOOLS_KEY = "schools";

	public enum EduCol
	{
		GEMEENTE, PO_SOORT, DENOMINATIE, BRIN, VESTIGING, POSTCODE, LATITUDE, LONGITUDE
	};

	private static final String outFileName = "dist/region-primary-school-students.json";

	private static EnumMap<EduCol, JsonNode> toEnumMap( final JsonNode node )
	{
		return JsonUtil.stream( (ArrayNode) node ).collect( Collectors.toMap(
				e -> EduCol.values()[e.getKey()], e -> e.getValue(),
				( v1, v2 ) -> v2,
				() -> new EnumMap<EduCol, JsonNode>( EduCol.class ) ) );
	}

	public static <T> TreeMap<String, Map<T, ProbabilityDistribution<String>>>
		parse( final InputStream is,
			final ProbabilityDistribution.Factory distFact,
			final BiFunction<String, EnumMap<EduCol, JsonNode>, T> classifier )
			throws IOException
	{
		final JsonNode root = JsonUtil.getJOM().readTree( is );
		final ObjectNode schools = ((ObjectNode) root.with( SCHOOLS_KEY ));
		return JsonUtil.stream( root, ZIPDIST_KEY )
				.flatMap( ldPv -> JsonUtil.stream( ldPv.getValue(),
						CBSRegionType.PROVINCE.getPrefix() ) )
				.flatMap( pvCr -> JsonUtil.stream( pvCr.getValue(),
						CBSRegionType.COROP.getPrefix() ) )
				.flatMap( crGm -> JsonUtil.stream( crGm.getValue(),
						CBSRegionType.MUNICIPAL.getPrefix() ) )
				.flatMap( gmZips -> JsonUtil.stream( gmZips.getValue() ) )
				.filter( zipWvs -> !zipWvs.getKey()
						.equals( CbsRegionHierarchy.REG_TAGS_KEY ) )
				.collect( Collectors.toMap(
						// key1: zip
						Map.Entry::getKey,
						zipWvs -> JsonUtil.stream( zipWvs.getValue() )
								// reclassify
								.collect( Collectors.groupingBy(
										wv -> classifier.apply( wv.getKey(),
												toEnumMap( schools
														.get( wv.getKey() ) ) ) ) )
								.entrySet().stream()
								.collect( Collectors.toMap(
										// key2: cat
										Map.Entry::getKey,
										catWvs -> distFact.createCategorical(
												catWvs.getValue().stream()
														.map( wv -> WeightedValue
																.of( wv.getKey(),
																		wv.getValue()
																				.asInt() ) ) ),
										( v1, v2 ) -> v2, HashMap::new ) ),
						( v1, v2 ) -> v2, TreeMap::new ) );
	}

	public static void main( final String[] args ) throws Exception
	{
		final String pc4GemFile = "dist/adm_pc4_2016_basis.json";
		final TreeMap<String, String> pc4Gem = JsonUtil
				.readArrayAsync( () -> FileUtil.toInputStream( pc4GemFile ),
						BagZipcode4RegionPolygons.class )
				.collect( () -> new TreeMap<String, String>(),
						( m, v ) -> m.put( String.format( "%04d", v.zip ),
								CBSRegionType.MUNICIPAL.format( v.gm ) ) )
				.blockingGet();

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

		final ObjectNode gmCultAgeSchoolPos = JsonUtil.getJOM()
				.createObjectNode();

		final String fileName = "03.-leerlingen-po-totaaloverzicht-2015-2016.csv";
		final Set<String> missing = new HashSet<>();
		try( final Stream<String> stream = Files.lines(
				Paths.get( "dist", fileName ),
				Charset.forName( "ISO-8859-1" ) ) )
		{
			stream.skip( 1 ).map( line -> line.split( ";" ) ).forEach( v ->
			{
				final String pc4Gm = pc4Gem
						.get( v[Col.POSTCODE_LEERLING.ordinal()] );
				if( pc4Gm == null ) return; // ANDERS/BUITENLAND

				final BagZipcode6Locations stat = pc6stat
						.get( v[Col.POSTCODE_VESTIGING.ordinal()] );
				if( stat == null )
				{
					missing.add( v[Col.POSTCODE_VESTIGING.ordinal()] );
					return;
				}

				final String id = v[Col.BRIN_NUMMER.ordinal()] + "_"
						+ v[Col.VESTIGINGSNUMMER.ordinal()];

				gmCultAgeSchoolPos //
						.with( ZIPDIST_KEY ).with( pc4Gm )
						.with( v[Col.POSTCODE_LEERLING.ordinal()] )
						.put( id, Integer.valueOf( v[Col.TOTAAL.ordinal()] ) );

				gmCultAgeSchoolPos.with( SCHOOLS_KEY ).putArray( id )
						// following order of ExportCol enumeration
						.add( CBSRegionType.MUNICIPAL.format( Integer
								.valueOf( v[Col.GEMEENTENUMMER.ordinal()] ) ) )
						.add( v[Col.SOORT_PO.ordinal()] )
						.add( v[Col.DENOMINATIE_VESTIGING.ordinal()] )
						.add( v[Col.BRIN_NUMMER.ordinal()] )
						.add( v[Col.VESTIGINGSNUMMER.ordinal()] )
						.add( v[Col.POSTCODE_VESTIGING.ordinal()] )
						.add( stat.geo.coords[1] ).add( stat.geo.coords[0] )
				//
				;
			} );
		}

		// put in administrative hierarchy...
		final String gmRegionFile = "dist/83287NED.json";
		try( final InputStream is = FileUtil.toInputStream( gmRegionFile );
				final OutputStream os = FileUtil.toOutputStream( outFileName,
						false ); )
		{
			final CbsRegionHierarchy gmRegs = JsonUtil.getJOM().readValue( is,
					CbsRegionHierarchy.class );

			gmCultAgeSchoolPos.replace( ZIPDIST_KEY, gmRegs.addAdminHierarchy(
					gmCultAgeSchoolPos.with( ZIPDIST_KEY ) ) );
			JsonUtil.getJOM().writer().withDefaultPrettyPrinter()
					.writeValue( os, gmCultAgeSchoolPos );
		}
		LOG.warn( "Missing stats for PC6 addresses: {}", missing );
	}

	public static void test( final String[] args ) throws Exception
	{
		try( final InputStream is = FileUtil.toInputStream( outFileName ) )
		{
			LOG.debug( "Creating RNG..." );
			final DistributionFactory distFact = new DistributionFactory();

			LOG.debug( "Aggregating into dist..." );

			final String LUTHER = "po_luther", STEINER = "po_steiner",
					SPECIAL = "po_special", REST = "po_rest";
			final Map<String, EnumMap<EduCol, JsonNode>> schoolCache = new HashMap<>();
			final Map<String, Map<String, ProbabilityDistribution<String>>> zipCultDists = parse(
					is, distFact, ( id, arr ) ->
					{
						schoolCache.computeIfAbsent( id, k -> arr );
						final String denom = arr.get( EduCol.DENOMINATIE )
								.asText();
						if( denom.startsWith( "Prot" ) // 23.1%
								|| denom.startsWith( "Geref" ) // 1.2%
								|| denom.startsWith( "Evan" ) // 0.1%
						) return LUTHER;

						if( denom.startsWith( "Antro" ) ) // 0.9%
							return STEINER;

						final String type = arr.get( EduCol.PO_SOORT )
								.asText();
						if( type.startsWith( "S" ) || type.contains( "s" ) )
							return SPECIAL;

						return REST;
					} );

			final ConditionalDistribution<String, Object[]> pc4SchoolDist = ConditionalDistribution
					.of( params -> zipCultDists
							.computeIfAbsent( params[0].toString(),
									zip -> Collections.emptyMap() )
							.computeIfAbsent( params[1].toString(),
									cat -> zipCultDists.get( params[0] )
											.get( REST ) ) );

			LOG.debug( "Testing dist fallback..." );
			zipCultDists.keySet().stream().sorted().limit( 10 )
					.forEach( pc4 -> Arrays.asList( LUTHER, STEINER, SPECIAL )
							.stream().forEach( cat -> LOG.debug(
									"...draw for {} x {}: {}", pc4, cat,
									schoolCache.get( pc4SchoolDist.draw(
											new Object[]
									{ pc4, cat } ) ) ) ) );
		}
	}
}
