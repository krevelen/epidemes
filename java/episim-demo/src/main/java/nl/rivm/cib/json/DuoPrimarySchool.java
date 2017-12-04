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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.WeightedValue;

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

	private static final Map<String, EnumMap<Col, String>> brinData = new HashMap<>();

	private static final Map<String, EnumMap<Col, Integer>> ageCounts = new HashMap<>();

	private static final Map<String, Map<String, Map<String, Integer>>> originCounts = new HashMap<>();

	private static final Map<String, List<WeightedValue<String>>> dist = new TreeMap<>();

	private static final Map<String, List<WeightedValue<String>>> distPC = new TreeMap<>();

	private static final Map<String, List<WeightedValue<String>>> distAnt = new TreeMap<>();

	private static final AtomicInteger count = new AtomicInteger();

	private static final AtomicInteger countPC = new AtomicInteger();

	private static final AtomicInteger countAnt = new AtomicInteger();

	public static void main( final String[] args ) throws Exception
	{
		final String fileName = "03.-leerlingen-po-totaaloverzicht-2015-2016.csv";
		try( Stream<String> stream = Files.lines( Paths.get( "dist", fileName ),
				Charset.forName( "ISO-8859-1" ) ) )
		{
			stream.skip( 1 ).map( line -> line.split( ";" ) ).forEach( v ->
			{
				final String loc = v[Col.BRIN_NUMMER.ordinal()] + "_"
						+ v[Col.VESTIGINGSNUMMER.ordinal()];

				brinData.computeIfAbsent( loc,
						k -> Stream.of( Col.BRIN_NUMMER, Col.VESTIGINGSNUMMER,
								Col.BEVOEGD_GEZAG_NUMMER,
								Col.INSTELLINGSNAAM_VESTIGING,
								Col.DENOMINATIE_VESTIGING, Col.PLAATSNAAM,
								Col.PROVINCIE, Col.SOORT_PO, Col.GEMEENTENUMMER,
								Col.GEMEENTENAAM )
								.collect( () -> new EnumMap<>( Col.class ),
										( m, i ) -> m.put( i, v[i.ordinal()] ),
										( m1, m2 ) -> m1.putAll( m2 ) ) );

				originCounts.computeIfAbsent( loc, k -> new HashMap<>() )
						.computeIfAbsent(
								v[Col.GEMEENTENAAM_LEERLING.ordinal()],
								k -> new HashMap<>() )
						.compute( v[Col.POSTCODE_LEERLING.ordinal()],
								( k, n ) -> (n == null ? 0 : n) + Integer
										.valueOf( v[Col.TOTAAL.ordinal()] ) );

				final EnumMap<Col, Integer> ageCount = ageCounts
						.computeIfAbsent( loc,
								k -> new EnumMap<>( Col.class ) );
				IntStream
						.range( Col.L3_MIN.ordinal(), Col.TOTAAL.ordinal() + 1 )
						.filter( i -> Integer.valueOf( v[i] ) > 0 )
						.forEach( i -> ageCount.compute( Col.values()[i],
								( k, n ) -> (n == null ? 0 : n)
										+ Integer.valueOf( v[i] ) ) );
			} );
		}
		LOG.info( "Got counts..\n{}",
				String.join( "\n",
						brinData.entrySet().stream().limit( 10 )
								.map( e -> e.getValue() + "\n\t"
										+ originCounts.get( e.getKey() )
										+ "\n\t" + ageCounts.get( e.getKey() ) )
								.toArray( String[]::new ) ) );

		brinData.entrySet().stream().forEach( e ->
		{
			final String denom = e.getValue().get( Col.DENOMINATIE_VESTIGING );
			final Map<String, List<WeightedValue<String>>> cultDist;
			final AtomicInteger cultCount;
			if( denom.startsWith( "Evan" ) // 0.1%
//			|| denom.contains( "PC" ) // 1.1%
//					|| denom.startsWith( "Prot" ) // 23.1%
					|| denom.startsWith( "Geref" ) // 1.2%
			)
			{
				cultDist = distPC;
				cultCount = countPC;
			} else if( denom.startsWith( "Antro" ) ) // 0.9%
			{
				cultDist = distAnt;
				cultCount = countAnt;
			} else
			{
				cultDist = dist;
				cultCount = count;
			}
			// count by zipcode
//			originCounts.get( e.getKey() ).values().stream()
//					.flatMap( m -> m.entrySet().stream() ) //
//					.forEach( n ->
//					{
//						cultDist.computeIfAbsent( n.getKey(),
//								k -> new ArrayList<>() )
//								.add( WeightedValue.of( e.getKey(),
//										n.getValue() ) );
//						cultCount.addAndGet( n.getValue() );
//					} );
			// count by municipality
			originCounts.get( e.getKey() ).entrySet().stream().forEach( n ->
			{
				final int sum = n.getValue().values().stream()
						.mapToInt( i -> i ).sum();
				cultDist.computeIfAbsent( n.getKey(), k -> new ArrayList<>() )
						.add( WeightedValue.of( e.getKey(), sum ) );
				cultCount.addAndGet( sum );
			} );
		} );

		final int sum = count.get() + countPC.get() + countAnt.get();
		LOG.info( "Got dist ({}={}%): \n\n{}", count, DecimalUtil.toScale(
				DecimalUtil.divide( count, sum ).doubleValue() * 100, 1 ),
				String.join( "\n",
						dist.entrySet().stream().limit( 10 ).map( e -> e
								.getKey()
								+ ": "
								+ Arrays.toString( e.getValue().stream()
										.map( wv -> wv.getValue() + ":"
												+ wv.getWeight()
												+ ageCounts
														.get( wv.getValue() ) )
										.toArray( String[]::new ) ) )
								.toArray( String[]::new ) ) );
		LOG.info( "Got dist Reform ({}={}%): \n\n{}", countPC,
				DecimalUtil.toScale(
						DecimalUtil.divide( countPC, sum ).doubleValue() * 100,
						1 ),
				String.join( "\n", distPC.entrySet().stream().limit( 10 )
						.map( e -> e.getKey() + ": " + Arrays.toString( e
								.getValue().stream()
								.map( wv -> wv.getValue() + ":" + wv.getWeight()
										+ ageCounts.get( wv.getValue() ) )
								.toArray( String[]::new ) ) )
						.toArray( String[]::new ) ) );
		LOG.info( "Got dist Anthro ({}={}%): \n\n{}", countAnt,
				DecimalUtil.toScale(
						DecimalUtil.divide( countAnt, sum ).doubleValue() * 100,
						1 ),
				String.join( "\n", distAnt.entrySet().stream().limit( 10 )
						.map( e -> e.getKey() + ": " + Arrays.toString( e
								.getValue().stream()
								.map( wv -> wv.getValue() + ":" + wv.getWeight()
										+ ageCounts.get( wv.getValue() ) )
								.toArray( String[]::new ) ) )
						.toArray( String[]::new ) ) );
//		(denominatie, gemeente/postcode, leeftijd) -> vestiging <-brin+vestiging/gemeente/postcode x leeftijd

	}
}
