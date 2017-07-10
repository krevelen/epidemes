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
package nl.rivm.cib.pilot.json;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.math.Range;
import io.coala.math.Tuple;
import io.coala.math.WeightedValue;
import io.coala.name.Identified;
import io.coala.random.ProbabilityDistribution;
import io.coala.util.FileUtil;
import io.reactivex.Observable;

/**
 * {@link HesitancyProfileJson} contains hesitancy distribution parameters,
 * fitted to e.g. PIENTER2 data
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class HesitancyProfileJson extends Identified.SimpleOrdinal<String>
{
	/**
	 * {@link Category}
	 */
	public static class Category extends Tuple
	{
		public Category( final Boolean religious, final Boolean alternative )
		{
			super( Arrays.<Comparable<?>>asList( religious, alternative ) );
		}

		public Boolean religious()
		{
			return (Boolean) values().get( 0 );
		}

		public Boolean alternative()
		{
			return (Boolean) values().get( 1 );
		}

//		public VaccineStatus status()
//		{
//			return (VaccineStatus) values().get( 1 );
//		}
	}

	public static Observable<WeightedValue<HesitancyProfileJson>>
		parse( final String fileName )
	{
		return parse( () -> FileUtil.toInputStream( fileName ) );
	}

	public static Observable<WeightedValue<HesitancyProfileJson>> parse(
		final Callable<InputStream> input,
		Function<HesitancyProfileJson, Number> weighter )
	{
		return JsonUtil.readArrayAsync( input, HesitancyProfileJson.class )
				.map( profile -> WeightedValue.of( profile,
						weighter.apply( profile ) ) );
	}

	public static Observable<WeightedValue<HesitancyProfileJson>>
		parse( final Callable<InputStream> input )
	{
		return parse( input, profile -> profile.fraction );
	}

	/** the initial religious persuasion */
	public boolean religious;

	/** the initial alternative medicine persuasion */
	public boolean alternative;

	/** the initial vaccination status */
	public VaccineStatus status;

	/** the original profile N */
	public int count;

	/** the original profile density/fraction */
	public BigDecimal fraction;

	/** reference index to hesitancy-initial.json */
	@JsonProperty( "indices" )
	public EnumMap<HesitancyDimension, Integer> indices;

	/** the initial attitude distributions per hesitancy dimension */
	@JsonProperty( "distributions" )
	public EnumMap<HesitancyDimension, DistParams> distParams;

	@Override
	public String id()
	{
		return this.id == null ? (this.id = (this.religious ? "Reli" : "Sec")
				+ "|" + (this.alternative ? "Alto" : "Reg") + "|" + this.status)
				: this.id;
	}

	public Category toCategory()
	{
		return new Category( this.religious, this.alternative );
	}

	/**
	 * {@link HesitancyDimension} is a 3C/4C dimension of attitude
	 */
	public enum HesitancyDimension
	{
		complacency, confidence, attitude, calculation;
	}

	/**
	 * {@link VaccineStatus} is a possible vaccination status
	 */
	public enum VaccineStatus
	{
		all, some, none;
	}

	/**
	 * {@link DistType} is a type of (R-fitted) distribution
	 */
	public enum DistType
	{
		weibull;
	}

	/**
	 * {@link DistParam} is a (R-fitted) distribution parameter name
	 */
	public enum DistParam
	{
		shape, scale;
	}

	/**
	 * {@link DistParams} describes and instantiates a (R-fitted) distribution
	 * of attitude values
	 */
	@JsonIgnoreProperties( ignoreUnknown = true )
	public static class DistParams
	{
		/** the fitted distribution type */
		public HesitancyProfileJson.DistType type;

		/** the minimum observable value */
		public Double min;

		/** the maximum observable value */
		public Double max;

		/** the distribution parameter estimate */
		public List<BigDecimal> est;

		/** the distribution parameter error (standard deviation) */
		public List<BigDecimal> sd;

		private ProbabilityDistribution<Double> distCache;

		/**
		 * @param distFact a {@link ProbabilityDistribution.Factory}
		 * @return a (cached) {@link ProbabilityDistribution} of {@link Double}s
		 */
		public ProbabilityDistribution<Double>
			createDist( final ProbabilityDistribution.Factory distFact )
		{
			if( this.distCache == null )
			{
				// avoid compl==conf, e.g. min: .5 -> .505, max: .5 -> .497
				final Range<Double> range = Range.of( this.min * 1.01,
						Math.pow( this.max, 1.01 ) );
				switch( this.type )
				{
				default:
					// TODO map other distribution types
				case weibull:
					this.distCache = distFact
							.createWeibull(
									this.est.get( DistParam.shape.ordinal() ),
									this.est.get( DistParam.scale.ordinal() ) )
							.map( range::crop );
				}
			}
			return this.distCache;
		}
	}

	@Override
	public String toString()
	{
		return JsonUtil.stringify( this );
	};
}