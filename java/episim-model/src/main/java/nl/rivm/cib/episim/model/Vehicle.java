package nl.rivm.cib.episim.model;

import nl.rivm.cib.episim.time.Timed;

/**
 * {@link Vehicle} is a mobile machine that transports people or cargo.
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Vehicle extends Timed
{

	TransmissionSpace getSpace();
	
	Status getStatus();
	
	// FIXME use Enterprise Transaction pattern ?
	enum Status
	{
		STILL,
		
		MOVING,
		
		;
	}
}
