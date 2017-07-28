package nl.rivm.cib.epidemes.geodb.jdbc;

import java.net.URI;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.hibernate.cfg.AvailableSettings;
import org.junit.Ignore;
import org.junit.Test;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.persist.HikariHibernateJPAConfig;
import io.coala.persist.JDBCUtil;
import io.coala.persist.JPAUtil;
import nl.rivm.cib.epidemes.geodb.bag.NHREntryDao;
import nl.rivm.cib.epidemes.geodb.bag.NHREntryDao_;
import nl.rivm.cib.epidemes.geodb.voorz.LRKEntryDao;
import nl.rivm.cib.epidemes.geodb.voorz.LRKEntryDao_;

/**
 * {@link GeoDBConnectorTest}
 * 
 * @version $Id: dcf9db7e0a7189e6816afdd8169e0a38f7f0a740 $
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
		final GeoDBConfig conf = ConfigCache.getOrCreate( GeoDBConfig.class );
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

//	@Ignore
	@Test
//	@SuppressWarnings( "unchecked" )
	public void jpaGeodbTest() throws Exception
	{
		final EntityManagerFactory GEODB = ConfigCache
				.getOrCreate( GeoJPAConfig.class ).createEMF();

		// NHR test
		JPAUtil.session( GEODB ).subscribe( em ->
		{
			final AtomicInteger count = new AtomicInteger( 0 );
			final String[] atts = { NHREntryDao_.registryCode.getName(),
					NHREntryDao_.name.getName(),
					NHREntryDao_.employeeFull.getName(),
					NHREntryDao_.employeeTotal.getName(),
					NHREntryDao_.zip.getName() };
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
			final String[] atts = { LRKEntryDao_.registryCode.getName(),
					LRKEntryDao_.type.getName(),
					LRKEntryDao_.childCapacity.getName(),
					LRKEntryDao_.municipalityCode.getName(),
					LRKEntryDao_.zip.getName() };
			final Map<LRKEntryDao.OrganizationType, Integer> totals = new EnumMap<>(
					LRKEntryDao.OrganizationType.class );
			em.createQuery(
					"SELECT e." + String.join( ", e.", atts )
							+ ", asgeojson(Transform(e.shape,4326)) FROM "
							+ LRKEntryDao.ENTITY_NAME + " AS e WHERE e."
							+ LRKEntryDao_.status.getName() + "=:status",
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

//	JsonUtil.getJOM().registerModule( new SimpleModule()
//			.addSerializer( new StdSerializer<Point>( Point.class )
//			{
//				@Override
//				public void serialize( final Point value,
//					final JsonGenerator gen,
//					final SerializerProvider serializers )
//					throws IOException, JsonProcessingException
//				{
//					serializers.findValueSerializer( ObjectNode.class )
//							.serialize( JsonUtil.getJOM().createObjectNode()
//									.put( "lon",
//											DecimalUtil.valueOf( value
//													.getPositionN( 0 )
//													.getCoordinate( 0 ) ) )
//									.put( "lat",
//											DecimalUtil.valueOf( value
//													.getPositionN( 0 )
//													.getCoordinate( 1 ) ) ),
//									gen, serializers );
//				}
//			} ) );

	public interface ExportJPAConfig extends HikariHibernateJPAConfig
	{
		@DefaultValue( "geodb_test_pu" )
		String[] jpaUnitNames();

//		@DefaultValue( "jdbc:mysql://localhost/testdb" )
//		@DefaultValue( "jdbc:hsqldb:mem:mymemdb" )
//		@DefaultValue( "jdbc:neo4j:bolt://192.168.99.100:7687/db/data" )
		@DefaultValue( "jdbc:hsqldb:file:target/testdb" )
		@Key( AvailableSettings.URL )
		URI jdbcUrl();
	}

//	@SuppressWarnings( "unchecked" )
	@Ignore // FIXME some dialect problem
	@Test
	public void jpaHsqldbTest() throws Exception
	{
		final EntityManagerFactory HSQLDB = ConfigFactory
				.create( ExportJPAConfig.class ).createEMF();
		JPAUtil.session( HSQLDB ).subscribe(
				em -> em.persist( new LRKEntryDao() ),
				e -> LOG.error( "Problem", e ) );
		HSQLDB.close();
	}

}
