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
package nl.rivm.cib.epidemes.pilot;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Area;
import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.ogm.cfg.OgmProperties;
import org.hibernate.ogm.datastore.neo4j.Neo4j;
import org.hibernate.ogm.datastore.neo4j.Neo4jProperties;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.Transaction;
import io.coala.math.LatLong;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.name.Identified;
import io.coala.persist.HibernateJPAConfig;
import io.coala.persist.JPAUtil;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.Signal;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.disease.Afflicted;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.Disease;
import nl.rivm.cib.episim.model.disease.infection.Infection;
import nl.rivm.cib.episim.model.locate.Geography;
import nl.rivm.cib.episim.model.locate.Locatable;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.person.Household;
import nl.rivm.cib.episim.model.person.HouseholdParticipant;
import nl.rivm.cib.episim.model.person.Population;
import nl.rivm.cib.episim.persist.fact.TransmissionFactDao;
import tec.uom.se.unit.Units;

/**
 * {@link PilotTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PilotTest
{

	/** */
	private static final Logger LOG = LogManager.getLogger( PilotTest.class );

	/**
	 * {@link MyORMConfig}
	 */
	public interface MyORMConfig extends HibernateJPAConfig
	{
		@DefaultValue( "rdbms_test_pu" ) // match persistence.xml
		@Key( JPA_UNIT_NAMES_KEY )
		String[] jpaUnitNames();

//		@DefaultValue( "jdbc:mysql://localhost/testdb" )
//		@DefaultValue( "jdbc:neo4j:bolt://192.168.99.100:7687/db/data" )
//		@DefaultValue( "jdbc:hsqldb:mem:mymemdb" )
		@DefaultValue( "jdbc:hsqldb:file:target/testdb" )
		@Key( HIBERNATE_CONNECTION_URL_KEY )
		URI jdbcUrl();
	}

	/**
	 * {@link MyOGMConfig} for an object-graph model (OGM/NoSQL) implementation
	 * such as MongoDB or Neo4J of our object-relation model (ORM/JPA) entities,
	 * requires vendor-specific hibernate dependency
	 */
	public interface MyOGMConfig extends HibernateJPAConfig
	{
		@DefaultValue( "nosql_test_pu" ) // match persistence.xml
		@Key( JPA_UNIT_NAMES_KEY )
		String[] jpaUnitNames();

		@DefaultValue( "pilot_testdb" )
		@Key( OgmProperties.DATABASE )
		String database();

		// :7474 HTTP REST port, 7687 bolt SSL port
		@DefaultValue( "192.168.99.100:7687" )
		@Key( OgmProperties.HOST )
		String host();

		@DefaultValue( "neo4j" )
		@Key( OgmProperties.USERNAME )
		String jdbcUsername();

		@DefaultValue( "epidemes" /* PASSWORD_PROMPT_VALUE */ )
		@Key( OgmProperties.PASSWORD )
		@ConverterClass( PasswordPromptConverter.class )
		String jdbcPassword();

		@Override
		default String jdbcPasswordKey()
		{
			return OgmProperties.PASSWORD;
		}

		@DefaultValue( Neo4j.EMBEDDED_DATASTORE_PROVIDER_NAME )
		@Key( OgmProperties.DATASTORE_PROVIDER )
		String ogmProvider();

		@DefaultValue( "target/" )
		@Key( Neo4jProperties.DATABASE_PATH )
		String hibernateOgmNeo4jDatabasePath();

	}

	/**
	 * {@link Measles}
	 * 
	 * <p>
	 * Disease (Vink et al. 2014) Serial Interval (latent + infectious), days
	 * Anderson and May 1991 (6) Vynnycky and White 2010 (8) <br>
	 * Influenza A 3–6 vs 2–4 <br>
	 * Measles 12–16 vs 7–16 <br>
	 * HPV 100-150?
	 */
	public static class Measles extends Infection.SimpleSEIR // identifier only, mechanisms = pathogen
	{

		private static final ID ID = Disease.ID.of( "measles" );

		/**
		 * {@link Measles} force of infection should result in a mean generation
		 * time (time between primary infecting secondary) of ca 11-12 days,
		 * with st.dev. 2-3 days, see Klinkenberg:2011:jtbi, secondary attack
		 * rate >90% in household/institutional contacts
		 * 
		 * for incubation and symptom times, see
		 * https://wwwnc.cdc.gov/travel/yellowbook/2016/infectious-diseases-related-to-travel/measles-rubeola
		 */
		public Measles( final Scheduler scheduler,
			final ProbabilityDistribution.Factory distFact,
			final Condition.Factory conditionFactory )
		{
			super( ID, scheduler, conditionFactory,

					// latent period (E->I) t+0, onset around t+14 but infectious already 4 days before
					distFact.createTriangular( 1, 14 - 4, 21 - 4 )//
							.toQuantities( TimeUnits.DAYS ).map( Duration::of ),

					// infectious period (I->R): t+14-4, [onset-4,onset+4] 
					distFact.createTriangular( 0, 8, 10 )
							.toQuantities( TimeUnits.DAYS ).map( Duration::of ),

					// wane period (R->S): infinite, forever immune
					distFact.createDeterministic(
							Duration.of( 100, TimeUnits.YEAR ) ),

					// incubation period (E->C, sub->clinical symptoms)
					// : t+7-21 days, rash, fever, Koplik spots, ...
					// : t+9-12 days (https://www.wikiwand.com/en/Incubation_period)
					distFact.createTriangular( 7, 11, 21 )
							.toQuantities( TimeUnits.DAYS ).map( Duration::of ),

					// symptom period (C->N, normal/asymptomatic)
					distFact.createTriangular( 4, 5, 7 )
							.toQuantities( TimeUnits.DAYS )
							.map( Duration::of ) );
		}
	}

	interface Step
	{

	}

	interface Routine // inspired by Zhang et al, 2016
	{
		void applyTo( Locatable person );

		// e.g. apply Timing.of("weekdays" ) for travel events;
	}

	@Singleton
	public static class MyMap implements Region.Factory, Place.Factory
	{
		/** the regions: geographic areas */
		private NavigableMap<Region.ID, Region> regions = new TreeMap<>();

		/** the places: geographic locations */
		private NavigableMap<Place.ID, Place> places = new TreeMap<>();

		/** the spaces: rooms, vehicles */
		private NavigableMap<String, Routine> routines = new TreeMap<>();

//		force of infection (S->E): t=?, viral shedding -> respiratory/surface contact 
//		if( // TODO if  (infection pressure [=#contacts] > threshold) &&
//		conditionOf( person ).compartment().isSusceptible() )

		/** RIVM National Institute for Public Health and the Environment */
		public LatLong RIVM_POSITION = LatLong.of( 52.1185272, 5.1868699,
				Units.DEGREE_ANGLE );

		@Override
		public Region get( final Region.ID id )
		{
			return this.regions.computeIfAbsent( id, key ->
			{
				final String name = null;// TODO lookup
				final String type = null;// TODO lookup
				final SortedMap<Instant, Region> parent = null;// TODO lookup
				final Quantity<Area> surfaceArea = null;// TODO lookup
				final Population<?> population = null;// TODO lookup

				return Region.of( id, name, type, parent, surfaceArea,
						population );
			} );
		}

		@Override
		public Place get( final Place.ID id )
		{
			return this.places.computeIfAbsent( id, key ->
			{
				final LatLong position = null;// TODO lookup
				final Region region = null;// TODO lookup
				final Geography[] geographies = null;// TODO lookup

				return Place.of( id, position, region, geographies );
			} );
		}

	}

	public static class MyPerson extends Identified.SimpleOrdinal<Afflicted.ID>
		implements HouseholdParticipant, Afflicted, Locatable
	{
		private static final AtomicLong count = new AtomicLong( 0L );
		private Scheduler scheduler;
		private Household<? extends HouseholdParticipant> household;
		private Map<Disease.ID, Condition> afflictions = new TreeMap<>();

		public MyPerson( final Scheduler scheduler,
			final Household<? extends HouseholdParticipant> household )
		{
			this.id = Afflicted.ID.of( count.incrementAndGet() );
			this.scheduler = scheduler;
			this.household = household;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Map<Disease.ID, Condition> afflictions()
		{
			return this.afflictions;
		}

		@Override
		public Household<? extends HouseholdParticipant> household()
		{
			return this.household;
		}

		@Override
		public Signal<Place> place()
		{
			return null;
		}

	}

	@Singleton
	public static class MeaslesScenario implements Scenario
	{
		@Inject
		private Scheduler scheduler;

		@Inject
		private LocalBinder binder;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		/**
		 * social network (household/opinions):
		 * <li>topology: households (marry/split, birth/death, migrate)
		 * <ul>
		 * <li>37230: #persons-births-deaths-migrations, per municipality /
		 * month '02-'17
		 * <li>71091: #splits: Scheiden; naar belangrijkste redenen, 2003
		 * <li>71488: #single-couple-married-kids-institute, per age 0:5:95+ /
		 * municipality / year '97-'16
		 * <li>37973: #single-couple-married-kids-institute, per
		 * ethnicity-generation / year '00-'16
		 * <li>37713: #persons, per ethnicity / age 0:5:95+ / municipality /
		 * year '96-'16
		 * </ul>
		 * <li>pro/con, weight; parent, partner, sibling, work/classmate
		 * <li>contact network (locations): home, work, school, vehicle, shop,
		 * sports (mobility routines)
		 * <ul>
		 * <li>82249: Social contacts 81124: Mobility - motives
		 * <li>81125: Traffic participants
		 * <li>37856: Mobility vehicle possession
		 * </ul>
		 * <li>disease/vaccine
		 * <ul>
		 * <li>measles: SEIRS (Vink et al)
		 * <li>cost-effectiveness: per case
		 */
		@Override
		public void init() throws Exception
		{
			final OffsetDateTime offset = OffsetDateTime.now()
					.truncatedTo( ChronoUnit.DAYS ); // start of current year

			final Population<?> pop = this.binder.inject( Population.class );

			// initiate Mixing: spaces/transports, mobility

			// initiate Opinion: hesitancy, interaction

			// initiate Infections: diseases/outbreaks, vaccine/interventions

			// virtual time added...
			after( Duration.of( 10, TimeUnits.HOURS ) ).call( t -> LOG
					.trace( "Virtual time is now: {}", t.prettify( offset ) ) );

			LOG.info( "Initialized model" );
		}

	}

	@Test
	public void measlesTest()
	{
		LOG.info( "Starting measles test" );

		// connect and setup database persistence
		final EntityManagerFactory EMF = ConfigCache
				.getOrCreate( MyORMConfig.class ).createEMF();

		// load scenario
		final LocalBinder binder = LocalConfig.builder().withId( "measles1" )

				// time API (virtual time management)
				.withProvider( Scheduler.class, Dsol3Scheduler.class )

				// math API (pseudo random)
				.withProvider( PseudoRandom.Factory.class,
						Math3PseudoRandom.MersenneTwisterFactory.class )
				.withProvider( ProbabilityDistribution.Factory.class,
						Math3ProbabilityDistribution.Factory.class )
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )

				// enterprise API (facts, actors)
				.withProvider( Actor.Factory.class,
						Actor.Factory.LocalCaching.class )
				.withProvider( Transaction.Factory.class,
						Transaction.Factory.LocalCaching.class )
				.withProvider( Fact.Factory.class,
						Fact.Factory.SimpleProxies.class )
				.withProvider( FactBank.class, FactBank.SimpleJPA.class )
				.withProvider( FactExchange.class,
						FactExchange.SimpleBus.class )

				// epidemes API
				.withProvider( Region.Factory.class, MyMap.class )
				.withProvider( Place.Factory.class, MyMap.class )

				.build().createBinder( Collections
						.singletonMap( EntityManagerFactory.class, EMF ) );

		// run scenario & generate output
		binder.inject( MeaslesScenario.class ).run();

		// confirm output
		JPAUtil.session( EMF, em ->
		{
			LOG.info( "Got DB entries: {}", TransmissionFactDao.class );
		} );

		EMF.close();
		LOG.info( "Completed measles test" );
	}

}
