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
package nl.rivm.cib.epidemes.geodb.adm;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.geolatte.geom.Point;

/**
 * {@link CBSDistrict2013Dao} see
 * <ul>
 * <li>2013:
 * http://statline.cbs.nl/Statweb/publication/?PA=82341ned</li>
 * <li>2014:
 * http://statline.cbs.nl/Statweb/selection/?PA=82829NED</li>
 * <li>2015:
 * http://statline.cbs.nl/Statweb/publication/?PA=83304ned</li>
 * <li>2013-2015:
 * http://statline.cbs.nl/Statweb/publication/?PA=80305ned</li>
 * <li>2006-2012:
 * http://statline.cbs.nl/Statweb/publication/?PA=80306ned</lli>
 * </ul>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Entity( name = CBSDistrict2013Dao.ENTITY_NAME )
@Table( /* schema = "nl", */ name = CBSDistrict2013Dao.TABLE_NAME )
public class CBSDistrict2013Dao
{
	public static final String ENTITY_NAME = "CBSDistrictDao";

	public static final String TABLE_NAME = "adm_cbs_wijkbuurt_2013_v1";

//	public static final String TABLE_NAME = "adm_cbs_wijkbuurt_2014_v1";

	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "objectid", nullable = false )
	protected Long id;

	@Column( name = "bu_code", length = 13 )
	protected String buroughCode;

	@Column( name = "bu_naam", length = 60 )
	protected String buroughName;

	@Column( name = "wk_code", length = 12 )
	protected String wardCode;

	@Column( name = "wk_naam", length = 51 )
	protected String wardName;

	@Column( name = "gm_code", length = 11 )
	protected String municipalityCode;

	@Column( name = "gm_naam", length = 34 )
	protected String municipalityName;

	@Column( name = "postcode" )
	protected int zipCode;

	@Column( name = "dek_perc" )
	protected int percentCoverage;

	@Column( name = "aant_inw" )
	protected int nrInhabitants;

	@Column( name = "aant_man" )
	protected int nrMale;

	@Column( name = "aant_vrouw" )
	protected int nrFemale;

	@Column( name = "p_00_14_jr" )
	protected int percent00to14yo;

	@Column( name = "p_15_24_jr" )
	protected int percent15to24yo;

	@Column( name = "p_25_44_jr" )
	protected int percent25to44yo;

	@Column( name = "p_45_64_jr" )
	protected int percent45to64yo;

	@Column( name = "p_65_eo_jr" )
	protected int percent65plusyo;

	@Column( name = "p_ongehuwd" )
	protected int percentUnmarried;

	@Column( name = "p_gehuwd" )
	protected int percentMarried;

	@Column( name = "p_gescheid" )
	protected int percentDivorced;

	@Column( name = "p_verweduw" )
	protected int percentWidow;

	@Column( name = "bev_dichth" )
	protected int populationDensity;

	@Column( name = "aantal_hh" )
	protected int nrHousholds;

	@Column( name = "p_eenp_hh" )
	protected int percentHouseholdsOf1;

	@Column( name = "p_hh_z_k" )
	protected int percentHouseholdsWithoutKids;

	@Column( name = "p_hh_m_k" )
	protected int percentHouseholdsWithKids;

	@Column( name = "gem_hh_gr",precision=38, scale=8 )
	protected BigDecimal avgHouseholdSize;

	@Column( name = "p_west_al" )
	protected int percentWesternForeigner;

	@Column( name = "p_n_w_al" )
	protected int percentNonWesternForeigner;

	@Column( name = "p_marokko" )
	protected int percentMoroccan;

	@Column( name = "p_ant_aru" )
	protected int percentAntillianAruban;

	@Column( name = "p_surinam" )
	protected int percentSurinam;

	@Column( name = "p_turkije" )
	protected int percentTurkish;

	@Column( name = "p_over_nw" )
	protected int percentOtherNonWestern;

	@Column( name = "auto_tot" )
	protected int carsTotal;

	@Column( name = "auto_hh",precision=38, scale=8 )
	protected BigDecimal carsPerHousehold;

	@Column( name = "auto_land" )
	protected int carsPerArea;

	@Column( name = "bedr_auto" )
	protected int bedr_auto;

	@Column( name = "motor_2w" )
	protected int motor_2w;

	@Column( name = "a_bst_b" ) // TODO car drivers?
	protected int a_bst_b;

	@Column( name = "a_bst_nb" ) // TODO car drivers?
	protected int a_bst_nb;

	@Column( name = "a_lftj6j" )
	protected int nr0to5yo;

	@Column( name = "a_lfto6j" )
	protected int nr6plusyo;
	
	@Column( name = "opp_tot" )
	protected int areaTotal;

	@Column( name = "opp_land" )
	protected int areaLand;

	@Column( name = "opp_water" )
	protected int areaWater;

	@Column( name = "indeling", length=10 )
	protected String layout;

	@Column( name = "oad" )
	protected int oad;

	@Column( name = "geometry" )
	protected Point<?> geometry;

// 2013 only
	@Column( name = "gem_nr" )
	protected int gemeenteNr;
	
	@Column( name = "sted" )
	protected int urbanity;
	
	@Column( name = "af_ziek_i",precision=38, scale=8 )
	protected BigDecimal distanceHospitalWithOutpatient;

	@Column( name = "av5_ziek_i",precision=38, scale=8 )
	protected BigDecimal nrHospitalsWithOutpatientIn5km;

	@Column( name = "av10ziek_i",precision=38, scale=8 )
	protected BigDecimal nrHospitalsWithOutpatientIn10km;

	@Column( name = "av20ziek_i",precision=38, scale=8 )
	protected BigDecimal nrHospitalsWithOutpatientIn20km;

	@Column( name = "af_ziek_e",precision=38, scale=8 )
	protected BigDecimal distanceHospitalInpatientOnly;

	@Column( name = "av5_ziek_e",precision=38, scale=8 )
	protected BigDecimal nrHospitalsInpatientOnlyIn5km;

	@Column( name = "av10ziek_e",precision=38, scale=8 )
	protected BigDecimal nrHospitalsInpatientOnlyIn10km;

	@Column( name = "av20ziek_e",precision=38, scale=8 )
	protected BigDecimal nrHospitalsInpatientOnlyIn20km;

	@Column( name = "af_kdv",precision=38, scale=8 )
	protected BigDecimal distanceChildCare;

	@Column( name = "av1_kdv",precision=38, scale=8 )
	protected BigDecimal nrChildCareIn1km;

	@Column( name = "av3_kdv",precision=38, scale=8 )
	protected BigDecimal nrChildCareIn3km;

	@Column( name = "av5_kdv",precision=38, scale=8 )
	protected BigDecimal nrChildCareIn5km;

	@Column( name = "af_bso",precision=38, scale=8 )
	protected BigDecimal distanceDayCare;

	@Column( name = "av1_bso",precision=38, scale=8 )
	protected BigDecimal nrDayCare1km;

	@Column( name = "av3_bso",precision=38, scale=8 )
	protected BigDecimal nrDayCare3km;

	@Column( name = "av5_bso",precision=38, scale=8 )
	protected BigDecimal nrDayCare5km;

	@Column( name = "af_oprith",precision=38, scale=8 )
	protected BigDecimal distanceHighway;

	@Column( name = "af_treinst",precision=38, scale=8 )
	protected BigDecimal distanceTrainStation;

	@Column( name = "af_overst",precision=38, scale=8 )
	protected BigDecimal distanceHubStation;

	@Column( name = "af_biblio",precision=38, scale=8 )
	protected BigDecimal distanceLibrary;

	@Column( name = "af_ijsbaan",precision=38, scale=8 )
	protected BigDecimal distanceIcetrack;

	@Column( name = "af_brandw",precision=38, scale=8 )
	protected BigDecimal distanceFireDept;
	
// 2014 only	
//	  gm_nr integer, (2014)
//	  stedelijkheid smallint (2014)
//	  ind_wijziging smallint, (2014)
//	  wijk_label character varying(59), (2014)
//	  regio_code character varying(11), (2014)
}
