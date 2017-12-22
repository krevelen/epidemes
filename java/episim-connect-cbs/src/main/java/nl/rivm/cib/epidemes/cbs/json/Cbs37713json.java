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
package nl.rivm.cib.epidemes.cbs.json;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Time;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;

/**
 * {@link Cbs37713json} helps to import CBS table 37713 data (JSON formatted).
 * See http://statline.cbs.nl/Statweb/selection/?PA=37713 and source data at
 * http://opendata.cbs.nl/ODataFeed/odata/37713/UntypedDataSet?$format=json
 * (find a tutorial on OpenData operators etc. at
 * http://www.odata.org/getting-started/basic-tutorial/)
 * 
 * <p>
 * Bevolking; leeftijd, herkomstgroepering, geslacht en regio, 1 januari [1996 -
 * 2016; Perjaar; 2016-10-05T02:00:00]
 * 
 * <pre>
 * CBS.37713
[[2]]	: Leeftijd (CBS.37713.Meta$Leeftijd $Key -> $Title)
[[3]]	: Herkomstgroepering (CBS.37713.Meta$Herkomstgroepering $Key -> $Title)
[[4]]	: Regio's (CBS.37713.Meta$RegioS $Key -> $Title)
[[5]]	: Perioden (CBS.37713.Meta$Perioden $Key -> $Title)
	: Totale bevolking (TopicGroup:NA)
[[6]]	: *  Mannen en vrouwen (Topic:Double)
[[7]]	: *  Mannen (Topic:Double)
[[8]]	: *  Vrouwen (Topic:Double)
	: Eerstegeneratieallochtoon (TopicGroup:NA)
[[9]]	: *  Mannen en vrouwen (Topic:Double)
[[10]]	: *  Mannen (Topic:Double)
[[11]]	: *  Vrouwen (Topic:Double)
	: Tweedegeneratieallochtoon (TopicGroup:NA)
[[12]]	: *  Mannen en vrouwen (Topic:Double)
[[13]]	: *  Mannen (Topic:Double)
[[14]]	: *  Vrouwen (Topic:Double)
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
//@JsonIgnoreProperties( ignoreUnknown = true )
public class Cbs37713json
{
	@JsonIgnore
	public Map<String, Object> props = new HashMap<>();

	@JsonAnySetter
	public void put( final String key, final Object value )
	{
		this.props.put( key, value );
	}

	/** {@code Perioden} as ISO8601 date */
//	@JsonProperty( "since" )
//	@JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd" )
//	public List<LocalDate> offsets;

	/** {@code Perioden} as duration (days) */
//	@JsonProperty( "ndays" )
//	public List<Integer> dayCounts;

	/** {@code Leeftijd}, range as int[] array */
	@JsonProperty( "age" )
	public int[] ages;

	/** {@code Herkomstgroepering}, transformed */
//	@JsonProperty( "ori" )
//	public String origin;

	/** {@code RegioS} */
	@JsonProperty( "reg" )
	public String region;

	/**
	 * {@link Category} is {@link Tuple} of entries in the {@link Cbs37713json}
	 * table with dimensions:
	 * <ol>
	 * <li>{@linkplain #regionId}/{@linkplain #regionType},
	 * <li>{@linkplain #gender}, and
	 * <li>{@linkplain #ageRange} (incl. cached {@linkplain #ageDist} for
	 * drawing individual ages)
	 * </ol>
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	public static class Category extends Tuple
	{

		/**
		 * {@link Category} constructor
		 * 
		 * @param values
		 */
		public Category( final Cbs37713json entry, final CBSGender gender )
		{
			super( Arrays.asList(

					// key 1
					entry.region

					// key 2: age bin
					, Range.of( entry.ages[0], entry.ages[0] + 5 )

					, gender // values

			) );
		}

		@Override
		public String toString()
		{
			return "hh" + JsonUtil.stringify( this );
		}

		@JsonProperty( "reg" )
		public String regionRef()
		{
			return (String) super.values().get( 0 );
		}

		public CBSRegionType regionType()
		{
			return CBSRegionType.parse( regionRef() );
		}

		@JsonProperty( "age_range" )
		@SuppressWarnings( "unchecked" )
		public Range<Integer> ageRange()
		{
			return (Range<Integer>) super.values().get( 1 );
		}

		@JsonProperty( "gender" )
		public CBSGender gender()
		{
			return (CBSGender) super.values().get( 2 );
		}

		@JsonIgnore
		private QuantityDistribution<Time> ageDistCache = null;

//		@SuppressWarnings( "unchecked" )
		public QuantityDistribution<Time> ageDist(
			final Function<Range<Integer>, ProbabilityDistribution<? extends Number>> distFact )
		{
			return this.ageDistCache == null
					? (this.ageDistCache = distFact.apply( ageRange() )
							.toQuantities( TimeUnits.YEAR ))
					: this.ageDistCache;
		}

	}

//	@SuppressWarnings( "unchecked" )
	public Number frequencyOf( final CBSGender gender )
	{
		return (Number) this.props.computeIfAbsent( gender.jsonKey(),
				key -> 0 );
	}

//	@SuppressWarnings( "unchecked" )
	public List<Number> frequencies()
	{
		return Arrays.stream( CBSGender.values() ).map( this::frequencyOf )
				.collect( Collectors.toList() );
	}

	public WeightedValue<Category> toWeightedValue( final CBSGender gender )
	{
		return WeightedValue.of( new Category( this, gender ),
				frequencyOf( gender ) );
	}

	@Deprecated
	public Stream<WeightedValue<Category>>
		asFrequencyStream( final Range<LocalDate> offsetRange )
	{
		return Arrays.stream( CBSGender.values() ).map( this::toWeightedValue );
	}

	@Deprecated
	public static Stream<WeightedValue<Category>> readSync(
		final Callable<InputStream> json, final Range<LocalDate> offsetRange )
		throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays.stream( JsonUtil.valueOf( is, Cbs37713json[].class ) )
					.flatMap( tuple -> tuple.asFrequencyStream( offsetRange ) );
		}
	}

	public Observable<WeightedValue<Category>>
		asFrequencyObservable( final Range<LocalDate> offsetRange )
	{
		return Observable.fromArray( CBSGender.values() )
				.map( this::toWeightedValue );
	}

	public static Observable<WeightedValue<Category>> readAsync(
		final Callable<InputStream> json, final Range<LocalDate> offsetRange )
	{
		return JsonUtil.readArrayAsync( json, Cbs37713json.class )
				.flatMap( tuple -> tuple.asFrequencyObservable( offsetRange ) );
	}
}