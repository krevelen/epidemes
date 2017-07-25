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
package nl.rivm.cib.pilot;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.LocalBinder;
import io.coala.exception.Thrower;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Tuple;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Expectation;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import nl.rivm.cib.episim.model.disease.ClinicalPhase;
import nl.rivm.cib.episim.model.disease.infection.EpidemicCompartment;
import nl.rivm.cib.util.JsonConfigurable;

/**
 * {@link Pathogen}
 * <ul>
 * <li>&Gamma;: growth/birth rate (&empty;&rarr;N or &empty;&rarr;M or
 * &empty;&rarr;S);
 * <li>P: newborn vaccination rate (N&rarr;V vs. N&rarr;S)
 * <li>&rho;: non-newborn vaccination rate (S&rarr;V)
 * <li>&delta;: maternal immunity waning rate (M&rarr;S)
 * <li>&beta;: contact/infection rate (S&rarr;I or S&rarr;E) of new/secondary
 * infections;
 * <li>a: incubation rate (E&rarr;I);
 * <li>&gamma;: recovery rate (I&rarr;R)
 * <li>&mu;: mortality/death rate (R&rarr;&empty;);
 * </ul>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Pathogen extends Proactive, JsonConfigurable<Pathogen>
{
	String TYPE_KEY = "type";

	String TYPE_DEFAULT = MSEIRS.class.getName();

	String PERIOD_DEFAULT = "const( 1 week )";

	<T> Observable<Condition> trajectory( Observable<T> pressure,
		Resistor<T> resistor );

	default Observable<Condition>
		binaryTrajectory( Observable<? extends Number> pressure )
	{
		return trajectory( pressure.map( DecimalUtil::valueOf ),
				( dt, p ) -> p.signum() < 1 ? dt.subtract( dt ) : dt );
	}

	default Observable<Condition>
		linearTrajectory( Observable<? extends Number> pressure )
	{
		return trajectory( pressure.map( DecimalUtil::valueOf ),
				Quantity::multiply );
	}

	@FunctionalInterface
	interface Resistor<T>
	{
		Quantity<Time> apply( Quantity<Time> dt, T t );
	}

	@SuppressWarnings( "rawtypes" )
	interface Condition extends Comparable
	{
//		MedicalStage getStage();

		ClinicalPhase getSymptoms();

//		Serostatus getSerostatus();

		EpidemicCompartment getCompartment();

		class Simple extends Tuple implements Condition
		{
			public Simple( final EpidemicCompartment.Simple epi,
				final ClinicalPhase.Simple symptoms )
			{
				super( Arrays.asList( epi, symptoms ) );
			}

			@Override
			public EpidemicCompartment.Simple getCompartment()
			{
				return (EpidemicCompartment.Simple) values().get( 0 );
			}

			@Override
			public ClinicalPhase getSymptoms()
			{
				return (ClinicalPhase) values().get( 1 );
			}
		}
	}

	class MSEIRS implements Pathogen
	{
		private final Map<String, QuantityDistribution<Time>> distCache = new HashMap<>();

		@Inject
		private Scheduler scheduler;

		@Inject
		private ProbabilityDistribution.Parser distParser;

		private JsonNode config;

		@Override
		public String toString()
		{
			return stringify();
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		private Quantity<Time> dtMapper( final EpiPeriod configKey )
		{
			return this.distCache.computeIfAbsent(
					fromConfig( configKey.jsonKey(), PERIOD_DEFAULT ), dist ->
					{
						try
						{
							return this.distParser.parseQuantity( dist,
									Time.class );
						} catch( final ParseException e )
						{
							return Thrower.rethrowUnchecked( e );
						}
					} ).draw();
		}

		@Override
		public Pathogen reset( final JsonNode config ) throws ParseException
		{
			this.config = config;
			return this;
		}

		@Override
		public JsonNode config()
		{
			return this.config;
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public <T> Observable<Condition> trajectory(
			final Observable<T> pressures, final Resistor<T> resistor )
		{
			return Observable.create( sub ->
			{
//				final Independent s = new Independent( scheduler(),
//						this::dtMapper, sub );
				final Pressurized<T> s = new Pressurized<T>( scheduler(),
						this::dtMapper, resistor, sub );
				pressures.subscribe( s );
				s.passive(); // initiate dynamics
			} );
		}

		public enum EpiPeriod
		{
			PASSIVE,
			/**
			 * invasion period (S->E or S->I), related to contact rate of
			 * new/secondary infections, <em>&beta;</em>
			 */
			RESISTANCE,

			/**
			 * exposed/latent period (E->I), related to incubation rate
			 * <em>a</em>
			 */
			LATENT,

			/**
			 * infectious/recovery period (I->R) <em>D</em>, related to
			 * transition rate <em>v</em>=1/<em>D</em>
			 */
			RECOVER,

			/**
			 * wane (temporary recovery) period (R->S) TODO conditional on e.g.
			 * co-morbidity, genetic factors, ...
			 */
			WANE,

			/** incubation period (E->C, sub->clinical symptoms) */
			INCUBATE,

			/** symptom period (C->R, normal/asymptomatic) */
			CLINICAL,
			//
			;

			private String jsonKey = null;

			@JsonValue
			public String jsonKey()
			{
				return this.jsonKey != null ? this.jsonKey
						: (this.jsonKey = name().toLowerCase() + "-period");
			}
		}

		/**
		 * {@link Pressurized} maintains an individual's remaining infection
		 * resistance
		 */
		public static class Independent implements Proactive
		{
			protected static final Condition PASSIVE = new Condition.Simple(
					EpidemicCompartment.Simple.PASSIVE_IMMUNE,
					ClinicalPhase.Simple.ASYMPTOMATIC );
			protected static final Condition SUSCEPTIBLE = new Condition.Simple(
					EpidemicCompartment.Simple.SUSCEPTIBLE,
					ClinicalPhase.Simple.ASYMPTOMATIC );
			protected static final Condition EXPOSED_SUBCLINICAL = new Condition.Simple(
					EpidemicCompartment.Simple.EXPOSED,
					ClinicalPhase.Simple.ASYMPTOMATIC );
			protected static final Condition EXPOSED_CLINICAL = new Condition.Simple(
					EpidemicCompartment.Simple.EXPOSED,
					ClinicalPhase.Simple.SYSTEMIC );
			protected static final Condition INFECTIVE_SUBCLINICAL = new Condition.Simple(
					EpidemicCompartment.Simple.INFECTIVE,
					ClinicalPhase.Simple.ASYMPTOMATIC );
			protected static final Condition INFECTIVE_CLINICAL = new Condition.Simple(
					EpidemicCompartment.Simple.INFECTIVE,
					ClinicalPhase.Simple.SYSTEMIC );
			protected static final Condition RECOVERED = new Condition.Simple(
					EpidemicCompartment.Simple.RECOVERED,
					ClinicalPhase.Simple.ASYMPTOMATIC );

			protected final AtomicReference<Condition> condition = new AtomicReference<>();
			protected final Function<EpiPeriod, Quantity<Time>> progressor;
			protected final ObservableEmitter<Condition> emitter;
			private final Scheduler scheduler;

			public Independent( final Scheduler scheduler,
				final Function<EpiPeriod, Quantity<Time>> progressor,
				final ObservableEmitter<Condition> emitter )
			{
				this.scheduler = scheduler;
				this.progressor = progressor;
				this.emitter = emitter;
			}

			@Override
			public Scheduler scheduler()
			{
				return this.scheduler;
			}

			/**
			 * sets condition {@link #PASSIVE} and schedules {@link #wane} after
			 * {@link EpiPeriod#PASSIVE}
			 */
			protected void passive()
			{
				this.emitter.onNext( PASSIVE );
				after( this.progressor.apply( EpiPeriod.PASSIVE ) )
						.call( this::wane );
			}

			/**
			 * sets condition {@link #SUSCEPTIBLE} and schedules {@link #expose}
			 * after {@link EpiPeriod#RESISTANCE}
			 */
			protected void wane()
			{
				this.emitter.onNext(
						this.condition.updateAndGet( x -> SUSCEPTIBLE ) );
				after( this.progressor.apply( EpiPeriod.RESISTANCE ) )
						.call( this::expose );
			}

			/**
			 * sets condition {@link #EXPOSED_SUBCLINICAL} and schedules both
			 * {@link #shed} after {@link EpiPeriod#LATENT} and
			 * {@link #clinical} after {@link EpiPeriod#INCUBATE}
			 */
			protected void expose()
			{
				this.emitter.onNext( this.condition
						.updateAndGet( c -> EXPOSED_SUBCLINICAL ) );
				after( this.progressor.apply( EpiPeriod.LATENT ) )
						.call( this::shed );
				after( this.progressor.apply( EpiPeriod.INCUBATE ) )
						.call( this::clinical );
			}

			/**
			 * sets condition {@link #INFECTIVE_SUBCLINICAL} or
			 * {@link #INFECTIVE_CLINICAL} and schedules {@link #recover} after
			 * {@link EpiPeriod#RECOVER}
			 */
			protected void shed()
			{
				this.emitter.onNext( this.condition.updateAndGet(
						c -> c.getSymptoms().isClinical() ? INFECTIVE_CLINICAL
								: INFECTIVE_SUBCLINICAL ) );
				after( this.progressor.apply( EpiPeriod.RECOVER ) )
						.call( this::recover );
			}

			/**
			 * sets condition {@link #INFECTIVE_CLINICAL} or
			 * {@link #EXPOSED_CLINICAL} and schedules {@link #subclinical}
			 * after {@link EpiPeriod#CLINICAL}
			 */
			protected void clinical()
			{
				this.emitter.onNext( this.condition
						.updateAndGet( c -> c.getCompartment().isInfective()
								? INFECTIVE_CLINICAL : EXPOSED_CLINICAL ) );
				after( this.progressor.apply( EpiPeriod.CLINICAL ) )
						.call( this::subclinical );
			}

			/**
			 * sets condition {@link #INFECTIVE_SUBCLINICAL} or
			 * {@link #EXPOSED_SUBCLINICAL}
			 */
			protected void subclinical()
			{
				this.emitter.onNext( this.condition
						.updateAndGet( c -> c.getCompartment().isInfective()
								? INFECTIVE_SUBCLINICAL
								: EXPOSED_SUBCLINICAL ) );
			}

			/**
			 * sets condition {@link #EXPOSED_SUBCLINICAL} and schedules
			 * {@link #wane} after {@link EpiPeriod#WANE}
			 */
			protected void recover()
			{
				this.emitter.onNext(
						this.condition.updateAndGet( c -> RECOVERED ) );
				after( this.progressor.apply( EpiPeriod.WANE ) )
						.call( this::wane );
			}
		}

		/**
		 * {@link Pressurized} maintains an individual's remaining infection
		 * resistance
		 */
		public static class Pressurized<T> extends Independent
			implements Observer<T>
		{
			private final AtomicReference<T> pressure = new AtomicReference<>();
			private final AtomicReference<Expectation> expose = new AtomicReference<>();
			private final AtomicReference<Quantity<Time>> resistance = new AtomicReference<>();
			private final AtomicReference<Quantity<Time>> since = new AtomicReference<>();

			private final Resistor<T> resistor;

			public Pressurized( final Scheduler scheduler,
				final Function<EpiPeriod, Quantity<Time>> progressor,
				final Resistor<T> resistor,
				final ObservableEmitter<Condition> emitter )
			{
				super( scheduler, progressor, emitter );
				this.resistor = resistor;
			}

			@Override
			public void onSubscribe( final Disposable d )
			{
				// never cancel/dispose observers
			}

			@Override
			public void onNext( final T pNew )
			{
				final T pOld = this.pressure.getAndSet( pNew );

				// no pressure or unchanged
				if( pOld == null || pOld.equals( pNew ) ) return;

				if( this.condition.get() == null || !this.condition.get()
						.getCompartment().isSusceptible() )
					return; // not susceptible: no exposure occurring

				final Quantity<Time> t = now().toQuantity( Time.class ),
						t0 = this.since.getAndSet( t );

				// update remaining resistance, subtracting dp for dt > 0
				if( t0 != null && !t0.equals( t ) )
				{
					final Quantity<Time> dt = t.subtract( t0 ),
							dp = this.resistor.apply( dt, pOld ),
							res0 = this.resistance
									.getAndUpdate( res -> res.subtract( dp ) );
					System.err.println( "t=" + t + " dt=" + dt + " dp=" + dp
							+ " res0=" + res0 + " res_t=" + this.resistance );
				}

				// reschedule exposure
				wane();
			}

			@Override
			public void onError( final Throwable e )
			{
				this.emitter.onError( e );
			}

			@Override
			public void onComplete()
			{
				this.emitter.onComplete();
			}

			/**
			 * sets/maintains condition {@link #SUSCEPTIBLE} and (re)schedules
			 * {@link #expose} after (pressured) {@link EpiPeriod#RESISTANCE}
			 */
			@Override
			protected void wane()
			{
				final Condition c = this.condition.get();
				if( c == null || !c.getCompartment().isSusceptible() )
					this.emitter.onNext(
							this.condition.updateAndGet( x -> SUSCEPTIBLE ) );

				final Quantity<Time> resistance = this.resistance
						.updateAndGet( dt -> dt != null ? dt
								: this.progressor
										.apply( EpiPeriod.RESISTANCE ) );

				if( QuantityUtil.signum( resistance ) < 1 )
				{
					System.err.println( "expose immediately: " + resistance );
					expose();
					return;
				}
				System.err.println( "scheduling expose after: " + resistance );
				// (re)schedule exposure
				final Expectation exp1 = after( resistance )
						.call( this::expose ),
						exp = this.expose.getAndSet( exp1 );

				// cancel previous, if any
				if( exp != null ) exp.remove();
			}

			@Override
			protected void expose()
			{
				this.expose.set( null );
				this.resistance.set( null );
				this.since.set( null );
				super.expose();
			}
		}
	}

	interface Factory
	{
		Pathogen create( JsonNode config ) throws Exception;

		default NavigableMap<String, Pathogen>
			createAll( final JsonNode config )
		{
			// array: generate default numbered name
			if( config.isArray() ) return IntStream.range( 0, config.size() )
					.mapToObj( i -> i ).collect( Collectors.toMap(
							i -> String.format( "pathogen%02d", i ), i ->
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
				final NavigableMap<String, Pathogen> result = new TreeMap<>();
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
					() -> "Invalid pathogen config: " + config );
		}

		@Singleton
		class SimpleBinding implements Factory
		{
			@Inject
			private LocalBinder binder;

			@Override
			public Pathogen create( final JsonNode config )
				throws ClassNotFoundException, ParseException
			{
				final JsonNode typeNode = Objects
						.requireNonNull( config, "No config?" ).get( TYPE_KEY );
				final String typeName = typeNode == null || typeNode.isNull()
						? TYPE_DEFAULT : typeNode.asText( TYPE_DEFAULT );
				final Class<? extends Pathogen> type = Class.forName( typeName )
						.asSubclass( Pathogen.class );
				return this.binder.inject( type ).reset( config );
			}
		}
	}
}
