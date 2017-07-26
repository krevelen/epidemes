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
package nl.rivm.cib.pilot.hh;

import java.text.ParseException;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
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
import nl.rivm.cib.episim.pilot.EcosystemScenario.Contagium;
import nl.rivm.cib.util.JsonSchedulable;

/**
 * {@link HHMemberMotor} or "Meeter" used to convene and adjourn members e.g. in
 * transmission spaces
 * 
 * @version $Id: 6a6ef9b07ee21a4e4aee7b41427000aff8573c57 $
 * @author Rick van Krevelen
 */
public interface HHMemberMotor extends JsonSchedulable<HHMemberMotor>
{
	String TYPE_KEY = "type";

	String CONVENE_KEY = "convene-timing";

	String DURATION_KEY = "duration-dist";

	/**
	 * @return an {@link Observable} stream of {@link Duration}s when and for
	 *         how long people driven by this motor convene
	 */
	Observable<Quantity<Time>> convene();

	/**
	 * {@link Contagium}
	 */
	class SimpleGatherer implements HHMemberMotor
	{

		private final ProbabilityDistribution.Parser distParser;

		private final Scheduler scheduler;

		private final Subject<Quantity<Time>> convene = PublishSubject.create();

		private JsonNode config;

		/** the nextConvene {@link Expectation} to cancel on {@link #reset} */
		private Expectation nextConvene = null;

		@Inject
		protected SimpleGatherer(
			final ProbabilityDistribution.Parser distParser,
			final Scheduler scheduler )
		{
			this.distParser = distParser;
			this.scheduler = scheduler;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

//		@SuppressWarnings( "unchecked" )
		@Override
		public HHMemberMotor reset( final JsonNode config )
			throws ParseException
		{
//			this.occupancySince = now();
//			this.occupancy.set( 0L );
			this.config = config;
			if( this.nextConvene != null ) this.nextConvene.remove();
			final String timing = Objects.requireNonNull(
					config.get( CONVENE_KEY ).asText(), "no timing?" );
			final String durationDist = Objects.requireNonNull(
					config.get( DURATION_KEY ).asText(), "no duration?" );
			final QuantityDistribution<Time> dist = this.distParser
					.parseQuantity( durationDist ).asType( Time.class );
			final Iterable<Instant> T = Timing.valueOf( timing )
					.offset( now().toJava8( scheduler().offset() ) ).iterate();
			atEach( T, t -> this.convene.onNext( dist.draw() ) ).subscribe(
					exp -> this.nextConvene = exp, Throwable::printStackTrace );
			return this;
		}

		@Override
		public String toString()
		{
			return stringify();
		}

		@Override
		public Observable<Quantity<Time>> convene()
		{
			return this.convene;
		}

		@Override
		public JsonNode config()
		{
			return this.config;
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
					.mapToObj( i -> i ).collect( Collectors
							.toMap( i -> String.format( "motor%02d", i ), i ->
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
					() -> "Invalid motor config: " + config );
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
								: HHMemberMotor.SimpleGatherer.class;
				return this.binder.inject( type ).reset( config );
			}
		}
	}
}
