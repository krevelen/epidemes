package nl.rivm.cib.episim.pienter2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;

/**
 * {@link Pienter2Reader}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Pienter2Reader
{
	/** */
	private static final Logger LOG = LogManager
			.getLogger( Pienter2Reader.class );

	public static void openFile()
	{
		
		DataContext dataContext = null;//TODO DataContextFactory.createSAS?(...);
		@SuppressWarnings( "null" )
		DataSet dataSet = dataContext.query()
		    .from("libraries")
		    .select("name")
		    .where("language").eq("Java")
		    .and("enhances_data_access").eq(true)
		    .execute();
		LOG.trace("Got a nice dataset: {}", dataSet);
	}
}
