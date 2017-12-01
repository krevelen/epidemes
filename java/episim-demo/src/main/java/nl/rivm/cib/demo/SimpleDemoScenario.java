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
package nl.rivm.cib.demo;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.Logger;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.enums.ValueType;

import io.coala.bind.InjectConfig;
import io.coala.data.DataLayer;
import io.coala.enterprise.persist.FactDao;
import io.coala.log.LogUtil;
import io.coala.math.MatrixUtil;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import nl.rivm.cib.demo.DemoModel.Cultural.PeerBroker;
import nl.rivm.cib.demo.DemoModel.Cultural.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Demical.Deme;
import nl.rivm.cib.demo.DemoModel.Demical.DemicFact;
import nl.rivm.cib.demo.DemoModel.Epidemical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Regions.RegionTuple;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.demo.DemoModel.Societies.SocietyTuple;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;

/**
 * {@link SimpleDemoScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class SimpleDemoScenario implements DemoModel, Scenario
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimpleDemoScenario.class );

	@InjectConfig
	private DemoConfig config;

	@Inject
	private DataLayer data;

	@Inject
	private Scheduler scheduler;

	@Inject
	private ProbabilityDistribution.Factory distFactory;

	@Inject
	private Deme deme;

	@Inject
	private SiteBroker siteBroker;

	@Inject
	private SocietyBroker societyBroker;

	@Inject
	private PeerBroker peerBroker;

	@Inject
	private HealthBroker healthBroker;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	// the data sources
	private Matrix persons, households;

	// model event statistics
	private final Map<Class<?>, AtomicLong> demicEventStats = new HashMap<>();

	// model event statistics
	private final Map<MSEIRS.Compartment, AtomicLong> sirEventStats = new HashMap<>();

	@Override
	public void init() throws Exception
	{
		LOG.info( "Initializing {}, config: {}", getClass().getSimpleName(),
				this.config );
		LOG.info( "RNG seed: {}, scheduler offset: {}",
				this.distFactory.getStream().seed(), scheduler().offset() );

		this.persons = Matrix.Factory.sparse( ValueType.OBJECT, 1_100_000,
				Persons.PROPERTIES.size() );
		this.households = Matrix.Factory.sparse( ValueType.OBJECT, 1_000_000,
				Households.PROPERTIES.size() );

		// register data sources
		this.data
				.withSource(
						map -> map.put( PersonTuple.class, Persons.PROPERTIES ),
						this.persons )
				.withSource( map -> map.put( HouseholdTuple.class,
						Households.PROPERTIES ), this.households )
				.withSource(
						map -> map.put( RegionTuple.class, Regions.PROPERTIES ),
						HashMap::new )
				.withSource( map -> map.put( SocietyTuple.class,
						Societies.PROPERTIES ), HashMap::new )
				.withSource(
						map -> map.put( SiteTuple.class, Sites.PROPERTIES ),
						HashMap::new );
//		this.data.getTable( PersonTuple.class ).changes()
//				.filter( chg -> chg.crud() == Table.Operation.UPDATE )
//				.subscribe( chg -> LOG.debug( "t={} chg: {}", now(), chg ), e ->
//				{
//				} );
//		this.data.getTable( HouseholdTuple.class ).changes()
//				.filter( chg -> chg.crud() == Table.Operation.UPDATE )
//				.subscribe( chg -> LOG.debug( "t={} chg: {}", now(), chg ), e ->
//				{
//				} );

		// populate and run deme
		final AtomicLong tLog = new AtomicLong();

		this.siteBroker.reset();
		this.societyBroker.reset();
		this.peerBroker.reset();
		this.healthBroker.reset();
		this.healthBroker.events().ofType( EpidemicFact.class )
				.subscribe(
						ev -> ev.sirDelta
								.forEach( ( sir, delta ) -> this.sirEventStats
										.computeIfAbsent( sir,
												k -> new AtomicLong() )
										.addAndGet( delta ) ),
						scheduler()::fail, this::logStats );

		this.deme.reset().events().observeOn( Schedulers.io() )
				.ofType( Demical.DemicFact.class ).subscribe( ev ->
				{
					tLog.updateAndGet( tPrev ->
					{
						final long tWall = System.currentTimeMillis();
						if( tWall - tPrev < 5000 ) return tPrev;
						logStats();
						return tWall;
					} );
					this.demicEventStats.computeIfAbsent( ev.getClass(),
							k -> new AtomicLong() ).incrementAndGet();
				}, scheduler()::fail, this::logStats );

	}

	private void logStats()
	{
		LOG.info( "t={} demic event x {}: {}",
				scheduler().now( DateTimeFormatter.ISO_WEEK_DATE ),
				this.demicEventStats.values().stream()
						.mapToLong( AtomicLong::get ).sum(),
				String.join( ", ", this.demicEventStats.entrySet().stream().map(
						e -> e.getKey().getSimpleName() + "=" + e.getValue() )
						.toArray( String[]::new ) ) );
	}

	public Observable<Map<String, Map<MSEIRS.Compartment, Long>>>
		emitEpidemicStats( final String timing )
	{
		return Observable.create( sub ->
		{
			if( scheduler().now() == null )
				scheduler().onReset( s -> s
						.atOnce( t -> scheduleLocalSIRStats( timing, sub ) ) );
			else
				scheduleLocalSIRStats( timing, sub );
		} );
	}

	private void scheduleLocalSIRStats( final String timing,
		final ObservableEmitter<Map<String, Map<MSEIRS.Compartment, Long>>> sub )
	{
		// schedule at end of current instant
		atOnce( t -> sub.onNext( getLocalSIRStats() ) );
		scheduler().atEnd( t -> sub.onComplete() );
		try
		{
			final Iterable<Instant> it = Timing.of( timing )
					.iterate( scheduler() );
			atEach( it, t -> sub.onNext( getLocalSIRStats() ) );
		} catch( final ParseException e )
		{
			sub.onError( e );
			return;
		}
//		final Matrix homecol = this.persons.selectColumns( Ret.LINK,
//				Persons.PROPERTIES.indexOf( Persons.HomeRegionRef.class ) );
//		sub.onNext( MatrixUtil.streamAvailableCoordinates( homecol, false ) // sequential
//				.map( homecol::getAsString ).filter( reg -> reg != null )
//				.distinct().collect( Collectors.toMap( reg -> reg,
//						reg -> new HashMap<>() ) ) );
	}

	private Map<String, Map<MSEIRS.Compartment, Long>> getLocalSIRStats()
	{
//		this.dataEventStats.entrySet().stream().map(
//		e -> e.getKey().getSimpleName() + " x " + e.getValue() )
//		.reduce( ( s1, s2 ) -> String.join( ", ", s1, s2 ) )
//		.orElse( "[]" ),
//this.sirEventStats.values().stream()
//		.mapToLong( AtomicLong::get ).sum(),
//String.join( ", ", this.sirEventStats.entrySet().stream()
//		.map( e -> e.getKey().name().substring( 0, 1 ) + "="
//				+ e.getValue() )
//		.toArray( String[]::new ) ),
		final Matrix epicol = this.persons.selectColumns( Ret.LINK,
				Persons.PROPERTIES.indexOf( Persons.PathogenCompartment.class ) );
		final Matrix homecol = this.persons.selectColumns( Ret.LINK,
				Persons.PROPERTIES.indexOf( Persons.HomeRegionRef.class ) );

		final Map<String, Map<MSEIRS.Compartment, Long>> result = MatrixUtil
				.streamAvailableCoordinates( epicol, false ) // sequential
				.filter( x -> homecol.getAsObject( x ) != null )
				.collect( Collectors.groupingBy( homecol::getAsString,
						Collectors.groupingBy( x -> MSEIRS.Compartment
								.values()[epicol.getAsInt( x ) - 1],
								Collectors.counting() ) ) );
		return result;
	}

	public Observable<? extends DemicFact> emitDemicEvents()
	{
		return this.deme.events();
	}

	public Observable<FactDao> emitFactDaos( final EntityManager em )
	{
		return ((SimplePersonBroker) this.deme).emitFacts()
				.map( fact -> FactDao.create( em, fact ) );
	}
}