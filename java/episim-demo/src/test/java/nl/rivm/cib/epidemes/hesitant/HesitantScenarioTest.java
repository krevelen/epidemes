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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.ujmp.core.SparseMatrix;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.bind.LocalBinder;
import io.coala.enterprise.Actor;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.name.Identified;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.random.PseudoRandom.Name;
import io.coala.util.FileUtil;
import nl.rivm.cib.episim.hesitant.HesitantScenarioConfig;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy;

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

	/**
	 * {@link VaccineStatus} is a possible vaccination status
	 */
	public static enum VaccineStatus
	{
		all, some, none;
	}

	/**
	 * {@link DistType} is a type of (R-fitted) distribution
	 */
	public static enum DistType
	{
		weibull;
	}

	/**
	 * {@link DistParam} is a (R-fitted) distribution parameter name
	 */
	public static enum DistParam
	{
		shape, scale;
	}

	/**
	 * {@link DistParams} describes and instantiates a (R-fitted) distribution
	 * of attitude values
	 */
	public static class DistParams
	{
		/** the fitted distribution type */
		public DistType type;

		/** the minimum observable value */
		public Double min;
		/** the maximum observable value */
		public Double max;
		/** the observable value range */

		/** the distribution parameter estimate */
		public Map<DistParam, BigDecimal> est;
		/** the distribution parameter error (standard deviation) */
		public Map<DistParam, BigDecimal> sd;

		/** the distribution */

		@Override
		public String toString()
		{
			return JsonUtil.stringify( this );
		};

		public ProbabilityDistribution<Double>
			createDist( final ProbabilityDistribution.Factory distFact )
		{
			// avoid compl==conf, e.g. min: .5 -> .505,  max: .5 -> .497
			final Range<Double> range = Range.of( this.min * 1.01,
					Math.pow( this.max, 1.01 ) );
			return distFact
					.createWeibull( this.est.get( DistParam.shape ),
							this.est.get( DistParam.scale ) )
					.map( range::crop );

		}
	}

	/**
	 * {@link HesitancyDimension} is a 3C/4C dimension of attitude
	 */
	public enum HesitancyDimension
	{
		complacency, confidence, attitude, calculation;
	}

	public static class HesitancyProfile
		extends Identified.SimpleOrdinal<String>
	{
		public static HesitancyProfile of( final String id )
		{
			return of( new HesitancyProfile(), id );
		}

		/** the initial religious persuasion */
		public boolean religious;
		/** the initial alternative medicine persuasion */
		public boolean alternative;
		/** the initial vaccination status */
		public VaccineStatus status;
		/** the original profile N */
		public int count;
		/** the original profile density/fraction */
		public BigDecimal fraction;
		/** the initial attitude distributions per hesitancy dimension */
		@JsonProperty( "distributions" )
		public EnumMap<HesitancyDimension, DistParams> distParams;

		@Override
		public String id()
		{
			return this.id == null
					? (this.id = (this.religious ? "Reli" : "Sec") + "|"
							+ (this.alternative ? "Alto" : "Reg") + "|"
							+ this.status)
					: this.id;
		}
	}

	public interface Social
	{

	}

	public static class HesitantIndividual implements Social, VaxHesitancy
	{

		private EnumMap<HesitancyDimension, Number> vaxAtt = new EnumMap<>(
				HesitancyDimension.class );

		private Map<Actor.ID, Number> opinionatorAppreciation = new HashMap<>();

		private Number DEFAULT_APPRECIATION = BigDecimal.valueOf( 5, 1 );

		// TODO calc att

		@Override
		public Number getComplacency()
		{
			return this.vaxAtt.get( HesitancyDimension.complacency );
		}

		@Override
		public Number getConfidence()
		{
			return this.vaxAtt.get( HesitancyDimension.confidence );
		}

		@Override
		public Number getCalculation()
		{
			return this.vaxAtt.get( HesitancyDimension.calculation );
		}

		@Override
		public void setCalculation( final Number calculation )
		{
			this.vaxAtt.put( HesitancyDimension.calculation, calculation );
		}

		@Override
		public Number getAppreciation( final Actor.ID sourceRef )
		{
			return this.opinionatorAppreciation.computeIfAbsent( sourceRef,
					id -> DEFAULT_APPRECIATION );
		}

		@Override
		public void observe( final Actor.ID sourceRef, final Number complacency,
			final Number confidence )
		{
			// TODO handle opinion

		}

	}

	@Test
	public void matrixTest() throws IOException, InterruptedException
	{
		final HesitantScenarioConfig conf = HesitantScenarioConfig
				.getOrFromYaml();
		LOG.info( "Starting matrix test with config: {}", conf.toYAML() );
		final LocalBinder binder = conf.createBinder();
		final PseudoRandom rnd = //JavaRandom.Factory.instance().create();
				binder.inject( PseudoRandom.Factory.class )
						.create( Name.of( "rng" ), 1L );

		final int numInd = 100; // 1Mx1M fits in normal heap size
		final int numLoc = 100; // 1Mx1M fits in normal heap size

//		final LocalId ctx = LocalId.of( new UUID() );
//		final List<Actor.ID> ids = IntStream.range( 0, n )
//				.mapToObj( Integer::toString ).map( i -> Actor.ID.of( i, ctx ) )
//				.collect( Collectors.toList() );

		// create a very large sparse matrix
		SparseMatrix pos = SparseMatrix.Factory.zeros( numInd, numLoc );
		LOG.trace( "Position matrix size: {}", pos.getSize() );

		for( int ind = 0; ind < numInd; ind++ )
		{
			int loc = rnd.nextInt( numLoc );
			double dur = rnd.nextDouble();
			pos.setAsBigDecimal( BigDecimal.valueOf( dur ), ind, loc );
		}

		// set diagonal (i.e. self-appreciation)
//		for( int i = n - 1; i >= 0; i-- )
//		{
//			pos.setAsBigDecimal( BigDecimal.ONE, i, i );
//		}

		LOG.trace( "Positions:\n{}", pos );

//		final CountDownLatch latch = new CountDownLatch( 1 );
//		final JFrame gui = m1.showGUI();
//		gui.addWindowListener( new WindowAdapter()
//		{
//			public void windowClosing( final WindowEvent evt )
//			{
//				latch.countDown();
//			}
//		} );
//		gui.addli
//		latch.await();
		// basic calculations
//		Matrix transpose = dense.transpose();
//		Matrix sum = dense.plus(sparse);
//		Matrix difference = dense.minus(sparse);
//		Matrix matrixProduct = dense.mtimes(sparse);
//		Matrix scaled = dense.times(2.0);
//
//		Matrix inverse = dense.inv();
//		Matrix pseudoInverse = dense.pinv();
//		double determinant = dense.det();
//
//		Matrix[] singularValueDecomposition = dense.svd();
//		Matrix[] eigenValueDecomposition = dense.eig();
//		Matrix[] luDecomposition = dense.lu();
//		Matrix[] qrDecomposition = dense.qr();
//		Matrix choleskyDecomposition = dense.chol();
	}

	@Test
	public void configTest() throws IOException
	{
//		LOG.trace( "Java props: {}", YamlUtil.toYAML( new Date().toString(),
//				ConfigUtil.expand( System.getProperties() ) ) );
		final HesitantScenarioConfig conf = HesitantScenarioConfig
				.getOrFromYaml();
		LOG.trace( "Start with config: {}", conf.toYAML() );
		final LocalBinder binder = conf.createBinder();
		final ProbabilityDistribution.Factory distFact = binder
				.inject( ProbabilityDistribution.Factory.class );

		final HesitancyProfile[] profiles = JsonUtil.valueOf(
				FileUtil.toInputStream( "hesitancy-dists.json" ),
				HesitancyProfile[].class );
		final Map<HesitancyProfile, Map<HesitancyDimension, ProbabilityDistribution<Double>>> dists = //new HashMap<>();
				Arrays.stream( profiles ).collect( Collectors.toMap( p -> p,
						p -> p.distParams.entrySet().stream()
								.collect( Collectors.toMap( e -> e.getKey(),
										e -> e.getValue()
												.createDist( distFact ) ) ) ) );

		final List<WeightedValue<HesitancyProfile>> profileDensity = Arrays
				.stream( profiles )
				.map( p -> WeightedValue.of( p, p.fraction ) )
				.collect( Collectors.toList() );
		LOG.trace( "Dists: {}", profileDensity );
		final ProbabilityDistribution<HesitancyProfile> profileDist = distFact
				.createCategorical( profileDensity );

		int i = 0, j = 0, k = 0;
		for( ; i < 100000; i++ )
		{
			final HesitancyProfile hes = profileDist.draw();
			final Map<HesitancyDimension, Double> draws = dists.get( hes )
					.entrySet().stream().collect( Collectors.toMap(
							e -> e.getKey(), e -> e.getValue().draw() ) );
			final Double attNew = (draws.get( HesitancyDimension.confidence )
					+ 1 - draws.get( HesitancyDimension.complacency )) / 2;
			draws.put( HesitancyDimension.attitude, attNew );
			if( hes.status == VaccineStatus.some )
			{
				j++;
				if( attNew >= .5 ) k++;
			}
		}
		LOG.info( "{} ({}%) status {}, of which {} ({}%) positive", j,
				DecimalUtil.toScale( DecimalUtil.multiply( j, 100. / i ), 1 ),
				VaccineStatus.some, k,
				DecimalUtil.toScale( DecimalUtil.multiply( k, 100. / j ), 1 ) );
	}
}
