package nl.rivm.cib.episim.persist;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import io.coala.json.JsonUtil;

/**
 * {@link AbstractDao} is a data access object for the location
 * dimension
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@JsonAutoDetect( fieldVisibility = Visibility.PROTECTED_AND_PUBLIC )
public abstract class AbstractDao
{
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + JsonUtil.stringify( this );
	}
}
