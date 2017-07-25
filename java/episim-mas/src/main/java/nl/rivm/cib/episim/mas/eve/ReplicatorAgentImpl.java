package nl.rivm.cib.episim.mas.eve;

import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Time;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.almende.eve.agent.Agent;
import com.almende.eve.protocol.jsonrpc.annotation.Access;
import com.almende.eve.protocol.jsonrpc.annotation.AccessType;
import com.almende.eve.protocol.jsonrpc.annotation.Name;
import com.almende.eve.protocol.jsonrpc.annotation.Sender;
import com.almende.eve.state.TypedKey;
import com.eaio.uuid.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.dsol3.Dsol3Config;
import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.QuantityUtil;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import io.coala.util.MapBuilder;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import nl.rivm.cib.episim.mas.ReplicatorAgent;
import tec.uom.se.unit.Units;

/**
 * {@link ReplicatorAgentImpl}
 * 
 * @version $Id: e21d2c1535cc51676c70e74c6ce9374ac0ebdfe0 $
 * @author Rick van Krevelen
 */
public class ReplicatorAgentImpl extends Agent implements ReplicatorAgent
{

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( ReplicatorAgentImpl.class );

	/** local scheduler for the local model */
	private transient Scheduler scheduler = null;

	private transient Subject<StepRatio> pace = BehaviorSubject.create();

	private transient Subject<Instant> time = BehaviorSubject.create();

	// FIXME FEATURE_REQ have FileState#put(TypedKey<T>,T) accept non-Serializable yet JSONifiable POJOs
	private transient volatile StepRatio myPace = null;

	// FIXME BUG in FileState#get(TypedKey): "java.lang.Long cannot be cast to org.joda.time.DateTime"
	private transient volatile DateTime myOffset = null;

	private transient volatile Quantity<Time> myDuration = null;

	/** */
	private transient Map<UUID, Disposable> subscriptions = new HashMap<>();

	@Override
	public String getType()
	{
		return getClass().getSimpleName()
				+ " $Id: e21d2c1535cc51676c70e74c6ce9374ac0ebdfe0 $";
	}

	// FIXME FEATURE_REQ add #put(TypedKey<T>,T)
	@SuppressWarnings( "unchecked" )
	protected <T> T put( final TypedKey<T> key, final T value )
	{
		return (T) getState().put( key.getKey(), value );
	}

	// FIXME FEATURE_REQ add #putIfUnchanged(TypedKey<T>,T,Object)
	protected <T> boolean putIfUnchanged( final TypedKey<T> key,
		final T newValue, final Object oldValue )
	{
		return getState().putIfUnchanged( key.getKey(), newValue, oldValue );
	}

	@Access( AccessType.PUBLIC )
	public JsonNode myState()
	{
		final ObjectNode result = JsonUtil.getJOM().createObjectNode();
		result.put( TIME_UNIT_KEY, getState().get( MY_TIME_UNIT_KEY ) );
		result.putPOJO( OFFSET_KEY, getState().get( MY_OFFSET_KEY ) );
		result.putPOJO( UNTIL_KEY, getState().get( MY_UNTIL_KEY ) );
		result.putPOJO( TOPICS_KEY, getState().get( MY_TOPICS_KEY ) );
		result.putPOJO( STEP_RATIO_KEY, getState().get( MY_PACE_KEY ) );
		return result;
	}

	protected void reset()
	{
		final ReplicationConfig conf = ConfigFactory
				.create( ReplicationConfig.class );
		LOG.trace( "{} - Resetting scenario with config: {}", getId(), conf );

		final StepRatio zeroPace = StepRatio.of( BigDecimal.ZERO,
				BigDecimal.ZERO );
		final Unit<?> timeUnit = conf.timeUnit();
		final DateTime offset = conf.offset();
		final DateTime until = conf.until();

		put( MY_TIME_UNIT_KEY, timeUnit.toString() ); // required?
		put( MY_OFFSET_KEY, offset );
		put( MY_UNTIL_KEY, until );
		put( MY_TOPICS_KEY, conf.topics() );
		put( MY_PACE_KEY, zeroPace );

		this.myOffset = offset;
		this.myDuration = QuantityUtil.valueOf(
				BigDecimal.valueOf( until.getMillis() - offset.getMillis() ),
				TimeUnits.MILLIS );
		this.pace.onNext( zeroPace );
		this.myPace = zeroPace;
		final Dsol3Config config = Dsol3Config.of( MapBuilder
				.<String, Object>unordered().put( Dsol3Config.ID_KEY, getId() )
				.put( Dsol3Config.START_TIME_KEY, "0 " + timeUnit )
				.put( Dsol3Config.RUN_LENGTH_KEY, QuantityUtil
						.toBigDecimal( this.myDuration, timeUnit ).toString() )
				.build() );
		LOG.info( "Starting replication, config: {}", config.toYAML() );
		this.scheduler = config.create( s ->
		{
//					Scenario.of( Store.of( s, Collections.emptySet() ) );
			this.time.onNext( s.now() ); // emit start time
			s.time().subscribe( this.time ); // emit new time
			LOG.trace( "{} initialized, t={}", getId(), s.now().prettify( 1 ) );
		} );
	}

	@SuppressWarnings( "unchecked" )
	protected Quantity<Dimensionless> fraction( final Instant t )
	{
		return t.unwrap().divide( this.myDuration );
	}

	@Override
	protected void onBoot()
	{
		this.time.subscribe( t ->
		{
			LOG.trace( "{} - t = {}, {}", getId(), t.prettify( 4 ), QuantityUtil
					.toString( fraction( t ).to( Units.PERCENT ), 4 ) );
		}, e ->
		{
			LOG.warn( "{} - Problem in scheduler", getId(), e );
		}, () ->
		{
			LOG.trace( "{} - replication completed!", getId() );
			this.scheduler = null;
		} );

		reset();

		// FIXME BUG have Scheduler @Sender be owner (local:) getId() by default
//		schedule( "subscribe",
//				JsonUtil.getJOM().createObjectNode()
//						.put( "sender", "local:" + getId() )
//						.put( "topic", TIME_TOPIC ).putNull( "timing" ),
//				0 );
//		schedule( "subscribe", JsonUtil.getJOM().createObjectNode()
//				.put( "sender", "local:" + getId() ).put( "topic", PACE_TOPIC )
//				.put( "timing", "0 0 0 14 * ? " + DateTime.now().getYear() ),
//				0 );
	}

	@Override
	@Access( AccessType.PUBLIC )
	public void myPace( final @Name( STEP_RATIO_KEY ) StepRatio pace )
	{
		if( this.scheduler == null ) reset();

		if( this.myPace.virtualMS.signum() != 1 && pace.virtualMS.signum() == 1
				&& pace.actualMS.signum() == 1 )
		{
			LOG.trace( "{} - CMD: now paced {} -> {}", getId(), this.myPace,
					pace );
			this.myPace = pace;
			scheduleBlock();
			this.scheduler.run();
		} else
		{
			LOG.trace( "{} - CMD: now unpaced {} -> {}", getId(), this.myPace,
					pace );
			this.myPace = pace;
			if( pace.virtualMS.signum() == 1 ) this.scheduler.run();
		}
	}

	private void scheduleBlock()
	{
		if( this.myPace.virtualMS.signum() != 1 )
		{
//			LOG.trace( "Cancelling delays, pace: {}", this.myPace );
			return;
		}
		final Duration dt = Duration.of( this.myPace.virtualMS,
				TimeUnits.MILLIS );
		final DateTime until = new DateTime(
				currentTimeMillis() + this.myPace.actualMS.longValue() );
//		LOG.trace( "{} - Scheduled block at {} until {}", getId(), dt, until );
		this.scheduler.after( dt ).call( t -> blockUntil( until ) );
	}

	private void blockUntil( final DateTime untilMS )
	{
		long delayMS = untilMS.minus( currentTimeMillis() ).getMillis();
//		LOG.trace( "{} - Resume @{}, delay: {}ms", getId(), untilMS, delayMS );
		for( ; delayMS > 0; delayMS = untilMS.minus( currentTimeMillis() )
				.getMillis() )
			try
			{
//				LOG.trace( "{} - t={}, Sleeping for {}ms", getId(),
//						this.scheduler.now().prettify( 3 ), delayMS );
				Thread.sleep( delayMS ); // suspend the simulator thread
			} catch( final InterruptedException ignore )
			{
			}
		scheduleBlock();
	}

	// FIXME FEATURE_REQ have @Sender as URI
	@Access( AccessType.PUBLIC )
	public void notify( final @Sender String sender,
		final @Name( "topic" ) String topic,
		final @Name( "value" ) JsonNode value )
	{
		LOG.debug( "{} - Received '{}' notification from {}: {}", getId(),
				topic, sender, value );
	}

	protected void publish( final UUID subKey, final URI listener,
		final String topic, final JsonNode value )
	{
		final ObjectNode args = (ObjectNode) JsonUtil.getJOM()
				.createObjectNode().put( "topic", topic ).set( "value", value );
		try
		{
			this.getSender().get().call( listener, "notify", args );
			LOG.trace( "{} - Published '{}' to {}", getId(), topic, listener,
					value );
		} catch( final IOException e )
		{
			LOG.error( LogUtil.messageOf( "{} - Unsubscribing {} from '{}'",
					getId(), listener, topic ), e );
			unsubscribe( subKey );
		}
	}

	@SuppressWarnings( "unchecked" )
	protected void publishTime( final UUID subKey, final URI listener,
		final Instant t )
	{
		final StepRatio pace = this.myPace;
		publish( subKey, listener, TIME_TOPIC, JsonUtil.getJOM()
				.createObjectNode()
				.put( "time", QuantityUtil.toBigDecimal( t.unwrap() ) )
				.put( "fraction",
						QuantityUtil.floatValue( fraction( t ),
								QuantityUtil.PURE ) )
				.put( "remaining_ms", pace.virtualMS.equals( BigDecimal.ZERO )
						? -1L
						: this.myDuration
								.subtract( t.unwrap().to( TimeUnits.MILLIS ) )
								.divide( pace.virtualMS )
								.multiply( pace.actualMS ).getValue()
								.longValue() ) );
	}

	protected void publishPace( final UUID subKey, final URI listener,
		final StepRatio p )
	{
		publish( subKey, listener, PACE_TOPIC,
				JsonUtil.getJOM().valueToTree( p ) );
	}

	// FIXME FEATURE_REQ have @Sender as URI
	@Override
	@Access( AccessType.PUBLIC )
	public UUID subscribe( final @Name( "sender" ) URI listener,
		final @Name( "topic" ) String topic,
		final @Name( "timing" ) Timing timing )
	{
		// TODO apply timing to Eve's real-time scheduler, not the sim scheduler
		final Disposable sub;
		final UUID result = new UUID();
		if( topic.equalsIgnoreCase( TIME_TOPIC ) )
		{
			LOG.trace( "{} - Subscribing {} to '{}', timing: {}", getId(),
					listener, TIME_TOPIC, timing );
			if( timing != null ) // poll the time periodically
				sub = this.scheduler
						.atEach( timing.offset( this.myOffset ).stream() )
						.subscribe( t -> publishTime( result, listener, t ),
								e -> LOG.error( "Problem", e ) );
			else // subscribe to all time changes directly
				sub = this.time.subscribe(
						time -> publishTime( result, listener, time ),
						e -> LOG.error( "Problem", e ) );
		} else if( topic.equalsIgnoreCase( PACE_TOPIC ) )
		{
			LOG.trace( "{} - Subscribing {} to '{}', timing: {}", getId(),
					listener, PACE_TOPIC, timing );
			if( timing != null ) // poll the pace periodically
			{
				try
				{
					this.scheduler
							.atEach( timing.offset( this.myOffset ).iterate() )
							.subscribe( scheduler ->
							{
								publishPace( result, listener, this.myPace );
							} );
				} catch( final ParseException e )
				{
					Thrower.rethrowUnchecked( e );
				}
				sub = null;
			} else // subscribe to all pace changes directly
				sub = this.pace.subscribe( pace ->
				{
					publishPace( result, listener, pace );
				} );
		} else
			return null;
		this.subscriptions.put( result, sub );
		return result;
	}

	@Override
	@Access( AccessType.PUBLIC )
	public boolean unsubscribe( final @Name( "id" ) UUID subKey )
	{
		return null == this.subscriptions.computeIfPresent( subKey,
				( uuid, subscription ) ->
				{
					subscription.dispose();
					return null; // delete entry
				} );
	}

	// FIXME FEATURE REQ have TypedKey as non-abstract, with static of() factory 
	static class MyTypedKey<T> extends TypedKey<T>
	{
		public MyTypedKey( final String key )
		{
			super( key );
		}

		public static <T> MyTypedKey<T> of( final String key )
		{
			return new MyTypedKey<T>( key );
		}
	}

	static final TypedKey<String> MY_TIME_UNIT_KEY = MyTypedKey
			.of( TIME_UNIT_KEY );

	static final TypedKey<DateTime> MY_OFFSET_KEY = MyTypedKey.of( OFFSET_KEY );

	static final TypedKey<DateTime> MY_UNTIL_KEY = MyTypedKey.of( UNTIL_KEY );

	static final TypedKey<List<String>> MY_TOPICS_KEY = MyTypedKey
			.of( TOPICS_KEY );

	static final TypedKey<StepRatio> MY_PACE_KEY = MyTypedKey
			.of( STEP_RATIO_KEY );

}
