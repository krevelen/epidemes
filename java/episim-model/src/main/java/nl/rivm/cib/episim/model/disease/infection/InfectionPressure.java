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
import java.text.ParseException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.exception.Thrower;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.time.ConditionalSignalQuantifier;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import nl.rivm.cib.episim.model.disease.infection.EpiTrajectory.EpiCondition.SimpleEpiClinical;

/**
 * {@link InfectionPressure} calculates the infection speed or pressure for a
 * susceptible individual given some composition of persons in epidemic states
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
//@FunctionalInterface
public interface InfectionPressure extends EpiTrajectory, Observer<Number>
{
	String DECAY_TYPE_KEY = "decay";

	/** @return {@link Decay} : pressure(t) &harr; resistance(t) */
	Decay decay();

	/**
	 * {@link Decay} defines relations between resistance, pressure and time in
	 * two (inverse) functions:
	 * <ul>
	 * <li>{@link #integrator} for the resistance change given a pressure amount
	 * over some duration; and
	 * <li>{@link #interceptor} for the duration until some resistance level is
	 * first reached given some pressure amount.
	 * </ul>
	 */
	interface Decay
		extends ConditionalSignalQuantifier<Quantity<Time>, BigDecimal>
	{

		/** decay that disregards any pressure signal constants */
		Decay CONSTANT = new ConstantDecay();

		/** resistance decay/change dr=-1*dt (k > 0); else dr=NA */
		Decay CONDITIONAL = new ConditionalDecay();

		/** resistance decay/change dr=-k*dt (k > 0); else dr=NA */
		Decay PROPORTIONAL = new ProportionalDecay();

		/**
		 * {@link ConstantDecay} resistance change: dr=-1*dt, regardless of k
		 */
		class ConstantDecay implements Decay
		{
			@Override
			public Integrator<Quantity<Time>, BigDecimal> integrator()
			{
				return ( t_0, dt, r_0, k ) -> QuantityUtil.min( dt, r_0 )
						.multiply( -1 );
			}

			@Override
			public Interceptor<Quantity<Time>, BigDecimal> interceptor()
			{
				return ( t_0, r_0, r_t, k ) -> r_t == null ? null
						: r_t.subtract( r_0 ).divide( -1 );
			}
		}

		/**
		 * {@link ConditionalDecay} resistance change; dr=-1*dt iff k>0, else NA
		 */
		class ConditionalDecay implements Decay
		{
			@Override
			public Integrator<Quantity<Time>, BigDecimal> integrator()
			{
				return ( t_0, dt, r_0, k ) -> k == null || k.signum() < 1 ? null
						: QuantityUtil.min( dt, r_0 ).multiply( -1 );
			}

			@Override
			public Interceptor<Quantity<Time>, BigDecimal> interceptor()
			{
				return ( t_0, r_0, r_t, k ) -> k == null || k.signum() < 1
						? null : r_t.subtract( r_0 ).divide( -1 );
			}
		}

		/**
		 * {@link ProportionalDecay} resistance change: dr=-k*dt iff k>0, else
		 * NA
		 */
		class ProportionalDecay implements Decay
		{
			@SuppressWarnings( "unchecked" )
			@Override
			public Integrator<Quantity<Time>, BigDecimal> integrator()
			{
				return ( t_0, dt, r_0, k ) -> k == null || k.signum() < 1 ? null
						: QuantityUtil.min( dt, r_0 ).multiply( -1 );
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public Interceptor<Quantity<Time>, BigDecimal> interceptor()
			{
				return ( t_0, r_0, r_t,
					k ) -> k == null || k.signum() < 1 || r_t == null ? null
							: r_t.subtract( r_0 ).divide( k.negate() );
			}
		}
	}

	/**
	 * {@link Simple} resists infection pressure based on {@link #decay}
	 */
	public static class Simple extends EpiTrajectory.SimpleClinical
		implements InfectionPressure
	{

		@InjectConfig
		private JsonNode config;

		@Override
		public JsonNode config()
		{
			return this.config;
		}

		@Override
		public void onSubscribe( final Disposable undead )
		{
			if( this.condition.get() == null ) passive(); // initialize
		}

		@Override
		public void onNext( final Number t )
		{
			setPressure( DecimalUtil.valueOf( t ) );
		}

		private transient Decay decayCache = null;

		private final AtomicReference<BigDecimal> pressure = new AtomicReference<>();
		private final AtomicReference<Expectation> expose = new AtomicReference<>();
		private final AtomicReference<Quantity<Time>> resistance = new AtomicReference<>();
		private final AtomicReference<Instant> since = new AtomicReference<>();

		public void setPressure( final BigDecimal pNew )
		{
			final BigDecimal pOld = this.pressure.getAndSet( pNew );

			// no pressure or unchanged
			if( pOld == null || pOld.equals( pNew ) ) return;

			if( this.condition.get() == null
					|| !this.condition.get().getCompartment().isSusceptible() )
				return; // not susceptible: no exposure occurring

			final Instant t = now(), t0 = this.since.getAndSet( t );

			// update remaining resistance, subtracting dp for dt > 0
			if( t0 != null && !t0.equals( t ) )
			{
				final Quantity<Time> dt = t.toQuantity( Time.class )
						.subtract( t0.toQuantity( Time.class ) ),
						dp = decay().integrator().dq( t0, dt,
								this.resistance.get(), pOld );
//							res0 = 
				this.resistance.getAndUpdate(
						res -> dp == null ? res : res.add( dp ) );
//					System.err.println( "t=" + t + " dt=" + dt + " dp=" + dp
//							+ " res0=" + res0 + " res_t=" + this.resistance );
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
			this.emitter.onComplete(); // sim/pressure completed
		}

		@Override
		public Decay decay()
		{
			if( this.decayCache == null ) try
			{
				this.decayCache = fromConfig( DECAY_TYPE_KEY, Decay.class,
						Decay.CONSTANT );
			} catch( final ClassNotFoundException e )
			{
				this.emitter.onError( e );
			}
			return this.decayCache;
		}

		/**
		 * sets/maintains condition {@link #SUSCEPTIBLE} and (re)schedules
		 * {@link #expose} after (pressured)
		 * {@link EpiTransition#SUSCEPTIBILITY}
		 */
		@Override
		protected void wane()
		{
			final SimpleEpiClinical c = this.condition.get();
			if( c == null || !c.getCompartment().isSusceptible() )
				this.emitter.onNext( this.condition
						.updateAndGet( x -> x == SimpleEpiClinical.MATERNAL
								? SimpleEpiClinical.SUSCEPTIBLE
								: SimpleEpiClinical.DORMANT ) );

			final Quantity<Time> resistance = this.resistance
					.updateAndGet( dt -> dt != null ? dt
							: dtMapper( EpiTransition.SUSCEPTIBILITY ) );

			final int signum = QuantityUtil.signum( resistance );
			if( signum < 0 )
				Thrower.throwNew( IllegalStateException::new,
						() -> "Exposed overdue: " + resistance );
			else if( signum == 0 )
				expose();
			else
				// (re)schedule exposure (if pressure exists)
				this.expose.updateAndGet( exp ->
				{
					// cancel previous, if any
					if( exp != null ) exp.remove();
					final Quantity<Time> dt = decay().interceptor().dtMin(
							now(), resistance, QuantityUtil.zero( Time.class ),
							this.pressure.get() );
					return dt == null ? null : after( dt ).call( this::expose );
				} );
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

	/**
	 * {@link Factory} used for {@link PressuredEpiTrajectory}
	 */
	interface Factory extends EpiTrajectory.Factory
	{
		@Override
		InfectionPressure create( JsonNode config ) throws Exception;

		String TYPE_DEFAULT = Simple.class.getName();

		@Singleton
		class SimpleBinding implements Factory
		{
			@Inject
			private LocalBinder binder;

			@Override
			public InfectionPressure create( final JsonNode config )
				throws ClassNotFoundException, ParseException
			{
				final JsonNode typeNode = Objects
						.requireNonNull( config, "No config?" ).get( TYPE_KEY );
				final String typeName = typeNode == null ? TYPE_DEFAULT
						: typeNode.asText( TYPE_DEFAULT );
				final Class<? extends InfectionPressure> type = Class
						.forName( typeName )
						.asSubclass( InfectionPressure.class );
				return this.binder.inject( type, config );
			}
		}
	}
}