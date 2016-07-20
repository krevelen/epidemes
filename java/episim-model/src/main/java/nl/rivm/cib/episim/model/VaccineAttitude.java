package nl.rivm.cib.episim.model;

import io.coala.time.Timed;

/**
 * {@link VaccineAttitude} reflects cumulative perceived safety/risk due to e.g.
 * opinions observed from media, pastor, family, doctors, etc.
 * 
 * @version $Id: f5d8e89c5356937430b94f625ecfe4fbd38401f8 $
 * @author Rick van Krevelen
 */
public interface VaccineAttitude extends Timed
{

	Vaccine getVaccine();

	/**
	 * @return {@code true} iff this attitude is willing to vaccinate given
	 *         {@link #getConfidence()}, {@link #getComplacency()}, and
	 *         {@link #getConvenience()}
	 */
	boolean isWilling();
}