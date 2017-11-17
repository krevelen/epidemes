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

import java.io.IOException;
import java.text.ParseException;
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
import org.junit.Ignore;
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
import io.coala.time.Instant;
import io.coala.time.Timing;
import io.coala.util.FileUtil;
import io.reactivex.internal.functions.Functions;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.cbs.TimeUtil;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.locate.ZipCode;

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
	private static final String CBS_37201_FILE = "cbs/37201_TS_2010_2015.json";
	private static final String CBS_83287_FILE = "cbs/83287NED.json";

	private static ProbabilityDistribution.Factory distFact = null;
	private static Range<LocalDate> timeRange = null;

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
				.map( LocalDate::parse );
	}

	@Test
	public void testTiming() throws ParseException
	{
		final ZonedDateTime dt = LocalDate.parse( "2012-01-01" )
				.atStartOfDay( TimeUtil.NL_TZ );
		for( Instant t : Timing.of( "0 0 * * * ?" ).offset( dt ).max( 10L )
				.iterate( Instant.ZERO ) )
			LOG.trace( "t={}, dt={} (offset: {} -> {})", t, t.prettify( dt ),
					dt, dt.toInstant() );
	}

	@SuppressWarnings( "deprecation" )
	@Ignore
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
		final Map<LocalDate, Collection<WeightedValue<Cbs71486json.Category>>> async = Cbs71486json
				.readAsync( () -> FileUtil.toInputStream( CBS_71486_FILE ),
						timeRange )
				.filter( wv -> wv.getValue().regionType() == regType )
				.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
						() -> new TreeMap<>() )
				.blockingGet();
		LOG.trace( "Got async: {}",
				async.entrySet().stream().collect( Collectors
						.toMap( e -> e.getKey(), e -> e.getValue().size() ) ) );

		final ConditionalDistribution<Cbs71486json.Category, LocalDate> dist = ConditionalDistribution
				.of( distFact::createCategorical, async );

		for( LocalDate dt = timeRange.lowerValue(); timeRange
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

		final Map<Region.ID, ProbabilityDistribution<CbsNeighborhood>> async = CbsNeighborhood
				.readAsync( () -> FileUtil.toInputStream( CBS_PC6_FILE ) )
				.toMultimap( bu -> bu.municipalRef(),
						CbsNeighborhood::toWeightedValue )
				.blockingGet().entrySet().parallelStream() // blocking
				.collect( Collectors.toMap( e -> e.getKey(),
						e -> distFact.createCategorical(
								// consume WeightedValue's for garbage collector
								e.getValue() ) ) );

		final ConditionalDistribution<CbsNeighborhood, Region.ID> dist = ConditionalDistribution
				.of( async::get );
		// region: not weighted, draw from another (weighted) distribution?
		final Region.ID regRef = distFact.getStream()
				.nextElement( async.keySet() );
		for( int i = 0; i < 10; i++ )
		{
			// borough: weighted by address count
			final CbsNeighborhood buurt = dist.draw( regRef );
			// zip: weighted by address count
			final ZipCode zip = buurt.zipDist( distFact::createCategorical )
					.draw();
			LOG.trace( "draw #{}: reg: {} of {}, codes: {}, ref: {}, zip: {}",
					i, regRef, async.size(), buurt.codes,
					buurt.neighbourhoodRef(), zip );
		}

		LOG.info( "done BoroughPC6" );
	}

	@Test
	public void read37230ned()
	{
		LOG.info( "start 37230ned" );

		final CBSRegionType regType = CBSRegionType.MUNICIPAL;

		// Java8 Time conversions: http://stackoverflow.com/a/23197731/1418999
		final Map<LocalDate, Collection<WeightedValue<Cbs37230json.Category>>> async = Cbs37230json
				.readAsync( () -> FileUtil.toInputStream( CBS_37230_FILE ),
						timeRange, CBSPopulationDynamic.EMIGRATION )
				.filter( wv -> wv.getValue().regionType() == regType )
				.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
						() -> new TreeMap<>() )
				.blockingGet();

//		LOG.trace( "parsed birth values, weighted by region: {}",
//				JsonUtil.toJSON( async ) );
		final ConditionalDistribution<Cbs37230json.Category, LocalDate> birthRegions = ConditionalDistribution
				.of( distFact::createCategorical, async );

		for( LocalDate dt = timeRange.lowerValue(); timeRange
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
		final Map<Region.ID, Collection<WeightedValue<Cbs37713json.Category>>> val = Cbs37713json
				.readAsync( () -> FileUtil.toInputStream( CBS_37713_FILE ),
						timeRange )
				.filter( wv -> wv.getValue().regionType() == regionType )
				.toMultimap( wv -> Region.ID.of( wv.getValue().regionRef() ),
						wv -> wv,
						// Navigable Map for out-of-bounds conditions
						TreeMap::new )
				.blockingGet();
		final ConditionalDistribution<Cbs37713json.Category, Region.ID> dist = ConditionalDistribution
				.of( distFact::createCategorical, val );

		for( int r = 0; r < 10; r++ )
		{
			final Region.ID regRef = distFact.getStream()
					.nextElement( val.keySet() );
			final Cbs37713json.Category tuple = dist.draw( regRef );
			for( int i = 0; i < 10; i++ )
				LOG.trace( "draw #{}: {} -> age {}", i, tuple,
						QuantityUtil.pretty( tuple
								.ageDist( distFact::createUniformContinuous )
								.draw(), 2 ) );
		}

		LOG.info( "done 37713" );
	}

	@Test
	public void read37201()
	{
		LOG.info( "start 37201" );

		final CBSRegionType regionType = CBSRegionType.MUNICIPAL;

		final ConditionalDistribution<Cbs37201json.Category, RegionPeriod> dist = ConditionalDistribution
				.of( distFact::createCategorical, Cbs37201json.readAsync(
						() -> FileUtil.toInputStream( CBS_37201_FILE ),
						timeRange )
						.filter(
								wv -> wv.getValue().regionType() == regionType )
						.toMultimap( wv -> wv.getValue().regionPeriod(),
								Functions.identity(),
								// Navigable Map for out-of-bounds conditions
								TreeMap::new )
						.blockingGet() );

		final String amsterdam = "GM0363";
		for( LocalDate dt = timeRange.lowerValue(); timeRange
				.contains( dt ); dt = dt.plus( 1L, ChronoUnit.WEEKS ) )
		{
			final Cbs37201json.Category tuple = dist
					.draw( RegionPeriod.of( amsterdam, dt ) );
			for( int i = 0; i < 3; i++ )
				LOG.trace(
						"dt={} cat={}, draw #{}: boy={}, mom={}, siblings={}",
						dt, tuple, i,
						tuple.genderDist( distFact::createCategorical ).draw()
								.isMale(),
						tuple.ageDist( distFact::createCategorical ).draw()
								.range(),
						tuple.rankDist( distFact::createCategorical ).draw()
								.rank() );
		}

		LOG.info( "done 37201" );
	}

	@Test
	public void read83287() throws IOException
	{
		LOG.info( "start 83287" );
		final Cbs83287Json json = Cbs83287Json
				.parse( FileUtil.toInputStream( CBS_83287_FILE ) );
		LOG.trace( "Parsed '{}' -> \nfrom-gm: {}\nto-gm: {}", CBS_83287_FILE,
				json.toMap(), json.toReverseMap() );

		LOG.info( "done 83287" );
	}
}
