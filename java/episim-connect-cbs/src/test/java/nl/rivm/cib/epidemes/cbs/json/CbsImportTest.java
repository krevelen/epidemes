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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.WeightedValue;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.util.FileUtil;
import nl.rivm.cib.episim.model.locate.Region;

/**
 * {@link CbsImportTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class CbsImportTest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( CbsImportTest.class );

	private static final String CBS_37713_FILE = "cbs/37713_2016jj00.json";

	@Test
	public void read37713() throws IOException
	{
		LOG.info( "start 37713" );

		// load scenario
		final LocalBinder binder = LocalConfig.builder()
				.withProvider( PseudoRandom.Factory.class,
						Math3PseudoRandom.MersenneTwisterFactory.class )
				.withProvider( ProbabilityDistribution.Factory.class,
						Math3ProbabilityDistribution.Factory.class )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )
				.build().createBinder();

		final ProbabilityDistribution.Factory distFact = binder
				.inject( ProbabilityDistribution.Factory.class );

		try( final InputStream is = FileUtil.toInputStream( CBS_37713_FILE ) )
		{
			final Cbs37713json[] entries = JsonUtil.valueOf( is,
					Cbs37713json[].class );
			final Map<CBSRegionType, List<WeightedValue<Cbs37713json.Tuple>>> maleAges = new HashMap<>();
			Arrays.stream( entries ).filter( entry -> entry.males > 0 )
					.map( entry -> WeightedValue.of( entry.toTuple( distFact ),
							entry.males ) )
					.map( wv -> maleAges
							.computeIfAbsent( wv.getValue().regionType,
									key -> new ArrayList<>() )
							.add( wv ) )
					.reduce( Boolean::logicalAnd ).orElse( Boolean.FALSE );

			final Map<Region.TypeID, ProbabilityDistribution<Cbs37713json.Tuple>> maleAgeDists = maleAges
					.entrySet().parallelStream()
					.collect( Collectors.toMap( e -> e.getKey().toTypeID(),
							e -> distFact.createCategorical( e.getValue() ) ) );

			final Map<CBSRegionType, List<WeightedValue<Cbs37713json.Tuple>>> femaleAges = new HashMap<>();
			Arrays.stream( entries ).filter( entry -> entry.females > 0 )
					.map( entry -> WeightedValue.of( entry.toTuple( distFact ),
							entry.females ) )
					.map( wv -> femaleAges
							.computeIfAbsent( wv.getValue().regionType,
									key -> new ArrayList<>() )
							.add( wv ) )
					.reduce( Boolean::logicalAnd ).orElse( Boolean.FALSE );

			final Map<Region.TypeID, ProbabilityDistribution<Cbs37713json.Tuple>> femaleAgeDists = femaleAges
					.entrySet().parallelStream()
					.collect( Collectors.toMap( e -> e.getKey().toTypeID(),
							e -> distFact.createCategorical( e.getValue() ) ) );

			for( int i = 0; i < 10; i++ )
			{
				final int round = i;
				maleAgeDists.entrySet().stream().forEach( e ->
				{
					final Cbs37713json.Tuple tuple = e.getValue().draw();
					LOG.trace( "Round {}, {}: {}, {} female age: {}", round,
							e.getKey(), tuple.regionId,
							tuple.alien ? "migrant" : "Dutch",
							QuantityUtil.toScale( tuple.ageDist.draw(), 2 ) );
				} );
				femaleAgeDists.entrySet().stream().forEach( e ->
				{
					final Cbs37713json.Tuple tuple = e.getValue().draw();
					LOG.trace( "Round {}, {}: {}, {} female age: {}", round,
							e.getKey(), tuple.regionId,
							tuple.alien ? "migrant" : "Dutch",
							QuantityUtil.toScale( tuple.ageDist.draw(), 2 ) );
				} );
			}
		}
		LOG.info( "done 37713" );
	}
}
