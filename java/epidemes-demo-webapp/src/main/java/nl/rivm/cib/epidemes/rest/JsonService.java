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
package nl.rivm.cib.epidemes.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.hibernate.cfg.AvailableSettings;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactBank;
import io.coala.enterprise.FactExchange;
import io.coala.enterprise.Transaction;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.persist.HibernateJPAConfig;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.pilot.DemePersistence;
import nl.rivm.cib.episim.pilot.DemeStats;
import nl.rivm.cib.episim.pilot.OutbreakScenario;

/**
 * {@link JsonService} see e.g. http://cxf.apache.org/docs/jax-rs.html
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Path( "/json/" )
//@Produces( {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
public class JsonService
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( JsonService.class );

	/**
	 * https://jsfiddle.net/gh/get/library/pure/highslide-software/highcharts.com/tree/master/samples/mapdata/countries/nl/nl-all
	 */
	private static final String[] HIGHCHART_PROVINCE_KEYS = { "nl-fr", "nl-gr",
			"nl-fl", "nl-ze", "nl-nh", "nl-zh", "nl-dr", "nl-ge", "nl-li",
			"nl-ov", "nl-nb", "nl-ut" };

	/**
	 * https://jsfiddle.net/gh/get/library/pure/highslide-software/highcharts.com/tree/master/samples/mapdata/countries/nl/nl-all-all
	 */
	private static final String[] HIGHCHART_MUNICIPAL_KEYS = { "nl-fr-gm0088",
			"nl-3557-gm0448", "nl-gr-gm1651", "nl-ze-gm1676", "nl-fr-gm1900",
			"nl-3560-gm1924", "nl-gr-gm0007", "nl-fl-gm0050", "nl-fl-gm0034",
			"nl-ov-gm0193", "nl-3559-gm0307", "nl-3559-gm0308",
			"nl-3557-gm1911", "nl-3557-gm0398", "nl-ov-gm0153",
			"nl-3557-gm0394", "nl-3557-gm0358", "nl-3560-gm0629",
			"nl-li-gm1640", "nl-3559-gm0632", "nl-3560-gm1901",
			"nl-3560-gm0599", "nl-3560-gm0612", "nl-3560-gm0614",
			"nl-ge-gm1586", "nl-ge-gm0197", "nl-ge-gm1859", "nl-ov-gm1708",
			"nl-dr-gm0119", "nl-fr-gm0079", "nl-fr-gm0058", "nl-fr-gm0072",
			"nl-fr-gm0093", "nl-ov-gm0166", "nl-ge-gm0244", "nl-ge-gm0269",
			"nl-fl-gm0171", "nl-fl-gm0303", "nl-ge-gm0302", "nl-3557-gm0363",
			"nl-3557-gm0392", "nl-3557-gm0479", "nl-3557-gm0431",
			"nl-3557-gm0437", "nl-3559-gm0736", "nl-3557-gm0417",
			"nl-3557-gm0376", "nl-3559-gm0317", "nl-3557-gm0420",
			"nl-3557-gm0405", "nl-3557-gm1598", "nl-3557-gm0424",
			"nl-3557-gm0453", "nl-3557-gm0375", "nl-ze-gm0664", "nl-ze-gm0654",
			"nl-ze-gm1695", "nl-ze-gm0687", "nl-ze-gm0677", "nl-3558-gm0748",
			"nl-ze-gm0716", "nl-ze-gm0717", "nl-3558-gm1723", "nl-3558-gm0744",
			"nl-fl-gm0995", "nl-3557-gm0388", "nl-gr-gm0005", "nl-gr-gm1663",
			"nl-gr-gm0053", "nl-gr-gm0056", "nl-3558-gm1719", "nl-3558-gm1709",
			"nl-3558-gm0758", "nl-3558-gm1655", "nl-gr-gm1895", "nl-gr-gm1987",
			"nl-gr-gm0040", "nl-fr-gm0140", "nl-fr-gm0055", "nl-fr-gm0051",
			"nl-fr-gm0653", "nl-3558-gm0828", "nl-3558-gm1671", "nl-gr-gm0010",
			"nl-gr-gm0024", "nl-gr-gm0003", "nl-ge-gm0267", "nl-fr-gm1891",
			"nl-fr-gm1722", "nl-fr-gm0096", "nl-ov-gm1896", "nl-ge-gm0232",
			"nl-fr-gm0082", "nl-ge-gm0230", "nl-ge-gm0243", "nl-ge-gm0233",
			"nl-ge-gm0203", "nl-ge-gm0273", "nl-3559-gm0313", "nl-3557-gm0451",
			"nl-3557-gm0362", "nl-3557-gm0415", "nl-3557-gm0384",
			"nl-3557-gm0432", "nl-3557-gm0532", "nl-3557-gm0457",
			"nl-3557-gm0425", "nl-3557-gm0381", "nl-3557-gm0402",
			"nl-3557-gm0406", "nl-3557-gm0377", "nl-3560-gm0588",
			"nl-3560-gm0584", "nl-ze-gm0718", "nl-ze-gm0678", "nl-ze-gm0715",
			"nl-ze-gm1714", "nl-3558-gm0851", "nl-3558-gm1674",
			"nl-3560-gm1783", "nl-3560-gm0518", "nl-3560-gm0556",
			"nl-3560-gm1842", "nl-gr-gm0765", "nl-fr-gm0070", "nl-3557-gm0373",
			"nl-3557-gm0441", "nl-gr-gm0009", "nl-gr-gm0014", "nl-fr-gm0059",
			"nl-fr-gm1908", "nl-fr-gm0063", "nl-gr-gm0022", "nl-fr-gm0086",
			"nl-gr-gm0015", "nl-fr-gm0090", "nl-gr-gm0025", "nl-ov-gm0148",
			"nl-ov-gm0160", "nl-ov-gm0158", "nl-ov-gm0164", "nl-ov-gm0173",
			"nl-ov-gm0163", "nl-ov-gm0175", "nl-ov-gm0177", "nl-fl-gm0184",
			"nl-ov-gm0180", "nl-ge-gm0200", "nl-ge-gm0202", "nl-ge-gm1705",
			"nl-ge-gm0241", "nl-ge-gm0252", "nl-ge-gm0265", "nl-ge-gm0213",
			"nl-ge-gm0277", "nl-3559-gm0352", "nl-ge-gm0216", "nl-ge-gm0236",
			"nl-ge-gm0281", "nl-ge-gm0275", "nl-ge-gm0293", "nl-ge-gm0225",
			"nl-ge-gm0296", "nl-ge-gm0299", "nl-ge-gm0222", "nl-3559-gm0339",
			"nl-ge-gm0279", "nl-ge-gm0228", "nl-3559-gm0340", "nl-ge-gm0289",
			"nl-3559-gm0344", "nl-3559-gm0312", "nl-3559-gm0321",
			"nl-3559-gm0335", "nl-3559-gm0353", "nl-3559-gm0351",
			"nl-3559-gm0355", "nl-3557-gm0393", "nl-3557-gm0365",
			"nl-3557-gm0370", "nl-3560-gm0534", "nl-3557-gm0383",
			"nl-3557-gm0361", "nl-3557-gm0416", "nl-3557-gm0439",
			"nl-3557-gm0400", "nl-3557-gm0385", "nl-3557-gm0478",
			"nl-3557-gm0396", "nl-3560-gm0484", "nl-3560-gm0499",
			"nl-ge-gm0297", "nl-3560-gm0512", "nl-3558-gm0797",
			"nl-3558-gm0865", "nl-3560-gm0523", "nl-3558-gm0870",
			"nl-3560-gm0482", "nl-3560-gm0531", "nl-3560-gm0537",
			"nl-3560-gm0545", "nl-3560-gm0546", "nl-3560-gm0553",
			"nl-3560-gm0569", "nl-3557-gm0473", "nl-3560-gm0576",
			"nl-3560-gm0489", "nl-3560-gm0585", "nl-3560-gm0610",
			"nl-3560-gm0505", "nl-3560-gm0617", "nl-3560-gm0590",
			"nl-3560-gm0503", "nl-3560-gm1892", "nl-3560-gm0644",
			"nl-3560-gm0623", "nl-3560-gm0491", "nl-3560-gm0611",
			"nl-3560-gm0613", "nl-3560-gm0608", "nl-3559-gm0331",
			"nl-3560-gm0620", "nl-3559-gm0356", "nl-3560-gm0622",
			"nl-3560-gm0626", "nl-3560-gm0547", "nl-3560-gm0638",
			"nl-3560-gm0642", "nl-3560-gm0597", "nl-3560-gm0542",
			"nl-3560-gm0643", "nl-3560-gm0502", "nl-3560-gm0513",
			"nl-ge-gm0263", "nl-ge-gm0668", "nl-3560-gm0689", "nl-ge-gm0733",
			"nl-ge-gm0304", "nl-3559-gm1904", "nl-3558-gm0753",
			"nl-3558-gm0772", "nl-3558-gm0848", "nl-3558-gm0855",
			"nl-3558-gm0766", "nl-3558-gm0784", "nl-3558-gm0779",
			"nl-3558-gm0785", "nl-3558-gm0796", "nl-3558-gm0798",
			"nl-3558-gm1667", "nl-3558-gm0823", "nl-3558-gm1728",
			"nl-3558-gm1659", "nl-3558-gm0820", "nl-3558-gm0846",
			"nl-3558-gm0845", "nl-3558-gm0794", "nl-3558-gm1652",
			"nl-3558-gm0847", "nl-3557-gm0852", "nl-3558-gm0815",
			"nl-3558-gm1685", "nl-3558-gm0786", "nl-3558-gm0856",
			"nl-3558-gm0858", "nl-3558-gm0757", "nl-3558-gm1724",
			"nl-3558-gm0861", "nl-3558-gm0866", "nl-3558-gm0867",
			"nl-3558-gm0874", "nl-3557-gm0880", "nl-li-gm0889", "nl-li-gm0899",
			"nl-li-gm0881", "nl-li-gm0882", "nl-li-gm0917", "nl-li-gm0888",
			"nl-li-gm0971", "nl-li-gm1883", "nl-li-gm0938", "nl-li-gm0962",
			"nl-li-gm0935", "nl-li-gm0994", "nl-li-gm0986", "nl-3560-gm1525",
			"nl-3559-gm0345", "nl-3559-gm1581", "nl-3557-gm0458",
			"nl-li-gm0984", "nl-3558-gm1658", "nl-li-gm1669", "nl-li-gm1641",
			"nl-li-gm0957", "nl-3560-gm0627", "nl-3560-gm1672",
			"nl-3560-gm1621", "nl-3560-gm0637", "nl-3558-gm0873",
			"nl-gr-gm0018", "nl-li-gm0907", "nl-3558-gm0756", "nl-3558-gm1684",
			"nl-3557-gm1696", "nl-3559-gm0310", "nl-dr-gm1699", "nl-ov-gm1700",
			"nl-dr-gm0118", "nl-dr-gm1701", "nl-dr-gm1731", "nl-3558-gm1702",
			"nl-3558-gm0755", "nl-ge-gm0196", "nl-ge-gm0226", "nl-3558-gm1721",
			"nl-li-gm0965", "nl-li-gm1729", "nl-gr-gm1730", "nl-ge-gm1740",
			"nl-ge-gm0262", "nl-ov-gm1742", "nl-ov-gm0150", "nl-3558-gm1771",
			"nl-ge-gm0285", "nl-ov-gm1773", "nl-ge-gm1876", "nl-3560-gm1884",
			"nl-3558-gm0743", "nl-li-gm1894", "nl-3558-gm0762",
			"nl-3560-gm1916", "nl-3560-gm0568", "nl-3560-gm1926",
			"nl-3560-gm1927", "nl-3557-gm0498", "nl-3560-gm0530",
			"nl-3560-gm0501", "nl-ze-gm0703", "nl-3558-gm0840",
			"nl-3558-gm0879", "nl-li-gm0928", "nl-li-gm1711", "nl-ov-gm1774",
			"nl-gr-gm0037", "nl-dr-gm1680", "nl-gr-gm0047", "nl-fr-gm0080",
			"nl-fr-gm0081", "nl-fr-gm0098", "nl-dr-gm0109", "nl-ov-gm0147",
			"nl-ov-gm0141", "nl-ov-gm0189", "nl-ge-gm0214", "nl-ge-gm0209",
			"nl-ge-gm0246", "nl-ge-gm0221", "nl-ge-gm0268", "nl-ge-gm0282",
			"nl-ge-gm1955", "nl-ge-gm0301", "nl-3559-gm0327", "nl-3559-gm0342",
			"nl-3557-gm0399", "nl-3557-gm0397", "nl-3560-gm0575",
			"nl-3560-gm0579", "nl-3559-gm0589", "nl-3560-gm0603",
			"nl-3560-gm0707", "nl-fr-gm0737", "nl-3558-gm0738",
			"nl-3558-gm0770", "nl-3558-gm0809", "nl-3558-gm0788",
			"nl-3558-gm0824", "nl-3558-gm0826", "nl-3558-gm0777",
			"nl-3558-gm0860", "nl-li-gm0944", "nl-li-gm0946", "nl-li-gm1507",
			"nl-li-gm0893", "nl-3558-gm1706", "nl-gr-gm0048", "nl-dr-gm1681",
			"nl-dr-gm1690", "nl-ge-gm1734", "nl-li-gm0983", "nl-ge-gm1509",
			"nl-ov-gm0183", "nl-ge-gm0294", "nl-li-gm0988", "nl-li-gm1903",
			"nl-fr-gm0085", "nl-dr-gm0106", "nl-ov-gm1735", "nl-3557-gm0450",
			"nl-gr-gm0017", "nl-3560-gm0606", "nl-li-gm0951", "nl-fr-gm0060",
			"nl-ov-gm0168", "nl-dr-gm0114", "nl-li-gm0981", "nl-fr-gm0074",
			"nl-ge-gm0274", "nl-3558-gm0844" /* , "undefined" */ };

	private static Map<Region.ID, String> PROVINCIAL_ID_MAP = Arrays
			.stream( HIGHCHART_PROVINCE_KEYS )
			.collect( Collectors.toMap(
					s -> Region.ID.of( "PV" + s.substring( 3 ) ), s -> s,
					( s1, s2 ) -> s1 ) );

	private static Map<Region.ID, String> MUNICIPAL_ID_MAP = Arrays
			.stream( HIGHCHART_MUNICIPAL_KEYS )
			.collect( Collectors.toMap(
					s -> Region.ID
							.of( s.substring( s.length() - 7, s.length() - 1 )
									.toUpperCase() ),
					s -> s, ( s1, s2 ) -> s1 ) );

	/**
	 * 
	 * {@link HighChartEntry}
	 */
	public static class HighChartEntry
	{
		@JsonProperty( "hc-key" )
		public String key;

		@JsonProperty( "value" )
		public Number value;

		public HighChartEntry( final String key, final Number value )
		{
			this.key = key;
			this.value = value;
		}
	}

	/**
	 * match container's JNDI settings (in pom.xml) and the web application
	 * ResourceLink (in context.xml) and web servlet's resource-ref (in
	 * web.xml), where global "java:comp/env/" context name is prepended by
	 * Tomcat
	 */
	public static final String DATASOURCE_JNDI = "java:comp/env/" // 
			+ "jdbc/demoDB";

	/**
	 * {@link DatasourceConfig} for the JPA EntityManagerFactory
	 */
	public interface DatasourceConfig extends HibernateJPAConfig
	{
		@DefaultValue( "epidemes_pu" ) // match persistence.xml
		@Key( JPA_UNIT_NAMES_KEY )
		String[] jpaUnitNames();

		@Override
		@DefaultValue( DATASOURCE_JNDI ) // match pom.xml JNDI settings
		@Key( AvailableSettings.DATASOURCE )
		String jdbcDatasourceJNDI();
	}

	private OutbreakScenario scenario = null;

	public JsonService()
	{
		// connect and setup database persistence FIXME bind EMF to context JNDI
		final EntityManagerFactory EMF = ConfigCache
				.getOrCreate( DatasourceConfig.class ).createEMF();

		// load scenario
		try
		{
			final LocalBinder binder = LocalConfig.builder()
					.withId( "outbreak1" )

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
					.withProvider( DemePersistence.class,
							DemePersistence.MemCache.class )
					.withProvider( DemeStats.class, DemeStats.Simple.class )

					.build().createBinder( Collections
							.singletonMap( EntityManagerFactory.class, EMF ) );

			// init scenario TODO run & generate output later...
			this.scenario = binder.inject( OutbreakScenario.class );
		} catch( final Exception e )
		{
			LOG.error( "Problem", e );
		}

		LOG.info( "Created {}", getClass().getSimpleName() );
	}

	@GET
	@Path( "/province" )
	@Produces( MediaType.APPLICATION_JSON )
	public String getProvinceValues()
	{
//		final Random rnd = new Random();
//		return JsonUtil.toJSON( Arrays.stream( HIGHCHART_PROVINCE_KEYS )
//				.map( k -> new HighChartEntry( k,
//						rnd.nextInt( HIGHCHART_PROVINCE_KEYS.length ) ) )
//				.collect( Collectors.toList() ) );
		return JsonUtil.toJSON( PROVINCIAL_ID_MAP.entrySet().parallelStream()
				.map( e -> new HighChartEntry( e.getValue(),
						this.scenario.getRegionalValue( e.getKey() ) ) )
				.collect( Collectors.toList() ) );
	}

	@GET
	@Path( "/municipal" )
	@Produces( MediaType.APPLICATION_JSON )
	public List<HighChartEntry> getMunicipalValues()
	{
//		final Random rnd = new Random();
//		return Arrays.stream( HIGHCHART_MUNICIPAL_KEYS )
//				.map( k -> new HighChartEntry( k,
//						rnd.nextInt( HIGHCHART_PROVINCE_KEYS.length ) ) )
//				.collect( Collectors.toList() );
		return MUNICIPAL_ID_MAP.entrySet().parallelStream()
				.map( e -> new HighChartEntry( e.getValue(),
						this.scenario.getRegionalValue( e.getKey() ) ) )
				.collect( Collectors.toList() );
	}

}
