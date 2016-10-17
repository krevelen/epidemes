package nl.rivm.cib.episim.geard;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Measurable;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import org.apache.logging.log4j.Logger;
import org.jscience.physics.amount.Amount;

import io.coala.bind.LocalBinder;
import io.coala.config.InjectConfig;
import io.coala.log.LogUtil;
import io.coala.math.MeasureUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.name.Identified;
import io.coala.random.AmountDistribution;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.rx.RxCollection;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Rate;
import io.coala.time.Scheduler;
import io.coala.time.Signal;
import io.coala.time.Units;
import nl.rivm.cib.episim.model.Gender;
import nl.rivm.cib.episim.model.Individual;
import nl.rivm.cib.episim.model.Partner;
import nl.rivm.cib.episim.model.person.Household;
import nl.rivm.cib.episim.model.person.HouseholdParticipant;
import nl.rivm.cib.episim.model.person.HouseholdPopulation;
import nl.rivm.cib.episim.model.person.MotherPicker;
import nl.rivm.cib.episim.model.person.Participant;
import nl.rivm.cib.episim.model.person.Population;

/**
 * {@link Geard2011Scenario}
 * 
 * @version $Id: 5e3a1f243ab46e936f50b9c59a81bada60d8a5f4 $
 * @author Rick van Krevelen
 */
@Singleton
public class Geard2011Scenario implements Proactive
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( Geard2011Scenario.class );

	static class Geard2011HouseholdComposition
	{
		/**
		 * "i j k": a household with i adults (18+), j school-age children
		 * (5-17) and k preschool-age children (<5).
		 */
		Number[] counts = null;

		public static Geard2011HouseholdComposition
			valueOf( final String values )
		{
			return of( Integer::valueOf,
					Geard2011Config.VALUE_SEP.split( values ) );
		}

		public static <T extends Number> Geard2011HouseholdComposition
			of( final Function<String, T> parser, final String... values )
		{
			final Geard2011HouseholdComposition result = new Geard2011HouseholdComposition();
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
			super( Arrays.asList( individual.gender(), individual.now()
					.subtract( individual.born() ).intValue( Units.ANNUM ) ) );
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

	private static long INDIVIDUAL_COUNT = 0;

	interface GeardIndividual extends Partner, HouseholdParticipant,
		MotherPicker.Mother, Identified.Ordinal<String>
	{
		/**
		 * @return {@code true} if this {@link Participant} can't leave home
		 */
		boolean isHomeMaker();

		Household<GeardIndividual> household();

		/**
		 * @param household
		 * @param birth
		 * @param gender
		 * @param homeMaker
		 * @return
		 */
		static GeardIndividual of( final Household<GeardIndividual> household,
			final Instant birth, final Gender gender, boolean homeMaker,
			final Range<Integer> fertilityAges,
			final Measurable<Duration> recoveryPeriod )
		{
			final GeardIndividual result = new GeardIndividual()
			{
				private final String id = "IND" + INDIVIDUAL_COUNT++;

				private final RxCollection<GeardIndividual> partners = RxCollection
						.of( new HashSet<>() );

				private final Range<Instant> fertilityInterval = fertilityAges == null
						? null
						: MotherPicker.birthToAgeInterval( birth,
								fertilityAges );

				@Override
				public Scheduler scheduler()
				{
					return household.scheduler();
				}

				@Override
				public Household<GeardIndividual> household()
				{
					return household;
				}

				@Override
				public RxCollection<GeardIndividual> partners()
				{
					return this.partners;
				}

				@Override
				public Instant born()
				{
					return birth;
				}

				@Override
				public Gender gender()
				{
					return gender;
				}

				@Override
				public boolean isHomeMaker()
				{
					return homeMaker;
				}

				@Override
				public Range<Instant> fertilityInterval()
				{
					return this.fertilityInterval;
				}

				@Override
				public Measurable<Duration> recoveryPeriod()
				{
					return recoveryPeriod;
				}

				@Override
				public String toString()
				{
					return "ind" + Integer.toHexString( hashCode() );
				}

				@Override
				public Population<?> population()
				{
					return household.population();
				}

				@Override
				public String id()
				{
					return id;
				}
			};
			household.population().members().add( result );
			household.members().add( result );
			return result;
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

	private final Scheduler scheduler;

	@Inject
	private LocalBinder binder;

	@Inject
	private ProbabilityDistribution.Factory distFact;

	@InjectConfig
	private Geard2011Config conf;

	private Amount<Duration> dt;

	private Amount<Dimensionless> yearsPerDt;

	private HouseholdPopulation<GeardIndividual> pop;

	private MotherPicker<GeardIndividual> momPicker;

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

	Signal<Rate> growthRate;
	Signal<Rate> immigrationRate;
	ProbabilityDistribution<Boolean> dtCoupleDist;
	ProbabilityDistribution<Boolean> dtLeaveHomeDist;
	ProbabilityDistribution<Boolean> dtDivorceDist;

	Signal<Range<Integer>> ageBirthing;
	Signal<Range<Integer>> ageCoupling;
	Signal<Range<Integer>> ageLeaving;
	Signal<Range<Integer>> ageDivorcing;
	AmountDistribution<Duration> agePartner;

	// TODO @InjectConfig(configType=Geard2011Config.class, methodName=...)
	AmountDistribution<Duration> birthGap;

	@Inject
	public Geard2011Scenario( final Scheduler scheduler )
	{
		this.scheduler = scheduler;
		scheduler.onReset( this::init );
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@SuppressWarnings( "unchecked" )
	public void init() throws IOException
	{
		LOG.trace( "Initializing, binder: {}, factory: {}", this.binder,
				this.distFact );

		this.dt = Amount.valueOf( 5, Units.DAYS );
		this.yearsPerDt = Amount.valueOf( 1, Units.ANNUAL ).times( this.dt )
				.to( Unit.ONE );

		final List<WeightedValue<Integer>> ageDist = Geard2011Config
				.importFrequencies( this.conf.age_dist(), 0, Integer::valueOf );
		this.age_dist = this.distFact.createCategorical( ageDist );

		final NavigableMap<Integer, BigDecimal> femaleDeathRates = Geard2011Config
				.importMap( this.conf.death_rates_female(), 1,
						Integer::valueOf );
		final NavigableMap<Integer, BigDecimal> maleDeathRates = Geard2011Config
				.importMap( this.conf.death_rates_male(), 1, Integer::valueOf );
		this.deathRates = ConditionalDistribution
				.of( this.distFact::createBernoulli, tuple ->
				{
					final BigDecimal p = Gender.MALE.equals( tuple.gender() )
							? maleDeathRates.computeIfAbsent( tuple.age(),
									key ->
									{
										return maleDeathRates.lastEntry()
												.getValue();
									} )
							: femaleDeathRates.computeIfAbsent( tuple.age(),
									key ->
									{
										return femaleDeathRates.lastEntry()
												.getValue();
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

		this.growthRate = Signal.Simple.of( scheduler,
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
		this.ageBirthing = Signal.Simple.of( scheduler, Range.of( 15, 50 ) );
		this.ageCoupling = Signal.Simple.of( scheduler, Range.of( 21, 60 ) );
		this.ageLeaving = Signal.Simple.of( scheduler, Range.of( 18, null ) );
		this.ageDivorcing = Signal.Simple.of( scheduler, Range.of( 24, 60 ) );
		this.agePartner = this.distFact.createNormal( -2, 2 )
				.toAmounts( Units.ANNUM );
		this.birthGap = this.distFact.createDeterministic( 270 )
				.toAmounts( Units.DAYS );

		this.momPicker = MotherPicker.of( scheduler );
		this.pop = HouseholdPopulation.of( "pop",
				RxCollection.of( new HashSet<GeardIndividual>() ),
				RxCollection.of( new HashSet<Household<GeardIndividual>>() ),
				scheduler );
		this.pop.events().ofType( Population.Birth.class ).subscribe( e ->
		{
			for( GeardIndividual i : ((Population.Birth<GeardIndividual>) e)
					.arrivals() )
				if( Gender.FEMALE.equals( i.gender() ) )
					this.momPicker.register( i );
		} );
		final int popSize = 20000;
		while( this.pop.members().size() < popSize )
			drawHousehold();

		scheduler.at( scheduler.now() ).call( this::updateAll,
				io.coala.time.Duration.of( this.dt ) );
		LOG.trace( "{} households initialized, total={}",
				this.pop.households().size(), this.pop.members().size() );

		this.pop.events().subscribe( e ->
		{
			LOG.trace( "Observed: {}", e );
		}, e ->
		{
			LOG.error( "Problem with population", e );
		} );

	}

	protected GeardIndividual drawIndividual(
		final Household<GeardIndividual> household, final Instant birth,
		boolean homeMaker )
	{
		final Gender gender = this.gender_dist.draw();
		if( !Gender.FEMALE.equals( gender ) ) return GeardIndividual
				.of( household, birth, gender, homeMaker, null, null );

		final Range<Integer> fertilityAges = this.ageBirthing.current();
		final Amount<Duration> recoveryPeriod = this.birthGap.draw();
		final GeardIndividual result = GeardIndividual.of( household, birth,
				gender, homeMaker, fertilityAges, recoveryPeriod );
		this.momPicker.register( result );
		return result;
	}

	protected Household<GeardIndividual> drawHousehold()
	{
		final Household<GeardIndividual> result = Household.of(
				"hh" + System.currentTimeMillis(), this.pop,
				RxCollection.of( new HashSet<>() ) );
		final Geard2011HouseholdComposition hh_comp = this.hh_comp_dist.draw();
		for( int i = 0; i < hh_comp.counts.length; i++ )
			for( int j = 0; j < hh_comp.counts[i].intValue(); j++ )
			{
				final double age = this.distFact.getStream().nextDouble()
						+ this.age_dist_sub.get( i ).draw();
				final Instant birth = Instant
						.of( Amount.valueOf( age, Units.ANNUM ).opposite() );
				boolean homeMaker = i == 0;
				drawIndividual( result, birth, homeMaker );
			}
		return result;
	}

	protected boolean drawLeavingHome( final GeardIndividual ind,
		final GenderAge tuple )
	{
		return !ind.isHomeMaker()
				&& this.ageLeaving.current().contains( tuple.age() )
				&& this.dtLeaveHomeDist.draw();
	}

	protected boolean drawCoupling( final GeardIndividual ind,
		final GenderAge tuple )
	{
		return ind.isSingle()
				&& this.ageCoupling.current().contains( tuple.age() )
				&& this.dtCoupleDist.draw();
	}

	protected boolean drawSeparating( final GeardIndividual ind,
		final GenderAge tuple )
	{
		return !ind.isSingle()
				&& this.ageDivorcing.current().contains( tuple.age() )
				&& this.dtDivorceDist.draw();
	}

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
	protected void updateAll( final io.coala.time.Duration stepSize )
	{
		// generate events per dt
		int emptyHhs = 0, death = 0, coupled = 0, left = 0, divorced = 0;
		for( Iterator<GeardIndividual> i = this.pop.members().iterator(); i
				.hasNext(); )
		{
			final GeardIndividual ind = i.next();
			final GenderAge tuple = new GenderAge( ind );
			if( this.deathRates.draw( tuple ) )
			{
				if( ind.household().members().size() == 1 ) emptyHhs++;
				ind.household().death( ind );
				death++;
				continue;
			}
			if( drawLeavingHome( ind, tuple ) )
			{
				ind.moveHouse( Household.of( "hh" + System.currentTimeMillis(),
						this.pop, RxCollection.of( new HashSet<>() ) ) );
				left++;
			} //else 
			if( drawCoupling( ind, tuple ) )
			{
//				this.pop.formCouple( newHh, hh ); // FIXME
				coupled++;
			} else if( drawSeparating( ind, tuple ) )
			{
//				this.pop.dissolveCouple( newHh, hh ); // FIXME
				divorced++;
			}
		}

		final long popSize = this.pop.members().size(); // update only annually?

		long growth = 0;
		if( this.momPicker.total() <= 0 )
			LOG.warn( "No eligible mothers?" );
		else
		{
			// pop birth + growth
			growth = death
					+ this.distFact
							.createBinomial(
									this.growthRate.current().doubleValue(
											Units.ANNUAL ) * popSize,
									this.yearsPerDt.doubleValue( Unit.ONE ) )
							.draw();

			final Set<Integer> momUnavailableAges = new HashSet<>();
			for( long i = growth; i > 0; i-- )
				growPop( momUnavailableAges );
		}

		// immigration
		final long immigration = this.distFact
				.createBinomial(
						this.immigrationRate.current()
								.doubleValue( Units.ANNUAL ) * popSize,
						this.yearsPerDt.doubleValue( Unit.ONE ) )
				.draw();
		for( long i = immigration; i > 0; )
			i -= drawHousehold().members().size();

		LOG.trace( "-{}+{}+{} individuals, -{}+{} households, -{}+{} couples",
				death, growth + death, immigration, emptyHhs, left, divorced,
				coupled );

		// repeat indefinitely
		after( stepSize ).call( this::updateAll, stepSize );
	}

	protected void growPop( final Set<Integer> momUnavailableAges )
	{
		GeardIndividual mom = null;
		int attempt = 0;
		while( mom == null && attempt++ < 30 )
		{
			int age = this.fertility_age_dist.draw();
			while( momUnavailableAges.contains( age ) )
				age = this.fertility_age_dist.draw();
			momUnavailableAges.add( age );
			mom = this.momPicker.pick( age, this.distFact.getStream() );
		}
		if( mom != null )
		{
			final GeardIndividual newborn = drawIndividual( mom.household(),
					now(), false );
			mom.household().birth( newborn );
		} else
			LOG.warn( "No candidate mothers available!" );
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