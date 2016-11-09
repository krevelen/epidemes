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
package nl.rivm.cib.epidemes.geodb;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table( name = "voorz_lrkp_20150115" )
public class OkoLocatieDao
{
	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "objectid", nullable = false )
	long id;

	@Column( name = "uniek_nr" )
	long uniekNr;

	@Column( name = "lrk_id" )
	long lrkId;

	@Column( name = "type_oko", length = 3 )
	OkoType typeOko;

	@Column( name = "actuele_naam_oko", length = 100 )
	String actueleNaam;

	@Column( name = "aantal_kindplaatsen" )
	int aantalKindplaatsen;

	@Column( name = "status", length = 15 )
	OkoStatus status;

	@Temporal( TemporalType.TIMESTAMP )
	@Column( name = "inschrijfdatum" )
	Date inschrijfdatum;

	@Temporal( TemporalType.TIMESTAMP )
	@Column( name = "uitschrijfdatum" )
	Date uitschrijfdatum;

	@Column( name = "opvanglocatie_adres", length = 50 )
	String adres;

	@Column( name = "opvanglocatie_postcode", length = 6 )
	String pc6;

	@Column( name = "opvanglocatie_woonplaats", length = 30 )
	String plaats;

	@Column( name = "pc4" )
	int pc4;

	@Column( name = "gem_code" )
	int gemCode;

	@Column( name = "verantwoordelijke_gemeente", length = 30 )
	String gemeente;

	@Column( name = "x_coord" )
	long xCoord;

	@Column( name = "y_coord" )
	long yCoord;

	@Column( name = "geocod", length = 20 )
	String geocod; // enum: pc6hnrletter, pc6hnrtoev, pc6hnr, pc6

//	@Column( name = "shape" )
//	 GeometryDao shape;
}