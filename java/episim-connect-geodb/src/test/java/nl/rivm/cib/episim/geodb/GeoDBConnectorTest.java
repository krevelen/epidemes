package nl.rivm.cib.episim.geodb;

import static org.aeonbits.owner.util.Collections.entry;
import static org.aeonbits.owner.util.Collections.map;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.persist.HibernateJPAConfig.SchemaPolicy;
import io.coala.persist.JDBCUtil;
import io.coala.persist.JPAUtil;
import nl.rivm.cib.epidemes.geodb.adm.LRKEntryDao;
import nl.rivm.cib.epidemes.geodb.adm.NHREntryDao;

/**
 * {@link GeoDBConnectorTest}
 * 
 * @version $Id: dfc7497317afea3de9d16ed8045f7436b2e3e8f5 $
 * @author Rick van Krevelen
 */
public class GeoDBConnectorTest
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( GeoDBConnectorTest.class );

	/**
	 * testing code from
	 * <a href="http://postgis.net/docs/ST_Transform.html">here</a> and <a href=
	 * "http://www.postgresonline.com/journal/archives/267-Creating-GeoJSON-Feature-Collections-with-JSON-and-PostGIS-functions.html">here</a>
	 */
	@Test
	public void geoJsonTest() throws ClassNotFoundException, SQLException
	{
		final GeoDBConfig conf = GeoDBConfig.getOrCreate();
		LOG.trace( "Testing with JDBC config: {}", conf.export() );
		final String tblName = "nl." + LRKEntryDao.TABLE_NAME,
				colName = "shape";
		final int epsg = 4326, limit = 100;
		conf.execute(
				String.format(
						"SELECT ST_AsGeoJSON(ST_Transform(lg.%s,%d)) "
								+ "FROM %s As lg LIMIT %d",
						colName, epsg, tblName, limit ),
				rs -> LOG.trace( "result: {}", JDBCUtil.toString( rs ) ) );
	}

	@SuppressWarnings( "unchecked" )
	@Ignore
	@Test
	public void jpaHsqldbTest() throws Exception
	{
		final HibHikConfig conf = HibHikConfig.getOrCreate( map(
				entry( HibHikConfig.SCHEMA_POLICY_KEY, SchemaPolicy.create ),
				entry( HibHikConfig.DEFAULT_SCHEMA_KEY, "PUBLIC" ),
				entry( HibHikConfig.DATASOURCE_CLASS_KEY,
						"org.hsqldb.jdbc.JDBCDataSource" ),
				entry( HibHikConfig.DATASOURCE_URL_KEY,
						"jdbc:hsqldb:file:target/geodb_test" ), //"jdbc:hsqldb:mem:mymemdb" 
				entry( HibHikConfig.DATASOURCE_USERNAME_KEY, "SA" ),
				entry( HibHikConfig.DATASOURCE_PASSWORD_KEY, "" ) ) );
		LOG.trace( "Testing with JPA config: {}", conf.export() );
		final EntityManagerFactory HSQLDB = conf
				.createEntityManagerFactory( "geodb_test_pu" );
		JPAUtil.session( HSQLDB ).subscribe(
				em -> em.persist( new LRKEntryDao() ),
				e -> LOG.error( "Problem", e ) );
		HSQLDB.close();
	}

	@SuppressWarnings( { "unchecked" } )
	@Test
	public void jpaGeodbTest() throws Exception
	{
		final HibHikConfig conf = HibHikConfig
				.getOrCreate( map( entry( HibHikConfig.SCHEMA_POLICY_KEY,
						SchemaPolicy.validate ) ) );
		LOG.trace( "Testing with JPA config: {}", conf.export() );

//		JsonUtil.getJOM().registerModule( new SimpleModule()
//				.addSerializer( new StdSerializer<Point>( Point.class )
//				{
//					@Override
//					public void serialize( final Point value,
//						final JsonGenerator gen,
//						final SerializerProvider serializers )
//						throws IOException, JsonProcessingException
//					{
//						serializers.findValueSerializer( ObjectNode.class )
//								.serialize( JsonUtil.getJOM().createObjectNode()
//										.put( "lon",
//												DecimalUtil.valueOf( value
//														.getPositionN( 0 )
//														.getCoordinate( 0 ) ) )
//										.put( "lat",
//												DecimalUtil.valueOf( value
//														.getPositionN( 0 )
//														.getCoordinate( 1 ) ) ),
//										gen, serializers );
//					}
//				} ) );

		final EntityManagerFactory GEODB = conf.createEntityManagerFactory(
				"geodb_test_pu",
				map( entry( HibHikConfig.DATASOURCE_PASSWORD_KEY,
						conf.hikariDataSourcePassword() ) ) );

		// NHR test
		JPAUtil.session( GEODB ).subscribe( em ->
		{
			final AtomicInteger count = new AtomicInteger( 0 );
			final String[] atts = { "registryCode", "name", "employeeFull",
					"employeeTotal", "zip" };
			em.createQuery(
					"SELECT e." + String.join( ", e.", atts )
							+ ", asgeojson(Transform(e.geometrie,4326)) FROM "
							+ NHREntryDao.ENTITY_NAME + " AS e",
					Object[].class ).setMaxResults( 100 ) // just to test
					.getResultList().forEach( e ->
					{
						LOG.trace( "Got NHR #{}: {}", count.incrementAndGet(),
								JsonUtil.stringify( e ) );
					} );
			LOG.trace( "Got total of {} entries", count.get() );
		}, e -> LOG.error( "Problem", e ) );

		// LRK test
		JPAUtil.session( GEODB ).subscribe( em ->
		{
			final AtomicInteger count = new AtomicInteger( 0 );
			final String[] atts = { "registryCode", "type", "childCapacity",
					"municipalityCode", "zip" };
			final Map<LRKEntryDao.OrganizationType, Integer> totals = new EnumMap<>(
					LRKEntryDao.OrganizationType.class );
			em.createQuery( "SELECT e." + String.join( ", e.", atts )
					+ ", asgeojson(Transform(e.shape,4326)) FROM "
					+ LRKEntryDao.ENTITY_NAME + " AS e WHERE e.status=:status",
					Object[].class )
					.setParameter( "status",
							LRKEntryDao.RegistryStatus.Ingeschreven )
					.setMaxResults( 100 ) // just to test
					.getResultList().forEach( e ->
					{
						totals.compute( (LRKEntryDao.OrganizationType) e[1],
								( k, v ) -> (e[2] == null ? 0 : (Integer) e[2])
										+ (v == null ? 0 : v) );

//						if( count.incrementAndGet() % 100 == 0 )
						LOG.trace( "Got LRK #{}: {} - cumulative capacity#: {}",
								count.incrementAndGet(),
								JsonUtil.stringify( e ), totals );
					} );
			LOG.trace( "Got total of {} entries - total capacity#: {}",
					count.get(), totals );
		}, e -> LOG.error( "Problem", e ) );
		GEODB.close();
	}

}
