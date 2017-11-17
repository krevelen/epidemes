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

import java.util.List;
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
import nl.rivm.cib.demo.DemoModel.Households;
import nl.rivm.cib.epidemes.cbs.json.CBSBirthRank;

/**
 * {@link ExpansionPicker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ExpansionPicker implements Timed
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( ExpansionPicker.class );

	private final Timed timed;
	private final PseudoRandom rng;
	private final Table<Households.HouseholdTuple> households;
	private final IndexPartition index;

//			@Inject
	public ExpansionPicker( final Scheduler scheduler, final PseudoRandom rng,
		final Table<Households.HouseholdTuple> data )
	{
		this.timed = scheduler;
		this.rng = rng;//distFact.getStream();
		this.households = data;//.getTable( HouseholdTuple.class );
		this.index = new IndexPartition( this.households, scheduler::fail );
		this.index.groupBy( Households.HomeRegionRef.class );
		this.index.groupBy( Households.KidRank.class );
		this.index.groupBy( Households.MomBirth.class,
				IntStream.range( 15, 50 /* TODO plus sim length (yr) */ )
						.mapToObj( dt -> now()
								.subtract( Duration.of( dt, TimeUnits.YEAR ) )
								.decimal() ) );
	}

	@Override
	public Instant now()
	{
		return this.timed.now();
	}

	public Households.HouseholdTuple pick( final Comparable<?> regRef,
		final Quantity<Time> momAge, final CBSBirthRank kidRank )
	{
//				LOG.trace( "Picking from {} households: {}",
//						this.households.size(), this.index );
		final Instant momBirth = now().subtract( momAge );
		final List<Object> keys = this.index.nearestKeys( ( k, v ) ->
		{
			LOG.trace( "Expanders [{};{};{}] deviate: {} in {}", regRef,
					kidRank, QuantityUtil.pretty( momAge, 1 ),
					k.getSimpleName(), v );
			return true;
		}, regRef, kidRank, momBirth.decimal() );
		return this.households.select( this.rng.nextElement( keys ) );
	}
}