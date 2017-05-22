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
package nl.rivm.cib.episim.hesitant;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import nl.rivm.cib.episim.hesitant.Advice.Advisor;
import nl.rivm.cib.episim.hesitant.Information.Informer;
import nl.rivm.cib.episim.hesitant.Opinion.Opinionator;
import nl.rivm.cib.episim.model.person.Attitude;

/**
 * T14 {@link Motivation} transactions are initiated by:
 * <ul>
 * <li>another O01 {@link PersonConfig}'s A13 {@link Opinionator} (e.g. persuade
 * a, relative, colleague, or other social network relation);</li>
 * <li>some O04 Inform organization's A40 {@link Informer}; or</li>
 * <li>some O05 Health organization's A50 {@link Advisor}.
 * </ul>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Motivation extends Fact, Attitude.Attributable<Motivation>
{
	/**
	 * A14 {@link Motivator} handles T14 {@link Motivation} execution,
	 * occasionally initiating a {@link Redirection}
	 */
	public interface Motivator
		extends Actor<Motivation>, Attitude.Attributable<Motivator>
	{

	}

//	Number getConfidence();
//
//	default Motivation withConfidence( final Number confidence )
//	{
//		return with( "confidence", confidence );
//	}
//
//	Number getComplacency();
//
//	default Motivation withComplacency( final Number complacency )
//	{
//		return with( "complacency", complacency );
//	}
//
//	default Motivation withAttitude( final VaxHesitancy hes )
//	{
//		return withConfidence( hes.getConfidence() )
//				.withComplacency( hes.getComplacency() );
//	}

//	/** @return {@link List} of the {@link VaxDose}s remaining */
//	List<VaxDose> getRemaining();
//
//	void setRemaining( List<VaxDose> doses );
//
//	default Motivation withRemaining( final List<VaxDose> doses )
//	{
//		setRemaining( doses );
//		return this;
//	}
//
//	default Motivation with( final VaxDose... doses )
//	{
//		return withRemaining( Arrays.asList( doses ) );
//	}

//	/**
//	 * @return {@link List} of {@link VaxOccasion}s, representing e.g. available
//	 *         week/days at a (youth) {@link HealthConfig} service
//	 */
//	List<VaxOccasion> getOccasions();
//
//	void setOccasions( List<VaxOccasion> occasions );
//
//	default Motivation withOccasions( final List<VaxOccasion> occasions )
//	{
//		setOccasions( occasions );
//		return this;
//	}
//
//	default Motivation with( final VaxOccasion... occasions )
//	{
//		return withOccasions(
//				occasions == null ? null : Arrays.asList( occasions ) );
//	}
}