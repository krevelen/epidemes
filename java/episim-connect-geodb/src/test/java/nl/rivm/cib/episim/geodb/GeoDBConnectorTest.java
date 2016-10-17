package nl.rivm.cib.episim.geodb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.persist.JDBCConfig;
import io.coala.persist.JDBCUtil;

/**
 * {@link GeoDBConnectorTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class GeoDBConnectorTest
{

	@Sources( { "classpath:geodb.properties" } )
	interface GeoDBConfig extends JDBCConfig
	{
		@Key( "jdbc.driver" )
		@DefaultValue( "org.postgresql.Driver" )
		String driver();

		@DefaultValue( "geodb.rivm.nl" ) //"pgl04-int-p.rivm.nl";
		String host();

		@DefaultValue( "sde_gdbrivm" )
		String db();

		@DefaultValue( "jdbc:postgresql://${host}/${db}" )
		String url();

		@DefaultValue( "" + true )
		boolean ssl();

		static void exec( final String sql, final Consumer<ResultSet> consumer )
			throws ClassNotFoundException, SQLException
		{
			ConfigCache.getOrCreate( GeoDBConfig.class ).execute( sql,
					consumer );
		}
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( GeoDBConnectorTest.class );

	@Ignore // FIXME conditionally run inside rivm.nl domain/network only
	@Test
	public void test() throws SQLException, ClassNotFoundException
	{
		GeoDBConfig.exec( "SELECT * FROM ``",
				rs -> LOG.trace( "result: {}", JDBCUtil.toString( rs ) ) );
	}

}
