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
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Time;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.ujmp.core.enums.ValueType;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.MatrixBuilder;
import io.coala.math.QuantityUtil;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Instant;
import io.coala.time.Proactive.Infiniterator;
import io.coala.time.Scheduler;
import io.coala.time.TimeQuantityConverter;
import io.coala.time.TimeUnits;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.episim.model.disease.infection.EpiTransition;
import nl.rivm.cib.episim.model.disease.infection.InfectionPressure;

/**
 * {@link IllnessTrajectoryTest} tests {@link IllnessTrajectory}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class IllnessTrajectoryTest
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( IllnessTrajectoryTest.class );

	public enum EpiPop
	{
		/** maternal/passive immune */
		M,
		/** susceptibles */
		S,
		/** exposed */
		E,
		/** infectives */
		I,
		/** recovered */
		R;
	}

	/**
	 * {@link EpiRate}
	 */
	public enum EpiRate
	{
		/** natural waning */
		alpha,
		/** contact/infection */
		beta,
		/** recovery */
		gamma,
		/** maternal waning */
		delta,
		/** birth/immigration among susceptibles */
		im_S,
		/** birth/immigration among infectives */
		im_I,
		/** birth/immigration among recovered/immune */
		im_R,
		/** mortality/emigration among susceptibles */
		ex_S,
		/** mortality/emigration among infectives */
		ex_I,
		/** mortality/emigration among recovered */
		ex_R;
	}

	/**
	 * {@link EpiPeriod}
	 */
	public interface EpiConfig extends Config
	{
		String SUSCEPTIBLES_KEY = "susceptibles-count";

		String INFECTIVES_KEY = "infectives-count";

		String RECOVERED_KEY = "recovered-count";

		String REPRODUCTION_KEY = "reproduction-ratio";

		String INFECTION_KEY = "infection-period";

		String WANING_KEY = "";

		int POPULATION_DEFAULT = 1000;

		/** @return initial number of susceptibles */
		@Key( SUSCEPTIBLES_KEY )
		@DefaultValue( "" + (POPULATION_DEFAULT - 1) )
		Long susceptibles();

		/** @return initial number of infectives */
		@Key( INFECTIVES_KEY )
		@DefaultValue( "" + 1 )
		Long infectives();

		/** @return initial number of recovered */
		@Key( RECOVERED_KEY )
		@DefaultValue( "" + 0 )
		Long recovered();

		default Map<EpiPop, Long> population()
		{
			return MapBuilder.ordered( EpiPop.class, Long.class )
					.fill( EpiPop.values(), 0L ).put( EpiPop.S, susceptibles() )
					.put( EpiPop.I, infectives() ).put( EpiPop.R, recovered() )
					.build();
		}

		@DefaultValue( 1 + " " + TimeUnits.HOURS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> dt();

		/**
		 * @return <em>R</em><sub>0</sub>: basic reproduction ratio (eg.
		 *         {@link #growth} vs. {@link #recovery}, >1 : epidemic)
		 */
		@Key( REPRODUCTION_KEY )
		@DefaultValue( "" + 14 )
		BigDecimal reproduction();

		/**
		 * @return mean period to secondary infectiveness (eg.
		 *         &gamma;<sup>-1</sup> in: <em>R &larr; &gamma; &middot;
		 *         I</em>, or (&gamma;<sup>-1</sup> + &kappa;<sup>-1</sup>) in:
		 *         <em>R &larr; &gamma; &middot; I &larr; &kappa; &middot;
		 *         E</em>)
		 */
//		@DefaultValue( 12 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> generation();

		/**
		 * @return &beta;<sup>-1</sup>: mean period of contact (in <em>E &larr;
		 *         &beta; &middot; S</em>)
		 */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> contact();

		/**
		 * @return &beta;<sup>-1</sup> or &kappa;<sup>-1</sup>: mean period of
		 *         infection (in <em>I &larr; &beta; &middot; S</em>) or
		 *         incubation (in <em>I &larr; &kappa; &middot; E</em>)
		 */
		@Key( INFECTION_KEY )
		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> infection();

		/**
		 * @return &gamma;<sup>-1</sup>: mean period of recovery (in <em>R
		 *         &larr; &gamma; &middot; I</em>)
		 */
		@DefaultValue( 12 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> recovery();

		/** @return &nu;<sup>-1</sup>: mean period of growth/birth */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> birth();

		/**
		 * @return &mu;<sup>-1</sup>: mean period of death/mortality (incl.
		 *         fatality)
		 */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> death();

		/** @return mean period of import/immigration */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> immigration();

		/** @return mean period of export/emigration */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> emigration();

		/**
		 * @return &rho;<sup>-1</sup>: mean period of vaccination (acceptance in
		 *         NL)
		 */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> vaccination();

		/**
		 * @return &alpha;<sup>-1</sup>: mean period of
		 *         natural/acquired/maternal immunity waning
		 */
		@Key( WANING_KEY )
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
		@ConverterClass( TimeQuantityConverter.class )
		Quantity<Time> waning();
	}

	@Test
	public void testSIR()
	{
		final EpiConfig conf = ConfigFactory.create( EpiConfig.class );

		// basic reproduction ratio (dimensionless) = beta/gamma
		final Quantity<Time> recovery = conf.recovery(),
				dt = conf.dt().to( recovery.getUnit() );
		final Unit<Time> unit_t = recovery.getUnit();
//		final Unit<Frequency> unit_f = unit_t.inverse()
//				.asType( Frequency.class );
		final Map<EpiPop, Long> pop = conf.population();
		LOG.trace( "Population: {}", pop );
		final BigDecimal R_0 = conf.reproduction(),
				gamma = DecimalUtil.inverse(
						QuantityUtil.toBigDecimal( recovery, unit_t ) ),
				beta = gamma.multiply( R_0 );
		final int i_c = EpiPop.values().length, i_n = i_c + 2,
				i_S = EpiPop.S.ordinal(), i_I = EpiPop.I.ordinal(),
				i_R = EpiPop.R.ordinal(), i_SI = i_n - 2, i_N = i_n - 1;
		// initialize population structure
		final MatrixBuilder M_rate = MatrixBuilder
				.sparse( ValueType.BIGDECIMAL, i_n, i_c )
				.label( "Rates", "t", "t+1" ).label( i_S, "S_t", "S_t+1" )
				.label( i_I, "I_t", "I_t+1" ).label( i_R, "R_t", "R_t+1" )
				.label( i_SI, "SI/N_t" ).label( i_N, "N_t" )

				// infection: beta * SI/N, subtract from i_S, add to I
				.subtract( beta, i_SI, i_S ).add( beta, i_SI, i_I )
				// recovery: gamma * I, subtract from I, add to R
				.subtract( gamma, i_I, i_I ).add( gamma, i_I, i_R ),
				M_pop = MatrixBuilder.sparse( ValueType.BIGDECIMAL, 1, i_c )
						// set compartment values
						.withContent( pop ),
				M_terms = MatrixBuilder.sparse( ValueType.BIGDECIMAL, 1, i_n )
						.labelColumns( EpiPop.values() ).label( i_SI, "SI/N" )
						.label( i_N, "N" );

		LOG.trace(
				"Euler's number:\n\t{} (Math)\n\t{} (Decimal)\n\t{} (precision: 20)",
				Math.E, DecimalUtil.E, DecimalUtil.euler( 20 ) );

		final BigDecimal dt_ratio = QuantityUtil.toBigDecimal( dt, unit_t );
		LOG.trace(
				"R_0: {}, recovery: {}, beta: {}/{}, gamma: {}/{}, t/dt: {}, M_rate:\n{}",
				R_0, recovery, beta, unit_t, gamma, unit_t, dt_ratio, M_rate );

		LOG.trace( "M_pop(0)\n{} ", M_pop.label( "Pop(t=0)" ) );

		BigDecimal N_t, SI_t, E = DecimalUtil.euler( 20 );
		for( int t = 0; t < 24; t++ )
		{
			N_t = M_pop.calcSum();
			SI_t = DecimalUtil.divide(
					M_pop.getNumber( i_I ).multiply( M_pop.getNumber( i_S ) ),
					N_t );

			final MatrixBuilder M_delta = M_terms.withContent( M_pop )
					.with( SI_t, i_SI ).with( N_t, i_N ).mtimes( M_rate );
//			LOG.trace( "M_terms({}) x M_rate = M_delta:\n{}", t, M_delta );
			M_delta.withAvailableNumbers( ( x, v ) -> BigDecimal.ONE.subtract(
					DecimalUtil.pow( E, v.negate().multiply( dt_ratio ) ) ) );
//			LOG.trace( "1-e(-M_delta({})) = M_delta_dt:\n{}", t, M_delta );
			LOG.trace( "M_terms({}) x M_rate = M_pop({}):\n{} ", t, t + 1,
					M_pop.label( "Pop(t=" + (t + 1) + ")" ).add( M_delta ) );
		}
		//                      S          E          I          R
		// deterministic -----------------------------------------
		// t=1    : NaN   997.8345        NaN     2.0822     0.0833
		// t=2    : NaN   995.4106        NaN     4.3326     0.2568
		// t=10   : NaN   186.0701        NaN   722.1404    91.7896
		// t=100  : NaN     0.0000        NaN     0.3687   999.6313

		// t=24/24: NaN   996.9208        NaN     2.7702     0.1417
	}

	@Test
	public void testPathogen()
	{
		final LocalConfig config = new LocalConfig.JsonBuilder().withId( "vir" )
				.withProvider( Scheduler.class, Dsol3Scheduler.class ).build();
		LOG.info( "start {} with binder config: {}", getClass().getSimpleName(),
				config );

		final LocalBinder binder = config
				.createBinder(
						MapBuilder.<Class<?>, Object>unordered()
								.put( ProbabilityDistribution.Parser.class,
										new DistributionParser( null ) )
								.build() );
		final Scheduler scheduler = binder.inject( Scheduler.class );
		final Subject<Long> pressure = PublishSubject.create();
		final Observable<Instant> T_p = Observable
				.fromArray( 1, 2, 3, 4, 5, 6, 7 )
				.map( t -> Instant.of( t, TimeUnits.DAYS ) );
		final BehaviorSubject<BigDecimal> S = BehaviorSubject
				.createDefault( BigDecimal.valueOf( 1000 ) ),
				I = BehaviorSubject.createDefault( BigDecimal.ONE ),
				R = BehaviorSubject.createDefault( BigDecimal.ZERO ),
				N = BehaviorSubject.createDefault(
						S.getValue().add( I.getValue() ).add( R.getValue() ) );
		S.zipWith( I, BigDecimal::add ).zipWith( R, BigDecimal::add )
				.subscribe( N );

		scheduler.onReset( T ->
		{

			// time step duration
			final Quantity<Time> dt = QuantityUtil.valueOf( 1, TimeUnits.DAY );
//			final Unit<Time> step = TimeUnits.DAY ;
			// growth/birth
//			final Quantity<Frequency> dt_inv = dt.inverse()
//					.asType( Frequency.class );
//			final Unit<Frequency> t_inv = dt_inv.getUnit();
			// growth/birth
//			final Quantity<Frequency> b = QuantityUtil.valueOf( 0, t_inv );
			// mortality/death
//			final Quantity<Frequency> d = QuantityUtil.valueOf( 0, t_inv );
			// immunity wane
//			final Quantity<Frequency> alpha = QuantityUtil.valueOf( 0, t_inv );

			// GIVENS:
			// basic reproduction ratio = beta/gamma
			final BigDecimal R_0 = BigDecimal.valueOf( 15 );
			// mean period of infection/recovery 
			final Quantity<Time> recovery = dt.multiply( 12 );
			// mean period of vaccination (acceptance in NL)
//			final Quantity<Time> vaccination = dt.multiply( 12 );

			// infectious/recovery rate
			final Quantity<Frequency> gamma = QuantityUtil.inverse( recovery )
					.asType( Frequency.class );
			// contact
			final Quantity<Frequency> beta = gamma.multiply( R_0 );
			// passive/waning
//			final Quantity<Frequency> delta = QuantityUtil.valueOf( .1, t_inv );
			// vaccination
//			final Quantity<Frequency> rho = QuantityUtil.inverse( vaccination )
//					.asType( Frequency.class );
			T.atEach( () -> (Infiniterator) () -> T.now().add( dt ), t ->
			{
				final BigDecimal s_0 = S.getValue(), i_0 = I.getValue(),
						r_0 = R.getValue(), n = N.getValue(),
						si = QuantityUtil.toBigDecimal( beta.multiply( s_0 )
								.multiply( i_0 ).divide( n ).multiply( dt ) ),
						ir = QuantityUtil.toBigDecimal(
								gamma.multiply( i_0 ).multiply( dt ) );
				LOG.trace( "S[{}]--({})->I[{}]--({})->R[{}] (n={})",
						DecimalUtil.round( s_0 ), DecimalUtil.toScale( si, 2 ),
						DecimalUtil.round( i_0 ), DecimalUtil.toScale( ir, 2 ),
						DecimalUtil.round( r_0 ), DecimalUtil.round( n ) );
				S.onNext( s_0.add( si ) );
				I.onNext( i_0.add( si ).subtract( ir ) );
				R.onNext( r_0.add( ir ) );
			} );

			final InfectionPressure.Factory pathogens = binder
					.inject( InfectionPressure.Factory.SimpleBinding.class );
			final InfectionPressure trajectory = pathogens.create( JsonUtil
					.getJOM().createObjectNode()//
					.put( InfectionPressure.DECAY_TYPE_KEY,
							InfectionPressure.Decay.ProportionalDecay.class
									.getName() )
					.put( EpiTransition.LATENCY.jsonKey(), "const(.5 week)" )
					.put( EpiTransition.WANING.jsonKey(), "const(.5 year)" )
			//
			);
			LOG.trace( "t={}, init pathogen: {}", T.now(), trajectory );

			// have pathogen subscribe to an individual's infection pressure
			pressure.doOnNext( p -> LOG.trace( "t={}, p={}", T.now(), p ) )
					.subscribe( trajectory );
			trajectory.emitCondition().subscribe(
					c -> LOG.trace( "t={}, trajectory @ {}", T.now(), c ),
					Exceptions::propagate );

			// add infectious scenario
			final AtomicLong p = new AtomicLong();
			T.atEach( T_p, t -> pressure.onNext( p.incrementAndGet() ) );

			LOG.trace( "t={}, initialized", T.now() );
		} );

		scheduler.run();

		LOG.info( "completed {}", getClass().getSimpleName() );
	}
}
