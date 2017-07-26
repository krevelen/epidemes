package nl.rivm.cib.episim.model;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.hibernate.cfg.AvailableSettings;
import org.junit.BeforeClass;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.Transaction;
import io.coala.exception.ExceptionStream;
import io.coala.log.LogUtil;
import io.coala.persist.HibernateJPAConfig;
import io.coala.time.SchedulerConfig;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.Afflict;

/**
 * {@link EpiModelTest}
 * 
 * TODO specialized logging, adding e.g. Timed#now() and Identified#id()
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class EpiModelTest
{
	private static final Logger LOG = LogUtil.getLogger( EpiModelTest.class );

	public interface MyJPAConfig extends HibernateJPAConfig
	{
		@DefaultValue( "model_test_pu" ) // match persistence.xml
		@Key( JPA_UNIT_NAMES_KEY )
		String[] jpaUnitNames();

//		@DefaultValue( "jdbc:mysql://localhost/testdb" )
//		@DefaultValue( "jdbc:hsqldb:mem:mymemdb" )
//		@DefaultValue( "jdbc:neo4j:bolt://192.168.99.100:7687/db/data" )
		@DefaultValue( "jdbc:hsqldb:file:target/testdb" )
		@Key( AvailableSettings.URL )
		URI jdbcUrl();
	}

	@SuppressWarnings( "serial" )
	@Singleton
	public static class World implements Scenario
	{

		@Inject
		private Scheduler scheduler;

		@Inject
		private Actor.Factory actors;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		private AtomicLong personCounter = new AtomicLong( 0L );

		@Override
		public void init() throws Exception
		{
			LOG.trace( "initializing..." );

			final Actor<Fact> person1 = this.actors
					.create( "pers" + this.personCounter.incrementAndGet() );

			final Condition cond = person1.subRole( Condition.class );
			final Fact rq = cond.initiate( cond.id() ).commit();
			LOG.trace( "initialized person specialist: {}, sent rq: {}",
					cond.id(), rq );

//			final DateTime offset = new DateTime(
//					this.actors.offset().toInstant().toEpochMilli() );
//			LOG.trace( "initialized occurred and expired fact sniffing" );
//
//			org1.emit().subscribe( fact ->
//			{
//				LOG.trace( "t={}, occurred: {}", now().prettify( offset ),
//						fact );
//			}, e -> LOG.error( "Problem", e ) );
//
//			final AtomicInteger counter = new AtomicInteger( 0 );
//			final Procurement proc = org1.specialist( Procurement.class );
//			final Sales sales = org1.specialist( Sales.class );
//			sales.setTotalValue( 0 );
//			sales.emit( FactKind.REQUESTED ).subscribe(
//					rq -> after( Duration.of( 1, TimeUnits.DAYS ) ).call( t ->
//					{
//						final Sale st = sales.respond( rq, FactKind.STATED )
//								.with( "stParam",
//										"stValue" + counter.getAndIncrement() );
//						sales.addToTotal( 1 );
//						LOG.trace( "{} responds: {} <- {}, total now: {}",
//								sales.id(), st.causeRef().prettyHash(),
//								st.getStParam(), sales.getTotalValue() );
//						st.commit( true );
//					} ), e -> LOG.error( "Problem", e ),
//					() -> LOG.trace( "sales/rq completed?" ) );
//			LOG.trace( "initialized business rule(s)" );
//
//			atEach( Timing.valueOf( "0 0 0 30 * ? *" ).offset( offset )
//					.iterate(), t ->
//					{
//						// spawn initial transactions from/with self
//						final Sale rq = proc.initiate( sales.id(), t.add( 1 ) )
//								.withRqParam( t );
//
//						// de/serialization test
//						final String json = rq.toJSON();
//						final String fact = Sale.fromJSON( json ).toString();
//						LOG.trace( "{} initiates: {} => {}", proc.id(), json,
//								fact );
//						rq.commit();
//					} );
//			LOG.trace( "intialized TestFact initiation" );

			LOG.trace( "initialization complete!" );
		}
	}

	@BeforeClass
	public static void listenExceptions()
	{
		ExceptionStream.asObservable().subscribe(
				t -> LOG.error( "Intercept " + t.getClass().getSimpleName() ),
				e -> LOG.error( "ExceptionStream failed", e ),
				() -> LOG.trace( "JUnit test completed" ) );
	}

	@SuppressWarnings( { "unchecked", "unchecked" } )
	@Test
	public void testModel()
		throws TimeoutException, IOException, InterruptedException
	{
		// configure replication FIXME via LocalConfig?
		ConfigCache.getOrCreate( SchedulerConfig.class, Collections
				.singletonMap( SchedulerConfig.DURATION_KEY, "" + 200 ) );

		// configure tooling
		final LocalBinder binder = LocalConfig.builder().withId( "world1" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class )
				.withProvider( Actor.Factory.class,
						Actor.Factory.LocalCaching.class )
				.withProvider( Transaction.Factory.class,
						Transaction.Factory.LocalCaching.class )
				.withProvider( Fact.Factory.class,
						Fact.Factory.SimpleProxies.class )
				.withProvider( FactBank.class, FactBank.SimpleJPA.class )
				.withProvider( FactExchange.class,
						FactExchange.SimpleBus.class )
				.build()
				.createBinder( Collections
						.singletonMap( EntityManagerFactory.class, ConfigFactory
								.create( MyJPAConfig.class ).createEMF() ) );

		LOG.info( "Starting {}, config: {}", getClass().getSimpleName(),
				binder );

		binder.inject( World.class ).run();

		final FactBank<Afflict> bank = binder.inject( FactBank.class )
				.matchTransactionKind( Afflict.class );
		bank.findAsStream( false )
				.forEach( f -> LOG.trace( "Fetched fact: {}", f ) );

		LOG.info( "completed, t={}", binder.inject( Scheduler.class ).now() );
	}

}
