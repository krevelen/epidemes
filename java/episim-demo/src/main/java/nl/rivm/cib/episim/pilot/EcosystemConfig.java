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
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.Transaction;
import io.coala.eve3.Eve3Exposer;
import io.coala.exception.Thrower;
import io.coala.guice4.Guice4LocalBinder;
import io.coala.inter.Exposer;
import io.coala.log.LogUtil;
import io.coala.math.QuantityConverter;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.util.Compare;
import io.coala.util.Comparison;
import io.coala.util.FileUtil;

/**
 * {@link EcosystemConfig}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface EcosystemConfig extends ReplicateConfig
{

	@DefaultValue( "7500000" )
	int popSize();

	// CBS 70895ned: Overledenen; geslacht en leeftijd, per week
	// http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70895ned&LA=NL

	// CBS 83190ned: overledenen in huishouden per leeftijd
	// http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T

	/**
	 * <ul>
	 * <li>Traffic intensity (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_catalog=CBS&_la=nl&tableId=81435ned&_theme=364">CBS
	 * 81435ned</a>)
	 * <li>Traffic participants (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81125ned&_theme=361">CBS
	 * 81125ned</a>)
	 * <li>Mobility - vehicle posession (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=37856&_theme=837">CBS
	 * 37856</a>)
	 * <li>Mobility - traveler characteristics (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81128ned&_theme=494">CBS
	 * 81128ned</a>)
	 * <li>Mobility - traffic characteristics (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81127ned&_theme=494">CBS
	 * 81127ned</a>)
	 * <li>Mobility - motives (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81124ned&_theme=494">CBS
	 * 81124ned</a>)
	 * </ul>
	 */

	@DefaultValue( "0.08 " + TimeUnits.ANNUAL_LABEL )
	@ConverterClass( QuantityConverter.class )
	Quantity<Frequency> couplingProportion();

	@DefaultValue( "0.02 " + TimeUnits.ANNUAL_LABEL )
	@ConverterClass( QuantityConverter.class )
	Quantity<Frequency> leavingProportion();

	@DefaultValue( "0.01 " + TimeUnits.ANNUAL_LABEL )
	@ConverterClass( QuantityConverter.class )
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

	// CBS 83225ned prognose kerncijfers (2016-2060), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83225ned&D1=0&D2=a&D3=0,131-133&D4=0,4,9,14,19,24,29,34,39,l&VW=T
	@DefaultValue( "${dataDir}age_dist.dat" )
	URI age_dist();

	// CBS 37979ned (1950-2015), e.g. http://statline.cbs.nl/statweb/publication/?vw=t&dm=slnl&pa=37979ned&d1=0-1,6-8,11,14,17,20-21,23-25,28-29&d2=0,5,10,15,20,25,30,35,40,45,50,57-l&hd=100614-1440&hdr=g1&stb=t
	// CBS 83225ned prognose kerncijfers (2016-2060), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83225ned&D1=0&D2=a&D3=0,131-133&D4=0,4,9,14,19,24,29,34,39,l&VW=T
	// bevolkingsvariant (2016-2060), e.g. https://www.cbs.nl/nl-nl/maatwerk/2016/32/bevolkingsvariant-2016-2060
	@DefaultValue( "${dataDir}death_rates_female.dat" )
	URI death_rates_female();

	// CBS 37979ned (1950-2015), e.g. http://statline.cbs.nl/statweb/publication/?vw=t&dm=slnl&pa=37979ned&d1=0-1,6-8,11,14,17,20-21,23-25,28-29&d2=0,5,10,15,20,25,30,35,40,45,50,57-l&hd=100614-1440&hdr=g1&stb=t
	// CBS 83225ned prognose kerncijfers (2016-2060), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83225ned&D1=0&D2=a&D3=0,131-133&D4=0,4,9,14,19,24,29,34,39,l&VW=T
	// bevolkingsvariant (2016-2060), e.g. https://www.cbs.nl/nl-nl/maatwerk/2016/32/bevolkingsvariant-2016-2060
	@DefaultValue( "${dataDir}death_rates_male.dat" )
	URI death_rates_male();

	// CBS 37422ned (1950-2015), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=37422ned&D1=0,4-5,7-14,16-19,21-22,26,35,40-41,47&D2=0,10,20,30,40,61-65&HDR=G1&STB=T&VW=T
	// CBS 83225ned prognose kerncijfers (2016-2060), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83225ned&D1=0&D2=a&D3=0,131-133&D4=0,4,9,14,19,24,29,34,39,l&VW=T
	// bevolkingsvariant (2016-2060), e.g. https://www.cbs.nl/nl-nl/maatwerk/2016/32/bevolkingsvariant-2016-2060
	@DefaultValue( "${dataDir}fertility_age_probs.dat" )
	URI fertility_age_probs();

	// CBS 37422ned (1950-2015), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=37422ned&D1=0,4-5,7-14,16-19,21-22,26,35,40-41,47&D2=0,10,20,30,40,61-65&HDR=G1&STB=T&VW=T
	// CBS 83225ned prognose kerncijfers (2016-2060), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83225ned&D1=0&D2=a&D3=0,131-133&D4=0,4,9,14,19,24,29,34,39,l&VW=T
	// bevolkingsvariant (2016-2060), e.g. https://www.cbs.nl/nl-nl/maatwerk/2016/32/bevolkingsvariant-2016-2060
	@DefaultValue( "${dataDir}fertility_rates.dat" )
	URI fertility_rates();

	// CBS 71488ned (1994-2016) hh compos, http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=71488ned&D1=0-10&D2=1-2&D3=0&D4=5-16,57-60,63,65-66,68-72,75,77-79,83-84,86-88,91-94,96-99,101,104-109,113-123,125-126,128-130,133,135-140,143,145-159,161-163,165-166,168,170,172,174-177,179-180,182-185,188-193,197-199,201-208,210-213,215,217,221-228,230,232-234,236,239,242-244,246-251,253,255-260,263,265-269,271-273,276-280,282-283,285-286,288-292,295-297,299-300,302-303,308,310-314,317-330,332-335,337-341,345,347-349,351-352,354,356-358,364-366,368-369,372-374,376,379-385,387-390,392-394,396,398-399,401-402,404,406,408-409,412-416,418-420,422-424,426-431,433-436,438-441,443-454,456-459,463-469,471-472,474-477,479-481,483-484,488,490-495,497,499,501-504,506-510,512,514-517,519-522,525-529,533-537,539-552,555-562,564-568,570-573,576-577,580-584,587,589,591-592,594,596,598-603,606-607,609,611-612,614-615,617-620,623-629,631,633-640,642-643&D5=12&HDR=G4,G1,T&STB=G3,G2&VW=T
	// CBS 81922ned (1 jan 2012) hh size PC4 avg, http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=81922ned&D1=66,70&D2=a&HDR=T&STB=G1&VW=T
	// CBS 37975 (1995-2016), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=37975&D1=0-5,10,15,19,22-26&D2=0,82-98&D3=0,5,10,15,17-21&HDR=T&STB=G1,G2&VW=T
	// CBS 71488ned (2000-2016), e.g. http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=71488ned&D1=a&D2=0&D3=0&D4=5-16&D5=6&HDR=G4,G1,G2,T&STB=G3&VW=T
	// CBS 83226ned (2016-2060), e.g. http://statline.cbs.nl/statweb/publication/?dm=slnl&pa=83226ned
	// Kindertal regionaal (2001-2014) grootstedelijke agglomeraties vs stadsgewesten
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