/* $Id: 6a6ef9b07ee21a4e4aee7b41427000aff8573c57 $
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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.NavigableMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.name.Identified;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import tec.uom.se.ComparableQuantity;

/**
 * {@link SocialGatherer} used to convene and adjourn members e.g. in
 * transmission spaces
 * 
 * @version $Id: 6a6ef9b07ee21a4e4aee7b41427000aff8573c57 $
 * @author Rick van Krevelen
 */
public interface SocialGatherer
	extends Identified<String>, JsonSchedulable<SocialGatherer>
{
	String TIMING_KEY = "convene-timing";

	String DURATION_KEY = "duration-dist";

	String SIZE_KEY = "capacity-dist";

	String AGES_KEY = "age-filter";

	String ASSORTATIVE_KEY = "assortative";

	boolean isAssortative();

	Range<ComparableQuantity<?>> memberAges();

	ProbabilityDistribution<Long> sizeLimitDist();

	/**
	 * Publishes when and for how long people driven by this motor convene, with
	 * errors diverted to {@link Scheduler#time}
	 * 
	 * @return an {@link Observable} stream of {@link Duration}s
	 */
	Observable<Quantity<Time>> summon();

	class SimpleGatherer extends Identified.SimpleOrdinal<String>
		implements SocialGatherer
	{

		/** */
		private static final Logger LOG = LogUtil
				.getLogger( SocialGatherer.SimpleGatherer.class );

		@Inject
		private ProbabilityDistribution.Parser distParser;

		@Inject
		private Scheduler scheduler;

		@InjectConfig
		private JsonNode config;

		private ProbabilityDistribution<Long> sizeDist;

		private Range<ComparableQuantity<?>> ageRange;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public JsonNode config()
		{
			return this.config;
		}

		@Override
		public String toString()
		{
			return stringify();
		}

		@Override
		public String id()
		{
			return fromConfig( ID_JSON_PROPERTY, "[NOID]" );
		}

		@Override
		public ProbabilityDistribution<Long> sizeLimitDist()
		{
			if( this.sizeDist == null )
			{
				final String sizeDist = fromConfig( SIZE_KEY,
						"uniform-discrete(50;200)" );
				try
				{
					this.sizeDist = this.distParser
							.parse( sizeDist, BigDecimal.class )
							.map( v -> ((Number) v).longValue() );
				} catch( final ParseException e )
				{
					scheduler().fail( e );
					this.sizeDist = ProbabilityDistribution
							.createDeterministic( 100L );
				}
			}
			return this.sizeDist;
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public Range<ComparableQuantity<?>> memberAges()
		{
			if( this.ageRange == null )
			{
				final String ages = fromConfig( AGES_KEY, "[0 yr;100 yr]" );
				try
				{
					this.ageRange = Range.parse( ages, QuantityUtil::valueOf )
							.map( q -> q.asType( Time.class ) );
				} catch( final ParseException e )
				{
					LOG.error( "Problem parsing {}: {}", ages, e.getMessage() );
					this.ageRange = Range.infinite();
				}
			}
			return this.ageRange;
		}

		@Override
		public boolean isAssortative()
		{
			return fromConfig( ASSORTATIVE_KEY, false );
		}

		private Subject<Instant> summonings = null;

		private QuantityDistribution<Time> dist = null;

		@Override
		public Observable<Quantity<Time>> summon()
		{
			if( this.summonings == null ) try
			{
				this.dist = this.distParser.parseQuantity(
						fromConfigNonEmpty( DURATION_KEY ), Time.class );
				this.summonings = PublishSubject.create();
				final String cron = fromConfigNonEmpty( TIMING_KEY );
				final Iterable<Instant> timing = Timing.valueOf( cron )
						.iterate( scheduler() );
				atEach( timing, this.summonings::onNext );
			} catch( final Exception e )
			{
				return Observable.error( e );
			}
			return this.summonings.map( t -> this.dist.draw() );
		}
	}

	interface Factory<T>
	{
		String TYPE_KEY = "type";

		T create( String name, ObjectNode config ) throws Exception;

		default T createOrFail( String name, JsonNode config )
		{
			try
			{
				return create( name, (ObjectNode) config );
			} catch( final Throwable e )
			{
				return Thrower.rethrowUnchecked( e );
			}
		}

		default NavigableMap<String, T> createAll( final JsonNode config )
			throws Exception
		{
			// array: generate default numbered name
			if( config.isArray() ) return JsonUtil.toMap( (ArrayNode) config,
					i -> String.format( "gatherer%02d", i ),
					this::createOrFail );

			// object: use field names to identify
			if( config.isObject() ) return JsonUtil.toMap( (ObjectNode) config,
					this::createOrFail );

			// unexpected
			return Thrower.throwNew( IllegalArgumentException::new,
					() -> "Invalid config: " + config );
		}

		@Singleton
		class SimpleBinding implements Factory<SocialGatherer>
		{
			@Inject
			private LocalBinder binder;

			@Override
			public SocialGatherer create( final String name,
				final ObjectNode config )
				throws ClassNotFoundException, ParseException
			{
				final Class<? extends SocialGatherer> type = config
						.has( TYPE_KEY )
								? Class
										.forName( config.get( TYPE_KEY )
												.textValue() )
										.asSubclass( SocialGatherer.class )
								: SimpleGatherer.class;
				return this.binder.inject( type,
						config.put( Identified.ID_JSON_PROPERTY, name ) );
			}
		}
	}
}
