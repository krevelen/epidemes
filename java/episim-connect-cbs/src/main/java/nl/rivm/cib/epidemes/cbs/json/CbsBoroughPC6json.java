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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.math.WeightedValue;
import io.coala.random.ProbabilityDistribution;
import io.reactivex.Observable;
import nl.rivm.cib.episim.model.ZipCode;
import nl.rivm.cib.episim.model.locate.Region;

/**
 * {@link CbsBoroughPC6json} helps to import zip/burough data (JSON formatted).
 * See
 * https://www.cbs.nl/nl-nl/maatwerk/2016/38/dominante-buurtcode-2016-bij-postcode-en-huisnummer-combinatie
 * 
 * <p>
 * Generated with R script:
 * 
 * <pre>
require(c('data.table','jsonlite'))
# 0. download <a href=https://www.cbs.nl/-/media/_excel/2016/38/2016-cbs-pc6huisnr20160801_buurt.zip>zipped CSV</a> (23MB) with 5 vars/columns: 2x GAB + 3x GM/WK/BU
# 1 (eta: minutes) read unzipped CSV of 215MB (~7.5M obs x 5 vars)
GAB_GWB <- as.data.table( read.csv2( 'pc6hnr20160801_gwb.csv' ) )
GAB_GWD.Json <- GAB_GWB[ # 2a. (eta: sec) addresses per zip (~483K obs x 5 vars)
  ,.(addr_count=.N), by=.(gemeentecode,wijkcode,gwb2016_code,pc6)
  ][ # 2b. (eta: minutes) flatten counts per borough (~12.8K obs x 3 vars)
  ,.(
    reg=sprintf('BU%08d',gwb2016_code)
    ,zip_count=list(as.list(setNames(addr_count,pc6)))
    ,codes=list(setNames(c(gemeentecode,wijkcode %% 100,gwb2016_code %% 100),c('gm','wk','bu')))
  ), by=.(gemeentecode,wijkcode,gwb2016_code)
  ][ # 2c. (eta: seconds) clean-up
  , ':=' (gemeentecode=NULL, wijkcode=NULL, gwb2016_code=NULL)
  ]
# 3. (eta: minutes) generate and write JSON format file (~11MB)
write( jsonlite::toJSON( GAB_GWD.Json, pretty=T, auto_unbox=T ), file=destFile )
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
//@JsonIgnoreProperties( ignoreUnknown = false )
public class CbsBoroughPC6json
{

	/** the borough code */
	@JsonProperty( "reg" )
	public String borough;

	/** the address count per zip code */
	@JsonProperty( "zip_count" )
	public Map<ZipCode, Integer> zipCounts;

	/** the region codes (GM,WK,BU) */
	@JsonProperty( "codes" )
	public Integer[] codes;

	@Override
	public String toString()
	{
		return "bu2pc6" + JsonUtil.stringify( this );
	}

	protected static Map<String, Region.ID> REGION_ID_CACHE = new TreeMap<>();

	protected static Region.ID toRef( final CBSRegionType regType,
		final Object... args )
	{
		return REGION_ID_CACHE.computeIfAbsent( regType.toString( args ),
				key -> Region.ID.of( key ) );
	}

	public Region.ID regionRef( final CBSRegionType type )
	{
		switch( type )
		{
		case MUNICIPAL:
			return municipalRef();
		case WARD:
			return wardRef();
		default:
			return ref();
		}
	}

	public Region.ID municipalRef()
	{
		return toRef( CBSRegionType.MUNICIPAL, this.codes[0] );
	}

	public Region.ID wardRef()
	{
		return toRef( CBSRegionType.WARD, this.codes[0], this.codes[1] );
	}

	public Region.ID ref()
	{
		return toRef( CBSRegionType.BOROUGH, (Object[]) this.codes );
	}

	@JsonIgnore
	protected ProbabilityDistribution<ZipCode> zipDistCache = null;

	public ProbabilityDistribution<ZipCode> zipDist(
		final Function<List<WeightedValue<ZipCode>>, ProbabilityDistribution<ZipCode>> distFact )
	{
		return this.zipDistCache == null
				? (this.zipDistCache = distFact
						.apply( WeightedValue.listOf( this.zipCounts ) ))
				: this.zipDistCache;
	}

	public int zipCount()
	{
		return this.zipCounts.values().stream().reduce( ( i1, i2 ) -> i1 + i2 )
				.orElse( 0 );
	}

	public WeightedValue<CbsBoroughPC6json> toWeightedValue()
	{
		return WeightedValue.of( this, zipCount() );
	}

	/** @return the parsed entries */
	@Deprecated
	public static Stream<CbsBoroughPC6json>
		readSync( final Callable<InputStream> json ) throws Exception
	{
		try( final InputStream is = json.call() )
		{
			return Arrays.stream(
					JsonUtil.valueOf( is, CbsBoroughPC6json[].class ) );
		}
	}

	/** @return the parsed entries */
	public static Observable<CbsBoroughPC6json>
		readAsync( final Callable<InputStream> json )
	{
		return JsonUtil.readArrayAsync( json, CbsBoroughPC6json.class );
	}
}