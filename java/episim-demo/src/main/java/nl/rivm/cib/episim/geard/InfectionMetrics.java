package nl.rivm.cib.episim.geard;

import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Frequency;

import io.coala.time.Indicator;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.Signal;
import nl.rivm.cib.episim.model.disease.infection.EpidemicCompartment;
import nl.rivm.cib.episim.model.disease.infection.OutbreakScale;
import nl.rivm.cib.episim.model.disease.infection.Pathogen;
import nl.rivm.cib.episim.model.disease.infection.Transmission;

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
 * @version $Id: 7aa57c407ea079fca3643e1dc055a7880ace72ed $
 * @author Rick van Krevelen
 */
@Deprecated
public interface InfectionMetrics extends Proactive
{
	PopulationMetrics getPopulationMetrics();

	Pathogen getInfection();

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
	Quantity<Dimensionless> getPrevalence();

	/**
	 * @return the number of {@link Transmission}s, i.e. 'successful'
	 *         contact events
	 */
	Indicator<Dimensionless> getEffectiveContactsNumber();

	Map<EpidemicCompartment, Indicator<Dimensionless>> getCompartmentAmounts();

	/**
	 * @param unit the {@link Frequency} to calculate
	 * @return &beta; = Effective contact rate
	 */
	default Signal<Quantity<Frequency>>
		getEffectiveContactRate( final Unit<Frequency> unit )
	{
		return getEffectiveContactsNumber()
				.map( n -> n.divide( now().toQuantity() )
						.asType( Frequency.class ).to( unit ) );
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