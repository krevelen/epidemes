/* $Id$
 * 
 * Part of ZonMW project no. 50-53000-98-156
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2016 RIVM National Institute for Health and Environment 
 */
package nl.rivm.cib.demo.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.config.YamlConfig;
import io.coala.data.DataLayer;
import io.coala.data.IndexPartition;
import io.coala.data.Table;
import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.Range;
import io.coala.time.Expectation;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.util.Compare;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoConfig;
import nl.rivm.cib.demo.DemoModel.Regional.SiteBroker;
import nl.rivm.cib.demo.DemoModel.Social.GatherFact;
import nl.rivm.cib.demo.DemoModel.Social.SocietyBroker;
import nl.rivm.cib.demo.DemoModel.Social.TimedGatherer;
import nl.rivm.cib.demo.Persons;
import nl.rivm.cib.demo.Persons.PersonTuple;
import nl.rivm.cib.demo.Sites;
import nl.rivm.cib.demo.Sites.SiteTuple;
import nl.rivm.cib.demo.Societies;
import nl.rivm.cib.demo.Societies.SocietyTuple;
import tec.uom.se.ComparableQuantity;

/**
 * {@link SocietyBrokerSimple}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class SocietyBrokerSimple implements SocietyBroker
{

	public interface SocietyConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

//		@Key( "motor-factory" )
//		@DefaultValue( "nl.rivm.cib.episim.model.SocialGatherer$Factory$SimpleBinding" )
//		Class<? extends SocialGatherer.Factory> socialGathererFactory();
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SocietyBrokerSimple.class );

	@InjectConfig
	private SocietyConfig config;

	@Inject
	private LocalBinder binder;

	@Inject
	private Scheduler scheduler;

	@Inject
	private DataLayer data;

	@Inject
	private SiteBroker siteBroker;

	private final PublishSubject<GatherFact> events = PublishSubject.create();

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + JsonUtil.stringify( this );
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public Observable<? extends GatherFact> events()
	{
		return this.events;
	}

	/** */
	private Table<SocietyTuple> societies;
	/** */
	private Table<PersonTuple> persons;
	/** */
	private IndexPartition capacityIndex;
	/** */
	private NavigableMap<String, TimedGatherer> gatherers;
	/** */
	private final Map<Object, List<Object>> societyMembers = new HashMap<>();
	/** */
	private final Map<Object, Object[]> ppSocieties = new HashMap<>();
	/** */
//	private ConditionalDistribution<EduCulture, String> cultureDist;

	/**
	 *
	 */
	@Override
	public SocietyBrokerSimple reset() throws Exception
	{
		final JsonNode gathererConfig = this.config.toJSON( GOALS_KEY );
		if( gathererConfig.size() == 0 )
			Thrower.throwNew( IllegalStateException::new,
					() -> "No gatherers configured: " + gathererConfig );

		LOG.debug( "{} creating gatherers: {}", gathererConfig );
		this.gatherers = this.binder.inject( TimedGatherer.SimpleFactory.class )
				.createAll( gathererConfig );

		this.societies = this.data.getTable( SocietyTuple.class );
		this.persons = this.data.getTable( PersonTuple.class );
		this.persons.onCreate( this::deferJoin, scheduler()::fail );
		this.persons.onDelete( this::abandonAll, scheduler()::fail );

		this.capacityIndex = new IndexPartition( this.societies,
				scheduler()::fail );
		this.capacityIndex.groupBy( Societies.Purpose.class );
		this.capacityIndex.groupBy( Societies.Capacity.class, Stream.of( 1 ) );
		// separate 'full' < 1 <= 'available'
//		this.capacityIndex.groupBy( Societies.CultureRef.class );

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	private Expectation pendingJoin = null;
	private List<PersonTuple> joinable = new ArrayList<>();

	// defer join() until home site is fully created
	private void deferJoin( final PersonTuple pp )
	{
		if( this.pendingJoin == null )
			this.pendingJoin = atOnce( this::handlePendingJoins );
		this.joinable.add( pp );
	}

	private void handlePendingJoins( final Instant now )
	{
		this.pendingJoin = null;
		if( now.isZero() ) LOG.info( "Initializing societies..." );
		final CountDownLatch latch = new CountDownLatch( 1 );
		final int n = this.joinable.size();
		final AtomicLong personCount = new AtomicLong(),
				lastCount = new AtomicLong(),
				t0 = new AtomicLong( System.currentTimeMillis() ),
				lastTime = new AtomicLong( t0.get() );
		new Thread( () ->
		{
			Thread.currentThread().setName( "socMon" );
			while( latch.getCount() > 0 )
			{
				try
				{
					latch.await( 1, TimeUnit.SECONDS );
				} catch( final InterruptedException e )
				{
					return;
				}
				if( latch.getCount() == 0 ) return;

				final long i = personCount.get(), i0 = lastCount.getAndSet( i ),
						t = System.currentTimeMillis(),
						ti = lastTime.getAndSet( t );
				LOG.info(
						"...joined {} of {} persons ({}%) in {}s, joining at {}/s...",
						i, n,
						DecimalUtil.toScale( DecimalUtil.divide( i * 100, n ),
								1 ),
						DecimalUtil.toScale(
								DecimalUtil.divide( t - t0.get(), 1000 ), 1 ),
						DecimalUtil.toScale(
								DecimalUtil.divide( (i - i0) * 1000, t - ti ),
								1 ) );
			}
		} ).start();
		final Map<String, Map<Object, AtomicLong>> roleSocCount = new HashMap<>();
		this.joinable.removeIf( pp ->
		{
			final Map<String, Object> roleMemberships = joinAll( pp );
			roleMemberships.forEach( ( role, socKey ) -> roleSocCount
					.computeIfAbsent( role, k -> new HashMap<>() )
					.computeIfAbsent( socKey, k -> new AtomicLong() )
					.incrementAndGet() );
			personCount.incrementAndGet();
			return true;
		} );
		latch.countDown();
		if( now.isZero() ) LOG
				.info( "Initialized {} societies with avg. size: {}",
						this.societyMembers.size(),
						String.join( "; ", roleSocCount.entrySet().stream()
								.map( e -> e.getKey() + " x "
										+ e.getValue().size()
										+ " x " + DecimalUtil.pretty(
												() -> ((double) e.getValue()
														.values().stream()
														.mapToLong(
																AtomicLong::get )
														.sum())
														/ e.getValue().size(),
												1 )
										+ "pp" )
								.toArray( String[]::new ) ) );
	}

	/**
	 * @param pp
	 */
	private Map<String, Object> joinAll( final PersonTuple pp )
	{
		final ComparableQuantity<Time> age = ageOf( pp );
		final Map<String, Object> roleMemberships = this.gatherers.entrySet()
				.stream().filter( e ->
				{
					final Range<ComparableQuantity<Time>> ageRange = e
							.getValue().memberAges()
							.map( q -> q.asType( Time.class ) );
					if( ageRange.gt( age ) )
					{
						// too young, schedule join for later
						final ComparableQuantity<Time> dtJoin = ageRange
								.lowerValue().subtract( age );
						if( Compare.lt( dtJoin, MEMBER_HORIZON ) )
							after( dtJoin ).call( t -> deferJoin( pp ) );
						return false;
					}
					// not too old?
					return !ageRange.lt( age );
				} ).collect( Collectors.toMap( Map.Entry::getKey, e ->
				{
					final SocietyTuple soc = findOrCreateLocalSociety(
							e.getValue(), pp );
					final Range<ComparableQuantity<Time>> ageRange = e
							.getValue().memberAges()
							.map( q -> q.asType( Time.class ) );
					final ComparableQuantity<Time> dtLeave = ageRange
							.upperInclusive()
									? ageRange.upperValue().subtract( age )
									: null;
					join( pp, soc, dtLeave );
					return soc.key();
				} ) );

		if( roleMemberships.isEmpty() )
			LOG.debug( "{} is not a member in any society",
					pp.pretty( Persons.PROPERTIES ) );
		else
			this.ppSocieties.compute( pp.key(),
					( k, socKeys ) -> roleMemberships.values()
							.toArray( new Object[roleMemberships.size()] ) );
		return roleMemberships;
	}

	private void join( final PersonTuple pp, final SocietyTuple soc,
		final ComparableQuantity<Time> dt )
	{
		final List<Object> members = this.societyMembers.get( soc.key() );
		final Object ppRef = pp.key();
		if( members.indexOf( pp.key() ) >= 0 )
		{
			LOG.warn( "Already member: {} in {}",
					pp.pretty( Persons.PROPERTIES ),
					soc.pretty( Societies.PROPERTIES ) );
			return;
		}
		members.add( pp.key() );
		soc.updateAndGet( Societies.MemberCount.class, n -> n + 1 );
		soc.updateAndGet( Societies.Capacity.class, n -> n - 1 );

		// if membership lasts beyond horizon, skip abandonment scheduling
		if( dt == null || Compare.gt( dt, MEMBER_HORIZON ) ) return;

		after( dt ).call( t ->
		{
			// abandon
			members.remove( ppRef );
			soc.updateAndGet( Societies.MemberCount.class, n -> n - 1 );
			soc.updateAndGet( Societies.Capacity.class, n -> n + 1 );
		} );
	}

	/**
	 * @param pp
	 */
	void abandonAll( final PersonTuple pp )
	{
		final Object[] socKeys = this.ppSocieties.remove( pp.key() );
		if( socKeys != null ) Arrays.stream( socKeys )
				.forEach( socKey -> this.societyMembers.compute( socKey,
						( k, members ) -> members != null
								&& members.remove( pp.key() )
								&& members.isEmpty() ? null : members ) );
	}

	/**
	 * @param siteKey
	 * @param dt
	 * @param participants
	 * @param onAdjourn
	 */
	void convene( final Object siteKey, final Quantity<Time> dt,
		final List<Object> participants, final Runnable onAdjourn )
	{
		this.events.onNext( new GatherFact().withSite( siteKey )
				.withDuration( dt ).withParticipants( participants ) );
	}

	/**
	 * @param gatherer
	 * @param person
	 * @return
	 */
	SocietyTuple findOrCreateLocalSociety( final TimedGatherer gatherer,
		final PersonTuple person )
	{
		// select societies with current capacity >= 1
		final List<?> socKeys = this.capacityIndex.keys( gatherer.id(), 1 );

		// if no societies yet, or all reached their capacity, create new
		if( socKeys.isEmpty() ) return createSociety( gatherer, person );

		// minimize society distance to person's home site
		final Entry<SocietyTuple, Double> nearestSite = this.siteBroker
				.selectNearest( person, IntStream.range( 0, socKeys.size() )
						.mapToObj( socKeys::get ).map( this.societies::select ),
						soc -> soc.get( Societies.SiteRef.class ) );

		// if nearest is still too far, create new
		final double kmNearest = Math.sqrt( nearestSite.getValue() ) * 111;
		if( kmNearest > gatherer.maxKm() )
			return createSociety( gatherer, person );

		return this.societies.select( socKeys.get( 0 ) );
	}

	SocietyTuple createSociety( final TimedGatherer gatherer,
		final PersonTuple pp )
	{
		final SiteTuple site;
		final Long capacity = gatherer.sizeLimitDist().draw();
		if( gatherer.id().equalsIgnoreCase( "junior" ) )
			site = this.siteBroker.assignLocalPrimarySchool( pp );
		else if( gatherer.id().equalsIgnoreCase( "career" ) )
			site = this.siteBroker.createLocalIndustry( pp );
		else
			site = this.siteBroker.createLocalSME( pp );

		final String name = gatherer.id() + "@"
				+ site.get( Sites.SiteName.class );
		final SocietyTuple soc = this.societies.insertValues( map -> map
				.set( Societies.EduCulture.class,
						site.get( Sites.EduCulture.class ) )
				.set( Societies.MemberCount.class, 0 )
				.set( Societies.Capacity.class, capacity.intValue() )
				.set( Societies.Purpose.class, gatherer.id() )
				.set( Societies.SiteRef.class, (Comparable<?>) site.key() )
				.set( Societies.SocietyName.class, name ) );

		final List<Object> members = new ArrayList<>();
		this.societyMembers.put( soc.key(), members );

		// initiate gatherings
		gatherer.summon().subscribe( dt ->
		{
			convene( site.key(), dt, members, () ->
			{
				LOG.trace( "Adjourned {}", name );
				// TODO fire event: convene/st, members returned home
			} );
//			LOG.trace( "Gathered {} members: {} (cap: {})", name,
//					members.size(), site.get( Sites.Capacity.class ) );
			// TODO fire event: convene/rq
		}, scheduler()::fail );
		return soc;
	}
}