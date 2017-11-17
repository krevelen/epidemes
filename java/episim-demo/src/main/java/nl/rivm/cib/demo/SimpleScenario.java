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

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.ujmp.core.Matrix;
import org.ujmp.core.enums.ValueType;

import io.coala.bind.InjectConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.enterprise.persist.FactDao;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import nl.rivm.cib.demo.DemoModel.Demical.Deme;
import nl.rivm.cib.demo.DemoModel.Demical.DemicFact;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Medical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Regions.RegionTuple;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Societies.SocietyTuple;

/**
 * {@link SimpleScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class SimpleScenario implements DemoModel, Scenario
{
	@InjectConfig
	private DemoConfig config;

//	@Inject
//	private LocalBinder binder;

	@Inject
	private DataLayer data;

	@Inject
	private Scheduler scheduler;

	// the data sources
	private Matrix persons, households;

	// model event statistics
	private final Map<Class<?>, AtomicLong> demeEventStats = new HashMap<>();

	// data event statistics
	private final Map<Class<?>, AtomicLong> dataEventStats = new HashMap<>();

	@Inject
	private Deme deme;

	@Inject
	private SiteBroker siteBroker;

	@Inject
	private SocietyBroker societyBroker;

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public void init() throws Exception
	{
		DemoTest.LOG.trace( "cfg: {}", this.config );

		this.persons = Matrix.Factory.sparse( ValueType.OBJECT, 20_000_000,
				Persons.PROPERTIES.size() );
		this.households = Matrix.Factory.sparse( ValueType.OBJECT, 10_000_000,
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

		// maintain data event statistics
		this.data.changes()
				.filter( chg -> chg.crud() != Table.Operation.UPDATE )
				.subscribe(
						chg -> this.dataEventStats
								.computeIfAbsent( chg.changedType(),
										k -> new AtomicLong() )
								.addAndGet( chg.crud() == Table.Operation.CREATE
										? 1 : -1 ),
						scheduler()::fail );

		// populate and run deme
		final AtomicLong tLog = new AtomicLong();

//		this.deme = this.binder.inject( SimpleDeme.class,
//				this.config.toJSON( DemoConfig.SCENARIO_BASE,
//						DemoConfig.POPULATION_BASE ) );
		this.siteBroker.reset();
		this.societyBroker.reset();
		this.deme.reset().events()//.observeOn( Schedulers.io() )
				.ofType( Demical.DemicFact.class ).subscribe( ev ->
				{
					tLog.updateAndGet( tPrev ->
					{
						final long tWall = System.currentTimeMillis();
						if( tWall - tPrev < 1000 ) return tPrev;
						logStats();
						return tWall;
					} );
					this.demeEventStats.computeIfAbsent( ev.getClass(),
							k -> new AtomicLong() ).incrementAndGet();
				}, scheduler()::fail, this::logStats );
	}

	private void logStats()
	{
		DemoTest.LOG.trace( "t={} data: {}, deme event x {}: {}",
				scheduler().now( DateTimeFormatter.ISO_WEEK_DATE ),
				this.dataEventStats.entrySet().stream().map(
						e -> e.getKey().getSimpleName() + " x " + e.getValue() )
						.reduce( ( s1, s2 ) -> String.join( ", ", s1, s2 ) )
						.orElse( "[]" ),
				this.demeEventStats.values().stream()
						.mapToLong( AtomicLong::get ).sum(),
				String.join( ", ", this.demeEventStats.entrySet().stream().map(
						e -> e.getKey().getSimpleName() + "=" + e.getValue() )
						.toArray( String[]::new ) ) );
	}

	public Observable<? extends DemicFact> emitDemicEvents()
	{
		return this.deme.events();
	}

	public Observable<FactDao> emitFactDaos( final EntityManager em )
	{
		return ((SimpleDeme) this.deme).emitFacts()
				.map( fact -> FactDao.create( em, fact ) );
	}
}