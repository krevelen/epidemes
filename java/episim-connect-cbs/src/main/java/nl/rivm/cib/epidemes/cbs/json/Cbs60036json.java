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

import javax.measure.quantity.Time;

import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.person.Gender;
import tec.uom.se.ComparableQuantity;

/**
 * {@link Cbs60036json} helps to import CBS table 60036ned data (JSON
 * formatted). See http://statline.cbs.nl/Statweb/selection/?PA=60036ned and
 * source data at
 * http://opendata.cbs.nl/ODataFeed/odata/60036ned/UntypedDataSet?$format=json
 * (find a tutorial on OpenData operators etc. at
 * http://www.odata.org/getting-started/basic-tutorial/)
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Cbs60036json
{

	public static enum CBSPartnerComposition implements CBSJsonProperty
	{
		TOTAL_MIXED( "total_m_f", true, true, false, null, null ),
		/** */
		M_EQ_F( "age_m_eq_f", false, true, false, -1, 1 ),
		/** */
		M_GT_F_1_5( "age_m_gt_f_1_5", false, true, false, 1, 5 ),
		/** */
		M_GT_F_5_10( "age_m_gt_f_5_10", false, true, false, 5, 10 ),
		/** */
		M_GT_F_10_15( "age_m_gt_f_10_15", false, true, false, 10, 15 ),
		/** */
		M_GT_F_15_20( "age_m_gt_f_15_20", false, true, false, 15, 20 ),
		/** */
		M_GT_F_20PLUS( "age_m_gt_f_20plus", false, true, false, 20, null ),
		/** */
		M_LT_F_1_5( "age_m_lt_f_1_5", false, false, true, 1, 5 ),
		/** */
		M_LT_F_5_10( "age_m_lt_f_5_10", false, false, true, 5, 10 ),
		/** */
		M_LT_F_10_15( "age_m_lt_f_10_15", false, false, true, 10, 15 ),
		/** */
		M_LT_F_15_20( "age_m_lt_f_15_20", false, false, true, 15, 20 ),
		/** */
		M_LT_F_20PLUS( "age_m_lt_f_20plus", false, false, true, 20, null ),
		/** */
		TOTAL_MALE( "total_m_m", true, true, true, null, null ),
		/** */
		M_EQ_M( "age_m_eq_m", false, true, true, -1, 1 ),
		/** */
		M_GT_M_1_5( "age_m_gt_m_1_5", false, true, true, 1, 5 ),
		/** */
		M_GT_M_5_10( "age_m_gt_m_5_10", false, true, true, 5, 10 ),
		/** */
		M_GT_M_10_15( "age_m_gt_m_10_15", false, true, true, 10, 15 ),
		/** */
		M_GT_M_15_20( "age_m_gt_m_15_20", false, true, true, 15, 20 ),
		/** */
		M_GT_M_20PLUS( "age_m_gt_m_20plus", false, true, true, 20, null ),
		/** */
		TOTAL_FEMALE( "total_m_m", true, true, true, null, null ),
		/** */
		F_EQ_F( "age_f_eq_f", false, false, false, -1, 1 ),
		/** */
		F_GT_F_1_5( "age_f_gt_f_1_5", false, false, false, 1, 5 ),
		/** */
		F_GT_F_5_10( "age_f_gt_f_5_10", false, false, false, 5, 10 ),
		/** */
		F_GT_F_10_15( "age_f_gt_f_10_15", false, false, false, 10, 15 ),
		/** */
		F_GT_F_15_20( "age_f_gt_f_15_20", false, false, false, 15, 20 ),
		/** */
		F_GT_F_20PLUS( "age_f_gt_f_20plus", false, false, false, 20, null );

		private final String jsonKey;

		private final Boolean aggregate;

		private final Gender referentGender;

		private final Gender partnerGender;

		private final Range<ComparableQuantity<Time>> ageDiff;

		private CBSPartnerComposition( final String jsonKey,
			final boolean aggregate, final Boolean maleFirst,
			final boolean maleSecond, final Integer ageDiffMin,
			final Integer ageDiffMax )
		{
			this.jsonKey = jsonKey;
			this.aggregate = aggregate;
			this.referentGender = maleFirst ? CBSGender.MALE : CBSGender.FEMALE;
			this.partnerGender = maleSecond ? CBSGender.MALE : CBSGender.FEMALE;
			this.ageDiff = Range.of( ageDiffMin, ageDiffMax )
					.map( yr -> QuantityUtil.valueOf( yr, TimeUnits.ANNUM ) );
		}

		@Override
		public String jsonKey()
		{
			return this.jsonKey;
		}

		public Boolean aggregate()
		{
			return this.aggregate;
		}

		public Gender referentGender()
		{
			return this.referentGender;
		}

		public Gender partnerGender()
		{
			return this.partnerGender;
		}

		public Range<ComparableQuantity<Time>> ageDiff()
		{
			return this.ageDiff;
		}
	}
}
