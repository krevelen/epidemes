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

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aeonbits.owner.util.Collections;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.util.FileUtil;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;

/**
 * {@link Cbs83287Json}: Gebieden in Nederland 2016 [2016; Stopgezet;
 * 2016-06-03T02:00:00]
 * 
 * <pre>
 * CBS.83287ned (see http://statline.cbs.nl/Statweb/publication/?PA=83287ned)
[[2]]	: Regio's (CBS.83287ned.Meta$RegioS), e.g. with(CBS.83287ned.Meta$RegioS, Title[match(CBS.83287ned[1,2], Key)])="Aa en Hunze"
	: Codes en namen van gemeenten
[[3]]	: *  Code ("Code_1":String), e.g. CBS.83287ned[1,3]="GM1680    "
[[4]]	: *  Naam ("Naam_2":String), e.g. CBS.83287ned[1,4]="Aa en Hunze                                       "
[[5]]	: *  Sortering naam ("SorteringNaam_3":String), e.g. CBS.83287ned[1,5]="Aa en Hunze                                       "
	: Lokaliseringen van gemeenten
	: *  Arbeidsmarktregio's
[[6]]	: *  *  Code ("Code_4":String), e.g. CBS.83287ned[1,6]="AM01      "
[[7]]	: *  *  Naam ("Naam_5":String), e.g. CBS.83287ned[1,7]="Groningen                                         "
	: *  Arrondissementen (rechtsgebieden)
[[8]]	: *  *  Code ("Code_6":String), e.g. CBS.83287ned[1,8]="AR05      "
[[9]]	: *  *  Naam ("Naam_7":String), e.g. CBS.83287ned[1,9]="Noord-Nederland                                   "
	: *  COROP-gebieden
[[10]]	: *  *  Code ("Code_8":String), e.g. CBS.83287ned[1,10]="CR07      "
[[11]]	: *  *  Naam ("Naam_9":String), e.g. CBS.83287ned[1,11]="Noord-Drenthe                                     "
	: *  COROP-subgebieden
[[12]]	: *  *  Code ("Code_10":String), e.g. CBS.83287ned[1,12]="CS070     "
[[13]]	: *  *  Naam ("Naam_11":String), e.g. CBS.83287ned[1,13]="Noord-Drenthe                                     "
	: *  COROP-plusgebieden
[[14]]	: *  *  Code ("Code_12":String), e.g. CBS.83287ned[1,14]="CP0700    "
[[15]]	: *  *  Naam ("Naam_13":String), e.g. CBS.83287ned[1,15]="Noord-Drenthe                                     "
	: *  GGD-regio's
[[16]]	: *  *  Code ("Code_14":String), e.g. CBS.83287ned[1,16]="GG0706    "
[[17]]	: *  *  Naam ("Naam_15":String), e.g. CBS.83287ned[1,17]="GGD Drenthe                                       "
	: *  Jeugdzorgregio’s
[[18]]	: *  *  Code ("Code_16":String), e.g. CBS.83287ned[1,18]="JZ04      "
[[19]]	: *  *  Naam ("Naam_17":String), e.g. CBS.83287ned[1,19]="Drenthe                                           "
	: *  Kamer van Koophandel
[[20]]	: *  *  Code ("Code_18":String), e.g. CBS.83287ned[1,20]="KK42      "
[[21]]	: *  *  Naam ("Naam_19":String), e.g. CBS.83287ned[1,21]="Noord                                             "
	: *  Landbouwgebieden
[[22]]	: *  *  Code ("Code_20":String), e.g. CBS.83287ned[1,22]="LB2205    "
[[23]]	: *  *  Naam ("Naam_21":String), e.g. CBS.83287ned[1,23]="Drentse Veenkoloniën en Hondsrug                  "
	: *  Landbouwgebieden (groepen)
[[24]]	: *  *  Code ("Code_22":String), e.g. CBS.83287ned[1,24]="LG02      "
[[25]]	: *  *  Naam ("Naam_23":String), e.g. CBS.83287ned[1,25]="Veenkoloniën en Oldambt                           "
	: *  Landsdelen
[[26]]	: *  *  Code ("Code_24":String), e.g. CBS.83287ned[1,26]="LD01      "
[[27]]	: *  *  Naam ("Naam_25":String), e.g. CBS.83287ned[1,27]="Noord-Nederland                                   "
	: *  NUTS1-gebieden
[[28]]	: *  *  Code ("Code_26":String), e.g. CBS.83287ned[1,28]="NL1       "
[[29]]	: *  *  Naam ("Naam_27":String), e.g. CBS.83287ned[1,29]="Noord-Nederland                                   "
	: *  NUTS2-gebieden
[[30]]	: *  *  Code ("Code_28":String), e.g. CBS.83287ned[1,30]="NL13      "
[[31]]	: *  *  Naam ("Naam_29":String), e.g. CBS.83287ned[1,31]="Drenthe                                           "
	: *  NUTS3-gebieden
[[32]]	: *  *  Code ("Code_30":String), e.g. CBS.83287ned[1,32]="NL131     "
[[33]]	: *  *  Naam ("Naam_31":String), e.g. CBS.83287ned[1,33]="Noord-Drenthe                                     "
	: *  Politie Regionale eenheden
[[34]]	: *  *  Code ("Code_32":String), e.g. CBS.83287ned[1,34]="RE01      "
[[35]]	: *  *  Naam ("Naam_33":String), e.g. CBS.83287ned[1,35]="Noord-Nederland                                   "
	: *  Provincies
[[36]]	: *  *  Code ("Code_34":String), e.g. CBS.83287ned[1,36]="PV22      "
[[37]]	: *  *  Naam ("Naam_35":String), e.g. CBS.83287ned[1,37]="Drenthe                                           "
	: *  Ressorten (rechtsgebieden)
[[38]]	: *  *  Code ("Code_36":String), e.g. CBS.83287ned[1,38]="RT12      "
[[39]]	: *  *  Naam ("Naam_37":String), e.g. CBS.83287ned[1,39]="Gerechtshof Arnhem-Leeuwarden                     "
	: *  RPA-gebieden
[[40]]	: *  *  Code ("Code_38":String), e.g. CBS.83287ned[1,40]="RP03      "
[[41]]	: *  *  Naam ("Naam_39":String), e.g. CBS.83287ned[1,41]="Centraal-Groningen                                "
	: *  Toeristengebieden
[[42]]	: *  *  Code ("Code_40":String), e.g. CBS.83287ned[1,42]="TR12      "
[[43]]	: *  *  Naam ("Naam_41":String), e.g. CBS.83287ned[1,43]="Groningse, Friese en Drentse Zandgronden          "
	: *  Veiligheidsregio's
[[44]]	: *  *  Code ("Code_42":String), e.g. CBS.83287ned[1,44]="VR03      "
[[45]]	: *  *  Naam ("Naam_43":String), e.g. CBS.83287ned[1,45]="Drenthe                                           "
	: *  Wgr-samenwerkingsgebieden
[[46]]	: *  *  Code ("Code_44":String), e.g. CBS.83287ned[1,46]="WG07      "
[[47]]	: *  *  Naam ("Naam_45":String), e.g. CBS.83287ned[1,47]="Noord- en Midden-Drenthe                          "
	: *  Zorgkantoorregio's
[[48]]	: *  *  Code ("Code_46":String), e.g. CBS.83287ned[1,48]="ZK06      "
[[49]]	: *  *  Naam ("Naam_47":String), e.g. CBS.83287ned[1,49]="Drenthe                                           "
	: Grootte en stedelijkheid van gemeenten
	: *  Gemeentegrootte
[[50]]	: *  *  Code ("Code_48":String), e.g. CBS.83287ned[1,50]="4"
[[51]]	: *  *  Omschrijving ("Omschrijving_49":String), e.g. CBS.83287ned[1,51]="20 000 tot 50 000 inwoners                        "
	: *  Stedelijkheid
[[52]]	: *  *  Code ("Code_50":String), e.g. CBS.83287ned[1,52]="5"
[[53]]	: *  *  Omschrijving ("Omschrijving_51":String), e.g. CBS.83287ned[1,53]="Niet stedelijk           "
	: Statistische gegevens
[[54]]	: *  Inwonertal ("Inwonertal_52":Long), e.g. CBS.83287ned[1,54]="25243"
[[55]]	: *  Omgevingsadressendichtheid ("Omgevingsadressendichtheid_53":Long), e.g. CBS.83287ned[1,55]="278"
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Cbs83287Json
{

	public static void main( final String[] args ) throws IOException
	{
		final String file = args == null || args.length == 0
				? "conf/83287NED.json" : args[0];
		final Cbs83287Json json = parse( FileUtil.toInputStream( file ) );
		LogUtil.getLogger( Cbs83287Json.class ).info( "Parsed {} -> \n{}\n{}",
				file, json.toMap(), json.toReverseMap() );
	}

	public static Cbs83287Json parse( final InputStream is ) throws IOException
	{
		return JsonUtil.valueOf( is, Cbs83287Json.class );
	}

	public Map<String, Map<CBSRegionType, String>> toMap()
	{
		return IntStream.range( 0, this.reg.length )
				// TODO use primitives directly?
				.mapToObj( i -> i )
				.collect( Collectors.toMap( i -> this.reg[i], i -> this.refs
						.values().stream().map( ref -> ref.get( i ) )
						.filter( ref -> !ref.isEmpty() ) // skip empty NUTS3 values
						.collect( Collectors.toMap(
								ref -> CBSRegionType.parse( ref ), ref -> ref,
								( k1, k2 ) -> k1, () -> new EnumMap<>(
										CBSRegionType.class ) ) ) ) );
	}

	public Map<CBSRegionType, Map<String, Set<String>>> toReverseMap()
	{
		return IntStream.range( 0, this.reg.length )
				// TODO use primitives directly?
				.mapToObj( i -> i )
				.flatMap( i -> this.refs.values().stream()
						.map( ref -> Collections.entry( ref.get( i ),
								this.reg[i] ) ) )
				.filter( e -> !e.getKey().isEmpty() ) // skip empty NUTS3 values
				.reduce( new EnumMap<CBSRegionType, Map<String, Set<String>>>(
						CBSRegionType.class ), ( m, e ) ->
						{
							m.computeIfAbsent(
									CBSRegionType.parse( e.getKey() ),
									type -> new TreeMap<>() )
									.computeIfAbsent( e.getKey(),
											agg -> new TreeSet<>() )
									.add( e.getValue() );
							return m;
						}, ( m1, m2 ) ->
						{
							m1.putAll( m2 );
							return m1;
						} );
	}

	@JsonProperty( "reg" )
	public String[] reg;

	@JsonIgnore
	public Map<String, List<String>> refs = new TreeMap<>();

	@JsonAnySetter
	public void set( final String key, final List<String> value )
	{
		this.refs.put( key, value );
	}

	@JsonAnyGetter
	public List<String> get( final String key )
	{
		return this.refs.get( key );
	}

}
