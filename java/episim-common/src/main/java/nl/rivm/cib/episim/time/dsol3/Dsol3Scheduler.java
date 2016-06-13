package nl.rivm.cib.episim.time.dsol3;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Consumer;

import javax.measure.Measurable;
import javax.measure.quantity.Quantity;
import javax.naming.NamingException;

import org.apache.logging.log4j.Logger;

import io.coala.dsol3.DsolTime;
import io.coala.exception.ExceptionFactory;
import io.coala.log.LogUtil;
import io.coala.time.x.Duration;
import io.coala.time.x.Instant;
import nl.rivm.cib.episim.time.Expectation;
import nl.rivm.cib.episim.time.Scheduler;
import nl.rivm.cib.episim.util.Caller;
import nl.rivm.cib.episim.util.Caller.ThrowingConsumer;
import nl.tudelft.simulation.dsol.ModelInterface;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Dsol3Scheduler}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class Dsol3Scheduler<Q extends Quantity> implements Scheduler
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( Dsol3Scheduler.class );

	private Instant last = null;

	/** the time */
	private final Subject<Instant, Instant> time = PublishSubject.create();

	/** the listeners */
	private final NavigableMap<Instant, Subject<Instant, Instant>> listeners = new ConcurrentSkipListMap<>();

	/** the scheduler */
	private final DEVSSimulator<Measurable<Q>, BigDecimal, DsolTime<Q>> scheduler;

	/**
	 * {@link Dsol3Scheduler} constructor
	 * 
	 * @param threadName
	 */
	@SuppressWarnings( { "unchecked", "serial", "rawtypes" } )
	public Dsol3Scheduler( final String id, final Instant startTime,
		final Duration warmUp, final Duration length,
		final Consumer<Scheduler> onInitialize )
	{
		this.scheduler = DsolTime.createDEVSSimulator( DEVSSimulator.class );
		try
		{
			final DsolTime start = DsolTime.valueOf( startTime );
			LOG.trace( "jscience: {} => dsol: {}", startTime, start );

			final ModelInterface model = new ModelInterface()
			{
				@Override
				public void constructModel( final SimulatorInterface simulator )
					throws RemoteException, SimRuntimeException
				{
					// schedule first event to rename the worker thread
					((DEVSSimulatorInterface<Measurable<Q>, BigDecimal, DsolTime<Q>>) simulator)
							.scheduleEvent( new SimEvent<DsolTime<Q>>( start,
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
			this.scheduler.initialize(
					DsolTime.createReplication( id, start,
							warmUp.unwrap().to( startTime.unwrap().getUnit() )
									.getValue(),
							length.unwrap().to( startTime.unwrap().getUnit() )
									.getValue(),
							model ),
					ReplicationMode.TERMINATING );

			// observe time changes
			this.scheduler.addListener( event ->
			{
				final Instant t = ((DsolTime) event.getContent()).toInstant();
				if( t.equals( this.last ) ) return;

				this.last = t;
				synchronized( this.listeners )
				{
					this.listeners.computeIfPresent( t, ( t1, timeProxy ) ->
					{
						try
						{
							timeProxy.onNext( t );
						} catch( final Throwable e )
						{
//							timeProxy.onError( e );
//							this.time.onError( e );
						}
						timeProxy.onCompleted();
						return null; // i.e. remove
					} );
					this.time.onNext( t );
				}
			}, SimulatorInterface.TIME_CHANGED_EVENT );

			// observe simulation completed
			this.scheduler.addListener( event ->
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
			throw ExceptionFactory.createUnchecked( e,
					"Problem creating scheduler" );
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
		try
		{
			if( !this.scheduler.isRunning() )
			{
				LOG.trace( "resuming, t={}, #events={}", now(),
						this.scheduler.getEventList().size() );
				this.scheduler.start();
			}
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

	@SuppressWarnings( "unchecked" )
	@Override
	public Expectation schedule( final Instant when, final Runnable runner )
	{
		synchronized( this.listeners )
		{
			return Expectation.of( this, when,
					this.listeners.computeIfAbsent( when, t ->
					{
						// create proxy and schedule the actual invocation of "onNext"
						final Subject<Instant, Instant> result = PublishSubject
								.create();
						try
						{
							this.scheduler
									.scheduleEvent( new SimEvent<DsolTime<Q>>(
											(DsolTime<Q>) DsolTime.valueOf( t ),
											this, result, "onNext",
											new Object[]
											{ t } ) );
						} catch( final Exception e )
						{
							this.time.onError( e );
						}
						return result;
					} ).subscribe( t ->
					{
						try
						{
							runner.run();
						} catch( final Exception e )
						{
							this.time.onError( e );
						}
					} ) );
		}
	}

	public static <Q extends Quantity> Dsol3Scheduler<Q> of( final String id,
		final Instant start, final Duration duration,
		final Runnable modelInitializer )
	{
		return of( id, start, duration, Caller.of( modelInitializer )::ignore );
	}

//	public static <Q extends Quantity> Dsol3Scheduler<Q> of( final String id,
//		final Instant start, final Duration length,
//		final Consumer<Scheduler> modelInitializer )
//	{
//		return new Dsol3Scheduler<Q>( id, start,
//				Duration.of( 0, start.unwrap().getUnit() ), length,
//				modelInitializer );
//	}

	public static <Q extends Quantity> Dsol3Scheduler<Q> of( final String id,
		final Instant start, final Duration duration,
		final ThrowingConsumer<Scheduler, ?> modelInitializer )
	{
		return new Dsol3Scheduler<Q>( id, start,
				Duration.of( 0, start.unwrap().getUnit() ), duration,
				Caller.rethrow( modelInitializer ) );
	}

}