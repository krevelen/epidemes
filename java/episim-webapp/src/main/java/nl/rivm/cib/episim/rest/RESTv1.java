package nl.rivm.cib.episim.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import nl.rivm.cib.episim.api.rest.AggregatorWS;
import nl.rivm.cib.episim.api.rest.WelcomeWS;

/**
 * {@link RESTv1}
 */
public class RESTv1 extends Application
{

	/** */
	private final Set<Class<?>> restClasses = new HashSet<Class<?>>(
			Arrays.asList( WelcomeWS.class, AggregatorWS.class ) );

	@Override
	public Set<Class<?>> getClasses()
	{
		return this.restClasses;
	}
}