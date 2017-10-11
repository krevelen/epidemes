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
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.util.Collections;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.MatrixBuilder;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;
import net.jodah.concurrentunit.Waiter;

/**
 * {@link IllnessTrajectoryTest} tests {@link IllnessTrajectory}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class IllnessTrajectoryTest
{
	/** */
	static final Logger LOG = LogUtil.getLogger( IllnessTrajectoryTest.class );

	@Test
	public void testSIRIntegration() throws TimeoutException
	{
		final SIRConfig conf = ConfigFactory.create( SIRConfig.class );
		final double[] T = conf.t();
		final long[] pop = conf.population();

		LOG.info( "Starting with SIR: {}, t in {}", pop, T );

		final int t = (int) T[0], N = (int) T[1], C = pop.length;
		final MatrixBuilder result = MatrixBuilder
				.sparse( N - t + 1, pop.length * 4 ).label( "Results" )
				.labelColumns( "~dS.dt", "~dI.dt", "~dR.dt", "S_gil(t)",
						"I_gil(t)", "R_gil(t)", "S_sel(t)", "I_sel(t)",
						"R_sel(t)", "S_dyn(t)", "I_dyn(t)", "R_dyn(t)" )
				.labelRows( i -> "max(t=<" + i + ")" );

		// the Dormand-Prince (embedded Runge-Kutta) ODE integrator
		// see https://www.wikiwand.com/en/Runge%E2%80%93Kutta_methods
		conf.deterministic( () -> new DormandPrince853Integrator( 1.0E-8, 10,
				1.0E-20, 1.0E-20 ) )
				.blockingForEach( yt -> result.withContent(
						Arrays.stream( yt.getValue() ).mapToObj( d -> d ),
						DecimalUtil.ceil( yt.getKey() ).longValue(), 0 * C ) );

		final int n = 10;

		final Waiter waiter = new Waiter();

		LOG.trace( "Running Gillespie x {}...", n );
		// see https://www.wikiwand.com/en/Gillespie_algorithm
		averages( () -> conf.stochasticGillespie( 1d ),
				d -> DecimalUtil.ceil( d ).longValue(), n )
						.forEach( stats -> result.withContent( stats.getValue(),
								stats.getKey(), 1 * C ) );

		LOG.trace( "Running Sellke x {}...", n );
		// see e.g. Cook et al. 2008, equation 3.5: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3227033/#__sec2title
		averages( () -> conf.stochasticSellke( 1d ),
				d -> DecimalUtil.ceil( d ).longValue(), n )
						.forEach( stats -> result.withContent( stats.getValue(),
								stats.getKey(), 2 * C ) );

		LOG.trace( "Running Epidemes x {}...", 1 );
		averages( () -> conf.stochasticLocal( 1d ),
				d -> DecimalUtil.ceil( d ).longValue(), 1 )
						.subscribe(
								stats -> result.withContent( stats.getValue(),
										stats.getKey(), 3 * C ),
								waiter::fail, waiter::resume );

		//waiter.await();

		//		Application.launch( CartesianPlot.class );

		LOG.trace( "result: \n{}", result.build().toString() );
		LOG.info( "Complete" );
	}

	private <T> Observable<Entry<T, Stream<BigDecimal>>> averages(
		final Supplier<Observable<Entry<Double, long[]>>> sir,
		final Function<Double, T> bins, final int n )
	{
		return Observable.create( sub ->
		{
			final NavigableMap<T, long[]> sums = java.util.Collections
					.synchronizedNavigableMap( new TreeMap<T, long[]>() );
			final long t0 = System.currentTimeMillis();
			final AtomicInteger iteration = new AtomicInteger();
			final AtomicLong sysTime = new AtomicLong( t0 );
			Observable.range( 0, n ).flatMap( i -> Observable.just( i )
					.subscribeOn( Schedulers.computation() ).map( ii ->
					{
						final int iii = iteration.incrementAndGet();
						final long t = System.currentTimeMillis();
						sysTime.updateAndGet( t1 ->
						{
							if( t - t1 > 10000 )
							{
								LOG.trace(
										"Progress {}% at ~{}/s, iteration {} of {}",
										DecimalUtil.floor( DecimalUtil
												.divide( iii * 100, n ) ),
										DecimalUtil.round( DecimalUtil
												.divide( iii * 1000, t - t0 ) ),
										iii, n );
								return t;
							}
							return t1;
						} );
						return sir.get()
								// group by bin size
								.groupBy( yt -> bins.apply( yt.getKey() ) )
								// take highest floating point t in this bin
								.flatMap( gr -> gr
										.reduce( ( yt1,
											yt2 ) -> yt1.getKey().compareTo(
													yt2.getKey() ) > 0 ? yt1
															: yt2 )
										.toObservable()
										.map( yt -> Collections.entry(
												gr.getKey(), yt.getValue() ) ) )
								// add to current sums
								.collect( () -> sums, ( sum,
									yt ) -> sum.compute( yt.getKey(), ( k,
										v ) -> v == null ? yt.getValue()
												: IntStream.range( 0, v.length )
														.mapToLong( iv -> v[iv]
																+ yt.getValue()[iv] )
														.toArray() ) )
								.blockingGet();
					} ) ).blockingSubscribe();

			sums.forEach( ( k, v ) -> sub
					.onNext( Collections.entry( k, Arrays.stream( v )
							.mapToObj( y -> DecimalUtil.divide( y, n ) ) ) ) );
			final long dt = System.currentTimeMillis() - t0;
			LOG.trace( "Completed {} iterations in {}s = {}/s", n,
					DecimalUtil.toScale( DecimalUtil.divide( dt, 1000 ), 1 ),
					DecimalUtil.round( DecimalUtil.divide( n * 1000, dt ) ) );
		} );
	}

//	@Ignore
//	@Test
//	public void testPathogen()
//	{
//		final LocalConfig config = new LocalConfig.JsonBuilder().withId( "vir" )
//				.withProvider( Scheduler.class, Dsol3Scheduler.class ).build();
//		LOG.info( "start {} with binder config: {}", getClass().getSimpleName(),
//				config );
//
//		final LocalBinder binder = config
//				.createBinder(
//						MapBuilder.<Class<?>, Object>unordered()
//								.put( ProbabilityDistribution.Parser.class,
//										new DistributionParser( null ) )
//								.build() );
//		final Scheduler scheduler = binder.inject( Scheduler.class );
//		final Subject<Long> pressure = PublishSubject.create();
//		final Observable<Instant> T_p = Observable
//				.fromArray( 1, 2, 3, 4, 5, 6, 7 )
//				.map( t -> Instant.of( t, TimeUnits.DAYS ) );
//		final BehaviorSubject<BigDecimal> S = BehaviorSubject
//				.createDefault( BigDecimal.valueOf( 1000 ) ),
//				I = BehaviorSubject.createDefault( BigDecimal.ONE ),
//				R = BehaviorSubject.createDefault( BigDecimal.ZERO ),
//				N = BehaviorSubject.createDefault(
//						S.getValue().add( I.getValue() ).add( R.getValue() ) );
//		S.zipWith( I, BigDecimal::add ).zipWith( R, BigDecimal::add )
//				.subscribe( N );
//
//		scheduler.onReset( T ->
//		{
//
//			// time step duration
//			final Quantity<Time> dt = QuantityUtil.valueOf( 1, TimeUnits.DAY );
////			final Unit<Time> step = TimeUnits.DAY ;
//			// growth/birth
////			final Quantity<Frequency> dt_inv = dt.inverse()
////					.asType( Frequency.class );
////			final Unit<Frequency> t_inv = dt_inv.getUnit();
//			// growth/birth
////			final Quantity<Frequency> b = QuantityUtil.valueOf( 0, t_inv );
//			// mortality/death
////			final Quantity<Frequency> d = QuantityUtil.valueOf( 0, t_inv );
//			// immunity wane
////			final Quantity<Frequency> alpha = QuantityUtil.valueOf( 0, t_inv );
//
//			// GIVENS:
//			// basic reproduction ratio = beta/gamma
//			final BigDecimal R_0 = BigDecimal.valueOf( 15 );
//			// mean period of infection/recovery 
//			final Quantity<Time> recovery = dt.multiply( 12 );
//			// mean period of vaccination (acceptance in NL)
////			final Quantity<Time> vaccination = dt.multiply( 12 );
//
//			// infectious/recovery rate
//			final Quantity<Frequency> gamma = QuantityUtil.inverse( recovery )
//					.asType( Frequency.class );
//			// contact
//			final Quantity<Frequency> beta = gamma.multiply( R_0 );
//			// passive/waning
////			final Quantity<Frequency> delta = QuantityUtil.valueOf( .1, t_inv );
//			// vaccination
////			final Quantity<Frequency> rho = QuantityUtil.inverse( vaccination )
////					.asType( Frequency.class );
//
//			// forward-Euler method
//			T.atEach( () -> (Infiniterator) () -> T.now().add( dt ), t ->
//			{
//				final BigDecimal s_0 = S.getValue(), i_0 = I.getValue(),
//						r_0 = R.getValue(), n = N.getValue(),
//						si = QuantityUtil.toBigDecimal( beta.multiply( s_0 )
//								.multiply( i_0 ).divide( n ).multiply( dt ) ),
//						ir = QuantityUtil.toBigDecimal(
//								gamma.multiply( i_0 ).multiply( dt ) );
//				LOG.trace( "S[{}]--({})->I[{}]--({})->R[{}] (n={})",
//						DecimalUtil.round( s_0 ), DecimalUtil.toScale( si, 2 ),
//						DecimalUtil.round( i_0 ), DecimalUtil.toScale( ir, 2 ),
//						DecimalUtil.round( r_0 ), DecimalUtil.round( n ) );
//				S.onNext( s_0.add( si ) );
//				I.onNext( i_0.add( si ).subtract( ir ) );
//				R.onNext( r_0.add( ir ) );
//			} );
//
//			final InfectionPressure.Factory pathogens = binder
//					.inject( InfectionPressure.Factory.SimpleBinding.class );
//			final InfectionPressure trajectory = pathogens.create( JsonUtil
//					.getJOM().createObjectNode()//
//					.put( InfectionPressure.DECAY_TYPE_KEY,
//							InfectionPressure.Decay.ProportionalDecay.class
//									.getName() )
//					.put( Period.LATENCY.jsonKey(), "const(.5 week)" )
//					.put( Period.WANING.jsonKey(), "const(.5 year)" )
//			//
//			);
//			LOG.trace( "t={}, init pathogen: {}", T.now(), trajectory );
//
//			// have pathogen subscribe to an individual's infection pressure
//			pressure.doOnNext( p -> LOG.trace( "t={}, p={}", T.now(), p ) )
//					.subscribe( trajectory );
//			trajectory.emitCondition().subscribe(
//					c -> LOG.trace( "t={}, trajectory @ {}", T.now(), c ),
//					Exceptions::propagate );
//
//			// add infectious scenario
//			final AtomicLong p = new AtomicLong();
//			T.atEach( T_p, t -> pressure.onNext( p.incrementAndGet() ) );
//
//			LOG.trace( "t={}, initialized", T.now() );
//		} );
//
//		scheduler.run();
//
//		LOG.info( "completed {}", getClass().getSimpleName() );
//	}

}
