package nl.rivm.cib.episim.pienter2;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.eobjects.metamodel.sas.metamodel.SasDataContext;
import org.junit.Test;

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

	@Test
	public void test()
	{

		File path = new File( ".." + File.separator + ".." + File.separator
				+ "data" + File.separator + "pienter" );
		assertTrue( path.getAbsolutePath() + " should exist", path.exists() );
		final DataContext ctx = new SasDataContext( path );
		final Schema schema = ctx.getDefaultSchema();

		LOG.trace( "Got Pienter2 table: {}",
				Arrays.asList( schema.getTableNames() ) );

		final Table table = schema.getTable( 0 );
		LOG.trace( "Got Pienter2 columns: {}",
				Arrays.asList( table.getColumnNames() ) );

//		File myFile = new File( "persons.csv" );
//
//		DataContext dataContext = DataContextFactory
//				.createCsvDataContext( myFile );
//		Schema schema = dataContext.getDefaultSchema();
//		Table[] tables = schema.getTables();
//
//		// CSV files only has a single table in the default schema
//		assert tables.length == 1;
//
//		Table table = tables[0];
//
//		// there are several ways to get columns - here we simply get them by name
//		Column firstNameColumn = table.getColumnByName( "first_name" );
//		Column lastNameColumn = table.getColumnByName( "last_name" );
//
//		// use the table and column types in the query
//		Query q = dataContext.query().from( table ).select( firstNameColumn )
//				.toQuery();
//		System.out.println( q.toString() );

	}

}
