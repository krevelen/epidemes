package nl.rivm.cib.episim.geodb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;

/**
 * {@link GeoDBConnector}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class GeoDBConnector
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( GeoDBConnector.class );

	{
		try
		{
			Class.forName( "org.postgresql.Driver" );
		} catch( final ClassNotFoundException e )
		{
			LOG.error( "Problem loading PostgreSQL driver", e );
		}
	}

	public static Connection connect() throws SQLException
	{
		final String host = "geodb.rivm.nl";//"pgl04-int-p.rivm.nl";
		final String db = "sde_gdbrivm";
		String url = String.format( "jdbc:postgresql://%s/%s", host, db );
		Properties props = new Properties();
		props.setProperty( "user", "krevelvr" );
		props.setProperty( "password", "Blu3b!rdRivm" );
		props.setProperty( "ssl", "true" );
		return DriverManager.getConnection( url, props );
	}
}
