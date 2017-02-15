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
package nl.rivm.cib.episim.pilot;

import java.text.ParseException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.random.ProbabilityDistribution;
import io.coala.rx.RxCollection;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.coala.time.Signal;
import nl.rivm.cib.episim.geard.GeardDemogConfig;
import nl.rivm.cib.episim.model.Gender;
import nl.rivm.cib.episim.model.Partner;
import nl.rivm.cib.episim.model.locate.Locatable;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.person.Household;
import nl.rivm.cib.episim.model.person.HouseholdParticipant;
import nl.rivm.cib.episim.model.person.HouseholdPopulation;
import nl.rivm.cib.episim.model.person.MotherPicker;
import nl.rivm.cib.episim.model.person.Participant;
import nl.rivm.cib.episim.model.person.Population;

@Singleton
public class EcosystemScenario implements Proactive
{

	interface Step<THIS extends Step<?>> extends Identified<Step.ID>
	{
		Duration duration(); // e.g. drawn from random distribution

		THIS next(); // e.g. drawn from random distribution

		class ID extends Id<String>
		{

		}
	}

	interface Activity extends Step<Activity>
	{
		String site();
	}

	interface Meeting extends Activity
	{
		String deadline();

		String purpose();

		String relations(); // e.g. decided by broker

		String persons(); // e.g. decided by broker
	}

	interface Transit extends Activity
	{
		String destination();

		String modality(); // e.g. decided by broker
		//String vehicle(); // e.g. decided by broker
	}

	interface Condition extends Step<Condition>
	{
		String compartment( String diseaseId ); // SIR

		String stage( String diseaseId );

		String symptoms();
	}

	interface Broker<F extends Fact> extends Actor<F>
	{
		interface Registration extends Fact
		{

		}

//		void register( Actor<F> registrant );
//
//		void unregister( Actor<F> registrant );

		Iterable<Actor<F>> rank( Comparator<? extends Actor<F>> comparator ); // prioritize by some distance metric

		default Actor<F> first( Comparator<? extends Actor<F>> comparator )
		{
			for( Actor<F> t : rank( comparator ) )
				return t;
			return null;
		}

		/**
		 * Apply the objects' {@link T#compareTo(Object) natural ordering}
		 * 
		 * @param ascend {@code true} to order ascending, {@code false} for
		 *            descending
		 * @return an {@link Iterable} object, e.g. a sorted list or db query
		 */
//		Iterable<T> rank( boolean ascend );
//
//		default T smallest()
//		{
//			for( T t : rank( true ) )
//				return t;
//			return null;
//		}
//
//		default T largest()
//		{
//			for( T t : rank( false ) )
//				return t;
//			return null;
//		}
	}

	interface VaccineAttitude extends Step<VaccineAttitude>
	{

		void absorb( String mediaEvent );

		void consume( String opinionEvent );

		void handle( String experienceEvent );

		Boolean isConfident( String vaccinationId ); // e.g. based on "life experience"

		Boolean isComplacent( String vaccinationId ); // e.g. based on "life experience"

		Boolean isConvenient( String vaccinationId ); // e.g. based on "life experience"

		Boolean isCalculated( String vaccinationId ); // e.g. based on "life experience"
	}

	/** */
	public interface Motivation extends Fact // e.g. media, peer, experience
	{

	}

	public interface Motivator extends Actor<Motivation>
	{

	}

	public interface Deme extends Actor<DemeFact>
	{

	}

	public interface DemeFact extends Fact
	{

	}

	public interface IndividualFact extends Fact
	{

	}

	public interface Health extends Actor<HealthFact>
	{

	}

	public interface HealthFact extends Fact
	{

	}

	public interface Mobility extends Actor<MobilityFact>
	{

	}

	public interface MobilityFact extends Fact
	{

	}

	public interface Media extends Actor<MediaFact>
	{

	}

	public interface MediaFact extends Fact
	{

	}

	public interface Contagium extends Actor<ContagiumFact>
	{

	}

	public interface ContagiumFact extends Fact
	{

	}

	/** A12 {@link Guardian} executes T12 {@link Disruption} requests */
	public interface Demographer extends Actor<Disruption>
	{

	}

	/** T12 {@link Disruption} initiator(s): {@link Deme} */
	public interface Disruption extends DemeFact
	{

	}

	/** A05 {@link Guardian} */
	public interface Guardian extends Actor<Guardianship>
	{
		List<Guardianship> wards(); // typically children
	}

	/** T05 {@link Guardianship} initiator(s): {@link Demographer} */
	public interface Guardianship extends DemeFact, IndividualFact
	{
	}

	private static long INDIVIDUAL_COUNT = 0;

	private static class Individual implements Partner, HouseholdParticipant,
		MotherPicker.Mother, Identified.Ordinal<String>, Locatable
	{

//		String location(); // should be consistent with activity site/vehicle
//
//		String lifephase(); // automaton based on age
//
//		Condition condition(); // default fall-back?
//
//		VaccineAttitude attitude(); // default fall-back?
//
//		Activity activity(); // belongs to a (common) routine pattern instance

		private final String id = "IND" + INDIVIDUAL_COUNT++;

		private final RxCollection<Individual> partners = RxCollection
				.of( new HashSet<>() );

		private Household<Individual> household;
		private Instant birth;
		private Gender gender;
		private boolean homeMaker;
		private Signal<Place> place;

		public static Individual of( final Household<Individual> household,
			final Instant birth, final Gender gender, final boolean homeMaker,
			final Place startLocation )
		{
			final Individual result = new Individual();
			result.household = household;
			result.birth = birth;
			result.gender = gender;
			result.homeMaker = homeMaker;
			result.place = Signal.Simple.of( household.scheduler(),
					startLocation );
			return result;
		}

		@Override
		public String id()
		{
			return this.id;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.household.scheduler();
		}

		@Override
		public Household<Individual> household()
		{
			return this.household;
		}

		@Override
		public RxCollection<Individual> partners()
		{
			return this.partners;
		}

		@Override
		public Instant born()
		{
			return this.birth;
		}

		@Override
		public Gender gender()
		{
			return this.gender;
		}

		@Override
		public HouseholdPopulation<Individual> population()
		{
			return this.household.population();
		}

		@Override
		public String toString()
		{
			return id();
		}

		@Override
		public Signal<Place> place()
		{
			return this.place;
		}
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( EcosystemScenario.class );

	@Inject
	private Scheduler scheduler;

	@Inject
	private ProbabilityDistribution.Factory distFact;

	private GeardDemogConfig conf = GeardDemogConfig.getOrFromYaml();

	private Quantity<Time> dt;

	private Quantity<Dimensionless> yearsPerDt;

	private HouseholdPopulation<Individual> pop;

	private MotherPicker<Individual> momPicker;

	@Inject
	private Actor.Factory actors;

	@Inject
	public EcosystemScenario( final Scheduler scheduler )
	{
		this.scheduler = scheduler;
		scheduler.onReset( this::init );
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	private void logProgress( int i, int total )
	{
		LOG.trace( LogUtil.messageOf(
				"{0} ({1,number,#.##%}) persons added, "
						+ "jvm free: ~{2,number,#,#00.#}MB (~{3,number,#.#%})",
				i, DecimalUtil.divide( i, total ),
				DecimalUtil.divide( Runtime.getRuntime().freeMemory(),
						1024 * 1024 ),
				DecimalUtil.divide( Runtime.getRuntime().freeMemory(),
						Runtime.getRuntime().totalMemory() ) ) );
	}

	interface Hierarchical<T extends Comparable<? super T>, THIS extends Hierarchical<T, THIS>>
	{
		T scale();

		THIS outer(); // null for root / top

		RxCollection<THIS> inner(); // null or empty for leaf / bottom
	}

	enum GroupLevel
	{
		POPULATION, // top aggregation
		ETHNICITY, // 
		HOUSEHOLD, // smallest unit
		;
	}

	interface Group extends Hierarchical<GroupLevel, Group>,
		Population<Participant>, Participant
	{

	}

	enum TerritoryLevel
	{
		FEDERATION, // EU, NATO, ...
		NATION, // country
		REGION, // state, landsdeel
		ZONE, // district, subregion (security/safety corop, ggd, province)
		DISTRICT, // county, department
		CITY, // metropolitan, municipal
		BUROUGH, // central, suburb
		WARD, // block
		ROAD, // street, railway, shipping lane, ...
		ADDRESS, // home, apartment
		;

		// transporter, modality, vehicle ?
	}

	interface Territory extends Hierarchical<TerritoryLevel, Territory>, Place
	{

	}

	/**
	 * <ol>
	 * <li>init (contact, transport, care) location networks</li>
	 * <li>init (age, sex, opinion, birth/death) population networks</li>
	 * <li>init (travel, contact, risk) behaviors</li>
	 * <li>init disease conditions/compartments</li>
	 * <li>run opinion-immunization\\contact-transmission events</li>
	 * </ol>
	 * 
	 * region [vacc degree, pop size, pop age dist, hh age dist]
	 * 
	 * @throws ParseException
	 */
	private void init() throws ParseException
	{
		LOG.info( "initializing..." );

		// FIXME apply outcome-driven event generation pruning

		// person: { activity[stepId], disease[ condition[stepId], attitude[stepId] ] }

		// calculate vaccination degree = #vacc / #non-vacc, given:
		//   disease/vaccine v 
		//   region(s) r
		//   cohort birth range [t1,t2]

//		final Mother mother = person.asExecutor( //Birth.class,
//				Mother.class );
//
//		// add business rule(s)
//		mother.emit( FactKind.REQUESTED ).subscribe( fact ->
//		{
//			after( Duration.of( 1, Units.DAYS ) ).call( t ->
//			{
//				final Birth st = mother.respond( fact, FactKind.STATED )
//						.with( "myParam1", "myValue1" ).commit( true );
//				LOG.trace( "t={}, {} responded: {} for incoming: {}",
//						t.prettify( this.actors.offset() ), mother.id(),
//						st, fact );
//			} );
//		} );
//
//		// initiate transactions with self
//		atEach( Timing.of( "0 0 0 14 * ? *" )
//				.offset( this.actors.offset() ).iterate(), t ->
//				{
//					mother.initiate( Birth.class, mother.id(), null,
//							t.add( 1 ), Collections.singletonMap(
//									"myParam0", "myValue0" ) );
//				} );

//		//final Set<Individual> pop = new HashSet<>();
//	//	final Set<Location> homes = new HashSet<>();
//	//	final int n_homes = 6000000;
//	//	final Set<Location> offices = new HashSet<>();
//	//	final int n_offices = 3000000;
//		final Infection measles = new Infection.Simple(
//				Amount.valueOf( 1, Units.DAILY ), Duration.of( "2 days" ),
//				Duration.of( "5 days" ), Duration.of( "9999 days" ),
//				Duration.of( "3 days" ), Duration.of( "7 days" ) );
//		
//		final TransmissionRoute route = TransmissionRoute.AIRBORNE;
//		final TransmissionSpace space = TransmissionSpace.of( scheduler,
//				route );
//		final Place rivm = Place.Simple.of( Place.RIVM_POSITION, Place.NO_ZIP,
//				space );
//		
//		final Collection<ContactIntensity> contactTypes = Collections
//				.singleton( ContactIntensity.FAMILY );
//		final Amount<Frequency> force = measles.getForceOfInfection(
//				rivm.getTransmissionSpace().getTransmissionRoutes(), contactTypes );
//		final Duration contactPeriod = Duration.of( "10 h" );
//		final double infectLikelihood = force.times( contactPeriod.toAmount() )
//				.to( Unit.ONE ).getEstimatedValue();
//		LOG.trace( "Infection likelihood: {} * {} * {} = {}", force,
//				contactPeriod, Arrays.asList( contactTypes ),
//				infectLikelihood );

//		final ProbabilityDistribution<Gender> genderDist = distFact
//				.createUniformCategorical( Gender.MALE, Gender.FEMALE );
//		final ProbabilityDistribution<Instant> birthDist = this.distFact
//				.createUniformDiscrete( -5, 0 ).toQuantities( TimeUnits.DAYS )
//				.map( Instant::of );
//
//		final Population pop = Population.of( "pop",
//				RxCollection.of( new HashSet<>() ), this.scheduler );
//		final MotherPicker mothers = null;
//		final Range<Integer> fertilityAges = null;
//		final Quantity<Time> recoveryPeriod = null;
//
//		// create organization
//		int n_pop = 160000;
//		long clock = System.currentTimeMillis();
//		for( int i = 0; i < n_pop; i++ )
//		{
//
//			final Actor<Fact> person = this.actors.create( "person" + i );
//			household.population().members().add( this );
//			household.members().add( this );
//			final Range<Instant> fertilityInterval = fertilityAges == null
//					? null
//					: MotherPicker.birthToAgeInterval( birth, fertilityAges );
//			mothers.registerDuring( this, fertilityInterval );
////			mothers.pickAndRemoveFor( );
//
//			final Gender gender = genderDist.draw();
//			final Instant birth = birthDist.draw();
//			LOG.trace( "#{} - gender: {}, birth: {}", i, gender,
//					birth.prettify( TimeUnits.DAY, 1 ) );
//			final Individual ind = Individual.of(
//					Household.Simple.of( pop, rivm ), birth, gender,
//					rivm.getTransmissionSpace(), false );
//			ind.with( Condition.Simple.of( ind, measles ) );
////				pop.add( ind );
//			final int nr = i;
//			ind.afflictions().get( measles ).emitTransitions().subscribe(
//					t -> LOG.trace( "Transition for #{} at t={}: {}", nr,
//							scheduler.now().prettify( Units.HOURS, 1 ), t ),
//					e -> LOG.warn( "Problem in transition", e ) );
//			if( distParser.getFactory().getStream()
//					.nextDouble() < infectLikelihood )
//			{
//				LOG.trace( "INFECTED #{}", i );
//				ind.after( Duration.of( "30 min" ) )
//						.call( ind.afflictions().get( measles )::infect );
//			}
//
//			if( System.currentTimeMillis() - clock > 1000 )
//			{
//				logProgress( i, n_pop );
//				clock = System.currentTimeMillis();
//			}
//		}
	}

}