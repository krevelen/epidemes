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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.logging.log4j.Logger;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3RandomNumberStream;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.ProbabilityDistribution.ArithmeticDistribution;
import io.coala.random.RandomNumberStream;
import io.coala.resource.x.FileUtil;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import io.coala.time.x.Rate;
import io.coala.util.Comparison;
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

		public List<ProbabilityDistribution<Integer>> splitAgeDistribution(
			final ProbabilityDistribution.Factory distFact,
			final List<WeightedValue<Integer, BigDecimal>> ageDist,
			final String path ) throws IOException
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
						final List<WeightedValue<Integer, BigDecimal>> subDist = new ArrayList<>();
						final int bound_b = Integer.valueOf( v );
						LOG.trace( "Filter: {} =< x < {}", bound_a, bound_b );
						for( WeightedValue<Integer, BigDecimal> wv : ageDist )
							if( wv.getValue() >= bound_a
									&& wv.getValue() < bound_b )
								subDist.add( wv );
						bound_a = bound_b;
						result.add( distFact.createCategorical( subDist ) );
					}
					final List<WeightedValue<Integer, BigDecimal>> subDist = new ArrayList<>();
					LOG.trace( "Filter: {} =< x", bound_a );
					for( WeightedValue<Integer, BigDecimal> wv : ageDist )
						if( wv.getValue() >= bound_a ) subDist.add( wv );
					result.add( distFact.createCategorical( subDist ) );
					Collections.reverse( result );
					return result;
				}
			}
			throw new IOException( "No cut-offs found in file at: " + path );
		}

		public <T> List<WeightedValue<T, BigDecimal>> importFrequencies(
			final String path, final int weightColumn,
			final Function<String, T> parser ) throws IOException
		{
			//Locale.setDefault( Locale.US );
			Objects.requireNonNull( parser );
			int lineNr = 0;
			try( final BufferedReader in = new BufferedReader(
					new InputStreamReader( FileUtil.toInputStream( path ) ) ) )
			{
				final List<WeightedValue<T, BigDecimal>> result = new ArrayList<>();
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
						result.add( WeightedValue.of(
								parser.apply( values[valueIndex] ), weight ) );
					lineNr++;
				}
				return result;
			}
		}

		public double adjust_prob( double year_prob, int days )
		{
			double fraction = days / 365.0;
			return 1.0 - Math.pow( 1.0 - year_prob, fraction );
		}

		private ArithmeticDistribution<Dimensionless>
			adjust_prob( final Rate prob, final Duration dt )
		{
			// TODO Auto-generated method stub
			return null;
		}

		// CBS 70895ned: Overledenen; geslacht en leeftijd, per week
		// http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70895ned&LA=NL

		// CBS 83190ned: overledenen in huishouden per leeftijd
		// http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T

		/** */
		private ProbabilityDistribution<HouseholdComposition> hh_comp_dist;

		/** age distribution - data taken from 2006 Australian census */
		private ProbabilityDistribution<Integer> age_dist;
		private List<ProbabilityDistribution<Integer>> age_dist_sub;

		/**
		 * death rate (proportion of individuals dying between age x and age x+1
		 */
		private ProbabilityDistribution<Integer> death_rates_female_dist;

		/**
		 * death rate (proportion of individuals dying between age x and age x+1
		 */
		private ProbabilityDistribution<Integer> death_rates_male_dist;

		/** Age-specific fertility rates (2009 ABS data) */
		private ProbabilityDistribution<Integer> fertility_rates_dist;

		/**
		 * Given that a birth has a occurred, probability distribution over the
		 * age of the mother. This is generated from ABS age-specific fertility
		 * rates, essentially rescaling them. Theoretically, this is incorrect,
		 * as age-specific fertility rates are expressed as "# births occurring
		 * to women of age x" divided by "number of women of age x" However,
		 * given that the age distribution is relatively flat over the relevant
		 * age range, rescaled fertility rates serve as a very close
		 * approximation.
		 */
		private ProbabilityDistribution<Integer> fertility_age_dist;

		/** birth parity probabilities */
		private ProbabilityDistribution<HouseholdComposition> fertility_age_parity;

		private ProbabilityDistribution<Gender> gender_dist;

		public void init( final Scheduler scheduler ) throws IOException
		{
			final long seed = 12345L;
			final RandomNumberStream rng = Math3RandomNumberStream.Factory
					.of( MersenneTwister.class ).create( "MAIN", seed );
			final ProbabilityDistribution.Factory distFact = Math3ProbabilityDistribution.Factory
					.of( rng );

			final List<WeightedValue<Integer, BigDecimal>> ageDist = importFrequencies(
					"sim-demog/age_dist.dat", 0, Integer::valueOf );
			this.age_dist = distFact.createCategorical( ageDist );

			this.death_rates_female_dist = distFact.createCategorical(
					importFrequencies( "sim-demog/death_rates_female.dat", 1,
							Integer::valueOf ) );

			this.death_rates_male_dist = distFact.createCategorical(
					importFrequencies( "sim-demog/death_rates_male.dat", 1,
							Integer::valueOf ) );

			this.fertility_age_dist = distFact.createCategorical(
					importFrequencies( "sim-demog/fertility_age_probs.dat", 1,
							Integer::valueOf ) );

			this.fertility_rates_dist = distFact.createCategorical(
					importFrequencies( "sim-demog/fertility_rates.dat", 1,
							Integer::valueOf ) );

//			this.fertility_age_parity = distFact
//					.createCategorical( importFrequencies(
//							"sim-demog/fertility_age_parity_probs.dat", 1,
//							Integer::valueOf ) );

			this.hh_comp_dist = distFact.createCategorical(
					importFrequencies( "sim-demog/hh_comp.dat", 0,
							HouseholdComposition::valueOf ) );
			this.age_dist_sub = splitAgeDistribution( distFact, ageDist,
					"sim-demog/hh_comp.dat" );

			// taken from Pop_HH.gen_hh_age_structured_pop(pop_hh.py:743)
			this.gender_dist = distFact.createUniformCategorical( Gender.FEMALE,
					Gender.MALE );

			/*
			 * # population parameters (rates are yearly) pop_size =
			 * integer(default=1000) growth_rate = float(default=0.000) imm_rate
			 * = float(default=0.000) use_parity = boolean(default=False)
			 * dyn_rates = boolean(default=False)
			 * 
			 * # demographic parameters (probs are yearly) couple_prob =
			 * float(default=0.08) leaving_prob = float(default=0.02)
			 * divorce_prob = float(default=0.01) couple_age =
			 * integer(default=21) couple_age_max = integer(default=60)
			 * leaving_age = integer(default=18) divorce_age =
			 * integer(default=24) divorce_age_max = integer(default=60)
			 * min_partner_age = integer(default=16) partner_age_diff =
			 * integer(default=-2) partner_age_sd = integer(default=2)
			 * 
			 * birth_gap_mean = integer(default=270) birth_gap_sd =
			 * integer(default=0) #immigration_prob = float(default=0.0)
			 */
			final Duration dt = Duration.of( Amount.valueOf( 1, NonSI.DAY ) );

			final Rate growth = Rate.of( 0, Units.ANNUAL );
			final Rate immigration = Rate.of( 0, Units.ANNUAL );
			final Rate couplers = Rate.of( 0.08, Units.ANNUAL );
			final Rate leavers = Rate.of( 0.02, Units.ANNUAL );
			final Rate divorcers = Rate.of( 0.01, Units.ANNUAL );

			final ArithmeticDistribution<Dimensionless> dtGrowth = adjust_prob(
					growth, dt );
			final ArithmeticDistribution<Dimensionless> dtImmigration = adjust_prob(
					immigration, dt );
			final ArithmeticDistribution<Dimensionless> dtCouplers = adjust_prob(
					couplers, dt );
			final ArithmeticDistribution<Dimensionless> dtLeavers = adjust_prob(
					leavers, dt );
			final ArithmeticDistribution<Dimensionless> dtDivorcers = adjust_prob(
					divorcers, dt );

			final ProbabilityDistribution<Range<Amount<Dimensionless>>> ageCoupling = distFact
					.createDeterministic( Range.of( 21, 60, Unit.ONE ) );
			final ArithmeticDistribution<Dimensionless> ageLeaving = ArithmeticDistribution
					.of( 18, Unit.ONE );
			final ProbabilityDistribution<Range<Amount<Dimensionless>>> ageDivorcing = distFact
					.createDeterministic( Range.of( 24, 60, Unit.ONE ) );
			final ProbabilityDistribution<Double> agePartner = distFact
					.createNormal( -2, 2 );

			final Set<Household> households = new HashSet<>();
			final int popSize = 20000;
			final Population pop = Population.Simple.of( scheduler );
			for( int size = 0; size < popSize; )
			{
				final HouseholdComposition hh_comp = this.hh_comp_dist.draw();

				final LatLong position = null; // TODO conditional on hh_comp?
				final ZipCode zip = null; // TODO draw conditional on hh_comp?
				final TransmissionSpace space = null;
				final Place home = Place.Simple.of( position, zip, space );
				final Household hh = Household.Simple.of( pop, home );
				households.add( hh );

				for( int i = 0; i < this.age_dist_sub.size(); i++ )
					for( int j = 0; j < hh_comp.counts[i].intValue(); j++ )
					{
						final double age = distFact.getStream().nextDouble()
								+ this.age_dist_sub.get( i ).draw();
						final Instant birth = Instant.of(
								Amount.valueOf( age, NonSI.YEAR ).opposite() );
//						LOG.trace( "Birth {}: {}", i,
//								MeasureUtil.toString( birth.toMeasure(), 1 ) );
						final Gender gender = this.gender_dist.draw();
						hh.getMembers().add( Individual.Simple.of( hh, birth,
								gender, home.getSpace() ) );
						size++;
					}
			}
			pop.reset( households );

			LOG.trace( "{} households initialized, total={}", households.size(),
					pop.getSize() );
		}

		/*
		 * "prob i j k": probability of a household with i adults (18+), j
		 * school-age children (5-17) and k preschool-age children (<5).
		 */
		public static class HouseholdComposition
		{
			// i, j, k
			private Number[] counts = null;

			public static <T> HouseholdComposition
				valueOf( final String values )
			{
				return of( Integer::valueOf, VALUE_SEP.split( values ) );
			}

			public static <T extends Number> HouseholdComposition
				of( final Function<String, T> parser, final String... values )
			{
				final HouseholdComposition result = new HouseholdComposition();
				if( values != null )
				{
					result.counts = new Number[values.length];
					for( int i = 0; i < values.length; i++ )
						result.counts[i] = parser.apply( values[i] );
				}
				return result;
			}

			@Override
			public String toString()
			{
				return String.format( "hh(18+yo: %d, 5-17yo: %d, 0-4yo:%d)",
						this.counts[0], this.counts[1], this.counts[2] );
			}
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
