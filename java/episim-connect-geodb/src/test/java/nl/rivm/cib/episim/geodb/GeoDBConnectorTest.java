package nl.rivm.cib.episim.geodb;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Ignore;
import org.junit.Test;

/**
 * {@link GeoDBConnectorTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class GeoDBConnectorTest
{

	@Ignore // FIXME conditionally run inside rivm.nl domain/network only
	@Test
	public void test() throws SQLException
	{
		final Connection conn = GeoDBConnector.connect();
		final Statement stat = conn.createStatement();
		final ResultSet res = stat.executeQuery( "SELECT * FROM ``" );
		assertNotNull( "null result meta data", res.getMetaData() );
	}

}
