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
package nl.rivm.cib.episim.model.disease;

import java.math.BigDecimal;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.aeonbits.owner.Config;

import io.coala.time.TimeQuantityConverter;
import io.coala.time.TimeUnits;
import io.coala.util.MapBuilder;

/**
 * {@link EpiPeriod}
 */
public interface EpiConfig extends Config
{
	String SUSCEPTIBLES_KEY = "susceptibles-count";

	String INFECTIVES_KEY = "infectives-count";

	String RECOVERED_KEY = "recovered-count";

	String REPRODUCTION_KEY = "reproduction-ratio";

	String INFECTION_KEY = "infection-period";

	String WANING_KEY = "";

	int POPULATION_DEFAULT = 1000;

	/** @return initial number of susceptibles */
	@Key( SUSCEPTIBLES_KEY )
	@DefaultValue( "" + (POPULATION_DEFAULT - 1) )
	Long susceptibles();

	/** @return initial number of infectives */
	@Key( INFECTIVES_KEY )
	@DefaultValue( "" + 1 )
	Long infectives();

	/** @return initial number of recovered */
	@Key( RECOVERED_KEY )
	@DefaultValue( "" + 0 )
	Long recovered();

	default Map<EpiPop, Long> population()
	{
		return MapBuilder.ordered( EpiPop.class, Long.class )
				.fill( EpiPop.values(), 0L ).put( EpiPop.S, susceptibles() )
				.put( EpiPop.I, infectives() ).put( EpiPop.R, recovered() )
				.build();
	}

	@DefaultValue( 1 + " " + TimeUnits.HOURS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> dt();

	/**
	 * @return <em>R</em><sub>0</sub>: basic reproduction ratio (eg.
	 *         {@link #growth} vs. {@link #recovery}, >1 : epidemic)
	 */
	@Key( REPRODUCTION_KEY )
	@DefaultValue( "" + 14 )
	BigDecimal reproduction();

	/**
	 * @return mean period to secondary infectiveness (eg. &gamma;<sup>-1</sup>
	 *         in: <em>R &larr; &gamma; &middot; I</em>, or
	 *         (&gamma;<sup>-1</sup> + &kappa;<sup>-1</sup>) in: <em>R &larr;
	 *         &gamma; &middot; I &larr; &kappa; &middot; E</em>)
	 */
//		@DefaultValue( 12 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> generation();

	/**
	 * @return &beta;<sup>-1</sup>: mean period of contact (in <em>E &larr;
	 *         &beta; &middot; S</em>)
	 */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> contact();

	/**
	 * @return &beta;<sup>-1</sup> or &kappa;<sup>-1</sup>: mean period of
	 *         infection (in <em>I &larr; &beta; &middot; S</em>) or incubation
	 *         (in <em>I &larr; &kappa; &middot; E</em>)
	 */
	@Key( INFECTION_KEY )
	@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> infection();

	/**
	 * @return &gamma;<sup>-1</sup>: mean period of recovery (in <em>R &larr;
	 *         &gamma; &middot; I</em>)
	 */
	@DefaultValue( 12 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> recovery();

	/** @return &nu;<sup>-1</sup>: mean period of growth/birth */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> birth();

	/**
	 * @return &mu;<sup>-1</sup>: mean period of death/mortality (incl.
	 *         fatality)
	 */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> death();

	/** @return mean period of import/immigration */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> immigration();

	/** @return mean period of export/emigration */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> emigration();

	/**
	 * @return &rho;<sup>-1</sup>: mean period of vaccination (acceptance in NL)
	 */
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> vaccination();

	/**
	 * @return &alpha;<sup>-1</sup>: mean period of natural/acquired/maternal
	 *         immunity waning
	 */
	@Key( WANING_KEY )
//		@DefaultValue( 10 + " " + TimeUnits.DAYS_LABEL )
	@ConverterClass( TimeQuantityConverter.class )
	Quantity<Time> waning();
}