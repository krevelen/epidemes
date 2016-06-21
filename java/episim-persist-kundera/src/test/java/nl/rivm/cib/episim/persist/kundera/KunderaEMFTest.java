package nl.rivm.cib.episim.persist.kundera;

import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import io.coala.log.LogUtil;

public class KunderaEMFTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( KunderaEMFTest.class );

	@Ignore // FIXME !
	@Test
	public void testEntityManager()
	{
		LOG.trace( "Testing getEntityManager()..." );
		final EntityManager em = KunderaEMF.createEntityManager();
		//em.find( entityClass, primaryKey )
		assertNotNull("No entity manager", em);
	}

}
