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
package nl.rivm.cib.episim.pilot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.quantity.Frequency;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;

import io.coala.bind.LocalConfig;
import io.coala.config.ConfigUtil;
import io.coala.config.YamlUtil;
import io.coala.exception.Thrower;
import io.coala.log.LogUtil;
import io.coala.math.QuantityConfigConverter;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scenario;
import io.coala.time.TimeUnits;
import io.coala.util.Compare;
import io.coala.util.Comparison;
import io.coala.util.FileUtil;

/**
 * {@link EcosystemConfig} may use national census data from CBS statline, e.g.:
 * <li>37230ned: Bevolkingsontwikkeling; regio per maand (http://statline.cbs.nl/Statweb/selection/?VW=T&DM=SLNL&PA=37230ned)
 * <li>37620: Personen in huishoudens naar leeftijd en geslacht, 1 januari (http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=37620)
 * <li>37973: Personen; positie in het huishouden, herkomstgroepering, per 1 januari (http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=37973)
 * <li>37975: Particuliere huishoudens naar samenstelling en grootte, 1 januari, 1995-2016 (http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=37975)
 * <li>70133NED: Huishoudens; typering naar grootte, 1 januari (http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70133NED)
 * <li>71488ned: Huishoudens; personen naar geslacht, leeftijd en regio, 1 januari, 1994-2016 (http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=71488ned)
 * <li>81922ned: Bevolking en huishoudens; viercijferige postcode, 1 januari 2012 (http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=81922ned)
 * <li>83226ned: Prognose huishoudens op 1 januari; kerncijfers 2016-2060 (http://statline.cbs.nl/statweb/publication/?dm=slnl&pa=83226ned)
 * <li>83225ned: prognose kerncijfers (2016-2060) (http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83225ned)
 * <li>maatwerk: Bevolkingsvariant 2016-2060 (op verzoek van het CPB): inwonertal, geboorte, sterfte en migratie (https://www.cbs.nl/nl-nl/maatwerk/2016/32/bevolkingsvariant-2016-2060)
 * <li>37422ned: Geboorte; kerncijfers, 1950-2015 (http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=37422ned)
 * <li>70895ned: Overledenen; geslacht en leeftijd, per week (http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70895ned)
 * <li>37979ned: Overledenen; kerncijfers, 1950-2015 (http://statline.cbs.nl/statweb/publication/?vw=t&dm=slnl&pa=37979ned)
 * <li>83190ned: overledenen in huishouden per leeftijd (http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T)
 * <li>81435ned: Traffic intensity (http://opendata.cbs.nl/dataportaal/portal.html?_catalog=CBS&_la=nl&tableId=81435ned&_theme=364)
 * <li>81125ned: Traffic participants (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81125ned&_theme=361)
 * <li>37856: Mobility - vehicle posession (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=37856&_theme=837)
 * <li>81128ned: Mobility - traveler characteristics (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81128ned&_theme=494)
 * <li>81127ned: Mobility - traffic characteristics (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81127ned&_theme=494)
 * <li>81124ned: Mobility - motives (http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81124ned&_theme=494)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface EcosystemConfig extends ReplicateConfig
{

	@DefaultValue( "7500000" )
	int popSize();

	@DefaultValue( "0.08 " + TimeUnits.ANNUAL_LABEL )
	@ConverterClass( QuantityConfigConverter.class )
	Quantity<Frequency> couplingProportion();

	@DefaultValue( "0.02 " + TimeUnits.ANNUAL_LABEL )
	@ConverterClass( QuantityConfigConverter.class )
	Quantity<Frequency> leavingProportion();

	@DefaultValue( "0.01 " + TimeUnits.ANNUAL_LABEL )
	@ConverterClass( QuantityConfigConverter.class )
	Quantity<Frequency> divorcingProportion();

//	this.growthRate = Signal.Simple.of( this.scheduler,
//			QuantityUtil.valueOf( 0.01, TimeUnits.ANNUAL ) );
//	this.immigrationRate = Signal.Simple.of( this.scheduler,
//			QuantityUtil.valueOf( 0, TimeUnits.ANNUAL ) );
//	this.ageBirthing = Signal.Simple.of( scheduler, Range.of( 15, 50 ) );
//	this.ageCoupling = Signal.Simple.of( scheduler, Range.of( 21, 60 ) );
//	this.ageLeaving = Signal.Simple.of( scheduler, Range.of( 18, null ) );
//	this.ageDivorcing = Signal.Simple.of( scheduler, Range.of( 24, 60 ) );
//	this.agePartner = this.distFact.createNormal( -2, 2 )
//			.toQuantities( TimeUnits.ANNUM );
//	this.birthGap = this.distFact.createDeterministic( 270 )
//			.toQuantities( TimeUnits.DAYS );

	@DefaultValue( "sim-demog/" )
	String dataDir();

	@DefaultValue( "${dataDir}age_dist.dat" )
	URI age_dist();

	@DefaultValue( "${dataDir}death_rates_female.dat" )
	URI death_rates_female();

	@DefaultValue( "${dataDir}death_rates_male.dat" )
	URI death_rates_male();

	@DefaultValue( "${dataDir}fertility_age_probs.dat" )
	URI fertility_age_probs();

	@DefaultValue( "${dataDir}fertility_rates.dat" )
	URI fertility_rates();

	@DefaultValue( "${dataDir}hh_comp.dat" )
	URI hh_comp();

	// TODO read from "paramspec_pop.cfg", rather: parse distributions

	Pattern VALUE_SEP = Pattern.compile( "\\s" );

	Logger LOG = LogUtil.getLogger( EcosystemConfig.class );

	static <T> List<WeightedValue<T>> importFrequencies( final URI path,
		final int weightColumn, final Function<String, T> parser )
		throws IOException
	{
		//Locale.setDefault( Locale.US );
		Objects.requireNonNull( parser );
		int lineNr = 0;
		try( final BufferedReader in = new BufferedReader(
				new InputStreamReader( FileUtil.toInputStream( path ) ) ) )
		{
			final List<WeightedValue<T>> result = new ArrayList<>();
			final int valueIndex = 1 - weightColumn;
			for( String line; (line = in.readLine()) != null; )
			{
				line = line.trim();
				if( line.startsWith( "#" ) ) continue;

				final String[] values = VALUE_SEP.split( line, 2 );
				final BigDecimal weight = new BigDecimal(
						values[weightColumn] );
				if( Compare.ge( BigDecimal.ZERO, weight ) )
					LOG.warn( "Ignoring value '{}' with weight: {} ({}:{})",
							values[valueIndex], weight, path, lineNr );
				else
					result.add( WeightedValue
							.of( parser.apply( values[valueIndex] ), weight ) );
				lineNr++;
			}
			return result;
		}
	}

	static <K extends Comparable<?>, V> NavigableMap<K, BigDecimal> importMap(
		final URI path, final int valueColumn,
		final Function<String, K> keyParser ) throws IOException
	{
		//Locale.setDefault( Locale.US );
		Objects.requireNonNull( keyParser );
		int lineNr = 0;
		try( final BufferedReader in = new BufferedReader(
				new InputStreamReader( FileUtil.toInputStream( path ) ) ) )
		{
			final NavigableMap<K, BigDecimal> result = new ConcurrentSkipListMap<>();
			final int valueIndex = 1 - valueColumn;
			for( String line; (line = in.readLine()) != null; )
			{
				line = line.trim();
				if( line.startsWith( "#" ) ) continue;

				final String[] values = VALUE_SEP.split( line, 2 );
				final BigDecimal weight = new BigDecimal( values[valueColumn] );
				if( Comparison.of( weight,
						BigDecimal.ZERO ) != Comparison.GREATER )
					LOG.warn( "Ignoring value '{}' with weight: {} ({}:{})",
							values[valueIndex], weight, path, lineNr );
				else
					result.put( keyParser.apply( values[valueIndex] ), weight );
				lineNr++;
			}
			return result;
		}
	}

	static List<ProbabilityDistribution<Integer>> splitAgeDistribution(
		final ProbabilityDistribution.Factory distFact,
		final List<WeightedValue<Integer>> ageDist, final URI path )
		throws IOException
	{
		final List<ProbabilityDistribution<Integer>> result = new ArrayList<>();
		try( final BufferedReader in = new BufferedReader(
				new InputStreamReader( FileUtil.toInputStream( path ) ) ) )
		{
			for( String line; (line = in.readLine()) != null; )
			{
				line = line.trim();
				if( line.startsWith( "#" ) ) continue;
				final String[] values = VALUE_SEP.split( line, 2 );
				if( values.length < 2 ) break;
				int bound_a = 0;
				for( String v : VALUE_SEP.split( values[1] ) )
				{
					final List<WeightedValue<Integer>> subDist = new ArrayList<>();
					final int bound_b = Integer.valueOf( v );
					LOG.trace( "Filter age: {} =< x < {}", bound_a, bound_b );
					for( WeightedValue<Integer> wv : ageDist )
						if( wv.getValue() >= bound_a
								&& wv.getValue() < bound_b )
							subDist.add( wv );
					bound_a = bound_b;
					result.add( distFact.createCategorical( subDist ) );
				}
				final List<WeightedValue<Integer>> subDist = new ArrayList<>();
				LOG.trace( "Filter age: {} =< x", bound_a );
				for( WeightedValue<Integer> wv : ageDist )
					if( wv.getValue() >= bound_a ) subDist.add( wv );
				result.add( distFact.createCategorical( subDist ) );
				Collections.reverse( result );
				return result;
			}
		}
		throw new IOException( "No cut-offs found in file at: " + path );
	}

	String EPIDEMES_YAML_FILE = "epidemes.yaml";

	String SCENARIO_NAME = "epidemes";

	String SCENARIO_TYPE_KEY = SCENARIO_NAME + KEY_SEP + "init";

	@Key( SCENARIO_TYPE_KEY )
	@DefaultValue( "nl.rivm.cib.episim.pilot.EcosystemScenario" )
	Class<? extends Scenario> scenarioType();

	default Scenario createScenario()
	{
		final Map<String, Object> export = ConfigUtil.export( this );
		export.put( ReplicateConfig.ID_KEY, SCENARIO_NAME );
		export.put( LocalConfig.ID_KEY, SCENARIO_NAME );

		// configure replication FIXME via LocalConfig?
		ReplicateConfig.getOrCreate( export );

		return ConfigFactory.create( LocalConfig.class, export ).createBinder()
				.inject( scenarioType() );
	}

	/**
	 * @param imports optional extra configuration settings
	 * @return the imported values
	 * @throws IOException if reading from {@link #EPIDEMES_YAML_FILE} fails
	 */
	static EcosystemConfig getOrFromYaml( final Map<?, ?>... imports )
	{
		try
		{
			return ConfigCache.getOrCreate( SCENARIO_NAME,
					EcosystemConfig.class,
					ConfigUtil.join(
							YamlUtil.flattenYaml(
									FileUtil.toInputStream( EPIDEMES_YAML_FILE ) ),
							imports ) );
		} catch( final IOException e )
		{
			return Thrower.rethrowUnchecked( e );
		}
	}

	/**
	 * @param args the command line arguments
	 * @throws IOException if reading from {@link #EPIDEMES_YAML_FILE} fails
	 */
	static void main( final String[] args ) throws IOException
	{
		final Map<?, ?> argMap = Arrays.asList( args ).stream()
				.filter( arg -> arg.contains( "=" ) )
				.map( arg -> arg.split( "=" ) ).collect( Collectors
						.toMap( parts -> parts[0], parts -> parts[1] ) );
		final EcosystemConfig config = getOrFromYaml( argMap );
		LogUtil.getLogger( EcosystemConfig.class )
				.info( "EPIDEMES scenario starting, config: {}", config.toYAML() );
		config.createScenario().run();
		LogUtil.getLogger( EcosystemConfig.class ).info( "EPIDEMES completed" );
	}

}