/* $Id: 05b6ce8b9d1475149fa8866f8e92318183a552b1 $
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;

import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.reactivex.Observable;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.cbs.TimeUtil;

/**
 * {@link Cbs37201json} helps to import CBS table 37230ned data (JSON
 * formatted). See http://statline.cbs.nl/Statweb/selection/?PA=37201 and source
 * data at
 * http://opendata.cbs.nl/ODataFeed/odata/37201/UntypedDataSet?$format=json
 * (find a tutorial on OpenData operators etc. at
 * http://www.odata.org/getting-started/basic-tutorial/)
 * 
 * <p>
 * [jaarlijkse] Geboorte; kerncijfers vruchtbaarheid, leeftijd moeder, regio
 * [1988 - 2015; Perjaar; 2016-07-12T02:00:00]
 * 
 * <pre>
CBS.37201 (see http://statline.cbs.nl/Statweb/publication/?PA=37201)
[[2]]	: Regio's (CBS.37201.Meta$RegioS), e.g. with(CBS.37201.Meta$RegioS, Title[match(CBS.37201[1,2], Key)])="Nederland"
[[3]]	: Perioden (CBS.37201.Meta$Perioden), e.g. with(CBS.37201.Meta$Perioden, Title[match(CBS.37201[1,3], Key)])="1988"
[[4]]	: Totaal levend geboren kinderen ("TotaalLevendGeborenKinderen_1":Double), e.g. CBS.37201[1,4]="  186647"
[[5]]	: Levend geboren kinderen, relatief ("LevendGeborenKinderenRelatief_2":Double), e.g. CBS.37201[1,5]="    12.6"
[[6]]	: Levend geboren jongens ("LevendGeborenJongens_3":Double), e.g. CBS.37201[1,6]="   95474"
[[7]]	: Levend geboren meisjes ("LevendGeborenMeisjes_4":Double), e.g. CBS.37201[1,7]="   91173"
[[8]]	: Algemeen vruchtbaarheidscijfer ("AlgemeenVruchtbaarheidscijfer_5":Double), e.g. CBS.37201[1,8]="    47.4"
[[9]]	: Gestandaardiseerd vruchtbaarheidscijfer ("GestandaardiseerdVruchtbaarheidscijfer_6":Double), e.g. CBS.37201[1,9]="    47.4"
[[10]]	: Gemiddeld kindertal per vrouw ("GemiddeldKindertalPerVrouw_7":Double), e.g. CBS.37201[1,10]="    1.55"
	: Levend geboren kinderen: leeftijd moe...
[[11]]	: *  Totaal levend geboren kinderen ("TotaalLevendGeborenKinderen_8":Double), e.g. CBS.37201[1,11]="  186647"
[[12]]	: *  Jonger dan 20 jaar ("JongerDan20Jaar_9":Double), e.g. CBS.37201[1,12]="    3171"
[[13]]	: *  20 tot 25 jaar ("k_20Tot25Jaar_10":Double), e.g. CBS.37201[1,13]="   27323"
[[14]]	: *  25 tot 30 jaar ("k_25Tot30Jaar_11":Double), e.g. CBS.37201[1,14]="   76407"
[[15]]	: *  30 tot 35 jaar ("k_30Tot35Jaar_12":Double), e.g. CBS.37201[1,15]="   60527"
[[16]]	: *  35 tot 40 jaar ("k_35Tot40Jaar_13":Double), e.g. CBS.37201[1,16]="   16613"
[[17]]	: *  40 tot 45 jaar ("k_40Tot45Jaar_14":Double), e.g. CBS.37201[1,17]="    2392"
[[18]]	: *  45 jaar of ouder ("k_45JaarOfOuder_15":Double), e.g. CBS.37201[1,18]="     214"
	: Levend geboren kinderen: rangnummer
[[19]]	: *  Totaal levend geboren kinderen ("TotaalLevendGeborenKinderen_16":Double), e.g. CBS.37201[1,19]="  186647"
[[20]]	: *  1e kind ("k_1eKind_17":Double), e.g. CBS.37201[1,20]="   83209"
[[21]]	: *  2e kind ("k_2eKind_18":Double), e.g. CBS.37201[1,21]="   65216"
[[22]]	: *  3e kind ("k_3eKind_19":Double), e.g. CBS.37201[1,22]="   25601"
[[23]]	: *  4e of volgende kinderen ("k_4eOfVolgendeKinderen_20":Double), e.g. CBS.37201[1,23]="   12621"
	: Levend geboren kinderen: burgerlijke s..
[[24]]	: *  Totaal levend geboren kinderen ("TotaalLevendGeborenKinderen_21":Double), e.g. CBS.37201[1,24]="  186647"
[[25]]	: *  Moeder gehuwd ("MoederGehuwd_22":Double), e.g. CBS.37201[1,25]="  167696"
[[26]]	: *  Moeder niet-gehuwd ("MoederNietGehuwd_23":Double), e.g. CBS.37201[1,26]="   18951"
 * </pre>
 * 
 * @version $Id: 05b6ce8b9d1475149fa8866f8e92318183a552b1 $
 * @author Rick van Krevelen
 */
//@JsonIgnoreProperties( ignoreUnknown = false )
public class Cbs37201json
{

	private static final List<Object> JSON_KEYS = Stream
			.concat( Arrays.stream( CBSGender.values() ),
					Stream.concat( Arrays.stream( CBSMotherAgeRange.values() ),
							Arrays.stream( CBSBirthRank.values() ) ) )
			.collect( Collectors.toList() );

	private static int indexOf( final Object key )
	{
		for( int i = 0; i < JSON_KEYS.size(); i++ )
			if( JSON_KEYS.get( i ).equals( key ) ) return i;
		return Thrower.throwNew( IllegalArgumentException::new,
				() -> key + " not in keys: " + JSON_KEYS );
	}

	@JsonIgnore
	public Map<String, Object> props = new TreeMap<>();

	@JsonAnySetter
	public void put( final String key, final Object value )
	{
		this.props.put( key, value );
	}

	@JsonAnyGetter
	public Object value( final String key )
	{
		return this.props.get( key );
	}

	/** {@code RegioS} */
	@JsonProperty( "reg" )
	public String region;

	/** {@code Perioden} */
	@JsonProperty( "since" )
	@JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd" )
	public LocalDate offset;

	/** difference between consecutive offsets in days */
	@JsonProperty( "ndays" )
	public List<Integer> dayCounts;

	/** the population count, not required */
	@JsonProperty( "born" )
	public List<Integer> born;

	/**
	 * Assumes Jackson's mapped Java type for JSON arrays is {@link List}, i.e.
	 * {@link DeserializationFeature#USE_JAVA_ARRAY_FOR_JSON_ARRAY} is disabled
	 * or {@code false}
	 * 
	 * @param change
	 * @return
	 * @see DeserializationFeature#USE_JAVA_ARRAY_FOR_JSON_ARRAY
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends Number & Comparable<? super T>> List<T>
		frequenciesFor( final CBSPopulationDynamic change )
	{
		if( !this.props.containsKey( change.jsonKey() ) )
			return Thrower.throwNew( IllegalArgumentException::new,
					() -> "Key unavailable: " + change.jsonKey() + ", keys: "
							+ this.props.keySet() );
		return (List<T>) this.props.get( change.jsonKey() );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Number & Comparable<? super T>> List<T>
		countsFor( final int index )
	{
		return Arrays.stream( CBSPopulationDynamic.values() )
				.map( c -> (T) frequenciesFor( c ).get( index ) )
				.collect( Collectors.toList() );
	}

	public static class Category extends Tuple
	{

		/**
		 * {@link Category} constructor
		 * 
		 * @param values
		 */
		public Category( final Cbs37201json entry, final int index )
		{
			super( Arrays.asList(

					RegionPeriod.of( entry.region, entry.offset ) // key

					, entry.dayCounts.get( index ) // calculate frequency/rate
					, Tuple.of( entry.frequenciesFor( index ) ) // values
			) );
		}

		@Override
		public String toString()
		{
			return "cbs37230" + JsonUtil.stringify( this );
		}

		public RegionPeriod regionPeriod()
		{
			return (RegionPeriod) super.values().get( 0 );
		}

		@JsonProperty( "reg" )
		public String regionRef()
		{
			return (String) regionPeriod().regionRef();
		}

		public CBSRegionType regionType()
		{
			return CBSRegionType.parse( regionPeriod().regionRef() );
		}

		@JsonProperty( "per" )
		@JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy" )
		public LocalDate offset()
		{
			return regionPeriod().periodRef();
		}

		@JsonProperty( "dur" )
		public Integer dayCount()
		{
			return (Integer) super.values().get( 1 );
		}

		@JsonProperty( "frq" )
		@SuppressWarnings( "rawtypes" )
		public List<? extends Comparable> frequencies()
		{
			return ((Tuple) super.values().get( 2 )).values();
		}

		@JsonIgnore
		private ProbabilityDistribution<CBSGender> genderDistCache = null;

		public ProbabilityDistribution<CBSGender> genderDist(
			final Function<List<WeightedValue<CBSGender>>, ProbabilityDistribution<CBSGender>> distFact )
		{
			return this.genderDistCache == null
					? (this.genderDistCache = distFact
							.apply( weights( CBSGender.class ) ))
					: this.genderDistCache;
		}

		@JsonIgnore
		private ProbabilityDistribution<CBSMotherAgeRange> ageDistCache = null;

//		@SuppressWarnings( "unchecked" )
		public ProbabilityDistribution<CBSMotherAgeRange> ageDist(
			final Function<List<WeightedValue<CBSMotherAgeRange>>, ProbabilityDistribution<CBSMotherAgeRange>> distFact )
		{
			return this.ageDistCache == null
					? this.ageDistCache = distFact
							.apply( weights( CBSMotherAgeRange.class ) )
					: this.ageDistCache;
		}

		@JsonIgnore
		private ProbabilityDistribution<CBSBirthRank> rankDistCache = null;

//		@SuppressWarnings( "unchecked" )
		public ProbabilityDistribution<CBSBirthRank> rankDist(
			final Function<List<WeightedValue<CBSBirthRank>>, ProbabilityDistribution<CBSBirthRank>> distFact )
		{
			return this.rankDistCache == null
					? this.rankDistCache = distFact
							.apply( weights( CBSBirthRank.class ) )
					: this.rankDistCache;
		}

		public <E extends Enum<E>> List<WeightedValue<E>>
			weights( final Class<E> enumType )
		{
			@SuppressWarnings( "rawtypes" )
			final List<? extends Comparable> freq = frequencies();
			return Arrays.stream( enumType.getEnumConstants() )
					.map( g -> WeightedValue.of( g,
							(Number) freq.get( indexOf( g ) ) ) )
					.collect( Collectors.toList() );
		}

	}

	/**
	 * @param index
	 * @return
	 */
	private List<Integer> frequenciesFor( final int index )
	{
		return JSON_KEYS.stream()
				.map( g -> frequenciesFor( ((CBSJsonProperty) g).jsonKey() )
						.get( index ) )
				.collect( Collectors.toList() );
	}

	@SuppressWarnings( "unchecked" )
	public List<Integer> frequenciesFor( final String jsonKey )
	{
		return (List<Integer>) this.props.computeIfAbsent( jsonKey,
				key -> Collections.emptyList() );
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	@Deprecated
	public Stream<WeightedValue<Category>>
		asFrequencyStream( final Range<LocalDate> offsetRange )
	{
		return TimeUtil.indicesFor( this.offset, this.dayCounts, offsetRange )
				.entrySet().stream()
				.map( e -> WeightedValue.of( new Category( this, e.getValue() ),
						this.born.get( e.getValue() ) ) );
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	@Deprecated
	public static Stream<WeightedValue<Category>> readSync(
		final Callable<InputStream> json, final Range<LocalDate> offsetRange )
		throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays.stream( JsonUtil.valueOf( is, Cbs37201json[].class ) )
					.flatMap( tuple -> tuple.asFrequencyStream( offsetRange ) );
		}
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	public Observable<WeightedValue<Category>>
		asFrequencyObservable( final Range<LocalDate> offsetRange )
	{
		return Observable
				.fromIterable( TimeUtil
						.indicesFor( this.offset, this.dayCounts, offsetRange )
						.entrySet() )
				.map( e -> WeightedValue.of( new Category( this, e.getValue() ),
						this.born.get( e.getValue() ) ) );
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	public static Observable<WeightedValue<Category>> readAsync(
		final Callable<InputStream> json, final Range<LocalDate> offsetRange )
	{
		return JsonUtil.readArrayAsync( json, Cbs37201json.class )
				.flatMap( tuple -> tuple.asFrequencyObservable( offsetRange ) );
	}

}