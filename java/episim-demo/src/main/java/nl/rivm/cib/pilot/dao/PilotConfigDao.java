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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NoResultException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;

import com.eaio.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.coala.bind.LocalId;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.persist.Persistable;
import io.coala.persist.UUIDToByteConverter;
import io.coala.time.Instant;
import nl.rivm.cib.pilot.PilotConfig;

/**
 * {@link PilotConfigDao} with JPA MetaModel in {@link HHConfigDao_}?
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity
@Table( name = "RUNS" )
@SequenceGenerator( name = PilotConfigDao.CFG_SEQ, allocationSize = 25 )
public class PilotConfigDao implements Persistable.Dao
{
	public static final String CFG_SEQ = "CFG_SEQ";

	private static final Logger LOG = LogUtil.getLogger( PilotConfigDao.class );

	public static PilotConfigDao find( final EntityManager em, final LocalId id )
	{
		final UUID contextRef = Objects.requireNonNull( id.contextRef() );
		try
		{
			final CriteriaBuilder cb = em.getCriteriaBuilder();
			final CriteriaQuery<PilotConfigDao> qry = cb
					.createQuery( PilotConfigDao.class );
			final Root<PilotConfigDao> root = qry.from( PilotConfigDao.class );
			final Predicate filter = cb.and();
			filter.getExpressions().add( cb.equal( root.get( 
					// FIXME PilotConfigDao_.context missing due to custom type? 
					"context" ), contextRef ) );
			return em.createQuery( qry.select( root ).where( filter ) )
					.getSingleResult();
		} catch( final NoResultException ignore )
		{
			return null; // ok, not found
		}
	}

	/**
	 * @param now current virtual time {@link Instant} for calculating age
	 * @param config the {@link HHConfig}
	 * @return a {@link HHMemberDao}
	 */
	public static PilotConfigDao create( final LocalId id, final PilotConfig config,
		final Number seed )
	{
		final PilotConfigDao result = new PilotConfigDao();
		result.context = Objects.requireNonNull( id.contextRef() );
		result.setup = Objects.requireNonNull( id.unwrap() ).toString();
		result.seed = seed.longValue();
		result.json = JsonUtil
				.stringify( config.toJSON( PilotConfig.SCENARIO_BASE ) );
		try
		{
			result.hash = MessageDigest.getInstance( "MD5" )
					.digest( result.json.getBytes( "UTF-8" ) );
		} catch( final NoSuchAlgorithmException
				| UnsupportedEncodingException e )
		{
			result.hash = Integer.toHexString( result.json.hashCode() )
					.getBytes();
			LOG.error( "Problem", e );
		}
		result.yaml = config.toYAML( PilotConfig.class.getSimpleName()
				+ " for replication: " + id.toJSON(), PilotConfig.SCENARIO_BASE );
		return result;
	}

	@Id
	@GeneratedValue//( generator = CFG_SEQ )
//	@SequenceGenerator( name = CFG_SEQ, sequenceName = CFG_SEQ )
	@Column( name = "PK", nullable = false, updatable = false )
	protected Integer pk = null;

	/** time stamp of insert, as per http://stackoverflow.com/a/3107628 */
	@Temporal( TemporalType.TIMESTAMP )
	@Column( name = "CREATED_TS", insertable = false, updatable = false,
		columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP" )
	@JsonIgnore
	protected Date created = null;

	@Column( name = "CONTEXT", unique = true, nullable = false,
		updatable = false, length = 16, columnDefinition = "BINARY(16)" )
	@Convert( converter = UUIDToByteConverter.class )
	protected UUID context;

	@Column( name = "SETUP", nullable = false, updatable = false )
	protected String setup;

	@Column( name = "SEED", nullable = false, updatable = false )
	protected long seed;

	@Lob
	@Basic( fetch = FetchType.LAZY )
	@Column( name = "HASH", nullable = false, updatable = false )
	protected byte[] hash;

	@Lob
	@Basic( fetch = FetchType.LAZY )
	@Column( name = "JSON", nullable = false, updatable = false )
	protected String json;

	@Lob
	@Basic( fetch = FetchType.LAZY )
	@Column( name = "YAML", nullable = false, updatable = false )
	protected String yaml;

}