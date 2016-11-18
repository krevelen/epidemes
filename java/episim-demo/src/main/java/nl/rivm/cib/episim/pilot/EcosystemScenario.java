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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.persist.JDBCConfig;
import io.coala.time.Duration;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;

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

	public interface Individual extends Actor<IndividualFact>
	{
		String birth();

		String gender();

		String location(); // should be consistent with activity site/vehicle

//		String lifephase(); // automaton based on age
//
//		Condition condition(); // default fall-back?
//
//		VaccineAttitude attitude(); // default fall-back?
//
//		Activity activity(); // belongs to a (common) routine pattern instance
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

	/** A12 {@link Guardian} executes T12 {@link Population} requests */
	public interface Demographer extends Actor<Population>
	{

	}

	/** T12 {@link Population} initiator(s): {@link Deme} */
	public interface Population extends DemeFact
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

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( EcosystemScenario.class );

	private final Scheduler scheduler;

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

	/**
	 * <ol>
	 * <li>init (contact, transport, care) location networks</li>
	 * <li>init (age, sex, opinion, birth/death) population networks</li>
	 * <li>init (travel, contact, risk) behaviors</li>
	 * <li>init disease conditions/compartments</li>
	 * <li>run opinion-immunization\\contact-transmission events</li>
	 * </ol>
	 * 
	 * @throws ParseException
	 */
	private void init() throws ParseException
	{
		EcosystemScenarioTest.LOG.info( "initializing..." );

		// create organization
		int total = 160000;
		long clock = System.currentTimeMillis();
		for( int i = 0; i < total; i++ )
		{
			if( System.currentTimeMillis() - clock > 1000 )
			{
				logProgress( i, total );
				clock = System.currentTimeMillis();
			}
//			final Actor<Fact> person = 
			this.actors.create( "person" + i );
//				final Mother mother = person.asExecutor( //Birth.class,
//						Mother.class );
//
//				// add business rule(s)
//				mother.emit( FactKind.REQUESTED ).subscribe( fact ->
//				{
//					after( Duration.of( 1, Units.DAYS ) ).call( t ->
//					{
//						final Birth st = mother.respond( fact, FactKind.STATED )
//								.with( "myParam1", "myValue1" ).commit( true );
//						LOG.trace( "t={}, {} responded: {} for incoming: {}",
//								t.prettify( this.actors.offset() ), mother.id(),
//								st, fact );
//					} );
//				} );
//
//				// initiate transactions with self
//				atEach( Timing.of( "0 0 0 14 * ? *" )
//						.offset( this.actors.offset() ).iterate(), t ->
//						{
//							mother.initiate( Birth.class, mother.id(), null,
//									t.add( 1 ), Collections.singletonMap(
//											"myParam0", "myValue0" ) );
//						} );

			//
//			Units.DAILY.toString(); // init new unit
//			final Scheduler scheduler = Dsol3Scheduler.of( "scenarioTest",
//					Instant.of( "0 days" ), Duration.of( "100 days" ), s ->
//					{
//						LOG.trace( "initialized, t={}",
//								s.now().prettify( NonSI.DAY, 1 ) );
//					} );
			//
//			//final Set<Individual> pop = new HashSet<>();
//			final int n_pop = 10;
////			final Set<Location> homes = new HashSet<>();
////			final int n_homes = 6000000;
////			final Set<Location> offices = new HashSet<>();
////			final int n_offices = 3000000;
//			final Infection measles = new Infection.Simple(
//					Amount.valueOf( 1, Units.DAILY ), Duration.of( "2 days" ),
//					Duration.of( "5 days" ), Duration.of( "9999 days" ),
//					Duration.of( "3 days" ), Duration.of( "7 days" ) );
			//
//			final TransmissionRoute route = TransmissionRoute.AIRBORNE;
//			final TransmissionSpace space = TransmissionSpace.of( scheduler,
//					route );
//			final Place rivm = Place.Simple.of( Place.RIVM_POSITION, Place.NO_ZIP,
//					space );
			//
//			final Collection<ContactIntensity> contactTypes = Collections
//					.singleton( ContactIntensity.FAMILY );
//			final Amount<Frequency> force = measles.getForceOfInfection(
//					rivm.getTransmissionSpace().getTransmissionRoutes(), contactTypes );
//			final Duration contactPeriod = Duration.of( "10 h" );
//			final double infectLikelihood = force.times( contactPeriod.toAmount() )
//					.to( Unit.ONE ).getEstimatedValue();
//			LOG.trace( "Infection likelihood: {} * {} * {} = {}", force,
//					contactPeriod, Arrays.asList( contactTypes ),
//					infectLikelihood );
			//
//			final DistributionParser distParser = new DistributionParser(
//					Math3ProbabilityDistribution.Factory
//							.of( Math3PseudoRandom.Factory
//									.of( MersenneTwister.class )
//									.create( "MAIN", 1234L ) ) );
//			final ProbabilityDistribution<Gender> genderDist = distParser
//					.getFactory()
//					.createUniformCategorical( Gender.MALE, Gender.FEMALE );
//			/*
//			 * FIXME RandomDistribution. Util .valueOf( "uniform(male;female)",
//			 * distParser, Gender.class );
//			 */
//			final ProbabilityDistribution<Instant> birthDist = Instant
//					.of( /* distFactory.getUniformInteger( rng, -5, 0 ) */
//							distParser.parse( "uniform-discrete(-5;0)",
//									Integer.class ),
//							NonSI.DAY );
//			final CountDownLatch latch = new CountDownLatch( 1 );
//			final Population pop = Population.Simple.of( scheduler );
//			for( int i = 1; i < n_pop; i++ )
//			{
//				final Gender gender = genderDist.draw();
//				final Instant birth = birthDist.draw();
//				LOG.trace( "#{} - gender: {}, birth: {}", i, gender,
//						birth.prettify( NonSI.DAY, 1 ) );
//				final Individual ind = Individual.Simple.of(
//						Household.Simple.of( pop, rivm ), birth, gender,
//						rivm.getTransmissionSpace(), false );
//				ind.with( Condition.Simple.of( ind, measles ) );
////				pop.add( ind );
//				final int nr = i;
//				ind.afflictions().get( measles ).emitTransitions()
//						.subscribe( ( t ) ->
//						{
//							LOG.trace( "Transition for #{} at t={}: {}", nr,
//									scheduler.now().prettify( Units.HOURS, 1 ), t );
//						}, ( e ) ->
//						{
//							LOG.warn( "Problem in transition", e );
//						}, () ->
//						{
//							latch.countDown();
//						} );
//				if( distParser.getFactory().getStream()
//						.nextDouble() < infectLikelihood )
//				{
//					LOG.trace( "INFECTED #{}", i );
//					ind.after( Duration.of( "30 min" ) )
//							.call( ind.afflictions().get( measles )::infect );
//				}
//			}
//			scheduler.time().subscribe( t ->
//			{
//				LOG.trace( "t = {}", t.prettify( NonSI.DAY, 1 ) );
//			}, e -> LOG.warn( "Problem in scheduler", e ), latch::countDown );
//			scheduler.resume();
//			latch.await( 3, TimeUnit.SECONDS );
//			assertEquals( "Should have completed", 0, latch.getCount() );
			//
		}
	}

	// CBS 70895ned: Overledenen; geslacht en leeftijd, per week
	// http://statline.cbs.nl/StatWeb/publication/?VW=T&DM=SLNL&PA=70895ned&LA=NL

	// CBS 83190ned: overledenen in huishouden per leeftijd
	// http://statline.cbs.nl/Statweb/publication/?DM=SLNL&PA=83190ned&D1=0&D2=0&D3=a&D4=0%2c2-3%2c5&D5=a&HDR=T%2cG2%2cG3&STB=G1%2cG4&VW=T

	interface MyHypersonicConfig extends JDBCConfig
	{
		@DefaultValue( "org.hsqldb.jdbc.JDBCDriver" )
		String driver();

//		@DefaultValue( "jdbc:mysql://localhost/testdb" )
//		@DefaultValue( "jdbc:hsqldb:mem:mymemdb" )
		@DefaultValue( "jdbc:hsqldb:file:target/testdb" )
		String url();

		@DefaultValue( "SA" )
		String username();

		@DefaultValue( "" )
		String password();

		static void exec( final String sql, final Consumer<ResultSet> consumer )
			throws ClassNotFoundException, SQLException
		{
			ConfigCache.getOrCreate( MyHypersonicConfig.class ).execute( sql,
					consumer );
		}
	}

	public static void main( final String[] args )
		throws IOException, ClassNotFoundException
	{
	}

	/**
	 * <ul>
	 * <li>Traffic intensity (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_catalog=CBS&_la=nl&tableId=81435ned&_theme=364">CBS
	 * 81435ned</a>)
	 * <li>Traffic participants (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81125ned&_theme=361">CBS
	 * 81125ned</a>)
	 * <li>Mobility - vehicle posession (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=37856&_theme=837">CBS
	 * 37856</a>)
	 * <li>Mobility - traveler characteristics (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81128ned&_theme=494">CBS
	 * 81128ned</a>)
	 * <li>Mobility - traffic characteristics (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81127ned&_theme=494">CBS
	 * 81127ned</a>)
	 * <li>Mobility - motives (<a href=
	 * "http://opendata.cbs.nl/dataportaal/portal.html?_la=nl&_catalog=CBS&tableId=81124ned&_theme=494">CBS
	 * 81124ned</a>)
	 * </ul>
	 */
	public void loadTraffic()
	{

	}

}