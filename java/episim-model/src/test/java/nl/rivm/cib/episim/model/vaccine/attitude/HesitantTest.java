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

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.enterprise.Actor;
import io.coala.log.LogUtil;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitant;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;

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

	/**
	 * Test the (default) {@link VaxHesitant} behaviors
	 */
	@Test
	public void testHesitant()
	{
		LOG.info( "Started test of {}", VaxHesitant.class.getSimpleName() );

		final VaxHesitant.WeightedAverager hes = new VaxHesitant.WeightedAverager(
				Actor.ID.of( "self", null ), 1, 0, .5, id -> .5 );
		final VaxOccasion occ = VaxOccasion.of( 1, 1, .5, .5 );
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ, hes.getConvenience( occ ),
				hes.getConfidence(), hes.getComplacency(),
				hes.isHesitant( occ ) );
		hes.observe( Actor.ID.of( "pro-vax", null ), 0, 1 );
		hes.observe( Actor.ID.of( "anti-vax", null ), 1, 0 );
		hes.observe( Actor.ID.of( "anti-vax", null ), 1, 0 );

		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ, hes.getConvenience( occ ),
				hes.getConfidence(), hes.getComplacency(),
				hes.isHesitant( occ ) );
		hes.observe( Actor.ID.of( "anti-vax2", null ), 1, 0 );
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ, hes.getConvenience( occ ),
				hes.getConfidence(), hes.getComplacency(),
				hes.isHesitant( occ ) );
		hes.setCalculation( 0 );
		LOG.trace( "hes1: calc={}, conv({})={}, conf={}, compl={} => {}",
				hes.getCalculation(), occ, hes.getConvenience( occ ),
				hes.getConfidence(), hes.getComplacency(),
				hes.isHesitant( occ ) );
		LOG.trace( "hes1: {}", hes );
		LOG.info( "Completed test of {}", VaxHesitant.class.getSimpleName() );
	}

}
