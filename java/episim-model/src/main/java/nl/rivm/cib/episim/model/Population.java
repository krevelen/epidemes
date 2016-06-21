/* $Id: 9dade484b159c8240410d99c1b9d08c6f16e1340 $
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

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import nl.rivm.cib.episim.model.DemographicEvent.Birth;
import nl.rivm.cib.episim.model.DemographicEvent.CoupleDissolution;
import nl.rivm.cib.episim.model.DemographicEvent.CoupleFormation;
import nl.rivm.cib.episim.model.DemographicEvent.Death;
import nl.rivm.cib.episim.model.DemographicEvent.Emigration;
import nl.rivm.cib.episim.model.DemographicEvent.Immigration;
import nl.rivm.cib.episim.model.DemographicEvent.NestDeparture;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.time.Timed;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Population} provides macro-level characteristics, following common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic models</a>
 * and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>, e.g. by
 * <a href="http://jasss.soc.surrey.ac.uk/16/1/8.html">Geard et al. (2013)</a>
 * 
 * <table>
 * <tr>
 * <th>&mu;</th>
 * <td>Average death rate</td>
 * </tr>
 * <tr>
 * <th>B</th>
 * <td>Average birth rate</td>
 * </tr>
 * </table>
 * 
 * @version $Id: 9dade484b159c8240410d99c1b9d08c6f16e1340 $
 * @author Rick van Krevelen
 */
public interface Population extends Timed
{

	void reset( Iterable<Household> households );

	Amount<Dimensionless> getSize();

	Observable<DemographicEvent> emitHouseholdEvents();

	Population birth( Individual newborn );

	Population death( Individual diseased );

	Population depart( Individual child, Household nest );

	Population immigrate( Household immigrants );

	Population emigrate( Household emigrants );

	Population formCouple( Household merging, Household abandoning );

	Population dissolveCouple( Household parting, Household dissolving );

	class Simple implements Population
	{
		public static Simple of( final Scheduler scheduler )
		{
			return new Simple( scheduler );
		}

		private final Subject<DemographicEvent, DemographicEvent> changes = PublishSubject
				.create();

		private final Scheduler scheduler;

		private Amount<Dimensionless> size;

		public Simple( final Scheduler scheduler )
		{
			this.scheduler = scheduler;
		}

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		@Override
		public void reset( final Iterable<Household> households )
		{
			int size = 0;
			for( Household hh : households )
				size += hh.getMembers().size();
			// FIXME persist?
			this.size = Amount.valueOf( size, Unit.ONE );
		}

		public Observable<DemographicEvent> emitHouseholdEvents()
		{
			return this.changes.asObservable();
		}

		@Override
		public Amount<Dimensionless> getSize()
		{
			return this.size;
		}

		protected Population addSize( final int delta )
		{
			return addSize( Amount.valueOf( delta, Unit.ONE ) );
		}

		protected Population addSize( final Amount<Dimensionless> delta )
		{
			this.size = this.size.plus( delta );
			return this;
		}

		@Override
		public Population birth( final Individual newborn )
		{
			this.changes.onNext( new Birth( newborn ) );
			return addSize( Amount.ONE );
		}

		@Override
		public Population death( final Individual diseased )
		{
			this.changes.onNext( new Death( diseased ) );
			return addSize( -1 );
		}

		@Override
		public Population depart( final Individual child, final Household nest )
		{
			this.changes.onNext( new NestDeparture( child, nest ) );
			return this;
		}

		@Override
		public Population immigrate( final Household immigrants )
		{
			this.changes.onNext( new Immigration( immigrants ) );
			return addSize( immigrants.getMembers().size() );
		}

		@Override
		public Population emigrate( final Household emigrants )
		{
			this.changes.onNext( new Emigration( emigrants ) );
			return addSize( -emigrants.getMembers().size() );
		}

		@Override
		public Population formCouple( final Household merging,
			final Household abandoning )
		{
			this.changes.onNext( new CoupleFormation( merging, abandoning ) );
			return this;
		}

		@Override
		public Population dissolveCouple( final Household parting,
			final Household dissolving )
		{
			this.changes.onNext( new CoupleDissolution( parting, dissolving ) );
			return this;
		}
	}

}
