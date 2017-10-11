/* $Id: 73fcf100b37b9cebac2739e4bea14198b4e2e020 $
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

import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.quantity.Frequency;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.bind.InjectConfig.Scope;
import io.coala.config.YamlConfig;
import io.coala.enterprise.Actor;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.disease.Afflict;
import nl.rivm.cib.episim.model.disease.Afflicted;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.infection.Contagion.Contagium;
import nl.rivm.cib.episim.model.locate.Place;

/**
 * Unlike disorders, allergies, or many other (bio)hazards known in etiology of
 * disease, {@link Pathogen}s are microbes living in {@link Contagium}s, causing
 * {@link Transmission} of infectious/transmissible/communicable/contagious
 * disease. {@link Pathogen}s include viruses, bacteria, fungi, and parasites
 * for which respective {@link Recovery} medications may exist (antibiotics,
 * antivirals, antifungals, and antiprotozoals/antihelminthics).
 * 
 * <p>
 * Some terminology from
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>:
 * <table>
 * <tr>
 * <th>R<sub>0</sub></th>
 * <td>Basic reproduction number</td>
 * </tr>
 * <tr>
 * <th>1 / &epsilon;</th>
 * <td>Average latent period</td>
 * </tr>
 * <tr>
 * <th>1 / &gamma;</th>
 * <td>Average infectious period</td>
 * </tr>
 * <tr>
 * <th>f</th>
 * <td>Average loss of immunity rate of recovered individuals</td>
 * </tr>
 * <tr>
 * <th>&delta;</th>
 * <td>Average temporary immunity period</td>
 * </tr>
 * </table>
 * 
 * <p>
 * measles, see e.g. https://en.wikipedia.org/wiki/File:Measles.webm; influenza
 * see e.g. Figure 5 in https://doi.org/10.1093/aje/kwm375
 * 
 * <p>
 * from Zhang (2016)?: typical life cycle (compartment transitions): - passively
 * immune - susceptible - vaccine-infected : latent (incubation-period),
 * infectious, recovered (1-2y), susceptible - contact-infected : latent
 * (incubation-period) - asymptomatic, infectious, recovering, removed (2-7y),
 * susceptible - symptomatic - mobile, recovering, removed (2-7y), susceptible -
 * immobilize - convalescent, removed (2-7y), susceptible - hospitalize -
 * convalescent, removed (2-7y), susceptible - death, removed
 * 
 * @version $Id: 73fcf100b37b9cebac2739e4bea14198b4e2e020 $
 * @author Rick van Krevelen
 */
public interface Pathogen extends Actor<Transmission>
{

	/**
	 * {@link Recovery} transactions initiated by some {@link Afflicted} host
	 * {@link Condition} represents the dynamics of clearing a {@link Afflict}
	 * persisted by the executor {@link Pathogen}, e.g. due to immune response.
	 * 
	 * <li>{@link #id()}: fact identifier
	 * <li>{@link #transaction()}: links initiator {@link Pathogen} &hArr;
	 * executor {@link Afflicted}
	 * <li>{@link #causeRef()}: reference to cause, e.g. immune system
	 * {@link Condition} producing Immunoglobulin M (IgM) for recent primary
	 * infections; immunoglobulin G (IgG) for past infection or immunization
	 * <li>{@link #kind()}:
	 * <ul>
	 * <li>[rq]: seroconversion &rArr; [st]: effective treatment/vaccination
	 * </ul>
	 * 
	 * @version $Id: 0cf1c75df00a9cefc122aaec1f98d86596665550 $
	 * @author Rick van Krevelen
	 */
	public interface Recovery extends Afflict
	{
	}

	/**
	 * The (derived) force of infection (denoted &lambda;) is the rate
	 * ({@link Frequency}) at which a secondary susceptible individual acquires
	 * this infectious disease from primary infectives. It is directly
	 * proportional to the effective transmission rate &beta; and gives the
	 * number of new infections given a number of infectives and the average
	 * duration of exposures (see
	 * <a href="https://en.wikipedia.org/wiki/Force_of_infection">wikipedia</a>
	 * ), here with calibration to specific circumstances:
	 * 
	 * @param routes a {@link Collection} of local {@link TransmissionRoute}s
	 * @param infectionPressure the infection pressure as
	 *            {@link ContactIntensity} for each primary infective currently
	 *            in contact
	 * @return the {@link Frequency} {@link Amount} of infection acquisition
	 */
//	Quantity<Frequency> getForceOfInfection(
//		Collection<TransmissionRoute> routes,
//		Collection<ContactIntensity> infectionPressure );

	interface Factory
	{
		Pathogen create( Comparable<?> name );
	}

	/**
	 * {@link SimpleSEIR} implements an {@link Pathogen} drawing probabilities
	 * for transition and duration of {@link EpiCompartment}s from some {link
	 * RandomDistribution}, independent of relation type between infective and
	 * susceptible {@link Afflicted}s, their current {@link Condition}s, or the
	 * contact {@link Place},{@link Duration}, or {@link TransmissionRoute}
	 */
	interface SimpleSEIR extends Pathogen
	{
		String INFECTION_RESISTANCE_KEY = "resistancePeriod"; // drawn by Condition

		String LATENT_PERIOD_KEY = "latentPeriod"; // Pathogen

		String RECOVER_PERIOD_KEY = "recoverPeriod"; // Pathogen

		String WANE_PERIOD_KEY = "wanePeriod"; // Pathogen or Condition

		String INCUBATE_PERIOD_KEY = "incubatePeriod"; // Pathogen

		String CLINICAL_PERIOD_KEY = "clinicalPeriod"; // Pathogen

		@Override
		default void onInit()
		{
			// handle response from Afflicted/Condition
			emit( Transmission.class, FactKind.STATED )
					.subscribe( this::onInvasion, this::onError );
			emit( Transmission.class, FactKind.DECLINED )
					.subscribe( this::onEscape, this::onError );

		}

		default void onContact( final Occupancy rq )
		{

		}

		default void onEscape( final Transmission dc )
		{
			// transmission failed, wait for another attempt by e.g. Contagion
		}

		default void onInvasion( final Transmission st )
		{

//			final Duration timeToInvasion = this
//					.getOrCreate( INFECTION_RESISTANCE_KEY ).draw();// draw once per Condition
//			final Instant timeOfInvasion = now().add( timeToInvasion );
//			if( rq.expire() == null // assume infinite pressure duration
//					|| Compare.lt( timeOfInvasion, rq.expire() ) )
//			{
//				// pressure is exhausted in duration of this contact
//				at( timeOfInvasion ).call( t ->
//				{
//					// become EXPOSED
//					respond( rq, FactKind.STATED ).commit();
//					after( getOrCreate( LATENT_PERIOD_KEY ).draw() ).call( () ->
//					{
//						// recovery...
//					} );
//				} );

//			after( getOrCreate( LATENT_PERIOD_KEY ).draw() )
//					.call( () -> shed( person ) );
//			after( getOrCreate( INCUBATE_PERIOD_KEY ).draw() )
//					.call( () -> systemic( person ) );
//			} else
//			{
//				// remain SUSCEPTIBLE
//				// seir.at(f.expire() ).call(t->seir.respond( f, FactKind.REJECTED ).commit());
//			}

		}

		/**
		 * {@link Config} specifies distributions for e.g. the pathogen's total
		 * <em>generation time</em>, i.e. the total primary-to-secondary
		 * infection interval (average latent + infectious periods), is e.g.
		 * <em>T<sub>g</sub></em>=2.6 days for influenza
		 * (<a href="https://doi.org/10.1038/nature04795">Ferguson, 2006</a>,
		 * supplement p.12), or <em>u</em>=11.9 or 11.1 days for measles/rubeola
		 * (<a href= "https://doi.org/10.1016/j.jtbi.2011.06.015">Klinkenberg,
		 * 2011</a>, Table 2 p.57)
		 */
		interface Config extends YamlConfig
		{
			/**
			 * typically assumes Poisson process for the individual "resistance
			 * to infection" and has been modeled as exponential ia.i.d.
			 * <em>l<sub>i</sub>=e<sup>&ndash;t</sup></em>
			 * (<a href="http://www.jstor.org/stable/3213811">Sellke, 1983</a>,
			 * p.391, <em>t</em> refers to ???) or as lognormal infectiousness
			 * profile <em>&kappa;(T)</em>
			 * (<a href="https://doi.org/10.1038/nature04795">Ferguson,
			 * 2006</a>, supplement p.10)
			 * 
			 * @see ProbabilityDistribution.Factory#createExponential(Number)
			 * @return a "individual resistance to infection" distribution
			 */
			@Key( INFECTION_RESISTANCE_KEY )
			@DefaultValue( "exp(11)" ) // FIXME fit this parameter !!
			String infectionResistanceDaysDist();

			/**
			 * distribution for time of latency until infectiousness, between
			 * {@link EpiCompartment.Compartment#EXPOSED E} and
			 * {@link EpiCompartment.Compartment#INFECTIVE I} (i.e. 1 / &epsilon;).
			 */
			@Key( LATENT_PERIOD_KEY )
			@DefaultValue( "exp(0.09)" ) // 1/11d (6-17d) -3d coryza etc before prodromal fever at 7-21d
			String latentPeriodDaysDist();

			/**
			 * distribution for time of infectiousness (viral shedding) until
			 * recovery: between {@link EpiCompartment.Compartment#INFECTIVE} and
			 * {@link EpiCompartment.Compartment#RECOVERED} conditions (i.e. 1 /
			 * &gamma;)
			 */
			@Key( RECOVER_PERIOD_KEY )
			@DefaultValue( "exp(.16)" ) // 1/10d (6-14d) = prodromal fever 3-7 days + rash 4-7days
			String recoverPeriodDaysDist();

			/**
			 * distribution for duration of immunity: between
			 * {@link EpiCompartment.Compartment#RECOVERED} and
			 * {@link EpiCompartment.Compartment#SUSCEPTIBLE} (i.e. &delta;), or
			 * {@code null} for infinite (there is no loss of immunity)
			 */
			@Key( WANE_PERIOD_KEY )
			@DefaultValue( "const(100000)" ) // infinite = never wane
			String wanePeriodDaysDist();

			/** distribution for time between infection and onset of symptoms */
			@Key( INCUBATE_PERIOD_KEY )
			@DefaultValue( "exp(0.07)" ) // 1/14 (7-21 days, says CDC)
			String incubatePeriodDaysDist();

			/** distribution for time duration of symptoms */
			@Key( CLINICAL_PERIOD_KEY )
			@DefaultValue( "exp(0.2)" ) // 1/5 (4-7 days, says CDC)
			String clinicalPeriodDaysDist();
		}

		/**
		 * {@link SimpleSEIR.Factory} is a simple {@link Pathogen.Factory} for
		 * generating {@link SimpleSEIR S-E-I-R} implementations of a
		 * {@link Pathogen}, with attributes drawn from distributions as
		 * speicifed using {@link SimpleSEIR.Config}, including:
		 * <li>{@link Config#infectionResistanceDaysDist() infection resistance}
		 * (S &rarr; E);
		 * <li>{@link Config#latentPeriodDaysDist() latent period} (E &rarr; I);
		 * <li>{@link Config#recoverPeriodDaysDist() recover period} (I &rarr;
		 * R);
		 * <li>{@link Config#wanePeriodDaysDist() wane period} (R &rarr; S); and
		 * <li>{@link Config#clinicalPeriodDaysDist() clinical period}
		 * (prodromal &rarr; postdromal).
		 */
		@Singleton
		class Factory implements Pathogen.Factory
		{
			/** */
			private static final Logger LOG = LogUtil
					.getLogger( Factory.class );

			@InjectConfig( Scope.BINDER )
			private Config config;

			@Inject
			private Actor.Factory actors;

			@Inject
			private ProbabilityDistribution.Parser distParser;

			private final Map<String, ProbabilityDistribution<Duration>> durationDists = new ConcurrentHashMap<>();

			private ProbabilityDistribution<Duration>
				getOrCreate( final String distName )
			{
				return this.durationDists.computeIfAbsent( distName, key ->
				{
					final String distValue = this.config
							.getProperty( distName );
					try
					{
						return this.distParser.parse( distValue );
					} catch( ParseException e )
					{
						LOG.error(
								"Problem parsing '" + distValue
										+ "', assuming 1 day for: " + distName,
								e );
						return this.distParser.getFactory().createDeterministic(
								Duration.of( 1, TimeUnits.DAYS ) );
					}
				} );
			}

			/**
			 * TODO upon first exposure, draw "pressure threshold" (duration as
			 * susceptible with infectious) for invasion request to Afflicted so
			 * its Afflictor can execute invasion, depending on how "healthy
			 * individual i accumulates 'exposure to infection' at a rate equal
			 * to the number of infected individuals present" (Sellke, 1983
			 * p.391), e.g. considering absolute v relative infectious
			 * occupancy, immune system tolerance, co-morbidity, ...)
			 */
			@Override
			public SimpleSEIR create( final Comparable<?> name )
			{
				final SimpleSEIR seir = this.actors.create( name )
						.proxyAs( SimpleSEIR.class )
						.with( INFECTION_RESISTANCE_KEY,
								getOrCreate( INFECTION_RESISTANCE_KEY ) )
						.specialist();
//				seir.emit( Transmission.class, FactKind.REQUESTED )
//						.subscribe( rq -> seir.onRequest( rq ) );
				return seir;
			}

//			@Override
//			public void infect()
//			{
//				if( !getCompartment().isSusceptible() )
//					throw ExceptionFactory.createUnchecked(
//							"Can't become exposed when: {}", getCompartment() );
//
//				setCompartment( EpidemicCompartment.Simple.EXPOSED );
//
//				after( disease().drawLatentPeriod() )
//						.call( this::setCompartment,
//								EpidemicCompartment.Simple.INFECTIVE )
//						.thenAfter( disease().drawRecoverPeriod() )
//						.call( this::setCompartment,
//								EpidemicCompartment.Simple.RECOVERED )
//						.thenAfter( disease().drawWanePeriod() )
//						.call( this::setCompartment,
//								EpidemicCompartment.Simple.SUSCEPTIBLE );
//				after( disease().drawOnsetPeriod() )
//						.call( this::setSymptomPhase, ClinicalPhase.SYSTEMIC )
//						.thenAfter( disease().drawSymptomPeriod() )
//						.call( this::setSymptomPhase, ClinicalPhase.ASYMPTOMATIC );
//			}

			//
//			@Override
//			public void afflict( final Afflicted person )
//			{
//				conditionOf( person ).set( EpidemicCompartment.Simple.EXPOSED );
//				after( this.latentPeriodDist.draw() ).call( () -> shed( person ) );
//				after( this.incubationPeriodDist.draw() )
//						.call( () -> systemic( person ) );
//			}
			//
//			public void shed( final Afflicted person )
//			{
//				conditionOf( person ).set( EpidemicCompartment.Simple.INFECTIVE );
//				after( this.recoverPeriodDist.draw() )
//						.call( () -> immune( person ) );
//			}
			//
//			public void immune( final Afflicted person )
//			{
//				conditionOf( person ).set( EpidemicCompartment.Simple.RECOVERED );
//				after( this.wanePeriodDist.draw() ).call( () -> wane( person ) );
//			}
			//
//			public void wane( final Afflicted person )
//			{
//				conditionOf( person ).set( EpidemicCompartment.Simple.SUSCEPTIBLE );
//			}
			//
//			public void systemic( final Afflicted person )
//			{
//				conditionOf( person ).set( ClinicalPhase.SYSTEMIC );
//				after( this.symptomaticPeriodDist.draw() )
//						.call( () -> heal( person ) );
//			}
			//
//			public void heal( final Afflicted person )
//			{
//				conditionOf( person ).set( ClinicalPhase.ASYMPTOMATIC );
//			}
		}
	}
//	class SimpleSEIR extends Identified.SimpleOrdinal<ID> implements Pathogen
//	{
//		private final Scheduler scheduler;
//		private final Condition.Factory conditionFactory;
////		private final ProbabilityDistribution<Quantity<Frequency>> forceDist;
//
//		private final ProbabilityDistribution<Duration> latentPeriodDist;
//		private final ProbabilityDistribution<Duration> recoverPeriodDist;
//		private final ProbabilityDistribution<Duration> wanePeriodDist;
//		private final ProbabilityDistribution<Duration> incubationPeriodDist;
//		private final ProbabilityDistribution<Duration> symptomaticPeriodDist;
//
//		public SimpleSEIR( final ID id, final Scheduler scheduler,
//			final Condition.Factory conditionFactory,
////			final Quantity<Frequency> forceConst,
//			final Duration latentPeriodConst, final Duration recoverPeriodConst,
//			final Duration wanePeriodConst, final Duration onsetPeriodConst,
//			final Duration symptomaticPeriodConst )
//		{
//			this( id, scheduler, conditionFactory,
////					ProbabilityDistribution.createDeterministic( forceConst ),
//					ProbabilityDistribution
//							.createDeterministic( latentPeriodConst ),
//					ProbabilityDistribution
//							.createDeterministic( recoverPeriodConst ),
//					ProbabilityDistribution
//							.createDeterministic( wanePeriodConst ),
//					ProbabilityDistribution
//							.createDeterministic( onsetPeriodConst ),
//					ProbabilityDistribution
//							.createDeterministic( symptomaticPeriodConst ) );
//		}
//
//		public SimpleSEIR( final ID id, final Scheduler scheduler,
//			final Condition.Factory conditionFactory,
////			final ProbabilityDistribution<Quantity<Frequency>> forceDist,
//			final ProbabilityDistribution<Duration> latentPeriodDist,
//			final ProbabilityDistribution<Duration> recoverPeriodDist,
//			final ProbabilityDistribution<Duration> wanePeriodDist,
//			final ProbabilityDistribution<Duration> incubationPeriodDist,
//			final ProbabilityDistribution<Duration> symptomaticPeriodDist )
//		{
//			this.id = id;
//			this.scheduler = scheduler;
//			this.conditionFactory = conditionFactory;
////			this.forceDist = forceDist;
//			this.latentPeriodDist = latentPeriodDist;
//			this.recoverPeriodDist = recoverPeriodDist;
//			this.wanePeriodDist = wanePeriodDist;
//			this.incubationPeriodDist = incubationPeriodDist;
//			this.symptomaticPeriodDist = symptomaticPeriodDist;
//		}
//
////		@Override
////		public Quantity<Frequency> getForceOfInfection(
////			final Collection<TransmissionRoute> routes,
////			final Collection<ContactIntensity> infectionPressure )
////		{
////			Quantity<Frequency> result = QuantityUtil.valueOf( 0,
////					TimeUnits.DAILY );
////			Quantity<Frequency> force = this.forceDist.draw();
////			for( ContactIntensity intensity : infectionPressure )
////				result = result.add( force.multiply( intensity.getFactor() )
////						.asType( Frequency.class ) );
////			return result;
////		}
//
//		@Override
//		public Scheduler scheduler()
//		{
//			return this.scheduler;
//		}
//
//		private Condition conditionOf( final Afflicted person )
//		{
//			return person.afflictions().computeIfAbsent( id(),
//					key -> this.conditionFactory.create( person, this ) );
//		}
//
//	}
}
