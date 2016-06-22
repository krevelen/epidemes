package nl.rivm.cib.episim.model;

import io.coala.time.Timed;

/**
 * {@link Vehicle} is a mobile machine that transports people or cargo.
 * 
 * @version $Id: 4f1dd68d7aee66104076eef90f27277a6f5deba7 $
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
