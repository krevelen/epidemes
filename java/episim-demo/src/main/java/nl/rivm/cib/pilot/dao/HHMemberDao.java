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
package nl.rivm.cib.pilot.dao;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.ujmp.core.Matrix;

import io.coala.persist.Persistable;
import io.coala.time.Instant;
import io.coala.time.TimeUnits;
import nl.rivm.cib.pilot.hh.HHMemberAttribute;
import nl.rivm.cib.pilot.hh.HHMemberStatus;

/**
 * {@link HHMemberDao} is an {@link Embeddable} member data access object
 */
@Embeddable
public class HHMemberDao implements Persistable.Dao
{
	/**
	 * @param now current virtual time {@link Instant} for calculating age
	 * @param data member data {@link Matrix} per {@link HHMemberAttribute}
	 * @param rowIndex the member's respective row index
	 * @return a {@link HHMemberDao}
	 */
	public static HHMemberDao create( final Instant now, final Matrix data,
		final long rowIndex )
	{
		if( rowIndex < 0 ) return null;
		final HHMemberDao result = new HHMemberDao();
		result.age = now.to( TimeUnits.ANNUM ).decimal()
				.subtract( data.getAsBigDecimal( rowIndex,
						HHMemberAttribute.BIRTH.ordinal() ) );
		result.male = data.getAsBoolean( rowIndex,
				HHMemberAttribute.MALE.ordinal() );
		result.status = HHMemberStatus.values()[data.getAsInt( rowIndex,
				HHMemberAttribute.STATUS.ordinal() )].jsonValue();
//			result.behavior = data.getAsInt( rowIndex,
//					HHMemberAttribute.BEHAVIOR.ordinal() );
		return result;
	}

	public static final String AGE_ATTR = "age";

	public static final String STATUS_ATTR = "status";

	public static final String MALE_ATTR = "male";

	public static final String BEHAVIOR_ATTR = "behaviorRef";

	public static final String OFFICE_ATTR = "officeZip";

	public static final String LEISURE_ATTR = "leisureZip";

	@Column
	protected BigDecimal age;

	@Column
	protected String status;

	@Column
	protected boolean male;

	@Column
	protected int behaviorRef;

	@Column
	protected String officeZip;

	@Column
	protected String leisureZip;

}