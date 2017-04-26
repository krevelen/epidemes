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
 * {@link Cbs71486json} helps to import CBS table 71486ned data (JSON
 * formatted). See http://statline.cbs.nl/Statweb/selection/?PA=71486ned and
 * source data at
 * http://opendata.cbs.nl/ODataFeed/odata/71486ned/UntypedDataSet?$format=json
 * (find a tutorial on OpenData operators etc. at
 * http://www.odata.org/getting-started/basic-tutorial/)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
//@JsonIgnoreProperties( ignoreUnknown = true )
public class Cbs71486json
{
	@JsonIgnore
	public Map<String, Object> props = new HashMap<>();

	@JsonAnySetter
	public void put( final String key, final Object value )
	{
		this.props.put( key, value );
	}

	public int countFor( final CBSHouseholdCompositionType comp )
	{
		return (Integer) this.props.get( comp.jsonKey() );
	}

	@JsonProperty( "per" )
	public String period;

	@JsonProperty( "reg" )
	public String region;

	@JsonProperty( "age" )
	public int[] ages;

	static class MyTuple extends Tuple
	{

		private static Map<String, Region.ID> REGION_ID_CACHE = new TreeMap<>();

		@JsonIgnore
		private QuantityDistribution<Time> ageDistCache = null;

		/**
		 * {@link MyTuple} constructor
		 * 
		 * @param values
		 */
		public MyTuple( final Cbs71486json entry, final CBSHousehold type )
		{
			super( Arrays.asList( type,
					REGION_ID_CACHE.computeIfAbsent( entry.region,
							key -> Region.ID.of( entry.region.trim() ) ),
					Range.of( entry.ages[0],
							entry.ages[1] < 0 ? 100 : entry.ages[1] ) ) );
		}

		@Override
		public String toString()
		{
			return "hh" + JsonUtil.stringify( this );
		}

		@JsonProperty( "hh_type" )
		public CBSHousehold householdType()
		{
			return (CBSHousehold) super.values().get( 0 );
		}

		@JsonIgnore
		public CBSRegionType regionType()
		{
			return CBSRegionType.parse( regionId().unwrap() );
		}

		@JsonProperty( "region_id" )
		public Region.ID regionId()
		{
			return (Region.ID) super.values().get( 1 );
		}

		@JsonProperty( "age_range" )
		@SuppressWarnings( "unchecked" )
		public Range<Integer> ageRange()
		{
			return (Range<Integer>) super.values().get( 2 );
		}

		public QuantityDistribution<Time> ageDist(
			final Function<Range<Integer>, QuantityDistribution<Time>> distFact )
		{
			return this.ageDistCache == null
					? (this.ageDistCache = distFact.apply( ageRange() ))
					: this.ageDistCache;
		}

		/**
		 * @param distFact the {@link ProbabilityDistribution.Factory} to use
		 * @param upperBound the upper bound for infinite extreme categories
		 * @return a uniform age distribution
		 */
		public QuantityDistribution<Time> ageDist(
			final ProbabilityDistribution.Factory distFact,
			final Integer upperBound )
		{
			return ageDist( range -> distFact
					.createUniformContinuous( range.getLower().getValue(),
							range.getUpper().isInfinity() ? upperBound
									: range.getUpper().getValue() )
					.toQuantities( TimeUnits.YEAR ) );
		}

		/**
		 * @param distFact the {@link ProbabilityDistribution.Factory} to use
		 * @return a uniform age distribution, with upper bound <=100 years
		 */
		public QuantityDistribution<Time>
			ageDist( final ProbabilityDistribution.Factory distFact )
		{
			return ageDist( distFact, 100 );
		}
	}

	public WeightedValue<MyTuple> toWeightedValue( final CBSHousehold type )
	{
		return WeightedValue.of( new MyTuple( this, type ), countFor( type ) );
	}

	@Deprecated
	public Stream<WeightedValue<MyTuple>> asFrequencyStream()
	{
		return Arrays.stream( CBSHousehold.values() )
				.filter( c -> !c.aggregate() ).map( c -> toWeightedValue( c ) );
	}

	@Deprecated
	public static Stream<WeightedValue<MyTuple>>
		readSync( final Callable<InputStream> json ) throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays.stream( JsonUtil.valueOf( is, Cbs71486json[].class ) )
					.flatMap( tuple -> tuple.asFrequencyStream() );
		}
	}

	public Observable<WeightedValue<MyTuple>> asFrequencyObservable()
	{
		return Observable.fromArray( CBSHousehold.values() )
				.subscribeOn( Schedulers.computation() )
				.filter( c -> !c.aggregate() ).map( c -> toWeightedValue( c ) );
	}

	public static Observable<WeightedValue<MyTuple>>
		readAsync( final Callable<InputStream> json )
	{
		return JsonUtil.readArrayAsync( json, Cbs71486json.class )
				.flatMap( tuple -> tuple.asFrequencyObservable() );
	}

}