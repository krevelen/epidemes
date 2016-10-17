package nl.rivm.cib.episim.tpl;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;

/**
 * {@link ServletContextListenerFreemarker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ServletContextListenerFreemarker implements ServletContextListener
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( ServletContextListenerFreemarker.class );

	@Override
	public void contextInitialized( final ServletContextEvent sce )
	{
		final ServletContext sc = sce.getServletContext();

		// TODO boot Eve?
		LOG.trace( "Got servlet context: {}", sc );
	}

	@Override
	public void contextDestroyed( final ServletContextEvent sce )
	{
		// empty
	}
}