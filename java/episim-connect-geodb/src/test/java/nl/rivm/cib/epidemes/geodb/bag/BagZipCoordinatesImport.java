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
package nl.rivm.cib.epidemes.geodb.bag;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.persist.JDBCConfig;
import io.coala.persist.JDBCUtil;
import io.coala.util.FileUtil;
import io.reactivex.Observable;

/**
 * {@link BagZipCoordinatesImport}
 * 
 * <pre>
 * CREATE TABLE nl.bag_pc6_2016_01
(
  objectid integer NOT NULL DEFAULT nextval('nummertje'::regclass),
  postcode character varying(6),
  openbareruimtenaam character varying(80),
  woonplaatsnaam character varying(80),
  bagcnt integer,
  pc6pers integer,
  pc6won integer,
  bedrcnt integer,
  wptot integer,
  wpfull integer,
  gmpwont integer,
  meandist integer,
  avgx integer,
  avgy integer,
  geometrie geometry,
  CONSTRAINT bag_pc
)
 * </pre>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class BagZipCoordinatesImport
{

	@Sources( { "file:${user.dir}/conf/geodb.properties" } )
	public interface GeoDBConfig extends JDBCConfig
	{
		@Key( JDBC_DRIVER_KEY )
		@DefaultValue( "org.postgresql.Driver" )
		Class<? extends Driver> jdbcDriver();

		@Key( JDBC_URL_KEY )
		@DefaultValue( "jdbc:postgresql://geodb.rivm.nl/sde_gdbrivm" )
		URI jdbcUrl();

		@DefaultValue( PASSWORD_PROMPT_VALUE )
		@Key( JDBC_PASSWORD_KEY )
		@ConverterClass( PasswordPromptConverter.class )
		String jdbcPassword();

//		@DefaultValue( "" + true )
//		boolean ssl();

	}

	public static class bag_pc6_2016_01
	{
		public String pc6;
		public int nr;
		public Map<String, Object> geo;
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( BagZipCoordinatesImport.class );

	public static void main( final String[] args )
		throws ClassNotFoundException, SQLException
	{
//		final GeoDBConfig conf = ConfigCache.getOrCreate( GeoDBConfig.class );
//		LOG.trace( "Testing with JDBC config: {}", conf.export() );
//		conf.execute( "SELECT t.postcode, t.pc6won, t.pc6pers"
//				+ ", ST_AsGeoJSON( ST_Transform( t.geometrie, 4326 ) ) AS "
//				+ geo + " FROM nl.bag_pc6_2016_01 As t" // 
////				+ " WHERE t.pc6won>0" //
////				+ " LIMIT 100" //
//				, rs -> handle( JDBCUtil.toJSON( rs ), file ) );

		// refactor imported json data: from array to object
//		handle( JsonUtil.readArrayAsync( () -> FileUtil.toInputStream( file ),
//				ObjectNode.class ), "bag-test2.json" );

		final GeoDBConfig conf = ConfigCache.getOrCreate( GeoDBConfig.class );
		LOG.trace( "Testing with JDBC config: {}", conf.export() );
		conf.execute( "SELECT t.postcode, t.pc6won, t.pc6pers"
				+ ", ST_AsGeoJSON( ST_Transform( t.geometrie, 4326 ) ) AS "
				+ geo + " FROM nl.voorz_lrkp_20150115 As t" // 
//				+ " WHERE t.pc6won>0" //
//				+ " LIMIT 100" //
				, rs -> handle( JDBCUtil.toJSON( rs ), file ) );
	}

	/**
	 * <pre>
	 * CREATE TABLE nl.voorz_duo_po_vestigingen_20170102
	(
	objectid integer NOT NULL,
	bevoegd_gezag_nummer character varying(255),
	brin_nummer character varying(255),
	vestigingsnummer character varying(255),
	vestigingsnaam character varying(255),
	soort_primair_onderwijs character varying(255),
	cluster character varying(255),
	denominatie character varying(255),
	straatnaam character varying(255),
	huisnummer_toevoeging character varying(255),
	pc6 character varying(255),
	pc4 integer,
	plaatsnaam character varying(255),
	gem_nr_2016 integer,
	gemeentenaam character varying(255),
	provincie character varying(255),
	ggd character varying(255),
	internetadres character varying(255),
	x_coord integer,
	y_coord integer,
	geocodering character varying(255),
	leerlingen integer,
	gewicht_vest integer,
	impulsgebied integer,
	afstand integer,
	shape geometry,
	CONSTRAINT enforce_geotype_shape CHECK (geometrytype(shape) = 'POINT'::text OR shape IS NULL),
	CONSTRAINT enforce_srid_shape CHECK (srid(shape) = 28992)
	)
	
	CREATE TABLE nl.voorz_lrkp_20150115
	(
	objectid integer NOT NULL,
	uniek_nr integer,
	lrk_id integer,
	type_oko character varying(3),
	actuele_naam_oko character varying(100),
	aantal_kindplaatsen smallint,
	status character varying(15),
	inschrijfdatum timestamp without time zone,
	uitschrijfdatum timestamp without time zone,
	opvanglocatie_adres character varying(50),
	opvanglocatie_postcode character varying(6),
	opvanglocatie_woonplaats character varying(30),
	pc4 smallint,
	gem_code smallint,
	verantwoordelijke_gemeente character varying(30),
	x_coord integer,
	y_coord integer,
	geocod character varying(20),
	shape geometry,
	CONSTRAINT enforce_geotype_shape CHECK (geometrytype(shape) = 'POINT'::text OR shape IS NULL),
	CONSTRAINT enforce_srid_shape CHECK (srid(shape) = 28992)
	)
	 * </pre>
	 */

	private static final String file = "bag-test.json";

	private static final String geo = "st_asgeojson";

	private static void handle( final Observable<ObjectNode> array,
		final String outfile )
	{
		final ObjectNode res = array.map( node ->
		{
			if( !node.has( geo ) ) return node;
			final JsonNode coords = node.remove( geo ).get( "coordinates" );
			return node.put( "lat", coords.get( 0 ).decimalValue() ).put( "lon",
					coords.get( 1 ).decimalValue() );
		} ).collectInto( JsonUtil.getJOM().createObjectNode(),
				( r, i ) -> r.set( i.remove( "postcode" ).asText(), i ) )
				.blockingGet();
		try( final OutputStream os = FileUtil.toOutputStream( outfile, false ) )
		{
			os.write( JsonUtil.toJSON( res ).getBytes() );
		} catch( final IOException e )
		{
			LOG.error( "Problem writing geo json", e );
		}
	}
}
