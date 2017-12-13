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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;
import org.ujmp.core.enums.ValueType;

import io.coala.bind.InjectConfig;
import io.coala.data.DataLayer;
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
import nl.rivm.cib.demo.DemoModel.Demical.DemicFact;
import nl.rivm.cib.demo.DemoModel.Demical.PersonBroker;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Medical.EpidemicFact;
import nl.rivm.cib.demo.DemoModel.Medical.HealthBroker;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Regional.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Regions.RegionTuple;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.demo.DemoModel.Social.PeerBroker;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Societies.SocietyTuple;
//import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Compartment;

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
	private PersonBroker personBroker;

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

	/** the data source */
	private Matrix persons, households;

	/** demographic event aggregates */
	private final Map<String, AtomicLong> demicEventStats = new TreeMap<>();

	/** epidemic event aggregates */
	private final Map<String, EnumMap<Compartment, AtomicLong>> sirEventStats = new TreeMap<>();

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

		// register data sources BEFORE initializing the brokers
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

		// reset brokers only AFTER data sources have been initialized
		this.siteBroker.reset();
		this.societyBroker.reset();
		this.peerBroker.reset();
		this.healthBroker.reset();

		// aggregate statistics
		this.healthBroker.events().observeOn( Schedulers.io() )
				.ofType( EpidemicFact.class ).subscribe( this::onEpidemicFact,
						scheduler()::fail, this::logStats );

		this.personBroker.reset().events().observeOn( Schedulers.io() )
				.ofType( Demical.DemicFact.class ).subscribe( this::onDemicFact,
						scheduler()::fail, this::logStats );
	}

	private void onEpidemicFact( final EpidemicFact ev )
	{
		ev.sirDelta.forEach( ( sir, delta ) ->
		{
			if( delta < 0 ) return; // only positive
			this.sirEventStats
					.computeIfAbsent(
							ev.pp.get( Persons.HomeRegionRef.class ).toString(),
							k -> new EnumMap<>( Compartment.class ) )
					.computeIfAbsent( sir, k -> new AtomicLong() )
					.addAndGet( delta );
		} );
	}

	private final AtomicLong logWalltime = new AtomicLong();

	private void onDemicFact( final DemicFact ev )
	{
		this.demicEventStats.computeIfAbsent( ev.getClass().getSimpleName(),
				k -> new AtomicLong() ).incrementAndGet();
		this.logWalltime.updateAndGet( tPrev ->
		{
			final long tWall = System.currentTimeMillis();
			if( tWall - tPrev < 5000 ) return tPrev;
			logStats();
			return tWall;
		} );
	}

	private void logStats()
	{
		LOG.info( "t={} sir transitions overall x {}; recent demic (x {}): {}",
				scheduler().now( DateTimeFormatter.ISO_WEEK_DATE ),
				this.sirEventStats.values().stream()
						.flatMap( e -> e.values().stream() )
						.mapToLong( AtomicLong::get ).sum(),
				this.demicEventStats.values().stream()
						.mapToLong( AtomicLong::get ).sum(),
				this.demicEventStats );
		this.demicEventStats.clear();
	}

	@Override
	public Observable<DemoModel> atEach( final String timing )
	{
		return Observable.create( sub ->
		{
			if( scheduler().now() == null )
				scheduler().onReset(
						s -> s.atOnce( t -> scheduleAtEach( timing, sub ) ) );
			else
				scheduleAtEach( timing, sub );
		} );
	}

	private void scheduleAtEach( final String timing,
		final ObservableEmitter<DemoModel> sub ) throws ParseException
	{
		atOnce( t -> sub.onNext( this ) );
		scheduler().atEnd( t -> sub.onComplete() );
		try
		{
			final Iterable<Instant> it = Timing.of( timing )
					.iterate( scheduler() );
			atEach( it, t -> sub.onNext( this ) );
		} catch( final ParseException e )
		{
			sub.onError( e );
			return;
		}
	}

	@Override
	public Map<String, EnumMap<Compartment, Long>> exportRegionalSIRDelta()
	{
		return this.sirEventStats.entrySet().stream().collect( Collectors.toMap(
				Map.Entry::getKey,
				e -> e.getValue().entrySet().stream().collect( Collectors.toMap(
						Map.Entry::getKey, f -> f.getValue().get(),
						( l, r ) -> l + r,
						() -> new EnumMap<>( Compartment.class ) ) ) ) );
	}

	@Override
	public Map<String, EnumMap<Compartment, Long>> exportRegionalSIRTotal()
	{
		final Matrix epicol = this.persons.selectColumns( Ret.LINK,
				Persons.PROPERTIES
						.indexOf( Persons.PathogenCompartment.class ) );
		final Matrix homecol = this.persons.selectColumns( Ret.LINK,
				Persons.PROPERTIES.indexOf( Persons.HomeRegionRef.class ) );

		return MatrixUtil.streamAvailableCoordinates( epicol, true )
				// sequential? called from another thread
				.filter( x -> homecol.getAsObject( x ) != null )
				.collect( Collectors.groupingBy( homecol::getAsString,
						Collectors.groupingBy(
								x -> Compartment.values()[epicol.getAsInt( x )
										- 1],
								() -> new EnumMap<>( Compartment.class ),
								Collectors.counting() ) ) );
	}

//	public Observable<? extends DemicFact> emitDemicEvents()
//	{
//		return this.deme.events();
//	}
//
//	public Observable<FactDao> emitFactDaos( final EntityManager em )
//	{
//		return ((SimplePersonBroker) this.deme).emitFacts()
//				.map( fact -> FactDao.create( em, fact ) );
//	}
}