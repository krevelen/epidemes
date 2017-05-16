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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import org.apfloat.Apfloat;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.enterprise.Actor;
import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;
import io.coala.util.Compare;

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

	/**
	 * perceived barriers may cause an "intention-behavior gap" (Sheeran, 2002)
	 * when there is no clear preference, i.e. fence-sitting (Leask, 2011)
	 * 
	 * @param occ the {@link VaxOccasion}
	 * @return {@code true} iff convenience does not match or outweigh perceived
	 *         barrier (i.e. complacency vs. confidence attitudes)
	 *         <p>
	 *         <table style="width:400; border:1px solid black;
	 *         cell-padding:2px; cell-spacing:0px;">
	 *         <caption><em><b>default</b> convenience levels leading to
	 *         hesitancy behavior, i.e. where:</em>
	 *         <nobr><code>{@link #getConvenience( VaxOccasion ) convenience} <= {@link #getComplacency() complacency}
				- {@link #getConfidence()
	 *         confidence}</code></nobr></caption>
	 *         <tr>
	 *         <th rowSpan=2 vAlign=bottom>{@linkplain #getConfidence()
	 *         confidence} level</th>
	 *         <th colSpan=3>{@linkplain #getComplacency() complacency}
	 *         level</th>
	 *         </tr>
	 *         <tr>
	 *         <th>LO</th>
	 *         <th>MID</th>
	 *         <th>HI</th>
	 *         <td><em><nobr>&#8599; hesitate</nobr> (convenience
	 *         barrier)</em></td>
	 *         </tr>
	 *         <tr>
	 *         <th>LO</th>
	 *         <td align=center>LO</td>
	 *         <td align=center>LO, MID</td>
	 *         <td align=center>all</td>
	 *         </tr>
	 *         <tr>
	 *         <th>MID</th>
	 *         <td align=center>none</td>
	 *         <td align=center>LO</td>
	 *         <td align=center>LO, MID</td>
	 *         </tr>
	 *         <tr>
	 *         <th>HI</th>
	 *         <td align=center>none</td>
	 *         <td align=center>none</td>
	 *         <td align=center>LO</td>
	 *         </tr>
	 *         <tr>
	 *         <td align=right><em><nobr>vaccinate &#8601;</nobr></em></td>
	 *         </tr>
	 *         </table>
	 */
	@Override
	boolean isPositive( VaxOccasion occ );

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

	void observe( Actor.ID sourceRef, Number complacency, Number confidence );

	default void observeRisk( Actor.ID sourceRef, Number vaccineRisk,
		Number diseaseRisk )
	{
		observe( sourceRef,
				BigDecimal.ONE.subtract( DecimalUtil.valueOf( vaccineRisk ) ),
				BigDecimal.ONE.subtract( DecimalUtil.valueOf( diseaseRisk ) ) );
	}

	BigDecimal getAppreciation( Actor.ID sourceRef );

	/**
	 * @param sourceRef the source to appreciate in attitude computation, where
	 *            {@code null} indicates the owner
	 * @return a positive weight or zero iff
	 *         {@linkplain #getAppreciation(Actor.ID) Appreciation} does not
	 *         meet the current {@linkplain #getCalculation() Calculation} level
	 *         <p>
	 *         <table style="width:300; border:1px solid black;
	 *         cell-padding:2px; cell-spacing:0px;">
	 *         <caption><em><b>default</b> weight of another's
	 *         {@link VaxPosition} based on their
	 *         {@linkplain #getAppreciation(Actor.ID) Appreciation} and
	 *         someone's {@linkplain #getCalculation() Calculation}
	 *         level</em></caption>
	 *         <tr>
	 *         <th rowSpan=2 vAlign=bottom>source
	 *         {@linkplain #getAppreciation(Actor.ID) Appreciation} level</th>
	 *         <th colSpan=3>own {@linkplain #getCalculation() Calculation}
	 *         level</th>
	 *         </tr>
	 *         <tr>
	 *         <th>LO</th>
	 *         <th>MID</th>
	 *         <th>HI</th>
	 *         </tr>
	 *         <tr>
	 *         <th>LO</th>
	 *         <td align=center>0.0</td>
	 *         <td align=center>0.0</td>
	 *         <td align=center>0.5</td>
	 *         </tr>
	 *         <tr>
	 *         <th>MID</th>
	 *         <td align=center>0.0</td>
	 *         <td align=center>0.5</td>
	 *         <td align=center>1.0</td>
	 *         </tr>
	 *         <tr>
	 *         <th>HI</th>
	 *         <td align=center>0.5</td>
	 *         <td align=center>1.0</td>
	 *         <td align=center>1.5</td>
	 *         </tr>
	 *         </table>
	 */
	default BigDecimal appreciationWeight( final Actor.ID sourceRef )
	{
		return Compare.max( BigDecimal.ZERO, getAppreciation( sourceRef )
				.subtract( ONE_HALF ).add( getCalculation() ) );
	}

	BigDecimal ONE_HALF = BigDecimal.valueOf( 5, 1 );

	// TODO check
	default boolean isHesitant( final VaxOccasion occ )
	{
		final Apfloat comp = DecimalUtil.toApfloat( getComplacency() );
		final Apfloat conf = DecimalUtil.toApfloat( getConfidence() );
		return Compare.le( DecimalUtil.valueOf( getConvenience( occ ) ),
				comp.equals( Apfloat.ZERO ) || conf.equals( Apfloat.ZERO )
						? BigDecimal.ZERO
						: DecimalUtil.valueOf(
								DecimalUtil.binaryEntropy( comp ).multiply(
										DecimalUtil.binaryEntropy( conf ) ) ) );
	}

	enum MyDeterminant
	{
		CONFIDENCE, COMPLACENCY;
	}

	/**
	 * @param appreciations a (m x n) {@link Matrix} to store m attitudes having
	 *            up to n connection weights
	 * @param positions a (n x 2) {@link Matrix} to store 2 determinants
	 *            observed for up to n connections
	 * @param owner an {@link Actor.ID} reference
	 * @param indexConverter a {@link Function} to convert {@link Actor.ID} into
	 *            a {@link Long} row index, such that: 0 <= row < m
	 * @return the {@link VaxHesitancy} attitude
	 */
	static VaxHesitancy of( final Matrix positions, final Matrix appreciations,
		final Actor.ID owner, final Function<Actor.ID, Long> indexConverter )
	{
		return of( positions, appreciations, owner, indexConverter, Compare::ge,
				occ -> Compare.min( occ.utility(), occ.proximity(),
						occ.clarity(), occ.affinity() ),
				( conf, comp ) -> ONE_HALF.subtract(
						ONE_HALF.multiply( conf.subtract( comp ) ) ) );
	}

	/**
	 * @param appreciations a (m x n) {@link Matrix} to store m attitudes having
	 *            up to n connection weights
	 * @param positions a (n x 2) {@link Matrix} to store 2 determinants
	 *            observed for up to n connections
	 * @param owner an {@link Actor.ID} reference
	 * @param indexConverter a {@link Function} to convert {@link Actor.ID} into
	 *            a {@link Long} row index, such that: 0 <= row < m
	 * @param calculationFilter {@link BiPredicate} : (appreciation,
	 *            calculation) &rarr; include &isin; [T,F]
	 * @param convenienceEvaluator {@link Function} : {@link VaxOccasion} &rarr;
	 *            convenience, a {@link BigDecimal} &isin; [0,1]
	 * @param barrierEvaluator {@link BiFunction} : (confidence, complacency)
	 *            &rarr; barrier, each a {@link BigDecimal} &isin; [0,1]
	 * @return the {@link VaxHesitancy} attitude
	 */
	static VaxHesitancy of( final Matrix positions, final Matrix appreciations,
		final Actor.ID owner, final Function<Actor.ID, Long> indexConverter,
		final BiPredicate<BigDecimal, BigDecimal> calculationFilter,
		final Function<VaxOccasion, BigDecimal> convenienceEvaluator,
		final BiFunction<BigDecimal, BigDecimal, BigDecimal> barrierEvaluator )
	{
		// sanity checks
		if( appreciations.getSize( 1 ) != positions.getSize( 0 ) )
			Thrower.throwNew( IllegalArgumentException::new,
					() -> "Dimensions incompatible, (n x "
							+ appreciations.getSize( 1 ) + ") vs ("
							+ positions.getSize( 0 ) + " x m)" );
		if( positions.getSize( 1 ) < MyDeterminant.values().length )
			Thrower.throwNew( IllegalArgumentException::new,
					() -> "Determinant columns missing, use (n x "
							+ MyDeterminant.values().length + ")" );

		return new VaxHesitancy()
		{
			private final long row = indexFor( owner );
			private BigDecimal calculation = ONE_HALF;
			private transient Matrix positionCache = null;

			private long indexFor( final Actor.ID ref )
			{
				final long row = indexConverter.apply( ref );
				if( row >= appreciations.getSize( 0 ) )
					Thrower.throwNew( IndexOutOfBoundsException::new,
							() -> row + " >= " + appreciations.getSize( 0 ) );
				if( row >= appreciations.getSize( 1 ) )
					Thrower.throwNew( IndexOutOfBoundsException::new,
							() -> row + " >= " + appreciations.getSize( 1 ) );
				if( row >= positions.getSize( 0 ) )
					Thrower.throwNew( IndexOutOfBoundsException::new,
							() -> row + " >= " + positions.getSize( 0 ) );
				return row;
			}

			@Override
			public String toString()
			{
				final Object label = positions.getRowLabel( this.row );
				return label == null ? owner.toString() : label.toString();
			}

			@Override
			public void setCalculation( final Number calculation )
			{
				this.calculation = DecimalUtil.valueOf( calculation );
				this.positionCache = null;
			}

			@Override
			public BigDecimal getCalculation()
			{
				return this.calculation;
			}

			private Matrix determinants()
			{
				return this.positionCache != null ? this.positionCache
						: (this.positionCache = MatrixUtil.apply(
								appreciations.selectRows( Ret.NEW, this.row ),
								bd -> calculationFilter.test( bd,
										this.calculation ) ? bd
												: BigDecimal.ZERO )
								.mtimes( positions ));
			}

			@Override
			public BigDecimal getConfidence()
			{
				return determinants().getAsBigDecimal( 0,
						MyDeterminant.CONFIDENCE.ordinal() );
			}

			@Override
			public BigDecimal getComplacency()
			{
				return determinants().getAsBigDecimal( 0,
						MyDeterminant.COMPLACENCY.ordinal() );
			}

			@Override
			public void observe( final Actor.ID sourceRef,
				final Number complacency, final Number confidence )
			{
				final long row = indexConverter.apply( sourceRef );
				positions.setAsBigDecimal( DecimalUtil.valueOf( complacency ),
						row, MyDeterminant.COMPLACENCY.ordinal() );
				positions.setAsBigDecimal( DecimalUtil.valueOf( confidence ),
						row, MyDeterminant.CONFIDENCE.ordinal() );
				this.positionCache = null;
			}

			@Override
			public BigDecimal getAppreciation( final Actor.ID sourceRef )
			{
				return appreciations.getAsBigDecimal( this.row,
						indexFor( sourceRef ) );
			}

			@Override
			public BigDecimal getConvenience( final VaxOccasion occ )
			{
				return convenienceEvaluator.apply( occ );
			}

			@Override
			public boolean isPositive( final VaxOccasion occ )
			{
				return Compare.gt( getConvenience( occ ), barrierEvaluator
						.apply( getConfidence(), getComplacency() ) );
			}
		};
	}

	class MatrixUtil
	{
		public static Matrix apply( final Matrix m,
			final Function<BigDecimal, BigDecimal> func )
		{
			return apply( m, func, m::getAsBigDecimal, m::setAsBigDecimal );
		}

		public static <T> Matrix apply( final Matrix m,
			final Function<T, T> func, final Function<long[], T> getter,
			final BiConsumer<T, long[]> setter )
		{
			if( m.getDimensionCount() > 3 )
				return Thrower.throwNew( UnsupportedOperationException::new,
						() -> "Too many dimensions: " + m.getDimensionCount() );

			for( long row = m.getSize( 0 ) - 1; row >= 0; row-- )
				if( m.getDimensionCount() == 1 )
				{
					final long[] coords = { row };
					setter.accept( func.apply( getter.apply( coords ) ),
							coords );
				} else
					for( long col = m.getSize( 1 ) - 1; col >= 0; col-- )
						if( m.getDimensionCount() == 2 )
						{
							final long[] coords = { row, col };
							setter.accept( func.apply( getter.apply( coords ) ),
									coords );
						} else
							for( long lev = m.getSize( 2 )
									- 1; lev >= 0; lev-- )
								if( m.getDimensionCount() == 3 )
								{
									final long[] coords = { row, col, lev };
									setter.accept(
											func.apply(
													getter.apply( coords ) ),
											coords );
								}
			return m;
		}
	}

	/**
	 * {@link WeightedAverager} averages own default {@link VaxPosition} and all
	 * those observed latest per source {@link Actor.ID}, filtered by their
	 * current reputation. In theory one could set the weight for their own
	 * {@link VaxPosition} to 0 by giving oneself a reputation below the
	 * (inverse) calculation threshold, effectively ignoring one's own position.
	 * However, if all relevant positions' weights sum to 0, then the default
	 * position carries all the weight.
	 */
	class WeightedAverager implements VaxHesitancy
	{
		public static WeightedAverager of( final Number myConfidence,
			final Number myComplacency, final Number myCalculation )
		{
			return of( myConfidence, myComplacency, myCalculation,
					id -> BigDecimal.ONE );
		}

		public static WeightedAverager of( final Number myConfidence,
			final Number myComplacency, final Number myCalculation,
			final Function<Actor.ID, Number> appreciator )
		{
			return of( myConfidence, myComplacency, myCalculation, appreciator,
					occ -> Compare.min( occ.utility(), occ.proximity(),
							occ.clarity(), occ.affinity() ) );
		}

		public static WeightedAverager of( final Number myConfidence,
			final Number myComplacency, final Number myCalculation,
			final Function<Actor.ID, Number> appreciator,
			final Function<VaxOccasion, Number> evaluator )
		{
			return new WeightedAverager( myConfidence, myComplacency,
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

		public WeightedAverager( final Number myConfidence,
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

		@Override
		public void observe( final Actor.ID ref, final Number confidence,
			final Number complacency )
		{
			this.positions.put( ref, toPosition( confidence, complacency ) );
			reset();
		}

		@Override
		public BigDecimal getAppreciation( final Actor.ID sourceRef )
		{
			return this.appreciator.apply( sourceRef );
		}

		public enum Index
		{
			CONFIDENCE, COMPLACENCY;
		}

		@Override
		@JsonIgnore
		public BigDecimal getComplacency()
		{
			return myPosition()[Index.COMPLACENCY.ordinal()];
		}

		@Override
		@JsonIgnore
		public BigDecimal getConfidence()
		{
			return myPosition()[Index.CONFIDENCE.ordinal()];
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
			final BigDecimal weight = DecimalUtil
					.valueOf( appreciationWeight( id ) );
			if( !BigDecimal.ZERO.equals( weight ) )
				for( int i = 0; i < sums.length; i++ )
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