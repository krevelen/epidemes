/* $Id: c39e899100411b91e2eba1647db63ae8268385d4 $
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
package nl.rivm.cib.pilot.json;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.coala.exception.Thrower;
import io.coala.math.DecimalUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.QuantityDistribution;
import io.coala.time.TimeUnits;

/**
 * {@link RelationFrequencyJson} parses data originally from <a
 * href=http://statline.cbs.nl/Statweb/publication/?PA=82249NED>CBS 82249NED</a>
 * 
 * @version $Id: c39e899100411b91e2eba1647db63ae8268385d4 $
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class RelationFrequencyJson
{

	public enum Gender
	{
		male, female;
	}

	public enum Relation
	{
		family, friends, neighbors;
	}

	public enum DistType
	{
		LOG_NORMAL( "lnorm" ), //
		;

		private final String jsonValue;

		private DistType( final String jsonValue )
		{
			this.jsonValue = jsonValue;
		}

		@JsonValue
		public String jsonValue()
		{
			return this.jsonValue;
		}
	}

	public enum ParamName
	{
		MEAN_LOG( "meanlog" ), //
		STDEV_LOG( "sdlog" ), //
		;
		private final String jsonValue;

		private ParamName( final String jsonValue )
		{
			this.jsonValue = jsonValue;
		}

		@JsonValue
		public String jsonValue()
		{
			return this.jsonValue;
		}
	}

	@JsonProperty( "gender" )
	public Gender gender;

	@JsonProperty( "relation" )
	public Relation relation;

	@JsonProperty( "age" )
	public String ageRange;

	@JsonProperty( "daily" )
	public BigDecimal daily;

	@JsonProperty( "weekly" )
	public BigDecimal weekly;

	@JsonProperty( "monthly" )
	public BigDecimal monthly;

	@JsonProperty( "less" )
	public BigDecimal annually;

	@JsonProperty( "never" )
	public BigDecimal rarely;

	@JsonProperty( "dist" )
	public DistParams dist;

	@Override
	public String toString()
	{
		return this.gender + "|" + this.ageRange + "|" + this.relation;
	}

	@JsonIgnore
	private Range<BigDecimal> ageRangeCache = null;

	public Range<BigDecimal> ageRange()
	{
		if( this.ageRangeCache == null )
		{
			final String[] ages = this.ageRange.replaceAll( ">", "" )
					.split( "-" );
			this.ageRangeCache = (ages.length == 1 ? Range.of( ages[0] )
					: Range.of( ages[0], ages[1] )).map( BigDecimal::new );
		}
		return this.ageRangeCache;
	}

	@JsonIgnore
	private QuantityDistribution<Time> intervalDistCache = null;

	public QuantityDistribution<Time>
		intervalDist( final ProbabilityDistribution.Factory distFact )
	{
		return this.intervalDistCache != null ? this.intervalDistCache
				: (this.intervalDistCache = this.dist.create( distFact ));
	}

	/**
	 * @return
	 */
	public Category toCategory()
	{
		return new Category( this.gender, this.relation,
				ageRange().lowerValue() );
	}

	public static class Category extends Tuple
	{

		public Category( final boolean male, final Relation relation,
			final Quantity<Time> age )
		{
			this( male ? Gender.male : Gender.female, relation, DecimalUtil
					.valueOf( age.to( TimeUnits.ANNUM ).getValue() ) );
		}

		public Category( final Gender gender, final Relation relation,
			final BigDecimal ageRange )
		{
			super( Arrays.asList( gender, relation, ageRange ) );
		}

		public Gender gender()
		{
			return (Gender) values().get( 0 );
		}

		public Relation relation()
		{
			return (Relation) values().get( 1 );
		}

//		@SuppressWarnings( "unchecked" )
		public BigDecimal floorAge()
		{
			return (BigDecimal) values().get( 2 );
		}
	}

	public static class DistParams
	{
		@JsonProperty( "type" )
		public DistType type;

		@JsonProperty( "params" )
		public EnumMap<ParamName, BigDecimal> params;

		@JsonProperty( "sd" )
		public EnumMap<ParamName, BigDecimal> error;

		public QuantityDistribution<Time>
			create( final ProbabilityDistribution.Factory distFact )
		{
			switch( this.type )
			{
			case LOG_NORMAL:
				return distFact
						.createLogNormal( this.params.get( ParamName.MEAN_LOG ),
								this.params.get( ParamName.STDEV_LOG ) )
						.toQuantities( TimeUnits.DAYS );
			default:
				return Thrower.throwNew( IllegalArgumentException::new,
						() -> "Unknown distribution: " + this.type );
			}
		}
	}
}
