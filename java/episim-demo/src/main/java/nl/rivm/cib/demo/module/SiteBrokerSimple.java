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
package nl.rivm.cib.demo.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.aeonbits.owner.util.Collections;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.Table;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.WeightedValue;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Scheduler;
import io.coala.util.InputStreamConverter;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.csv.DuoPrimarySchool;
import nl.rivm.cib.csv.DuoPrimarySchool.EduCol;
import nl.rivm.cib.demo.DemoConfig;
import nl.rivm.cib.demo.DemoModel.EpiFact;
import nl.rivm.cib.demo.DemoModel.Regional.SiteBroker;
import nl.rivm.cib.demo.Households;
import nl.rivm.cib.demo.Households.HouseholdTuple;
import nl.rivm.cib.demo.Pedagogy;
import nl.rivm.cib.demo.Persons;
import nl.rivm.cib.demo.Persons.PersonTuple;
import nl.rivm.cib.demo.Sites;
import nl.rivm.cib.demo.Sites.BuiltFunction;
import nl.rivm.cib.demo.Sites.SiteTuple;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.json.CbsRegionCentroidDensity;
import nl.rivm.cib.json.CbsRegionCentroidDensity.ExportCol;

/**
 * {@link SiteBrokerSimple}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class SiteBrokerSimple implements SiteBroker
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SiteBrokerSimple.class );

	public interface SiteConfig extends YamlConfig
	{
		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

		@Key( "hh-zip-density" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
				+ "gm_pc6_centroid_density.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbsZipcodeDensityData();

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

	// TODO map missing to new (historic/merged) regions, eg CBS70739ned
	private static final String FALLBACK_REG = "GM0363";

	private final AtomicLong siteSeq = new AtomicLong();

//	private Table<Persons.PersonTuple> persons;
	private Table<HouseholdTuple> households;
	private Table<SiteTuple> sites;
//	private Table<Regions.RegionTuple> regions;

	//	private IndexPartition sirIndex;
	/** east-west, longitude, meridian */
//	private IndexPartition ewMeridian;
	/** north-south, latitude, parallel */
//	private IndexPartition nsParallel;

	/** */
	private final Map<String, EnumMap<ExportCol, JsonNode>> zipCache = new HashMap<>();
	/** */
	private final Map<String, EnumMap<DuoPrimarySchool.EduCol, JsonNode>> schoolCache = new HashMap<>();
	/** zip codes with residences */
	private ConditionalDistribution<String, String> regionalHomeSiteDist;
	/** zip codes with FTE >= {@link #SMALL_EMPLOYER_CAPACITY} */
	private ConditionalDistribution<String, String> regionalCorpZipDist;
	/** zip codes with FTE < {@link #SMALL_EMPLOYER_CAPACITY} */
	private ConditionalDistribution<String, String> regionalSMESiteDist;
	/** */
	private TreeMap<String, Map<Pedagogy, ProbabilityDistribution<String>>> primarySchools;

	@Override
	public SiteBrokerSimple reset() throws Exception
	{
		this.sites = this.data.getTable( SiteTuple.class );
		this.households = this.data.getTable( HouseholdTuple.class )
				.onCreate( this::assignResidence, scheduler()::fail );

//		this.persons = 
		this.data.getTable( PersonTuple.class )
				.onCreate( this::copyHouseholdHome, scheduler()::fail );

		LOG.debug( "...importing home sites" );
		setupResidentialSites();
		LOG.debug( "...importing large enterprise/corporate sites" );
		setupIndustrialSites();
		LOG.debug( "...importing small & medium enterprise sites" );
		setupSMESites();
		LOG.debug( "...importing primary school sites" );
		setupSchoolSites();

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	private Stream<WeightedValue<String>> toWeightedValues(
		final EnumMap<CBSRegionType, String> keys,
		final EnumMap<ExportCol, JsonNode> values, final ExportCol weightCol )
	{
		final String siteName = String.join( "_", keys.values().stream()
				.map( Object::toString ).toArray( String[]::new ) );
		this.zipCache.computeIfAbsent( siteName, k -> values );
		return Stream.of(
				WeightedValue.of( siteName, values.get( weightCol ).asInt() ) );
	}

	protected void setupResidentialSites() throws IOException
	{
		try( final InputStream is = this.config.cbsZipcodeDensityData() )
		{
			final Map<String, ProbabilityDistribution<String>> residenceDists = //
					CbsRegionCentroidDensity.parse( is, this.distFactory,
							( keys, zip6 ) -> toWeightedValues( keys, zip6,
									ExportCol.RESIDENTS ) );

			this.regionalHomeSiteDist = regName -> residenceDists
					.computeIfAbsent( regName,
							k -> residenceDists.get( FALLBACK_REG ) )
					.draw();
		}
	}

	protected void setupIndustrialSites() throws IOException
	{
		try( final InputStream is = this.config.cbsZipcodeDensityData() )
		{
			final Map<String, ProbabilityDistribution<String>> workZipDists = //
					CbsRegionCentroidDensity.parse( is, this.distFactory,
							( keys,
								zip6 ) -> zip6.get( ExportCol.EMPLOYEES )
										// skip small-medium enterprise zones
										.asInt() < ZIP6_SME_FTE_LIMIT
												? Stream.empty()
												: toWeightedValues( keys, zip6,
														ExportCol.EMPLOYEES ) );

			this.regionalCorpZipDist = regName -> workZipDists.computeIfAbsent(
					regName, k -> workZipDists.get( FALLBACK_REG ) ).draw();
		}
	}

	protected void setupSMESites() throws IOException
	{
		try( final InputStream is = this.config.cbsZipcodeDensityData() )
		{
			final Map<String, ProbabilityDistribution<String>> smeZipDists = //
					CbsRegionCentroidDensity.parse( is, this.distFactory,
							( keys,
								zip6 ) -> zip6.get( ExportCol.EMPLOYEES )
										// only small-medium enterprise zones
										.asInt() >= ZIP6_SME_FTE_LIMIT
												? Stream.empty()
												: toWeightedValues( keys, zip6,
														ExportCol.EMPLOYEES ) );

			this.regionalSMESiteDist = regName -> smeZipDists.computeIfAbsent(
					regName, k -> smeZipDists.get( FALLBACK_REG ) ).draw();
		}
	}

	protected void setupSchoolSites() throws IOException
	{
		try( final InputStream is = this.config.duoPrimarySchoolData() )
		{
			this.primarySchools = DuoPrimarySchool.parse( is, this.distFactory,
					( id, values ) ->
					{
						// cache school data
						this.schoolCache.computeIfAbsent( id, k -> values );
						// resolve school categories for assortative hh-sampling
						return Stream.of( Pedagogy.resolveDuo( values ),
								Pedagogy.ALL );
					} );
		}
	}

	protected void assignResidence( final HouseholdTuple hh )
	{
		final String homeReg = (String) hh
				.get( Households.HomeRegionRef.class );
		final String homeBoroZip = this.regionalHomeSiteDist.draw( homeReg );
		final EnumMap<ExportCol, JsonNode> zipData = this.zipCache
				.get( homeBoroZip );
		final SiteTuple site = this.sites.insertValues( map -> map
				.set( Sites.RegionRef.class, homeReg )
				.set( Sites.SiteFunction.class, BuiltFunction.RESIDENCE )
				.set( Sites.SiteName.class,
						homeBoroZip + "/" + this.siteSeq.incrementAndGet() )
				.set( Sites.Latitude.class,
						zipData.get( ExportCol.LATITUDE ).asDouble() )
				.set( Sites.Longitude.class,
						zipData.get( ExportCol.LONGITUDE ).asDouble() )
//				.put( Sites.Capacity.class, 
//									zipData.get( ExportCol.RESIDENTIAL ) 
		);
		hh.set( Households.HomeSiteRef.class, (Comparable<?>) site.key() );
	}

	protected void copyHouseholdHome( final PersonTuple pp )
	{
		final HouseholdTuple hh = this.households
				.select( pp.get( Persons.HouseholdRef.class ) );
		final Comparable<?> homeRegRef = hh
				.get( Households.HomeRegionRef.class ),
				homeSiteRef = hh.get( Households.HomeSiteRef.class );
		pp.set( Persons.HomeRegionRef.class, homeRegRef );
		pp.set( Persons.HomeSiteRef.class, homeSiteRef );
	}

	private double distanceSquaredDegrees( final double[] latlon,
		final SiteTuple site )
	{
		return distanceSquaredDegrees( latlon, site.get( Sites.Latitude.class ),
				site.get( Sites.Longitude.class ) );
	}

	private double distanceSquaredDegrees( final double[] x, final double... y )
	{
		double[] dx = { x[0] - x[0], x[1] - y[1] };
		return dx[0] * dx[0] + dx[1] * dx[1];
	}

	@Override
	public <T> Entry<T, Double> selectNearest( final PersonTuple pp,
		final Stream<T> options, final Function<T, Object> optionSiteKeyMapper )
	{
		return selectNearest( pp, options,
				( targetCoords, t ) -> distanceSquaredDegrees( targetCoords,
						this.sites.select( optionSiteKeyMapper.apply( t ) ) ) );
	}

	public <T> Entry<T, Double> selectNearest( final PersonTuple pp,
		final Stream<T> options,
		final BiFunction<double[], T, Double> distanceMapper )
	{
		final SiteTuple targetSite = this.sites
				.select( pp.get( Persons.HomeSiteRef.class ) );
		final double[] targetCoords = { targetSite.get( Sites.Latitude.class ),
				targetSite.get( Sites.Longitude.class ) };
		return options
				.map( t -> Collections.entry( t,
						distanceMapper.apply( targetCoords, t ) ) )
				.min( ( l, r ) -> Double.compare( l.getValue(), r.getValue() ) )
				.orElse( null );
	}

	@Override
	public SiteTuple createLocalSME( final PersonTuple pp )
	{
		final String smeRegRef = (String) pp.get( Persons.HomeRegionRef.class );
		final String smeBoroZip = this.regionalSMESiteDist.draw( smeRegRef );
		final EnumMap<ExportCol, JsonNode> smeZipData = this.zipCache
				.get( smeBoroZip );
		return this.sites.insertValues( map -> map
				.set( Sites.RegionRef.class, smeRegRef )
				.set( Sites.SiteFunction.class, BuiltFunction.SMALL_ENTERPRISE )
				.set( Sites.SiteName.class,
						smeBoroZip + "/" + this.siteSeq.incrementAndGet() )
				.set( Sites.Latitude.class,
						smeZipData.get( ExportCol.LATITUDE ).asDouble() )
				.set( Sites.Longitude.class,
						smeZipData.get( ExportCol.LONGITUDE ).asDouble() )
//				.put( Sites.Capacity.class,
//						smeZipData.get( ExportCol.EMPLOYEES ) ) 
		);
	}

	@Override
	public SiteTuple createLocalIndustry( final PersonTuple pp )
	{
		final String corpRegRef = (String) pp
				.get( Persons.HomeRegionRef.class );
		final String corpBoroZip = this.regionalCorpZipDist.draw( corpRegRef );
		final EnumMap<ExportCol, JsonNode> corpZipData = this.zipCache
				.get( corpBoroZip );
		return this.sites.insertValues( map -> map
				.set( Sites.RegionRef.class, corpRegRef )
				.set( Sites.SiteFunction.class, BuiltFunction.LARGE_ENTERPRISE )
				.set( Sites.SiteName.class,
						corpBoroZip + "/" + this.siteSeq.incrementAndGet() )
				.set( Sites.Latitude.class,
						corpZipData.get( ExportCol.LATITUDE ).asDouble() )
				.set( Sites.Longitude.class,
						corpZipData.get( ExportCol.LONGITUDE ).asDouble() )
//				.put( Sites.Capacity.class,
//						corpZipData.get( ExportCol.EMPLOYEES ) ) 
		);
	}

	@Override
	public SiteTuple assignLocalPrimarySchool( final PersonTuple pp )
	{
		final HouseholdTuple hh = this.households
				.get( pp.get( Persons.HouseholdRef.class ) );
		final Pedagogy hhPedagogy = hh.get( Households.EduCulture.class );
		final SiteTuple homeSite = this.sites
				.select( pp.get( Persons.HomeSiteRef.class ) );
		// crop "0153_00_05_7512_CJ" (GM_WK_BU_PC4_PC6) to PC4 [11..11+4]
		final String siteName = homeSite.get( Sites.SiteName.class ),
				homeZip = siteName.substring( 11, 15 );

		final Map<Pedagogy, ProbabilityDistribution<String>> zipSchools = this.primarySchools
				.computeIfAbsent( homeZip, k -> new HashMap<>() );
		final ProbabilityDistribution<String> pedagogySchools = hhPedagogy == null
				? null : zipSchools.get( hhPedagogy );

		final String schoolName;
		if( pedagogySchools != null ) // hh already has a pedagogy (for sibling)
			schoolName = pedagogySchools.draw();
		else if( hhPedagogy == null && !zipSchools.isEmpty() )
		{
			schoolName = zipSchools.get( Pedagogy.ALL ).draw();
		} else
		{
			// search all schools for the nearest of the same pedagogy, if any
			final Entry<Entry<String, EnumMap<EduCol, JsonNode>>, Double> nearest = selectNearest(
					pp,
					this.schoolCache.entrySet().stream()
							.filter( e -> hhPedagogy == null || Pedagogy
									.resolveDuo( e.getValue() ) == hhPedagogy ),
					( targetCoords, entry ) -> distanceSquaredDegrees(
							targetCoords,
							entry.getValue().get( EduCol.LATITUDE ).asDouble(),
							entry.getValue().get( EduCol.LONGITUDE )
									.asDouble() ) );

			schoolName = nearest.getKey().getKey();
			LOG.debug( "...no record for {}/{}, elected nearest: {} (at {}km)",
					homeZip, hhPedagogy, schoolName, DecimalUtil.pretty(
							() -> Math.sqrt( nearest.getValue() ) * 111, 2 ) );

			// cache search result for other children from same zip/pedagogy
			zipSchools
					.computeIfAbsent(
							hhPedagogy == null
									? Pedagogy.resolveDuo(
											nearest.getKey().getValue() )
									: hhPedagogy,
							k -> ProbabilityDistribution
									.createDeterministic( schoolName ) );

			final SiteTuple schoolSite = this.sites
					.selectWhere(
							site -> site.isEqual( Sites.SiteFunction.class,
									BuiltFunction.PRIMARY_EDUCATION )
									&& site.isEqual( Sites.SiteName.class,
											schoolName ) )
					.findAny().orElse( null );
			if( schoolSite != null ) return schoolSite;
		}

		final EnumMap<EduCol, JsonNode> values = this.schoolCache
				.get( schoolName );
		final Pedagogy sitePedagogy = Pedagogy.resolveDuo( values );

		// update household culture / pedagogy
		if( hhPedagogy == null )
			hh.updateAndGet( Households.EduCulture.class, old -> sitePedagogy );

		// create the elected school's site
		return this.sites.insertValues( map -> map
				.set( Sites.SiteName.class, schoolName )
				.set( Sites.SiteFunction.class,
						BuiltFunction.PRIMARY_EDUCATION )
				.set( Sites.EduCulture.class, sitePedagogy )
				.set( Sites.RegionRef.class,
						values.get( DuoPrimarySchool.EduCol.GEMEENTE )
								.asText() )
				.set( Sites.Latitude.class,
						values.get( DuoPrimarySchool.EduCol.LATITUDE )
								.asDouble() )
				.set( Sites.Longitude.class, values
						.get( DuoPrimarySchool.EduCol.LONGITUDE ).asDouble() )
//				.put( Sites.Capacity.class, 1000 ) 
		);
	}
}