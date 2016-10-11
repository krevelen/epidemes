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
package nl.rivm.cib.episim.model.scenario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.measure.DecimalMeasure;
import javax.measure.Measurable;
import javax.measure.quantity.Dimensionless;

import org.aeonbits.owner.Converter;
import org.apache.logging.log4j.Logger;

import io.coala.config.YamlConfig;
import io.coala.log.LogUtil;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.coala.util.Comparison;
import io.coala.util.FileUtil;

interface Geard2011Config extends YamlConfig
{
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

	Logger LOG = LogUtil.getLogger( Geard2011Config.class );

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
				if( Comparison.of( weight,
						BigDecimal.ZERO ) != Comparison.GREATER )
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

	@DefaultValue( "0.08" )
	@ConverterClass( DecimalMeasureConverter.class )
	Measurable<Dimensionless> annualIndividualCouplingProbability();

	@DefaultValue( "0.02" )
	@ConverterClass( DecimalMeasureConverter.class )
	Measurable<Dimensionless> annualIndividualLeavingProbability();

	@DefaultValue( "0.01" )
	@ConverterClass( DecimalMeasureConverter.class )
	Measurable<Dimensionless> annualIndividualDivorcingProbability();

	class DecimalMeasureConverter
		implements Converter<DecimalMeasure<Dimensionless>>
	{
		@Override
		public DecimalMeasure<Dimensionless> convert( final Method method,
			final String input )
		{
			return DecimalMeasure.valueOf( input );
		}
	}
}