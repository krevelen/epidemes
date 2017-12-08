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

import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.log.LogUtil;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Scheduler;
import io.coala.util.InputStreamConverter;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.EpiFact;
import nl.rivm.cib.demo.DemoModel.Households;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Regional.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Sites;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.demo.DemoModel.Social.EduCulture;
import nl.rivm.cib.json.CbsRegionCentroidDensity;
import nl.rivm.cib.json.DuoPrimarySchool;

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

		@Key( "hh-zip-density" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
				+ "gm_pc6_centroid_density.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbsZipcodeResidenceData();

		@Key( "primary-school-densities" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM + "gm_pc4_po_pupils.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream duoPrimarySchoolData();
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

	private final Map<String, double[]> siteCoords = new HashMap<>();
//	private final Map<Comparable<?>, NavigableSet<Comparable<?>>> regionSites = new HashMap<>();
	private ConditionalDistribution<Comparable<?>, String> regionalHomeSiteDist;
	/** */
	private final Map<String, EnumMap<DuoPrimarySchool.ExportCol, JsonNode>> schoolCache = new HashMap<>();
	/** */
	private ConditionalDistribution<String, Object[]> primarySchoolDist;

	@Override
	public SimpleSiteBroker reset() throws Exception
	{
		this.sites = this.data.getTable( SiteTuple.class );
		this.households = this.data.getTable( HouseholdTuple.class ).onCreate(
				hh -> hh.set( Households.HomeSiteRef.class,
						this.regionalHomeSiteDist.draw( (String) hh
								.get( Households.HomeRegionRef.class ) ) ),
				scheduler()::fail );

//		this.persons = 
		this.data.getTable( PersonTuple.class ).onCreate( this::onCreate,
				scheduler()::fail );

		final Map<String, ProbabilityDistribution<String>> gmZips = CbsRegionCentroidDensity
				.parse( this.config.cbsZipcodeResidenceData(), this.distFactory,
						( keys, values ) ->
						{
							final String siteName = String.join( "_", keys );
							final double[] coords = {
					values.get( CbsRegionCentroidDensity.ExportCol.LATITUDE )
							.asDouble(), values
									.get( CbsRegionCentroidDensity.ExportCol.LONGITUDE )
									.asDouble() };
							this.siteCoords.put( siteName, coords );
							return siteName;
						} );

		try( final InputStream is = this.config.duoPrimarySchoolData() )
		{
			final Map<String, Map<EduCulture, ProbabilityDistribution<String>>> dists = DuoPrimarySchool
					.parse( is, this.distFactory, ( id, arr ) ->
					{
						this.schoolCache.computeIfAbsent( id, k -> arr );
						return EduCulture.resolvePO( arr );
					} );

			this.primarySchoolDist = ConditionalDistribution.of( params -> dists
					.computeIfAbsent( params[0].toString(),
							zip -> new HashMap<>() )
					.computeIfAbsent( (EduCulture) params[1], cat -> dists
							.get( params[0] ).get( EduCulture.OTHERS ) ) );
		}

		this.regionalHomeSiteDist = regName ->
		{
			final String siteName = gmZips.computeIfAbsent( regName, k ->
			{
				// TODO map missing to new (merged) regions, see CBS 70739ned
//				LOG.debug( "Using fallback site dist for region: {}", regName );
				return gmZips.get( "GM0363" );
			} ).draw();
			final double[] siteCoords = this.siteCoords.get( siteName );
			final SiteTuple site = this.sites.insertValues(
					map -> map.put( Sites.RegionRef.class, regName )
							.put( Sites.Purpose.class, HOME_FUNCTION )
							.put( Sites.SiteName.class, siteName )
							.put( Sites.Latitude.class, siteCoords[0] )
							.put( Sites.Longitude.class, siteCoords[1] )
//							.put( Sites.Capacity.class, 10 ) 
			);
//			this.regionSites.computeIfAbsent( regName, k -> new TreeSet<>() )
//					.add( (Comparable<?>) site.key() );
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
	public SiteTuple findNearby( final String lifeRole, final PersonTuple pp )
	{
//		final ComparableQuantity<Time> age = ageOf( person );
		final Object originSiteRef = pp.get( Persons.HomeSiteRef.class );
		final SiteTuple origin = this.sites
				.select( Objects.requireNonNull( originSiteRef, "homeless?" ) );

		final String homeZip = origin.get( Sites.SiteName.class ).substring( 5,
				9 );
		final EduCulture cult = EduCulture.OTHERS; // TODO from hh
		String schoolName = this.primarySchoolDist.draw( homeZip, cult );
		if( schoolName == null )
			LOG.debug( "No school dist for {} x {}", homeZip, cult );
//		else
//			LOG.debug( "Setting school for {}, age {}: {} x {} -> {}",
//					person, QuantityUtil.pretty( age, TimeUnits.YEAR, 1 ),
//					homeZip, cult,
//					this.schoolCache.get( schoolName ).values() );

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

}