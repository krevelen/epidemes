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
package nl.rivm.cib.epidemes.geodb.bag;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.geolatte.geom.Point;

import io.coala.persist.Persistable;

/**
 * {@link NHREntryDao} describes entries in the National Commerce Registry
 * <p>
 * RIVM xCoord/yCoord use EPSG:28992 geodesy, see http://epsg.io/28992
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity( name = NHREntryDao.ENTITY_NAME )
@Table( /*schema = "nl",*/ name = "werk_nhr_bag_2016_01" )
public class NHREntryDao implements Persistable.Dao
{
	public static final String ENTITY_NAME = "NHREntryDao";

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "objectid", nullable = false )
	protected Long id;

	@Column( name = "vestigingsnummer", length = 12 )
	protected String establishmentNr;

	@Column( name = "dossiernr", length = 8 )
	protected String registryCode;

	@Column( name = "subdossiernr", length = 4 )
	protected String registrySubcode;

	@Column( name = "handelsnaam45", length = 45 )
	protected String name;

	@Column( name = "bedr_straat", length = 50 )
	protected String street;

	@Column( name = "bedr_hnr integer" )
	protected Long houseNr;

	@Column( name = "bedr_hnr_toev", length = 25 )
	protected String houseNrExt;

	@Column( name = "bedr_pc6", length = 6 )
	protected String zip;

	@Column( name = "bedr_wpl" )
	protected String city;

	@Column( name = "geocod", length = 5 )
	protected String geoCode;

	@Column( name = "pht", length = 18 )
	protected String zipHouseNr;

	@Column( name = "hoofdactiviteitencode", length = 6 )
	protected String primaryActivityCode;

	@Column( name = "nevenactiviteitencode1", length = 6 )
	protected String secondaryActivityCode;

	@Column( name = "nevenactiviteitencode2", length = 6 )
	protected String tertiaryActivityCode;

	@Column( name = "klassewptot", length = 2 )
	protected String employeeTotalClass;

	@Column( name = "klassewpfull", length = 2 )
	protected String employeeFullClass;

	@Temporal( TemporalType.DATE )
	@Column( name = "peildatum_werkzame_personen" )
	protected Date employeeCountDate;

	@Column( name = "wptot" )
	protected Long employeeTotal;

	@Column( name = "wpfull" )
	protected Long employeeFull;

	@Column( name = "xcoord" )
	protected Long xCoord;

	@Column( name = "ycoord" )
	protected Long yCoord;

	@Column( name = "geometrie" )
	protected Point<?> geometrie;
}