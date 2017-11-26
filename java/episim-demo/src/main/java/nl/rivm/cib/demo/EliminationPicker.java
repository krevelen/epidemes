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
package nl.rivm.cib.demo;

import java.util.stream.IntStream;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.data.IndexPartition;
import io.coala.data.Table;
import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.random.PseudoRandom;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timed;
import nl.rivm.cib.demo.DemoModel.Persons;

/**
 * {@link EliminationPicker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class EliminationPicker implements Timed
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( EliminationPicker.class );

	private final Timed timed;
	private final PseudoRandom rng;
	private final Table<Persons.PersonTuple> data;
	private final IndexPartition index;
//			@SuppressWarnings( "rawtypes" )
//			private final Filter<BigDecimal, Filter<Comparable, Root<PersonTuple>>> picker;

//			@Inject
	public EliminationPicker( final Scheduler scheduler, final PseudoRandom rng,
		final Table<Persons.PersonTuple> data )
	{
		this.timed = scheduler;
		this.rng = rng;
		this.data = data;
		
		this.index = new IndexPartition( this.data, scheduler::fail );
		this.index.groupBy( Persons.HomeRegionRef.class );
		this.index.groupBy( Persons.Birth.class,
				IntStream.range( -20, 0 /* TODO plus sim length (yr) */ )
						.mapToObj( dt -> now()
								.add( Duration.of( 5 * dt, TimeUnits.YEAR ) )
								.decimal() ) );
//				this.picker = Table.Picker.of( data, rng, ( v, p, d ) ->
//				{
//					LOG.trace( "Deviate: {} widened to {}", v, p, d );
//					return true;
//				} ).splitBy( Persons.HomeRegionRef.class ).thenBy(
//						Persons.Birth.class,
//						IntStream
//								.range( 0, 100 /* TODO plus sim length (yr) */ )
//								.mapToObj(
//										dt -> now()
//												.subtract( Duration.of( dt,
//														TimeUnits.YEAR ) )
//												.decimal() ) );
	}

	@Override
	public Instant now()
	{
		return this.timed.now();
	}

	public Persons.PersonTuple pick( final Comparable<?> regRef,
		final Quantity<Time> age )
	{
//				LOG.trace( "Picking from {} persons: {}",
//						this.picker.index().keys().size(),
//						this.picker.index() );
//		LOG.trace( "Elimination pick from {}", this.index );
		return this.data.select(
				this.rng.nextElement( this.index.nearestKeys( ( p, d ) ->
				{
					LOG.trace( "Death [{};{}] deviate: widened to {}:{}",
							regRef, QuantityUtil.pretty( age, 3 ),
							p.getSimpleName(), d );
					return true;
				}, regRef, now().subtract( age ).decimal() ) ) );
//				this.picker.narrow(
//						now().subtract( Duration.of( age ) ).decimal() )
//						.narrow( regRef ).pick();
	}
}