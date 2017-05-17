package nl.rivm.cib.episim.hesitant;

import java.text.ParseException;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Logger;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.math.Range;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import nl.rivm.cib.episim.hesitant.Advice.Advisor;
import nl.rivm.cib.episim.hesitant.Motivation.Motivator;
import nl.rivm.cib.episim.hesitant.Opinion.Opinionator;
import nl.rivm.cib.episim.hesitant.Redirection.Redirector;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy;

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

	private final HesitantScenarioConfig config = HesitantScenarioConfig
			.getOrFromYaml();

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
//	private ProbabilityDistribution<Number> advisorConfidence;
//	private ProbabilityDistribution<Number> advisorComplacency;
//	private ProbabilityDistribution<VaxOccasion> treatOccasionDist;
	private ProbabilityDistribution<VaxHesitancy> personHesitancyDist;
	private ProbabilityDistribution<Duration> myVaccinationDelay;
	private Range<Duration> myVaccinationDelayRange;

	private void initHealthParameters() throws ParseException
	{

		final HealthConfig healthCfg = this.config.healthOrg();
		LOG.trace( "health org cfg: {}", healthCfg );

		this.campaignTiming = healthCfg.campaignTiming();
		this.adviceDelay = this.distParser
				.parseQuantity( healthCfg.adviceDelay() ).abs()
				.map( Duration::of );
//		this.advisorConfidence = this.distParser
//				.parse( healthCfg.advisorConfidence() );
//		this.advisorComplacency = this.distParser
//				.parse( healthCfg.advisorComplacency() );
//
//		this.treatOccasionDist = new ProbabilityDistribution<VaxOccasion>()
//		{
//			private final ProbabilityDistribution<Number> occasionProximity = distParser
//					.parse( healthCfg.occasionProximity() );
//			private final ProbabilityDistribution<Number> occasionClarity = distParser
//					.parse( healthCfg.occasionClarity() );
//			private final ProbabilityDistribution<Number> occasionUtility = distParser
//					.parse( healthCfg.occasionUtility() );
//			private final ProbabilityDistribution<Number> occasionAffinity = distParser
//					.parse( healthCfg.occasionAffinity() );
//
//			@Override
//			public VaxOccasion draw()
//			{
//				return VaxOccasion.of( this.occasionProximity.draw(),
//						this.occasionClarity.draw(),
//						this.occasionUtility.draw(),
//						this.occasionAffinity.draw() );
//			}
//		};
	}

	private void initPersonParameters() throws ParseException
	{
		final PersonConfig personCfg = this.config.personOrg();
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
				return VaxHesitancy.SimpleWeightedAverager.of(
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

//	private static void logFact( final Fact f )
//	{
//		LOG.trace( f );
//	}

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
		final Actor<Fact> rivm = initHealth( HealthConfig.RIVM_NAME );
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
		kid.subRole( Redirector.class ).consultAdvisors( rivm.id() )
				.consultPersons( mom.id(), dad.id() );

//		final Map<Actor.ID, Level> relationWeights = new HashMap<>();
//		relationWeights.put( mom.id(), Level.HI );
//		relationWeights.put( dad.id(), Level.HI );

	}

//	private void reminder( final Advisor advisor, final VaxRegistrant indiv,
//		final VaxDose vax )
//	{
//		advisor.after( this.adviceDelay.draw() ).call( t ->
//		{
//			// same values for entire cohort
//			final Number conf = this.advisorConfidence.draw();
//			final Number comp = this.advisorComplacency.draw();
//			final VaxOccasion occ = this.treatOccasionDist.draw();
//
//			// TODO filter by status
////			if( indiv.isEligible( vax ) )
////			{
////				final Fact mot = advisor
////						.initiate( Motivation.class, indiv.personRef,
////								indiv.regRef )
////						.withConfidence( conf ).withComplacency( comp )
////						.with( occ ).commit();
////				LOG.trace( "Campaign {}: {}", indiv.regRef.prettyHash(), mot );
////			}
//		} );
//	}

	private Actor<Fact> initHealth( final String name ) throws ParseException
	{
		final Actor<Fact> org = this.actorFactory.create( name );
		final Advisor advisor = org.subRole( Advisor.class );
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
			motivator.with( this.personHesitancyDist.draw() )
					.emit( FactKind.REQUESTED ).subscribe( rq ->
					{
//						final VaxHesitancy att = (VaxHesitancy)rq.getAttitude();
//						motivator.getAttitude().observeRisk( rq.creatorRef(),
//								att.getConfidence(), att.getComplacency() );
//						rq.getOccasions()
//								.stream().filter( occ -> !motivator
//										.getAttitude().isHesitant( occ ) )
//								.forEach( occ ->
//								{
//									motivator
//											.initiate( Redirection.class,
//													org.id() )
//											.with( GoalType.VACCINATE )
//											.commit();
//									// TODO implement Goal handling
//								} );

					}, HesitantScenario::logError );
		} );
		return org;
	}

}