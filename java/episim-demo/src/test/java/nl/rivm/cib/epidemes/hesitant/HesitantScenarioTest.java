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
package nl.rivm.cib.epidemes.hesitant;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.coala.bind.LocalBinder;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.coala.util.FileUtil;
import nl.rivm.cib.episim.hesitant.HesitantScenarioConfig;

/**
 * {@link HesitantScenarioTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class HesitantScenarioTest
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( HesitantScenarioTest.class );

	public static enum VaccineStatus
	{
		all, some, none;
	}

	public static enum DistType
	{
		weibull;
	}

	public static enum DistParam
	{
		shape, scale;
	}

	public static class HesitancyDist
	{
		public DistType type;

		public Double min;
		public Double max;
		@JsonIgnore
		public Range<Double> range;

		public Map<DistParam, BigDecimal> est;
		public Map<DistParam, BigDecimal> sd;
		@JsonIgnore
		public ProbabilityDistribution<Double> dist;

		@Override
		public String toString()
		{
			return JsonUtil.stringify( this );
		};
	}

	public static class HesitancyProfile
	{
		public boolean religious;
		public boolean alternative;
		public VaccineStatus status;
		public int count;
		public BigDecimal fraction;
		public Map<String, HesitancyDist> distributions;

		@Override
		public String toString()
		{
			return JsonUtil.stringify( this );
		};
	}

	@Test
	public void configTest() throws IOException
	{
//		LOG.trace( "Java props: {}", YamlUtil.toYAML( new Date().toString(),
//				ConfigUtil.expand( System.getProperties() ) ) );
		final HesitantScenarioConfig conf = HesitantScenarioConfig
				.getOrFromYaml();
		LOG.trace( "Start with config: {}", conf.toYAML() );
		final HesitancyProfile[] profiles = JsonUtil.valueOf(
				FileUtil.toInputStream( "hesitancy-dists.json" ),
				HesitancyProfile[].class );
		final LocalBinder binder = conf.createBinder();
		final ProbabilityDistribution.Factory distFact = binder
				.inject( ProbabilityDistribution.Factory.class );
		Arrays.stream( profiles )
				.forEach( p -> p.distributions.values().forEach( params ->
				{
					// avoid compl==conf; min: .5 -> .505,  max: .5 -> .497
					params.range = Range.of( params.min * 1.01,
							Math.pow( params.max, 1.01 ) );
					params.dist = distFact
							.createWeibull( params.est.get( DistParam.shape ),
									params.est.get( DistParam.scale ) )
							.map( params.range::crop );
				} ) );
		final List<WeightedValue<HesitancyProfile>> profileDensity = Arrays
				.stream( profiles )
				.map( p -> WeightedValue.of( p, p.fraction ) )
				.collect( Collectors.toList() );
		LOG.trace( "Dists: {}", profileDensity );
		final ProbabilityDistribution<HesitancyProfile> profileDist = distFact
				.createCategorical( profileDensity );

		int i = 0, j = 0, k = 0;
		for( ; i < 1000; i++ )
		{
			final HesitancyProfile hes = profileDist.draw();
			final Map<String, Double> draws = hes.distributions.entrySet()
					.stream().collect( Collectors.toMap( e -> e.getKey(),
							e -> ((HesitancyDist) e.getValue()).dist.draw() ) );
			final Double attNew = (draws.get( "confidence" ) + 1
					- draws.get( "complacency" )) / 2;
			draws.put( "attitude", attNew );
			if( hes.status == VaccineStatus.some )
			{
				j++;
				if( attNew >= .5 ) k++;
				LOG.trace(
						"Draw {}, status={}, reli={}, alto={}, attDiff={}, dist-draws={}",
						i, hes.status, hes.religious ? "Y" : "N",
						hes.alternative ? "Y" : "N", draws );
			}
		}
		LOG.trace( "{} of {} draws undecided, {} ({}%) positive", j, i, k,
				100. * k / j );
	}
}
