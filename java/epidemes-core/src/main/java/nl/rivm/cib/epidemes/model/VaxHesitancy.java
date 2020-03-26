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
package nl.rivm.cib.epidemes.model;

import java.math.BigDecimal;
import java.util.function.BiFunction;

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
}