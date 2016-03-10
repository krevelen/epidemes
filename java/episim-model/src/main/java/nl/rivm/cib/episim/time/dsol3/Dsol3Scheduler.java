package nl.rivm.cib.episim.time.dsol3;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.NavigableMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

import javax.measure.Measurable;
import javax.naming.NamingException;

import org.apache.logging.log4j.Logger;

import io.coala.dsol3.DsolTime;
import io.coala.exception.x.ExceptionBuilder;
import io.coala.log.LogUtil;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Timed.Scheduler;
import nl.tudelft.simulation.dsol.ModelInterface;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Dsol3Scheduler}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Dsol3Scheduler implements Scheduler
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( Dsol3Scheduler.class );

	private Instant last = null;

	/** the time */
	private final Subject<Instant, Instant> time = PublishSubject.create();

	/** the listeners */
	private final NavigableMap<Instant, Subject<Instant, Instant>> listeners = new ConcurrentSkipListMap<>();

	/** the scheduler */
	private final DEVSSimulator<Measurable<?>, BigDecimal, DsolTime> scheduler;

	/**
	 * {@link Dsol3Scheduler} constructor
	 * 
	 * @param threadName
	 */
	@SuppressWarnings( { "unchecked", "serial", "rawtypes" } )
	public Dsol3Scheduler( final String id, final Instant startTime,
		final Instant endTime, final Consumer<Scheduler> onInitialize )
	{
		this.scheduler = DsolTime.createDEVSSimulator( DEVSSimulator.class );
		try
		{
			final DsolTime start = DsolTime.valueOf( startTime );

			final ModelInterface model = new ModelInterface()
			{
				@Override
				public void constructModel( final SimulatorInterface simulator )
					throws RemoteException, SimRuntimeException
				{
					// schedule first event to rename the worker thread
					((DEVSSimulatorInterface<Measurable<?>, BigDecimal, DsolTime>) simulator)
							.scheduleEvent( new SimEvent<DsolTime>( start,
									simulator, new Runnable()
									{
										@Override
										public void run()
										{
											Thread.currentThread()
													.setName( id );
										}
									}, "run", null ) );

					// trigger onInitialize function
					onInitialize.accept( Dsol3Scheduler.this );
				}

				@Override
				public SimulatorInterface getSimulator()
				{
					return scheduler;
				}
			};

			// initialize the simulator
			final BigDecimal warmup = BigDecimal.ZERO;
			final BigDecimal length = endTime.subtract( startTime ).unwrap()
					.to( startTime.unwrap().getUnit() ).getValue();
			this.scheduler.initialize( DsolTime.createReplication( id, start,
					warmup, length, model ), ReplicationMode.TERMINATING );

			// observe time changes
			this.scheduler.addListener( ( final EventInterface event ) ->
			{
				final Instant t = ((DsolTime) event.getContent()).toInstant();
				if( t.equals( this.last ) )
				{
					LOG.trace( "Skipping t={}", t );
					return;
				}
				this.last = t;
				synchronized( this.listeners )
				{
					final Subject<Instant, Instant> timeProxy = this.listeners
							.remove( t );
					if( timeProxy != null )
					{
						timeProxy.onNext( t );
						timeProxy.onCompleted();
					}
					this.time.onNext( t );
				}
			}, SimulatorInterface.TIME_CHANGED_EVENT );

			// observe simulation completed
			this.scheduler.addListener( ( final EventInterface event ) ->
			{
				synchronized( this.listeners )
				{
					this.listeners.values().removeIf( timeProxy ->
					{
						timeProxy.onCompleted();
						return true;
					} );
					this.time.onCompleted();
				}
			}, SimulatorInterface.END_OF_REPLICATION_EVENT );
		} catch( final RemoteException | SimRuntimeException
				| NamingException e )
		{
			this.time.onError( e );
			throw ExceptionBuilder.unchecked( e, "Problem creating scheduler" )
					.build();
		}
	}

	@Override
	public Instant now()
	{
		return this.scheduler.getSimulatorTime().toInstant();
	}

	@Override
	public void resume()
	{
		LOG.trace( "resuming, t={}, #events={}", now(),
				this.scheduler.getEventList().size() );
		try
		{
			this.scheduler.start();
		} catch( final SimRuntimeException e )
		{
			this.time.onError( e );
		}
	}

	@Override
	public Observable<Instant> time()
	{
		return this.time.asObservable();
	}

	@Override
	public Expectation schedule( final Instant when, final Callable<?> call )
	{
		synchronized( this.listeners )
		{
			this.listeners.computeIfAbsent( when, ( Instant t ) ->
			{
				// create proxy and schedule the actual invocation of "onNext"
				final Subject<Instant, Instant> result = PublishSubject
						.create();
				try
				{
					this.scheduler.scheduleEvent(
							new SimEvent<DsolTime>( DsolTime.valueOf( t ), this,
									result, "onNext", new Object[]
							{ t } ) );
				} catch( final Exception e )
				{
					this.time.onError( e );
				}
				return result;
			} );
			final Subscription sub = this.listeners.get( when )
					.subscribe( ( Instant t ) ->
					{
						try
						{
							call.call();
						} catch( Exception e )
						{
							this.time.onError( e );
						}
					} );
			return Expectation.of( when, sub );
		}
	}

}