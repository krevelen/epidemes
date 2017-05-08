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
import java.util.Collection;
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
import nl.rivm.cib.episim.model.ZipCode;
import nl.rivm.cib.episim.model.locate.Region;

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

	private static final String CBS_PC6_FILE = "cbs/pc6_buurt.json";

	// households
	private static final String CBS_37975_FILE = "cbs/37975_2016JJ00.json";
	private static final String CBS_71486_FILE = "cbs/71486ned-TS-2010-2016.json";

	private static final String CBS_37230_FILE = "cbs/37230ned_TS_2012_2017.json";
	private static final String CBS_37713_FILE = "cbs/37713_2012JJ00_AllOrigins.json";

	private static ProbabilityDistribution.Factory distFact = null;
	private static Range<ZonedDateTime> timeRange = null;

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
		timeRange = Range.of( "2012-06-13", "2014-02-13" )
				// Range.upFromAndIncluding( "2012-06-13" ) 
				.map( LocalDate::parse )
				.map( dt -> dt.atStartOfDay( TimeUtil.NL_TZ ) );
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

	@Test
	public void read71486ned() throws Exception
	{
		LOG.info( "start 71486ned" );

		final CBSRegionType regType = CBSRegionType.MUNICIPAL;

		// async, one JSON object per iteration, observable handles auto-close
		final Map<ZonedDateTime, Collection<WeightedValue<Cbs71486json.Category>>> async = Cbs71486json
				.readAsync( () -> FileUtil.toInputStream( CBS_71486_FILE ),
						timeRange )
				.filter( wv -> wv.getValue().regionType() == regType )
				.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
						() -> new TreeMap<>() )
				.blockingGet();
		LOG.trace( "Got async: {}",
				async.entrySet().stream().collect(
						Collectors.toMap( e -> e.getKey().toLocalDate(),
								e -> e.getValue().size() ) ) );

		final ConditionalDistribution<Cbs71486json.Category, ZonedDateTime> dist = ConditionalDistribution
				.of( distFact::createCategorical, async );

		for( ZonedDateTime dt = timeRange.lowerValue(); timeRange
				.contains( dt ); dt = dt.plus( 3L, ChronoUnit.WEEKS ) )
		{
			final Cbs71486json.Category tuple = dist.draw( dt );
			for( int i = 0; i < 5; i++ )
				LOG.trace( "t={}, draw #{}: {}, hh-type draw: {}, age draw: {}",
						dt, i, tuple,
						tuple.hhTypeDist( distFact::createCategorical ).draw(),
						QuantityUtil.toScale( tuple
								.ageDist( distFact::createUniformContinuous )
								.draw(), 2 ) );
		}

		LOG.info( "done 71486ned" );
	}

	@Test
	public void readBoroughPC6()
	{
		LOG.info( "start BoroughPC6" );

		// TODO use groupBy operator somehow using flatMap and keep regId key?
//		final Observable<ProbabilityDistribution<CbsBoroughPC6json>> async = CbsBoroughPC6json
//				.readAsync( () -> FileUtil.toInputStream( CBS_PC6_FILE ) )
//				.groupBy( bu -> bu.municipalRef() )
//				.flatMap( gr -> distFact
//						.createCategorical(
//								gr.map( CbsBoroughPC6json::toWeightedValue ) )
//						.map( dist -> Collections.entry( gr.getKey(),
//								dist ) ) );

		final Map<Region.ID, ProbabilityDistribution<CbsBoroughPC6json>> async = CbsBoroughPC6json
				.readAsync( () -> FileUtil.toInputStream( CBS_PC6_FILE ) )
				.toMultimap( bu -> bu.municipalRef(),
						CbsBoroughPC6json::toWeightedValue )
				.blockingGet().entrySet().parallelStream() // blocking
				.collect( Collectors.toMap( e -> e.getKey(),
						e -> distFact.createCategorical(
								// consume WeightedValue's for garbage collector
								e.getValue() ) ) );

		final ConditionalDistribution<CbsBoroughPC6json, Region.ID> dist = ConditionalDistribution
				.of( async::get );
		// region: not weighted, draw from another (weighted) distribution?
		final Region.ID regRef = distFact.getStream()
				.nextElement( async.keySet() );
		for( int i = 0; i < 10; i++ )
		{
			// borough: weighted by address count
			final CbsBoroughPC6json buurt = dist.draw( regRef );
			// zip: weighted by address count
			final ZipCode zip = buurt.zipDist( distFact::createCategorical )
					.draw();
			LOG.trace( "draw #{}: reg: {} of {}, codes: {}, ref: {}, zip: {}",
					i, regRef, async.size(), buurt.codes, buurt.ref(), zip );
		}

		LOG.info( "done BoroughPC6" );
	}

	@Test
	public void read37230ned()
	{
		LOG.info( "start 37230ned" );

		final CBSRegionType regType = CBSRegionType.MUNICIPAL;

		// Java8 Time conversions: http://stackoverflow.com/a/23197731/1418999
		final Map<ZonedDateTime, Collection<WeightedValue<Cbs37230json.Category>>> async = Cbs37230json
				.readAsync( () -> FileUtil.toInputStream( CBS_37230_FILE ),
						CBSPopulationDynamic.EMIGRATION, timeRange )
				.filter( wv -> wv.getValue().regionType() == regType )
				.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
						() -> new TreeMap<>() )
				.blockingGet();

//		LOG.trace( "parsed birth values, weighted by region: {}",
//				JsonUtil.toJSON( async ) );
		final ConditionalDistribution<Cbs37230json.Category, ZonedDateTime> birthRegions = ConditionalDistribution
				.of( distFact::createCategorical, async );

		for( ZonedDateTime dt = timeRange.lowerValue(); timeRange
				.contains( dt ); dt = dt.plus( 1L, ChronoUnit.WEEKS ) )
		{
			final Cbs37230json.Category tuple = birthRegions.draw( dt );
			for( int i = 0; i < 10; i++ )
				LOG.trace( "dt={} cat={}, draw #{}: {}", dt, tuple, i,
						QuantityUtil.toScale( tuple
								.timeDist( distFact::createExponential ).draw(),
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
		final ConditionalDistribution<Cbs37713json.Category, ZonedDateTime> dist = ConditionalDistribution
				.of( distFact::createCategorical, Cbs37713json.readAsync(
						() -> FileUtil.toInputStream( CBS_37713_FILE ),
						timeRange ).filter(
								wv -> wv.getValue().regionType() == regionType )
						.toMultimap( wv -> wv.getValue().offset(), wv -> wv )
						.blockingGet() );

		for( ZonedDateTime dt = timeRange.lowerValue(); timeRange
				.contains( dt ); dt = dt.plus( 1L, ChronoUnit.WEEKS ) )
		{
			final Cbs37713json.Category tuple = dist.draw( dt );
			for( int i = 0; i < 10; i++ )
				LOG.trace( "dt={} cat={}, draw #{}: {} {}", dt, tuple, i,
						tuple.genderDist( distFact::createCategorical ).draw(),
						QuantityUtil.toScale( tuple
								.ageDist( distFact::createUniformContinuous )
								.draw(), 3 ) );
		}

		LOG.info( "done 37713" );
	}
}
