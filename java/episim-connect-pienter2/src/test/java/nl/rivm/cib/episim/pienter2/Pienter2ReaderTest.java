package nl.rivm.cib.episim.pienter2;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.eobjects.metamodel.sas.metamodel.SasDataContext;
import org.junit.Ignore;
import org.junit.Test;

import io.coala.json.JsonUtil;
import io.coala.util.FileUtil;
import io.coala.xml.JAXBCache;

/**
 * {@link Pienter2ReaderTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Pienter2ReaderTest
{

	/** */
	private static final Logger LOG = LogManager
			.getLogger( Pienter2ReaderTest.class );

	private static final File XML = new File( ".." + File.separator + ".."
			+ File.separator + "data" + File.separator + "pienter"
			+ File.separator + "pienter.xml" );

	@Ignore // FIXME org.eobjects.metamodel.sas.SasReaderException: Page 5 has unknown type: -112
	@Test
	public void testSAS()
	{
		LOG.info( "Start PIENTER2 SAS import test" );
		final File path = new File( ".." + File.separator + ".."
				+ File.separator + "data" + File.separator + "pienter" );
		assertTrue( path.getAbsolutePath() + " should exist", path.exists() );
		final DataContext ctx = new SasDataContext( path );
		final Schema schema = ctx.getDefaultSchema();

		LOG.trace( "Got Pienter2 table: {}",
				Arrays.asList( schema.getTableNames() ) );

		final Table table = schema.getTable( 0 );
		LOG.trace( "Got Pienter2 columns: {}",
				Arrays.asList( table.getColumnNames() ) );

		LOG.info( "Completed PIENTER2 SAS import test" );
	}

	interface SASPienter
	{
		String ROOT_ELEMENT = "LIBRARY";
		String ENTRY_ELEMENT = "PIENTER2_LV5";
		String PARTICIPANT_ID_COL = "ParticipantId";

		// demographics
		String HH_PROV_COL = "Provincie";
		String HH_CITY_COL = "Gemeente";
		String HH_PC4_COL = "ZipCode";
		String HH_SIZE_COL = "pershuis";
		String HH_INCOME_COL = "netink";
		String HH_AGE_1_COL = "Lftpershuis1";
		String HH_AGE_2_COL = "Lftpershuis2";
		String HH_AGE_3_COL = "Lftpershuis3";
		String HH_AGE_4_COL = "Lftpershuis4";
		String HH_AGE_5_COL = "Lftpershuis5";
		String HH_AGE_6_COL = "Lftpershuis6";
		String HH_AGE_7_COL = "Lftpershuis7";
		String HH_AGE_8_COL = "Lftpershuis8";
		String HH_AGE_9_COL = "Lftpershuis9";
		String HH_AGE_10_COL = "Lftpershuis10";
		String MY_BIRTH_COL = "gebdat2";
		String MY_AGE_FLOATYR_COL = "age"; // age in years (float), derived
		String MY_AGE_INTYEAR_COL = "LFTB"; // age in years (int), derived
		String MY_AGE_GROUP_COL = "Lftgroeb";
		String MY_SEX_COL = "gesl2";
		String MY_MARITAL_COL = "burg";
		String MY_NATIONALITY_COL = "nl"; // 1=NL, 2=Surinaamse, 3=Antilliaanse, 4=Arubaanse, 5=Turkse,  6=Marokkaanse, 7=andnatio
		String MY_NATIONALITY_EXT_COL = "natioand";
		String MY_ORIGIN_COL = "gebland"; // 1=NL, 2=Surinaamse, 3=Antilliaanse, 4=Arubaanse, 5=Turkse,  6=Marokkaanse, 7=andnatio
		String MY_ORIGIN_EXT_COL = "geblandand";
		String MY_NL_ARRIVAL_YEAR_COL = "jaarnl";
		String MY_EDU_LEVEL_COL = "oplzelf";
		String MY_RELIGION_COL = "geloof";
		String MY_DENOMINATION_COL = "pckgeloof"; //pckgeloofa
		
		// contact
		String HH_DAYCARE1_COL = "crechhuis"; // 1=+, 2=-
		String HH_DAYCARE2_COL = "crechkind"; // 1=+, 2=-
		String HH_DAYCARE1_SEMIDAYS_COL = "crechhuisdeel"; // 0..10
		String HH_DAYCARE2_SEMIDAYS_COL = "crechkinddeel"; // 0..10
		String MY_CONTACT_PATIENTS_COL = "beroepgrcont_patient";
		String MY_CONTACT_CLIENTS_COL = "beroepgrcont_client";
		String MY_CONTACT_PUPILS_COL = "beroepgrcont_kind";
		String MY_SEX_AGE_COL = "sekslft";
		String MY_SEX_PARTNER_COL = "vastpart";//1=+,2=-,3=?
		String MY_SEX_PARTNER_ORIGIN_COL = "Landvastpart";//1=nl, 2... see natio
		String MY_SEX_PARTNER_COUNT_HY_COL = "aantalsekspartgetal";
		// TODO add contact age groups and frequencies...
		
		// vaccine
		String MY_VACC_COL = "rvp";
		String MY_VACC_PROF_COL = "beroepinent"; // 1=+, 2=-
		String MY_VAC_ATT_COL = "houdingvac";
		String MY_VAC_ATT_INF_COL = "";// 1=antro, 2=homeo, 3=natuur, 4=geloof, 5=anders, 6=geen
		String MY_VAC_ATT_KID_COL = "toekvactoedien"; //1=surely...6=surely not,7=n/a
		String MY_VAC_ATT_KID_POS_COL = "redenvacja"; // 1=+,2=-
		String MY_VAC_ATT_KID_NEG_COL = "redenvacnee"; // 1=+,2=-
		String MY_VAC_ATT_KID_CONFIDENT_COL = "kindvacbescherm"; // 1=agree...5=disagree
		String MY_VAC_ATT_KID_COMPLACENT_COL = "noodzaakvac"; // 1=agree...5=disagree
		String MY_VAC_ATT_KID_WARY_COL = "veiligvac"; // 1=agree...5=disagree
		String MY_VAC_ATT_KID_ADVERSE_COL = "afweeropbvac"; // 1=agree...5=disagree
		String MY_VAC_ATT_KID_HERD_COL = "gezondhandvac"; // 1=agree...5=disagree
		String MY_GENITAL_WARTS_COL = "Genitwrat"; // 1=+, 2=-, 3=? HPV?
		String MY_MEASLES_BMR1_COL = "bmr1";// datum
		String MY_MEASLES_BMR2_COL = "bmr2";// datum
		String MY_MEASLES_BMR3_COL = "bmr3";// datum
		String MY_MEASLES_BMR4_COL = "bmr4";// datum
		String MY_MEASLES_BMR1_REP_COL = "bmr1_rep";
		String MY_MEASLES_BMR_REP_COL = "bmr_rep";
		String MY_MEASLES1_COL = "mazelen1";// datum
		String MY_MEASLES2_COL = "mazelen2";// datum
		String MY_MEASLES3_COL = "mazelen3";// datum
		String MY_MEASLES1_REP_COL = "mazelen1_rep";
		String MY_INFLUENZA1_COL = "Influenza1";// datum
		String MY_INFLUENZA2_COL = "Influenza2";// datum
		String MY_INFLUENZA_REP_COL = "influenza_rep";//Influenza1..2 (datum) influenza_rep

		JAXBCache<?> jaxb = JAXBCache.of( PienterEntry.class );

	}

	@XmlRootElement( name = SASPienter.ROOT_ELEMENT )
	public static class SASLibrary
	{
		@XmlElements( @XmlElement( name = SASPienter.ENTRY_ELEMENT ) )
		public List<PienterEntry> records;
	}

	public static class PienterEntry
	{
		@XmlElement( name = SASPienter.PARTICIPANT_ID_COL )
		public int participantId;

		@XmlElement( name = SASPienter.HH_PC4_COL )
		public String zipCode;

		@XmlElement( name = SASPienter.HH_SIZE_COL )
		public short hhSize;

		@XmlElement( name = SASPienter.HH_AGE_1_COL )
		public short hhAge1;

		@XmlElement( name = SASPienter.HH_AGE_2_COL )
		public short hhAge2;

		@XmlElement( name = SASPienter.HH_AGE_3_COL )
		public short hhAge3;

		@XmlElement( name = SASPienter.HH_AGE_4_COL )
		public short hhAge4;

		@XmlElement( name = SASPienter.HH_AGE_5_COL )
		public short hhAge5;

		@XmlElement( name = SASPienter.HH_AGE_6_COL )
		public short hhAge6;

		@XmlElement( name = SASPienter.HH_AGE_7_COL )
		public short hhAge7;

		@XmlElement( name = SASPienter.HH_AGE_8_COL )
		public short hhAge8;

		@XmlElement( name = SASPienter.HH_AGE_9_COL )
		public short hhAge9;

		@XmlElement( name = SASPienter.HH_AGE_10_COL )
		public short hhAge10;

		@Override
		public String toString()
		{
			return JsonUtil.stringify( this );
		}
	}

	@Test
	public void testJAXB() throws IOException
	{
		LOG.info( "Start PIENTER2 JAXB import test" );
		assertTrue( XML.getAbsolutePath() + " should exist", XML.exists() );
		try( final InputStream is = FileUtil.toInputStream( XML ) )
		{

			final SASLibrary root = JAXB.unmarshal( is, SASLibrary.class );
			LOG.trace( "Got records: {}", JsonUtil.toJSON( root.records ) );
		}

		LOG.info( "Completed PIENTER2 JAXB test" );
	}

//	@Ignore
	@Test
	public void testStAX() throws IOException
	{
		LOG.info( "Start PIENTER2 StAX test" );
		assertTrue( XML.getAbsolutePath() + " should exist", XML.exists() );

		final AtomicInteger counter = new AtomicInteger( 0 );
		final String[] path = { SASPienter.ROOT_ELEMENT,
				SASPienter.ENTRY_ELEMENT };
		try( final InputStream is = FileUtil.toInputStream( XML ) )
		{
			SASPienter.jaxb.matchAndParse( PienterEntry.class, is, path )
					// XmlUtil.matchElementPath( is, path )
					.doOnError( e -> LOG.warn( "ignoring error: {}",
							e.getMessage() ) )
					//.take( 100 )
					.subscribe( xml ->
					{
						counter.incrementAndGet();
//						LOG.trace( "Matched and parsed #{}: {}",
//								counter.incrementAndGet(), xml );
					}, e ->
					{
					}, () ->
					{
						try
						{
//							LOG.trace( "Completed XML stream filter" );
							is.close();
						} catch( final Exception ignore )
						{
						}
					} );
			LOG.info( "Completed PIENTER2 StAX test, got: {} entries",
					counter.get() );
		}
	}

}
