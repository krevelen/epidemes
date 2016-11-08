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
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Actor.ID;
import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;
import io.coala.name.Identified;
import io.coala.util.Compare;

/**
 * {@link VaxHesitant} provides an implementation of the Four C model by
 * <a href="http://dx.doi.org/10.1177/2372732215600716">Betsch et al., 2015</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface VaxHesitant extends Identified<Actor.ID>
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
	Number getComplacency();

	/** @return confidence {@link Number} (perceived utility of vaccination) */
	Number getConfidence();

	/**
	 * convenience level depends on the {@link VaxHesitant}'s judgment of a
	 * {@link VaxOccasion} regarding factors like "physical availability,
	 * affordability and willingness-to-pay, geographical accessibility, ability
	 * to understand (language and health literacy) and appeal of immunization
	 * service" (MacDonald et al., 2015)
	 * 
	 * @param occ the {@link VaxOccasion} to judge
	 * @return the perceived convenience {@link Number}
	 */
	default Number getConvenience( final VaxOccasion occ )
	{
		return Compare.min( occ.getUtility(), occ.getProximity(),
				occ.getClarity(), occ.getAffinity() );
	}

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
	Number getCalculation();

	void setCalculation( Number calculation );

	void observe( Actor.ID sourceRef, Number vaccineRisk, Number diseaseRisk );

	Number getAppreciation( Actor.ID sourceRef );

	/**
	 * @param sourceRef the source to check reputation for exclusion from
	 *            attitude computation
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
	default Number appreciationWeight( final Actor.ID sourceRef )
	{
		return Compare.max( BigDecimal.ZERO,
				DecimalUtil.valueOf( getAppreciation( sourceRef ) )
						.subtract( APPRECIATION_WEIGHT_MINIMUM )
						.add( DecimalUtil.valueOf( getCalculation() ) ) );
	}

	BigDecimal APPRECIATION_WEIGHT_MINIMUM = BigDecimal.valueOf( 5, 1 );

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
	default boolean isHesitant( final VaxOccasion occ )
	{
		return Compare.le( DecimalUtil.valueOf( getConvenience( occ ) ),
				DecimalUtil.binaryEntropy( getComplacency() )
						.multiply( DecimalUtil.binaryEntropy( getConfidence() ) ) );
	}

	/**
	 * {@link WeightedAverager} averages own and all observed and filtered
	 * {@link VaxPosition}s. In theory one could set the weight for their own
	 * {@link VaxPosition} to 0 by giving oneself a reputation below the
	 * (inverse) calculation threshold, effectively ignoring one's own position.
	 * However, if all relevant positions' weights sum to 0, then the defaul
	 * position carries all the weight.
	 */
	class WeightedAverager implements VaxHesitant
	{
		private final Actor.ID myRef;

		private final Function<Actor.ID, BigDecimal> reputer;

		/** (dynamic) argument "memory" of an individual */
		@JsonProperty
		private final Map<Actor.ID, BigDecimal[]> positions = new HashMap<>();

		@JsonProperty
		private final BigDecimal[] myDefault;

		private transient BigDecimal[] myPosition = null;

		private BigDecimal calculation;

		public WeightedAverager( final Actor.ID myRef,
			final Number myVaccineRisk, final Number myDiseaseRisk,
			final Number myCalculation )
		{
			this( myRef, myVaccineRisk, myDiseaseRisk, myCalculation,
					id -> BigDecimal.ONE );
		}

		public WeightedAverager( final Actor.ID myRef,
			final Number myVaccineRisk, final Number myDiseaseRisk,
			final Number myCalculation,
			final Function<Actor.ID, Number> reputer )
		{
			setCalculation( myCalculation );
			this.reputer = id -> DecimalUtil.valueOf( reputer.apply( id ) );
			this.myRef = myRef;
			this.myDefault = toPosition( myVaccineRisk, myDiseaseRisk );
			reset();
		}

		@Override
		public ID id()
		{
			return this.myRef;
		}

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
		public void observe( final Actor.ID ref, final Number vaccineRisk,
			final Number diseaseRisk )
		{
			if( ref.equals( this.myRef ) ) throw new IllegalArgumentException(
					"Another can't generate own position" );
			this.positions.put( ref, toPosition( vaccineRisk, diseaseRisk ) );
			reset();
		}

		@Override
		public BigDecimal getAppreciation( final Actor.ID sourceRef )
		{
			return this.reputer.apply( sourceRef );
		}

		enum Index
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

		private BigDecimal[] toPosition( final Number vaccineRisk,
			final Number diseaseRisk )
		{
			return new BigDecimal[] {
					BigDecimal.ONE
							.subtract( DecimalUtil.valueOf( vaccineRisk ) ),
					BigDecimal.ONE
							.subtract( DecimalUtil.valueOf( diseaseRisk ) ) };
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
			final BigDecimal w = weightedAddition( sums, id(), this.myDefault )
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
		public String toString()
		{
			return JsonUtil.stringify( this );
		}
	}
}