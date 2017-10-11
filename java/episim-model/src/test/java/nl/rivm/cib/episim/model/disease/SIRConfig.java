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

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.util.Collections;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;
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
import io.reactivex.subjects.BehaviorSubject;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Transition;

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

	default Observable<Map.Entry<Double, double[]>>
		deterministic( final Supplier<FirstOrderIntegrator> integrators )
	{
		return Observable.create( sub ->
		{
			final double gamma = 1. / recovery();
			final double beta = gamma * reproduction();
			final double[] y0 = Arrays.stream( population() )
					.mapToDouble( n -> n ).toArray();
			final double[] t = t();

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

	default Observable<Entry<Double, long[]>> stochasticGillespie()
	{
		return stochasticGillespie( 0d );
	}

	default Observable<Entry<Double, long[]>>
		stochasticGillespie( final double maxDt )
	{
		return Observable.create( sub ->
		{
			final double gamma = 1. / recovery();
			final double beta = gamma * reproduction();
			final long[] y = population();
			final double[] T = t();
			final double dt = Double.isFinite( maxDt ) && maxDt > 0 ? maxDt
					: T[1];

			final Long seed = seed();
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

	default Observable<Entry<Double, long[]>> stochasticSellke()
	{
		return stochasticSellke( 0d );
	}

	default Observable<Entry<Double, long[]>>
		stochasticSellke( final double maxDt )
	{
		return Observable.create( sub ->
		{
			final double beta = reproduction() / recovery();
			final long[] y = population();
			final double[] T = t();
			final double dt = Double.isFinite( maxDt ) && maxDt > 0 ? maxDt
					: T[1];

			final Long seed = seed();
			final RandomGenerator rng = new MersenneTwister(
					seed == null ? System.currentTimeMillis() : seed );

			final ExponentialDistribution resistanceDist = new ExponentialDistribution(
					rng, 1 ),
					recoverDist = new ExponentialDistribution( rng,
							recovery() );

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

	default Observable<Entry<Double, long[]>> stochasticLocal()
	{
		return stochasticLocal( 0d );
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
	default Observable<Entry<Double, long[]>>
		stochasticLocal( final double maxDt )
	{

		return Observable.create( sub ->
		{
//			final double gamma = 1. / recovery();
//			final double beta = gamma * reproduction();
			final long[] y = population();
			final double[] T = t();
//			final double dt = Double.isFinite( maxDt ) && maxDt > 0 ? maxDt
//					: T[1];

			final ObjectNode sirConfig = JsonUtil.getJOM().createObjectNode()//
//					.put( InfectionPressure.DECAY_TYPE_KEY,
//					InfectionPressure.Decay.ConditionalDecay.class
//							.getName() )
					.put( MSEIRS.Transition.SUSCEPTIBILITY.stringify(),
							String.format( "exp(%s day)", reproduction() ) )
					.put( MSEIRS.Transition.INFECTIOUS.stringify(),
							String.format( "exp(%s day)", recovery() ) )
					.put( MSEIRS.Transition.WANING_NATURAL.stringify(),
							String.format( "const(%s day)", T[1] ) );
			final LocalConfig binderConfig = new LocalConfig.JsonBuilder()
					.withId( "vir" )
//					.withProvider( Scheduler.Factory.class,
//							Scheduler.Factory.Rebinder.class )
					.withProvider( Scheduler.class, Dsol3Scheduler.class )
					.withProvider( PseudoRandom.Factory.class,
							Math3PseudoRandom.MersenneTwisterFactory.class )
					.withProvider( ProbabilityDistribution.Factory.class,
							Math3ProbabilityDistribution.Factory.class )
					.withProvider( ProbabilityDistribution.Parser.class,
							DistributionParser.class )
					.withProvider( MSEIRS.Broker.class,
							MSEIRS.Broker.SimpleDefault.class, sirConfig )
					.build();

//			final Long seed = seed();
//			final RandomGenerator rng = new MersenneTwister(
//					seed == null ? System.currentTimeMillis() : seed );

			final LocalBinder binder = binderConfig.createBinder();
			final SchedulerConfig config = SchedulerConfig
					.getOrCreate( MapBuilder.unordered()
							.put( SchedulerConfig.DURATION_KEY, "" + T[1] )
							.build() );
			final Transition si = Transition.SUSCEPTIBILITY,
					ir = Transition.INFECTIOUS;
			binder.inject( Scheduler.class, config.toJSON() ).run( s ->
			{
				publishCopy( sub, s.now().decimal().doubleValue(), y );

				final long subpopSize = y[0] + y[1], initialSusceptible = y[0];
				final BehaviorSubject<Double> pressure = BehaviorSubject
						.createDefault( DecimalUtil.divide( y[1], subpopSize )
								.doubleValue() );
				final MSEIRS.Broker pathogen = binder
						.inject( MSEIRS.Broker.class );
				Observable.rangeLong( 0L, subpopSize )
						.flatMap( i -> pathogen
								.create( i < initialSusceptible ? si : ir )
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
								pressure.onNext(
										DecimalUtil.divide( y[1], subpopSize )
												.doubleValue() );
								publishCopy( sub,
										s.now().decimal().doubleValue(), y );
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
}