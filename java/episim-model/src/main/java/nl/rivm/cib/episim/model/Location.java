/* $Id: e67e3079fc342154fce540f31a263151797d8350 $
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
package nl.rivm.cib.episim.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;
import javax.measure.unit.NonSI;

import org.jscience.geography.coordinates.LatLong;
import org.jscience.physics.amount.Amount;
import org.opengis.spatialschema.geometry.geometry.Position;

import io.coala.exception.x.ExceptionBuilder;
import io.coala.json.x.Wrapper;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Accumulator;
import nl.rivm.cib.episim.time.Timed;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Location}
 * 
 * @version $Id: e67e3079fc342154fce540f31a263151797d8350 $
 * @author Rick van Krevelen
 */
public interface Location extends Timed
{

	/** RIVM National Institute for Public Health and the Environment */
	Position RIVM_POSITION = LatLong.valueOf( 52.1185272, 5.1868699,
			NonSI.DEGREE_ANGLE );

	/** the NO_ZIP {@link ZipCode} constant */
	ZipCode NO_ZIP = ZipCode.valueOf( "0000" );

	/**
	 * {@link ZipCode} is a simple {@link Wrapper} of {@link String} values
	 * 
	 * @version $Id: e67e3079fc342154fce540f31a263151797d8350 $
	 * @author Rick van Krevelen
	 */
	class ZipCode extends Wrapper.Simple<String>
	{
		/**
		 * @param value the {@link String} value to wrap
		 * @return the {@link ZipCode} decorator
		 */
		public static ZipCode valueOf( final String value )
		{
			return Util.of( value, new ZipCode() );
		}
	}

	/** @return the global {@link Position} of this {@link Location} */
	default Position getPosition()
	{
		return RIVM_POSITION;
	}

	/** @return the {@link ZipCode} of this {@link Location} */
	default ZipCode getZip()
	{
		return NO_ZIP;
	}

	default Collection<TransmissionRoute> getTransmissionRoutes()
	{
		return Collections.singleton( TransmissionRoute.AIRBORNE );
	}

	/** @param visitor the {@link Individual} arriving */
	default void arrive( final Individual visitor )
	{
		visitor.getConditions().forEach( ( infection, condition ) ->
		{
			per( infection ).per( condition.getCompartment() )
					.onArrival( visitor );
		} );
	}

	/** @param visitor the {@link Individual} departing */
	default void depart( final Individual visitor )
	{
		visitor.getConditions().forEach( ( infection, condition ) ->
		{
			per( infection ).per( condition.getCompartment() )
					.onDeparture( visitor );
		} );
	}

	interface CompartmentArrivals
	{
		Location getLocation();

		Infection getInfection();

		EpidemicCompartment getCompartment();

		Map<Individual, Instant> getArrivals();

		IndividualDynamics per( Individual individual );

		default void onArrival( final Individual visitor )
		{
			final Instant previous;
			if( (previous = getArrivals().put( visitor,
					getLocation().now() )) != null )
				throw ExceptionBuilder
						.unchecked( "%s already arrived at %s on %s", visitor,
								getLocation(), previous )
						.build();

			if( getCompartment().isInfective() )
			{

				//  TODO update force of infection as f(R0, D) and reschedule (advance)
				// pending transmissions, or cancel on prior departure

				//Map< Individual, >
//				final Amount<Duration> duration = null;
//				final Amount<Frequency> force = getInfection()
//						.getForceOfInfection( this, visitor,
//								getLocation().per( getInfection() )
//										.per( EpidemicCompartment.Simple.SUSCEPTIBLE )
//										.getArrivals().keySet(),
//								duration );
			}
		}

		default void onDeparture( final Individual visitor )
		{
			final Instant arrival;
			if( (arrival = getArrivals().remove( visitor )) == null )
				throw ExceptionBuilder.unchecked( "%s already departed from %s",
						visitor, getLocation() ).build();
			arrival.toAmount();
//			final Instant departure = getLocation().now();
//			for( Map.Entry<Individual, Instant> entry : getOccupantArrivals()
//					.entrySet() )
//			{
//				final Instant start = arrival.compareTo( entry.getValue() ) > 0
//						? arrival : entry.getValue();
//				final Duration overlap = Duration.between( start, departure );
//
//				// FIXME generate contact events from overlapping occupancy/vector infestation/contamination stays
//				// FIXME generate transmission events
//			}
		}

		class Simple implements CompartmentArrivals
		{
			private final Location location;

			private final Infection infection;

			private final EpidemicCompartment compartment;

			private final Map<Individual, Instant> arrivals = new ConcurrentSkipListMap<>();

			private final Map<Individual, IndividualDynamics> transitions = new ConcurrentSkipListMap<>();

			private Simple( final Location location, final Infection infection,
				final EpidemicCompartment compartment )
			{
				this.location = location;
				this.infection = infection;
				this.compartment = compartment;
			}

			@Override
			public Location getLocation()
			{
				return this.location;
			}

			@Override
			public Infection getInfection()
			{
				return this.infection;
			}

			@Override
			public EpidemicCompartment getCompartment()
			{
				return this.compartment;
			}

			@Override
			public Map<Individual, Instant> getArrivals()
			{
				return this.arrivals;
			}

			@Override
			public IndividualDynamics per( final Individual individual )
			{
				synchronized( this.transitions )
				{
					IndividualDynamics result = this.transitions
							.get( individual );
					if( result == null )
					{
						final Amount<Frequency> rate = null;
						result = new IndividualDynamics.Simple( individual,
								Accumulator.of( getLocation().scheduler(),
										Amount.ZERO, rate ) );
						this.transitions.put( individual, result );
					}
					return result;
				}
			}
		}
	}

	interface IndividualDynamics
	{
		Individual getIndividual();

		Accumulator<Dimensionless> getAccumulator();

		class Simple implements IndividualDynamics
		{
			private Individual individual;

			private Accumulator<Dimensionless> accumulator;

			public Simple( final Individual individual,
				final Accumulator<Dimensionless> accumulator )
			{
				this.individual = individual;
				this.accumulator = accumulator;
			}

			@Override
			public Individual getIndividual()
			{
				return this.individual;
			}

			@Override
			public Accumulator<Dimensionless> getAccumulator()
			{
				return this.accumulator;
			}
		}
	}

	interface InfectionArrivals
	{
		Location getLocation();

		Infection getInfection();

		CompartmentArrivals per( EpidemicCompartment compartment );

		class Simple implements InfectionArrivals
		{
			private final Location location;

			private final Infection infection;

			private final Map<EpidemicCompartment, CompartmentArrivals> compartments = new ConcurrentSkipListMap<>();

			private Simple( final Location location, final Infection infection )
			{
				this.location = location;
				this.infection = infection;
			}

			@Override
			public Location getLocation()
			{
				return this.location;
			}

			@Override
			public Infection getInfection()
			{
				return this.infection;
			}

			@Override
			public CompartmentArrivals
				per( final EpidemicCompartment compartment )
			{
				synchronized( this.compartments )
				{
					CompartmentArrivals result = this.compartments
							.get( compartment );
					if( result == null )
					{
						result = new CompartmentArrivals.Simple( getLocation(),
								getInfection(), compartment );
						this.compartments.put( compartment, result );
					}
					return result;
				}
			}
		}
	}

	/**
	 * @param infection the local {@link Infection}
	 * @return the respective {@link InfectionArrivals}
	 */
	InfectionArrivals per( Infection infection );

	/**
	 * @return an {@link Observable} stream of {@link ContactEvent}s generated
	 *         by {@link Carrier} occupants of this {@link Location}
	 */
	Observable<ContactEvent> emitContacts();

	/**
	 * @return an {@link Observable} stream of {@link TransmissionEvent}s
	 *         generated by {@link Carrier} occupants of this {@link Location}
	 */
	Observable<TransmissionEvent> emitTransmissions();

	/**
	 * {@link Carrier}s staying at this {@link Location} may cause it to
	 * generate {@link ContactEvent}s based on available
	 * {@link TransmissionRoute}s (e.g. contaminated objects, food, water,
	 * blood, ...)
	 * 
	 * @param visitor the temporary occupant {@link Individual}
	 * @param arrival the {@link Instant} of arrival
	 * @param departure the {@link Instant} of departure
	 */
	public default void stay( final Individual visitor, final Instant arrival,
		final Instant departure )
	{
		at( arrival ).call( this::arrive, visitor );
		at( departure ).call( this::depart, visitor );
	}

	/**
	 * {@link SimpleInfection} is a {@link Infection} and {@link Observer} of
	 * {@link ContactEvent}s which in turn may trigger its transmission by
	 * generating {@link TransmissionEvent}s.
	 * 
	 * @version $Id: e67e3079fc342154fce540f31a263151797d8350 $
	 * @author Rick van Krevelen
	 */
	class Simple implements Location
	{

		private final Map<Infection, InfectionArrivals> infections = new ConcurrentHashMap<>();

		private final transient Subject<ContactEvent, ContactEvent> contacts = PublishSubject
				.create();

		private final transient Subject<TransmissionEvent, TransmissionEvent> transmissions = PublishSubject
				.create();

		private Scheduler scheduler = null;

		private Simple( final Scheduler scheduler )
		{
			this.scheduler = scheduler;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public Observable<ContactEvent> emitContacts()
		{
			return this.contacts.asObservable();
		}

		@Override
		public Observable<TransmissionEvent> emitTransmissions()
		{
			return this.transmissions.asObservable();
		}

		@Override
		public InfectionArrivals per( final Infection infection )
		{
			synchronized( this.infections )
			{
				InfectionArrivals result = this.infections.get( infection );
				if( result == null )
				{
					result = new InfectionArrivals.Simple( this, infection );
					this.infections.put( infection, result );
				}
				return result;
			}
		}
	}

}
