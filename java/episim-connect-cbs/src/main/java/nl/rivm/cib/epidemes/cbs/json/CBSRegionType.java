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

import io.coala.exception.Thrower;
import nl.rivm.cib.episim.model.locate.Region;

/**
 * {@link CBSRegionType} FIXME: regular expressions to distinguish digit lengths
 * 
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

	/** corop */
	COROP( "CR", "CR%02d" ),

	/** provincie */
	PROVINCE( "PV", "PV%02d" ),

	/** landsdeel */
	TERRITORY( "LD", "LD%02d" ),

	/** land in het Koninkrijk, e.g. 'NL00' (Nederland) */
	COUNTRY( "NL", "NL%02d" ),

	/** arbeidsmarktregio */
	LABORMARKET( "AM" ),

	/** arrondissement */
	AR( "AR" ),

	/** COROP-subgebied */
	CS( "CS" ),

	/** COROP-plusgebied */
	CP( "CP" ),

	/** GGD-regio */
	GGD( "GG" ),

	/** Jeugdzorgregio */
	JZ( "JZ" ),

	/** Kamer van Koophandel */
	COMMERCE( "KK" ),

	/** Landbouwgebied */
	AGRICULTURE( "LB" ),

	/** Landbouwgebied-groep */
	AGRICULTURE_GROUP( "LG" ),

	/** Ressorten (rechtsgebied) */
	RE( "RE" ),

	/**  */
	RT( "RT" ),

	/** RPA-gebied */
	RP( "RP" ),

	/** Toeristengebied */
	TR( "TR" ),

	/** Veiligheidsregio */
	VR( "VR" ),

	/** WGR-samenwerkingsgebied */
	WG( "WG" ),

	/** Zorgkantoorregio */
	ZK( "ZK" ),
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

	protected String getPrefix()
	{
		return this.prefix;
	}

	@SuppressWarnings( "unchecked" )
	public <T> String toString( final T... args )
	{
		return String.format( this.format, args );
	}

	public Region.TypeID toTypeID()
	{
		return this.typeId;
	}

	public static CBSRegionType parse( final String regionId )
	{
		for( CBSRegionType value : values() )
			if( regionId.contains( value.prefix ) ) return value;
		return Thrower.throwNew( IllegalArgumentException.class,
				"Unknown region type for: {}", regionId );
	}
}