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
package nl.rivm.cib.episim.model.disease;

import java.math.BigDecimal;
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
import io.coala.config.JsonConfigurable;
import io.coala.exception.Thrower;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Tuple;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.ConditionalSignalQuantifier;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import nl.rivm.cib.episim.model.disease.infection.EpidemicCompartment;

/**
 * {@link IllnessTrajectory}
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
public interface IllnessTrajectory
	extends Proactive, JsonConfigurable<IllnessTrajectory>
{
	String TYPE_KEY = "type";

	String TYPE_DEFAULT = MSEIRS.class.getName();

	String PERIOD_DEFAULT = "const( 1 week )";

	/**
	 * @param pressure the stream of pressure signal constants
	 * @param decay defines relations between resistance, pressure and time
	 * @return a trajectory possibly affected by the observed pressure signal
	 *         and resistance decay function
	 */
	Observable<Condition> integrator( Observable<? extends Number> pressure,
		ResistanceDecay decay );

	/** @return a trajectory that disregards any pressure signal constants */
	default Observable<Condition> invariant()
	{
		return integrator( Observable.empty(),
				new ResistanceDecay.ConstantDecay() );
	}

	/**
	 * @param pressure a numeric pressure signal k
	 * @return a trajectory with resistance change dr=-1*dt (k > 0); else dr=NA
	 */
	default Observable<Condition>
		binary( Observable<? extends Number> pressure )
	{
		return integrator( pressure, new ResistanceDecay.BinaryDecay() );
	}

	/**
	 * @param pressure a numeric pressure signal k
	 * @return a trajectory with resistance change dr=-k*dt (k > 0); else dr=NA
	 */
	default Observable<Condition>
		linear( Observable<? extends Number> pressure )
	{
		return integrator( pressure, new ResistanceDecay.LinearDecay() );
	}

	/**
	 * {@link ResistanceDecay} defines relations between resistance, pressure
	 * and time in two functions: {@link #differentiator} for the difference
	 * given a {@link BigDecimal} pressure state over some time period, and
	 * {@link #interceptor} for the period until some resistance level is
	 * reached.
	 */
	interface ResistanceDecay
		extends ConditionalSignalQuantifier<Quantity<Time>, BigDecimal>
	{
		/**
		 * {@link ConstantDecay} resistance: dr=-1*dt, regardless of k
		 */
		class ConstantDecay implements ResistanceDecay
		{
			@Override
			public Differentiater<Quantity<Time>, BigDecimal> differentiater()
			{
				return ( t_0, dt, r_0, k ) -> QuantityUtil.min( dt, r_0 )
						.multiply( -1 );
			}

			@Override
			public Intercepter<Quantity<Time>, BigDecimal> intercepter()
			{
				return ( t_0, r_0, r_t, k ) -> r_t == null ? null
						: r_t.subtract( r_0 ).divide( -1 );
			}
		}

		/**
		 * {@link BinaryDecay} resistance; dr=-1*dt iff k>0, else NA
		 */
		class BinaryDecay implements ResistanceDecay
		{
			@Override
			public Differentiater<Quantity<Time>, BigDecimal> differentiater()
			{
				return ( t_0, dt, r_0, k ) -> k == null || k.signum() < 1 ? null
						: QuantityUtil.min( dt, r_0 ).multiply( -1 );
			}

			@Override
			public Intercepter<Quantity<Time>, BigDecimal> intercepter()
			{
				return ( t_0, r_0, r_t, k ) -> k == null || k.signum() < 1
						? null : r_t.subtract( r_0 ).divide( -1 );
			}
		}

		/**
		 * {@link LinearDecay} resistance: dr=-k*dt iff k>0, else NA
		 */
		class LinearDecay implements ResistanceDecay
		{
			@SuppressWarnings( "unchecked" )
			@Override
			public Differentiater<Quantity<Time>, BigDecimal> differentiater()
			{
				return ( t_0, dt, r_0, k ) -> k == null || k.signum() < 1 ? null
						: QuantityUtil.min( dt, r_0 ).multiply( -1 );
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public Intercepter<Quantity<Time>, BigDecimal> intercepter()
			{
				return ( t_0, r_0, r_t,
					k ) -> k == null || k.signum() < 1 || r_t == null ? null
							: r_t.subtract( r_0 ).divide( k.negate() );
			}
		}
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

	class MSEIRS implements IllnessTrajectory
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
		public IllnessTrajectory reset( final JsonNode config )
			throws ParseException
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
		public Observable<Condition> integrator(
			final Observable<? extends Number> pressure,
			final ResistanceDecay resistance )
		{
			return Observable.create( sub ->
			{
//				final Independent s = new Independent( scheduler(),
//						this::dtMapper, sub );
				final Pressured<BigDecimal> s = new Pressured<BigDecimal>(
						scheduler(), this::dtMapper, resistance, sub );
				pressure.map( DecimalUtil::valueOf )
						.subscribe( s::setPressure );
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
		 * {@link Pressured} maintains an individual's remaining infection
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
		 * {@link Pressured} maintains an individual's remaining infection
		 * resistance
		 */
		public static class Pressured<K> extends Independent
		{
			private final AtomicReference<K> pressure = new AtomicReference<>();
			private final AtomicReference<Expectation> expose = new AtomicReference<>();
			private final AtomicReference<Quantity<Time>> resistance = new AtomicReference<>();
			private final AtomicReference<Instant> since = new AtomicReference<>();

			private final ConditionalSignalQuantifier.Differentiater<Quantity<Time>, K> differentiator;
			private final ConditionalSignalQuantifier.Intercepter<Quantity<Time>, K> interceptor;

			public Pressured( final Scheduler scheduler,
				final Function<EpiPeriod, Quantity<Time>> progressor,
				final ConditionalSignalQuantifier<Quantity<Time>, K> resistance,
				final ObservableEmitter<Condition> emitter )
			{
				super( scheduler, progressor, emitter );
				this.differentiator = resistance.differentiater();
				this.interceptor = resistance.intercepter();
			}

			public void setPressure( final K pNew )
			{
				final K pOld = this.pressure.getAndSet( pNew );

				// no pressure or unchanged
				if( pOld == null || pOld.equals( pNew ) ) return;

				if( this.condition.get() == null || !this.condition.get()
						.getCompartment().isSusceptible() )
					return; // not susceptible: no exposure occurring

				final Instant t = now(), t0 = this.since.getAndSet( t );

				// update remaining resistance, subtracting dp for dt > 0
				if( t0 != null && !t0.equals( t ) )
				{
					final Quantity<Time> dt = t.toQuantity( Time.class )
							.subtract( t0.toQuantity( Time.class ) ),
							dp = this.differentiator.dq( t0, dt,
									this.resistance.get(), pOld ),
							res0 = this.resistance.getAndUpdate(
									res -> dp == null ? res : res.add( dp ) );
					System.err.println( "t=" + t + " dt=" + dt + " dp=" + dp
							+ " res0=" + res0 + " res_t=" + this.resistance );
				}

				// reschedule exposure
				wane();
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
					expose();
					return;
				}

				// (re)schedule exposure
				final Quantity<Time> dt = this.interceptor.dt( now(),
						resistance, QuantityUtil.zero( Time.class ),
						this.pressure.get() );

				final Expectation exp1 = dt == null ? null
						: after( dt ).call( this::expose ),
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
		IllnessTrajectory create( JsonNode config ) throws Exception;

		default NavigableMap<String, IllnessTrajectory>
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
				final NavigableMap<String, IllnessTrajectory> result = new TreeMap<>();
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
			public IllnessTrajectory create( final JsonNode config )
				throws ClassNotFoundException, ParseException
			{
				final JsonNode typeNode = Objects
						.requireNonNull( config, "No config?" ).get( TYPE_KEY );
				final String typeName = typeNode == null || typeNode.isNull()
						? TYPE_DEFAULT : typeNode.asText( TYPE_DEFAULT );
				final Class<? extends IllnessTrajectory> type = Class
						.forName( typeName )
						.asSubclass( IllnessTrajectory.class );
				return this.binder.inject( type ).reset( config );
			}
		}
	}
}
