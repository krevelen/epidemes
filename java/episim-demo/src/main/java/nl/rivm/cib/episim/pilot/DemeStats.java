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
package nl.rivm.cib.episim.pilot;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Logger;

import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.log.LogUtil;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import io.reactivex.Observable;
import nl.rivm.cib.episim.model.person.DomesticChange;
import nl.rivm.cib.episim.model.person.Residence;

/**
 * {@link DemeStats}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface DemeStats extends Proactive
{

	void reportDeme( Timing timing, Residence.Deme source );

	@Singleton
	class Simple implements DemeStats
	{
		/** */
		private static final Logger LOG = LogUtil
				.getLogger( DemeStats.Simple.class );

		@Inject
		private Scheduler scheduler;

		/** stats */
		private final AtomicLong immigrations = new AtomicLong();
		private final AtomicLong emigrations = new AtomicLong();
		private final AtomicLong births = new AtomicLong();
		private final AtomicLong deaths = new AtomicLong();
		private final AtomicLong households = new AtomicLong();
		private final AtomicLong persons = new AtomicLong();

		@Override
		public Scheduler scheduler()
		{
			return this.scheduler;
		}

		private void onError( final Throwable t )
		{
			LOG.error( "Problem observing stats", t );
		}

		@Override
		public void reportDeme( final Timing timing,
			final Residence.Deme source )
		{
			source.emit( FactKind.REQUESTED ).subscribe( rq ->
			{
				this.households.incrementAndGet();
				this.persons.addAndGet( rq.getMembers().size() );
			}, this::onError );

			final Observable<Fact> stated = source.emitFacts()
					.filter( f -> f.kind() == FactKind.STATED );
			stated.ofType( DomesticChange.Immigrate.class ).subscribe( st ->
			{
				this.immigrations.incrementAndGet();
//				this.households.incrementAndGet();
//				this.persons.addAndGet( st.getMembers().size() );
			}, this::onError );
			stated.ofType( DomesticChange.Emigrate.class ).subscribe( st ->
			{
				this.emigrations.incrementAndGet();
				this.households.decrementAndGet();
				this.persons.addAndGet( -st.getMembers().size() );
			}, this::onError );
			stated.ofType( DomesticChange.Birth.class ).subscribe( st ->
			{
				this.births.incrementAndGet();
				this.persons.incrementAndGet();
			}, this::onError );
			stated.ofType( DomesticChange.Death.class ).subscribe( st ->
			{
				if( st.getMembers().isEmpty() )
					this.households.decrementAndGet();
				this.deaths.incrementAndGet();
				this.persons.decrementAndGet();
			}, this::onError );

			try
			{
				final Instant now = now();
				atEach( timing.offset( now.toJava8( scheduler().offset() ) )
						.iterate( now ), this::stats );
			} catch( final ParseException e )
			{
				onError( e );
			}
		}

		private LocalDateTime offsetCache = null;

		protected LocalDateTime offset()
		{
			if( this.offsetCache == null ) this.offsetCache = scheduler()
					.offset().toLocalDate().atStartOfDay();
			return this.offsetCache;
		}

		protected void stats( final Instant t )
		{
			LOG.info( LogUtil.messageOf(
					"[t] %s | [hh] %+4d %+4d = %6d | [pp] %+4d %+4d = %7d",
					t.prettify( offset() ), this.immigrations.getAndSet( 0L ),
					-this.emigrations.getAndSet( 0L ), this.households.get(),
					this.births.getAndSet( 0L ), -this.deaths.getAndSet( 0L ),
					this.persons.get() ) );
		}
	}
}
