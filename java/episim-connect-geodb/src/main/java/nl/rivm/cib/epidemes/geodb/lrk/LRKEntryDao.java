/* $Id: 474906e117ef85b400559a8509e16754bd5e4f33 $
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
package nl.rivm.cib.epidemes.geodb.lrk;

import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Geometry;

import io.coala.persist.Persistable;

/**
 * {@link LRKEntryDao}
 * <p>
 * Hibernate spatial type converts geometry field to JTD Geometry
 * <p>
 * Google Earth projects uses EPSG:900913 geodesy, see http://epsg.io/900913
 * <p>
 * RIVM xCoord/yCoord use EPSG:28992 geodesy, see http://epsg.io/28992
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity( name = LRKEntryDao.ENTITY_NAME )
@Table( name = "voorz_lrkp_20150115" )
public class LRKEntryDao implements Persistable.Dao
{
	public static final String ENTITY_NAME = "LRKEntryDao";

	public enum RegistryStatus
	{
		/** unregistered */
		Uitgeschreven,
		/** registered */
		Ingeschreven;

		@Converter
		public static class JPAConverter
			implements AttributeConverter<RegistryStatus, String>
		{
			@Override
			public String
				convertToDatabaseColumn( final RegistryStatus attribute )
			{
				return attribute.name();
			}

			@Override
			public RegistryStatus
				convertToEntityAttribute( final String dbData )
			{
				return RegistryStatus.valueOf( dbData );
			}
		}
	}

	public enum OrganizationType
	{
		/** */
		GOB,
		/** Voorziening Gastouderopvang / kindergarten parents */
		VGO,
		/** Buitenschoolse Opvang / (pre/after)school day care */
		BSO,
		/** Kinderdagverblijf / preschool, nursery school, kindergarten */
		KDV,
		/** Peuterspeelzaal / infant/toddler child care */
		PSZ;

		@Converter
		public static class JPAConverter
			implements AttributeConverter<OrganizationType, String>
		{
			@Override
			public String
				convertToDatabaseColumn( final OrganizationType attribute )
			{
				return attribute.name();
			}

			@Override
			public OrganizationType
				convertToEntityAttribute( final String dbData )
			{
				return OrganizationType.valueOf( dbData );
			}
		}
	}

	@Column( name = "objectid", nullable = false )
	protected Long id;

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "uniek_nr" )
	protected Long uniekNr;

	@Column( name = "lrk_id" )
	protected Long registryCode;

	@Column( name = "type_oko", length = 3 )
	@Convert( converter = OrganizationType.JPAConverter.class )
	protected OrganizationType type;

	@Column( name = "actuele_naam_oko", length = 100 )
	protected String name;

	@Column( name = "aantal_kindplaatsen" )
	protected Integer childCapacity;

	@Column( name = "status", length = 15 )
	@Convert( converter = RegistryStatus.JPAConverter.class )
	protected RegistryStatus status;

	@Temporal( TemporalType.TIMESTAMP )
	@Column( name = "inschrijfdatum" )
	protected Date registryDate;

	@Temporal( TemporalType.TIMESTAMP )
	@Column( name = "uitschrijfdatum" )
	protected Date deregistryDate;

	@Column( name = "opvanglocatie_adres", length = 50 )
	protected String address;

	@Column( name = "opvanglocatie_postcode", length = 6 )
	protected String zip;

	@Column( name = "opvanglocatie_woonplaats", length = 30 )
	protected String city;

	@Column( name = "pc4" )
	protected Integer pc4;

	@Column( name = "gem_code" )
	protected Integer municipalityCode;

	@Column( name = "verantwoordelijke_gemeente", length = 30 )
	protected String municipality;

	@Column( name = "x_coord" )
	protected Long xCoord;

	@Column( name = "y_coord" )
	protected Long yCoord;

//	@Column( name = "geocod", length = 20 )
//	protected String geocod; // enum: pc6hnrletter, pc6hnrtoev, pc6hnr, pc6

	@Column( name = "shape" )
	@Type(type="org.hibernate.spatial.GeometryType")
	protected Geometry shape;
}