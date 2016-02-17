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
package nl.rivm.cib.episim.model.impl;

import static org.aeonbits.owner.util.Collections.entry;
import static org.aeonbits.owner.util.Collections.map;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;

import org.jscience.physics.amount.Amount;

import io.coala.bind.Binder;
import io.coala.random.RandomDistribution;
import io.coala.random.RandomNumberStream;
import nl.rivm.cib.episim.model.Condition;
import nl.rivm.cib.episim.model.Infection;
import nl.rivm.cib.episim.model.Relation;
import nl.rivm.cib.episim.model.TransmissionRoute;

/**
 * {@link Measles} (morbilli, mazelen) is a highly contagious and potentially
 * deadly disease caused by the measles virus (MeV) including respiratory system
 * infections and a generalized rash. It is described at
 * <a href="https://en.wikipedia.org/wiki/Measles">wikipedia</a> and has a
 * <a href="http://rijksvaccinatieprogramma.nl/De_ziekten/Mazelen">RVP page</a>,
 * a <A href="http://www.rivm.nl/Onderwerpen/M/Mazelen">RIVM page</a>, a
 * <href="http://www.cdc.gov/measles/">US CDC page</a>, and a
 * <a href="http://www.who.int/topics/measles/en/">WHO page</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Measles implements Infection
{

	@SuppressWarnings( "unchecked" )
	private static final Map<TransmissionRoute, Amount<Dimensionless>> ROUTE_LIKELIHOODS = map(
			entry( TransmissionRoute.AIRBORNE, Amount.ONE ),
			entry( TransmissionRoute.DIRECT, Amount.ONE ),
			entry( TransmissionRoute.ORAL, Amount.ONE ) );

	private RandomDistribution<Amount<Duration>> latentPeriodDist;

	private RandomDistribution<Amount<Duration>> infectiousPeriodDist;

	private RandomDistribution<Amount<Duration>> immunizationPeriodDist;

	private RandomDistribution<Amount<Duration>> onsetPeriodDist;

	private RandomDistribution<Amount<Duration>> seroconversionPeriodDist;

	@Inject
	public Measles( final Binder binder )
	{
		final RandomNumberStream rng = binder
				.inject( RandomNumberStream.class );
		final RandomDistribution.Factory rdf = binder
				.inject( RandomDistribution.Factory.class );
		this.latentPeriodDist = rdf
				.getConstant( Amount.valueOf( 1, NonSI.HOUR ) );
		this.infectiousPeriodDist = rdf
				.getConstant( Amount.valueOf( 100, NonSI.YEAR ) );
		this.immunizationPeriodDist = rdf
				.getConstant( Amount.valueOf( 0, NonSI.DAY ) );
		this.onsetPeriodDist = RandomDistribution.Util
				.asAmount( rdf.getTriangular( rng, 1, 6, 30 ), NonSI.MONTH );
		this.seroconversionPeriodDist = rdf
				.getConstant( Amount.valueOf( 28, NonSI.DAY ) );
	}

	@Override
	public Collection<TransmissionRoute> getTransmissionRoutes()
	{
		return ROUTE_LIKELIHOODS.keySet();
	}

	@Override
	public Amount<Dimensionless> getTransmissionLikelihood(
		final TransmissionRoute route, final Amount<Duration> duration,
		final Relation relation, final Condition condition )
	{
		final Amount<Dimensionless> result = ROUTE_LIKELIHOODS.get( route );
		return result == null ? Amount.ZERO : result;
	}

}