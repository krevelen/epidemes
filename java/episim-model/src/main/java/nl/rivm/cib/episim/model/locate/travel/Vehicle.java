package nl.rivm.cib.episim.model.locate.travel;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import nl.rivm.cib.episim.model.disease.infection.TransmissionSpace;
import nl.rivm.cib.episim.model.locate.Place;

/**
 * {@link Vehicle} is a mobile carrier of a {@link #space()} having a
 * {@link #status()} of being either {@link Vehicle.Status#STILL} at or
 * {@link Vehicle.Status#MOVING} toward a {@link #destination()}.
 * 
 * @version $Id: 4f1dd68d7aee66104076eef90f27277a6f5deba7 $
 * @author Rick van Krevelen
 */
public interface Vehicle extends Actor<Vehicle.Movement>
{

	Place destination();

	TransmissionSpace space();

	Status status();

	/**
	 * {@link Directory} will retrieve or generate specified {@link Vehicle}
	 */
	interface Directory
	{
		Vehicle lookup( Actor.ID id );
	}

	interface Movement extends Fact
	{

	}

	enum Status
	{
		/** delivering/taking passengers */
		STILL,

		/** transporting passengers */
		MOVING,

		;
	}
}
