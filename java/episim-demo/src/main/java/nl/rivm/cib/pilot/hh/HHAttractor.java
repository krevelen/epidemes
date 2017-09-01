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
package nl.rivm.cib.pilot.hh;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.exception.Thrower;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import nl.rivm.cib.episim.model.JsonSchedulable;
import nl.rivm.cib.pilot.json.HesitancyProfileJson;
import nl.rivm.cib.pilot.json.HesitancyProfileJson.Category;

/**
 * {@link HHAttractor} adds special proactive entities acting as special
 * households, representing the nationally or locally communicated (dynamic)
 * positions of e.g. public health, religious, alternative medicinal
 * authorities, or socially observed disease or adverse events, and determining
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface HHAttractor extends JsonSchedulable<HHAttractor>
{
	String TYPE_KEY = "type";

	String RELIGIOUS_KEY = "religious";

	boolean RELIGIOUS_DEFAULT = false;

	String ALTERNATIVE_KEY = "alternative";

	boolean ALTERNATIVE_DEFAULT = false;

	String SCHEDULE_KEY = "schedule";

//	String SERIES_SEP = Pattern.quote( ";" );

	/**
	 * @return an {@link Observable} stream of {@link HHAttribute} values
	 *         {@link Map mapped} as {@link BigDecimal}
	 */
	Observable<Map<HHAttribute, BigDecimal>> adjustments();

	HesitancyProfileJson.Category toHesitancyProfile();

	/**
	 * {@link Broker} assigns opinion attractors to e.g. households
	 */
	@FunctionalInterface
	public interface Broker
	{
		int next( long hhIndex );
	}

	/**
	 * {@link SignalSchedule} executes simple position updates configured as
	 * {@link SignalSchedule.SeriesTiming} entries
	 */
	class SignalSchedule implements HHAttractor
	{
		@InjectConfig
		private JsonNode config;

		@Inject
		private Scheduler scheduler;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Observable<Map<HHAttribute, BigDecimal>> adjustments()
		{
			if( this.config == null ) return Observable.empty();

			final Map<HHAttribute, BigDecimal> initial = Arrays
					.stream( HHAttribute.values() )
					.filter( attr -> this.config.has( attr.stringify() ) )
					.collect(
							Collectors.toMap( attr -> attr, attr -> this.config
									.get( attr.stringify() ).decimalValue() ) );

			return Observable.create( sub ->
			{
				sub.onNext( initial );
				if( this.config.has( SCHEDULE_KEY ) )
					iterate( this.config.get( SCHEDULE_KEY ), HHAttribute.class,
							BigDecimal.class ).subscribe( sub::onNext,
									sub::onError );
			} );
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + config();
		}

		@Override
		public Category toHesitancyProfile()
		{
			return new HesitancyProfileJson.Category(
					fromConfig( RELIGIOUS_KEY, RELIGIOUS_DEFAULT ),
					fromConfig( ALTERNATIVE_KEY, ALTERNATIVE_DEFAULT ) );
		}

		@Override
		public JsonNode config()
		{
			return this.config;
		}
	}

	interface Factory
	{
		HHAttractor create( JsonNode config ) throws Exception;

		default NavigableMap<String, HHAttractor>
			createAll( final JsonNode config )
		{
			// array: generate default numbered name
			if( config.isArray() ) return IntStream.range( 0, config.size() )
					.mapToObj( i -> i ).collect( Collectors.toMap(
							i -> String.format( "attractor%02d", i ), i ->
							{
								try
								{
									return create( config.get( i ) );
								} catch( final Exception e )
								{
									return Thrower.rethrowUnchecked( e );
								}
							}, ( k1, k2 ) -> k1, TreeMap::new ) );

			// object: use field names to identify behavior
			if( config.isObject() )
			{
				final NavigableMap<String, HHAttractor> result = new TreeMap<>();
				config.fields().forEachRemaining( prop ->
				{
					try
					{
						result.put( prop.getKey(), create( prop.getValue() ) );
					} catch( final Exception e )
					{
						Thrower.rethrowUnchecked( e );
					}
				} );
				return result;
			}
			// unexpected
			return Thrower.throwNew( IllegalArgumentException::new,
					() -> "Invalid attractor config: " + config );
		}

		@Singleton
		class SimpleBinding implements Factory
		{
			@Inject
			private LocalBinder binder;

			@Override
			public HHAttractor create( final JsonNode config )
				throws ClassNotFoundException, ParseException
			{
				final Class<? extends HHAttractor> type = config.has( TYPE_KEY )
						? Class.forName( config.get( TYPE_KEY ).textValue() )
								.asSubclass( HHAttractor.class )
						: HHAttractor.SignalSchedule.class;
				return this.binder.inject( type, config );
			}
		}
	}
}
