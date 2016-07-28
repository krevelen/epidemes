package nl.rivm.cib.episim.persist.kundera;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.aeonbits.owner.util.Collections.map;

import java.util.Map;

import static org.aeonbits.owner.util.Collections.entry;

/**
 * {@link KunderaEMF} provides a singleton {@link EntityManagerFactory}
 * implemented by the Kundera JPA2.1-compliant Object-<>-NoSQL mapper,
 * configurable for e.g. Neo4J graph database. See also <a href=
 * "https://xamry.wordpress.com/2013/03/01/marrying-jpa-with-graph-databases/">
 * this blog post</a> by the developer
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class KunderaEMF
{
	/** the PERSISTENCE_UNIT_NAME */
	private static final String PERSISTENCE_UNIT_NAME = "twissandra,twingo,twirdbms";

	/** the PERSISTENCE_CONFIG FIXME read from config */
	@SuppressWarnings( "unchecked" )
	private static final Map<?, ?> PERSISTENCE_CONFIG = map(
			entry( "key", "value" ), entry( "key", "value" ),
			entry( "key", "value" ) );

	/** the singleton INSTANCE */
	private static KunderaEMF INSTANCE = null;

	/** the emf */
	private final EntityManagerFactory emf;

	/**
	 * {@link KunderaEMF} singleton constructor
	 */
	private KunderaEMF()
	{
		this.emf = Persistence.createEntityManagerFactory(
				PERSISTENCE_UNIT_NAME, PERSISTENCE_CONFIG );
	}

	/** @return the singleton {@link KunderaEMF} instance */
	public static synchronized KunderaEMF getInstance()
	{
		if( INSTANCE == null ) INSTANCE = new KunderaEMF();
		return INSTANCE;
	}

	public static EntityManager createEntityManager()
	{
		return getInstance().emf.createEntityManager();
	}
}
