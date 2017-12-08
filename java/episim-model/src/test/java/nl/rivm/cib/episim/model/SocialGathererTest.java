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
package nl.rivm.cib.episim.model;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Scheduler;
import io.coala.util.MapBuilder;
import io.reactivex.disposables.Disposable;

/**
 * {@link SocialGathererTest} tests {@link SocialGatherer}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class SocialGathererTest
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SocialGathererTest.class );

	@Test
	public void testGatherer() throws TimeoutException
	{
		final LocalConfig config = new LocalConfig.JsonBuilder().withId( "soc" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class ).build();
		LOG.info( "start {} with binder config: {}", getClass().getSimpleName(),
				config );

		final LocalBinder binder = config
				.createBinder(
						MapBuilder.<Class<?>, Object>unordered()
								.put( ProbabilityDistribution.Parser.class,
										new DistributionParser( null ) )
								.build() );
		final Scheduler scheduler = binder.inject( Scheduler.class );

		scheduler.onReset( s ->
		{
			final SocialGatherer.Factory<SocialGatherer> gatherers = binder
					.inject( SocialGatherer.Factory.SimpleBinding.class );
			final SocialGatherer gatherer = gatherers.create( "gath",
					JsonUtil.getJOM().createObjectNode()//
							.put( SocialGatherer.TIMING_KEY,
									"59 29 9 ? * MON *" )
							.put( SocialGatherer.DURATION_KEY, "const(6 h)" )
			//
			);
			LOG.trace( "t={}, init gatherer: {}", s.now(), gatherer );

			// have pathogen subscribe to an individual's infection pressure
			final Disposable dis = gatherer.summon()
					.subscribe( t -> LOG.trace( "t={}, dt={}, {} gathering...",
							s.now( DateTimeFormatter.ISO_OFFSET_DATE_TIME ), t,
							gatherer ), s::fail );

			if( !dis.isDisposed() ) LOG.trace( "t={}, initialized", s.now() );
		} );

		scheduler.atEnd( t -> LOG.trace( "t={}, end reached", t ) );
		scheduler.run();

		LOG.info( "completed {}", getClass().getSimpleName() );

	}

}
