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
package nl.rivm.cib.episim.model.scenario;

import io.coala.time.Timed;

/**
 * {@link MatingOperator} manipulates populations to project macro-level
 * features (birth rates etc.) onto available micro-level elements (individual
 * mothers etc.) using <em>proportionate stratification</em> (i.e. sample is
 * representative across all relevant strata) and
 * <em>systematic random sampling</em> (i.e. sample is taken from entire
 * population at once using some system)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
interface MatingOperator extends Timed
{
//	void register( Stratifiable candidate );
//	
//	Map<Object, ProbabilityDistribution<?>> samplers();
//	
//	ProbabilityDistribution<Stratifiable> sampler();
//
//	/**
//	 * {@link Stratifiable} describes individual-level features/strata known for
//	 * the population-level distribution of births among mothers such as age,
//	 * income etc.
//	 * 
//	 * @version $Id$
//	 * @author Rick van Krevelen
//	 */
//	interface Stratifiable
//	{
//		Map<Object, Object> strata();
//	}
//
//	class Simple implements MatingOperator
//	{
//
//		public static MatingOperator.Simple of( final Scheduler scheduler,
//			final PseudoRandom rng,
//			final AmountDistribution<Duration> fertilityAgeDist,
//			final AmountDistribution<Duration> recoveryPeriodDist )
//		{
//			final MatingOperator.Simple result = new Simple();
//			result.scheduler = scheduler;
//			result.rng = rng;
//			result.fertilityAgeDist = fertilityAgeDist;
//			result.recoveryPeriodDist = recoveryPeriodDist;
//			return result;
//		}
//
//		private Scheduler scheduler;
//
//		private PseudoRandom rng;
//
//		private AmountDistribution<Duration> fertilityAgeDist;
//
//		private AmountDistribution<Duration> recoveryPeriodDist;
//
//		// TODO store in (distributed) table?
//		private SortedSet<MatingOperator.Stratifiable> candidates = new TreeSet<MatingOperator.Stratifiable>(
//				( o1, o2 ) ->
//				{
//					return o1.birthDate().compareTo( o2.birthDate() );
//				} );
//
//		@Override
//		public Scheduler scheduler()
//		{
//			return this.scheduler;
//		}
//
//		@Override
//		public void register( final MatingOperator.Stratifiable candidate )
//		{
//			// TODO Auto-generated method stub
//
//		}
//
//		private SortedSet<MatingOperator.Stratifiable>
//			candidates( final Amount<Duration> age )
//		{
//			return this.candidates.subSet( () ->
//			{
//				return age;
//			}, () ->
//			{
//				return age.plus( Amount.valueOf( 1, Units.ANNUM ) );
//			} );
//		}
//
//		@Override
//		public MatingOperator.Stratifiable chooseMother()
//		{
//			final SortedSet<MatingOperator.Stratifiable> candidates = candidates(
//					this.fertilityAgeDist.draw() );
//			if( candidates.size() == 0 ) return null;
//			final MatingOperator.Stratifiable result = this.rng
//					.nextElement( candidates );
//
//			final Amount<Duration> wait = this.recoveryPeriodDist.draw();
//			if( isFertileOn(
//					now().subtract( result.birthDate() ).add( wait ) ) )
//				after( wait ).call( this::register, result );
//			return result;
//		}
//	}
}