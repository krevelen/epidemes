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
package nl.rivm.cib.episim.model.vaccine.attitude;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.enterprise.Actor;
import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.MatrixUtil;
import io.coala.util.Compare;
import nl.rivm.cib.episim.model.person.Attitude;

/**
 * {@link VaxHesitancy} is an extended {@link Attitude} towards vaccination
 * according to the <b>Four C</b> model
 * (<a href="http://dx.doi.org/10.1177/2372732215600716">Betsch <em>et al.</em>,
 * 2015</a>) which categorizes determinants into {@link #getComplacency},
 * {@link #getConfidence}, {@link #getConvenience}, and {@link #getCalculation}.
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
/**
 * {@link VaxHesitancy}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface VaxHesitancy extends Attitude<VaxOccasion>
{

	/**
	 * lack of perceived threat due to e.g. (pre-existing) conformance to
	 * protestantist (Luther), homeopathic (Hahneman), or anthroposophic
	 * (Steiner) beliefs, or "the idea that it is selfish-rational to omit
	 * vaccination as long as enough other individuals are vaccinated to keep
	 * the infection risk low" thus leading complacent people to "passively
	 * omit" (Betsch et al., 2015)
	 * 
	 * @return complacency {@link Number} (perceived inverse risk of disease)
	 */
	BigDecimal getComplacency();

	/** @return confidence {@link Number} (perceived utility of vaccination) */
	BigDecimal getConfidence();

	default boolean isPositive()
	{
		return Compare.gt( getConfidence(), getComplacency() );
	}

	/**
	 * convenience level depends on the {@link VaxHesitancy}'s judgment of a
	 * {@link VaxOccasion} regarding factors like "physical availability,
	 * affordability and willingness-to-pay, geographical accessibility, ability
	 * to understand (language and health literacy) and appeal of immunization
	 * service" (MacDonald et al., 2015)
	 * 
	 * @param occ the {@link VaxOccasion} to judge
	 * @return the perceived convenience {@link Number}
	 */
	BigDecimal getConvenience( VaxOccasion occ );

	/**
	 * HIGH: no "strong pre-existing attitude, extensive search for pros and
	 * cons of vaccination." Furthermore, "any additional information about
	 * costs or (social) benefits will influence the decision because it is
	 * included in and updates the utility calculation" (Betsch et al., 2015).
	 * Vice versa, LOW: strong pre-existing attitude, low active information
	 * search (Fischer et al., 2011)
	 * 
	 * @return calculation {@link Number} of information/research diligence
	 *         (pre)disposition/threshold
	 */
	BigDecimal getCalculation();

	void setCalculation( Number calculation );

//	BigDecimal getAppreciation( Actor.ID sourceRef );

	/**
	 * perceived barriers may cause an "intention-behavior gap" (Sheeran, 2002)
	 * when there is no clear preference, i.e. fence-sitting (Leask, 2011)
	 * 
	 * @param occ the {@link VaxOccasion}
	 * @return {@code true} iff convenience does not match or outweigh perceived
	 *         barrier (i.e. complacency vs. confidence attitudes)
	 * 
	 */
	@Override
	boolean isPositive( VaxOccasion occ );

	// example
//	static BigDecimal entropyBarrier( final Number confidence,
//		final Number complacency )
//	{
//		final Apfloat comp = DecimalUtil.toApfloat( complacency );
//		final Apfloat conf = DecimalUtil.toApfloat( confidence );
//		return comp.signum() == 0 || conf.signum() == 0 ? BigDecimal.ZERO
//				: DecimalUtil.valueOf( DecimalUtil.binaryEntropy( comp )
//						.multiply( DecimalUtil.binaryEntropy( conf ) ) );
//	}

	/**
	 * {@link BiFunction} : (appreciation, calculation) &rarr; include, each a
	 * {@link BigDecimal} &isin; [0,1]
	 * 
	 * @param appreciation
	 * @param calculationLevel the {@linkplain #getCalculation()} level
	 * @return if( appreciation &ge; 1 &minus; calculationLevel) &rarr;
	 *         appreciation, else &rarr; 0
	 */
	static BigDecimal thresholdAppreciation( final BigDecimal appreciation,
		final BigDecimal calculationLevel )
	{
		return Compare.lt( appreciation, opposite( calculationLevel ) )
				? BigDecimal.ZERO : appreciation;
	}

	/**
	 * Simple calculation-filter for appreciation weights
	 * 
	 * @param appreciation some appreciation weight
	 * @param calculationLevel some calculation level
	 * @return max( 0, appreciation &minus; &frac12; &plus; calculationLevel )
	 * 
	 * @see #getCalculation()
	 * @see
	 *      <table style="width:300; border:1px solid black; cell-padding:2px;
	 *      cell-spacing:0px;">
	 *      <caption><em>calculating appreciation</em></caption>
	 *      <tr>
	 *      <th rowSpan=2 vAlign=bottom>appreciation</th>
	 *      <th colSpan=3>own {@linkplain #getCalculation() calculation}
	 *      level</th>
	 *      </tr>
	 *      <tr>
	 *      <th>0</th>
	 *      <th>&frac12;</th>
	 *      <th>1</th>
	 *      </tr>
	 *      <tr>
	 *      <th>0</th>
	 *      <td align=center>0</td>
	 *      <td align=center>0</td>
	 *      <td align=center>&frac12;</td>
	 *      </tr>
	 *      <tr>
	 *      <th>&frac12;</th>
	 *      <td align=center>0</td>
	 *      <td align=center>&frac12;</td>
	 *      <td align=center>1</td>
	 *      </tr>
	 *      <tr>
	 *      <th>1</th>
	 *      <td align=center>&frac12;</td>
	 *      <td align=center>1</td>
	 *      <td align=center><sup>3</sup>/<sub>2</sub></td>
	 *      </tr>
	 *      </table>
	 */
	static BigDecimal shiftedAppreciation( final BigDecimal appreciation,
		final BigDecimal calculationLevel )
	{
		return Compare.max( BigDecimal.ZERO, appreciation
				.subtract( DecimalUtil.ONE_HALF ).add( calculationLevel ) );
	}

	/**
	 * (confidence, complacency) &rarr; barrier, each a {@link BigDecimal}
	 * &isin; [0,1]
	 * 
	 * @param confidence &isin; [0,1]
	 * @param complacency &isin; [0,1]
	 * @return barrier : &frac12; &minus; &frac12; &times; (confidence &minus;
	 *         complacency)
	 */
	static BigDecimal averageBarrier( final BigDecimal confidence,
		final BigDecimal complacency )
	{
		return DecimalUtil.ONE_HALF.subtract( DecimalUtil.ONE_HALF
				.multiply( confidence.subtract( complacency ) ) );
	}

	/**
	 * ({@link #getConfidence() confidence}, {@link #getComplacency()
	 * complacency}) &rarr; barrier, each a {@link BigDecimal} &isin; [0,1]
	 * 
	 * @param confidence &isin; [0,1]
	 * @param complacency &isin; [0,1]
	 * @return complacency &minus; confidence
	 * @see #getConvenience(VaxOccasion)
	 * @see
	 *      <p>
	 *      <table style="width:400; border:1px solid black; cell-padding:2px;
	 *      cell-spacing:0px;">
	 *      <caption><em>convenience levels leading to hesitancy behavior (conv
	 *      &and; conf - compl)</caption>
	 *      <tr>
	 *      <th rowSpan=2 vAlign=bottom>{@linkplain #getConfidence() confidence}
	 *      level</th>
	 *      <th colSpan=3>{@linkplain #getComplacency() complacency} level</th>
	 *      </tr>
	 *      <tr>
	 *      <th>0</th>
	 *      <th>&frac12;</th>
	 *      <th>1</th>
	 *      <td><em><nobr>&#8599; hesitate</nobr> (convenience
	 *      barrier)</em></td>
	 *      </tr>
	 *      <tr>
	 *      <th>0</th>
	 *      <td align=center>0</td>
	 *      <td align=center>&le; &frac12;</td>
	 *      <td align=center>&#8868;</td>
	 *      </tr>
	 *      <tr>
	 *      <th>&frac12;</th>
	 *      <td align=center>&perp;</td>
	 *      <td align=center>0</td>
	 *      <td align=center>&le; &frac12;</td>
	 *      </tr>
	 *      <tr>
	 *      <th>1</th>
	 *      <td align=center>&perp;</td>
	 *      <td align=center>&perp;</td>
	 *      <td align=center>0</td>
	 *      </tr>
	 *      <tr>
	 *      <td align=right><em><nobr>vaccinate &#8601;</nobr></em></td>
	 *      </tr>
	 *      </table>
	 */
	static BigDecimal differenceBarrier( final BigDecimal confidence,
		final BigDecimal complacency )
	{
		return complacency.subtract( confidence );
	}

	/**
	 * {@link VaxOccasion} &rarr; convenience, a {@link BigDecimal} &isin; [0,1]
	 * 
	 * @param occ a {@link VaxOccasion}
	 * @return min({@link VaxOccasion#utility() utility},
	 *         {@link VaxOccasion#proximity() proximity},
	 *         {@link VaxOccasion#clarity() clarity},
	 *         {@link VaxOccasion#affinity() affinity})
	 */
	static BigDecimal minimumConvenience( final VaxOccasion occ )
	{
		return Compare.min( occ.utility(), occ.proximity(), occ.clarity(),
				occ.affinity() );
	}

	/**
	 * {@link SocialFactors} of vaccination hesitancy are influenced socially
	 */
	enum SocialFactors
	{
		CONFIDENCE, COMPLACENCY;
		// calculation and convenience are not influenced socially
	}

	/**
	 * @param x
	 * @return 1-x
	 */
	static BigDecimal opposite( final Number x )
	{
		return BigDecimal.ONE.subtract( DecimalUtil.valueOf( x ) );
	}

	/**
	 * {@link MatrixWeightedAverager}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class MatrixWeightedAverager implements VaxHesitancy
	{
		/**
		 * @param appreciations a (m x n) {@link Matrix} to store m attitudes
		 *            having up to n connection weights
		 * @param positions a (n x 2) {@link Matrix} to store 2 determinants
		 *            observed for up to n connections
		 * @param indexConverter a {@link Function} to convert {@link Actor.ID}
		 *            into a {@link Long} row index, such that: 0 <= row < m
		 * @param owner an {@link Actor.ID} reference
		 * @return the {@link VaxHesitancy} attitude
		 */
		public static MatrixWeightedAverager of( final Matrix positions,
			final Matrix appreciations,
			final Function<Actor.ID, Long> indexConverter,
			final Actor.ID owner )
		{
			return new MatrixWeightedAverager( positions, appreciations,
					indexConverter, owner, DecimalUtil.ONE_HALF );
		}

		private final Matrix positions;
		private final Matrix appreciations;
		private final Actor.ID owner;
		private final long row;
		private final Function<Actor.ID, Long> indexConverter;
		private BigDecimal calculation;
		private BiFunction<BigDecimal, BigDecimal, BigDecimal> appreciationFilter = VaxHesitancy::thresholdAppreciation;
		private Function<VaxOccasion, BigDecimal> convenienceEvaluator = VaxHesitancy::minimumConvenience;
		private BiFunction<BigDecimal, BigDecimal, BigDecimal> barrierEvaluator = VaxHesitancy::averageBarrier;
		private transient boolean positionCurrent = false;

		public MatrixWeightedAverager( final Matrix positions,
			final Matrix appreciations,
			final Function<Actor.ID, Long> indexConverter, final Actor.ID owner,
			final BigDecimal initialCalculation )
		{
			// sanity checks
			if( appreciations.getSize( 1 ) != positions.getSize( 0 ) )
				Thrower.throwNew( IllegalArgumentException::new,
						() -> "Dimensions incompatible, (n x "
								+ appreciations.getSize( 1 ) + ") vs ("
								+ positions.getSize( 0 ) + " x m)" );
			if( positions.getSize( 1 ) < SocialFactors.values().length )
				Thrower.throwNew( IllegalArgumentException::new,
						() -> "Determinant columns missing, use (n x "
								+ SocialFactors.values().length + ")" );

			this.positions = positions;
			this.appreciations = appreciations;
			this.owner = owner;
			this.indexConverter = indexConverter;
			this.row = indexFor( owner );
			setCalculation( initialCalculation );
		}

		public MatrixWeightedAverager reset()
		{
			this.positionCurrent = false;
			return this;
		}

		public MatrixWeightedAverager
			withCalculation( final Number calculation )
		{
			setCalculation( calculation );
			return this;
		}

		/**
		 * @param calculationFilter {@link BiFunction} : (appreciation,
		 *            calculation) &rarr; include &isin; [0,1]
		 * @return this {@link MatrixWeightedAverager}
		 */
		public MatrixWeightedAverager withAppreciationFilter(
			final BiFunction<BigDecimal, BigDecimal, BigDecimal> calculationFilter )
		{
			this.appreciationFilter = calculationFilter;
			return this;
		}

		/**
		 * @param convenienceEvaluator {@link Function} : {@link VaxOccasion}
		 *            &rarr; convenience, a {@link BigDecimal} &isin; [0,1]
		 * @return this {@link MatrixWeightedAverager}
		 */
		public MatrixWeightedAverager withConvenienceEvaluator(
			final Function<VaxOccasion, BigDecimal> convenienceEvaluator )
		{
			this.convenienceEvaluator = convenienceEvaluator;
			return this;
		}

		/**
		 * @param barrierEvaluator {@link BiFunction} : (confidence,
		 *            complacency) &rarr; barrier, each a {@link BigDecimal}
		 *            &isin; [0,1]
		 * @return this {@link MatrixWeightedAverager}
		 */
		public MatrixWeightedAverager withBarrierEvaluator(
			final BiFunction<BigDecimal, BigDecimal, BigDecimal> barrierEvaluator )
		{
			this.barrierEvaluator = barrierEvaluator;
			return this;
		}

		@Override
		public String toString()
		{
			final Object label = positions.getRowLabel( this.row );
			return label == null ? owner().toString() : label.toString();
		}

		@Override
		public void setCalculation( final Number calculation )
		{
			this.calculation = DecimalUtil.valueOf( calculation );
			reset();
		}

		@Override
		public BigDecimal getCalculation()
		{
			return this.calculation;
		}

		public long indexFor( final Actor.ID ref )
		{
			return this.indexConverter.apply( ref );
		}

		public BigDecimal calculationFilter( final BigDecimal appreciation )
		{
			return this.appreciationFilter.apply( appreciation,
					this.calculation );
		}

		@Override
		public BigDecimal getConvenience( final VaxOccasion occ )
		{
			return this.convenienceEvaluator.apply( occ );
		}

		public Matrix determinants()
		{
			if( !this.positionCurrent )
			{
				final Matrix myCalculatingWeights = MatrixUtil
						.computeBigDecimal( this.appreciations.selectRows(
								Ret.NEW, this.row ), this::calculationFilter );
				final double sum = myCalculatingWeights.getValueSum();
				if( sum > 0 )
				{
					// communicate all positions and weigh/contemplate!
					final Matrix newPosition = myCalculatingWeights
							.mtimes( this.positions ).divide( sum );
					// update own position
					MatrixUtil.insertBigDecimal( this.positions, newPosition,
							this.row, 0 );
					this.positionCurrent = true;
					return newPosition;
				} else // i.e. sum <= 0 : keep current position
					this.positionCurrent = true;
			}
			return this.positions.selectRows( Ret.LINK, this.row );
		}

		@Override
		public BigDecimal getConfidence()
		{
			return determinants().getAsBigDecimal( 0,
					SocialFactors.CONFIDENCE.ordinal() );
		}

		@Override
		public BigDecimal getComplacency()
		{
			return determinants().getAsBigDecimal( 0,
					SocialFactors.COMPLACENCY.ordinal() );
		}

		public void setPosition( final Actor.ID sourceRef,
			final Number confidence, final Number complacency )
		{
			final long row = this.indexConverter.apply( sourceRef );
			this.positions.setAsBigDecimal( DecimalUtil.valueOf( confidence ),
					row, SocialFactors.CONFIDENCE.ordinal() );
			this.positions.setAsBigDecimal( DecimalUtil.valueOf( complacency ),
					row, SocialFactors.COMPLACENCY.ordinal() );
			reset();
		}

		public void setOpposition( final Actor.ID sourceRef,
			final Number vaccineRisk, final Number diseaseRisk )
		{
			setPosition( sourceRef, opposite( vaccineRisk ),
					opposite( diseaseRisk ) );
		}

//		@Override
		public BigDecimal getAppreciation( final Actor.ID sourceRef )
		{
			return this.appreciations.getAsBigDecimal( this.row,
					indexFor( sourceRef ) );
		}

		@Override
		public boolean isPositive( final VaxOccasion occ )
		{
			return Compare.ge( getConvenience( occ ), this.barrierEvaluator
					.apply( getConfidence(), getComplacency() ) );
		}

		/**
		 * @return
		 */
		public Object owner()
		{
			return this.owner;
		}

		/**
		 * @param authority
		 * @param zeros
		 */
		public void setAppreciation( final Actor.ID owner,
			final Matrix weights )
		{
			MatrixUtil.insertBigDecimal( this.appreciations, weights,
					this.indexConverter.apply( owner ), 0 );
		}
	}

	/**
	 * {@link SimpleWeightedAverager} averages own default {@link VaxPosition}
	 * and all those observed latest per source {@link Actor.ID}, filtered by
	 * their current reputation. In theory one could set the weight for their
	 * own {@link VaxPosition} to 0 by giving oneself a reputation below the
	 * (inverse) calculation threshold, effectively ignoring one's own position.
	 * However, if all relevant positions' weights sum to 0, then the default
	 * position carries all the weight.
	 */
	class SimpleWeightedAverager implements VaxHesitancy
	{
		public static SimpleWeightedAverager of( final Number myConfidence,
			final Number myComplacency, final Number myCalculation )
		{
			return of( myConfidence, myComplacency, myCalculation,
					id -> BigDecimal.ONE );
		}

		public static SimpleWeightedAverager of( final Number myConfidence,
			final Number myComplacency, final Number myCalculation,
			final Function<Actor.ID, Number> appreciator )
		{
			return of( myConfidence, myComplacency, myCalculation, appreciator,
					occ -> Compare.min( occ.utility(), occ.proximity(),
							occ.clarity(), occ.affinity() ) );
		}

		public static SimpleWeightedAverager of( final Number myConfidence,
			final Number myComplacency, final Number myCalculation,
			final Function<Actor.ID, Number> appreciator,
			final Function<VaxOccasion, Number> evaluator )
		{
			return new SimpleWeightedAverager( myConfidence, myComplacency,
					myCalculation, appreciator, evaluator );
		}

		private final Function<Actor.ID, BigDecimal> appreciator;

		private final Function<VaxOccasion, BigDecimal> evaluator;

		/** (dynamic) argument "memory" of an individual */
		@JsonProperty
		private final Map<Actor.ID, BigDecimal[]> positions = new HashMap<>();

		@JsonProperty
		private final BigDecimal[] myDefault;

		private transient BigDecimal[] myPosition = null;

		private BigDecimal calculation;

		public SimpleWeightedAverager( final Number myConfidence,
			final Number myComplacency, final Number myCalculation,
			final Function<Actor.ID, Number> appreciator,
			final Function<VaxOccasion, Number> evaluator )
		{
			setCalculation( myCalculation );
			this.appreciator = id -> DecimalUtil
					.valueOf( appreciator.apply( id ) );
			this.evaluator = occ -> DecimalUtil
					.valueOf( evaluator.apply( occ ) );
			this.myDefault = toPosition( myConfidence, myComplacency );
			reset();
		}

		@Override
		public String toString()
		{
			return JsonUtil.stringify( this );
		}

		@Override
		public void setCalculation( final Number calculation )
		{
			if( calculation == this.calculation ) return;
			this.calculation = DecimalUtil.valueOf( calculation );
			reset();
		}

		@Override
		public BigDecimal getCalculation()
		{
			return this.calculation;
		}

		public void observe( final Actor.ID ref, final Number confidence,
			final Number complacency )
		{
			this.positions.put( ref, toPosition( confidence, complacency ) );
			reset();
		}

		public void observeRisk( final Actor.ID sourceRef,
			final Number vaccineRisk, final Number diseaseRisk )
		{
			observe( sourceRef,
					BigDecimal.ONE
							.subtract( DecimalUtil.valueOf( vaccineRisk ) ),
					BigDecimal.ONE
							.subtract( DecimalUtil.valueOf( diseaseRisk ) ) );
		}

//		@Override
//		public BigDecimal getAppreciation( final Actor.ID sourceRef )
//		{
//			return this.appreciator.apply( sourceRef );
//		}

		@Override
		@JsonIgnore
		public BigDecimal getComplacency()
		{
			return myPosition()[SocialFactors.COMPLACENCY.ordinal()];
		}

		@Override
		@JsonIgnore
		public BigDecimal getConfidence()
		{
			return myPosition()[SocialFactors.CONFIDENCE.ordinal()];
		}

		private BigDecimal[] toPosition( final Number confidence,
			final Number complacency )
		{
			return new BigDecimal[] { DecimalUtil.valueOf( confidence ),
					DecimalUtil.valueOf( complacency ) };
		}

		private void reset()
		{
			this.myPosition = null;
		}

		/**
		 * @param sums
		 * @param id
		 * @param augend
		 * @return the weight to allow map/reduce
		 */
		private BigDecimal weightedAddition( final BigDecimal[] sums,
			final Actor.ID id, final BigDecimal[] augend )
		{
			final BigDecimal weight = VaxHesitancy.shiftedAppreciation(
					this.appreciator.apply( id ), getCalculation() );
			if( weight.signum() != 0 ) for( int i = 0; i < sums.length; i++ )
				sums[i] = sums[i].add( augend[i].multiply( weight ) );
			return weight;
		}

		/**
		 * if necessary, compute attitude from default + filtered (dynamic)
		 * "memory"
		 * 
		 * @return the current position
		 */
		private BigDecimal[] myPosition()
		{
			if( this.myPosition != null ) return this.myPosition;

			// start from default position
			final int len = this.myDefault.length;

			// initialize at default position, applying weight accordingly
			final BigDecimal[] sums = new BigDecimal[] { BigDecimal.ZERO,
					BigDecimal.ZERO };
			final BigDecimal w = weightedAddition( sums, null, this.myDefault )
					.add( this.positions.entrySet().parallelStream()
							.map( entry -> weightedAddition( sums,
									entry.getKey(), entry.getValue() ) )
							.reduce( BigDecimal::add )
							.orElse( BigDecimal.ZERO ) );

			this.myPosition = new BigDecimal[len];
			if( BigDecimal.ZERO.equals( w ) ) // zero weights: assume default
				System.arraycopy( this.myDefault, 0, this.myPosition, 0, len );
			else // final division for the weighted average
				for( int i = 0; i < sums.length; i++ )
					this.myPosition[i] = DecimalUtil.divide( sums[i], w );
			return this.myPosition;
		}

		@Override
		public BigDecimal getConvenience( VaxOccasion occ )
		{
			return this.evaluator.apply( occ );
		}

		@Override
		public boolean isPositive( final VaxOccasion occ )
		{
			return Compare.gt( getConfidence().subtract( getComplacency() ),
					getConvenience( occ ) );
		}
	}
}