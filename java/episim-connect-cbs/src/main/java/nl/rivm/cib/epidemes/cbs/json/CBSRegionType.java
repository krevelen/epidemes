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

import com.fasterxml.jackson.annotation.JsonValue;

import io.coala.exception.Thrower;
import nl.rivm.cib.episim.model.locate.Region;

/**
 * {@link CBSRegionType}
 * <p>
 * TODO: regular expressions to distinguish digit lengths
 * <p>
 * see http://statline.cbs.nl/StatWeb/selection/default.aspx?DM=SLNL&PA=83287ned
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public enum CBSRegionType
{

	/** Woonplaats */
	CITY( "WP" ),

	/** buurt, e.g. 'BU00030000' */
	BOROUGH( "BU", "BU%04d%02d%02d" ),

	/** wijk, e.g. 'WK000300' */
	WARD( "WK", "WK%04d%02d" ),

	/** gemeente, e.g. 'GM0003' (Appingedam) */
	MUNICIPAL( "GM", "GM%04d" ),

	/** corop (analytical region) */
	COROP( "CR", "CR%02d" ),

	/** COROP-subgebied */
	COROP_SUB( "CS" ),

	/** COROP-plusgebied */
	COROP_PLUS( "CP" ),

	/** provincie */
	PROVINCE( "PV", "PV%02d" ),

	/** landsdeel */
	TERRITORY( "LD", "LD%02d" ),

	/** land */
	COUNTRY( "NL" ),

	/** NUTS1 */
	NUTS1( "NL" ),

	/** NUTS2 */
	NUTS2( "NL" ),

	/** NUTS3 */
	NUTS3( "NL" ),

	/** arbeidsmarktregio (GEM+UWV) */
	LABOR_MARKET( "AM" ),

	/** arrondissement (rechtbank) */
	DISTRICT( "AR" ),

	/** GGD-regio */
	HEALTH_SERVICES( "GG" ),

	/** Jeugdzorgregio */
	CHILD_SERVICES( "JZ" ),

	/** Kamer van Koophandel */
	COMMERCE( "KK" ),

	/** Landbouwgebied */
	AGRICULTURE( "LB" ),

	/** Landbouwgebied-groep */
	AGRI_GROUP( "LG" ),

	/** Politie regionale eenheid */
	POLICE( "RE" ),

	/** Ressorten (gerechtshof) */
	APPEAL_COURT( "RT" ),

	/** RPA-gebied */
	LABOR_PLATFORM( "RP" ),

	/** Toeristengebied */
	TOURISM( "TR" ),

	/** Veiligheidsregio */
	SAFETY( "VR" ),

	/** WGR-samenwerkingsgebied */
	MUNICIPAL_COOP( "WG" ),

	/** Zorgkantoorregio */
	HEALTH_WELFARE( "ZK" ),
//
	;

	private final String prefix;

	private final String format;

	private final Region.TypeID typeId;

	private CBSRegionType( final String prefix )
	{
		this( prefix, null );
	}

	private CBSRegionType( final String prefix, final String format )
	{
		this.prefix = prefix;
		this.format = format;
		this.typeId = Region.TypeID.of( name() );
	}

	@JsonValue
	public String getPrefix()
	{
		return prefix();
	}

	public String prefix()
	{
		return this.prefix;
	}

	@SuppressWarnings( "unchecked" )
	public <T> String toString( final T... args )
	{
		return format( args );
	}

	@SuppressWarnings( "unchecked" )
	public <T> String format( final T... args )
	{
		return String.format( this.format, args );
	}

	public Region.TypeID toTypeID()
	{
		return this.typeId;
	}

	public static CBSRegionType parse( final String regionId )
	{
		if( regionId == null ) return null;
		final String s = regionId.trim();
		if( s.isEmpty() ) return null;

		// FIXME replace by regular expressions?
		if( s.substring( 0, 2 ).equalsIgnoreCase( "NL" ) ) switch( s.length() )
		{
		case 3:
			return NUTS1;
		case 5:
			return NUTS3;
		case 4:
			return s.charAt( 2 ) == '0' ? COUNTRY : NUTS2;
		default:
			return Thrower.throwNew( IllegalArgumentException::new,
					() -> "Unknown region type for: " + regionId );
		}

		for( CBSRegionType value : values() )
			if( s.substring( 0, value.prefix.length() )
					.equalsIgnoreCase( value.prefix ) )
				return value;

		return Thrower.throwNew( IllegalArgumentException::new,
				() -> "Unknown region type for: " + regionId );
	}
}