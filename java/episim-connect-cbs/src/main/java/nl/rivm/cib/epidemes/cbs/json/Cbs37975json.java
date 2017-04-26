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
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * {@link Cbs37975json} helps to import CBS table 37975 data (JSON formatted).
 * See http://statline.cbs.nl/Statweb/selection/?PA=37975 and source data at
 * http://opendata.cbs.nl/ODataFeed/odata/37975/UntypedDataSet?$format=json
 * (find a tutorial on OpenData operators etc. at
 * http://www.odata.org/getting-started/basic-tutorial/)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
//@JsonIgnoreProperties( ignoreUnknown = true )
public class Cbs37975json
{
	@JsonIgnore
	public Map<String, Object> props = new HashMap<>();

	@JsonAnySetter
	public void put( final String key, final Object value )
	{
		this.props.put( key, value );
	}

	public int countFor( final CBSHousehold comp )
	{
		return (Integer) this.props.get( comp.jsonKey );
	}

	@JsonProperty( "per" )
	public String period;

	@JsonProperty( "age" )
	public String referentAge;

	public Tuple toKeyTuple( final CBSHousehold type )
	{
		return Tuple.of( type, this.referentAge );
	}

	public WeightedValue<Tuple> toWeightedValue( final CBSHousehold type )
	{
		return WeightedValue.of( toKeyTuple( type ), countFor( type ) );
	}

	@Deprecated
	public Stream<WeightedValue<Tuple>> asFrequencyStream()
	{
		return Arrays.stream( CBSHousehold.values() )
				.filter( c -> !c.aggregate() ).map( c -> toWeightedValue( c ) );
	}

	@SuppressWarnings( "deprecation" )
	@Deprecated
	public static Stream<WeightedValue<Tuple>>
		readSync( final Callable<InputStream> json ) throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays.stream( JsonUtil.valueOf( is, Cbs37975json[].class ) )
					.flatMap( tuple -> tuple.asFrequencyStream() );
		}
	}

	public Observable<WeightedValue<Tuple>> asFrequencyObservable()
	{
		return Observable.fromArray( CBSHousehold.values() )
				.subscribeOn( Schedulers.computation() )
				.filter( c -> !c.aggregate() ).map( c -> toWeightedValue( c ) );
	}

	public static Observable<WeightedValue<Tuple>>
		readAsync( final Callable<InputStream> json )
	{
		return JsonUtil.readArrayAsync( json, Cbs37975json.class )
				.flatMap( tuple -> tuple.asFrequencyObservable() );
	}

}