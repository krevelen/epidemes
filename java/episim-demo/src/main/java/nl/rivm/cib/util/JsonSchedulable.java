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
package nl.rivm.cib.util;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import io.coala.json.JsonUtil;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * {@link JsonSchedulable}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface JsonSchedulable<THIS extends JsonSchedulable<?>>
	extends Proactive, JsonConfigurable<THIS>
{

	String SCHEDULE_KEY = "schedule";

	String SERIES_SEP = Pattern.quote( ";" );

	/** {@link SignalYaml} specifies position update rule configurations */
	class SignalYaml
	{
		@JsonProperty( "occurrence" )
		public String occurrence;

		@JsonProperty( "period" )
		public Period period;

		/** an integer-to-value mapping due to array flattening in YamlUtil */
		public Map<String, SortedMap<Integer, JsonNode>> series;
	}

	default <K, V> Observable<Map<K, V>> iterate( final JsonNode node,
		final Class<K> keyType, final Class<V> valueType )
	{
		if( node.isArray() ) return Observable.fromIterable( node )
				.flatMap( schedule -> iterate( schedule, keyType, valueType ) );

		return Observable.create( sub ->
		{
			try
			{
				final SignalYaml item = JsonUtil.valueOf( node,
						SignalYaml.class );
				final Iterable<Instant> timing = Timing.of( item.occurrence )
						.offset( now().toJava8( scheduler().offset() ) )
						.iterate();
				final Map<K, List<V>> series = item.series.entrySet()
						.parallelStream()
						.collect( Collectors.toMap(
								e -> JsonUtil.valueOf( '"' + e.getKey() + '"',
										keyType ),
								e -> e.getValue().values().stream().map(
										v -> JsonUtil.valueOf( v,
												valueType ) )
										.collect( Collectors.toList() ) ) );
				atEach( timing,
						t -> scheduleSeries( sub, item.period, series, 0 ) )
								.subscribe( exp ->
								{
								}, sub::onError );
			} catch( final Exception e )
			{
				sub.onError( e );
			}
		} );

	}

	/** this method repeatedly schedules itself until the series are complete */
	default <K, V> void scheduleSeries( final ObservableEmitter<Map<K, V>> sub,
		final Period period, final Map<K, List<V>> series, final int index )
	{
		if( series.isEmpty() || sub.isDisposed() ) return;
		final Map<K, V> values = series.entrySet().parallelStream()
				.filter( e -> index < e.getValue().size() )
				.collect( Collectors.toMap( Entry::getKey,
						e -> e.getValue().get( index ) ) );

		// continue this series incidence for next index only if non-empty
		if( !values.isEmpty() )
		{
			sub.onNext( values );
			final ZonedDateTime now = now().toJava8( scheduler().offset() );
			after( Duration.between( now, now.plus( period ) ).toMillis(),
					TimeUnits.MILLIS )
							.call( t -> scheduleSeries( sub, period, series,
									index + 1 ) );
		}
	}
}
