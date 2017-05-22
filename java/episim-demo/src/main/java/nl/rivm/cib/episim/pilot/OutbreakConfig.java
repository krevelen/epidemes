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

import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.measure.quantity.Time;

import io.coala.config.YamlConfig;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.time.TimeUnits;
import io.coala.util.InputStreamConverter;
import io.reactivex.internal.functions.Functions;
import io.reactivex.observables.GroupedObservable;
import nl.rivm.cib.epidemes.cbs.json.CBSPopulationDynamic;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.Cbs37201json;
import nl.rivm.cib.epidemes.cbs.json.Cbs37230json;
import nl.rivm.cib.epidemes.cbs.json.Cbs71486json;
import nl.rivm.cib.epidemes.cbs.json.CbsNeighborhood;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.locate.Region.ID;
import tec.uom.se.ComparableQuantity;

public interface OutbreakConfig extends YamlConfig
{
	@DefaultValue( "" + 1000 )
	int popSize();

	@DefaultValue( "(-inf:50]" )
	String momAge();

	@DefaultValue( "GM0363" )
	Region.ID fallbackRegionRef();

	@DefaultValue( "0 0 * * * ?" )
	String statisticsRecurrence();

	@DefaultValue( "MUNICIPAL" )
	CBSRegionType cbsRegionLevel();

	@DefaultValue( "cbs/pc6_buurt.json" )
	@ConverterClass( InputStreamConverter.class )
	InputStream cbsNeighborhoodsData();

	default Map<ID, Collection<WeightedValue<CbsNeighborhood>>>
		cbsNeighborhoods()
	{
		final CBSRegionType cbsRegionLevel = this.cbsRegionLevel();
		return CbsNeighborhood.readAsync( this::cbsNeighborhoodsData )
				.toMultimap( bu -> bu.regionRef( cbsRegionLevel ),
						CbsNeighborhood::toWeightedValue )
				.blockingGet();
	}

	@DefaultValue( "cbs/37201_TS_2010_2015.json" )
	@ConverterClass( InputStreamConverter.class )
	InputStream cbs37201Data();

	default Map<RegionPeriod, Collection<WeightedValue<Cbs37201json.Category>>>
		cbs37201( final Range<LocalDate> timeFilter )
	{
		final CBSRegionType cbsRegionLevel = this.cbsRegionLevel();
		return Cbs37201json.readAsync( this::cbs37201Data, timeFilter )
				.filter( wv -> wv.getValue().regionType() == cbsRegionLevel )
				.toMultimap( wv -> wv.getValue().regionPeriod(),
						Functions.identity(),
						// Navigable Map for resolving out-of-bounds conditions
						TreeMap::new )
				.blockingGet();
	}

	@DefaultValue( "cbs/71486ned-TS-2010-2016.json" )
	@ConverterClass( InputStreamConverter.class )
	InputStream cbs71486Data();

	default Map<RegionPeriod, Collection<WeightedValue<Cbs71486json.Category>>>
		cbs71486( final Range<LocalDate> timeFilter )
	{
		final CBSRegionType cbsRegionLevel = this.cbsRegionLevel();
		return Cbs71486json.readAsync( this::cbs71486Data, timeFilter )
				.filter( wv -> wv.getValue().regionType() == cbsRegionLevel )
				.toMultimap( wv -> wv.getValue().regionPeriod(),
						Functions.identity(), TreeMap::new )
				.blockingGet();
	}

	@DefaultValue( "cbs/37230ned_TS_2012_2017.json" )
	@ConverterClass( InputStreamConverter.class )
	InputStream cbs37230Data();

	default void cbs37230( final Range<LocalDate> timeFilter,
		final BiFunction<CBSPopulationDynamic, CBSRegionType, Consumer<Map<LocalDate, Collection<WeightedValue<Cbs37230json.Category>>>>> timeseriesConsumer,
		final CBSPopulationDynamic... metrics )
	{
		Arrays.stream( metrics == null || metrics.length == 0
				? CBSPopulationDynamic.values() : metrics )
				.filter( metric -> metric != CBSPopulationDynamic.POP )
				.forEach( metric ->
		// FIXME parse and group by all metrics in a single parsing run

		Cbs37230json.readAsync( this::cbs37230Data, timeFilter, metric )
				.groupBy(
						wv -> wv.getValue().regionType() )
				.blockingForEach(
						( GroupedObservable<CBSRegionType, WeightedValue<Cbs37230json.Category>> g ) -> timeseriesConsumer
								.apply( metric,
										g.getKey() )
								.accept( g
										.toMultimap(
												wv -> wv.getValue().offset(),
												wv -> wv, TreeMap::new )
										.blockingGet() ) ) );
	}

	default Range<ComparableQuantity<Time>> momAgeRange() throws ParseException
	{
		return Range.parse( this.momAge() )
				.map( yr -> QuantityUtil.valueOf( yr, TimeUnits.ANNUM ) );
	}

	@DefaultValue( "cbs/37713_2016JJ00_AllOrigins.json" )
	@ConverterClass( InputStreamConverter.class )
	InputStream cbs37713();

//	this.genderOriginDist = ConditionalDistribution.of(
//			this.distFact::createCategorical,
//			Cbs37713json.readAsync( this.config::cbs37713, this.timeRange )
//					.filter( wv -> wv.getValue()
//							.regionType() == this.cbsRegionLevel )
//					.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
//							() -> new TreeMap<>() )
//					.blockingGet() );

//	Cbs37975json
//			.readAsync( this.config::cbs37975 )
//			.toList().blockingGet();

//		@DefaultValue( "cbs/37975_2016JJ00.json" )
//		@ConverterClass( InputStreamConverter.class )
//		InputStream cbs37975();
}