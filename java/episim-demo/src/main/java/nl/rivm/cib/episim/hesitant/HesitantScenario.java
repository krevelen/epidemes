package nl.rivm.cib.episim.hesitant;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.config.GlobalConfig;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.random.DistributionParsable;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
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
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitant;
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
	 * T13 {@link Opinion} transactions are initiated by another O01
	 * {@link PersonOrg}'s A15 {@link Redirector}
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
		/**
		 * A14 {@link Motivator} handles T14 {@link Motivation} execution,
		 * occasionally initiating a {@link Redirection}
		 */
		public interface Motivator extends Actor<Motivation>
		{

			VaxHesitant getAttitude();

			void setAttitude( VaxHesitant attitude );

			default Motivator withAttitude( VaxHesitant attitude )
			{
				setAttitude( attitude );
				return this;
			}
		}

		Level getVaccineRisk(); // auto-add dynabean property

		void setVaccineRisk( Level risk );

		default Motivation withVaccineRisk( final Level risk )
		{
			setVaccineRisk( risk );
			return this;
		}

		Level getDiseaseRisk(); // auto-add dynabean property

		void setDiseaseRisk( Level risk );

		default Motivation withDiseaseRisk( final Level risk )
		{
			setDiseaseRisk( risk );
			return this;
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

		@DefaultValue( HealthOrg.RIVM_NAME )
		String healthAdvisorName();

		default Actor<Fact> init( final Actor<Fact> actor )
			throws ParseException
		{
			final Actor.ID healthRef = actor.peerRef( healthAdvisorName() );

			actor.specialist( Redirector.class,
					director -> actor.after( 0 ).call( t -> director
							.initiate( Advice.class, healthRef ).commit() ) );

			// add Motivation execution behavior
			actor.specialist( Motivator.class, motivator ->
			{
				motivator.emit( FactKind.REQUESTED ).subscribe( rq ->
				{
					motivator.getAttitude().observe( rq.creatorRef(),
							rq.getVaccineRisk().toNumber(),
							rq.getDiseaseRisk().toNumber() );
					LOG.trace( "{} handling {}", actor.id().unwrap(), rq );
					rq.getOccasions().stream().filter(
							occ -> !motivator.getAttitude().isHesitant( occ ) )
							.forEach( occ ->
							{
								LOG.trace( "{} vaccinating for {}",
										actor.id().unwrap(), occ );
								motivator
										.initiate( Redirection.class,
												actor.id() )
										.with( GoalType.VACCINATE ).commit();
							} );

				}, HesitantScenario::logError );
			} );
			return actor;
		}
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
		@ConverterClass( DistributionParsable.FromString.class )
		DistributionParsable<?> adviceStDelay();

		default Actor<Fact> init( final Actor<Fact> actor )
			throws ParseException
		{
			final Advisor advisor = actor.specialist( Advisor.class );
			final ProbabilityDistribution.Parser distParser = actor.binder()
					.inject( ProbabilityDistribution.Parser.class );

			// self-initiate national campaign Advice 
			final DateTimeFormatter dtFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
			final ZonedDateTime offset = actor.offset();
			advisor.atEach( campaignTiming().offset( offset ).iterate(),
					t -> advisor.initiate( advisor.id() ).commit() );

			// add request handling behavior
			final Set<Actor.ID> cohort = new HashSet<>();
			final ProbabilityDistribution<Duration> stDelay = adviceStDelay()
					.parse( distParser, Time.class ).abs().map( Duration::of );
			advisor.emit( FactKind.REQUESTED ).subscribe( rq ->
			{
				final Duration delay = stDelay.draw();
				LOG.trace( "{} handling {}, delay: {}, cohort: {}",
						advisor.id().unwrap(), rq.prettify(),
						delay.prettify( TimeUnits.HOURS, 1 ), cohort );
				if( rq.creatorRef().equals( advisor.id() ) )
				{
					// execute internal request for national campaign
					cohort.forEach( targetId -> advisor.after( delay )
							.call( t -> LOG.trace( "Response (int): {}", advisor
									.initiate( Motivation.class, targetId,
											rq.id() )
									.withVaccineRisk( Level.LO )
									.withDiseaseRisk( Level.HI )
									.with( VaxOccasion.of( 1, 1, 1, 1 ) )
									.commit() ) ) );
				} else // respond to external request
				{
					// FIXME schedule addition/deletion per cohort
					cohort.add( rq.creatorRef().organizationRef() );

					advisor.after( delay ).call( t -> LOG.trace(
							"Response (ext): {}",
							advisor.respond( rq, FactKind.STATED ).commit() ) );
				}
			}, HesitantScenario::logError );
			return actor;
		}
	}

	private ScenarioConfig config;

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

	@Override
	public void init() throws Exception
	{
		this.config = ScenarioConfig.getOrFromYaml();
		// FIXME how do actors communicate with each other, e.g. message bus ??

		// Add RIVM (O05)
		final HealthOrg hcfg = this.config.healthOrg();
		LOG.trace( "rivm cfg: {}", hcfg );
		hcfg.init( this.actorFactory.create( HealthOrg.RIVM_NAME ) );

//		// Add Media (O04)
//		final Actor<Fact> media = this.actorFactory.create( "media" );
//		rivm.specialist( Informer.class, this::initInformer );

		// Add a Person
		final PersonOrg personConfig = this.config.personOrg();
		final VaxHesitant.WeightedAverager hes = new VaxHesitant.WeightedAverager(
				Actor.ID.of( "self", null ), 1, 0, .5, id -> .5 );
		LOG.trace( "person cfg: {}, hes: {}", personConfig, hes );

		final Actor<Fact> joram = personConfig
				.init( this.actorFactory.create( "joram" ) );

		final Actor<Fact> mom = personConfig
				.init( this.actorFactory.create( "mom" ) );
		final Actor<Fact> dad = personConfig
				.init( this.actorFactory.create( "dad" ) );
		final Map<Actor.ID, Level> relationWeights = new HashMap<>();
		relationWeights.put( mom.id(), Level.HI );
		relationWeights.put( dad.id(), Level.HI );
		joram.after( 0, TimeUnits.DAYS ).call( t -> relationWeights.forEach(
				( k, v ) -> joram.initiate( Opinion.class, k ).commit() ) );
//		if( !actionable( vo ) )
//		{
//			// decline
//		} else
//		{
//			// promise/execute/state
//		}

	}

}