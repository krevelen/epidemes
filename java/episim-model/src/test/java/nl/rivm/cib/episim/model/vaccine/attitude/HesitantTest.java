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
package nl.rivm.cib.episim.model.vaccine.attitude;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;

import io.coala.enterprise.Actor;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.MatrixUtil;
import nl.rivm.cib.episim.model.person.Attitude;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy.SocialFactors;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy.MatrixWeightedAverager;

/**
 * {@link HesitantTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class HesitantTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( HesitantTest.class );

//	@Test
//	public void dateParseTest() throws ParseException
//	{
//		LOG.trace( "parsed: {}", new SimpleDateFormat( "M/d/yyyy H:mm:ss a" )
//				.parse( "9/26/2015 12:33:21 PM" ) );
//	}

	/**
	 * Test the (default) {@link VaxHesitancy} behaviors
	 */
	@Test
	public void testMatrixAttitude()
	{
		LOG.info( "Started test of {}", Attitude.class.getSimpleName() );
		final int factorIndex = 0;
		final SparseMatrix weights = SparseMatrix.Factory.zeros( 2, 2 );
		final SparseMatrix factors = SparseMatrix.Factory.zeros( 2, 1 );
		weights.setLabel( "weights" );
		factors.setLabel( "factors" );
		factors.setColumnLabel( factorIndex, "factorA" );
		final int n = 2;
		final List<Attitude<Object>> attitudes = new ArrayList<>();
		for( int i = 0; i < n; i++ )
		{
			weights.setRowLabel( i, "ind" + i );
			weights.setColumnLabel( i, "w" + i );
			weights.setAsBigDecimal( BigDecimal.ONE, i, i );
			factors.setRowLabel( i, "v" + i );
			factors.setAsBigDecimal( BigDecimal.ONE, i, factorIndex );
			attitudes.add( Attitude.of( factors, weights, i, ( o,
				v ) -> v.getAsBigDecimal( 0, factorIndex ).signum() >= 0 ) );
		}

		LOG.trace( "Setup:\n{}\n{}\n{}\n{}", factors, weights, attitudes,
				attitudes.get( 0 ).isPositive( null ) );

		LOG.info( "Completed test of {}", Attitude.class.getSimpleName() );
	}

	@Test
	public void testMatrixHesitancy() throws IOException, InterruptedException
	{
//		final HesitantScenarioConfig conf = HesitantScenarioConfig
//				.getOrFromYaml();
//		LOG.info( "Starting matrix test with config: {}", conf.toYAML() );
//		final LocalBinder binder = conf.createBinder();
//		final PseudoRandom rnd = //JavaRandom.Factory.instance().create();
//				binder.inject( PseudoRandom.Factory.class )
//						.create( Name.of( "rng" ), 1L );

		LOG.info( "Started test of {}",
				MatrixWeightedAverager.class.getSimpleName() );
		final long n = 10;
		final Matrix opinions = Matrix.Factory.zeros( n, 2 );
		final Matrix pressure = Matrix.Factory.zeros( n, n );
		// partition/neighborhood 1
		MatrixUtil.insertBigDecimal( pressure,
				SparseMatrix.Factory.ones( n / 2, n / 2 ), 0, 0 );
		// partition/neighborhood 2
		MatrixUtil.insertBigDecimal( pressure,
				SparseMatrix.Factory.ones( n / 2, n / 2 ), n / 2, n / 2 );
		pressure.setLabel( "Pressure" );
		opinions.setLabel( "Opinion" );
		Arrays.stream( SocialFactors.values() ).forEach(
				col -> opinions.setColumnLabel( col.ordinal(), col.name() ) );
		final List<MatrixWeightedAverager> hes = new ArrayList<>();
		for( long i = 0; i < n; i++ )
		{
			opinions.setAsBigDecimal(
					BigDecimal.valueOf( (i + 2) * .2 % 1 + .1 ), i,
					SocialFactors.CONFIDENCE.ordinal() );
			opinions.setAsBigDecimal( BigDecimal.valueOf( i * .2 % 1 ), i,
					SocialFactors.COMPLACENCY.ordinal() );
			final String label = "hh" + i;
			opinions.setRowLabel( i, label );
			pressure.setRowLabel( i, label );
			pressure.setColumnLabel( i, "appr_" + label );
			// incremental self-reliance on (i,i) coordinates
			pressure.setAsBigDecimal( BigDecimal.valueOf( i ), i, i );
			final MatrixWeightedAverager att = MatrixWeightedAverager.of(
					opinions, pressure, id -> (Long) id.unwrap(),
					Actor.ID.of( i, null ) );
			opinions.setRowLabel( i, "" + att.isPositive() + i );
			hes.add( att );
		}
		LOG.trace( "Created matrices:\n{}\n{}", opinions, pressure );
		final VaxOccasion vax = VaxOccasion.of( DecimalUtil.ONE_HALF,
				DecimalUtil.ONE_HALF, DecimalUtil.ONE_HALF,
				DecimalUtil.ONE_HALF );
		LOG.trace( "Attitude towards occasion #1 {} -> {}",
				vax.asMap().values(), hes.stream().collect( Collectors
						.toMap( att -> att, att -> att.isPositive( vax ) ) ) );

		final Actor.ID authority = Actor.ID.of( 4L, null );
		// position set by one hesitant (ind1) is shared immediately with all
		hes.get( 0 ).setPosition( authority, BigDecimal.ONE, BigDecimal.ZERO );
		// isolate one position from influences, effectively fixing its values
		hes.get( 0 ).setAppreciation( authority, Matrix.Factory.zeros( 1, n ) );
		// expected: family 1 converges faster (to authority) than family 2
		LOG.trace( "Updated matrices:\n{}\n{}", opinions, pressure );

		// update opinions based on new occasion
		// expected: group 1 converges faster than group 2, opinion #4 stable
		LOG.trace( "Attitude towards occasion #2 {} -> {}, current:\n{}",
				vax.asMap()
						.values(),
				hes.stream()
						.collect(
								Collectors.toMap( att -> att,
										att -> att.reset() // reset to update values
												.isPositive( vax ) ) ),
				opinions );

		// update opinions based on new occasion
		// expected: group 1 converges faster than group 2, opinion #4 stable
		LOG.trace( "Attitude towards occasion #3 {} -> {}, current:\n{}",
				vax.asMap()
						.values(),
				hes.stream()
						.collect(
								Collectors.toMap( att -> att,
										att -> att.reset() // reset to update values
												.isPositive( vax ) ) ),
				opinions );

		LOG.info( "Completed test of {}",
				MatrixWeightedAverager.class.getSimpleName() );
	}

	/**
	 * Test the (default) {@link VaxHesitancy} behaviors
	 */
	@Test
	public void testHesitant()
	{
		LOG.info( "Started test of {}", VaxHesitancy.class.getSimpleName() );

		final VaxHesitancy.SimpleWeightedAverager hes = VaxHesitancy.SimpleWeightedAverager
				.of( 1, 0, .5, id -> .5 );
		final VaxOccasion occ = VaxOccasion.of( 1, 1, .5, .5 );
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ.asMap().values(),
				hes.getConvenience( occ ), hes.getConfidence(),
				hes.getComplacency(), hes.isPositive( occ ) );
//		assertTrue("should be hesitant by default", hes.isHesitant( occ ));

		hes.observeRisk( Actor.ID.of( "pro-vax", null ), 0, 1 );
		hes.observeRisk( Actor.ID.of( "anti-vax", null ), 1, 0 );
		hes.observeRisk( Actor.ID.of( "anti-vax", null ), 1, 0 ); // will overwrite?
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ.asMap().values(),
				hes.getConvenience( occ ), hes.getConfidence(),
				hes.getComplacency(), hes.isPositive( occ ) );
//		assertFalse("should become non-hesitant", hes.isHesitant( occ ));

		hes.observeRisk( Actor.ID.of( "anti-vax2", null ), 1, 0 );
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ.asMap().values(),
				hes.getConvenience( occ ), hes.getConfidence(),
				hes.getComplacency(), hes.isPositive( occ ) );
//		assertTrue("should become hesitant again", hes.isHesitant( occ ));

		hes.observeRisk( Actor.ID.of( "pro-vax2", null ), 0, 1 );
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ.asMap().values(),
				hes.getConvenience( occ ), hes.getConfidence(),
				hes.getComplacency(), hes.isPositive( occ ) );
//		assertFalse("should be non-hesitant again", hes.isHesitant( occ ));

		hes.setCalculation( 0 );
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ.asMap().values(),
				hes.getConvenience( occ ), hes.getConfidence(),
				hes.getComplacency(), hes.isPositive( occ ) );
		LOG.trace( "hes1: {}", hes );
//		assertTrue("should be hesitant again", hes.isHesitant( occ ));

		LOG.info( "Completed test of {}", VaxHesitancy.class.getSimpleName() );
	}

}
