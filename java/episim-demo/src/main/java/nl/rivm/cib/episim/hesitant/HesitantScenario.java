package nl.rivm.cib.episim.hesitant;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Logger;

import io.coala.config.GlobalConfig;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.math.Range;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
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

		/**
		 * @return {@link VaxOccasion}s, typically added by a Health initiator
		 */
		List<VaxOccasion> getOccasions();

		void setOccasions( List<VaxOccasion> occasions );

		default Motivation with( List<VaxOccasion> occasions )
		{
			setOccasions( occasions );
			return this;
		}

		default Motivation with( final VaxOccasion... occasions )
		{
			return with(
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
			default Redirector referToPersons( final Actor.ID... ids )
			{
				return ids == null || ids.length == 0 ? this
						: referToPersons( Arrays.asList( ids ) );
			}

			/** @param ids */
			default Redirector referToPersons( final Iterable<Actor.ID> ids )
			{
				ids.forEach( id -> initiate( Opinion.class, id ).commit() );
				return this;
			}

			/** @param ids */
			default Redirector referToAdvisors( final Actor.ID... ids )
			{
				return ids == null || ids.length == 0 ? this
						: referToAdvisors( Arrays.asList( ids ) );
			}

			/** @param ids */
			default Redirector referToAdvisors( final Iterable<Actor.ID> ids )
			{
				ids.forEach( id -> initiate( Advice.class, id ).commit() );
				return this;
			}

			/** @param ids */
			default Redirector referToMedia( final Actor.ID... ids )
			{
				return ids == null || ids.length == 0 ? this
						: referToMedia( Arrays.asList( ids ) );
			}

			/** @param ids */
			default Redirector referToMedia( final Iterable<Actor.ID> ids )
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

	public interface Advice extends Fact
	{
		interface Advisor extends Actor<Advice>
		{
		}
	}

	public interface Treatment extends Fact
	{
		public interface Treater extends Actor<Treatment>
		{

		}
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

		/**
		 * 
		 * @return
		 */

		@DefaultValue( "const(1)" )
		String myConfidence();

		@DefaultValue( "const(0)" )
		String myComplacencyDist();

		@DefaultValue( "const(0.5)" )
		String myCalculationDist();

		// DKTP-1 ~ cauchy(x_0=59;gamma=3.5)
		@DefaultValue( "cauchy(59 day;3.5 day)" )
		String myVaccinationDelayDist();

		@DefaultValue( "[25 day;125 day]" )
		String myVaccinationDelayRange();
	}

	/**
	 * O05 {@link HealthOrg}
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

		@DefaultValue( "const(1)" )
		String advisorConfidence();

		@DefaultValue( "const(0)" )
		String advisorComplacency();

		// TODO categorize

		@DefaultValue( "const(1)" )
		String occasionProximity();

		@DefaultValue( "const(1)" )
		String occasionClarity();

		@DefaultValue( "const(1)" )
		String occasionUtility();

		@DefaultValue( "const(1)" )
		String occasionAffinity();
	}

	private final ScenarioConfig config = ScenarioConfig.getOrFromYaml();

	@Inject
	private ProbabilityDistribution.Parser distParser;

	@Inject
	private Actor.Factory actorFactory;

	@Inject
	private Scheduler scheduler;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	private static void logError( final Throwable t )
	{
		LOG.error( "Problem", t );
	}

	private Timing campaignTiming;
	private ProbabilityDistribution<Duration> adviceDelay;
	private ProbabilityDistribution<Number> advisorConfidence;
	private ProbabilityDistribution<Number> advisorComplacency;
	private ProbabilityDistribution<Number> occasionProximity;
	private ProbabilityDistribution<Number> occasionClarity;
	private ProbabilityDistribution<Number> occasionUtility;
	private ProbabilityDistribution<Number> occasionAffinity;
	private ProbabilityDistribution<Number> myConfidence;
	private ProbabilityDistribution<Number> myComplacency;
	private ProbabilityDistribution<Number> myCalculation;
	private ProbabilityDistribution<Duration> myVaccinationDelay;
	private Range<Duration> myVaccinationDelayRange;

	@Override
	public void init() throws Exception
	{

		final HealthOrg healthCfg = this.config.healthOrg();
		LOG.trace( "health org cfg: {}", healthCfg );

		// TODO implement config to dist-parsing-dynabean (to allow caching)
		this.campaignTiming = healthCfg.campaignTiming();
		this.adviceDelay = this.distParser
				.parseQuantity( healthCfg.adviceDelay() ).abs()
				.map( Duration::of );
		this.advisorConfidence = this.distParser
				.parse( healthCfg.advisorConfidence() );
		this.advisorComplacency = this.distParser
				.parse( healthCfg.advisorComplacency() );
		this.occasionProximity = this.distParser
				.parse( healthCfg.occasionProximity() );
		this.occasionClarity = this.distParser
				.parse( healthCfg.occasionClarity() );
		this.occasionUtility = this.distParser
				.parse( healthCfg.occasionUtility() );
		this.occasionAffinity = this.distParser
				.parse( healthCfg.occasionAffinity() );

		final PersonOrg personCfg = this.config.personOrg();
		LOG.trace( "person cfg: {}", personCfg );

		this.myConfidence = this.distParser.parse( personCfg.myConfidence() );
		this.myComplacency = this.distParser
				.parse( personCfg.myComplacencyDist() );
		this.myCalculation = this.distParser
				.parse( personCfg.myCalculationDist() );
		this.myVaccinationDelayRange = Range
				.parse( personCfg.myVaccinationDelayRange(), Duration.class );
		this.myVaccinationDelay = this.distParser
				.parseQuantity( personCfg.myVaccinationDelayDist() )
				.map( Duration::of ).map( this.myVaccinationDelayRange::crop );
		LOG.trace( "crop {} to {}", Duration.ZERO,
				this.myVaccinationDelayRange.crop( Duration.ZERO ) );

		// Add RIVM (O05)
		final Actor<Fact> rivm = initHealth( HealthOrg.RIVM_NAME );

//		// Add Media (O04)
//		final Actor<Fact> media = this.actorFactory.create( "media" );
//		rivm.specialist( Informer.class, this::initInformer );

		// Add Persons (O01)
		final Actor<Fact> mom = initPerson( "mom" );
		final Actor<Fact> dad = initPerson( "dad" );
		final Actor<Fact> kid = initPerson( "kid" );
		for( int i = 0; i < 100; i++ )
			LOG.trace( "{}", this.myVaccinationDelay.draw() );

		// configure parental relations TODO from social network config?
		kid.specialist( Redirector.class ).referToAdvisors( rivm.id() )
				.referToPersons( mom.id(), dad.id() );

//		final Map<Actor.ID, Level> relationWeights = new HashMap<>();
//		relationWeights.put( mom.id(), Level.HI );
//		relationWeights.put( dad.id(), Level.HI );

	}

	private void onCampaignRequest( final Advisor advisor, final Fact.ID rqId,
		final Set<Actor.ID> cohort )
	{
		advisor.after( this.adviceDelay.draw() ).call( t ->
		{
			final Number conf = this.advisorConfidence.draw();
			final Number comp = this.advisorComplacency.draw();
			final VaxOccasion occ = VaxOccasion.of(
					this.occasionProximity.draw(), this.occasionClarity.draw(),
					this.occasionUtility.draw(), this.occasionAffinity.draw() );

			cohort.forEach( targetId ->
			{
				final Fact mot = advisor
						.initiate( Motivation.class, targetId, rqId )
						.withConfidence( conf ).withComplacency( comp )
						.with( occ ).commit();
				LOG.trace( "Campaign {}: {}", rqId.prettyHash(), mot );
			} );
		} );
	}

	private Actor<Fact> initHealth( final String name ) throws ParseException
	{
		final Actor<Fact> org = this.actorFactory.create( name );
		final Advisor advisor = org.specialist( Advisor.class );

		// self-initiate national campaign Advice 
		advisor.atEach( this.campaignTiming,
				t -> advisor.initiate( advisor.id() ).commit() );
		// add request handling behavior
		final Set<Actor.ID> cohort = new HashSet<>();
		advisor.emit( FactKind.REQUESTED ).filter( Fact::isInternal )
				.subscribe( rq ->
				{
					if( rq.isInternal() )
						// execute internal request (for known cohort invitate)
						onCampaignRequest( advisor, rq.id(), cohort );
					else
						// no explicit response to external request (for advice)
						cohort.add( rq.creatorRef().organizationRef() );
					LOG.trace( "{} handled, cohort: {}", rq, cohort );
				}, HesitantScenario::logError );
		return org;
	}

	private Actor<Fact> initPerson( final String name ) throws ParseException
	{
		final Actor<Fact> org = this.actorFactory.create( name );
		// add opinion spreading dynamics 
		org.specialist( Opinionator.class, opin ->
		{
		} );

		// add Motivation execution behavior
		org.specialist( Motivator.class, motivator ->
		{
			motivator
					.withAttitude( VaxHesitancy.averager( org.id(),
							this.myConfidence.draw(), this.myComplacency.draw(),
							this.myCalculation.draw() ) )
					.emit( FactKind.REQUESTED ).subscribe( rq ->
					{
						motivator.getAttitude().observeRisk( rq.creatorRef(),
								rq.getConfidence(), rq.getComplacency() );
						LOG.trace( "{} handling {}", org.id().unwrap(), rq );
						rq.getOccasions()
								.stream().filter( occ -> !motivator
										.getAttitude().isHesitant( occ ) )
								.forEach( occ ->
								{
									LOG.trace( "{} vaccinating for {}",
											org.id().unwrap(), occ );
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