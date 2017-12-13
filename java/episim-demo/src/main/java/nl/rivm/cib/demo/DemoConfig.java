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
package nl.rivm.cib.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aeonbits.owner.Config.Sources;

import io.coala.config.ConfigUtil;
import io.coala.config.LocalDateConverter;
import io.coala.config.PeriodConverter;
import io.coala.config.YamlConfig;
import io.coala.math.DecimalUtil;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Compartment;
import nl.rivm.cib.pilot.PilotConfig.RandomSeedConverter;

/**
 * {@link DemoConfig} used in {@link DemoModel}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Sources( {
		"file:" + DemoConfig.CONFIG_BASE_PARAM + DemoConfig.CONFIG_YAML_FILE,
		"file:" + DemoConfig.CONFIG_BASE_DIR + DemoConfig.CONFIG_YAML_FILE,
		"classpath:" + DemoConfig.CONFIG_YAML_FILE } )
public interface DemoConfig extends YamlConfig
{

	String CONFIG_YAML_FILE = "demo.yaml";

	/** configuration and data file base directory */
	String CONFIG_BASE_DIR = "dist/";

	/** configuration file name system property */
	String CONFIG_BASE_KEY = "config.base";

	/** configuration file name system property */
	String CONFIG_BASE_PARAM = "${" + CONFIG_BASE_KEY + "}";

	@DefaultValue( CONFIG_BASE_DIR )
	@Key( CONFIG_BASE_KEY )
	String configBase();

	String DATASOURCE_JNDI = "jdbc/pilotDB";

	/** configuration key separator */
	String KEY_SEP = ConfigUtil.CONFIG_KEY_SEP;

	/** configuration key */
	String SCENARIO_BASE = "scenario";

	/** configuration key */
	String REPLICATION_PREFIX = SCENARIO_BASE + KEY_SEP + "replication"
			+ KEY_SEP;

	/** configuration key */
	String DEMOGRAPHY_BASE = "demography";

	/** configuration key */
	String GEOGRAPHY_BASE = "geography";

	/** configuration key */
	String MOTION_BASE = "mobility";

	/** configuration key */
	String EPIDEMIOLOGY_BASE = "epidemiology";

	/** configuration key */
	String HESITANCY_BASE = "hesitancy";

	@Key( REPLICATION_PREFIX + "setup-name" )
	@DefaultValue( "pilot" )
	String setupName();

	@Key( REPLICATION_PREFIX + "random-seed" )
	@DefaultValue( "NaN" )
	@ConverterClass( RandomSeedConverter.class )
	Long randomSeed();

	@Key( REPLICATION_PREFIX + "duration-period" )
	@DefaultValue( "P1Y" )
	@ConverterClass( PeriodConverter.class )
	Period duration();

	@Key( REPLICATION_PREFIX + "offset-date" )
	@DefaultValue( "2012-01-01" )
	@ConverterClass( LocalDateConverter.class )
	LocalDate offset();

	String CONF_ARG = "conf";

	String sep = ";", eol = "\r\n";

	static String toHeader( final List<Compartment> sirCols,
		final Map<String, Set<String>> colMapping )
	{
		return "ActualTime" + sep + "VirtualTime" + sep
				+ String.join( sep, sirCols.stream()
						.map( c -> c.name().substring( 0, 1 ) + "_TOTAL" )
						.toArray( String[]::new ) )
				+ sep
				+ String.join( sep, sirCols.stream()
						.flatMap( c -> colMapping.keySet().stream()
								.map( reg -> c.name().substring( 0, 1 ) + '_'
										+ reg ) )
						.toArray( String[]::new ) )
				+ eol;
	}

	static String toLine( final List<Compartment> sirCols, final String t,
		final Map<String, Set<String>> colMapping,
		final Map<String, EnumMap<Compartment, Long>> homeSIR )
	{
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME
				.format( ZonedDateTime.now() ) + sep + t
				+ sep
				+ String.join( sep, sirCols.stream()
						.map( c -> colMapping.entrySet().stream()
								.flatMap( e -> e.getValue().stream() )
								.map( reg -> homeSIR
										.computeIfAbsent( reg,
												k -> new EnumMap<>(
														Compartment.class ) )
										.computeIfAbsent( c, k -> 0L ) )
								.mapToLong( n -> n ).sum() )
						.map( Object::toString ).toArray( String[]::new ) )
				+ sep
				+ String.join( sep, sirCols.stream().flatMap( c -> colMapping
						.entrySet().stream()
						.map( e -> e.getValue().stream()
								.mapToLong( reg -> homeSIR.get( reg ).get( c ) )
								.sum() ) )
						.map( Object::toString ).toArray( String[]::new ) )
				+ eol;
	}

	static BigDecimal evaluate( final String key,
		final Map<String, EnumMap<Compartment, Long>> values,
		final Compartment descendCol )
	{
		final EnumMap<Compartment, Long> v = values.computeIfAbsent( key,
				k -> new EnumMap<>( Compartment.class ) );
		final Long dividend = v.get( descendCol );
		if( dividend == null || dividend.longValue() == 0 )
			return BigDecimal.ZERO;
		return DecimalUtil.divide( dividend,
				v.values().stream().mapToLong( n -> n ).sum() );
	}

	static String toLog( final List<Compartment> sirCols,
		final Map<String, EnumMap<Compartment, Long>> homeSIR, final int n,
		final Compartment descendCol )
	{
		return String.join( ", ",
				homeSIR.keySet().stream().sorted( ( r,
					l ) -> evaluate( l, homeSIR, descendCol ).compareTo(
							evaluate( r, homeSIR, descendCol ) ) )
						.limit( n )
						.map( reg -> reg + ":["
								+ String.join( ",",
										sirCols.stream().map( c -> homeSIR
												.computeIfAbsent( reg,
														k -> new EnumMap<>(
																Compartment.class ) )
												.computeIfAbsent( c, k -> 0L )
												.toString() )
												.toArray( String[]::new ) )
								+ "]" )
						.toArray( String[]::new ) );
	}
}