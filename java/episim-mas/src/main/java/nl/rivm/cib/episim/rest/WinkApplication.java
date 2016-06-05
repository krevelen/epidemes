package nl.rivm.cib.episim.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * {@link WinkApplication}
 */
public class WinkApplication extends Application
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