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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;
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
import io.reactivex.schedulers.Schedulers;
import nl.rivm.cib.episim.model.locate.Region;

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

	/** {@code Leeftijd}, range as int[] array */
	@JsonProperty( "age" )
	public int[] ages;

	/** {@code Herkomstgroepering}, transformed */
//	@JsonProperty( "ori" )
//	public String origin;

	/** {@code RegioS} */
	@JsonProperty( "reg" )
	public String region;

//	/** {@code Totale bevolking; Mannen} */
//	@JsonProperty( "mal" )
//	public int males = 0; // missing if 0

//	/** {@code Totale bevolking; Vrouwen} */
//	@JsonProperty( "fem" )
//	public int females = 0; // missing if 0

//	private static final String DUTCH = "nl";

//	private static Map<Integer, QuantityDistribution<Time>> AGE_DIST_CACHE = new TreeMap<>();

//	public Cbs37713json.Tuple
//		toTuple( final ProbabilityDistribution.Factory distFact )
//	{
//		final Cbs37713json.Tuple res = new Tuple();
//		res.ageDist = AGE_DIST_CACHE.computeIfAbsent( this.ages[0],
//				key -> distFact
//						.createUniformContinuous( key,
//								this.ages[1] < 0 ? 100 : this.ages[1] )
//						.toQuantities( TimeUnits.ANNUM ) );
////		res.alien = !DUTCH.equalsIgnoreCase( this.origin );
//		res.regionId = REGION_ID_CACHE.computeIfAbsent( this.region,
//				key -> Region.ID.of( this.region.trim() ) );
//		res.regionType = CBSRegionType.parse( this.region );
//		return res;
//	}
//
//	public static class Tuple
//	{
//		@JsonIgnore
//		public QuantityDistribution<Time> ageDist;
////		public boolean alien;
//		public Region.ID regionId;
//		public CBSRegionType regionType;
//
//		@Override
//		public String toString()
//		{
//			return JsonUtil.stringify( this );
//		}
//	}

	public int countFor( final CBSGender comp )
	{
		return (Integer) this.props.computeIfAbsent( comp.jsonKey, key -> 0 );
	}

	public enum CBSGender
	{
		MALE( "mal" ),

		FEMALE( "fem" ),

		;

		private final String jsonKey;

		private CBSGender( final String jsonKey )
		{
			this.jsonKey = jsonKey;
		}
	}

	public static class MyTuple extends Tuple
	{

		private static Map<String, Region.ID> REGION_ID_CACHE = new TreeMap<>();

		@JsonIgnore
		private QuantityDistribution<Time> ageDistCache = null;

		/**
		 * {@link MyTuple} constructor
		 * 
		 * @param values
		 */
		public MyTuple( final CBSGender gender, final Cbs37713json entry )
		{
			super( Arrays.asList(
					REGION_ID_CACHE.computeIfAbsent( entry.region,
							key -> Region.ID.of( entry.region.trim() ) ),
					gender, Range.of( entry.ages[0],
							entry.ages[1] < 0 ? 100 : entry.ages[1] ) ) );
		}

		@Override
		public String toString()
		{
			return "hh" + JsonUtil.stringify( this );
		}

		@JsonProperty( "region_id" )
		public Region.ID regionId()
		{
			return (Region.ID) super.values().get( 0 );
		}

		@JsonIgnore
		public CBSRegionType regionType()
		{
			return CBSRegionType.parse( regionId().unwrap() );
		}

		@JsonProperty( "gender" )
		public CBSGender gender()
		{
			return (CBSGender) super.values().get( 1 );
		}

		@JsonProperty( "age_range" )
		@SuppressWarnings( "unchecked" )
		public Range<Integer> ageRange()
		{
			return (Range<Integer>) super.values().get( 2 );
		}

		@SuppressWarnings( "unchecked" )
		public QuantityDistribution<Time> ageDist(
			final Function<Range<Integer>, ProbabilityDistribution<? extends Number>> distFact )
		{
			return this.ageDistCache == null
					? (this.ageDistCache = distFact.apply( ageRange() )
							.toQuantities( TimeUnits.YEAR ))
					: this.ageDistCache;
		}

	}

	public MyTuple toTuple( final CBSGender gender )
	{
		return new MyTuple( gender, this );
	}

	@Deprecated
	public Stream<WeightedValue<MyTuple>> asFrequencyStream()
	{
		return Arrays.stream( CBSGender.values() ).map( gender -> WeightedValue
				.of( toTuple( gender ), countFor( gender ) ) );
	}

	@Deprecated
	public static Stream<WeightedValue<MyTuple>>
		readSync( final Callable<InputStream> json ) throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays.stream( JsonUtil.valueOf( is, Cbs37713json[].class ) )
					.flatMap( tuple -> tuple.asFrequencyStream() );
		}
	}

	public Observable<WeightedValue<MyTuple>> asFrequencyObservable()
	{
		return Observable.fromArray( CBSGender.values() )
				.subscribeOn( Schedulers.computation() )
				.map( gender -> WeightedValue.of( toTuple( gender ),
						countFor( gender ) ) );
	}

	public static Observable<WeightedValue<MyTuple>>
		readAsync( final Callable<InputStream> json )
	{
		return JsonUtil.readArrayAsync( json, Cbs37713json.class )
				.flatMap( tuple -> tuple.asFrequencyObservable() );
	}
}