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
package nl.rivm.cib.episim.hesitant;

import io.coala.math.Range;
import io.coala.time.Duration;
import io.coala.time.TimeUnits;

/**
 * {@link VaxProcess} TODO as BPM with time/sequence constraints on dose vax
 * activities?
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface VaxProcess
{

	/**
	 * see https://www.wikiwand.com/en/DPT_vaccine and
	 * https://www.wikiwand.com/en/Hib_vaccine and
	 * https://www.wikiwand.com/en/Hepatitis_B_vaccine
	 */
	String DKTP_SERIES = "DKTP";

	VaxDose[] DKTP = {
			VaxDose.of( 1, DKTP_SERIES, Duration.of( 42, TimeUnits.DAYS ),
					Range.of( Duration.of( 25, TimeUnits.DAYS ),
							Duration.of( 5, TimeUnits.ANNUM ) ) ),
			VaxDose.of( 2, DKTP_SERIES, Duration.of( 90, TimeUnits.DAYS ) ),
			VaxDose.of( 3, DKTP_SERIES,
					Duration.of( 120, TimeUnits.DAYS ) ),
			VaxDose.of( 4, DKTP_SERIES,
					Duration.of( 330, TimeUnits.DAYS ) ),
			VaxDose.of( 5, DKTP_SERIES, Duration.of( 4, TimeUnits.ANNUM ) ),
			VaxDose.of( 6, DKTP_SERIES,
					Duration.of( 9, TimeUnits.ANNUM ) ) };

	/** see https://www.wikiwand.com/en/Pneumococcal_vaccine */
	String PNEU_SERIES = "Pneu";

	VaxDose[] PNEU = {
			VaxDose.of( 1, PNEU_SERIES, Duration.of( 60, TimeUnits.DAYS ),
					Range.of( Duration.of( 25, TimeUnits.DAYS ),
							Duration.of( 5, TimeUnits.ANNUM ) ) ),
			VaxDose.of( 2, PNEU_SERIES,
					Duration.of( 120, TimeUnits.DAYS ) ),
			VaxDose.of( 3, PNEU_SERIES,
					Duration.of( 330, TimeUnits.DAYS ) ) };

	/** see https://www.wikiwand.com/en/Meningococcal_vaccine */
	String MENC_SERIES = "MenC";

	VaxDose[] MENC = { VaxDose.of( 1, MENC_SERIES,
			Duration.of( 1.16, TimeUnits.ANNUM ) ) };

	/** see https://www.wikiwand.com/en/MMR_vaccine */
	String BMR_SERIES = "BMR";

	VaxDose[] BMR = {
			VaxDose.of( 1, BMR_SERIES,
					Duration.of( 1.16, TimeUnits.ANNUM ) ),
			VaxDose.of( 2, BMR_SERIES,
					Duration.of( 9, TimeUnits.ANNUM ) ) };

	/** see https://www.wikiwand.com/en/HPV_vaccines */
	String HPV_SERIES = "HPV";
	VaxDose[] HPV = {
			VaxDose.of( 1, HPV_SERIES, Duration.of( 12, TimeUnits.ANNUM ) ),
			VaxDose.of( 2, HPV_SERIES,
					Duration.of( 12, TimeUnits.ANNUM ) ) };

	VaxDose[][] RVP_SCHEDULE = { DKTP, PNEU, BMR, MENC, HPV };

}