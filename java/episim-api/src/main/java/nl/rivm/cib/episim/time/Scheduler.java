package nl.rivm.cib.episim.time;

import java.util.concurrent.Callable;

import io.coala.time.x.Instant;
import rx.Observable;

/**
 * {@link Scheduler}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Scheduler extends Timed
{

	@Override
	default Scheduler scheduler()
	{
		return this;
	}

	Observable<Instant> time();

	/** */
	void resume();

	/**
	 * @param when the {@link Instant} of execution
	 * @param call the {@link Callable}
	 * @return the occurrence {@link Expectation}, for optional cancellation
	 */
	Expectation schedule( Instant when, Callable<?> call );

}