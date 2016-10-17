package nl.rivm.cib.episim.model.locate.travel;

import nl.rivm.cib.episim.model.locate.Locatable;

/**
 * {@link Vehicle} is a mobile machine that transports people or cargo.
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Vehicle extends Locatable
{

	Status getStatus();

	// FIXME use Enterprise Transaction pattern ?
	enum Status
	{
		STILL,

		MOVING,

		;
	}
}
