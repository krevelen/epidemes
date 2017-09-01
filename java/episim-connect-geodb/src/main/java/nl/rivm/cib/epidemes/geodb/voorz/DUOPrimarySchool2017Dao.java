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
package nl.rivm.cib.epidemes.geodb.voorz;

/**
 * {@link DUOPrimarySchool2017Dao}
 * see https://duo.nl/open_onderwijsdata/databestanden/po/adressen/adressen-po-3.jsp,
 * https://duo.nl/open_onderwijsdata/databestanden/vo/adressen/adressen-vo-2.jsp,
 * https://duo.nl/open_onderwijsdata/databestanden/vo/leerlingen/leerlingen-vo-1.jsp,
 * https://duo.nl/open_onderwijsdata/databestanden/ho/adressen/adressen-ho1.jsp,
 * https://duo.nl/open_onderwijsdata/databestanden/ho/ingeschreven/hbo-ingeschr/ingeschrevenen-hbo4.jsp,
 * https://duo.nl/open_onderwijsdata/databestanden/ho/ingeschreven/wo-ingeschr/ingeschrevenen-wo4.jsp
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class DUOPrimarySchool2017Dao
{
	public static final String TABLE_NAME = "voorz_duo_po_vestigingen_20170102";
	
//	CREATE TABLE nl.voorz_duo_po_vestigingen_20170102
//	(
//	  objectid integer NOT NULL,
//	  bevoegd_gezag_nummer character varying(255),
//	  brin_nummer character varying(255),
//	  vestigingsnummer character varying(255),
//	  vestigingsnaam character varying(255),
//	  soort_primair_onderwijs character varying(255),
//	  cluster character varying(255),
//	  denominatie character varying(255),
//	  straatnaam character varying(255),
//	  huisnummer_toevoeging character varying(255),
//	  pc6 character varying(255),
//	  pc4 integer,
//	  plaatsnaam character varying(255),
//	  gem_nr_2016 integer,
//	  gemeentenaam character varying(255),
//	  provincie character varying(255),
//	  ggd character varying(255),
//	  internetadres character varying(255),
//	  x_coord integer,
//	  y_coord integer,
//	  geocodering character varying(255),
//	  leerlingen integer,
//	  gewicht_vest integer,
//	  impulsgebied integer,
//	  afstand integer,
//	  shape geometry,
//	  CONSTRAINT enforce_geotype_shape CHECK (geometrytype(shape) = 'POINT'::text OR shape IS NULL),
//	  CONSTRAINT enforce_srid_shape CHECK (srid(shape) = 28992)
//	)
	
//	 TABLE nl.voorz_scholen_2003
//	 (
//	   objectid integer NOT NULL,
//	   admin_nr character varying(10),
//	   naam_org character varying(100),
//	   strnam_nen character varying(30),
//	   wplnam_nen character varying(20),
//	   gemnm character varying(30),
//	   functie character varying(30),
//	   pc6nr character varying(6),
//	   huisnr character varying(6),
//	   huistoe character varying(5),
//	   xcoord integer,
//	   ycoord integer,
//	   pc6hnrtoe character varying(17),
//	   pc4nr smallint,
//	   shape geometry,
//	   CONSTRAINT enforce_geotype_shape CHECK (geometrytype(shape) = 'POINT'::text OR shape IS NULL),
//	   CONSTRAINT enforce_srid_shape CHECK (srid(shape) = 28992)
//	 )
}
