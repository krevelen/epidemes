package nl.rivm.cib.episim.geodb;

import java.util.Collections;

import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.config.ConfigUtil;
import io.coala.log.LogUtil;
import io.coala.persist.HibernateJPAConfig.SchemaPolicy;
import nl.rivm.cib.epidemes.geodb.daycare.ChildcareRegistryEntryDao;
import io.coala.persist.JPAUtil;

/**
 * {@link GeoDBConnectorTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class GeoDBConnectorTest
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( GeoDBConnectorTest.class );

	@Test
	public void jpaHsqldbTest() throws Exception
	{
//		GeoDBConfig.exec( "SELECT * FROM ``",
//				rs -> LOG.trace( "result: {}", JDBCUtil.toString( rs ) ) );

		final HibHikConfig conf = HibHikConfig.getOrCreate();
		LOG.trace( "Testing with JPA config: {}", ConfigUtil.export( conf ) );
		EntityManagerFactory EMF = conf.createEntityManagerFactory(
				"geodb_test_pu",
				Collections.singletonMap( HibHikConfig.SCHEMA_POLICY_KEY,
						SchemaPolicy.create ),
				Collections.singletonMap( HibHikConfig.DEFAULT_SCHEMA_KEY,
						"PUBLIC" ),
				Collections.singletonMap( HibHikConfig.DATASOURCE_CLASS_KEY,
						"org.hsqldb.jdbc.JDBCDataSource" ),
				Collections.singletonMap( HibHikConfig.DATASOURCE_URL_KEY,
						"jdbc:hsqldb:mem:mymemdb" ),
				Collections.singletonMap( HibHikConfig.DATASOURCE_USERNAME_KEY,
						"SA" ),
				Collections.singletonMap( HibHikConfig.DATASOURCE_PASSWORD_KEY,
						"" ) );
		JPAUtil.session( EMF ).subscribe( em ->
		{
			em.persist( new ChildcareRegistryEntryDao() );
		}, e -> LOG.error( "Problem", e ) );
		EMF.close();
	}

}
