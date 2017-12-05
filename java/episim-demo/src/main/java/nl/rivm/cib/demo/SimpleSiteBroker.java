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

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.log.LogUtil;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.EpiFact;
import nl.rivm.cib.demo.DemoModel.Epidemical.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Households;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Sites;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;

/**
 * {@link SimpleSiteBroker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class SimpleSiteBroker implements SiteBroker
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimpleSiteBroker.class );

	public interface SiteConfig extends YamlConfig
	{
		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();
	}

	@InjectConfig
	private SiteConfig config;

	@Inject
	private Scheduler scheduler;

	@Inject
	private DataLayer data;

	@Inject
	private ProbabilityDistribution.Factory distFactory;

	private final PublishSubject<EpiFact> events = PublishSubject.create();

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public Observable<? extends EpiFact> events()
	{
		return this.events;
	}

//	private Table<Persons.PersonTuple> persons;
	private Table<HouseholdTuple> households;
	private Table<SiteTuple> sites;
//	private Table<Regions.RegionTuple> regions;

	//	private IndexPartition sirIndex;
	/** east-west, longitude, meridian */
//	private IndexPartition ewMeridian;
	/** north-south, latitude, parallel */
//	private IndexPartition nsParallel;

	private final Map<Comparable<?>, NavigableSet<Comparable<?>>> regionSites = new HashMap<>();
	private ConditionalDistribution<Comparable<?>, Comparable<?>> regionalHomeSiteDist;

	@Override
	public SimpleSiteBroker reset() throws Exception
	{
		this.sites = this.data.getTable( SiteTuple.class );
		this.households = this.data.getTable( HouseholdTuple.class ).onCreate(
				hh -> hh.set( Households.HomeSiteRef.class,
						this.regionalHomeSiteDist.draw(
								hh.get( Households.HomeRegionRef.class ) ) ),
				scheduler()::fail );
//		this.persons = 
		this.data.getTable( PersonTuple.class ).onCreate( this::onCreate,
				scheduler()::fail );

//		this.regions = this.data.getTable( RegionTuple.class );
		final ProbabilityDistribution<Double> latDist = this.distFactory
				.createUniformContinuous( 50.5, 54.5 );
		final ProbabilityDistribution<Double> lonDist = this.distFactory
				.createUniformContinuous( 3.5, 6.5 );
		this.regionalHomeSiteDist = regName ->
		{
			// TODO get lat,long,zip from CBS distribution
			final SiteTuple site = this.sites.insertValues(
					map -> map.put( Sites.RegionRef.class, regName )
							.put( Sites.Purpose.class, HOME_FUNCTION )
							.put( Sites.Latitude.class, latDist.draw() )
							.put( Sites.Longitude.class, lonDist.draw() )
//							.put( Sites.Capacity.class, 10 ) 
			);
			this.regionSites.computeIfAbsent( regName, k -> new TreeSet<>() )
					.add( (Comparable<?>) site.key() );
			return (Comparable<?>) site.key();
		};
		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	public void onCreate( final PersonTuple pp )
	{
		final HouseholdTuple hh = this.households
				.select( pp.get( Persons.HouseholdRef.class ) );
		final Comparable<?> homeRegRef = hh
				.get( Households.HomeRegionRef.class ),
				homeSiteRef = hh.get( Households.HomeSiteRef.class );
		pp.set( Persons.HomeRegionRef.class, homeRegRef );
		pp.set( Persons.HomeSiteRef.class, homeSiteRef );
//		pp.set( Persons.SiteRef.class, homeSiteRef );
//		LOG.debug( "Updating homeRef to {} in {} for {}", homeSiteRef,
//				homeRegRef, pp );
	}

	private static final String HOME_FUNCTION = "home";

	private final AtomicLong siteSeq = new AtomicLong();

	@Override
	public SiteTuple findNearby( final String lifeRole,
		final Object originSiteRef )
	{
		final SiteTuple origin = this.sites
				.select( Objects.requireNonNull( originSiteRef, "homeless?" ) );
		// TODO use actual locations/regions
//		LOG.warn( "TODO extend existing {} site near {}", lifeRole, origin );
		final Double lat = origin.get( Sites.Latitude.class ),
				lon = origin.get( Sites.Longitude.class );
		final Object regRef = origin.get( Sites.RegionRef.class );
		return this.sites
				.insertValues( map -> map.put( Sites.RegionRef.class, regRef )
						.put( Sites.SiteName.class,
								regRef + "/" + this.siteSeq.incrementAndGet() )
						.put( Sites.Purpose.class, lifeRole )
						.put( Sites.Latitude.class, lat )
						.put( Sites.Longitude.class, lon )
						.put( Sites.Capacity.class, 100 ) ); // TODO
	}

//	final String denom = v[Col.DENOMINATIE_VESTIGING.ordinal()];
//	final String cultCat;
//	if( denom.startsWith( "Evan" ) // 0.1%
//			// || denom.contains( "PC" ) // 1.1%
//			|| denom.startsWith( "Prot" ) // 23.1%
//			|| denom.startsWith( "Geref" ) ) // 1.2%
//		cultCat = "po_luther";
//	else if( denom.startsWith( "Antro" ) ) // 0.9%
//		cultCat = "po_steiner";
//	else if( v[Col.SOORT_PO.ordinal()].equals( "Vso" )
//			|| v[Col.SOORT_PO.ordinal()].equals( "So" ) )
//		cultCat = "po_special";
//	else
//		cultCat = "po_rest";

}