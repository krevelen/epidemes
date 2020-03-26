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
package nl.rivm.cib.epidemes.data.cbs;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;
import io.coala.math.Range;

/**
 * {@link CbsRegionHistory} parses the following CSV format:
 * 
 * <pre>
Einddatum;Eindjaar;Oude code;Nieuwe code;Huidige code;Huidige naam
1-1-1905;1905;GM1134;GM0489;GM0489;Barendrecht
1-8-1908;1908;GM1255;GM1060;GM0738;Aalburg
:
etc.
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class CbsRegionHistory
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( CbsRegionHistory.class );

	public enum Col
	{
		CHANGE_DATE, CHANGE_YEAR, FROM_CODE, TO_CODE, CURRENT_CODE, CURRENT_NAME;

	}

//	private static final String outFileName = "dist/region-primary-school-students.json";

	public static TreeMap<String, TreeMap<LocalDate, String>>
		parse( final String path, final String... morePath ) throws IOException
	{
		try( final Stream<String> stream = Files.lines(
				Paths.get( path, morePath ), Charset.forName( "ISO-8859-1" ) ) )
		{
			final TreeMap<String, TreeMap<LocalDate, String>> map = stream
					.skip( 1 ).map( line -> line.split( ";" ) )
					.filter( v -> v.length == Col.values().length
							&& !v[Col.FROM_CODE.ordinal()]
									.equals( v[Col.TO_CODE.ordinal()] ) )
					.collect( Collectors.groupingBy(
							v -> v[Col.FROM_CODE.ordinal()], TreeMap::new,
							Collectors.toMap( v ->
							{
								final String[] date = v[Col.CHANGE_DATE
										.ordinal()].split( "-" );
								return LocalDate.of( Integer.valueOf( date[2] ),
										Integer.valueOf( date[1] ),
										Integer.valueOf( date[0] ) );
							}, v -> v[Col.TO_CODE.ordinal()], ( v1, v2 ) -> v2,
									TreeMap::new ) ) );

			// recurse/expand histories per entry
			map.entrySet().stream().map( e -> e.getValue() ).forEach( to ->
			{
				String last = to.lastEntry().getValue();
				while( map.containsKey( last ) )
				{
					to.putAll( map.get( last ) );
					last = to.lastEntry().getValue();
				}
			} );
			return map;
		}
	}

	public static TreeMap<String, String> allChangesAsPer( final LocalDate date,
		final TreeMap<String, TreeMap<LocalDate, String>> map )
		throws IOException
	{
		return map.entrySet().stream()
				.filter( e -> e.getValue().floorEntry( date ) != null )
				.collect( Collectors.toMap( e -> e.getKey(),
						e -> e.getValue().floorEntry( date ).getValue(),
						( v1, v2 ) -> v2, TreeMap::new ) );
	}

	public static TreeMap<String, String> allChangesDuring(
		final Range<LocalDate> period,
		final TreeMap<String, TreeMap<LocalDate, String>> map )
		throws IOException
	{
		return map.entrySet().stream()
				.filter( e -> !period.apply( e.getValue() ).isEmpty() )
				.collect( Collectors.toMap(
						e -> e.getKey(), e -> period.apply( e.getValue() )
								.lastEntry().getValue(),
						( v1, v2 ) -> v2, TreeMap::new ) );
	}

	public static void main( final String[] args ) throws IOException
	{
		final TreeMap<String, TreeMap<LocalDate, String>> map = parse( "dist",
				"gm_changes_before_2018.csv" );
		LOG.trace( "Got history {}", map );

		final LocalDate date = LocalDate.of( 2016, 1, 1 );
		LOG.trace( "Changes (since 1830) as per {}: {}", date,
				allChangesAsPer( date, map ) );

		final Range<LocalDate> period = Range.of( date.minusYears( 3 ), date );
		LOG.trace( "Changes during {}: {}", period,
				allChangesDuring( period, map ) );
	}
}
