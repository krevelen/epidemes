package nl.rivm.cib.episim.model.scenario;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

import javax.inject.Inject;
import javax.measure.Measurable;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import org.apache.logging.log4j.Logger;
import org.jscience.geography.coordinates.LatLong;
import org.jscience.physics.amount.Amount;

import io.coala.bind.LocalBinder;
import io.coala.config.InjectConfig;
import io.coala.log.LogUtil;
import io.coala.math.MeasureUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.random.AmountDistribution;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Instant;
import io.coala.time.Rate;
import io.coala.time.Scheduler;
import io.coala.time.Signal;
import io.coala.time.Units;
import nl.rivm.cib.episim.model.Gender;
import nl.rivm.cib.episim.model.Household;
import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.Place;
import nl.rivm.cib.episim.model.Population;
import nl.rivm.cib.episim.model.TransmissionSpace;
import nl.rivm.cib.episim.model.ZipCode;

/**
 * {@link Geard2011Scenario}
 * 
 * @version $Id: 5e3a1f243ab46e936f50b9c59a81bada60d8a5f4 $
 * @author Rick van Krevelen
 */
public class Geard2011Scenario implements Scenario
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( Geard2011Scenario.class );

	/**
	 * {@link GenderAge} wraps a {@link Tuple} in: Gender x Age
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	static class GenderAge extends Tuple
	{

		/**
		 * {@link GenderAge} constructor
		 * 
		 * @param values
		 */
		public GenderAge( final Individual individual )
		{
			super( Arrays.asList( individual.getGender(),
					individual.now().subtract( individual.getBirth() )
							.intValue( Units.ANNUM ) ) );
		}

		public Gender gender()
		{
			return (Gender) values().get( 0 );
		}

		public Integer age()
		{
			return (Integer) values().get( 1 );
		}

	}

	public static double adjust( final Measurable<Dimensionless> prob,
		final Measurable<Dimensionless> fraction )
	{
		// [geard] 1.0 - Math.pow( 1.0 - year_prob, num_days / 365.0 )
		final double result = 1.0
				- Math.pow( 1.0 - prob.doubleValue( Unit.ONE ),
						fraction.doubleValue( Unit.ONE ) );
//		LOG.trace( "adjust {} => {} per {}", prob, result, fraction );
		return result;
	}

	private Scheduler scheduler;

	// CBS 70895ned: Overledenen; geslacht en leeftijd, per week
	// http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70895ned&LA=NL

	// CBS 83190ned: overledenen in huishouden per leeftijd
	// http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T

	/** */
	ProbabilityDistribution<Geard2011HouseholdComposition> hh_comp_dist;

	/** age distribution - data taken from 2006 Australian census */
	ProbabilityDistribution<Integer> age_dist;
	List<ProbabilityDistribution<Integer>> age_dist_sub;

	/**
	 * death rate (proportion of individuals dying between age x and age x+1
	 */
	ConditionalDistribution<Boolean, GenderAge> deathRates;

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
	ProbabilityDistribution<Geard2011HouseholdComposition> fertility_age_parity;

	ProbabilityDistribution<Gender> gender_dist;

	Amount<Duration> dt;
	Signal<Rate> organicGrowthRate;
	Signal<Rate> immigrationRate;
	ProbabilityDistribution<Boolean> dtCoupleDist;
	ProbabilityDistribution<Boolean> dtLeaveHomeDist;
	ProbabilityDistribution<Boolean> dtDivorceDist;

	Signal<Range<Integer>> ageCoupling;
	Signal<Range<Integer>> ageLeaving;
	Signal<Range<Integer>> ageDivorcing;
	AmountDistribution<Duration> agePartner;
	AmountDistribution<Duration> birthGap;

	io.coala.time.Duration stepSize;

	Amount<Dimensionless> yearsPerDt;

	@Inject
	LocalBinder binder;

	@Inject
	ProbabilityDistribution.Factory distFact;

	@InjectConfig
	Geard2011Config conf;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	public void init( final Scheduler scheduler ) throws IOException
	{
		LOG.trace( "Initializing, binder: {}, factory: {}", this.binder,
				this.distFact );

		this.scheduler = scheduler;

		final List<WeightedValue<Integer>> ageDist = Geard2011Config
				.importFrequencies( this.conf.age_dist(), 0, Integer::valueOf );
		this.age_dist = this.distFact.createCategorical( ageDist );

		// TODO create Bernoulli draw for death conditional on (sex, age)

		final NavigableMap<Integer, BigDecimal> femaleDeathRates = Geard2011Config
				.importMap( this.conf.death_rates_female(), 1,
						Integer::valueOf );
		final NavigableMap<Integer, BigDecimal> maleDeathRates = Geard2011Config
				.importMap( this.conf.death_rates_male(), 1, Integer::valueOf );
		this.deathRates = ConditionalDistribution
				.of( this.distFact::createBernoulli, tuple ->
				{
					final Integer age = (Integer) tuple.values().get( 1 );
					final BigDecimal p = Gender.MALE
							.equals( tuple.values().get( 0 ) )
									? maleDeathRates.computeIfAbsent( age,
											key ->
											{
												return femaleDeathRates
														.lastEntry().getValue();
											} )
									: femaleDeathRates.computeIfAbsent( age,
											key ->
											{
												return femaleDeathRates
														.lastEntry().getValue();
											} );
					return adjust( MeasureUtil.toAmount( p, Unit.ONE ),
							this.yearsPerDt );
				} );

		this.fertility_age_dist = this.distFact
				.createCategorical( Geard2011Config.importFrequencies(
						this.conf.fertility_age_probs(), 1,
						Integer::valueOf ) );

		this.fertility_rates_dist = this.distFact.createCategorical(
				Geard2011Config.importFrequencies( this.conf.fertility_rates(),
						1, Integer::valueOf ) );

//			this.fertility_age_parity = distFact
//					.createCategorical( importFrequencies(
//							"sim-demog/fertility_age_parity_probs.dat", 1,
//							?::valueOf ) );

		this.hh_comp_dist = this.distFact.createCategorical(
				Geard2011Config.importFrequencies( this.conf.hh_comp(), 0,
						Geard2011HouseholdComposition::valueOf ) );
		this.age_dist_sub = Geard2011Config.splitAgeDistribution( this.distFact,
				ageDist, this.conf.hh_comp() );

		// taken from Pop_HH.gen_hh_age_structured_pop(pop_hh.py:743)
		this.gender_dist = distFact.createUniformCategorical( Gender.FEMALE,
				Gender.MALE );

		this.dt = Amount.valueOf( 10, Units.DAYS );
		this.stepSize = io.coala.time.Duration.of( this.dt );
		this.yearsPerDt = Amount.valueOf( 1, Units.ANNUAL ).times( this.dt )
				.to( Unit.ONE );

		this.organicGrowthRate = Signal.Simple.of( scheduler,
				Rate.of( 0.01, Units.ANNUAL ) );
		this.immigrationRate = Signal.Simple.of( scheduler,
				Rate.of( 0, Units.ANNUAL ) );
		this.dtCoupleDist = this.distFact.createBernoulli(
				adjust( this.conf.annualIndividualCouplingProbability(),
						this.yearsPerDt ) );
		this.dtLeaveHomeDist = this.distFact.createBernoulli(
				adjust( this.conf.annualIndividualLeavingProbability(),
						this.yearsPerDt ) );
		this.dtDivorceDist = this.distFact.createBernoulli(
				adjust( this.conf.annualIndividualDivorcingProbability(),
						this.yearsPerDt ) );
		this.ageCoupling = Signal.Simple.of( scheduler, Range.of( 21, 60 ) );
		this.ageLeaving = Signal.Simple.of( scheduler, Range.of( 18, null ) );
		this.ageDivorcing = Signal.Simple.of( scheduler, Range.of( 24, 60 ) );
		this.agePartner = this.distFact.createNormal( -2, 2 )
				.toAmounts( Units.ANNUM );
		this.birthGap = this.distFact.createDeterministic( 270 )
				.toAmounts( Units.DAYS );

		this.pop = Population.Simple.of( scheduler );

		final Set<Household> households = new HashSet<>();
		final int popSize = 20000;
		for( int size = 0; size < popSize; )
		{
			final Geard2011HouseholdComposition hh_comp = this.hh_comp_dist
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
					final Instant birth = Instant.of(
							Amount.valueOf( age, Units.ANNUM ).opposite() );
//						LOG.trace( "Birth {}: {}", i,
//								MeasureUtil.toString( birth.toMeasure(), 1 ) );
					final Gender gender = this.gender_dist.draw();
					boolean homeMaker = i == 0;
					hh.members().add( Individual.Simple.of( hh, birth, gender,
							home.getSpace(), homeMaker ) );
					size++;
				}
		}
		this.pop.reset( households );

		scheduler.at( scheduler.now() ).call( this::updateAll );
		LOG.trace( "{} households initialized, total={}", households.size(),
				pop.size() );
	}

	Population pop;

//	int growth = 0, draw = 0;

	/**
	 * Geard (simulation.py, pop_hh.py):
	 * <p>
	 * for each dt, update pop
	 * <ul>
	 * <li>age pop: increment all ages</li>
	 * <li>update_individuals: for each individual, either</li>
	 * <ul>
	 * <li>add death (handle_orphans) and birth (select_mother);</li>
	 * <li>OR choose_partner (age in &lang;couple_age, 60&rang; &and; single);
	 * </li>
	 * <li>OR leave_home (age &gt; leaving_age &and; at_parents &and; single);
	 * </li>
	 * <li>OR separate_couple (age in &lang;divorce_age, 50&rang; &and;
	 * partner);
	 * <li>OR nothing</li>
	 * </ul>
	 * <li>grow pop: add births (select_mother) with current pop growth_rate
	 * </li>
	 * <li>grow pop: add immigrants (dupl_household) with current pop imm_rate
	 * </li>
	 * </ul>
	 */
	protected void updateAll()
	{
		// generate events per dt
		int emptyHhs = 0, death = 0, coupled = 0, left = 0, divorced = 0;
		for( Iterator<Household> h = this.pop.households().iterator(); h
				.hasNext(); )
		{
			final Household hh = h.next();
			for( Iterator<Individual> i = hh.members().iterator(); i
					.hasNext(); )
			{
				final Individual ind = i.next();
				final GenderAge tuple = new GenderAge( ind );
				if( this.deathRates.draw( tuple ) )
				{
					// death/birth
					i.remove();
					death++;
					if( hh.members().isEmpty() )
					{
						h.remove();
						emptyHhs++;
					}
					this.pop.death( ind );
//					this.pop.birth( newborn ); // FIXME
					continue;
				}
				final Integer age = tuple.age();
				if( !ind.isHomeMaker()
						&& this.ageLeaving.current().contains( age )
						&& this.dtLeaveHomeDist.draw() )
				{
					// leave home
					i.remove();
					// TODO create Household
					this.pop.depart( ind, hh );
					left++;
				} //else 
				if( ind.isSingle() && Gender.MALE.equals( ind.getGender() ) // TODO check
						&& this.ageCoupling.current().contains( age )
						&& this.dtCoupleDist.draw() )
				{
					// partner
//					this.pop.formCouple( newHh, hh ); // FIXME
					coupled++;
				} else if( !ind.isSingle()
						&& this.ageDivorcing.current().contains( age )
						&& this.dtDivorceDist.draw() )
				{
					// separate
//					this.pop.dissolveCouple( newHh, hh ); // FIXME
					divorced++;
				}
			}
		}

		// pop growth
		final double annGrowthRate = this.organicGrowthRate.current()
				.doubleValue( Units.ANNUAL ); // update only annually?
		final long growth = this.distFact
				.createBinomial( annGrowthRate * this.pop.size(),
						this.yearsPerDt.doubleValue( Unit.ONE ) )
				.draw();
//		for( long i = growthDraw; i > 0; i-- )
//		{
//		this.pop.birth( newborn ); // FIXME
//		}

		// immigration
		final double annImmRate = this.immigrationRate.current()
				.doubleValue( Units.ANNUAL ); // update only annually?
		final long immigration = this.distFact
				.createBinomial( annImmRate * this.pop.size(),
						this.yearsPerDt.doubleValue( Unit.ONE ) )
				.draw();
//		for( long i = immigrationDraw; i > 0; i-- )
//		{
//		this.pop.immigrate( immigrants ); // FIXME
//		}

		LOG.trace( "-{}+{}+{} individuals, -{}+{} households, -{}+{} couples",
				death, growth + death, immigration, emptyHhs, left, divorced,
				coupled );

		// repeat indefinitely
		after( this.stepSize ).call( this::updateAll );
	}

//	if( this.dtGrowth.draw() )
//	{
//		growth += this.dtGrowth.draw().getExactValue();
//		LOG.trace( "growth: {}, t = {}", growth, now().prettify( 4 ) );
//	}
	/**
	 * TODO:
	 * <li>exchange operator (immigrate, emigrate, arrive, depart)
	 * <li>lifecycle operator (birth, eligible, match, divorce, death)
	 * <li>morbidity operator (infect, medicate)
	 */
}