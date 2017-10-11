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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.util.Collections;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.MatrixBuilder;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Scheduler;
import io.coala.time.SchedulerConfig;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import net.jodah.concurrentunit.Waiter;
import nl.rivm.cib.episim.model.disease.IllnessTrajectory;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Transition;

/**
 * {@link MSEIRSTest} tests {@link IllnessTrajectory}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class MSEIRSTest
{
	/** */
	static final Logger LOG = LogUtil.getLogger( MSEIRSTest.class );

	@Test
	public void compareMSEIRS() throws TimeoutException
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
		deterministic( conf, () -> new DormandPrince853Integrator( 1.0E-8, 10,
				1.0E-20, 1.0E-20 ) ).blockingForEach( yt -> result.withContent(
						Arrays.stream( yt.getValue() ).mapToObj( d -> d ),
						DecimalUtil.ceil( yt.getKey() ).longValue(), 0 * C ) );

		final int n = 10;

		final Waiter waiter = new Waiter();

		LOG.trace( "Running Gillespie x {}...", n );
		// see https://www.wikiwand.com/en/Gillespie_algorithm
		averages( () -> stochasticGillespie( conf, 1d ),
				d -> DecimalUtil.ceil( d ).longValue(), n )
						.forEach( stats -> result.withContent( stats.getValue(),
								stats.getKey(), 1 * C ) );

		LOG.trace( "Running Sellke x {}...", n );
		// see e.g. Cook et al. 2008, equation 3.5: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3227033/#__sec2title
		averages( () -> stochasticSellke( conf, 1d ),
				d -> DecimalUtil.ceil( d ).longValue(), n )
						.forEach( stats -> result.withContent( stats.getValue(),
								stats.getKey(), 2 * C ) );

		LOG.trace( "Running Epidemes x {}...", 1 );
		averages( () -> stochasticLocal( conf, 1d ),
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

	static void publishCopy(
		final ObservableEmitter<Entry<Double, double[]>> sub, final double t,
		final double[] y )
	{
		sub.onNext( Collections.entry( t, Arrays.copyOf( y, y.length ) ) );
	}

	static void publishCopy( final ObservableEmitter<Entry<Double, long[]>> sub,
		final double t, final long[] y )
	{
		sub.onNext( Collections.entry( t, Arrays.copyOf( y, y.length ) ) );
	}

	/**
	 * {@link SIRConfig} used in {@link MSEIRSTest}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public interface SIRConfig extends Mutable
	{
		@DefaultValue( "" + 12 )
		double reproduction();

		@DefaultValue( "" + 14 )
		double recovery();

		@Separator( "," )
		@DefaultValue( "50,1,0" )
		long[] population();

		@DefaultValue( "0,40" )
		double[] t();

		@DefaultValue( "ORANGE,RED,GREEN" )
		String[] colors();

		@DefaultValue( "WHITE,PURPLE,BLUE" )
		String[] colors2();

		Long seed();
	}

	public static Observable<Map.Entry<Double, double[]>> deterministic(
		final SIRConfig config,
		final Supplier<FirstOrderIntegrator> integrators )
	{
		return Observable.create( sub ->
		{
			final double gamma = 1. / config.recovery();
			final double beta = gamma * config.reproduction();
			final double[] y0 = Arrays.stream( config.population() )
					.mapToDouble( n -> n ).toArray();
			final double[] t = config.t();

			try
			{
				final FirstOrderIntegrator integrator = integrators.get();

				integrator.addStepHandler( new StepHandler()
				{
					@Override
					public void init( final double t0, final double[] y0,
						final double t )
					{
						publishCopy( sub, t0, y0 );
					}

					@Override
					public void handleStep( final StepInterpolator interpolator,
						final boolean isLast ) throws MaxCountExceededException
					{
						publishCopy( sub, interpolator.getInterpolatedTime(),
								interpolator.getInterpolatedState() );
						if( isLast ) sub.onComplete();
					}
				} );

				integrator.integrate( new FirstOrderDifferentialEquations()
				{
					@Override
					public int getDimension()
					{
						return y0.length;
					}

					@Override
					public void computeDerivatives( final double t,
						final double[] y, final double[] yp )
					{
						// SIR terms (flow rates)
						final double n = y[0] + y[1] + y[2],
								flow_si = beta * y[0] * y[1] / n,
								flow_ir = gamma * y[1];

						yp[0] = -flow_si;
						yp[1] = flow_si - flow_ir;
						yp[2] = flow_ir;
					}
				}, t[0], y0, t[1], y0 );
			} catch( final Exception e )
			{
				sub.onError( e );
			}
		} );
	}

	public static Observable<Entry<Double, long[]>>
		stochasticGillespie( final SIRConfig config )
	{
		return stochasticGillespie( config, 0d );
	}

	public static Observable<Entry<Double, long[]>>
		stochasticGillespie( final SIRConfig config, final double maxDt )
	{
		return Observable.create( sub ->
		{
			final double gamma = 1. / config.recovery();
			final double beta = gamma * config.reproduction();
			final long[] y = config.population();
			final double[] T = config.t();
			final double dt = Double.isFinite( maxDt ) && maxDt > 0 ? maxDt
					: T[1];

			final Long seed = config.seed();
			final RandomGenerator rng = new MersenneTwister(
					seed == null ? System.currentTimeMillis() : seed );

			for( double t = T[0]; t < T[1]; )
			{
				publishCopy( sub, t, y );

				// SIR terms (flow rates)
				final double n = y[0] + y[1] + y[2],
						flow_si = beta * y[0] * y[1] / n,
						flow_ir = gamma * y[1], flow_sum = flow_si + flow_ir;

				final double t2 = t
						+ new ExponentialDistribution( rng, 1d / flow_sum )
								.sample();

				// publish intermediate values
				for( double t1 = Math.min( t2, t + dt ), tMax = Math.min( T[1],
						t2 ); t1 < tMax; t1 += dt )
					publishCopy( sub, t1, y );

				// advance time to next event
				t = t2;

				// determine event (s->i, i->r, ...)
				if( rng.nextDouble() < flow_si / flow_sum )
				{
					y[0]--; // from S
					y[1]++; // to I
				} else
				{
					y[1]--; // from I
					y[2]++; // to R
					if( y[0] != 0 && y[1] == 0 )
					{
						y[0]--; // from S
						y[1]++; // to I
					}
				}
			}
			sub.onComplete();
		} );
	}

	public static Observable<Entry<Double, long[]>>
		stochasticSellke( final SIRConfig config )
	{
		return stochasticSellke( config, 0d );
	}

	public static Observable<Entry<Double, long[]>>
		stochasticSellke( final SIRConfig config, final double maxDt )
	{
		return Observable.create( sub ->
		{
			final double beta = config.reproduction() / config.recovery();
			final long[] y = config.population();
			final double[] T = config.t();
			final double dt = Double.isFinite( maxDt ) && maxDt > 0 ? maxDt
					: T[1];

			final Long seed = config.seed();
			final RandomGenerator rng = new MersenneTwister(
					seed == null ? System.currentTimeMillis() : seed );

			final ExponentialDistribution resistanceDist = new ExponentialDistribution(
					rng, 1 ),
					recoverDist = new ExponentialDistribution( rng,
							config.recovery() );

			// pending infections (mapping resistance -> amount)
			final TreeMap<Double, Integer> tInfect = IntStream
					.range( 0, (int) y[0] )
					.mapToObj( i -> resistanceDist.sample() )
					.collect( Collectors.toMap( r -> r, r -> 1, Integer::sum,
							TreeMap::new ) );
			// pending recoveries (mapping time -> amount)
			final TreeMap<Double, Integer> tRecover = new TreeMap<>();

			double cumPres = 0;
			// Re-initialize infectives as susceptibles with zero resistance
			tInfect.put( cumPres, (int) y[1] );
			y[0] += y[1]; // I -> S
			y[1] -= y[1]; // I -> 0
			for( double t = T[0]; t < T[1]; )
			{
				publishCopy( sub, t, y );
				final long localPopSize = y[0] + y[1] + y[2];
				final Double ri = tInfect.isEmpty() ? null : tInfect.firstKey(),
						ti = ri == null ? null :
				// now + remaining resistance per relative pressure
				t + (ri - cumPres)
						/ (beta * Math.max( y[1], 1 ) / localPopSize),
						tr = tRecover.isEmpty() ? null : tRecover.firstKey();

				// time of next infection is earliest
				if( ti != null && (tr == null || ti < tr) )
				{
					final int ni = tInfect.remove( ri );
					cumPres = ri;

					// publish intermediate values
					for( double t1 = Math.min( ti, t + dt ), tMax = Math
							.min( T[1], ti ); t1 < tMax; t1 += dt )
						publishCopy( sub, t1, y );

					// infect
					t = ti;
					y[0] -= ni; // from S
					y[1] += ni; // to I

					// schedule S_t recoveries at t+Exp(1/gamma)
					for( int i = 0; i < ni; i++ )
						tRecover.compute( t + recoverDist.sample(),
								( k, v ) -> v == null ? 1 : v + 1 );
				}
				// time of next recovery is earliest
				else if( tr != null )
				{
					final int nr = tRecover.remove( tr );
					if( ri != null )
						// advance cumulative pressure by dt * relative pressure
						cumPres += (tr - t) * beta * y[1] / localPopSize;

					// publish intermediate values
					for( double t1 = Math.min( tr, t + dt ), tMax = Math
							.min( T[1], tr ); t1 < tMax; t1 += dt )
						publishCopy( sub, t1, y );

					// recover
					t = tr;
					y[1] -= nr; // from I
					y[2] += nr; // to R
				}
				// no events remaining
				else
				{
					// publish intermediate values
					for( double t1 = t + dt; t1 < T[1]; t1 += dt )
						publishCopy( sub, t1, y );

					// time ends
					break;
				}
			}
			sub.onComplete();
		} );
	}

	public static Observable<Entry<Double, long[]>>
		stochasticLocal( final SIRConfig config )
	{
		return stochasticLocal( config, 0d );
	}

	/**
	 * TODO extend Sellke with Gillespie-approach:
	 * <li>maintain ordered mapping of occupancy-amounts to room-ids, e.g. <br>
	 * roomPressure <- Map<Integer, List<ID>> <br>
	 * totalPressure <- roomPressure.eachEntry.map((n,ids)->n*ids.size()).sum
	 * <br>
	 * Observable<Change<ID,Integer,Integer>>.sub(upd -> <br>
	 * {rooms.get(upd.oldPressure).remove(upd.id); <br>
	 * rooms.get(upd.newPressure).add(upd.id);}
	 * <li>when next infection is due (according to pressure/resistance
	 * dynamics) attribute infection uniformly to room at that sorted position:
	 * <br>
	 * idRnd = floor(rnd.draw()*totalRooms), cumPressure = 0 <br>
	 * roomPressure.eachEntry((n,ids)->{ <br>
	 * curPressure = n*ids.size(); <br>
	 * if(idRnd < cumPressure) { <br>
	 * scaled = idRnd ids.size() <br>
	 * return ids.get(scaled)} <br>
	 * cumPressure +=curPressure;})
	 * 
	 * @param maxDt
	 * @return
	 */
	public static Observable<Entry<Double, long[]>>
		stochasticLocal( final SIRConfig config, final double maxDt )
	{

		return Observable.create( sub ->
		{
//			final double gamma = 1. / recovery();
//			final double beta = gamma * reproduction();
			final long[] y = config.population();
			final double[] T = config.t();
//			final double dt = Double.isFinite( maxDt ) && maxDt > 0 ? maxDt
//					: T[1];

			final ObjectNode sirConfig = JsonUtil.getJOM().createObjectNode()//
//					.put( InfectionPressure.DECAY_TYPE_KEY,
//					InfectionPressure.Decay.ConditionalDecay.class
//							.getName() )
					.put( MSEIRS.Transition.SUSCEPTIBILITY.stringify(),
							String.format( "exp(%s day)",
									config.reproduction() ) )
					.put( MSEIRS.Transition.INFECTIOUS.stringify(),
							String.format( "exp(%s day)", config.recovery() ) )
					.put( MSEIRS.Transition.WANING_NATURAL.stringify(),
							String.format( "const(%s day)", T[1] ) );
			final LocalConfig binderConfig = new LocalConfig.JsonBuilder()
					.withId( "vir" )
					.withProvider( Scheduler.class, Dsol3Scheduler.class )
//					.withProvider( PseudoRandom.Factory.class,
//							Math3PseudoRandom.MersenneTwisterFactory.class )
//					.withProvider( ProbabilityDistribution.Factory.class,
//							Math3ProbabilityDistribution.Factory.class )
					.withProvider( ProbabilityDistribution.Parser.class,
							DistributionParser.class )
					.withProvider( MSEIRS.Broker.class,
							MSEIRS.Broker.SimpleDefault.class, sirConfig )
					.build();

			final LocalBinder binder = binderConfig
					.createBinder( MapBuilder.<Class<?>, Object>unordered()
							.put( ProbabilityDistribution.Factory.class,
									new Math3ProbabilityDistribution.Factory(
											new Math3PseudoRandom.MersenneTwisterFactory()
													.create( PseudoRandom.Config.NAME_DEFAULT,
															config.seed() ) ) )
							.build() );
			final Transition si = Transition.SUSCEPTIBILITY,
					ir = Transition.INFECTIOUS;
			binder.inject( Scheduler.class,
					SchedulerConfig
							.getOrCreate( MapBuilder.unordered()
									.put( SchedulerConfig.DURATION_KEY,
											"" + T[1] )
									.build() )
							.toJSON() )
					.run( s ->
					{
						publishCopy( sub, s.now().decimal().doubleValue(), y );

						final long subpopSize = y[0] + y[1],
								initialSusceptible = y[0];
						final BehaviorSubject<Double> pressure = BehaviorSubject
								.createDefault(
										DecimalUtil.divide( y[1], subpopSize )
												.doubleValue() );
						final MSEIRS.Broker pathogen = binder
								.inject( MSEIRS.Broker.class );
						Observable.rangeLong( 0L, subpopSize )
								.flatMap(
										i -> pathogen
												.create( i < initialSusceptible
														? si : ir )
												.mseirsEmitter() )
								.subscribe( c ->
								{
									boolean changed = false;
									if( c == si )
									{
										y[0]--;
										y[1]++;
										changed = true;
									} else if( c == ir )
									{
										y[1]--;
										y[2]++;
										changed = true;
									}
									if( changed )
									{
										pressure.onNext( DecimalUtil
												.divide( y[1], subpopSize )
												.doubleValue() );
										publishCopy( sub,
												s.now().decimal().doubleValue(),
												y );
									}
								}, sub::onError );
						for( int i = 0; i < subpopSize; i++ )
						{
//					final int ii = i;

//					final InfectionPressure trajectory = binder
//							.inject( InfectionPressure.Factory.class )
//							.create( sirConfig );
//					pressure.subscribe( trajectory );
//					trajectory.emitCondition()
//							//.map( EpiTrajectory.EpiCondition::getEpiCompartment )
//							.subscribe( c ->
//							{
//								final double t = T[0]
//										+ s.now().decimal().doubleValue();
////								if( t > T[1] )
////								{
////									sub.onComplete();
////									return;
////								}
//								if( c.getEpiCompartment().isInfective() )
//								{
//									y[0]--;
//									y[1]++;
//									pressure.onNext( y[1] );
//									publishCopy( sub, t, y );
//								} else if( !c.getEpiCompartment()
//										.isSusceptible() )
//								{
//									y[1]--;
//									y[2]++;
//									pressure.onNext( y[1] );
//									sub.onNext( Collections.entry( t, y ) );
//								} else
//									sub.onComplete();
//							} );
						}
					} );

			sub.onComplete();
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
