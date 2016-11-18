package nl.rivm.cib.episim.geodb;

import static org.aeonbits.owner.util.Collections.entry;
import static org.aeonbits.owner.util.Collections.map;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManagerFactory;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.persist.HibernateJPAConfig.SchemaPolicy;
import io.coala.persist.JPAUtil;
import nl.rivm.cib.epidemes.geodb.lrk.LRKEntryDao;

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

//	GeoDBConfig.exec( "SELECT * FROM ``",
//	rs -> LOG.trace( "result: {}", JDBCUtil.toString( rs ) ) );

	@SuppressWarnings( "unchecked" )
//	@Test
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
		JPAUtil.session( HSQLDB ).subscribe( em ->
		{
			em.persist( new LRKEntryDao() );
		}, e -> LOG.error( "Problem", e ) );
		HSQLDB.close();
	}

	@Ignore
	@Test
	@SuppressWarnings( "unchecked" )
	public void jpaGeodbTest() throws Exception
	{
		final HibHikConfig conf = HibHikConfig
				.getOrCreate( map( entry( HibHikConfig.SCHEMA_POLICY_KEY,
						SchemaPolicy.validate ) ) );
		LOG.trace( "Testing with JPA config: {}", conf.export() );

		String passwd = conf.hikariDataSourcePassword();
		if( passwd == null || passwd.isEmpty()
				|| passwd.toLowerCase().contains( "prompt" ) )
		{
			final String message = "Enter password for "
					+ conf.hikariDataSourceUsername();
			if( System.console() == null ) // inside IDE console
			{
				final JPasswordField pf = new JPasswordField();
				passwd = JOptionPane.showConfirmDialog( null, pf, message,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE ) == JOptionPane.OK_OPTION
								? new String( pf.getPassword() ) : "";
			} else
				passwd = new String(
						System.console().readPassword( "%s> ", message ) );
		}

		final EntityManagerFactory GEODB = conf.createEntityManagerFactory(
				"geodb_test_pu",
				map( entry( HibHikConfig.DATASOURCE_PASSWORD_KEY, passwd ) ) );
		JPAUtil.session( GEODB ).subscribe( em ->
		{
			final AtomicInteger count = new AtomicInteger( 0 );
			final String[] atts = { "registryCode", "type", "childCapacity",
					"municipalityCode", "zip", "xCoord", "yCoord", "shape" };
			final Map<LRKEntryDao.OrganizationType, Integer> totals = new EnumMap<>(
					LRKEntryDao.OrganizationType.class );
			em.createQuery( "SELECT e." + String.join( ", e.", atts ) + " FROM "
					+ LRKEntryDao.ENTITY_NAME + " AS e WHERE e.status=:status",
					Object[].class )
					.setParameter( "status",
							LRKEntryDao.RegistryStatus.Ingeschreven )
					.getResultList().forEach( e ->
					{
						totals.compute( (LRKEntryDao.OrganizationType) e[1],
								( k, v ) -> (e[2] == null ? 0 : (Integer) e[2])
										+ (v == null ? 0 : v) );
						if( count.incrementAndGet() % 100 == 0 )
							LOG.trace( "Got #{}: {} - cumulative capacity#: {}",
									count.get(), JsonUtil.stringify( e ),
									totals );
					} );
			LOG.trace( "Got total of {} entries - total capacity#: {}",
					count.get(), totals );
		}, e -> LOG.error( "Problem", e ) );
		GEODB.close();
	}

}
