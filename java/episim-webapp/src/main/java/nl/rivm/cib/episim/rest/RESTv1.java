package nl.rivm.cib.episim.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * {@link RESTv1}
 */
public class RESTv1 extends Application
{

	/** */
	private final Set<Class<?>> restClasses = new HashSet<Class<?>>(
			Arrays.asList( WelcomeRSImpl.class, AggregatorRSImpl.class ) );

	@Override
	public Set<Class<?>> getClasses()
	{
		return this.restClasses;
	}
}