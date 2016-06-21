package nl.rivm.cib.episim.model.metrics;

import java.util.Map;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import io.coala.time.x.Instant;
import nl.rivm.cib.episim.model.EpidemicCompartment;
import nl.rivm.cib.episim.model.Infection;
import nl.rivm.cib.episim.model.TransmissionEvent;
import nl.rivm.cib.episim.time.Indicator;
import nl.rivm.cib.episim.time.Signal;
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
	PopulationMetrics getPopulationMetrics();

	Infection getInfection();

	OutbreakScale getOutbreakScale();

	/** the proportion of newly diagnosed cases in population at risk */
	Indicator<Dimensionless> getIncidence( Instant since );

	/**
	 * @return an {@link Amount} of (period) prevalence, i.e. the proportion of
	 *         current population that is or was infected between {@code since}
	 *         and {@link #now()}
	 */
	Indicator<Dimensionless> getPrevalence( Instant since );

	/**
	 * @return an {@link Amount} of (point) prevalence, i.e. the proportion of
	 *         the current population that is or was infected (ignores deaths)
	 */
	Amount<Dimensionless> getPrevalence();

	/**
	 * @return the number of {@link TransmissionEvent}s, i.e. 'successful'
	 *         contact events
	 */
	Indicator<Dimensionless> getEffectiveContactsNumber();

	Map<EpidemicCompartment, Indicator<Dimensionless>> getCompartmentAmounts();

	/**
	 * @param unit the {@link Frequency} to calculate
	 * @return &beta; = Effective contact rate
	 */
	default Signal<Amount<Frequency>>
		getEffectiveContactRate( final Unit<Frequency> unit )
	{
		return getEffectiveContactsNumber().transform( ( n ) ->
		{
			return n.divide( scheduler().now().toAmount() ).to( unit );
		} );
	}

	// Indicator<Dimensionless> getVaccineCosts();

	// disease burden analysis

	// Indicator<Dimensionless> getMortality() // vaccine-preventable
	// Indicator<Dimensionless> getMorbidity() // vaccine-preventable
	// YLLs due to mortality
	// Indicator<Dimensionless> getYearsOfLifeLost()
	// YLDs due to morbidity
	// Indicator<Dimensionless> getYearsOfHealthyLifeLostDueToDisability()

	// disease burden analysis

	// N: deaths per 10^n individuals
	// Indicator<Dimensionless> getTotalMortality()
	// I: incidence (new cases fraction of population per interval)
	// P: prevalence (total cases fraction of population per instant/interval/lifetime)
	// Indicator<Dimensionless> getTotalMorbidity()
	// YLLs = N x L (standard life expectancy at age of death in years)
	// Indicator<Dimensionless> getTotalYearsOfLifeLost()
	// YLDs = I x DW x L or P x DW (almost equivalent when not discounting age etc)
	// Indicator<Dimensionless> getTotalYearsOfHealthyLifeLostDueToDisability()

	// Indicator<Dimensionless> getTotalLifeYears();
	// DALYs = YLL + YLD
	// Indicator<Dimensionless> getTotalDisabilityAdjustedLifeYears();
}