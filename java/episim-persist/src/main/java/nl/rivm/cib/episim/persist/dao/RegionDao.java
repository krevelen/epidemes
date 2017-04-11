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
package nl.rivm.cib.episim.persist.dao;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import io.coala.bind.BindableDao;
import io.coala.bind.LocalBinder;
import io.coala.math.QuantityJPAConverter;
import io.coala.persist.JPAUtil;
import io.coala.time.Instant;
import nl.rivm.cib.episim.model.locate.Geography;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.persist.AbstractDao;
import nl.rivm.cib.episim.persist.dimension.IsoTimeDimensionDao;
import tec.uom.se.unit.Units;

/**
 * {@link RegionDao}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity
@Cacheable
@Table( name = "REGION" )
public class RegionDao extends AbstractDao
	implements BindableDao<Region, RegionDao>
{
	/** the primary key (for database join statements) */
	@Id
	@GeneratedValue
	@Column( name = "PK" )
	protected Integer pk;

	/** the code (e.g. CBS reference) */
	@Column( name = "CODE" )
	protected String code;

	/**
	 * the {@link Geography} type of region, e.g. province (administrative) or
	 * parish (religious)
	 */
	@Column( name = "TYPE" )
	protected String type;

	/** the name */
	@Column( name = "NAME" )
	protected String name;

	/** the size */
	@Column( name = "SIZE" )
	@Convert( converter = QuantityJPAConverter.class )
	protected Quantity<?> size;

	public static final Unit<Area> AREA_UNIT = Units.SQUARE_METRE;

	// unidirectional ternary association, see https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch07.html#collections-ternary
	@OneToMany
	@MapKeyJoinColumn( name = "REGION_PARENTS" )
	@OrderBy( "posix" )
	protected SortedMap<IsoTimeDimensionDao, RegionDao> parents;

	// unidirectional ternary association, see https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch07.html#collections-ternary
	@OneToMany
	@MapKeyJoinColumn( name = "REGION_CHILDREN" )
	@OrderBy( "posix" )
	protected SortedMap<IsoTimeDimensionDao, RegionDao> children;

	@Transactional
	public static RegionDao find( final EntityManager em, final Region region )
	{
//		final Comparable<?> value = Objects.requireNonNull( id.unwrap() );
//		final UUID contextRef = Objects.requireNonNull( id.contextRef() );
		try
		{
			final CriteriaBuilder cb = em.getCriteriaBuilder();
			final CriteriaQuery<RegionDao> qry = cb
					.createQuery( RegionDao.class );
			final Root<RegionDao> root = qry.from( RegionDao.class );
			return em
					.createQuery( qry.select( root ).where( cb.and(
//							cb.equal( root.get( "name" ), region.name() ),
//							cb.equal( root.get( "type" ), region.type() ),
							cb.equal( root.get( "code" ), region.id() ) ) ) )
					.getSingleResult();
		} catch( final NoResultException ignore )
		{
			return null;
		}
	}

	/**
	 * @param em the {@link EntityManager} context
	 * @param region the {@link Region} to persist
	 * @param offset for persisting the parent trace timing
	 * @return a {@link RegionDao}
	 */
	@Transactional
	public static RegionDao create( final EntityManager em, final Region region,
		final OffsetDateTime offset )
	{
		final RegionDao result = new RegionDao();
		result.code = region.id().unwrap();
		result.name = region.name();
		result.type = region.type().unwrap();
		result.size = region.surfaceArea();
		if( region.parents() != null )
		{
			result.parents = new TreeMap<>();
			region.parents()
					.forEach( ( since, parent ) -> result.parents.put(
							IsoTimeDimensionDao.persist( em, since,
									offset ),
							persist( em, parent, offset ) ) );
		}

		em.persist( result );
		return result;
	}

	@Transactional // not really
	public static RegionDao persist( final EntityManager em,
		final Region region, final OffsetDateTime offset )
	{
		return JPAUtil.findOrCreate( em, () -> find( em, region ),
				() -> create( em, region, offset ) );
	}

	@Override
	public Region restore( final LocalBinder binder )
	{
		final Map<Instant, Region> parents = this.parents.entrySet().stream()
				.collect( Collectors.toMap( e -> e.getKey().restore( binder ),
						e -> e.getValue().restore( binder ) ) );
		final Map<Instant, Region> children = this.children.entrySet().stream()
				.collect( Collectors.toMap( e -> e.getKey().restore( binder ),
						e -> e.getValue().restore( binder ) ) );
		return Region.of( Region.ID.of( this.code ), this.name,
				Region.TypeID.of( this.type ), parents, children,
				this.size.asType( Area.class ) );
	}
}
