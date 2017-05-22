package nl.rivm.cib.episim.model.locate;

import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;

/**
 * {@link Transporter} is a mobile carrier having a current {@link #status()}
 * {@link Destination} being e.g.
 * <li>moving ({@link Destination#kind()} == {@link FactKind#REQUESTED}); or
 * <li>arrived ({@link Destination#kind()} == {@link FactKind#STATED})
 * 
 * @version $Id: 4f1dd68d7aee66104076eef90f27277a6f5deba7 $
 * @author Rick van Krevelen
 */
public interface Transporter extends Actor<Transporter.Destination>
{

	Destination status();

	default Place.ID position()
	{
		return status() == null ? null : status().getPlaceRef();
	}

	default boolean isMoving()
	{
		return status() != null && status().kind() == FactKind.REQUESTED;
	}

	default boolean isArrived()
	{
		return status() != null && status().kind() == FactKind.STATED;
	}

	/**
	 * {@link Directory} will retrieve or generate specified {@link Transporter}
	 */
	interface Directory
	{
		Transporter lookup( Actor.ID id );
	}

	interface Destination extends Fact, Locatable<Destination>
	{

	}
}
