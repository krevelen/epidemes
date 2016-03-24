package nl.rivm.cib.episim.model;

import javax.measure.quantity.Dimensionless;

import nl.rivm.cib.episim.math.Indicator;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link VaccineAttitude} reflects perceived safety/risk due to e.g. opinions
 * observed from media, pastor, family, doctors, etc.
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface VaccineAttitude extends Timed
{

	Vaccine getVaccine();

	/**
	 * @return the (lack in) fear of side-effects
	 */
	Belief getConfidence();

	/**
	 * @return the (lack in) fear of disease
	 */
	Belief getComplacency();

	/**
	 * @return the (lack in) opportunity
	 */
	Belief getConvenience();

	/**
	 * TODO logarithmic belief combination?
	 * 
	 * @return the willingness as combined {@link Belief} of
	 *         {@link #getConfidence()}, {@link #getComplacency()}, and
	 *         {@link #getConvenience()}
	 */
	Belief getWillingness();

	interface Belief
	{
		Indicator<Dimensionless> evaluate();
	}
}