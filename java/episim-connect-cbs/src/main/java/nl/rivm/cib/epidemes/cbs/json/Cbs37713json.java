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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Time;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.locate.Region;

/**
 * {@link Cbs37713json} helps to import CBS table 37713 data (JSON formatted). See
 * http://statline.cbs.nl/Statweb/selection/?PA=37713 and source data at
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
@JsonIgnoreProperties( ignoreUnknown = true )
public class Cbs37713json
{
	/** {@code Leeftijd}, range as int[] array */
	@JsonProperty( "age" )
	public List<Integer> ages;

	/** {@code Herkomstgroepering}, transformed */
	@JsonProperty( "ori" )
	public String origin;

	/** {@code RegioS} */
	@JsonProperty( "reg" )
	public String region;

	/** {@code Totale bevolking; Mannen} */
	@JsonProperty( "mal" )
	public int males = 0; // missing if 0

	/** {@code Totale bevolking; Vrouwen} */
	@JsonProperty( "fem" )
	public int females = 0; // missing if 0

	private static final String DUTCH = "nl";

	private static Map<Integer, QuantityDistribution<Time>> ageDists = new TreeMap<>();

	private static Map<String, Region.ID> regionIds = new TreeMap<>();

	public Cbs37713json.Tuple
		toTuple( final ProbabilityDistribution.Factory distFact )
	{
		final Cbs37713json.Tuple res = new Tuple();
		res.ageDist = ageDists.computeIfAbsent( this.ages.get( 0 ),
				key -> distFact
						.createUniformContinuous( key, this.ages.get( 1 ) )
						.toQuantities( TimeUnits.ANNUM ) );
		res.alien = !DUTCH.equalsIgnoreCase( this.origin );
		res.regionId = regionIds.computeIfAbsent( this.region,
				key -> Region.ID.of( this.region.trim() ) );
		res.regionType = CBSRegionType.parse( this.region );
		return res;
	}

	public static class Tuple
	{
		@JsonIgnore
		public QuantityDistribution<Time> ageDist;
		public boolean alien;
		public Region.ID regionId;
		public CBSRegionType regionType;

		@Override
		public String toString()
		{
			return JsonUtil.stringify( this );
		}
	}
}