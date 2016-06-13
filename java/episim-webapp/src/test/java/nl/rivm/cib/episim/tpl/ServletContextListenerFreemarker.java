package nl.rivm.cib.episim.tpl;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.Logger;

import com.almende.eve.deploy.Boot;

import io.coala.log.LogUtil;
import io.coala.resource.x.FileUtil;

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

		
	}

	@Override
	public void contextDestroyed( final ServletContextEvent sce )
	{
		// empty
	}
}