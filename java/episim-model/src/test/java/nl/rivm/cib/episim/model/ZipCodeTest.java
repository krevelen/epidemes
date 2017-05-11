package nl.rivm.cib.episim.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import nl.rivm.cib.episim.model.locate.ZipCode;

/**
 * {@link ZipCodeTest} tests {@link ZipCode}
 * 
 * @version $Id: e0ef6d1eaf79e3d56d75415a3e9194589bb224db $
 * @author Rick van Krevelen
 */
public class ZipCodeTest
{

	@Test
	public void testPostCode6()
	{
		final String pc6 = "1234AB";
		final String pc4 = "1234";
		final String pc3 = "123";
		final String[] valid = { pc6, "1234 ab ", "\t1234\tab\r\n",
				"1234\t\taB" };
		final String[] invalid = { pc3, pc4, "234 ab ", "\t234\tab\r\n",
				"1234a" };
		for( String i : valid )
		{
			final ZipCode zip = ZipCode.valueOf( i );
			assertEquals( "should parse as pc6: " + pc6, pc6,
					zip.toPostCode6() );
			assertEquals( "should parse as pc4: " + pc4, pc4,
					zip.toPostCode4() );
			assertEquals( "should parse as pc3: " + pc3, pc3,
					zip.toPostCode3() );
		}
		for( String i : invalid )
		{
			final ZipCode zip = ZipCode.valueOf( i );
			assertNotEquals( "should not parse as pc6: " + pc6, pc6,
					zip.toPostCode6() );
			assertNotEquals( "should not parse as pc4: " + pc4, pc4,
					zip.toPostCode4() );
			assertNotEquals( "should not parse as pc3: " + pc3, pc3,
					zip.toPostCode3() );
		}
	}

}
