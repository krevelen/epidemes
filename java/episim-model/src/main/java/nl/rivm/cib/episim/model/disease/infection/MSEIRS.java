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
package nl.rivm.cib.episim.model.disease.infection;

import java.beans.PropertyChangeEvent;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.measure.Quantity;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.AtomicDouble;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.bind.LocalBinding;
import io.coala.config.JsonConfigurable;
import io.coala.config.Jsonifiable;
import io.coala.exception.Thrower;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Expectation;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import nl.rivm.cib.episim.model.disease.IllnessTrajectory;

/**
 * {@link MSEIRS} produces an {@link IllnessTrajectory} that implements a
 * <a href=
 * "https://www.wikiwand.com/en/Compartmental_models_in_epidemiology#/The_MSEIRS_model">compartmental
 * model</a> <em>M &rarr; S &rarr; E &rarr; I &rarr; R &rarr; S</em> schema
 * 
 * <ul>
 * Legend:
 * <li>P: newborn vaccination rate (N&rarr;V vs. N&rarr;S)
 * <li>&rho;: non-newborn vaccination rate (S&rarr;V)
 * <li>&delta;: maternal immunity waning rate (M&rarr;S)
 * <li>&beta;: contact/infection rate (S&rarr;I or S&rarr;E) of new/secondary
 * infections;
 * <li>a: incubation rate (E&rarr;I);
 * <li>&gamma;: recovery rate (I&rarr;R)
 * </ul>
 * <p>
 * <table>
 * <tr>
 * <th><a href="https://www.wikiwand.com/en/Infection">DISEASE</a></th>
 * <th>AVG. <a href=
 * "https://www.wikiwand.com/en/Basic_reproduction_number">REPRODUCTIVE<br/>
 * RATIO</a> <em>R<sub>0</sub></em></th>
 * <th>AVG. <a href="https://www.wikiwand.com/en/Serial_interval">SERIAL<br/>
 * INTERVAL</a> <em>TA+IB</em></th>
 * <th>AVG.
 * <a href= "https://www.wikiwand.com/en/Case_fatality_rate">FATALITY<br/>
 * RATE</a> <em>CFR</em></th>
 * </tr>
 * <tr>
 * <td>Ebola</td>
 * <td>1.5-2</td>
 * <td>9-15</td>
 * <td>70%</td>
 * </tr>
 * <tr>
 * <td>Smallpox</td>
 * <td>5-7</td>
 * <td>14-16</td>
 * <td>30%</td>
 * </tr>
 * <tr>
 * <td>Measles</td>
 * <td>12-18</td>
 * <td>10-14</td>
 * <td>0.3-28%</td>
 * </tr>
 * <tr>
 * <td>SARS</td>
 * <td>2-3</td>
 * <td>8-9</td>
 * <td>11%</td>
 * </tr>
 * <tr>
 * <td>Diphtheria</td>
 * <td>6-7</td>
 * <td>26</td>
 * <td>5-20%</td>
 * </tr>
 * <tr>
 * <td>Whooping cough</td>
 * <td>12-17</td>
 * <td>19-28</td>
 * <td>0.1-1%</td>
 * </tr>
 * <tr>
 * <td>Flu</td>
 * <td>2-6</td>
 * <td>3-5</td>
 * <td>.4-4%</td>
 * </tr>
 * <tr>
 * <td>Rubella</td>
 * <td>5-7</td>
 * <td>15-23</td>
 * <td>0.05%</td>
 * </tr>
 * <tr>
 * <td>Mumps</td>
 * <td>4-7</td>
 * <td>18-19</td>
 * <td>0.0001%</td>
 * </tr>
 * <tr>
 * <td>Chicken pox</td>
 * <td>3-17</td>
 * <td>14-16</td>
 * <td>0.00001-0.0001%</td>
 * </tr>
 * <caption align=bottom>Source: <a href=
 * "http://www.washingtonpost.com/wp-srv/special/health/how-ebola-spreads/">Washington
 * Post</a></caption>
 * </table>
 */
public interface MSEIRS //extends IllnessTrajectory
{
	MSEIRS reset( Transition initialTransition, StateMachine transitioner );

	Observable<Transition> mseirsEmitter();

	/**
	 * {@link Compartment} implementation of {@link EpiCompartment}
	 */
	enum Compartment implements EpiCompartment, Jsonifiable
	{
		/**
		 * {@link Compartment} for MATERNALLY DERIVED or PASSIVELY IMMUNE
		 * infants, e.g. naturally (due to maternal antibodies in placenta and
		 * colostrum) or artificially (induced via antibody-transfer). See
		 * https://www.wikiwand.com/en/Passive_immunity
		 */
		PASSIVE_IMMUNE( false, false ),

		/**
		 * {@link Compartment} for SUSCEPTIBLE individuals (post-vaccination)
		 */
		SUSCEPTIBLE( false, true ),

		/**
		 * {@link Compartment} for EXPOSED individuals, i.e. LATENT INFECTED or
		 * PRE-INFECTIVE carriers
		 */
		EXPOSED( false, false ),

		/**
		 * {@link Compartment} for primary INFECTIVE individuals, currently able
		 * to transmit disease by causing secondary infections
		 */
		INFECTIVE( true, false ),

		/**
		 * {@link Compartment} for individuals RECOVERED from the disease, and
		 * naturally IMMUNE (vis-a-vis {@link #VACCINATED} or acquired immune),
		 * possibly waning again into {@link #SUSCEPTIBLE}
		 */
		RECOVERED( false, false ),

		/**
		 * {@link Compartment} for susceptible NEWBORN individuals after
		 * maternal immunity waned and before a (parental) vaccination decision
		 * is made
		 */
		NEWBORN( false, true ),

		/**
		 * {@link Compartment} for VACCINATED individuals having acquired
		 * immunity, possibly waning again into {@link #SUSCEPTIBLE}
		 */
		VACCINATED( false, false ),

		/**
		 * {@link Compartment} for susceptible but DORMANT individuals having
		 * recovered from prior exposure, but who will not become
		 * {@link #INFECTIVE} before being re-{@link #EXPOSED}
		 */
		DORMANT( false, true ),

		;

		private final boolean infective;

		private final boolean susceptible;

		private String json = null;

		private Compartment( final boolean infective,
			final boolean susceptible )
		{
			this.infective = infective;
			this.susceptible = susceptible;
		}

		@Override
		public String id()
		{
			return name();
		}

		@Override
		public boolean isInfective()
		{
			return this.infective;
		}

		@Override
		public boolean isSusceptible()
		{
			return this.susceptible;
		}

		@Override
		public String stringify()
		{
			return this.json != null ? this.json
					: (this.json = name().substring( 0, 1 ));
		}
	}

	/**
	 * {@link Transition} see <a href=
	 * "https://en.wikipedia.org/wiki/Compartmental_models_in_epidemiology">
	 * compartmental models</a>
	 */
	public enum Transition implements Jsonifiable
	{
		/**
		 * M &rarr; S or M &rarr; N: passive/maternal immunity waning (frequency
		 * = &delta;, mean period = <em>&delta;<sup>-1</sup></em>)
		 */
		PASSIVE( Compartment.NEWBORN ),

		/**
		 * S &rarr; E (or S &rarr; I): pre-exposure contact or infection
		 * (frequency = &beta;, mean period = <em>&beta;<sup>-1</sup></em>)
		 */
		SUSCEPTIBILITY( Compartment.EXPOSED ),

		/**
		 * E &rarr; I: viral loading/incubation (frequency = &sigma;, mean
		 * period = <em>&sigma;<sup>-1</sup></em>)
		 */
		LATENCY( Compartment.INFECTIVE ),

		/**
		 * I &rarr; R: infectious/recovery (frequency = &gamma;, mean period =
		 * <em>&gamma;<sup>-1</sup></em>)
		 */
		INFECTIOUS( Compartment.RECOVERED ),

		/**
		 * N &rarr; V (or S &rarr; V): pre-vaccination (newborn)
		 * susceptibility/acceptance/hesitancy (frequency = &rho;, mean period =
		 * <em>&rho;<sup>-1</sup></em>), possibly conditional on beliefs
		 */
		ACCEPTANCE( Compartment.VACCINATED ),

		/**
		 * R &rarr; S: wane/decline (frequency = &alpha;, mean period =
		 * <em>&alpha;<sup>-1</sup></em>), possibly conditional on co-morbidity,
		 * genetic factors, ...
		 */
		WANING_PASSIVE( Compartment.DORMANT ),

		/**
		 * R &rarr; S: wane/decline (frequency = &alpha;, mean period =
		 * <em>&alpha;<sup>-1</sup></em>), possibly conditional on co-morbidity,
		 * genetic factors, ...
		 */
		WANING_NATURAL( Compartment.DORMANT ),

		/**
		 * R &rarr; S: wane/decline (frequency = &alpha;, mean period =
		 * <em>&alpha;<sup>-1</sup></em>), possibly conditional on co-morbidity,
		 * genetic factors, ...
		 */
		WANING_ACQUIRED( Compartment.DORMANT ),

		/**
		 * E &rarr; &empty;: dying/mortality (frequency = &mu;, mean period =
		 * <em>&mu;<sup>-1</sup></em>)
		 */
		//		MORTALITY( null ),

		/**
		 * normal/absent &rarr; apparent/sub-/clinical: symptom
		 * latency/incubation
		 */
		//		APPEARANCE,

		/**
		 * apparent/sub-/clinical &rarr; normal/asymptomatic:
		 * morbidity/apparent/disabled
		 */
		//		DISAPPEARANCE,

		//
		;

		private final MSEIRS.Compartment result;

		private String json = null;

		private Transition( final MSEIRS.Compartment result )
		{
			this.result = result;
		}

		public MSEIRS.Compartment outcome()
		{
			return this.result;
		}

		@Override
		public String stringify()
		{
			return this.json != null ? this.json
					: (this.json = name().toLowerCase().replace( "_", "-" )
							+ "-period");
		}
	}

	/**
	 * {@link StateMachine} definition separated to enable reuse across
	 * {@link IllnessTrajectory}s
	 */
	@FunctionalInterface
	interface StateMachine
	{
		Maybe<Transition> next( Transition current );
	}

	/**
	 * {@link Simple} follows a state machine
	 */
	class Simple implements MSEIRS
	{

		private final BehaviorSubject<Transition> emitter = BehaviorSubject
				.create();

		private transient Disposable transitioning = null;

		@Override
		public Simple reset( final Transition initialPeriod,
			final StateMachine stateMachine )
		{
			if( this.transitioning != null ) this.transitioning.dispose();
			this.emitter.onNext( initialPeriod );

			this.transitioning = this.emitter.subscribe(
					transition -> stateMachine.next( transition ).subscribe(
							this.emitter::onNext, this.emitter::onError ),
					e ->
					{
						// don't propagate onto self
					} );
			return this;
		}

		@Override
		public Observable<Transition> mseirsEmitter()
		{
			return this.emitter;
		}
	}

	/**
	 * {@link Broker} (e.g. pathogen) can create a {@link IllnessTrajectory} for
	 * all individuals in a population
	 */
	interface Broker extends JsonConfigurable<Broker>
	{
		String TRAJECTORY_TYPE_KEY = "trajectory-type";

		default MSEIRS create( final Transition initialPeriod ) throws Exception
		{
			return create( initialPeriod, null );
		}

		MSEIRS create( Transition initialPeriod,
			Observable<PropertyChangeEvent> stateChanges ) throws Exception;

		static Transition defaultTransition( final Compartment current )
		{
			switch( current )
			{
			case PASSIVE_IMMUNE:
				return Transition.WANING_PASSIVE;
			case SUSCEPTIBLE:
				return Transition.SUSCEPTIBILITY;
			case EXPOSED:
				return Transition.LATENCY;
			case INFECTIVE:
				return Transition.INFECTIOUS;
			case RECOVERED:
				return Transition.WANING_NATURAL;
			case NEWBORN:
				return Transition.ACCEPTANCE;
			case VACCINATED:
				return Transition.WANING_ACQUIRED;
			case DORMANT:
				return Transition.SUSCEPTIBILITY;
			default:
				return Thrower.throwNew( IllegalArgumentException::new,
						() -> "No default for: " + current );
			}
		}

//		@Singleton
		/**
		 * {@link SimpleDefault} triggers the default transitions proactively,
		 * independent of co-morbidity or other host condition/configuration
		 */
		class SimpleDefault implements Broker, Proactive, LocalBinding
		{
			@InjectConfig
			private transient JsonNode config;

			@Inject
			private transient LocalBinder binder;

			@Inject
			private transient Scheduler scheduler;

			@Inject
			private transient ProbabilityDistribution.Parser distParser;

			private transient final Map<Transition, QuantityDistribution<?>> distCache = new HashMap<>();

			@Override
			public JsonNode config()
			{
				return this.config;
			}

			@Override
			public LocalBinder binder()
			{
				return this.binder;
			}

			@Override
			public Scheduler scheduler()
			{
				return this.scheduler;
			}

			protected ProbabilityDistribution.Parser distParser()
			{
				return this.distParser;
			}

			protected QuantityDistribution<?>
				cachedDelayDist( final Transition period )
			{
				return this.distCache.computeIfAbsent( period, key ->
				{
					try
					{
						final String dist = fromConfig( key.stringify(),
								(String) null );
						return dist == null ? distParser().getFactory()
								.createDeterministic( null ).toQuantities()
								: distParser().parseQuantity( dist );
					} catch( final Exception e )
					{
						return Thrower.rethrowUnchecked( e );
					}
				} );
			}

			protected Maybe<Transition>
				defaultDelayedTransition( final Transition current )
			{
				return Maybe.create( sub ->
				{
					try
					{
						final Quantity<?> delay = cachedDelayDist( current )
								.draw();
						final Transition next = Broker
								.defaultTransition( current.outcome() );
						if( delay == null )
							sub.onSuccess( next );
						else
							after( delay ).call( () -> sub.onSuccess( next ) );
					} catch( final Throwable e )
					{
						sub.onError( e );
					}
				} );
			}

			@Override
			public MSEIRS create( final Transition initialPeriod,
				final Observable<PropertyChangeEvent> stateChanges )
				throws ClassNotFoundException, ParseException
			{
				return binder()
						.inject( (Class<? extends MSEIRS>) fromConfig(
								TRAJECTORY_TYPE_KEY, MSEIRS.class,
								MSEIRS.Simple.class ) )
						.reset( initialPeriod, this::defaultDelayedTransition );
			}
		}

		class LocalPressure extends SimpleDefault
		{
			static class Tuple implements Comparable<Tuple>
			{
				Double resistance;
				MSEIRS machine;

				@Override
				public int compareTo( final Tuple o )
				{
					return 0;
				}

			}

			public static final String PRESSURE_CHANGE_PROPERTY_NAME = "pressure";

			private ProbabilityDistribution<Double> resistanceDistCache;

			protected ProbabilityDistribution<Double> resistanceDist()
			{
				return this.resistanceDistCache != null
						? this.resistanceDistCache
						: (this.resistanceDistCache = distParser().getFactory()
								.createExponential( 1d ));
			}

			protected Maybe<Transition> pressuredDelayedTransition(
				final Transition current, final Double resistance )
			{
				return current == Transition.SUSCEPTIBILITY
						? Maybe.create( sub ->
						{

							// delay until resistance threshold is reached 

						} ) : defaultDelayedTransition( current );
			}

			private final TreeMap<Double, MSEIRS> indResistance = new TreeMap<>();

			private final AtomicDouble curPressure = new AtomicDouble();

			private final AtomicDouble cumPressure = new AtomicDouble();

			private final AtomicReference<Expectation> pending = new AtomicReference<>();

			@Override
			public MSEIRS create( final Transition initialPeriod,
				final Observable<PropertyChangeEvent> stateChanges )
				throws ClassNotFoundException, ParseException
			{
				final Double resistance = this.cumPressure.get()
						+ resistanceDist().draw();

				final MSEIRS result = binder()
						.inject( (Class<? extends MSEIRS>) fromConfig(
								TRAJECTORY_TYPE_KEY, MSEIRS.class,
								MSEIRS.Simple.class ) )
						.reset( initialPeriod,
								c -> pressuredDelayedTransition( c,
										resistance ) );

				// somehow track which machine to transition upon 
				// reaching resistance thresholds

				this.indResistance.put( resistance, result );

				// cache 
				stateChanges
						.filter( ev -> PRESSURE_CHANGE_PROPERTY_NAME
								.equals( ev.getPropertyName() ) )
						.map( PropertyChangeEvent::getNewValue )
						.cast( Number.class ).map( Number::doubleValue )
						.filter( dp -> dp != 0 && Double.isFinite( dp ) )
						.subscribe( delta ->
						{
							final double newPressure = this.curPressure
									.addAndGet( delta );
							this.pending.getAndUpdate( pending ->
							{
								if( pending != null ) pending.remove();
								if( this.indResistance.isEmpty() ) return null;
								final Double nextThreshold = this.indResistance
										.firstKey();
								final double delay = (nextThreshold
										- this.cumPressure.get())*newPressure;
								return after( delay ).call( t ->
								{
									this.indResistance.remove( nextThreshold );
								} );
							} );
						} );

				return result;
			}

		}
	}
}