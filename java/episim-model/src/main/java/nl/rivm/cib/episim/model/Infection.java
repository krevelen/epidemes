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
package nl.rivm.cib.episim.model;

import java.util.Map;

import io.coala.time.x.Duration;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Infection} results in infectious/transmissible/communicable/contagious
 * disease and is caused by pathogen microbes like viruses, bacteria, fungi, and
 * parasites (bacterial, viral, fungal, parasitic) for which respective
 * medications may exist (antibiotics, antivirals, antifungals, and
 * antiprotozoals/antihelminthics)
 * 
 * <p>
 * Some terminology from
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Infection
{

	/**
	 * spreads via animal-human contact, see
	 * http://www.cdc.gov/onehealth/zoonotic-diseases.html
	 */
	//boolean isZoonotic();

	/**
	 * @return {@code true} if this {@link Infection} is opportunistic,
	 *         requiring impairment of host defenses, {@code false} otherwise
	 *         (i.e. primary pathogens with intrinsic virulence)
	 */
	//boolean isOpportunistic();

	/**
	 * useful in behavior-driven transmission among symptom-observing humans
	 * 
	 * @return {@code true} if this {@link Infection} is long-term or chronic,
	 *         {@code false} otherwise (i.e. short-term or acute)
	 */
	//boolean isChronic();

	/**
	 * useful in behavior-driven transmission among symptom-observing humans
	 * 
	 * @return {@code true} if this {@link Condition} is systemic, causing
	 *         sepsis, {@code false} otherwise (i.e. short-term or acute)
	 */
	//boolean isSystemic();

	/**
	 * @return an {@link Observable} stream of {@link TransmissionEvent}s
	 */
	Observable<TransmissionEvent> getTransmission();

	/**
	 * {@link SimpleInfection} is a {@link Infection} and {@link Observer} of
	 * {@link ContactEvent}s which in turn may trigger its transmission by
	 * generating {@link TransmissionEvent}s.
	 * 
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
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public abstract class SimpleInfection
		implements Infection, Observer<ContactEvent>
	{

		private Subject<TransmissionEvent, TransmissionEvent> transmission = PublishSubject
				.create();

		@Override
		public Observable<TransmissionEvent> getTransmission()
		{
			return this.transmission.asObservable();
		}

		public Subscription
			subscribeTo( final Observable<ContactEvent> contacts )
		{
			return contacts.filter( new Func1<ContactEvent, Boolean>()
			{
				@Override
				public Boolean call( final ContactEvent contact )
				{
					return contact.getPrimaryInfectiveDiseases()
							.containsKey( SimpleInfection.this );
				}
			} ).subscribe( this );
		}

		@Override
		public void onCompleted()
		{
			this.transmission.onCompleted();
		}

		@Override
		public void onError( final Throwable e )
		{
			this.transmission.onError( e );
		}

		@Override
		public void onNext( final ContactEvent contact )
		{
			for( Map.Entry<Carrier, Relation> susceptible : contact
					.getSecondarySusceptibles().entrySet() )
				if( transmit( contact.getLocation(), contact.getMedium(),
						contact.getDuration(), susceptible.getValue() ) )
				{
					final Carrier exposed = susceptible.getKey();
					this.transmission.onNext( new TransmissionEvent()
					{
						@Override
						public Infection getDisease()
						{
							return SimpleInfection.this;
						}

						@Override
						public ContactEvent getContact()
						{
							return contact;
						}

						@Override
						public Carrier getSecondaryExposed()
						{
							return exposed;
						}
					} );
				}
		}

		/**
		 * Decide whether transmission occurs between some susceptible and some
		 * infective {@link Carrier} of this {@link Infection}
		 * 
		 * @param location the {@link Location} of contact
		 * @param route the {@link Route} of contact
		 * @param duration the {@link Duration} of contact
		 * @param relation the {@link Relation} between {@link Carrier}s
		 * @return {@code true} iff the transmission is likely, {@code false}
		 *         otherwise
		 */
		public abstract boolean transmit( Location location, Route route,
			Duration duration, Relation relation );

	}
}
