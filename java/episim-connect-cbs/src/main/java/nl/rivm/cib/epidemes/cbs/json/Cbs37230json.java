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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;

import io.coala.exception.Thrower;
import io.coala.function.ThrowingFunction;
import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.TimeUnits;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import nl.rivm.cib.episim.cbs.RegionPeriod;
import nl.rivm.cib.episim.cbs.TimeUtil;

/**
 * {@link Cbs37230json} helps to import CBS table 37230ned data (JSON
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
public class Cbs37230json
{

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
		public Category( final Cbs37230json entry,
			final CBSPopulationDynamic metric, final int index )
		{
			super( Arrays.asList(

					RegionPeriod.of( entry.region,
							entry.offset.plusDays( IntStream.range( 0, index )
									.map( entry.dayCounts::get ).sum() ) ) // key

					, entry.dayCounts.get( index ) // calculate frequency/rate
					, metric, // value filter
					entry.frequenciesFor( metric ).get( index ) // value
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

		@JsonProperty( "kpi" )
		public CBSPopulationDynamic metric()
		{
			return (CBSPopulationDynamic) super.values().get( 2 );
		}

		@JsonProperty( "val" )
		public Integer value()
		{
			return (Integer) super.values().get( 3 );
		}

		@JsonIgnore
		private QuantityDistribution<Time> timeDistCache = null;

		/**
		 * @param change the metric
		 * @param distFact distribution factory taking the daily average value,
		 *            e.g.
		 *            {@link ProbabilityDistribution.Factory#createExponential(Number)}
		 * @return a time distribution of the duration between events
		 */
//		@SuppressWarnings( "unchecked" )
		public QuantityDistribution<Time> timeDist(
			final Function<BigDecimal, ProbabilityDistribution<Double>> distFact )
		{
			return this.timeDistCache == null
					? (this.timeDistCache = distFact
							.apply( DecimalUtil.divide( dayCount(), value() ) )
							.toQuantities( TimeUnits.DAYS ))
					: this.timeDistCache;
		}

	}

	private WeightedValue<Category>
		toWeightedValue( final CBSPopulationDynamic metric, final int index )
	{
		return WeightedValue.of( new Category( this, metric, index ),
				frequenciesFor( metric ).get( index ) );
	}

	/**
	 * @param metric the {@linkCBSPopulationChange} frequency metric to use as
	 *            weights
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	@Deprecated
	public Stream<WeightedValue<Category>> asFrequencyStream(
		final CBSPopulationDynamic metric, final Range<LocalDate> offsetRange )
	{
		return TimeUtil.indicesFor( this.offset, this.dayCounts, offsetRange )
				.values().stream().map( e -> toWeightedValue( metric, e ) );
	}

	/**
	 * @param metric the {@linkCBSPopulationChange} frequency metric to use as
	 *            weights
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	@Deprecated
	public static Stream<WeightedValue<Category>> readSync(
		final Callable<InputStream> json, final CBSPopulationDynamic metric,
		final Range<LocalDate> offsetRange ) throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays.stream( JsonUtil.valueOf( is, Cbs37230json[].class ) )
					.flatMap( tuple -> tuple.asFrequencyStream( metric,
							offsetRange ) );
		}
	}

	/**
	 * @param metric the {@linkCBSPopulationChange} frequency metric to use as
	 *            weights
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	public Observable<WeightedValue<Category>> asFrequencyObservable(
		final CBSPopulationDynamic metric, final Range<LocalDate> offsetRange )
	{
		return Observable
				.fromIterable( TimeUtil
						.indicesFor( this.offset, this.dayCounts, offsetRange )
						.values() )
				.map( e -> toWeightedValue( metric, e ) );
	}

	/**
	 * @param weightedBy the {@linkCBSPopulationChange} frequency metric to use
	 *            as weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	public static Observable<WeightedValue<Category>> readAsync(
		final Callable<InputStream> json, final Range<LocalDate> offsetRange,
		final Observable<CBSPopulationDynamic> metrics )
	{
		return JsonUtil.readArrayAsync( json, Cbs37230json.class )
				.flatMap( tuple -> metrics.flatMap( metric -> tuple
						.asFrequencyObservable( metric, offsetRange ) ) );
	}

	/**
	 * @param metrics the {@linkCBSPopulationChange} frequency metrics to use as
	 *            weights, or null for {@link #populationAtStart}
	 * @param offsetRange the offset range, or {@code null} for all available
	 * @return the resolved/truncated offsets and respective weighted tuples
	 */
	public static Observable<WeightedValue<Category>> readAsync(
		final Callable<InputStream> json, final Range<LocalDate> offsetRange,
		final CBSPopulationDynamic... metrics )
	{
		return readAsync( json, offsetRange,
				metrics == null || metrics.length == 0
						? Observable.just( CBSPopulationDynamic.POP )
						: Observable.fromArray( metrics ) );
	}

	/**
	 * historic an local demography event rates (births, deaths, migrations,
	 * ...)
	 */
	public static class EventProducer
	{
		final BigDecimal scalingFactor;
		final ProbabilityDistribution.Factory distFact;
		ConditionalDistribution<Category, LocalDate> nationalFreqDist;
		ConditionalDistribution<Category, LocalDate> siteDist;

		// <LocalDate, WeightedValue<Category>>
		public EventProducer( final CBSPopulationDynamic metric,
			final ProbabilityDistribution.Factory distFact,
			final Callable<InputStream> data,
			final CBSRegionType cbsRegionLevel, final Range<LocalDate> dtRange,
			final BigDecimal scalingFactor )
		{
			this.scalingFactor = scalingFactor;
			this.distFact = distFact;
			readAsync( data, dtRange, metric )
					.groupBy( wv -> wv.getValue().regionType() )
					.filter( g -> g.getKey() == cbsRegionLevel
							|| g.getKey() == CBSRegionType.COUNTRY )
					// GroupedObservable<CBSRegionType, WeightedValue<Category>>
					.blockingForEach( g ->
					{
						// event locations at configured regional level
						if( g.getKey() == cbsRegionLevel )
							this.siteDist = create( g );

						// event rates at national level, scaled to synth.pop.size?
						if( g.getKey() == CBSRegionType.COUNTRY )
							this.nationalFreqDist = create( g );
					} );
		}

		private ConditionalDistribution<Category, LocalDate> create(
			final GroupedObservable<CBSRegionType, WeightedValue<Category>> g )
		{
			// Navigable TreeMap to resolve out-of-bounds conditions
			return ConditionalDistribution
					.of( this.distFact::createCategorical,
							g.toMultimap( wv -> wv.getValue().offset(),
									wv -> wv, () -> new TreeMap<>() )
									.blockingGet() );
		}

		public Quantity<Time> nextDelay( final LocalDate dt,
			final ThrowingFunction<String, Integer, Exception> eventSitePersonCounter )
			throws Exception
		{
			final String regRef = this.siteDist.draw( dt ).regionPeriod()
					.regionRef();
			final int n = eventSitePersonCounter.apply( regRef );
			final QuantityDistribution<Time> timeDist = this.nationalFreqDist
					.draw( dt )
					.timeDist( freq -> this.distFact.createExponential(
							DecimalUtil.divide( freq, this.scalingFactor ) ) );
			Quantity<Time> dur = QuantityUtil.zero( Time.class );
			for( int i = 0; i < n; i++ )
				dur = dur.add( timeDist.draw() );
			return dur;
		}
	}
}