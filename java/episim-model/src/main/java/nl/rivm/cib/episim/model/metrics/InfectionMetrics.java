package nl.rivm.cib.episim.model.metrics;

import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;

import io.coala.time.x.TimeSpan;
import nl.rivm.cib.episim.math.Indicator;
import nl.rivm.cib.episim.model.EpidemicCompartment;
import nl.rivm.cib.episim.model.EpidemicOccurrence;
import nl.rivm.cib.episim.model.Infection;
import nl.rivm.cib.episim.model.TransmissionEvent;
import nl.rivm.cib.episim.time.Timed;

/**
 * {@link InfectionMetrics} follows common
 * <a href="https://en.wikipedia.org/wiki/Epidemic_model">epidemic modeling
 * terminology</a> and approaches for <a href=
 * "https://en.wikipedia.org/wiki/Mathematical_modelling_of_infectious_disease">
 * mathematical modeling of infectious disease</a>
 * 
 * <table>
 * <tr>
 * <th>M</th>
 * <td>Passively immune infants</td>
 * </tr>
 * <tr>
 * <th>S</th>
 * <td>Susceptibles</td>
 * </tr>
 * <tr>
 * <th>E</th>
 * <td>Exposed individuals in the latent period</td>
 * </tr>
 * <tr>
 * <th>I</th>
 * <td>Infectives</td>
 * </tr>
 * <tr>
 * <th>R</th>
 * <td>Recovered with immunity</td>
 * </tr>
 * </table>
 * 
 * @version $Id: 9dade484b159c8240410d99c1b9d08c6f16e1340 $
 * @author Rick van Krevelen
 */
public interface InfectionMetrics extends Timed
{
	Infection getInfection();

	EpidemicOccurrence getOccurrence();

	Map<EpidemicCompartment, Indicator<Dimensionless>> getCompartmentAmounts();

	/** @return the number of {@link TransmissionEvent}s */
	Indicator<Dimensionless> getEffectiveContactsNumber();

	/**
	 * @return &beta; = Effective contact rate (=
	 *         {@link #getEffectiveContactsNumber()} per {@link TimeSpan})
	 */
	Indicator<Frequency> getEffectiveContactRate();

	// Indicator<Dimensionless> getVaccineCosts();

	// disease burden analysis

	// Indicator<Dimensionless> getMortality()
	// Indicator<Dimensionless> getMorbidity()
	// YLLs due to mortality
	// Indicator<Dimensionless> getYearsOfLifeLost()
	// YLDs due to morbidity
	// Indicator<Dimensionless> getYearsOfHealthyLifeLostDueToDisability()
}