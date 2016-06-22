package nl.rivm.cib.episim.model.scenario;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.measure.Measurable;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.logging.log4j.Logger;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.physics.amount.Amount;

import io.coala.log.LogUtil;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3RandomNumberStream;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.ProbabilityDistribution.ArithmeticDistribution;
import io.coala.random.PseudoRandom;
import io.coala.resource.FileUtil;
import io.coala.time.Instant;
import io.coala.time.Rate;
import io.coala.time.Scheduler;
import io.coala.util.Comparison;
import nl.rivm.cib.episim.model.Gender;
import nl.rivm.cib.episim.model.Household;
import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.Place;
import nl.rivm.cib.episim.model.Population;
import nl.rivm.cib.episim.model.TransmissionSpace;
import nl.rivm.cib.episim.model.Units;
import nl.rivm.cib.episim.model.ZipCode;

/**
 * {@link Geard2011Scenario}
 * 
 * @version $Id: 5e3a1f243ab46e936f50b9c59a81bada60d8a5f4 $
 * @author Rick van Krevelen
 */
public class Geard2011Scenario
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( Geard2011Scenario.class );

	private static final Pattern VALUE_SEP = Pattern.compile( "\\s" );

	public static double adjust( final Measurable<Frequency> prob,
		final Measurable<Duration> dt )
	{
		// [geard] 1.0 - Math.pow( 1.0 - year_prob, num_days / 365.0 )
		return 1.0 - Math.pow( 1.0 - prob.doubleValue( Units.ANNUAL ),
				dt.doubleValue( NonSI.YEAR_CALENDAR ) );
	}

	public static class HouseholdComposition
	{
		/**
		 * "i j k": a household with i adults (18+), j school-age children
		 * (5-17) and k preschool-age children (<5).
		 */
		private Number[] counts = null;

		public static <T> Geard2011Scenario.HouseholdComposition
			valueOf( final String values )
		{
			return of( Integer::valueOf, VALUE_SEP.split( values ) );
		}

		public static <T extends Number> Geard2011Scenario.HouseholdComposition
			of( final Function<String, T> parser, final String... values )
		{
			final Geard2011Scenario.HouseholdComposition result = new HouseholdComposition();
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
			return "hh" + Arrays.asList( this.counts );
		}
	}

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
					LOG.trace( "Filter age: {} =< x < {}", bound_a, bound_b );
					for( WeightedValue<Integer, BigDecimal> wv : ageDist )
						if( wv.getValue() >= bound_a
								&& wv.getValue() < bound_b )
							subDist.add( wv );
					bound_a = bound_b;
					result.add( distFact.createCategorical( subDist ) );
				}
				final List<WeightedValue<Integer, BigDecimal>> subDist = new ArrayList<>();
				LOG.trace( "Filter age: {} =< x", bound_a );
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
					result.add( WeightedValue
							.of( parser.apply( values[valueIndex] ), weight ) );
				lineNr++;
			}
			return result;
		}
	}

	// CBS 70895ned: Overledenen; geslacht en leeftijd, per week
	// http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70895ned&LA=NL

	// CBS 83190ned: overledenen in huishouden per leeftijd
	// http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T

	/** */
	ProbabilityDistribution<Geard2011Scenario.HouseholdComposition> hh_comp_dist;

	/** age distribution - data taken from 2006 Australian census */
	ProbabilityDistribution<Integer> age_dist;
	List<ProbabilityDistribution<Integer>> age_dist_sub;

	/**
	 * death rate (proportion of individuals dying between age x and age x+1
	 */
	ProbabilityDistribution<Integer> death_rates_female_dist;

	/**
	 * death rate (proportion of individuals dying between age x and age x+1
	 */
	ProbabilityDistribution<Integer> death_rates_male_dist;

	/** Age-specific fertility rates (2009 ABS data) */
	ProbabilityDistribution<Integer> fertility_rates_dist;

	/**
	 * Given that a birth has a occurred, probability distribution over the age
	 * of the mother. This is generated from ABS age-specific fertility rates,
	 * essentially rescaling them. Theoretically, this is incorrect, as
	 * age-specific fertility rates are expressed as "# births occurring to
	 * women of age x" divided by "number of women of age x" However, given that
	 * the age distribution is relatively flat over the relevant age range,
	 * rescaled fertility rates serve as a very close approximation.
	 */
	ProbabilityDistribution<Integer> fertility_age_dist;

	/** birth parity probabilities */
	ProbabilityDistribution<Geard2011Scenario.HouseholdComposition> fertility_age_parity;

	ProbabilityDistribution<Gender> gender_dist;

	Amount<Duration> dt;
	ProbabilityDistribution<Boolean> dtGrowth;
	ProbabilityDistribution<Boolean> dtImmigration;
	ProbabilityDistribution<Boolean> dtCouplers;
	ProbabilityDistribution<Boolean> dtLeavers;
	ProbabilityDistribution<Boolean> dtDivorcers;

	ProbabilityDistribution<Range<Amount<Duration>>> ageCoupling;
	ArithmeticDistribution<Duration> ageLeaving;
	ProbabilityDistribution<Range<Amount<Duration>>> ageDivorcing;
	ArithmeticDistribution<Duration> agePartner;
	ArithmeticDistribution<Duration> birthGap;

	public void init( final Scheduler scheduler ) throws IOException
	{
		final long seed = 12345L;
		final PseudoRandom rng = Math3RandomNumberStream.Factory
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

		this.fertility_age_dist = distFact.createCategorical( importFrequencies(
				"sim-demog/fertility_age_probs.dat", 1, Integer::valueOf ) );

		this.fertility_rates_dist = distFact.createCategorical(
				importFrequencies( "sim-demog/fertility_rates.dat", 1,
						Integer::valueOf ) );

//			this.fertility_age_parity = distFact
//					.createCategorical( importFrequencies(
//							"sim-demog/fertility_age_parity_probs.dat", 1,
//							?::valueOf ) );

		this.hh_comp_dist = distFact.createCategorical( importFrequencies(
				"sim-demog/hh_comp.dat", 0, HouseholdComposition::valueOf ) );
		this.age_dist_sub = splitAgeDistribution( distFact, ageDist,
				"sim-demog/hh_comp.dat" );

		// taken from Pop_HH.gen_hh_age_structured_pop(pop_hh.py:743)
		this.gender_dist = distFact.createUniformCategorical( Gender.FEMALE,
				Gender.MALE );

		this.dt = Amount.valueOf( 1, NonSI.DAY );

		// TODO read from "paramspec_pop.cfg", rather: parse distributions
		this.dtGrowth = distFact.createBernoulli(
				adjust( Rate.of( 0, Units.ANNUAL ), this.dt ) );
		this.dtImmigration = distFact.createBernoulli(
				adjust( Rate.of( 0, Units.ANNUAL ), this.dt ) );
		this.dtCouplers = distFact.createBernoulli(
				adjust( Rate.of( 0.08, Units.ANNUAL ), this.dt ) );
		this.dtLeavers = distFact.createBernoulli(
				adjust( Rate.of( 0.02, Units.ANNUAL ), this.dt ) );
		this.dtDivorcers = distFact.createBernoulli(
				adjust( Rate.of( 0.01, Units.ANNUAL ), this.dt ) );
		this.ageCoupling = distFact
				.createDeterministic( Range.of( 21, 60, NonSI.YEAR_CALENDAR ) );
		this.ageLeaving = distFact.createDeterministic( 18 )
				.toAmounts( NonSI.YEAR_CALENDAR );
		this.ageDivorcing = distFact
				.createDeterministic( Range.of( 24, 60, NonSI.YEAR_CALENDAR ) );
		this.agePartner = distFact.createNormal( -2, 2 )
				.toAmounts( NonSI.YEAR_CALENDAR );
		this.birthGap = distFact.createDeterministic( 270 )
				.toAmounts( NonSI.DAY );

		final Set<Household> households = new HashSet<>();
		final int popSize = 20000;
		final Population pop = Population.Simple.of( scheduler );
		for( int size = 0; size < popSize; )
		{
			final Geard2011Scenario.HouseholdComposition hh_comp = this.hh_comp_dist
					.draw();

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
					final Instant birth = Instant
							.of( Amount.valueOf( age, NonSI.YEAR ).opposite() );
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
}