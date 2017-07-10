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

import io.coala.bind.LocalBinder;
import io.coala.exception.Thrower;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import nl.rivm.cib.util.HHScenarioConfigurable;

/**
 * {@link HHMemberBehavior} adds special proactive entities acting as special
 * households, representing the nationally or locally communicated (dynamic)
 * positions of e.g. public health, religious, alternative medicinal
 * authorities, or socially observed disease or adverse events, and determining
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface HHMemberBehavior
	extends HHScenarioConfigurable<HHMemberBehavior>
{
	String TYPE_KEY = "type";

	/**
	 * @return an {@link Observable} stream of {@link HHAttribute} values
	 *         {@link Map mapped} as {@link BigDecimal}
	 */
	Observable<Map<HHMemberAttribute, BigDecimal>> adjustments();

	/**
	 * {@link SignalSchedule} executes simple position updates configured as
	 * {@link SignalSchedule.SignalYaml} entries
	 */
	class SignalSchedule implements HHMemberBehavior
	{

		private JsonNode config;

		@Inject
		private Scheduler scheduler;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Observable<Map<HHMemberAttribute, BigDecimal>> adjustments()
		{
			if( this.config == null ) return Observable.empty();

			final Map<HHMemberAttribute, BigDecimal> initial = Arrays
					.stream( HHMemberAttribute.values() )
					.filter( attr -> this.config.has( attr.jsonValue() ) )
					.collect(
							Collectors.toMap( attr -> attr, attr -> this.config
									.get( attr.jsonValue() ).decimalValue() ) );

			return Observable.create( sub ->
			{
				sub.onNext( initial );
				if( this.config.has( SCHEDULE_KEY ) )
					iterate( this.config.get( SCHEDULE_KEY ),
							HHMemberAttribute.class, BigDecimal.class )
									.subscribe( sub::onNext, sub::onError );
			} );
		}

		@Override
		public HHMemberBehavior reset( final JsonNode config )
			throws ParseException
		{
			this.config = config;
			return this;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + this.config;
		}
	}

	interface Factory
	{
		HHMemberBehavior create( JsonNode config ) throws Exception;

		default NavigableMap<String, HHMemberBehavior>
			createAll( final JsonNode config )
		{
			// array: generate default numbered name
			if( config.isArray() ) return IntStream.range( 0, config.size() )
					.mapToObj( i -> i ).collect( Collectors.toMap(
							i -> String.format( "behavior%02d", i ), i ->
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
				final NavigableMap<String, HHMemberBehavior> result = new TreeMap<>();
				config.fields().forEachRemaining( e ->
				{
					try
					{
						result.put( e.getKey(), create( e.getValue() ) );
					} catch( final Exception e1 )
					{
						Thrower.rethrowUnchecked( e1 );
					}
				} );
				return result;
			}
			// unexpected
			return Thrower.throwNew( IllegalArgumentException::new,
					() -> "Invalid behavior config: " + config );
		}

		@Singleton
		class SimpleBinding implements Factory
		{
			@Inject
			private LocalBinder binder;

			@Override
			public HHMemberBehavior create( final JsonNode config )
				throws ClassNotFoundException, ParseException
			{
				final Class<? extends HHMemberBehavior> type = config
						.has( TYPE_KEY )
								? Class
										.forName( config.get( TYPE_KEY )
												.textValue() )
										.asSubclass( HHMemberBehavior.class )
								: HHMemberBehavior.SignalSchedule.class;
				return this.binder.inject( type ).reset( config );
			}
		}
	}
}
