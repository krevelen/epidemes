package nl.rivm.cib.episim.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.coala.json.Wrapper;

/**
 * {@link ZipCode} is a simple {@link Wrapper} of {@link String} values
 * 
 * @version $Id: b982370873248279b0a036896e18ecdfca28e257 $
 * @author Rick van Krevelen
 */
public class ZipCode extends Wrapper.Simple<String>
{

	private static final Pattern POSTCODE_PATTERN = Pattern
			.compile( "(\\d+)\\s*([a-zA-Z]{2})" );

	/**
	 * @param value the {@link String} value to wrap
	 * @return the {@link ZipCode} decorator
	 */
	public static ZipCode valueOf( final String value )
	{
		return Util.of( value, new ZipCode() );
	}

	protected Matcher match()
	{
		if( unwrap() == null || unwrap().isEmpty() ) return null;
		final Matcher result = POSTCODE_PATTERN.matcher( unwrap() );
		return result.find() ? result : null;
	}

	public String toPostCode3()
	{
		final Matcher m = match();
		return m == null ? null : m.group( 1 ).substring( 0, 3 );
	}

	public String toPostCode4()
	{
		final Matcher m = match();
		return m == null ? null : m.group( 1 );
	}

	public String toPostCode6()
	{
		final Matcher m = match();
		return m == null ? null : m.group( 1 ) + m.group( 2 ).toUpperCase();
	}
}