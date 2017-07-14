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

import java.text.ParseException;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.quantity.Time;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.LocalBinder;
import io.coala.exception.Thrower;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Duration;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.util.HHScenarioConfigurable;

/**
 * {@link HHMemberMotor} or "Meeter" used to convene and adjourn members e.g. in
 * transmission spaces
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface HHMemberMotor extends HHScenarioConfigurable<HHMemberMotor>
{
	String TYPE_KEY = "type";

	String CONVENE_KEY = "convene-timing";

	String DURATION_KEY = "duration-dist";

	/**
	 * @return an {@link Observable} stream of {@link HHAttribute} values
	 *         {@link Map mapped} as {@link BigDecimal}
	 */
//	Observable<Map<HHMemberAttribute, BigDecimal>> adjustments();

	/**
	 * @return an {@link Observable} stream of {@link Duration}s when and for
	 *         how long people driven by this motor convene
	 */
	Observable<Duration> convene();

	Duration occupancyChange( long delta );

	/**
	 * {@link Broker} assigns mobility/behavior brokers to persons
	 */
	@FunctionalInterface
	public interface Broker
	{
		int next( long ppIndex );
	}

	/**
	 * {@link SignalSchedule} executes simple position updates configured as
	 * {@link SignalSchedule.SignalYaml} entries
	 */
	class SignalSchedule implements HHMemberMotor
	{

		private JsonNode config;

		@Inject
		private ProbabilityDistribution.Parser distParser;

		@Inject
		private Scheduler scheduler;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		private final AtomicLong occupancy = new AtomicLong();

		private Instant occupancySince = null;

		private final Subject<Duration> convene = PublishSubject.create();

		private Expectation nextConvene = null;

		@SuppressWarnings( "unchecked" )
		@Override
		public Duration occupancyChange( final long delta )
		{
			final long n = this.occupancy.getAndAdd( delta );
			final Instant t = now();
			final Duration dt = Duration.of( this.occupancySince.unwrap()
					.subtract( now().unwrap() ).multiply( n ) );
			this.occupancySince = t;
			if( n > delta ) return Thrower.throwNew( IllegalStateException::new,
					() -> "Negative occupancy? (" + delta + " > " + n + ")" );
			return dt;
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public HHMemberMotor reset( final JsonNode config )
			throws ParseException
		{
			this.occupancySince = now();
			this.occupancy.set( 0L );
			this.config = config;
			if( this.nextConvene != null ) this.nextConvene.remove();
			final String timing = Objects.requireNonNull(
					config.get( CONVENE_KEY ).asText(), "no timing?" );
			final String durationDist = Objects.requireNonNull(
					config.get( DURATION_KEY ).asText(), "no duration?" );
			final QuantityDistribution<Time> dist = this.distParser
					.parseQuantity( durationDist ).asType( Time.class );
			atEach( Timing.valueOf( timing )
					.offset( now().toJava8( scheduler().offset() ) ).iterate(),
					t -> this.convene.onNext( Duration.of( dist.draw() ) ) )
							.subscribe( exp -> this.nextConvene = exp,
									Throwable::printStackTrace );
			return this;
		}

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + this.config;
		}

		@Override
		public Observable<Duration> convene()
		{
			return this.convene;
		}
	}

	interface Factory
	{
		HHMemberMotor create( JsonNode config ) throws Exception;

		default NavigableMap<String, HHMemberMotor>
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
				final NavigableMap<String, HHMemberMotor> result = new TreeMap<>();
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
			public HHMemberMotor create( final JsonNode config )
				throws ClassNotFoundException, ParseException
			{
				final Class<? extends HHMemberMotor> type = config
						.has( TYPE_KEY )
								? Class
										.forName( config.get( TYPE_KEY )
												.textValue() )
										.asSubclass( HHMemberMotor.class )
								: HHMemberMotor.SignalSchedule.class;
				return this.binder.inject( type ).reset( config );
			}
		}
	}
}
