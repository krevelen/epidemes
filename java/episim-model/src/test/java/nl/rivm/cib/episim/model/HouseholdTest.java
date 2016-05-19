/* $Id: 4cd06794959ca7bd59432990ae8f934d02b9e26d $
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
package nl.rivm.cib.episim.model;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.measure.unit.NonSI;
import javax.measure.unit.UnitFormat;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.math.ValueWeight;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3RandomNumberStream;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.RandomNumberStream;
import io.coala.resource.x.FileUtil;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.dsol3.Dsol3Scheduler;
import nl.rivm.cib.episim.util.Caller;

/**
 * {@link HouseholdTest}
 * 
 * @version $Id: 4cd06794959ca7bd59432990ae8f934d02b9e26d $
 * @author Rick van Krevelen
 */
public class HouseholdTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( HouseholdTest.class );
	{
		UnitFormat.getInstance().alias( NonSI.DAY, "days" );
		//UnitFormat.getInstance().label(DAILY, "daily");
	}

	public static class Geard2011Scenario
	{
		private static final Pattern VALUE_SEP = Pattern.compile( "\\s" );

		private List<ValueWeight<Integer, ?>> importFrequencies(
			final String path, final boolean valueFirst ) throws IOException
		{
			try( final BufferedReader in = new BufferedReader(
					new InputStreamReader( FileUtil.toInputStream( path ) ) ) )
			{
				final List<ValueWeight<Integer, ?>> result = new ArrayList<>();
				for( String line; (line = in.readLine()) != null; )
				{
					line = line.trim();
					if( line.startsWith( "#" ) ) continue;

					final String[] values = VALUE_SEP.split( line );
					final int valueIndex = valueFirst ? 0 : 1;
					final int frequencyIndex = 1 - valueIndex;
					result.add( ValueWeight.of(
							Integer.parseInt( values[valueIndex] ),
							new BigDecimal( values[frequencyIndex] ) ) );
				}
				in.close();
				return result;
			}
		}

		public void init( final Scheduler s ) throws IOException
		{
			final long seed = 12345L;
			final RandomNumberStream rng = Math3RandomNumberStream.Factory
					.of( MersenneTwister.class ).create( "MAIN", seed );
			final ProbabilityDistribution.Factory distFact = Math3ProbabilityDistribution.Factory
					.of( rng );
			
			final ProbabilityDistribution<Integer> age_dist = distFact
					.createCategorical( importFrequencies(
							"sim-demog/age_dist.dat", false ) );
			LOG.trace( "Drew age: {}", age_dist.draw() );
			
			final ProbabilityDistribution<Integer> death_rates_female_dist = distFact
					.createCategorical( importFrequencies(
							"sim-demog/death_rates_female.dat", true ) );
			LOG.trace( "Drew death_rates_female: {}", death_rates_female_dist.draw() );
			// CBS 70895ned: Overledenen; geslacht en leeftijd, per week
			// http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70895ned&LA=NL
			// CBS 83190ned: overledenen in huishouden per leeftijd
			// http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T
			
			final ProbabilityDistribution<Integer> death_rates_male_dist = distFact
					.createCategorical( importFrequencies(
							"sim-demog/death_rates_male.dat", true ) );
			LOG.trace( "Drew death_rates_male: {}", death_rates_male_dist.draw() );
			
			final ProbabilityDistribution<Integer> fertility_age_dist = distFact
					.createCategorical( importFrequencies(
							"sim-demog/fertility_age_probs.dat", true ) );
			LOG.trace( "Drew fertility_age_probs: {}", fertility_age_dist.draw() );
			
			final ProbabilityDistribution<Integer> fertility_rates_dist = distFact
					.createCategorical( importFrequencies(
							"sim-demog/fertility_rates.dat", true ) );
			LOG.trace( "Drew fertility_rates: {}", fertility_rates_dist.draw() );

			// birth parity ??
			
			// household composition ??

			LOG.trace( "Households initialized, t={}",
					s.now().prettify( NonSI.DAY, 1 ) );
		}
	}

	/**
	 * This test should:
	 * <ol>
	 * </ol>
	 * 
	 * @throws Throwable
	 */
	@Test
	public void householdCompositionTest() throws Throwable
	{
		LOG.trace( "Initializing household composition scenario..." );

		final Scheduler scheduler = Dsol3Scheduler.of( "dsol3Test",
				Instant.of( "0 days" ), Duration.of( "100 days" ),
				Caller.rethrow( new Geard2011Scenario()::init ) );

		LOG.trace( "Starting household composition scenario..." );
		final CountDownLatch latch = new CountDownLatch( 1 );
		scheduler.time().subscribe( ( t ) ->
		{
			LOG.trace( "t = {}", t.prettify( NonSI.DAY, 1 ) );
		}, ( e ) ->
		{
			LOG.warn( "Problem in scheduler", e );
		}, () ->
		{
			latch.countDown();
		} );
		scheduler.resume();
		latch.await( 3, TimeUnit.SECONDS );
		assertEquals( "Should have completed", 0, latch.getCount() );
	}

}
