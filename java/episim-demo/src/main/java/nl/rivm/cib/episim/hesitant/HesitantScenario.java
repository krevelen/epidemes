package nl.rivm.cib.episim.hesitant;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.coala.config.GlobalConfig;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.FactKind;
import io.coala.json.Wrapper;
import io.coala.log.LogUtil;
import io.coala.math.Range;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Advice.Advisor;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Disruption.Disruptor;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Information.Informer;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Motivation.Motivator;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Opinion.Opinionator;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Redirection.GoalType;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Redirection.Redirector;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;

/**
 * {@link HesitantScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class HesitantScenario implements Scenario
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( HesitantScenario.class );

	public interface Contact extends Fact
	{
		public interface Mixer extends Actor<Contact>
		{

		}
	}

	/**
	 * T11 {@link Infection} transactions are initiated by some O02
	 * {@link Contagium}'s A15 {@link Transmitter}
	 */
//		public interface Infection extends Fact
//		{
//			/** A11 {@link Afflictor} handles T13 {@link Opinion} execution */
//			public interface Afflictor extends Actor<Infection>
//			{
//
//			}
//		}

	/**
	 * T13 {@link Opinion} facts subscribe a {@link PersonOrg} to some other O01
	 * {@link PersonOrg} (initiated by their A15 {@link Redirector}) to receive
	 * {@link Motivation} updates
	 */
	public interface Opinion extends Fact
	{
		/** A13 {@link Opinionator} handles T13 {@link Opinion} execution */
		public interface Opinionator extends Actor<Opinion>
		{

		}
	}

	/**
	 * T14 {@link Motivation} transactions are initiated by:
	 * <ul>
	 * <li>another O01 {@link PersonOrg}'s A13 {@link Opinionator} (e.g.
	 * persuade a, relative, colleague, or other social network relation);</li>
	 * <li>some O04 Inform organization's A40 {@link Informer}; or</li>
	 * <li>some O05 Health organization's A50 {@link Advisor}.
	 * </ul>
	 */
	public interface Motivation extends Fact
	{
		Number getConfidence();

		default Motivation withConfidence( final Number confidence )
		{
			return with( "confidence", confidence );
		}

		Number getComplacency();

		default Motivation withComplacency( final Number complacency )
		{
			return with( "complacency", complacency );
		}

		default Motivation withAttitude( final VaxHesitancy hes )
		{
			return withConfidence( hes.getConfidence() )
					.withComplacency( hes.getComplacency() );
		}

		/**
		 * A14 {@link Motivator} handles T14 {@link Motivation} execution,
		 * occasionally initiating a {@link Redirection}
		 */
		public interface Motivator extends Actor<Motivation>
		{

			/** @return the {@link VaxHesitancy} */
			VaxHesitancy getAttitude();

			default Motivator withAttitude( final VaxHesitancy hes )
			{
				return with( "attitude", hes );
			}

		}

		/** @return {@link List} of the {@link VaxDose}s remaining */
		List<VaxDose> getRemaining();

		void setRemaining( List<VaxDose> doses );

		default Motivation withRemaining( final List<VaxDose> doses )
		{
			setRemaining( doses );
			return this;
		}

		default Motivation with( final VaxDose... doses )
		{
			return withRemaining( Arrays.asList( doses ) );
		}

		/**
		 * @return {@link List} of {@link VaxOccasion}s, representing e.g.
		 *         available week/days at a (youth) {@link HealthOrg} service
		 */
		List<VaxOccasion> getOccasions();

		void setOccasions( List<VaxOccasion> occasions );

		default Motivation withOccasions( final List<VaxOccasion> occasions )
		{
			setOccasions( occasions );
			return this;
		}

		default Motivation with( final VaxOccasion... occasions )
		{
			return withOccasions(
					occasions == null ? null : Arrays.asList( occasions ) );
		}
	}

	/**
	 * {@link Disruption}
	 */
	public interface Disruption extends Fact
	{
		/** {@link Disruptor} handles {@link Disruption} execution */
		public interface Disruptor extends Actor<Redirection>
		{

		}
	}

	/**
	 * T15 {@link Redirection} transactions are initiated by:
	 * <ul>
	 * <li>the {@link PersonOrg}'s own A12 {@link Disruptor} behavior;</li>
	 * <li>the {@link PersonOrg}'s own A14 {@link Motivator}; or</li>
	 * <li>the {@link PersonOrg}'s own A15 {@link Redirector}.</li>
	 * </ul>
	 */
	public interface Redirection extends Fact
	{
		enum GoalType
		{
			VACCINATE;
		}

		GoalType getGoalType();

		void setGoalType( GoalType goalType );

		default Redirection with( final GoalType goalType )
		{
			setGoalType( goalType );
			return this;
		}

		/** A15 {@link Redirector} handles T15 {@link Redirection} execution */
		public interface Redirector extends Actor<Redirection>
		{
			/** @param ids */
			default Redirector consultPersons( final Actor.ID... ids )
			{
				return ids == null || ids.length == 0 ? this
						: consultPersons( Arrays.asList( ids ) );
			}

			/** @param ids */
			default Redirector consultPersons( final Iterable<Actor.ID> ids )
			{
				ids.forEach( id -> initiate( Opinion.class, id ).commit() );
				return this;
			}

			/** @param ids */
			default Redirector consultAdvisors( final Actor.ID... ids )
			{
				return ids == null || ids.length == 0 ? this
						: consultAdvisors( Arrays.asList( ids ) );
			}

			/** @param ids */
			default Redirector consultAdvisors( final Iterable<Actor.ID> ids )
			{
				ids.forEach( id -> initiate( Advice.class, id ).commit() );
				return this;
			}

			/** @param ids */
			default Redirector consultMedia( final Actor.ID... ids )
			{
				return ids == null || ids.length == 0 ? this
						: consultMedia( Arrays.asList( ids ) );
			}

			/** @param ids */
			default Redirector consultMedia( final Iterable<Actor.ID> ids )
			{
				ids.forEach( id -> initiate( Information.class, id ).commit() );
				return this;
			}
		}
	}

	public interface Plan extends Fact
	{
		public interface Planner extends Actor<Plan>
		{

		}
	}

	public interface Activity extends Fact
	{
		public interface Activator extends Actor<Activity>
		{

		}
	}

	public interface Information extends Fact
	{
		public interface Informer extends Actor<Information>
		{

		}
	}

	@JsonInclude( Include.NON_NULL )
	public static class VaxDose implements Identified.Ordinal<VaxDose.ID>
	{
		static class ID extends Id.OrdinalChild<Integer, String>
		{
			public static ID of( final Integer value, final String series )
			{
				return of( value, series, new ID() );
			}
		}

		public static VaxDose of( final int sequence, final String series,
			final Duration scheduleAge )
		{
			return of( sequence, series, scheduleAge, null );
		}

		public static VaxDose of( final int sequence, final String series,
			final Duration scheduleAge, final Range<Duration> ageWindow )
		{
			final VaxDose result = new VaxDose();
			result.name = ID.of( sequence, series );
			result.scheduleAge = scheduleAge;
			result.ageWindow = ageWindow;
			return result;
		}

		public ID name;
		public Duration scheduleAge;
		public Range<Duration> ageWindow;

		@Override
		public ID id()
		{
			return this.name;
		}
	}

	// TODO as BPM with time/sequence constraints on dose vax activities?
	public interface VaxProcess
	{

		/**
		 * see https://www.wikiwand.com/en/DPT_vaccine and
		 * https://www.wikiwand.com/en/Hib_vaccine and
		 * https://www.wikiwand.com/en/Hepatitis_B_vaccine
		 */
		String DKTP_SERIES = "DKTP";

		VaxDose[] DKTP = {
				VaxDose.of( 1, DKTP_SERIES, Duration.of( 42, TimeUnits.DAYS ),
						Range.of( Duration.of( 25, TimeUnits.DAYS ),
								Duration.of( 5, TimeUnits.ANNUM ) ) ),
				VaxDose.of( 2, DKTP_SERIES, Duration.of( 90, TimeUnits.DAYS ) ),
				VaxDose.of( 3, DKTP_SERIES,
						Duration.of( 120, TimeUnits.DAYS ) ),
				VaxDose.of( 4, DKTP_SERIES,
						Duration.of( 330, TimeUnits.DAYS ) ),
				VaxDose.of( 5, DKTP_SERIES, Duration.of( 4, TimeUnits.ANNUM ) ),
				VaxDose.of( 6, DKTP_SERIES,
						Duration.of( 9, TimeUnits.ANNUM ) ) };

		/** see https://www.wikiwand.com/en/Pneumococcal_vaccine */
		String PNEU_SERIES = "Pneu";

		VaxDose[] PNEU = {
				VaxDose.of( 1, PNEU_SERIES, Duration.of( 60, TimeUnits.DAYS ),
						Range.of( Duration.of( 25, TimeUnits.DAYS ),
								Duration.of( 5, TimeUnits.ANNUM ) ) ),
				VaxDose.of( 2, PNEU_SERIES,
						Duration.of( 120, TimeUnits.DAYS ) ),
				VaxDose.of( 3, PNEU_SERIES,
						Duration.of( 330, TimeUnits.DAYS ) ) };

		/** see https://www.wikiwand.com/en/Meningococcal_vaccine */
		String MENC_SERIES = "MenC";

		VaxDose[] MENC = { VaxDose.of( 1, MENC_SERIES,
				Duration.of( 1.16, TimeUnits.ANNUM ) ) };

		/** see https://www.wikiwand.com/en/MMR_vaccine */
		String BMR_SERIES = "BMR";

		VaxDose[] BMR = {
				VaxDose.of( 1, BMR_SERIES,
						Duration.of( 1.16, TimeUnits.ANNUM ) ),
				VaxDose.of( 2, BMR_SERIES,
						Duration.of( 9, TimeUnits.ANNUM ) ) };

		/** see https://www.wikiwand.com/en/HPV_vaccines */
		String HPV_SERIES = "HPV";
		VaxDose[] HPV = {
				VaxDose.of( 1, HPV_SERIES, Duration.of( 12, TimeUnits.ANNUM ) ),
				VaxDose.of( 2, HPV_SERIES,
						Duration.of( 12, TimeUnits.ANNUM ) ) };

		VaxDose[][] RVP_SCHEDULE = { DKTP, PNEU, BMR, MENC, HPV };

	}

	public static class VaxRegistrant extends Wrapper.SimpleOrdinal<Instant>
	{
		private final Actor.ID personRef;

		private final Fact.ID regRef;

		/** upcoming invite/reminder */
		private transient List<VaxDose> vaxStatus = Collections.emptyList();

		public VaxRegistrant( final Advice reg )
		{
			wrap( reg.getBirth() );
			this.personRef = reg.creatorRef().organizationRef();
			this.regRef = reg.id();
		}

		@Override
		public boolean equals( final Object that )
		{
			return this.personRef.equals( ((VaxRegistrant) that).personRef );
		}

		public void update( final Treatment treatment )
		{
			for( VaxDose dose : treatment.getDoses() )
			{
				// TODO remove previous doses in the vaccine series/process
			}
		}
	}

	public interface Advice extends Fact
	{
		interface Advisor extends Actor<Advice>
		{
		}

		/** @return the requester/registrant's time of birth */
		Instant getBirth();

		/** @param birth the requester/registrant's time of birth */
		void setBirth( Instant birth );

		/** @param birth the requester/registrant's time of birth */
		default Advice withBirth( final Instant birth )
		{
			setBirth( birth );
			return this;
		}

		/** @return the requester/registrant's vaccination status */
		List<VaxDose> getVaxStatus();

		/** @param status the requester/registrant's vaccination status */
		void setVaxStatus( List<VaxDose> status );

		/** @param status the requester/registrant's vaccination status */
		default Advice with( final VaxDose... status )
		{
			setVaxStatus( Arrays.asList( status ) );
			return this;
		}
	}

	public interface Treatment extends Fact
	{
		public interface Treater extends Actor<Treatment>
		{

		}

		/**
		 * @return
		 */
		public List<VaxDose> getDoses();
	}

	/**
	 * O01 {@link PersonOrg}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public interface PersonOrg extends GlobalConfig
	{
		String BASE_KEY = "person";

		@DefaultValue( "const(1)" )
		String myConfidence();

		@DefaultValue( "const(0)" )
		String myComplacencyDist();

		@DefaultValue( "const(0.5)" )
		String myCalculationDist();

		// DKTP-1 ~ cauchy(x_0=59;gamma=3.5), subtract 14 days invitation delay
		@DefaultValue( "cauchy(45 day;3.5 day)" )
		String myVaccinationDelayDist();

		@DefaultValue( "[25 day;+inf>" )
		String myVaccinationDelayRange();
	}

	/**
	 * O05 {@link HealthOrg}, eg
	 * <ul>
	 * <li>RIVM/RVP (National Vaccination Program)</li>
	 * <li>DVP (Vaccine and Prevention Service)</li>
	 * <li>JGZ (Youth Health Service)</li>
	 * <ul>
	 * <li>CB-MZ (Maternal and Child Health [MCH] centers)</li>
	 * <li>CJG (Youth and Family Center [YFC])</li>
	 * <li>GGD (Municipal Health Service [MHS])</li>
	 * </ul>
	 * </ul>
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public interface HealthOrg extends GlobalConfig
	{
		String BASE_KEY = "health";

		String RIVM_NAME = "rivm";

		// sec min hour date month weekday[0=Sun] year 
		@DefaultValue( "0 0 0 L-3 * ? *" )
		Timing campaignTiming();

		@DefaultValue( " Normal ( -0.5 day;1 h )" )
		String adviceDelay();

		/** @return advisor confidence in vaccine efficacy and safety */
		@DefaultValue( "const(1)" )
		String advisorConfidence();

		/** healthOrg inherently urgent (non-complacent) about herd immunity */
		@DefaultValue( "const(0)" )
		String advisorComplacency();

		/** @return (regional) GGD-to-person proximity, eg thuisvaccinatie.nl */
		@DefaultValue( "const(1)" )
		String occasionProximity();

		/** @return personal understandability of vaccination activity */
		@DefaultValue( "const(1)" )
		String occasionClarity();

		/** @return personal (financial/social) gain of vaccination activity */
		@DefaultValue( "const(1)" )
		String occasionUtility();

		/** @return perceived appeal of vaccination event */
		@DefaultValue( "const(1)" )
		String occasionAffinity();
	}

	private final ScenarioConfig config = ScenarioConfig.getOrFromYaml();

	@Inject
	private ProbabilityDistribution.Parser distParser;

	@Inject
	private Actor.Factory actorFactory;

	@Inject
	private FactExchange factExchange;

	@Inject
	private Scheduler scheduler;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	private Timing campaignTiming;
	private ProbabilityDistribution<Duration> adviceDelay;
	private ProbabilityDistribution<Number> advisorConfidence;
	private ProbabilityDistribution<Number> advisorComplacency;
	private ProbabilityDistribution<VaxOccasion> treatOccasionDist;
	private ProbabilityDistribution<VaxHesitancy> personHesitancyDist;
	private ProbabilityDistribution<Duration> myVaccinationDelay;
	private Range<Duration> myVaccinationDelayRange;

	private void initHealthParameters() throws ParseException
	{

		final HealthOrg healthCfg = this.config.healthOrg();
		LOG.trace( "health org cfg: {}", healthCfg );

		this.campaignTiming = healthCfg.campaignTiming();
		this.adviceDelay = this.distParser
				.parseQuantity( healthCfg.adviceDelay() ).abs()
				.map( Duration::of );
		this.advisorConfidence = this.distParser
				.parse( healthCfg.advisorConfidence() );
		this.advisorComplacency = this.distParser
				.parse( healthCfg.advisorComplacency() );

		this.treatOccasionDist = new ProbabilityDistribution<VaxOccasion>()
		{
			private final ProbabilityDistribution<Number> occasionProximity = distParser
					.parse( healthCfg.occasionProximity() );
			private final ProbabilityDistribution<Number> occasionClarity = distParser
					.parse( healthCfg.occasionClarity() );
			private final ProbabilityDistribution<Number> occasionUtility = distParser
					.parse( healthCfg.occasionUtility() );
			private final ProbabilityDistribution<Number> occasionAffinity = distParser
					.parse( healthCfg.occasionAffinity() );

			@Override
			public VaxOccasion draw()
			{
				return VaxOccasion.of( this.occasionProximity.draw(),
						this.occasionClarity.draw(),
						this.occasionUtility.draw(),
						this.occasionAffinity.draw() );
			}
		};
	}

	private void initPersonParameters() throws ParseException
	{
		final PersonOrg personCfg = this.config.personOrg();
		LOG.trace( "person cfg: {}", personCfg );

		this.personHesitancyDist = new ProbabilityDistribution<VaxHesitancy>()
		{
			private ProbabilityDistribution<Number> myConfidence = distParser
					.parse( personCfg.myConfidence() );
			private ProbabilityDistribution<Number> myComplacency = distParser
					.parse( personCfg.myComplacencyDist() );
			private ProbabilityDistribution<Number> myCalculation = distParser
					.parse( personCfg.myCalculationDist() );

			@Override
			public VaxHesitancy draw()
			{
				return VaxHesitancy.averager( //org.id(),
						this.myConfidence.draw(), this.myComplacency.draw(),
						this.myCalculation.draw() );
			}

		};
		this.myVaccinationDelayRange = Range
				.parse( personCfg.myVaccinationDelayRange(), Duration.class );
		this.myVaccinationDelay = this.distParser
				.parseQuantity( personCfg.myVaccinationDelayDist() )
				.map( Duration::of ).map( this.myVaccinationDelayRange::crop );
	}

	private static void logFact( final Fact f )
	{
		LOG.trace( f );
	}

	private static void logError( final Throwable t )
	{
		LOG.error( t );
	}

	@Override
	public void init() throws Exception
	{
		// TODO implement config to dist-parsing-dynabean (ie apply caching)
		initHealthParameters();
		initPersonParameters();

		// Add RIVM (O05)
		final Actor<Fact> rivm = initHealth( HealthOrg.RIVM_NAME );
		this.factExchange.snif().subscribe( LOG::trace,
				HesitantScenario::logError );

//		// TODO Add Media (O04) and respective pos/neg hypes
//		final Actor<Fact> media = this.actorFactory.create( "media" );
//		rivm.specialist( Informer.class, this::initInformer );

		// Add Persons (O01)
		final Actor<Fact> mom = initPerson( "mom" );
		final Actor<Fact> dad = initPerson( "dad" );
		final Actor<Fact> kid = initPerson( "kid" );

		// test
		for( int i = 0; i < 100; i++ )
			LOG.trace( "{}", this.myVaccinationDelay.draw() );

		// configure parental relations TODO from social network config?
		kid.specialist( Redirector.class ).consultAdvisors( rivm.id() )
				.consultPersons( mom.id(), dad.id() );

//		final Map<Actor.ID, Level> relationWeights = new HashMap<>();
//		relationWeights.put( mom.id(), Level.HI );
//		relationWeights.put( dad.id(), Level.HI );

	}

	private void reminder( final Advisor advisor, final VaxRegistrant indiv,
		final VaxDose vax )
	{
		advisor.after( this.adviceDelay.draw() ).call( t ->
		{
			// same values for entire cohort
			final Number conf = this.advisorConfidence.draw();
			final Number comp = this.advisorComplacency.draw();
			final VaxOccasion occ = this.treatOccasionDist.draw();

			// TODO filter by status
//			if( indiv.isEligible( vax ) )
//			{
//				final Fact mot = advisor
//						.initiate( Motivation.class, indiv.personRef,
//								indiv.regRef )
//						.withConfidence( conf ).withComplacency( comp )
//						.with( occ ).commit();
//				LOG.trace( "Campaign {}: {}", indiv.regRef.prettyHash(), mot );
//			}
		} );
	}

	private Actor<Fact> initHealth( final String name ) throws ParseException
	{
		final Actor<Fact> org = this.actorFactory.create( name );
		final Advisor advisor = org.specialist( Advisor.class );
		final NavigableSet<VaxRegistrant> cohorts = new ConcurrentSkipListSet<>();

		// self-initiate national campaign Advice 
		advisor.atEach( this.campaignTiming,
				t -> advisor.initiate( advisor.id() ).commit() );
		// add request handling behavior
		advisor.emit( FactKind.REQUESTED ).filter( Fact::isInternal )
				.subscribe( rq ->
				{
					if( rq.isInternal() )
					{
						// TODO execute extra call/recall, e.g. during outbreak
					} else
					{
						// respond to external request with schedule/invite
						after( this.adviceDelay.draw() ).call( t ->
						{
							// invite for first vaxdose 
							rq.getVaxStatus();
							advisor.initiate( Motivation.class, rq.creatorRef(),
									rq.id() )
//									.withRemaining( VaxProcess.RVP_SCHEDULE )
									.commit();
						} );
						cohorts.add( new VaxRegistrant( rq ) );
					}
					LOG.trace( "{} handled, cohort: {}", rq, cohorts );
				}, HesitantScenario::logError );
		return org;
	}

	private Actor<Fact> initPerson( final String name ) throws ParseException
	{
		final Actor<Fact> org = this.actorFactory.create( name );
		// add opinion spreading dynamics 
		org.specialist( Opinionator.class, opin ->
		{
			// TODO spread (pos/neg) experience among (direct) social contacts
		} );

		// add Motivation execution behavior
		org.specialist( Motivator.class, motivator ->
		{
			motivator.withAttitude( this.personHesitancyDist.draw() )
					.emit( FactKind.REQUESTED ).subscribe( rq ->
					{
						motivator.getAttitude().observeRisk( rq.creatorRef(),
								rq.getConfidence(), rq.getComplacency() );
						rq.getOccasions()
								.stream().filter( occ -> !motivator
										.getAttitude().isHesitant( occ ) )
								.forEach( occ ->
								{
									motivator
											.initiate( Redirection.class,
													org.id() )
											.with( GoalType.VACCINATE )
											.commit();
									// TODO implement Goal handling
								} );

					}, HesitantScenario::logError );
		} );
		return org;
	}

}