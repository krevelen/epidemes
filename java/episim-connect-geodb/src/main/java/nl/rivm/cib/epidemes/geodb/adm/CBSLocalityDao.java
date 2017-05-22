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

/**
 * {@link CBSLocalityDao} see https://www.cbs.nl/nl-nl/achtergrond/2014/13/bevolkingskernen-in-nederland-2011
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class CBSLocalityDao
{
//	TABLE nl.adm_cbs_bevolkingskernen_2011
//	(
//	  objectid integer NOT NULL,
//	  kern_code character varying(10),
//	  kern_naam character varying(45),
//	  prov_code character varying(10),
//	  bkgr_code character varying(10),
//	  x_gba integer,
//	  y_gba integer,
//	  aantkern01 smallint,
//	  bev11tot integer,
//	  gemlft integer,
//	  bev_t0_14 integer,
//	  bev_t15_24 integer,
//	  bev_t25_44 integer,
//	  bev_t45_64 integer,
//	  bev_t65pl integer,
//	  bev_man integer,
//	  bev_vrw integer,
//	  ppart_tot integer,
//	  ppart0_14 integer,
//	  ppart15_24 integer,
//	  ppart25_44 integer,
//	  ppart45_64 integer,
//	  ppart65pl integer,
//	  tot_eenp integer,
//	  eenp15_24 integer,
//	  eenp25_44 integer,
//	  eenp45_64 integer,
//	  eenp65pl integer,
//	  tot_mp_mk integer,
//	  mp_mk0_14 integer,
//	  mp_mk15_24 integer,
//	  mp_mk25_44 integer,
//	  mp_mk45_64 integer,
//	  mp_mk65pl integer,
//	  tot_mp_zk integer,
//	  mp_zk0_14 integer,
//	  mp_zk15_24 integer,
//	  mp_zk25_44 integer,
//	  mp_zk45_64 integer,
//	  mp_zk65pl integer,
//	  tot_ongeh integer,
//	  tot_ong_mk integer,
//	  tot_ong_zk integer,
//	  tot_gehuwd integer,
//	  tot_geh_mk integer,
//	  tot_geh_zk integer,
//	  tot_eenoud integer,
//	  tot_samenw integer,
//	  tot_instit integer,
//	  kn_autoch integer,
//	  kn_allo_w integer,
//	  kn_allo_nw integer,
//	  p_lagopl15 integer,
//	  p_midopl15 integer,
//	  p_hogopl15 integer,
//	  p_wbv_1524 integer,
//	  p_wbv_2544 integer,
//	  p_wbv_4554 integer,
//	  p_wbv_5564 integer,
//	  p_wbv_6574 integer,
//	  p_wrk_lndb integer,
//	  p_wrk_nijv integer,
//	  p_wrk_cmd integer,
//	  p_wrk_ncmd integer,
//	  p_wrk_ov integer,
//	  p_wrk_onb integer,
//	  bev01tot integer,
//	  saldo_bin integer,
//	  saldo_buit integer,
//	  tothh_eenp integer,
//	  tothh_mpmk integer,
//	  tothh_mpzk integer,
//	  tothh_t integer,
//	  tothh_1 integer,
//	  tothh_2 integer,
//	  tothh_3 integer,
//	  tothh_4 integer,
//	  tothh_5 integer,
//	  tothh_6pl integer,
//	  woning11 integer,
//	  woning01 integer,
//	  won_prc_e integer,
//	  won_prc_h integer,
//	  won_prc_o integer,
//	  won_bez_t numeric(38,8),
//	  won_bez_e numeric(38,8),
//	  won_bez_h numeric(38,8),
//	  won_woz_t integer,
//	  won_woz_e integer,
//	  won_woz_h integer,
//	  wooneenh smallint,
//	  recrwon smallint,
//	  afs_haprak numeric(38,8),
//	  av5_haprak numeric(38,8),
//	  afs_hapost numeric(38,8),
//	  afs_ziekhs numeric(38,8),
//	  afs_apoth numeric(38,8),
//	  afs_bsop numeric(38,8),
//	  afs_kdvblf numeric(38,8),
//	  afs_basond numeric(38,8),
//	  afs_vmbo numeric(38,8),
//	  afs_havwo numeric(38,8),
//	  afs_super numeric(38,8),
//	  afs_ovlevm numeric(38,8),
//	  av5_ovlevm numeric(38,8),
//	  afs_oprit numeric(38,8),
//	  opptot smallint,
//	  landn smallint,
//	  watern smallint,
//	  oad smallint,
//	  sted smallint,
//	  kernnummer smallint,
//	  shape geometry,
//	  CONSTRAINT enforce_srid_shape CHECK (srid(shape) = 28992)
//	)
}
