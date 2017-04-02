/* $Id$
 * 
 * Part of ZonMW project no. 50-53000-98-156
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2016 RIVM National Institute for Health and Environment 
 */
package nl.rivm.cib.epidemes.rest;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;

/**
 * {@link RestApplication} see e.g.
 * https://cwiki.apache.org/confluence/display/CXF20DOC/JAX-RS+Deployment#JAX-RSDeployment-JBoss
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@ApplicationPath( "/rest-v1" ) // overridden by <servlet-mapping/> in web.xml
public class RestApplication extends Application
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( RestApplication.class );

	public RestApplication( @Context ServletContext sc )
	{
		LOG.trace( "starting app" );
		if( sc == null )
			throw new IllegalArgumentException( "ServletContext is null" );
//		if( !"contextParamValue"
//				.equals( sc.getInitParameter( "contextParam" ) ) )
//			throw new IllegalStateException(
//					"ServletContext is not initialized" );
	}

	@Override
	public Set<Class<?>> getClasses()
	{
		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add( JsonService.class );
		return classes;
	}
}
