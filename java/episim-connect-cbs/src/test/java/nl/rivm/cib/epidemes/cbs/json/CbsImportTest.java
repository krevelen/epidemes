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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import io.coala.bind.LocalConfig;
import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.ConditionalDistribution;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.util.FileUtil;
import nl.rivm.cib.epidemes.cbs.json.Cbs37713json.CBSGender;

/**
 * {@link CbsImportTest}
 * 
 * trying:
 * <li>draw 2012 regional hh-type using 81922ned
 * <li>draw hh-subtype for kid count using national hh-type frequencies in 37975
 * <li>draw kid ages using mom age per child birth in 37201
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class CbsImportTest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( CbsImportTest.class );

	// households
	private static final String CBS_37975_FILE = "cbs/37975_2016JJ00.json";
	private static final String CBS_71486_FILE = "cbs/71486ned_2012JJ00.json";

	private static final String CBS_37230_FILE = "cbs/37230ned_monthly_change_series.json";
	private static final String CBS_37713_FILE = "cbs/37713_2012JJ00_AllOrigins.json";

	private static ProbabilityDistribution.Factory distFact;

	@BeforeClass
	public static void setupDistributionFactoy()
	{
		distFact = LocalConfig.builder()
				.withProvider( PseudoRandom.Factory.class,
						Math3PseudoRandom.MersenneTwisterFactory.class )
				.withProvider( ProbabilityDistribution.Factory.class,
						Math3ProbabilityDistribution.Factory.class )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )
				.build().createBinder()
				.inject( ProbabilityDistribution.Factory.class );

	}

	@SuppressWarnings( "deprecation" )
	@Test
	public void read37975() throws Exception
	{
		LOG.info( "start 37975" );

		// async, one JSON object per iteration, observable handles auto-close
		final List<WeightedValue<Tuple>> async = Cbs37975json
				.readAsync( () -> FileUtil.toInputStream( CBS_37975_FILE ) )
				.toList().blockingGet();

		// sync, takes memory for all JSON objects, and must manage auto-close
		final List<WeightedValue<Tuple>> sync = Cbs37975json
				.readSync( () -> FileUtil.toInputStream( CBS_37975_FILE ) )
				.collect( Collectors.toList() );
		assertThat( "async==sync", async, equalTo( sync ) );

		final ProbabilityDistribution<Tuple> dist = distFact
				.createCategorical( async );
		for( int i = 0; i < 10; i++ )
			LOG.trace( "Draw #{}: hh[type, ref-age]: {}", i, dist.draw() );

		LOG.info( "done 37975" );
	}

	@SuppressWarnings( "deprecation" )
	@Test
	public void read71486ned() throws Exception
	{
		LOG.info( "start 71486ned" );

		final CBSRegionType regType = CBSRegionType.MUNICIPAL;

		// async, one JSON object per iteration, observable handles auto-close
		final List<WeightedValue<Cbs71486json.MyTuple>> async = Cbs71486json
				.readAsync( () -> FileUtil.toInputStream( CBS_71486_FILE ) )
				.filter( wv -> wv.getValue().regionType() == regType ).toList()
				.blockingGet();

		// sync, takes memory for all JSON objects, and must manage auto-close
		final List<WeightedValue<Cbs71486json.MyTuple>> sync = Cbs71486json
				.readSync( () -> FileUtil.toInputStream( CBS_71486_FILE ) )
				.filter( wv -> wv.getValue().regionType() == regType )
				.collect( Collectors.toList() );
		assertThat( "async==sync", async, equalTo( sync ) );

		final ProbabilityDistribution<Cbs71486json.MyTuple> dist = distFact
				.createCategorical( async );
		for( int i = 0; i < 10; i++ )
		{
			final Cbs71486json.MyTuple tuple = dist.draw();
			LOG.trace( "Draw #{}: {}, age draw: {}", i, tuple, QuantityUtil
					.toScale( tuple.ageDist( distFact ).draw(), 2 ) );
		}

		LOG.info( "done 71486ned" );
	}

	@Test
	public void read37230ned()
	{
		LOG.info( "start 37230ned" );

		final CBSRegionType regType = CBSRegionType.MUNICIPAL;

		// Java8 Time conversions: http://stackoverflow.com/a/23197731/1418999
		final Range<ZonedDateTime> timeRange = Range
				.of( "2012-06-13", "2014-02-13" ).map( LocalDate::parse )
				.map( dt -> dt.atStartOfDay( Cbs37230jsonSeries.NL_TZ ) );
		final Map<ZonedDateTime, Collection<WeightedValue<Cbs37230jsonSeries.MyTuple>>> async = Cbs37230jsonSeries
				.readAsync( () -> FileUtil.toInputStream( CBS_37230_FILE ),
						Cbs37230jsonSeries.CBSPopulationChange.BIRTHS,
						timeRange )
				.filter( wv -> wv.getValue().regionType() == regType )
				.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
						() -> new TreeMap<>() )
				.blockingGet();

//		LOG.trace( "parsed birth values, weighted by region: {}",
//				JsonUtil.toJSON( async ) );
		final ConditionalDistribution<Cbs37230jsonSeries.MyTuple, ZonedDateTime> birthRegionDist = ConditionalDistribution
				.of( distFact::createCategorical, async );

		final Cbs37230jsonSeries.CBSPopulationChange metric = Cbs37230jsonSeries.CBSPopulationChange.BIRTHS;

		for( ZonedDateTime dt = timeRange.getLower().getValue(); timeRange
				.contains( dt ); dt = dt.plus( 1L, ChronoUnit.WEEKS ) )
		{
			Cbs37230jsonSeries.MyTuple tuple = birthRegionDist.draw( dt );
			for( int i = 0; i < 10; i++ )
				LOG.trace( "dt={} profile={}, draw #{}: {}", dt, tuple, i,
						QuantityUtil.toScale(
								tuple.timeDist( metric,
										distFact::createExponential ).draw(),
								3 ) );
		}
//		distFact.createCon

		LOG.info( "done 37230ned" );
	}

	@Test
	public void read37713()
	{
		LOG.info( "start 37713" );

		final CBSRegionType regionType = CBSRegionType.MUNICIPAL;
		final ConditionalDistribution<Cbs37713json.MyTuple, CBSGender> dist = ConditionalDistribution
				.of( distFact::createCategorical, Cbs37713json.readAsync(
						() -> FileUtil.toInputStream( CBS_37713_FILE ) ).filter(
								wv -> wv.getValue().regionType() == regionType )
						.toMultimap( wv -> wv.getValue().gender(), wv -> wv,
								() -> new EnumMap<>( CBSGender.class ) )
						.blockingGet() );

		for( int i = 0; i < 10; i++ )
		{
			final int round = i;
			Arrays.stream( CBSGender.values() )
					.map( gender -> dist.draw( gender ) )
					.forEach( tuple -> LOG.trace( "Draw #{}, {}, {}", round,
							tuple, QuantityUtil.toScale( tuple
									.ageDist(
											distFact::createUniformContinuous )
									.draw(), 3 ) ) );
		}

		LOG.info( "done 37713" );
	}
}
