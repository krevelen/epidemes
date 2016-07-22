package nl.rivm.cib.episim.model.scenario;

import io.coala.time.Proactive;

/**
 * {@link PopulationGenerator}
 * 
 * @version $Id: bcec9251efbae6d083213e672391a360753793d8 $
 * @author Rick van Krevelen
 */
public interface PopulationGenerator extends Proactive
{

	// TODO apply yearly birth rates (per mother age category?)

	// TODO apply yearly survival rates (per age category?)

}
