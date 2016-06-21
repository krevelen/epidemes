package nl.rivm.cib.episim.mas.eve;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.Logger;

import com.almende.eve.agent.AgentConfig;
import com.almende.eve.agent.AgentHost;
import com.almende.eve.capabilities.Config;
import com.almende.eve.deploy.Boot;
import com.almende.eve.state.State;
import com.almende.eve.state.StateBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.log.LogUtil;
import io.coala.resource.x.FileUtil;

/**
 * {@link MasServletContextListener} allows {@code init-param}s:
 * <dl>
 * <dt>{@code "eve.state.lifecycle"}</dt>
 * <dd>{@code "session"} to delete Eve agent states upon session time-out.
 * Default is {@code null}.</dd>
 * </dl>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@SuppressWarnings( "deprecation" )
public class MasServletContextListener implements ServletContextListener
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( MasServletContextListener.class );

	private static final String STATE_LIFECYCLE_PARAM = "eve.state.lifecycle";

	private static final String STATE_LIFECYCLE_SESSION = "session";

	private Config eveConfig = null;

	private boolean deleteEveStates = false;

	@Override
	public void contextInitialized( final ServletContextEvent sce )
	{
		final ServletContext sc = sce.getServletContext();

		final Object lifecycle = sc.getAttribute( STATE_LIFECYCLE_PARAM );
		if( lifecycle != null && lifecycle instanceof String )
			this.deleteEveStates = ((String) lifecycle)
					.equalsIgnoreCase( STATE_LIFECYCLE_SESSION );

		// Get the eve.yaml file and load it. (In production code this requires some serious input checking)
		final String path = sc.getInitParameter( "eve_config" ) == null
				? "eve.yaml" : sc.getInitParameter( "eve_config" );
		try( final InputStream is = FileUtil.toInputStream( path,
				sc.getClassLoader() ) )
		{
			this.eveConfig = (Config) Boot.boot( is );
		} catch( final Throwable e )
		{
			LOG.warn( "Problem: {}", e.getMessage() );
			final String fullname = "WEB-INF/" + path;
			try( final InputStream is = FileUtil.toInputStream( fullname,
					sc.getClassLoader() ) )
			{
				this.eveConfig = (Config) Boot.boot( is );
			} catch( final IOException e1 )
			{
				LOG.warn( "Problem: {}", e1.getMessage() );
			}
		}
	}

	/** Delete all agent state */
	@Override
	public void contextDestroyed( final ServletContextEvent sce )
	{
		if( !this.deleteEveStates || this.eveConfig == null ) return;

		final AgentHost host = AgentHost.getInstance();
		for( JsonNode agentNode : this.eveConfig.withArray( "agents" ) )
		{
			if( !agentNode.isObject() )
			{
				LOG.warn( "Invalid agent config: {}", agentNode );
				continue;
			}
			final AgentConfig agentCfg = new AgentConfig(
					(ObjectNode) agentNode );

			// TODO send "die" event to agent before deletion?
			// host.getAgent( agentCfg.getId() )...;
			final State state = new StateBuilder()
					.withConfig( agentCfg.with( "state" ) ).build();
			if( state != null ) state.delete();
			host.deleteAgent( agentCfg.getId() );
		}

	}
}