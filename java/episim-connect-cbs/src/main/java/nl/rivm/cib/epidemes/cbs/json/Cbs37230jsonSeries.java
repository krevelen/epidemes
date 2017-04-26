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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Time;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;

import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;
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
 * {@link Cbs37230jsonSeries} helps to import CBS table 37230ned data (JSON
 * formatted). See http://statline.cbs.nl/Statweb/selection/?PA=37230ned and
 * source data at
 * http://opendata.cbs.nl/ODataFeed/odata/37230ned/UntypedDataSet?$format=json
 * (find a tutorial on OpenData operators etc. at
 * http://www.odata.org/getting-started/basic-tutorial/)
 * 
 * <p>
 * Bevolkingsontwikkeling; regio per maand [Januari 2002 - februari 2017*;
 * Permaand; 2017-04-07T02:00:00]
 * 
 * <pre>
 * [[2]]	: Regio's (CBS.37230ned.Meta$RegioS), e.g. with(CBS.37230ned.Meta$RegioS, Title[match(CBS.37230ned[1,2], Key)])="Nederland"
[[3]]	: Perioden (CBS.37230ned.Meta$Perioden), e.g. with(CBS.37230ned.Meta$Perioden, Title[match(CBS.37230ned[1,3], Key)])="2002 januari"
	: Bevolkingsontwikkeling
[[4]]	: *  Bevolking aan het begin van de periode ("BevolkingAanHetBeginVanDePeriode_1":Double), e.g. CBS.37230ned[1,4]="16105285"
[[5]]	: *  Levendgeborenen ("Levendgeborenen_2":Double), e.g. CBS.37230ned[1,5]="   17019"
[[6]]	: *  Overledenen ("Overledenen_3":Double), e.g. CBS.37230ned[1,6]="   13469"
	: *  Vestiging in de gemeente
[[7]]	: *  *  Totale vestiging ("TotaleVestiging_4":Double), e.g. CBS.37230ned[1,7]="   66547"
[[8]]	: *  *  Vestiging vanuit een andere gemeente ("VestigingVanuitEenAndereGemeente_5":Double), e.g. CBS.37230ned[1,8]="   55181"
[[9]]	: *  *  Vestiging vanuit het buitenland ("VestigingVanuitHetBuitenland_6":Double), e.g. CBS.37230ned[1,9]="   11366"
	: *  Vertrek uit de gemeente
[[10]]	: *  *  Totaal vertrek (inclusief administrat... ("TotaalVertrekInclusiefAdministrat_7":Double), e.g. CBS.37230ned[1,10]="   62482"
[[11]]	: *  *  Vertrek naar een andere gemeente ("VertrekNaarEenAndereGemeente_8":Double), e.g. CBS.37230ned[1,11]="   55181"
	: *  *  Vertrek naar buitenland
[[12]]	: *  *  *  Vertrek naar buitenland inclusief adm... ("VertrekNaarBuitenlandInclusiefAdm_9":Double), e.g. CBS.37230ned[1,12]="    7301"
[[13]]	: *  *  *  Vertrek naar buitenland exclusief adm... ("VertrekNaarBuitenlandExclusiefAdm_10":Double), e.g. CBS.37230ned[1,13]="    5456"
	: *  *  Administratieve correcties
[[14]]	: *  *  *  Saldo administratieve correcties ("SaldoAdministratieveCorrecties_11":Double), e.g. CBS.37230ned[1,14]="   -1845"
[[15]]	: *  *  *  Administratieve opnemingen ("AdministratieveOpnemingen_12":Double), e.g. CBS.37230ned[1,15]="    1847"
[[16]]	: *  *  *  Administratieve afvoeringen ("AdministratieveAfvoeringen_13":Double), e.g. CBS.37230ned[1,16]="    3692"
	: *  Bevolkingsgroei
[[17]]	: *  *  Bevolkingsgroei ("Bevolkingsgroei_14":Double), e.g. CBS.37230ned[1,17]="    7615"
[[18]]	: *  *  Bevolkingsgroei, relatief ("BevolkingsgroeiRelatief_15":Double), e.g. CBS.37230ned[1,18]="    0.05"
[[19]]	: *  *  Bevolkingsgroei sinds 1 januari ("BevolkingsgroeiSinds1Januari_16":Double), e.g. CBS.37230ned[1,19]="    7615"
[[20]]	: *  *  Bevolkingsgroei sinds 1 januari, rela... ("BevolkingsgroeiSinds1JanuariRela_17":Double), e.g. CBS.37230ned[1,20]="    0.05"
[[21]]	: *  Bevolking aan het einde van de periode ("BevolkingAanHetEindeVanDePeriode_18":Double), e.g. CBS.37230ned[1,21]="16112900"
	: Regionale coderingen
[[22]]	: *  Gemeente ("Gemeente_19":String), e.g. CBS.37230ned[1,22]="NA"
[[23]]	: *  Landsdeel ("Landsdeel_20":String), e.g. CBS.37230ned[1,23]="NA"
[[24]]	: *  Provincie ("Provincie_21":String), e.g. CBS.37230ned[1,24]="NA"
[[25]]	: *  COROP-gebied ("COROPGebied_22":String), e.g. CBS.37230ned[1,25]="NA"
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
//@JsonIgnoreProperties( ignoreUnknown = false )
public class Cbs37230jsonSeries
{

	/** time zone used for timestamps of local dates in the Dutch census data */
	public static final ZoneId NL_TZ = ZoneId.of( "CET" );

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

//	public static class CBSRegionCodes
//	{
//		@JsonProperty( "gm" )
//		public Integer municipality;
//		@JsonProperty( "cr" )
//		public Integer corop;
//		@JsonProperty( "pv" )
//		public Integer province;
//		@JsonProperty( "ld" )
//		public Integer territory;
//	}

	public static enum CBSPopulationChange
	{
		/** */
		BIRTHS( "births" ),
		/** */
		DEATHS_PERCENT( "deaths" ),
		/** */
		FROM_MUNICIPALITY_PERCENT( "im_gm" ),
		/** */
		TO_MUNICIPALITY_PERCENT( "em_gm" ),
		/** */
		IMMIGRATION_PERCENT( "im_nl" ),
		/** */
		EMIGRATION_PERCENT( "em_nl" ),
		//
		;

		private final String jsonKey;

		private CBSPopulationChange( final String jsonKey )
		{
			this.jsonKey = jsonKey;
		}
	}

	/** {@code RegioS} */
	@JsonProperty( "reg" )
	public String region;

	/** {@code Perioden} */
	@JsonProperty( "per_offset_date" )
	@JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd" )
	public LocalDate offset;

	/** difference between consecutive offsets in days */
	@JsonProperty( "per_length_days" )
	public List<Integer> dayCounts;

	/** difference between consecutive offsets in days */
	@JsonProperty( "pop_start" )
	public List<Integer> populationAtStart;

	/** (reconstructed) offsets */
	@JsonIgnore
	public List<ZonedDateTime> offsets = null;

	/** (derived) offset-to-index mapping */
	@JsonIgnore
	private NavigableMap<ZonedDateTime, Integer> indices = null;

	private Collection<Integer>
		indicesFor( final Range<ZonedDateTime> offsetRange )
	{
		// Java8 Time conversions: http://stackoverflow.com/a/23197731/1418999
		if( this.indices == null ) // reconstruct all offsets and respective indices
		{
			this.indices = new TreeMap<>();
			final ZonedDateTime[] offsets = new ZonedDateTime[this.dayCounts
					.size()];
			ZonedDateTime dt = this.offset.atStartOfDay( NL_TZ );
			for( int i = 0; i < this.dayCounts.size(); dt = dt
					.plus( this.dayCounts.get( i++ ), ChronoUnit.DAYS ) )
			{
				offsets[i] = dt;
				this.indices.put( dt, i );
			}
			this.offsets = Arrays.asList( offsets );
		}
		if( offsetRange == null ) return this.indices.values();
		if( offsetRange.isLessThan( this.indices.firstKey() ) )
			return Collections
					.singleton( this.indices.firstEntry().getValue() );
		if( offsetRange.isGreaterThan( this.indices.lastKey() ) )
			return Collections.singleton( this.indices.lastEntry().getValue() );
		final ZonedDateTime from = offsetRange.getLower().isInfinity()
				? this.indices.firstKey() : offsetRange.getLower().getValue();
		final ZonedDateTime to = offsetRange.getUpper().isInfinity()
				? this.indices.lastKey() : offsetRange.getUpper().getValue();
		return this.indices.subMap( from, to ).values();
	}

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
		countsFor( final CBSPopulationChange change )
	{
		if( !this.props.containsKey( change.jsonKey ) )
			return Thrower.throwNew( IllegalArgumentException.class,
					"Key unavailable: {}, keys: {}", change.jsonKey,
					this.props.keySet() );
		return (List<T>) this.props.get( change.jsonKey );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Number & Comparable<? super T>> List<T>
		countsFor( final int index )
	{
		return Arrays.stream( CBSPopulationChange.values() )
				.map( c -> (T) countsFor( c ).get( index ) )
				.collect( Collectors.toList() );
	}

	static class MyTuple extends Tuple
	{

		private static Map<String, Region.ID> REGION_ID_CACHE = new TreeMap<>();

		/**
		 * {@link MyTuple} constructor
		 * 
		 * @param values
		 */
		public MyTuple( final Cbs37230jsonSeries entry, final int index )
		{
			super( Arrays.asList( REGION_ID_CACHE.computeIfAbsent( entry.region,
					key -> Region.ID.of( entry.region.trim() ) )
			//
					, entry.offsets.get( index ) // for filtering
					, entry.populationAtStart.get( index ) // needed for fraction
					, entry.dayCounts.get( index ) // needed for frequency
					, Tuple.of( (List<? extends Comparable<?>>) entry
							.countsFor( index ) ) // needed for distributions
			) );
		}

		@Override
		public String toString()
		{
			return "chg" + JsonUtil.stringify( this );
		}

		@JsonProperty( "reg" )
		public Region.ID regionId()
		{
			return (Region.ID) super.values().get( 0 );
		}

		public CBSRegionType regionType()
		{
			return CBSRegionType.parse( regionId().unwrap() );
		}

		@JsonProperty( "offset" )
		public ZonedDateTime offset()
		{
			return (ZonedDateTime) super.values().get( 1 );
		}

		@JsonProperty( "pop" )
		public Integer populationAtStart()
		{
			return (Integer) super.values().get( 2 );
		}

		@JsonProperty( "dur" )
		public Integer dayCount()
		{
			return (Integer) super.values().get( 3 );
		}

		@JsonProperty( "chg" )
		@SuppressWarnings( "unchecked" )
		public List<?> counts()
		{
			return ((Tuple) super.values().get( 4 )).values();
		}

		public Number countFor( final CBSPopulationChange change )
		{
			return (Number) counts().get( change.ordinal() );
		}

		@JsonIgnore
		private Map<CBSPopulationChange, QuantityDistribution<Time>> timeDistCache = null;

		/**
		 * @param change the metric
		 * @param distFact distribution factory taking the daily average value,
		 *            e.g.
		 *            {@link ProbabilityDistribution.Factory#createExponential(Number)}
		 * @return a time distribution of the duration between events
		 */
		@SuppressWarnings( "unchecked" )
		public QuantityDistribution<Time> timeDist(
			final CBSPopulationChange change,
			final Function<BigDecimal, ProbabilityDistribution<Double>> distFact )
		{
			final Map<CBSPopulationChange, QuantityDistribution<Time>> cache = this.timeDistCache == null
					? (this.timeDistCache = new EnumMap<>(
							CBSPopulationChange.class ))
					: this.timeDistCache;
			return cache.computeIfAbsent( change,
					key -> distFact
							.apply( DecimalUtil.divide( countFor( change ),
									dayCount() ) )
							.toQuantities( TimeUnits.DAYS ) );
		}

	}

	public MyTuple toTuple( final int index )
	{
		return new MyTuple( this, index );
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	@Deprecated
	public Stream<WeightedValue<MyTuple>> asFrequencyStream(
		final CBSPopulationChange weightedBy,
		final Range<ZonedDateTime> offsetRange )
	{
		final List<? extends Number> weights = weightedBy == null
				? this.populationAtStart : countsFor( weightedBy );
		return indicesFor( offsetRange ).stream()
				.map( i -> WeightedValue.of( toTuple( i ), weights.get( i ) ) );
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	@Deprecated
	public static Stream<WeightedValue<MyTuple>> readSync(
		final Callable<InputStream> json, final CBSPopulationChange weightedBy,
		final Range<ZonedDateTime> offsetRange ) throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays
					.stream( JsonUtil.valueOf( is,
							Cbs37230jsonSeries[].class ) )
					.flatMap( tuple -> tuple.asFrequencyStream( weightedBy,
							offsetRange ) );
		}
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	public Observable<WeightedValue<MyTuple>> asFrequencyObservable(
		final CBSPopulationChange weightedBy,
		final Range<ZonedDateTime> offsetRange )
	{
		final List<? extends Number> weights = weightedBy == null
				? this.populationAtStart : countsFor( weightedBy );
		return Observable.fromIterable( indicesFor( offsetRange ) )
				.subscribeOn( Schedulers.computation() )
				.map( i -> WeightedValue.of( toTuple( i ), weights.get( i ) ) );
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	public static Observable<WeightedValue<MyTuple>> readAsync(
		final Callable<InputStream> json, final CBSPopulationChange weightedBy,
		final Range<ZonedDateTime> offsetRange )
	{
		return JsonUtil.readArrayAsync( json, Cbs37230jsonSeries.class )
				.flatMap( tuple -> tuple.asFrequencyObservable( weightedBy,
						offsetRange ) );
	}
}