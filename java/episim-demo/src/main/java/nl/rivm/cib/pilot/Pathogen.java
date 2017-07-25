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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.LocalBinder;
import io.coala.exception.Thrower;
import io.coala.math.DecimalUtil;
import io.coala.math.Tuple;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.Expectation;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
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

	String TYPE_DEFAULT = MSEIR.class.getName();

	String PERIOD_DEFAULT = "const( 1.1 week )";

	String PASSIVE_PERIOD_KEY = "passivePeriod";

	/**
	 * invasion period (S->E or S->I), related to contact rate of new/secondary
	 * infections, <em>&beta;</em>
	 */
	String RESISTANCE_PERIOD_KEY = "resistancePeriod";

	/** exposed/latent period (E->I), related to incubation rate <em>a</em> */
	String LATENT_PERIOD_KEY = "latentPeriod"; // Pathogen

	/**
	 * infectious/recovery period (I->R) <em>D</em>, related to transition rate
	 * <em>v</em>=1/<em>D</em>
	 */
	String RECOVER_PERIOD_KEY = "recoverPeriod";

	/**
	 * wane (temporary recovery) period (R->S) TODO conditional on e.g.
	 * co-morbidity, genetic factors, ...
	 */
	String WANE_PERIOD_KEY = "wanePeriod";

	/** incubation period (E->C, sub->clinical symptoms) */
	String INCUBATE_PERIOD_KEY = "incubatePeriod";

	/** symptom period (C->R, normal/asymptomatic) */
	String CLINICAL_PERIOD_KEY = "clinicalPeriod";

	Observable<? extends Condition>
		illnessTrajectory( Observable<? extends Number> pressureSource );

	@SuppressWarnings( "rawtypes" )
	interface Condition extends Comparable
	{
//		MedicalStage getStage();

		ClinicalPhase getSymptoms();

//		Serostatus getSerostatus();

		EpidemicCompartment getCompartment();

		Simple PASSIVE = new Simple( EpidemicCompartment.Simple.PASSIVE_IMMUNE,
				ClinicalPhase.Simple.ASYMPTOMATIC );

		Simple SUSCEPTIBLE = new Simple( EpidemicCompartment.Simple.SUSCEPTIBLE,
				ClinicalPhase.Simple.ASYMPTOMATIC );

		Simple EXPOSED = new Simple( EpidemicCompartment.Simple.EXPOSED,
				ClinicalPhase.Simple.ASYMPTOMATIC );

		Simple EXPOSED_CLINICAL = new Simple(
				EpidemicCompartment.Simple.EXPOSED,
				ClinicalPhase.Simple.SYSTEMIC );

		Simple INFECTIVE = new Simple( EpidemicCompartment.Simple.INFECTIVE,
				ClinicalPhase.Simple.ASYMPTOMATIC );

		Simple INFECTIVE_CLINICAL = new Simple(
				EpidemicCompartment.Simple.INFECTIVE,
				ClinicalPhase.Simple.SYSTEMIC );

		Simple RECOVERED = new Simple( EpidemicCompartment.Simple.RECOVERED,
				ClinicalPhase.Simple.ASYMPTOMATIC );

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

	class MSEIR implements Pathogen
	{
		private final Map<String, QuantityDistribution<Time>> distCache = new HashMap<>();

		@Inject
		private Scheduler scheduler;

		@Inject
		private ProbabilityDistribution.Parser distParser;

		private JsonNode config;

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		/**
		 * {@link Status} maintains an individual's remaining infection
		 * resistance
		 */
		public static class Status<T> implements Proactive
		{
			private final Scheduler scheduler;
			private final Function<String, Quantity<Time>> dtMapper;
			private final BehaviorSubject<Condition> emitter = BehaviorSubject
					.create();
			private final AtomicReference<Expectation> expose = new AtomicReference<>();
			private final BiFunction<Quantity<Time>, T, Quantity<Time>> pressureChanger;
			private final AtomicReference<T> pressure = new AtomicReference<>();
			private final AtomicReference<Quantity<Time>> resistance = new AtomicReference<>();
			private final AtomicReference<Quantity<Time>> since = new AtomicReference<>();

			public Status( final Scheduler scheduler, final Condition initial,
				final Function<String, Quantity<Time>> dtMapper,
				final BiFunction<Quantity<Time>, T, Quantity<Time>> pressurizer,
				final T initialPressure )
			{
				Objects.requireNonNull( initial,
						"Condition is required, diseased?" );
				Objects.requireNonNull( initial.getCompartment(),
						"Compartment is required, diseased?" );
				this.scheduler = scheduler;
				this.dtMapper = dtMapper;
				this.pressureChanger = pressurizer;
				this.pressure.set( initialPressure );
				atOnce( t ->
				{
					switch( (EpidemicCompartment.Simple) initial
							.getCompartment() )
					{
					case PASSIVE_IMMUNE:
						this.emitter.onNext( initial );
						after( this.dtMapper.apply( PASSIVE_PERIOD_KEY ) )
								.call( this::wane );
						break;
					case SUSCEPTIBLE:
						wane();
						break;
					case EXPOSED:
						expose();
						break;
					case INFECTIVE:
						shed();
						break;
					case RECOVERED:
						recover();
						break;
					}
				} );
			}

			@Override
			public Scheduler scheduler()
			{
				return this.scheduler;
			}

			@SuppressWarnings( "unchecked" )
			public void setPressure( final T pressure )
			{
				final T oldP = this.pressure.getAndSet( pressure );

				if( oldP != null && oldP.equals( pressure ) ) return; // pressure unchanged

				if( this.emitter.getValue() == null || !this.emitter.getValue()
						.getCompartment().isSusceptible() )
					return; // not susceptible: no exposure occurring

				final Quantity<Time> t = now().toQuantity( Time.class ),
						t0 = this.since.getAndSet( t );

				// update remaining resistance, subtracting dp for dt > 0
				if( t0 != null && !t0.equals( t ) )
					this.resistance.getAndUpdate(
							res -> res.subtract( this.pressureChanger.apply(
									t.subtract( t0 ), this.pressure.get() ) ) );

				// reschedule exposure
				wane();
			}

			protected void wane()
			{
				// check resistance is initialized
				this.resistance.getAndUpdate( dt -> dt != null ? dt
						: this.dtMapper.apply( RESISTANCE_PERIOD_KEY ) );
				// (re)schedule exposure
				final Expectation exp = this.expose.getAndSet(
						after( this.resistance.get() ).call( this::expose ) );
				// cancel previous, if any
				if( exp != null ) exp.remove();
				// emit only if not already susceptible
				if( !this.emitter.getValue().getCompartment().isSusceptible() )
					this.emitter.onNext( Condition.SUSCEPTIBLE );
			}

			protected void expose()
			{
				this.emitter.onNext( Condition.EXPOSED );
				after( this.dtMapper.apply( INCUBATE_PERIOD_KEY ) )
						.call( this::clinical );
				after( this.dtMapper.apply( LATENT_PERIOD_KEY ) )
						.call( this::shed );
			}

			protected void shed()
			{
				this.emitter.onNext(
						this.emitter.getValue().getSymptoms().isClinical()
								? Condition.INFECTIVE_CLINICAL
								: Condition.INFECTIVE );
				after( this.dtMapper.apply( RECOVER_PERIOD_KEY ) )
						.call( this::recover );
			}

			protected void clinical()
			{
				this.emitter.onNext(
						this.emitter.getValue().getCompartment().isInfective()
								? Condition.INFECTIVE_CLINICAL
								: Condition.EXPOSED_CLINICAL );
				after( this.dtMapper.apply( CLINICAL_PERIOD_KEY ) )
						.call( this::subclinical );
			}

			protected void subclinical()
			{
				this.emitter.onNext(
						this.emitter.getValue().getCompartment().isInfective()
								? Condition.INFECTIVE : Condition.EXPOSED );
			}

			private void recover()
			{
				this.emitter.onNext( Condition.RECOVERED );
				after( this.dtMapper.apply( WANE_PERIOD_KEY ) )
						.call( this::wane );
			}
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public Observable<? extends Condition>
			illnessTrajectory( final Observable<? extends Number> pressure )
		{
			final Status<BigDecimal> status = new Status<BigDecimal>(
					scheduler(), Condition.PASSIVE,
					configKey -> this.distCache.computeIfAbsent(
							fromConfig( configKey, PERIOD_DEFAULT ), key ->
							{
								try
								{
									return this.distParser.parseQuantity( key,
											Time.class );
								} catch( final ParseException e )
								{
									return Thrower.rethrowUnchecked( e );
								}
							} ).draw(),
					Quantity::divide, BigDecimal.ZERO );
			pressure.map( DecimalUtil::valueOf ).subscribe( status::setPressure,
					status.emitter::onError );
			return status.emitter;
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
					() -> "Invalid attractor config: " + config );
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
				return this.binder.inject( type, config ).reset( config );
			}
		}
	}
}
