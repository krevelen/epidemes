///* $Id$
// * 
// * Part of ZonMW project no. 50-53000-98-156
// * 
// * @license
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy
// * of the License at
// * 
// * http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// * 
// * Copyright (c) 2016 RIVM National Institute for Health and Environment 
// */
//package nl.rivm.cib.episim.model.disease.infection;
//
//import java.text.ParseException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.atomic.AtomicReference;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import javax.measure.Quantity;
//import javax.measure.quantity.Time;
//
//import com.fasterxml.jackson.databind.JsonNode;
//
//import io.coala.bind.InjectConfig;
//import io.coala.bind.LocalBinder;
//import io.coala.config.JsonConfigurable;
//import io.coala.exception.Thrower;
//import io.coala.json.Attributed;
//import io.coala.random.ProbabilityDistribution;
//import io.coala.random.QuantityDistribution;
//import io.coala.time.Scheduler;
//import io.reactivex.Observable;
//import io.reactivex.subjects.PublishSubject;
//import nl.rivm.cib.episim.model.disease.ClinicalPhase;
//import nl.rivm.cib.episim.model.disease.IllnessTrajectory;
//import nl.rivm.cib.episim.model.disease.infection.EpiTrajectory.EpiCondition.SimpleEpiClinical;
//
///**
// * {@link EpiTrajectory} produces a configurable {@link IllnessTrajectory} that
// * implements a <a href=
// * "https://www.wikiwand.com/en/Compartmental_models_in_epidemiology#/The_MSEIRS_model">compartmental
// * model</a> <em>M &rarr; S &rarr; E &rarr; I &rarr; R &rarr; S</em> schema
// * 
// * <ul>
// * Legend:
// * <li>P: newborn vaccination rate (N&rarr;V vs. N&rarr;S)
// * <li>&rho;: non-newborn vaccination rate (S&rarr;V)
// * <li>&delta;: maternal immunity waning rate (M&rarr;S)
// * <li>&beta;: contact/infection rate (S&rarr;I or S&rarr;E) of new/secondary
// * infections;
// * <li>a: incubation rate (E&rarr;I);
// * <li>&gamma;: recovery rate (I&rarr;R)
// * </ul>
// * <p>
// * <table>
// * <tr>
// * <th><a href="https://www.wikiwand.com/en/Infection">DISEASE</a></th>
// * <th>AVG. <a href=
// * "https://www.wikiwand.com/en/Basic_reproduction_number">REPRODUCTIVE<br/>
// * RATIO</a> <em>R<sub>0</sub></em></th>
// * <th>AVG. <a href="https://www.wikiwand.com/en/Serial_interval">SERIAL<br/>
// * INTERVAL</a> <em>TA+IB</em></th>
// * <th>AVG.
// * <a href="https://www.wikiwand.com/en/Case_fatality_rate">FATALITY<br/>
// * RATE</a> <em>CFR</em></th>
// * </tr>
// * <tr>
// * <td>Ebola</td>
// * <td>1.5-2</td>
// * <td>9-15</td>
// * <td>70%</td>
// * </tr>
// * <tr>
// * <td>Smallpox</td>
// * <td>5-7</td>
// * <td>14-16</td>
// * <td>30%</td>
// * </tr>
// * <tr>
// * <td>Measles</td>
// * <td>12-18</td>
// * <td>10-14</td>
// * <td>0.3-28%</td>
// * </tr>
// * <tr>
// * <td>SARS</td>
// * <td>2-3</td>
// * <td>8-9</td>
// * <td>11%</td>
// * </tr>
// * <tr>
// * <td>Diphtheria</td>
// * <td>6-7</td>
// * <td>26</td>
// * <td>5-20%</td>
// * </tr>
// * <tr>
// * <td>Whooping cough</td>
// * <td>12-17</td>
// * <td>19-28</td>
// * <td>0.1-1%</td>
// * </tr>
// * <tr>
// * <td>Flu</td>
// * <td>2-6</td>
// * <td>3-5</td>
// * <td>.4-4%</td>
// * </tr>
// * <tr>
// * <td>Rubella</td>
// * <td>5-7</td>
// * <td>15-23</td>
// * <td>0.05%</td>
// * </tr>
// * <tr>
// * <td>Mumps</td>
// * <td>4-7</td>
// * <td>18-19</td>
// * <td>0.0001%</td>
// * </tr>
// * <tr>
// * <td>Chicken pox</td>
// * <td>3-17</td>
// * <td>14-16</td>
// * <td>0.00001-0.0001%</td>
// * </tr>
// * <caption align=bottom>Source: <a href=
// * "http://www.washingtonpost.com/wp-srv/special/health/how-ebola-spreads/">Washington
// * Post</a></caption>
// * </table>
// */
//public interface EpiTrajectory extends IllnessTrajectory
//{
//	@Override
//	Observable<? extends EpiCondition> emitCondition();
//
//	interface EpiCondition extends IllnessTrajectory.Condition
//	{
//
//		EpiCompartment getEpiCompartment();
//
//		class SimpleEpiClinical extends
//			Attributed.Reactive.SimpleOrdinal<SimpleEpiClinical> implements
//			EpiCondition, ClinicalPhase.Attributable<SimpleEpiClinical>,
//			EpiCompartment.Attributable<SimpleEpiClinical>
//		{
//
//			public static final SimpleEpiClinical MATERNAL = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.PASSIVE_IMMUNE )
//					.with( ClinicalPhase.Simple.ABSENT );
//
//			public static final SimpleEpiClinical SUSCEPTIBLE = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.SUSCEPTIBLE )
//					.with( ClinicalPhase.Simple.ABSENT );
//
//			public static final SimpleEpiClinical EXPOSED = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.EXPOSED )
//					.with( ClinicalPhase.Simple.LATENT );
//
//			public static final SimpleEpiClinical APPARENT_EXPOSED = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.EXPOSED )
//					.with( ClinicalPhase.Simple.SYMPTOMATIC_APPARENT );
//
//			public static final SimpleEpiClinical INFECTIVE = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.INFECTIVE )
//					.with( ClinicalPhase.Simple.LATENT );
//
//			public static final SimpleEpiClinical APPARENT_INFECTIVE = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.INFECTIVE )
//					.with( ClinicalPhase.Simple.SYMPTOMATIC_APPARENT );
//
//			public static final SimpleEpiClinical RECOVERED = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.RECOVERED )
//					.with( ClinicalPhase.Simple.LATENT );
//
//			public static final SimpleEpiClinical NEWBORN = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.NEWBORN )
//					.with( ClinicalPhase.Simple.ABSENT );
//
//			public static final SimpleEpiClinical VACCINATED = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.VACCINATED )
//					.with( ClinicalPhase.Simple.LATENT );
//
//			public static final SimpleEpiClinical DORMANT = new SimpleEpiClinical()
//					.with( EpiCompartment.SimpleMSEIRS.DORMANT )
//					.with( ClinicalPhase.Simple.LATENT );
//
//			@Override
//			public EpiCompartment getEpiCompartment()
//			{
//				return getCompartment();
//			}
//		}
//	}
//
//	/**
//	 * {@link SimplePressured} maintains an individual's remaining infection
//	 * resistance
//	 */
//	public static class SimpleClinical
//		implements EpiTrajectory, JsonConfigurable<SimpleClinical>
//	{
//
//		protected final AtomicReference<SimpleEpiClinical> condition = new AtomicReference<>();
//
//		protected final PublishSubject<SimpleEpiClinical> emitter = PublishSubject
//				.create();
//
//		@Inject
//		private transient Scheduler scheduler;
//
//		@Inject
//		private transient ProbabilityDistribution.Parser distParser;
//
//		@InjectConfig
//		private JsonNode config;
//
//		private final Map<String, QuantityDistribution<Time>> distCache = new HashMap<>();
//
//		protected Quantity<Time> dtMapper( final Transition configKey )
//		{
//			return this.distCache.computeIfAbsent(
//					fromConfig( configKey.jsonKey(), PERIOD_DEFAULT ), dist ->
//					{
//						try
//						{
//							return this.distParser.parseQuantity( dist,
//									Time.class );
//						} catch( final ParseException e )
//						{
//							return Thrower.rethrowUnchecked( e );
//						}
//					} ).draw();
//		}
//
//		@Override
//		public String toString()
//		{
//			return stringify();
//		}
//
//		@Override
//		public Scheduler scheduler()
//		{
//			return this.scheduler;
//		}
//
//		@Override
//		public JsonNode config()
//		{
//			return this.config;
//		}
//
//		/**
//		 * sets condition {@link #MATERNAL} and schedules {@link #wane} after
//		 * {@link Transition#PASSIVE} is 'consumed'
//		 */
//		protected void passive()
//		{
//			this.emitter.onNext( this.condition
//					.updateAndGet( c -> SimpleEpiClinical.MATERNAL ) );
//			after( dtMapper( Transition.PASSIVE ) ).call( this::wane );
//		}
//
//		/**
//		 * sets condition {@link #DORMANT} and schedules {@link #expose} after
//		 * {@link Transition#SUSCEPTIBILITY} is 'consumed'
//		 */
//		protected void wane()
//		{
//			this.emitter.onNext( this.condition
//					.updateAndGet( x -> x == SimpleEpiClinical.MATERNAL
//							? SimpleEpiClinical.SUSCEPTIBLE
//							: SimpleEpiClinical.DORMANT ) );
//			after( dtMapper( Transition.SUSCEPTIBILITY ) )
//					.call( this::expose );
//		}
//
//		/**
//		 * sets condition {@link #EXPOSED} and schedules both {@link #shed}
//		 * after {@link Transition#LATENCY} and {@link #clinical} after
//		 * {@link Transition#APPEARANCE} is 'consumed'
//		 */
//		protected void expose()
//		{
//			this.emitter.onNext( this.condition
//					.updateAndGet( c -> SimpleEpiClinical.EXPOSED ) );
//			after( dtMapper( Transition.LATENCY ) ).call( this::shed );
//			after( dtMapper( Transition.APPEARANCE ) )
//					.call( this::clinical );
//		}
//
//		/**
//		 * sets condition {@link #INFECTIVE} or {@link #APPARENT_INFECTIVE} and
//		 * schedules {@link #recover} after {@link Transition#INFECTIOUS} is
//		 * 'consumed'
//		 */
//		protected void shed()
//		{
//			this.emitter.onNext(
//					this.condition.updateAndGet( c -> c.getPhase().isApparent()
//							? SimpleEpiClinical.APPARENT_INFECTIVE
//							: SimpleEpiClinical.INFECTIVE ) );
//			after( dtMapper( Transition.INFECTIOUS ) ).call( this::recover );
//		}
//
//		/**
//		 * sets condition {@link #APPARENT_INFECTIVE} or
//		 * {@link #APPARENT_EXPOSED} and schedules {@link #subclinical} after
//		 * {@link Transition#MORBIDITY} is 'consumed'
//		 */
//		protected void clinical()
//		{
//			this.emitter.onNext( this.condition
//					.updateAndGet( c -> c.getCompartment().isInfective()
//							? SimpleEpiClinical.APPARENT_INFECTIVE
//							: SimpleEpiClinical.APPARENT_EXPOSED ) );
//			after( dtMapper( Transition.DISAPPEARANCE ) )
//					.call( this::subclinical );
//		}
//
//		/**
//		 * sets condition {@link #INFECTIVE} or {@link #EXPOSED} is 'consumed'
//		 */
//		protected void subclinical()
//		{
//			this.emitter.onNext( this.condition
//					.updateAndGet( c -> c.getCompartment().isInfective()
//							? SimpleEpiClinical.INFECTIVE
//							: SimpleEpiClinical.EXPOSED ) );
//		}
//
//		/**
//		 * sets condition {@link #EXPOSED} and schedules {@link #wane} after
//		 * {@link Transition#WANING} is 'consumed'
//		 */
//		protected void recover()
//		{
//			this.emitter.onNext( this.condition
//					.updateAndGet( c -> SimpleEpiClinical.RECOVERED ) );
//			after( dtMapper( Transition.WANING ) ).call( this::wane );
//		}
//
//		@Override
//		public Observable<SimpleEpiClinical> emitCondition()
//		{
//			return this.emitter;
//		}
//	}
//
//	/**
//	 * {@link Factory} used for {@link PressuredEpiTrajectory}
//	 */
//	interface Factory extends IllnessTrajectory.Broker
//	{
//		@Override
//		EpiTrajectory create( JsonNode config ) throws Exception;
//
//		String TYPE_DEFAULT = SimpleClinical.class.getName();
//
//		@Singleton
//		class SimpleBinding implements Factory
//		{
//			@Inject
//			private LocalBinder binder;
//
//			@Override
//			public EpiTrajectory create( final JsonNode config )
//				throws ClassNotFoundException, ParseException
//			{
//				final JsonNode typeNode = Objects
//						.requireNonNull( config, "No config?" ).get( TYPE_KEY );
//				final String typeName = typeNode == null ? TYPE_DEFAULT
//						: typeNode.asText( TYPE_DEFAULT );
//				final Class<? extends EpiTrajectory> type = Class
//						.forName( typeName ).asSubclass( EpiTrajectory.class );
//				return this.binder.inject( type, config );
//			}
//		}
//	}
//}