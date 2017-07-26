package nl.rivm.cib.episim.geard;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.name.Identified;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.rx.RxCollection;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.Signal;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.person.Gender;
import nl.rivm.cib.episim.model.person.MotherPicker;

/**
 * {@link GeardDemogScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@SuppressWarnings( "deprecation" )
@Singleton
public class GeardDemogScenario implements Scenario
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( GeardDemogScenario.class );

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
					GeardDemogConfig.VALUE_SEP.split( values ) );
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
			super( Arrays.asList( individual.gender().id(),
					QuantityUtil.intValue( individual.now()
							.subtract( individual.born() ).toQuantity(),
							TimeUnits.ANNUM ) ) );
		}

		public Gender gender()
		{
			return () -> (String) values().get( 0 );
		}

		public Integer age()
		{
			return (Integer) values().get( 1 );
		}

	}

	private static long INDIVIDUAL_COUNT = 0;

	interface GeardIndividual extends Partner, HouseholdParticipant,
		MotherPicker.Mother, Identified<String>
	{
		/**
		 * @return {@code true} if this {@link Participant} can't leave home
		 */
		boolean isHomeMaker();

		GeardHousehold<GeardIndividual> household();

		/**
		 * @param household
		 * @param birth
		 * @param gender
		 * @param homeMaker
		 * @return
		 */
		static GeardIndividual of(
			final GeardHousehold<GeardIndividual> household,
			final Instant birth, final Gender gender, boolean homeMaker,
			final Range<Integer> fertilityAges,
			final Quantity<Time> recoveryPeriod )
		{
			final GeardIndividual result = new GeardIndividual()
			{
				private final String id = "IND" + INDIVIDUAL_COUNT++;

				private final RxCollection<GeardIndividual> partners = RxCollection
						.of( new HashSet<>() );

//				private final Range<Instant> fertilityInterval = fertilityAges == null
//						? null
//						: MotherPicker.birthToAgeInterval( birth,
//								fertilityAges );

				@Override
				public Scheduler scheduler()
				{
					return household.scheduler();
				}

				@Override
				public GeardHousehold<GeardIndividual> household()
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

//				@Override
//				public Range<Instant> fertilityInterval()
//				{
//					return this.fertilityInterval;
//				}
//
//				@Override
//				public Quantity<Time> recoveryPeriod()
//				{
//					return recoveryPeriod;
//				}

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

	public static double adjust( final Quantity<Dimensionless> prob,
		final Quantity<Dimensionless> fraction )
	{
		// [geard] 1.0 - Math.pow( 1.0 - year_prob, num_days / 365.0 )
		final double result = 1.0 - Math.pow(
				1.0 - QuantityUtil.doubleValue( prob, QuantityUtil.PURE ),
				QuantityUtil.doubleValue( fraction, QuantityUtil.PURE ) );
		// LOG.trace( "adjust {} => {} per {}", prob, result, fraction );
		return result;
	}

	@Inject
	private Scheduler scheduler;

	@Inject
	private ProbabilityDistribution.Factory distFact;

	private GeardDemogConfig conf = GeardDemogConfig.getOrFromYaml();

	private Quantity<Time> dt;

	private Quantity<Dimensionless> yearsPerDt;

	private HouseholdPopulation<GeardIndividual> pop;

	private MotherPicker<GeardIndividual> momPicker;

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

	Signal<Quantity<Frequency>> growthRate;
	Signal<Quantity<Frequency>> immigrationRate;
	ProbabilityDistribution<Boolean> dtCoupleDist;
	ProbabilityDistribution<Boolean> dtLeaveHomeDist;
	ProbabilityDistribution<Boolean> dtDivorceDist;

	Signal<Range<Integer>> ageBirthing;
	Signal<Range<Integer>> ageCoupling;
	Signal<Range<Integer>> ageLeaving;
	Signal<Range<Integer>> ageDivorcing;
	QuantityDistribution<Time> agePartner;

	QuantityDistribution<Time> birthGap;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	private <K, V> V getOrCopyLast( final NavigableMap<K, V> map, final K key )
	{
		return map.computeIfAbsent( key, k -> map.lastEntry().getValue() );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void init() throws IOException, InterruptedException
	{
		this.dt = QuantityUtil.valueOf( 5, TimeUnits.DAYS );
		this.yearsPerDt = QuantityUtil.valueOf( 1, TimeUnits.ANNUAL )
				.multiply( this.dt ).asType( Dimensionless.class );

		final List<WeightedValue<Integer>> ageDist = GeardDemogConfig
				.importFrequencies( this.conf.age_dist(), 0, Integer::valueOf );
		this.age_dist = this.distFact.createCategorical( ageDist );

		final NavigableMap<Integer, BigDecimal> femaleDeathRates = GeardDemogConfig
				.importMap( this.conf.death_rates_female(), 1,
						Integer::valueOf );
		final NavigableMap<Integer, BigDecimal> maleDeathRates = GeardDemogConfig
				.importMap( this.conf.death_rates_male(), 1, Integer::valueOf );
		this.deathRates = ConditionalDistribution
				.of( this.distFact::createBernoulli,
						( GenderAge tuple ) -> adjust(
								QuantityUtil.valueOf( getOrCopyLast(
										tuple.gender().isMale() ? maleDeathRates
												: femaleDeathRates,
										tuple.age() ), QuantityUtil.PURE ),
								this.yearsPerDt ) );

		this.fertility_age_dist = this.distFact
				.createCategorical( GeardDemogConfig.importFrequencies(
						this.conf.fertility_age_probs(), 1,
						Integer::valueOf ) );

		this.fertility_rates_dist = this.distFact.createCategorical(
				GeardDemogConfig.importFrequencies( this.conf.fertility_rates(),
						1, Integer::valueOf ) );

		// this.fertility_age_parity = distFact
		// .createCategorical( importFrequencies(
		// "sim-demog/fertility_age_parity_probs.dat", 1,
		// ?::valueOf ) );

		this.hh_comp_dist = this.distFact.createCategorical(
				GeardDemogConfig.importFrequencies( this.conf.hh_comp(), 0,
						Geard2011HouseholdComposition::valueOf ) );
		this.age_dist_sub = GeardDemogConfig.splitAgeDistribution(
				this.distFact, ageDist, this.conf.hh_comp() );

		// taken from Pop_HH.gen_hh_age_structured_pop(pop_hh.py:743)
		this.gender_dist = distFact.createUniformCategorical( Gender.FEMALE,
				Gender.MALE );

		final Quantity<Time> yr = QuantityUtil.valueOf( 1, TimeUnits.ANNUM );
		this.growthRate = Signal.Simple.of( this.scheduler,
				QuantityUtil.valueOf( 0.01, TimeUnits.ANNUAL ) );
		this.immigrationRate = Signal.Simple.of( this.scheduler,
				QuantityUtil.valueOf( 0, TimeUnits.ANNUAL ) );
		this.ageBirthing = Signal.Simple.of( scheduler, Range.of( 15, 50 ) );
		this.ageCoupling = Signal.Simple.of( scheduler, Range.of( 21, 60 ) );
		this.ageLeaving = Signal.Simple.of( scheduler, Range.of( 18, null ) );
		this.ageDivorcing = Signal.Simple.of( scheduler, Range.of( 24, 60 ) );
		this.agePartner = this.distFact.createNormal( -2, 2 )
				.toQuantities( TimeUnits.ANNUM );
		this.birthGap = this.distFact.createDeterministic( 270 )
				.toQuantities( TimeUnits.DAYS );
		this.dtCoupleDist = this.distFact.createBernoulli(
				adjust( this.conf.couplingProportion().multiply( yr )
						.asType( Dimensionless.class ), this.yearsPerDt ) );
		this.dtLeaveHomeDist = this.distFact.createBernoulli(
				adjust( this.conf.leavingProportion().multiply( yr )
						.asType( Dimensionless.class ), this.yearsPerDt ) );
		this.dtDivorceDist = this.distFact.createBernoulli(
				adjust( this.conf.divorcingProportion().multiply( yr )
						.asType( Dimensionless.class ), this.yearsPerDt ) );

		this.momPicker = MotherPicker.of( scheduler );
		this.pop = HouseholdPopulation.of( "pop",
				RxCollection.of( new HashSet<GeardIndividual>() ),
				RxCollection
						.of( new HashSet<GeardHousehold<GeardIndividual>>() ),
				scheduler );
		this.pop.events().ofType( Population.Birth.class ).subscribe( b ->
		{
			for( GeardIndividual i : ((Population.Birth<GeardIndividual>) b)
					.arrivals() )
				if( i.gender().isFemale() ) this.momPicker.register( i );
		}, e -> LOG.error( "Problem", e ) );
//		final ForkJoinPool pool = new ForkJoinPool();
		final int popSize = this.conf.popSize();
		int size = 0, oldSize = size;
		long t, s = t = System.currentTimeMillis();
		while( size < popSize )
		{
////		final List<Integer> failed = //
////					CompletableFuture
////							.supplyAsync( () -> IntStream
////									.range( 0, pool.getParallelism() )
////									.parallel()
////									.filter( i -> drawHousehold() == null )
////									.collect( ArrayList<Integer>::new,
////											( list, i ) -> list.add( i ),
////											( l1, l2 ) -> l1.addAll( l2 ) ),
////									pool )
////							.join();
//			final List<Integer> failed = //
//					pool.submit( () -> IntStream
//							.range( 0, pool.getParallelism() ).parallel()
//							.filter( i -> drawHousehold() == null )
//							.collect( () -> new ArrayList<Integer>(),
//									( list, i ) -> list.add( i ),
//									( l1, l2 ) -> l1.addAll( l2 ) ) )
//							.join();
//			LOG.trace( "Failed threads: {}", failed );
			drawHousehold();

			size = this.pop.members().size();
			if( System.currentTimeMillis() - s > 1000 )
			{
				s = System.currentTimeMillis();
				long secs = (s - t) / 1000;
				LOG.info( "pop +{} = {} of {} ({}% @{}/sec)", size - oldSize,
						size, popSize, 100 * size / popSize,
						((float) size) / secs );
				oldSize = size;
			}
		}

		at( scheduler.now() ).call( this::updateAll );
		LOG.trace( "{} households initialized, total={}",
				this.pop.households().size(), this.pop.members().size() );

	}

	protected GeardIndividual drawIndividual(
		final GeardHousehold<GeardIndividual> household, final Instant birth,
		boolean homeMaker )
	{
		final Gender gender = this.gender_dist.draw();
		if( !gender.isFemale() ) return GeardIndividual.of( household, birth,
				gender, homeMaker, null, null );

		final Range<Integer> fertilityAges = this.ageBirthing.current();
		final Quantity<Time> recoveryPeriod = this.birthGap.draw();
		final GeardIndividual result = GeardIndividual.of( household, birth,
				gender, homeMaker, fertilityAges, recoveryPeriod );
		this.momPicker.register( result );
		return result;
	}

	private AtomicInteger hhCount = new AtomicInteger();

	protected GeardHousehold<GeardIndividual> drawHousehold()
	{
		final GeardHousehold<GeardIndividual> result = GeardHousehold.of(
				"hh" + this.hhCount.incrementAndGet(), this.pop,
				RxCollection.of( new HashSet<>() ) );
		final Geard2011HouseholdComposition hh_comp = this.hh_comp_dist.draw();
		for( int i = 0; i < hh_comp.counts.length; i++ )
			for( int j = 0; j < hh_comp.counts[i].intValue(); j++ )
			{
				final BigDecimal age = this.distFact.getStream()
						.nextBigDecimal().add( DecimalUtil
								.valueOf( this.age_dist_sub.get( i ).draw() ) );
				final Instant birth = Instant.of(
						QuantityUtil.valueOf( age.negate(), TimeUnits.ANNUM ) );
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

	private io.coala.time.Duration stepSize;

	/**
	 * Geard (simulation.py, pop_hh.py):
	 * <p>
	 * for each dt, update pop
	 * <ul>
	 * <li>age pop: increment all ages</li>
	 * <li>update_individuals: for each individual, either
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
	 * </li>
	 * <li>grow pop: add births (select_mother) with current pop growth_rate
	 * </li>
	 * <li>grow pop: add immigrants (dupl_household) with current pop imm_rate
	 * </li>
	 * </ul>
	 */
	protected void updateAll()
	{
		this.stepSize = io.coala.time.Duration.of( this.dt );
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
				ind.moveHouse( GeardHousehold.of(
						"hh" + System.currentTimeMillis(), this.pop,
						RxCollection.of( new HashSet<>() ) ) );
				left++;
			} // else
			if( drawCoupling( ind, tuple ) )
			{
				// this.pop.formCouple( newHh, hh ); // FIXME
				coupled++;
			} else if( drawSeparating( ind, tuple ) )
			{
				// this.pop.dissolveCouple( newHh, hh ); // FIXME
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
			growth = death + this.distFact.createBinomial(
					QuantityUtil.doubleValue( this.growthRate.current(),
							TimeUnits.ANNUAL ) * popSize,
					QuantityUtil.doubleValue( this.yearsPerDt,
							QuantityUtil.PURE ) )
					.draw();

			final Set<Range<Integer>> momUnavailableAges = new HashSet<>();
			for( long i = growth; i > 0; i-- )
				growPop( momUnavailableAges );
		}

		// immigration
		final long immigration = this.distFact.createBinomial(
				QuantityUtil.doubleValue( this.immigrationRate.current(),
						TimeUnits.ANNUAL ) * popSize,
				QuantityUtil.doubleValue( this.yearsPerDt, QuantityUtil.PURE ) )
				.draw();
		for( long i = immigration; i > 0; )
			i -= drawHousehold().members().size();

		LOG.info(
				"t={} -{}+{}+{} individuals, -{}+{} households, -{}+{} couples",
				now().prettify( 2 ), death, growth + death, immigration,
				emptyHhs, left, divorced, coupled );

		// repeat indefinitely
		after( this.stepSize ).call( this::updateAll );
	}

	protected void growPop( final Set<Range<Integer>> momUnavailableAges )
	{
		GeardIndividual mom = null;
		int spread = 0;
		int age = this.fertility_age_dist.draw();
		final Range<Integer> fertilityAges = this.ageBirthing.current();
		Range<Integer> ageRange = null;
		while( mom == null && !fertilityAges.equals( ageRange ) )
		{
			ageRange = fertilityAges
					.intersect( Range.of( age - spread, age + spread ) );
			momUnavailableAges.add( ageRange );
			mom = this.momPicker.pick( ageRange, this.distFact.getStream() );
			spread++;
		}
		if( mom != null )
		{
			final GeardIndividual newborn = drawIndividual( mom.household(),
					now(), false );
			mom.household().birth( newborn );
		} else
			LOG.warn( "No candidate mothers in age range {}?", ageRange );
	}

	// if( this.dtGrowth.draw() )
	// {
	// growth += this.dtGrowth.draw().getExactValue();
	// LOG.trace( "growth: {}, t = {}", growth, now().prettify( 4 ) );
	// }
	/**
	 * TODO:
	 * <li>exchange operator (immigrate, emigrate, arrive, depart)
	 * <li>lifecycle operator (birth, eligible, match, divorce, death)
	 * <li>morbidity operator (infect, medicate)
	 */
}