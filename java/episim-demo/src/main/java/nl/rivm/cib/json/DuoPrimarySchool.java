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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.util.FileUtil;
import nl.rivm.cib.epidemes.cbs.json.CbsRegionHierarchy;

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

	private static final String outFileName = "dist/region-primary-school-students.json";

	public static void main_old( final String[] args ) throws Exception
	{
		try( final InputStream is = FileUtil.toInputStream( outFileName ) )
		{
			final JsonNode root = JsonUtil.getJOM().readTree( is );

			JsonUtil.forEach( (ObjectNode) root,
					( ld, ldNode ) -> JsonUtil.forEach(
							((ObjectNode) ldNode).with( "prov" ), ( pv,
								pvNode ) -> JsonUtil.forEach(
										((ObjectNode) pvNode).with( "corop" ), (
											cr, crNode ) -> JsonUtil.forEach(
													((ObjectNode) ldNode)
															.with( "gm" ),
													( gm, gmNode ) ->
													{
													} ) ) ) );
//			final Map<String, WeightedValue<String>> localChoice;
		}
	}

	public static void main( final String[] args ) throws Exception
	{

		final String pc4GemFile = "dist/adm_pc4_2016_basis.json";

		final TreeMap<String, String> pc4Gem = JsonUtil
				.readArrayAsync( () -> FileUtil.toInputStream( pc4GemFile ),
						BagZipcode4RegionPolygons.class )
				.collect( () -> new TreeMap<String, String>(),
						( m, v ) -> m.put( String.format( "%04d", v.zip ),
								String.format( "GM%04d", v.gm ) ) )
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

//		(denominatie, gemeente/postcode, leeftijd) -> vestiging: [latlon, maxage, cap]

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
						.with( "dist" ).with( pc4Gm )
						.with( v[Col.POSTCODE_LEERLING.ordinal()] )
						.with( v[Col.DENOMINATIE_VESTIGING.ordinal()] )
						.put( id, Integer.valueOf( v[Col.TOTAAL.ordinal()] ) );

				gmCultAgeSchoolPos.with( "schools" )
//				.with(String.format( "GM%04d", Integer.valueOf(
//										v[Col.GEMEENTENUMMER.ordinal()] ) ) )
						.putArray( id )
						.add( String.format( "GM%04d",
								Integer.valueOf(
										v[Col.GEMEENTENUMMER.ordinal()] ) ) )
						.add( v[Col.SOORT_PO.ordinal()] )
						.add( v[Col.POSTCODE_VESTIGING.ordinal()] )
						.add( v[Col.DENOMINATIE_VESTIGING.ordinal()] )
						.add( stat.geo.coords[1] ).add( stat.geo.coords[0] )
						.add( Integer.valueOf( v[Col.TOTAAL.ordinal()] ) );

//
//				brinData.computeIfAbsent( loc, k -> Stream.of( Col.BRIN_NUMMER,
//						Col.VESTIGINGSNUMMER, Col.BEVOEGD_GEZAG_NUMMER,
//						Col.INSTELLINGSNAAM_VESTIGING,
//						Col.DENOMINATIE_VESTIGING, Col.PLAATSNAAM,
//						Col.POSTCODE_VESTIGING, Col.PROVINCIE, Col.SOORT_PO,
//						Col.GEMEENTENUMMER, Col.GEMEENTENAAM )
//						.collect( () -> new EnumMap<>( Col.class ),
//								( m, i ) -> m.put( i, v[i.ordinal()] ),
//								( m1, m2 ) -> m1.putAll( m2 ) ) );
//
//				originCounts.computeIfAbsent( loc, k -> new HashMap<>() )
//						.computeIfAbsent(
//								v[Col.GEMEENTENAAM_LEERLING.ordinal()],
//								k -> new HashMap<>() )
//						.compute( v[Col.POSTCODE_LEERLING.ordinal()],
//								( k, n ) -> (n == null ? 0 : n) + Integer
//										.valueOf( v[Col.TOTAAL.ordinal()] ) );
//
//				final EnumMap<Col, Integer> ageCount = ageCounts
//						.computeIfAbsent( loc,
//								k -> new EnumMap<>( Col.class ) );
//				IntStream
//						.range( Col.L3_MIN.ordinal(), Col.TOTAAL.ordinal() + 1 )
//						.filter( i -> Integer.valueOf( v[i] ) > 0 )
//						.forEach( i -> ageCount.compute( Col.values()[i],
//								( k, n ) -> (n == null ? 0 : n)
//										+ Integer.valueOf( v[i] ) ) );
			} );
		}

//		LOG.info( "Got: {}", JsonUtil.toJSON( gmCultAgeSchoolPos ) );

		// put in administrative hierarchy...
		final String gmRegionFile = "dist/83287NED.json";
		try( final InputStream is = FileUtil.toInputStream( gmRegionFile );
				final OutputStream os = FileUtil.toOutputStream( outFileName,
						false ); )
		{
			final CbsRegionHierarchy gmRegs = JsonUtil.getJOM().readValue( is,
					CbsRegionHierarchy.class );

			gmCultAgeSchoolPos.replace( "dist", gmRegs
					.addAdminHierarchy( gmCultAgeSchoolPos.with( "dist" ) ) );
			JsonUtil.getJOM().writer().withDefaultPrettyPrinter()
					.writeValue( os, gmCultAgeSchoolPos );
		}
		LOG.warn( "Missing stats for PC6 addresses: {}", missing );

//		LOG.info( "Got counts..\n{}",
//				String.join( "\n",
//						brinData.entrySet().stream().limit( 10 )
//								.map( e -> e.getValue() + "\n\t"
//										+ originCounts.get( e.getKey() )
//										+ "\n\t" + ageCounts.get( e.getKey() ) )
//								.toArray( String[]::new ) ) );
//
//		brinData.entrySet().stream().forEach( e ->
//		{
//			final String denom = e.getValue().get( Col.DENOMINATIE_VESTIGING );
//			final Map<String, List<WeightedValue<String>>> cultDist;
//			final AtomicInteger cultCount;
//			if( denom.startsWith( "Evan" ) // 0.1%
////			|| denom.contains( "PC" ) // 1.1%
////					|| denom.startsWith( "Prot" ) // 23.1%
//					|| denom.startsWith( "Geref" ) // 1.2%
//			)
//			{
//				cultDist = distPC;
//				cultCount = countPC;
//			} else if( denom.startsWith( "Antro" ) ) // 0.9%
//			{
//				cultDist = distAnt;
//				cultCount = countAnt;
//			} else
//			{
//				cultDist = dist;
//				cultCount = count;
//			}
//			// count by zipcode
////			originCounts.get( e.getKey() ).values().stream()
////					.flatMap( m -> m.entrySet().stream() ) //
////					.forEach( n ->
////					{
////						cultDist.computeIfAbsent( n.getKey(),
////								k -> new ArrayList<>() )
////								.add( WeightedValue.of( e.getKey(),
////										n.getValue() ) );
////						cultCount.addAndGet( n.getValue() );
////					} );
//			// count by municipality
//			originCounts.get( e.getKey() ).entrySet().stream().forEach( n ->
//			{
//				final int sum = n.getValue().values().stream()
//						.mapToInt( i -> i ).sum();
//				cultDist.computeIfAbsent( n.getKey(), k -> new ArrayList<>() )
//						.add( WeightedValue.of( e.getKey(), sum ) );
//				cultCount.addAndGet( sum );
//			} );
//		} );
//
//		final int sum = count.get() + countPC.get() + countAnt.get();
//		LOG.info( "Got dist ({}={}%): \n\n{}", count, DecimalUtil.toScale(
//				DecimalUtil.divide( count, sum ).doubleValue() * 100, 1 ),
//				String.join( "\n",
//						dist.entrySet().stream().limit( 10 ).map( e -> e
//								.getKey()
//								+ ": "
//								+ Arrays.toString( e.getValue().stream()
//										.map( wv -> wv.getValue() + ":"
//												+ wv.getWeight()
//												+ ageCounts
//														.get( wv.getValue() ) )
//										.toArray( String[]::new ) ) )
//								.toArray( String[]::new ) ) );
//		LOG.info( "Got dist Reform ({}={}%): \n\n{}", countPC,
//				DecimalUtil.toScale(
//						DecimalUtil.divide( countPC, sum ).doubleValue() * 100,
//						1 ),
//				String.join( "\n", distPC.entrySet().stream().limit( 10 )
//						.map( e -> e.getKey() + ": " + Arrays.toString( e
//								.getValue().stream()
//								.map( wv -> wv.getValue() + ":" + wv.getWeight()
//										+ ageCounts.get( wv.getValue() ) )
//								.toArray( String[]::new ) ) )
//						.toArray( String[]::new ) ) );
//		LOG.info( "Got dist Anthro ({}={}%): \n\n{}", countAnt,
//				DecimalUtil.toScale(
//						DecimalUtil.divide( countAnt, sum ).doubleValue() * 100,
//						1 ),
//				String.join( "\n", distAnt.entrySet().stream().limit( 10 )
//						.map( e -> e.getKey() + ": " + Arrays.toString( e
//								.getValue().stream()
//								.map( wv -> wv.getValue() + ":" + wv.getWeight()
//										+ ageCounts.get( wv.getValue() ) )
//								.toArray( String[]::new ) ) )
//						.toArray( String[]::new ) ) );
//
//		final String gmRegionFile = "dist/83287NED.json";
//		LOG.info( "Reading municipal region hierarchies from {}",
//				gmRegionFile );
//		try( final InputStream is = FileUtil.toInputStream( gmRegionFile ) )
//		{
//			final CbsRegionHierarchy gmRegs = JsonUtil.getJOM().readValue( is,
//					CbsRegionHierarchy.class );
//			final ObjectNode gmBoroZipPos = gmRegs.addAdminHierarchy( null );
//		}

	}
}
