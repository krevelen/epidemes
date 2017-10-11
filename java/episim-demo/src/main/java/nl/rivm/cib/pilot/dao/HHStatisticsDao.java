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
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.ujmp.core.Matrix;

import io.coala.bind.LocalId;
import io.coala.json.JsonUtil;
import io.coala.math.DecimalUtil;
import io.coala.persist.JPAUtil;
import io.coala.persist.Persistable;
import io.coala.time.Instant;
import io.coala.time.TimeUnits;
import nl.rivm.cib.pilot.hh.HHAttitudeEvaluator;
import nl.rivm.cib.pilot.hh.HHAttribute;
import nl.rivm.cib.pilot.hh.HHMemberAttribute;

/**
 * {@link HHStatisticsDao} with JPA MetaModel in {@link HouseholdDao_}?
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity
@Table( name = "HOUSEHOLDS" )
@SequenceGenerator( name = HHStatisticsDao.HH_SEQ, allocationSize = 25 )
public class HHStatisticsDao implements Persistable.Dao
{
	public static final String HH_SEQ = "HH_SEQ";

	private static final String TIME_COL_DEF = "DECIMAL(10,4)";

	private static final String DECIMAL_COL_DEF = "DECIMAL(15,8)";

	/**
	 * @param now current virtual time {@link Instant} for calculating age
	 * @param households household data {@link Matrix} per {@link HHAttribute}
	 * @param i the household's respective row index
	 * @param members member data {@link Matrix} per {@link HHMemberAttribute}
	 * @return a {@link HHMemberDao}
	 */
	public static HHStatisticsDao create( final PilotConfigDao run,
		final long i, final Instant now, final int seq,
		final String[] attractorNames, final Matrix households,
		final Matrix members, final Map<Long, Integer> activity,
		final HHAttitudeEvaluator evaluator )
	{
		final HHStatisticsDao result = new HHStatisticsDao();
		result.config = run;
		result.index = i;
		result.hh = households.getAsInt( i, HHAttribute.IDENTIFIER.ordinal() );
		result.seq = seq;
		result.inclusionDays = now.to( TimeUnits.DAYS ).decimal()
				.subtract( households.getAsBigDecimal( i,
						HHAttribute.SINCE_DAYS.ordinal() ) );
		result.attractorRef = attractorNames[households.getAsInt( i,
				HHAttribute.ATTRACTOR_REF.ordinal() ) % attractorNames.length];
		result.socialNetworkSize = households.getAsInt( i,
				HHAttribute.SOCIAL_NETWORK_SIZE.ordinal() );
		result.socialAssortativity = households.getAsBigDecimal( i,
				HHAttribute.SOCIAL_ASSORTATIVITY.ordinal() );
		result.impressPeriodDays = households.getAsBigDecimal( i,
				HHAttribute.IMPRESSION_PERIOD_DAYS.ordinal() );
		result.impressNumberRounds = households.getAsInt( i,
				HHAttribute.IMPRESSION_ROUNDS.ordinal() );
		result.impressNumberPeers = households.getAsInt( i,
				HHAttribute.IMPRESSION_FEEDS.ordinal() );
		result.impressNumberByPeer = JsonUtil.stringify( activity );
		if( evaluator != null )
		{
			result.attitude = evaluator.isPositive( null, households, i );
			final long pos = activity.keySet().stream().mapToLong( j -> j ).map(
					j -> evaluator.isPositive( null, households, j ) ? 1L : 0L )
					.sum();
			result.impressFractionPositive = pos == 0 ? BigDecimal.ZERO
					: DecimalUtil.divide( pos, activity.size() );
		}
		result.impressWeightAssortative = households.getAsBigDecimal( i,
				HHAttribute.IMPRESSION_INPEER_WEIGHT.ordinal() );
		result.impressWeightDissortative = households.getAsBigDecimal( i,
				HHAttribute.IMPRESSION_OUTPEER_WEIGHT.ordinal() );
		result.impressWeightSelf = households.getAsBigDecimal( i,
				HHAttribute.IMPRESSION_SELF_MULTIPLIER.ordinal() );
		result.impressWeightAttractor = households.getAsBigDecimal( i,
				HHAttribute.IMPRESSION_ATTRACTOR_MULTIPLIER.ordinal() );
//		result.schoolAssortativity = households.getAsBigDecimal( i,
//				HHAttribute.SCHOOL_ASSORTATIVITY.ordinal() );
		result.calculation = households.getAsBigDecimal( i,
				HHAttribute.CALCULATION.ordinal() );
		result.confidence = households.getAsBigDecimal( i,
				HHAttribute.CONFIDENCE.ordinal() );
		result.complacency = households.getAsBigDecimal( i,
				HHAttribute.COMPLACENCY.ordinal() );
		result.referent = HHMemberDao.create( now, members,
				households.getAsLong( i, HHAttribute.REFERENT_REF.ordinal() ) );
		result.partner = HHMemberDao.create( now, members,
				households.getAsLong( i, HHAttribute.PARTNER_REF.ordinal() ) );
		result.child1 = HHMemberDao.create( now, members,
				households.getAsLong( i, HHAttribute.CHILD1_REF.ordinal() ) );
		result.child2 = HHMemberDao.create( now, members,
				households.getAsLong( i, HHAttribute.CHILD2_REF.ordinal() ) );
		result.child3 = HHMemberDao.create( now, members,
				households.getAsLong( i, HHAttribute.CHILD3_REF.ordinal() ) );
		return result;
	}

	/**
	 * @param em
	 */
	public void persist( final EntityManager em, final LocalId id )
	{
		if( !em.contains( this.config ) )
			this.config = JPAUtil.findOrCreate( em,
					() -> PilotConfigDao.find( em, id ),
					() -> em.merge( this.config ) );
		em.persist( this );
	}

	@Id
	@GeneratedValue //( generator = HH_SEQ )
//	@SequenceGenerator( name = HH_SEQ, sequenceName = HH_SEQ )
	@Column( name = "PK", nullable = false, updatable = false )
	protected Integer pk = null;

	/** time stamp of insert, as per http://stackoverflow.com/a/3107628 */
//	@Temporal( TemporalType.TIMESTAMP )
//	@Column( name = "CREATED_TS", insertable = false, updatable = false,
//		columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP" )
//	@JsonIgnore
//	protected Date created = null;

//	@Column( name = "CONTEXT", nullable = false, updatable = false, length = 16,
//		columnDefinition = "BINARY(16)" )
//	@Convert( converter = UUIDToByteConverter.class )
//	protected UUID context;

	@ManyToOne( optional = false, cascade = CascadeType.PERSIST,
		fetch = FetchType.LAZY )
//	@Column( name = "CONFIG", nullable = false, updatable = false )
	public PilotConfigDao config;

	@Column( name = "SEQ", nullable = false, updatable = false )
	protected int seq;

	@Column( name = "INDEX", nullable = false, updatable = false )
	protected long index;

	@Column( name = "HH", nullable = false, updatable = false )
	protected long hh;

	@Column( name = "HH_DT_DAYS", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal inclusionDays;

	@Column( name = "ATTRACTOR_REF", nullable = false, updatable = false )
	protected String attractorRef;

	@Column( name = "SOCIAL_ASSORTATIVITY", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal socialAssortativity;

	@Column( name = "SOCIAL_NETWORK_SIZE", nullable = false, updatable = false )
	protected int socialNetworkSize;

	@Column( name = "IMPRESS_DT_DAYS", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal impressPeriodDays;

	@Column( name = "IMPRESS_N_ROUNDS", nullable = false, updatable = false )
	protected int impressNumberRounds;

	@Column( name = "IMPRESS_N_PEERS", nullable = false, updatable = false )
	protected int impressNumberPeers;

	@Column( name = "IMPRESS_N_BY_PEER", nullable = false, updatable = false,
		columnDefinition = "CLOB NOT NULL" )
	@Lob
	protected String impressNumberByPeer;

	@Column( name = "IMPRESS_F_POSITIVE", nullable = true, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal impressFractionPositive;

	@Column( name = "IMPRESS_W_ASSORT", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal impressWeightAssortative;

	@Column( name = "IMPRESS_W_DISSORT", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal impressWeightDissortative;

	@Column( name = "IMPRESS_W_SELF", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal impressWeightSelf;

	@Column( name = "IMPRESS_W_ATTRACTOR", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal impressWeightAttractor;

//	@Column( name = "SCHOOL_ASSORTATIVITY", nullable = false, updatable = false,
//		columnDefinition = ATTITUDE_COL_DEF )
//	protected BigDecimal schoolAssortativity;

	@Column( name = "CALCULATION", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal calculation;

	@Column( name = "CONFIDENCE", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal confidence;

	@Column( name = "COMPLACENCY", nullable = false, updatable = false,
		columnDefinition = DECIMAL_COL_DEF )
	protected BigDecimal complacency;

	@Column( name = "ATTITUDE", nullable = true, updatable = false,
		columnDefinition = "INT(1)" )
	protected Boolean attitude;

	@AttributeOverrides( {
			@AttributeOverride( name = HHMemberDao.AGE_ATTR,
				column = @Column( name = "REFERENT_AGE", nullable = false,
					updatable = false, columnDefinition = TIME_COL_DEF ) ),
			@AttributeOverride( name = HHMemberDao.STATUS_ATTR,
				column = @Column( name = "REFERENT_STATUS", nullable = false,
					updatable = false ) ),
			@AttributeOverride( name = HHMemberDao.MALE_ATTR,
				column = @Column( name = "REFERENT_MALE", nullable = false,
					updatable = false, columnDefinition = "INT(1)" ) ),
//			@AttributeOverride( name = MemberDao.BEHAVIOR_ATTR,
//				column = @Column( name = "REFERENT_BEHAVIOR", nullable = false,
//					updatable = false ) )
	} )
	@Embedded
	protected HHMemberDao referent;

	@AttributeOverrides( {
			@AttributeOverride( name = HHMemberDao.AGE_ATTR,
				column = @Column( name = "PARTNER_AGE", nullable = true,
					updatable = false, columnDefinition = TIME_COL_DEF ) ),
			@AttributeOverride( name = HHMemberDao.STATUS_ATTR,
				column = @Column( name = "PARTNER_STATUS", nullable = true,
					updatable = false ) ),
			@AttributeOverride( name = HHMemberDao.MALE_ATTR,
				column = @Column( name = "PARTNER_MALE", nullable = true,
					updatable = false, columnDefinition = "INT(1)" ) ),
//			@AttributeOverride( name = MemberDao.BEHAVIOR_ATTR,
//				column = @Column( name = "PARTNER_BEHAVIOR", nullable = true,
//					updatable = false ) )
	} )
	@Embedded
	protected HHMemberDao partner;

	@AttributeOverrides( {
			@AttributeOverride( name = HHMemberDao.AGE_ATTR,
				column = @Column( name = "CHILD1_AGE", nullable = true,
					updatable = false, columnDefinition = TIME_COL_DEF ) ),
			@AttributeOverride( name = HHMemberDao.STATUS_ATTR,
				column = @Column( name = "CHILD1_STATUS", nullable = true,
					updatable = false ) ),
			@AttributeOverride( name = HHMemberDao.MALE_ATTR,
				column = @Column( name = "CHILD1_MALE", nullable = true,
					updatable = false, columnDefinition = "INT(1)" ) ),
//			@AttributeOverride( name = MemberDao.BEHAVIOR_ATTR,
//				column = @Column( name = "CHILD1_BEHAVIOR", nullable = true,
//					updatable = false ) ) 
	} )
	@Embedded
	protected HHMemberDao child1;

	@AttributeOverrides( {
			@AttributeOverride( name = HHMemberDao.AGE_ATTR,
				column = @Column( name = "CHILD2_AGE", nullable = true,
					updatable = false, columnDefinition = TIME_COL_DEF ) ),
			@AttributeOverride( name = HHMemberDao.STATUS_ATTR,
				column = @Column( name = "CHILD2_STATUS", nullable = true,
					updatable = false ) ),
			@AttributeOverride( name = HHMemberDao.MALE_ATTR,
				column = @Column( name = "CHILD2_MALE", nullable = true,
					updatable = false, columnDefinition = "INT(1)" ) ),
//			@AttributeOverride( name = MemberDao.BEHAVIOR_ATTR,
//				column = @Column( name = "CHILD2_BEHAVIOR", nullable = true,
//					updatable = false ) ) 
	} )
	@Embedded
	protected HHMemberDao child2;

	@AttributeOverrides( {
			@AttributeOverride( name = HHMemberDao.AGE_ATTR,
				column = @Column( name = "CHILD3_AGE", nullable = true,
					updatable = false, columnDefinition = TIME_COL_DEF ) ),
			@AttributeOverride( name = HHMemberDao.STATUS_ATTR,
				column = @Column( name = "CHILD3_STATUS", nullable = true,
					updatable = false ) ),
			@AttributeOverride( name = HHMemberDao.MALE_ATTR,
				column = @Column( name = "CHILD3_MALE", nullable = true,
					updatable = false, columnDefinition = "INT(1)" ) ),
//			@AttributeOverride( name = MemberDao.BEHAVIOR_ATTR,
//				column = @Column( name = "CHILD3_BEHAVIOR", nullable = true,
//					updatable = false ) ) 
	} )
	@Embedded
	protected HHMemberDao child3;

	@Override
	public String toString()
	{
		return stringify();
	}
}