package nl.rivm.cib.episim.hesitant;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalConfig;
import io.coala.config.ConfigUtil;
import io.coala.config.GlobalConfig;
import io.coala.config.YamlUtil;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.random.QuantityDistribution;
import io.coala.random.DistributionParsable;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.ReplicateConfig;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import io.coala.util.FileUtil;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Advice.Advisor;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Information.Informer;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Motivation.Motivator;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Opinion.Opinionator;
import nl.rivm.cib.episim.hesitant.HesitantScenario.Redirection.Director;
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

	/**
	 * The {@link Level} represents ordinal preferences on a three-point Likert
	 * scale with declared (crisp) {@link #toNumber()} values:
	 * <p>
	 * {@link #LO} = 0, {@link #MID} = .5, and {@link #HI} = 1.
	 * <p>
	 * The {@link #MID} range boundaries used during conversion by
	 * {@link #valueOf(Number)}, i.e. {@link #MID_LOWER}=0.25 and
	 * {@link #MID_UPPER}=0.75, are both <em>exclusive</em> in order to maintain
	 * inversion symmetry:
	 * <p>
	 * <code>{@link #LO} &le; {@link #MID_LOWER} &lt; {@link #MID} &lt; {@link #MID_UPPER} &le; {@link #HI}</code>
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public enum Level
	{
		LO, MID, HI;

		public static final BigDecimal MID_LOWER = BigDecimal.valueOf( 25, 2 );

		public static final BigDecimal MID_UPPER = BigDecimal.valueOf( 75, 2 );

		/**
		 * @param value the {@link Number} to map
		 * @return the respective {@link Level}
		 */
		public static Level valueOf( final Number value )
		{
			final BigDecimal d = DecimalUtil.valueOf( value );
			final Level result = d.compareTo( MID_LOWER ) > 0
					? d.compareTo( MID_UPPER ) < 0 ? MID : HI : LO;
			return result;
		}

		public Level invert()
		{
			return values()[values().length - 1 - ordinal()];
		}

		private static final BigDecimal MAX = BigDecimal
				.valueOf( values().length - 1 );

		private BigDecimal number = null;

		public BigDecimal toNumber()
		{
			return this.number != null ? this.number
					: (this.number = DecimalUtil.divide( ordinal(), MAX ));
		}
	}

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
	 * T13 {@link Opinion} transactions are initiated by another O01 Person's
	 * A15 {@link Director}
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
	 * <li>another O01 Person's A13 {@link Opinionator} (e.g. persuade a,
	 * relative, colleague, or other social network relation);</li>
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

		}

		Level getVaccineRisk();

		void setVaccineRisk( Level risk );

		default Motivation withVaccineRisk( final Level risk )
		{
			setVaccineRisk( risk );
			return this;
		}

		Level getDiseaseRisk();

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
	 * <li>the O01 Person's own A12 {@link Disruptor} behavior;</li>
	 * <li>the O01 Person's own A14 {@link Motivator}; or</li>
	 * <li>the O01 Person's own A15 {@link Director}.</li>
	 * </ul>
	 */
	public interface Redirection extends Fact
	{
		/** A15 {@link Director} handles T15 {@link Redirection} execution */
		public interface Director extends Actor<Redirection>
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

	public interface PersonConfig extends GlobalConfig
	{

		@DefaultValue( "rivm" )
		String healthAdvisorName();

		default Actor<Fact> init( final Actor<Fact> actor )
			throws ParseException
		{
			actor.specialist( Director.class,
					director -> actor.after( 0 ).call( t ->
					{
						// request RIVM advice
						LOG.trace(
								"Initiating {}", director
										.initiate( Advice.class,
												actor.peerRef(
														healthAdvisorName() ) )
										.commit() );
//						LOG.trace(
//								"Initiating " + director
//										.initiate( Advice.class,
//												actor.peerRef(
//														healthAdvisorName() ) )
//										.commit() );
					} ) );
			actor.specialist( Motivator.class, motivator -> motivator
					.emit( FactKind.REQUESTED ).subscribe( rq ->
					{
						LOG.trace( "Handling {}", rq );
						// TODO update position
						// motivator.respond( rq, FactKind.STATED );
					}, e -> LOG.error( "Problem", e ) ) );
			return actor;
		}
	}

	public interface HealthConfig extends GlobalConfig
	{
		// sec min hour date month weekday[0=Sun] year 
		@DefaultValue( "0 0 0 L-3 * ? *" )
		Timing adviceRqTiming();

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
			advisor.atEach( adviceRqTiming().iterate() ).subscribe(
					t -> advisor.initiate( advisor.id() ).commit(),
					e -> LOG.error( "Problem", e ) );

			// add request handling behavior
			final Set<Actor.ID> cohort = new HashSet<>();
			final QuantityDistribution<Time> stDelay = adviceStDelay()
					.parse( distParser, Time.class ).abs();
			advisor.emit( FactKind.REQUESTED ).subscribe( rq ->
			{
				final Quantity<Time> delay = stDelay.draw();
				LOG.trace( "{} handling {}, delay: {}, cohort: {}",
						advisor.id(), rq, delay, cohort );
				if( rq.creatorRef().equals( advisor.id() ) )
				{
					// execute national campaign
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
			}, e -> LOG.error( "Problem", e ) );
			return actor;
		}
	}

	/**
	 * {@link ScenarioConfig}
	 */
	public interface ScenarioConfig extends ReplicateConfig
	{

		String HESITANT_YAML_FILE = "hesitant.yaml";

		String SCENARIO_NAME = "scenario";

		String SCENARIO_TYPE_KEY = SCENARIO_NAME + KEY_SEP + "init";

		String SCENARIO_TYPE_DEFAULT = "nl.rivm.cib.episim.hesitant.HesitantScenario";

		@Key( SCENARIO_TYPE_KEY )
		@DefaultValue( SCENARIO_TYPE_DEFAULT )
		Class<? extends Scenario> scenarioType();

		@Key( DURATION_KEY )
		@DefaultValue( "" + 100 )
		@Override // add new default value
		BigDecimal rawDuration();

		default Scenario createScenario()
		{
			final Map<String, Object> export = ConfigUtil.export( this );
			export.put( ReplicateConfig.ID_KEY, SCENARIO_NAME );
			export.put( LocalConfig.ID_KEY, SCENARIO_NAME );

			// configure replication FIXME via LocalConfig?
			ReplicateConfig.getOrCreate( export );

			return ConfigCache.getOrCreate( LocalConfig.class, export )
					.createBinder().inject( scenarioType() );
		}

		default <C extends GlobalConfig> C actorConfig( final Actor.ID id,
			final Class<C> configType )
		{
			return actorConfig( id.unwrap().toString(), configType );
		}

		default <C extends GlobalConfig> C actorConfig( final String path,
			final Class<C> configType )
		{
			return subConfig( path, configType,
					//Collections.singletonMap( LocalConfig.ID_KEY, id ),
					ConfigUtil.export( this, Pattern.compile(
							"^" + Pattern.quote( LocalConfig.BINDER_KEY ) ) ) );
		}

		/**
		 * @param imports optional extra configuration settings
		 * @return the imported values
		 * @throws IOException if reading from {@link #HESITANT_YAML_FILE} fails
		 */
		static ScenarioConfig fromYaml( final Map<?, ?>... imports )
			throws IOException
		{
			return ConfigFactory.create( ScenarioConfig.class, ConfigUtil.join(
					YamlUtil.flattenYaml(
							FileUtil.toInputStream( HESITANT_YAML_FILE ) ),
					imports ) );
		}
	}

	@InjectConfig
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

	@Override
	public void init() throws Exception
	{
		// FIXME how do actors communicate with each other, e.g. message bus ??

		// Add RIVM (O05)
		final HealthConfig hcfg = this.config.actorConfig( "health",
				HealthConfig.class );
		LOG.trace( "rivm cfg: {}", hcfg );
		hcfg.init( this.actorFactory.create( "rivm" ) );

//		// Add Media (O04)
//		final Actor<Fact> media = this.actorFactory.create( "media" );
//		rivm.specialist( Informer.class, this::initInformer );

		// Add a Person
		final PersonConfig personConfig = this.config.actorConfig( "person",
				PersonConfig.class );
		personConfig.init( this.actorFactory.create( "joram" ) );

//		if( !actionable( vo ) )
//		{
//			// decline
//		} else
//		{
//			// promise/execute/state
//		}

	}

	/**
	 * @param args the command line arguments
	 * @throws Exception
	 */
	public static void main( final String[] args ) throws Exception
	{
		final ScenarioConfig config = ScenarioConfig.fromYaml();
		LOG.trace( "HESITANT scenario starting, config: {}", config.toYAML() );
		config.createScenario().run();
		LOG.info( "HESITANT completed" );
	}

}