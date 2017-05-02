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
package nl.rivm.cib.episim.pilot;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Area;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.config.YamlConfig;
import io.coala.enterprise.Actor;
import io.coala.enterprise.Fact;
import io.coala.enterprise.FactKind;
import io.coala.exception.Thrower;
import io.coala.function.ThrowingBiConsumer;
import io.coala.json.Attributed;
import io.coala.log.LogUtil;
import io.coala.math.LatLong;
import io.coala.math.Range;
import io.coala.math.WeightedValue;
import io.coala.name.Id;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.Scenario;
import io.coala.time.Scheduler;
import io.coala.time.TimeUnits;
import io.coala.time.Timing;
import io.coala.util.InputStreamConverter;
import io.reactivex.observables.GroupedObservable;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.epidemes.cbs.json.CBSPopulationDynamic;
import nl.rivm.cib.epidemes.cbs.json.CBSRegionType;
import nl.rivm.cib.epidemes.cbs.json.Cbs37230json;
import nl.rivm.cib.epidemes.cbs.json.Cbs71486json;
import nl.rivm.cib.epidemes.cbs.json.TimeUtil;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.infection.Pathogen;
import nl.rivm.cib.episim.model.locate.Geography;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.model.locate.travel.Vehicle;
import tec.uom.se.unit.Units;

/**
 * {@link OutbreakScenario}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@Singleton
public class OutbreakScenario implements Scenario
{

	public interface Config extends YamlConfig
	{
		@DefaultValue( "" + 1000 )
		int popSize();

		@DefaultValue( "[2012-01-01; +inf>" )
		String timeRange();

		@DefaultValue( "cbs/37713_2016JJ00_AllOrigins.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs37713();

		@DefaultValue( "cbs/71486ned-TS-2010-2016.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs71486();

		@DefaultValue( "cbs/37230ned_monthly_change_series.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream cbs37230();

//		@DefaultValue( "cbs/37975_2016JJ00.json" )
//		@ConverterClass( InputStreamConverter.class )
//		InputStream cbs37975();

		@DefaultValue( "MUNICIPAL" )
		CBSRegionType cbsRegionLevel();
	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( OutbreakScenario.class );

	@InjectConfig
	private transient Config config;

	@Inject
	private transient Scheduler scheduler;

	@Inject
	private transient Actor.Factory actors;

	@Inject
	private transient ProbabilityDistribution.Factory distFact;

//		@Inject
//		private LocalBinder binder;

	/** spatial scope of analysis */
	private transient CBSRegionType cbsRegionLevel = null;

	/** temporal scope of analysis */
	private transient Range<ZonedDateTime> timeRange = null;

	/** population initialization and immigration */
//	private transient ConditionalDistribution<Cbs37713json.Category, ZonedDateTime> genderOriginDist = null;

	// TODO birth, add 37201 (sex, mom age/count/status)

	// TODO deaths, add 83190ned (age, gender, position)

	// TODO immigration, add 37713 (gender, origin)

	/** national monthly demographic rates (births, deaths, migrations, ...) */
	private transient Map<CBSPopulationDynamic, ConditionalDistribution<Cbs37230json.Category, ZonedDateTime>> demogDelayDist = new EnumMap<>(
			CBSPopulationDynamic.class );

	/** regional monthly demographic places (births, deaths, migrations, ...) */
	private transient Map<CBSPopulationDynamic, ConditionalDistribution<Cbs37230json.Category, ZonedDateTime>> demogRegionDist = new EnumMap<>(
			CBSPopulationDynamic.class );

	/**
	 * household types and referent ages, e.g. based on empirical CBS 71486
	 * (composition) & 60036ned (age diff) & 37201 (mom age) data
	 */
	private transient ConditionalDistribution<Cbs71486json.Category, ZonedDateTime> hhTypeAgeDist = null;

	private void readMonthlyRates( final CBSPopulationDynamic metric,
		final BiConsumer<CBSPopulationDynamic, Map<ZonedDateTime, Collection<WeightedValue<Cbs37230json.Category>>>> national,
		final BiConsumer<CBSPopulationDynamic, Map<ZonedDateTime, Collection<WeightedValue<Cbs37230json.Category>>>> regional )
	{
		// TODO parse and group by all metrics in a single parsing run
		Cbs37230json.readAsync( this.config::cbs37230, metric, this.timeRange )
				.groupBy( wv -> wv.getValue().regionType() ).blockingForEach( (
					GroupedObservable<CBSRegionType, WeightedValue<Cbs37230json.Category>> g ) ->
				{
					if( g.getKey() == this.cbsRegionLevel )
						regional.accept( metric,
								g.toMultimap( wv -> wv.getValue().offset(),
										wv -> wv, () -> new TreeMap<>() )
										.blockingGet() );
					if( g.getKey() == CBSRegionType.COUNTRY ) // top level
						national.accept( metric,
								g.toMultimap( wv -> wv.getValue().offset(),
										wv -> wv, () -> new TreeMap<>() )
										.blockingGet() );
				} );
	}

	// TODO partnership/marriage rate from CBS 37890 (region, age, gender) & 60036ned (age diff) ?

	// TODO separation/divorce rate from CBS 37890 (region, age, gender) ?

	/**
	 * social network (household/opinions):
	 * <li>topology: households (marry/split, birth/death, migrate)
	 * <ul>
	 * <li>37230: #persons-births-deaths-migrations, per municipality / month
	 * '02-'17
	 * <li>71091: #splits: Scheiden; naar belangrijkste redenen, 2003
	 * <li>71488: #single-couple-married-kids-institute, per age 0:5:95+ /
	 * municipality / year '97-'16
	 * <li>37973: #single-couple-married-kids-institute, per
	 * ethnicity-generation / year '00-'16
	 * <li>37713: #persons, per ethnicity / age 0:5:95+ / municipality /
	 * annually '96-'16
	 * </ul>
	 * <li>pro/con, weight; parent, partner, sibling, work/classmate
	 * <li>contact network (locations): home, work, school, vehicle, shop,
	 * sports (mobility routines)
	 * <ul>
	 * <li>82249: Social contacts
	 * <li>81124: Mobility - motives
	 * <li>81125: Traffic participants
	 * <li>37856: Mobility vehicle possession
	 * </ul>
	 * <li>disease/vaccine
	 * <ul>
	 * <li>measles: SEIRS (Vink et al)
	 * <li>cost-effectiveness: per case
	 */
	@Override
	public void init() throws Exception
	{
		this.timeRange = Range
				.parse( this.config.timeRange(), LocalDate::parse )
				.map( dt -> dt.atStartOfDay( TimeUtil.NL_TZ ) );
		LOG.trace( "Init; range: {}", timeRange );
//			final Population<?> pop = this.binder.inject( Population.class );

		this.cbsRegionLevel = this.config.cbsRegionLevel();

		Arrays.stream(
				new CBSPopulationDynamic[]
		{ CBSPopulationDynamic.BIRTHS, CBSPopulationDynamic.DEATHS,
				CBSPopulationDynamic.IMMIGRATION,
				CBSPopulationDynamic.EMIGRATION } )
				.forEach( metric -> readMonthlyRates( metric, ( key,
					map ) -> this.demogDelayDist.put( key,
							ConditionalDistribution.of(
									this.distFact::createCategorical, map ) ),
						( key, map ) -> this.demogRegionDist.put( key,
								ConditionalDistribution.of(
										this.distFact::createCategorical, map ) ) ) );

		this.hhTypeAgeDist = ConditionalDistribution.of(
				this.distFact::createCategorical,
				Cbs71486json.readAsync( this.config::cbs71486, this.timeRange )
						.filter( wv -> wv.getValue()
								.regionType() == this.cbsRegionLevel )
						.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
								() -> new TreeMap<>() )
						.blockingGet() );

//		this.genderOriginDist = ConditionalDistribution.of(
//				this.distFact::createCategorical,
//				Cbs37713json.readAsync( this.config::cbs37713, this.timeRange )
//						.filter( wv -> wv.getValue()
//								.regionType() == this.cbsRegionLevel )
//						.toMultimap( wv -> wv.getValue().offset(), wv -> wv,
//								() -> new TreeMap<>() )
//						.blockingGet() );

//		Cbs37975json
//				.readAsync( this.config::cbs37975 )
//				.toList().blockingGet();

//		final Actor<Fact> home = this.actors.create( "home" );
		final ZonedDateTime dt = now().toJava8( this.timeRange.lowerValue() );

		LOG.trace( "next birth due {} @ {}",
				this.demogDelayDist.get( CBSPopulationDynamic.BIRTHS )
						.draw( dt ).timeDist( this.distFact::createExponential )
						.draw().to( TimeUnits.MINUTE ),
				this.demogRegionDist.get( CBSPopulationDynamic.BIRTHS )
						.draw( dt ).regionId() );
		for( int i = 1, n = 10; // this.config.popSize(); 
				i < n; )
		{
			final Cbs71486json.Category hhCat = this.hhTypeAgeDist.draw( dt );
			final Region.ID regRef = hhCat.regionId();
			final Quantity<Time> hhRefAge = hhCat
					.ageDist( this.distFact::createUniformContinuous ).draw();
			final CBSHousehold hh = hhCat
					.hhTypeDist( this.distFact::createCategorical ).draw();
			final Actor<Fact> rick = this.actors.create( i++ );
			LOG.trace(
					"New bsn: {}, reg: {}, age: {}, hh: {} (adults: {}, status: {}, kids: {})",
					rick.id().unwrap(), regRef, hhRefAge, hh,
					hh.adultCount(), (hh.couple()
							? (hh.registered() ? "cohab" : "duo") : "solo"),
					hh.childCount() );

//			final Category profile = this.genderOriginDist
//					.draw( genderDist.draw() );
//			rick.with( "male", profile.gender() );
//			rick.with( "age", profile
//					.ageDist( this.distFact::createUniformContinuous ).draw() );
//			rick.with( "regionRef", profile.regionId() );
//				rick.specialist( Redirection.Director.class )
//						// add Redirection/request handler
//						.emit( Actor.RQ_FILTER, ( role, rq ) ->
//						{
//							LOG.trace( "role {} rq {} routine {}", role, rq,
//									rq.getRoutine() );
//
//							if( Routine.STANDARD.equals( rq.getRoutine() ) )
//							{
//								// FIXME use factory to schedule this persons life
//
//								role.initiate( Plan.class, rick.id() ).commit();
//							}
//							// cancel prior engagements
//						} )
//						// self-initiate behavior redirection
//						.initiate( rick.id() ).with( Routine.STANDARD )
//						.commit();
//				atEach( Timing.of( "0 0 8 ? * MON-FRI" ).offset( offset )
//						.iterate(), t ->
//						{
//							LOG.trace( "rick now at t= {}",
//									t.prettify( offset ) );
//							rick.specialist( Activity.Activator.class )
//									.initiate( Visit.class, home.id(),
//											now().add( Duration.of( 10,
//													TimeUnits.HOURS ) ) );
//						} );
		}

		// TODO initiate Mixing: spaces/transports, mobility

		// initiate Opinion: hesitancy, interaction

		// initiate Infections: diseases/outbreaks, vaccine/interventions

		// virtual time added...
		after( Duration.of( 10, TimeUnits.HOURS ) )
				.call( t -> LOG.trace( "Virtual time is now: {}",
						t.prettify( timeRange.lowerValue() ) ) );

		LOG.info( "Initialized model" );
	}

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	/**
	 * {@link Measles}
	 * 
	 * <p>
	 * Disease (Vink et al. 2014) Serial Interval (latent + infectious), days
	 * Anderson and May 1991 (6) Vynnycky and White 2010 (8) <br>
	 * Influenza A 3–6 vs 2–4 <br>
	 * Measles 12–16 vs 7–16 <br>
	 * HPV 100-150?
	 */
	@Singleton
	public static class MeaslesFactory extends Actor.Factory.LocalCaching
	{
		// globally unique?
		private transient AtomicInteger mvTypeCounter = new AtomicInteger( 0 );

		public Pathogen createMeasles()
		{
			return super.create( Actor.ID
					.of( "MV-" + this.mvTypeCounter.incrementAndGet(), id() ) )
							.specialist( Pathogen.class );
		}

		/**
		 * {@link Measles} force of infection should result in a mean generation
		 * time (time between primary infecting secondary) of ca 11-12 days,
		 * with st.dev. 2-3 days, see Klinkenberg:2011:jtbi, secondary attack
		 * rate >90% in household/institutional contacts
		 * <p>
		 * for incubation and symptom times, see e.g.
		 * https://wwwnc.cdc.gov/travel/yellowbook/2016/infectious-diseases-related-to-travel/measles-rubeola
		 */
		public MeaslesFactory( final Scheduler scheduler,
			final ProbabilityDistribution.Factory distFact,
			final Condition.Factory conditionFactory )
		{
			super();

			// Pathogen.SimpleSEIR 

			// latent period (E->I) t+0, onset around t+14 but infectious already 4 days before
			distFact.createTriangular( 1, 14 - 4, 21 - 4 )//
					.toQuantities( TimeUnits.DAYS ).map( Duration::of );

			// infectious period (I->R): t+14-4, [onset-4,onset+4] 
			distFact.createTriangular( 0, 8, 10 ).toQuantities( TimeUnits.DAYS )
					.map( Duration::of );

			// wane period (R->S): infinite, forever immune
			distFact.createDeterministic( Duration.of( 100, TimeUnits.YEAR ) );

			// incubation period (E->C, sub->clinical symptoms)
			// : t+7-21 days, rash, fever, Koplik spots, ...
			// : t+9-12 days (https://www.wikiwand.com/en/Incubation_period)
			distFact.createTriangular( 7, 11, 21 )
					.toQuantities( TimeUnits.DAYS ).map( Duration::of );

			// symptom period (C->N, normal/asymptomatic)
			distFact.createTriangular( 4, 5, 7 ).toQuantities( TimeUnits.DAYS )
					.map( Duration::of );

//				force of infection (S->E): t=?, viral shedding -> respiratory/surface contact 
//				if( // TODO if  (infection pressure [=#contacts] > threshold) &&
//				conditionOf( person ).compartment().isSusceptible() )
		}
	}

	/**
	 * {@link Routine} categorizes the type of meeting, inspired by
	 * Zhang:2016:phd
	 */
	public static class Purpose extends Id.Ordinal<String>
	{
		/**
		 * career-related, including: (pre/elementary/high)school,
		 * college/university, employment/retirement
		 */
		public static final OutbreakScenario.Purpose OCCUPATIONAL = of(
				"OCCUPATIONAL" );

		/** social events, including: family visits, sports, leisure, ... */
		public static final OutbreakScenario.Purpose SOCIAL = of( "SOCIAL" );

		/** recreational events, including: holidays, vacation, ... */
		public static final OutbreakScenario.Purpose RECESS = of( "RECESS" );

		/** medical events, including: doctor visits, hospitalize, ... */
		public static final OutbreakScenario.Purpose MEDICAL = of( "MEDICAL" );

		/** spiritual events, including: church visit, pilgrimage, ... */
		public static final OutbreakScenario.Purpose SPRITUAL = of(
				"SPRITUAL" );

		public static OutbreakScenario.Purpose of( final String value )
		{
			return Util.of( value, new Purpose() );
		}

	}

	public interface MeetingTemplate
	{
		/** determines type of venue and participants */
		OutbreakScenario.Purpose purpose();

		/** (daily/weekly/...) recurrence pattern, e.g. "0 0 8 ? * MON-FRI" */
		Timing recurrence();

		/** interruption-completion time span (positive), e.g. "[1 h; 2 h]" */
		Range<Duration> duration();

		static OutbreakScenario.MeetingTemplate of( final String purpose,
			final String recurrence, final String duration )
		{
			return of( Purpose.of( purpose ), Timing.of( recurrence ),
					Range.parse( duration, Duration.class ) );
		}

		static OutbreakScenario.MeetingTemplate of(
			final OutbreakScenario.Purpose purpose, final Timing recurrence,
			final Range<Duration> duration )
		{
			return new MeetingTemplate()
			{
				@Override
				public OutbreakScenario.Purpose purpose()
				{
					return purpose;
				}

				@Override
				public Timing recurrence()
				{
					return recurrence;
				}

				@Override
				public Range<Duration> duration()
				{
					return duration;
				}
			};
		}
	}

	/**
	 * {@link Routine} categorizes the type of plans, inspired by Zhang:2016:phd
	 */
	public static class Routine extends Id.Ordinal<String>
	{
		/** day-care/school-child, working class hero, retiree */
		public static final OutbreakScenario.Routine STANDARD = of(
				"STANDARD" );

		// for special-care, adjusted workplaces, elderly homes, ...
		// TODO public static final Routine SPECIAL = of( "SPECIAL" );

		public static OutbreakScenario.Routine of( final String value )
		{
			return Util.of( value, new Routine() );
		}

		public interface Attributable<THIS> extends Attributed
		{
			void setRoutine( OutbreakScenario.Routine routine );

			OutbreakScenario.Routine getRoutine();

			@SuppressWarnings( "unchecked" )
			default THIS with( final OutbreakScenario.Routine routine )
			{
				setRoutine( routine );
				return (THIS) this;
			};
		}
	}

	/** {@link Redirection} switches behaviors, e.g. work &hArr; holiday */
	public interface Redirection
		extends Fact, Routine.Attributable<OutbreakScenario.Redirection>
	{

		/** {@link Director} handles {@link Redirection} requests */
		public interface Director extends Actor<OutbreakScenario.Redirection>,
			Routine.Attributable<Redirection.Director>
		{

			/**
			 * @param handler
			 * @return self
			 */
			default Redirection.Director onRequest(
				ThrowingBiConsumer<Redirection.Director, OutbreakScenario.Redirection, ?> handler )
			{
				emit( FactKind.REQUESTED ).subscribe( rq ->
				{
					try
					{
						handler.accept( this, rq );
					} catch( final Throwable e )
					{
						Thrower.rethrowUnchecked( e );
					}
				}, e -> LOG.error( "Problem", e ) );
				return this;
			}

		}
	}

	/**
	 * {@link Plan} provides a (weekly) routine or process, e.g. working_parent
	 */
	public interface Plan extends Fact
	{
		/** {@link Planner} handles {@link Plan} requests */
		interface Planner extends Actor<OutbreakScenario.Plan>
		{

		}
	}

	/** {@link Activity} triggers actions like movement or contact */
	public interface Activity extends Fact
	{
		/** {@link Activator} handles {@link Activity} requests */
		interface Activator extends Actor<OutbreakScenario.Activity>
		{

		}
	}

	@Singleton
	public static class MyDirectory
		implements Region.Directory, Place.Directory, Vehicle.Directory
	{
		/** the regions: geographic areas, for analytics on clustering, ... */
		private NavigableMap<Region.ID, Region> regions = new TreeMap<>();

		/**
		 * the places: geographic locations, to broker by type
		 * ({@link Purpose}), proximity ({@link LatLong})
		 */
		private NavigableMap<Place.ID, Place> places = new TreeMap<>();

		/** the spaces: rooms, vehicles */
		private NavigableMap<Actor.ID, Vehicle> vehicles = new TreeMap<>();

		/** RIVM National Institute for Public Health and the Environment */
		public LatLong RIVM_POSITION = LatLong.of( 52.1185272, 5.1868699,
				Units.DEGREE_ANGLE );

		@Override
		public Region lookup( final Region.ID id )
		{
			return this.regions.computeIfAbsent( id, key ->
			{
				final String name = null;// TODO lookup
				final Region.TypeID type = null;// TODO lookup
				final SortedMap<Instant, Region> parents = null;// TODO lookup
				final SortedMap<Instant, Region> children = null;// TODO lookup
				final Quantity<Area> surfaceArea = null;// TODO lookup

				return Region.of( id, name, type, parents, children,
						surfaceArea );
			} );
		}

		@Override
		public Place lookup( final Place.ID id )
		{
			return this.places.computeIfAbsent( id, key ->
			{
				final LatLong position = null;// TODO lookup
				final Region region = null;// TODO lookup
				final Geography[] geographies = null;// TODO lookup

				return Place.of( id, position, region, geographies );
			} );
		}

		@Inject
		private Actor.Factory actorFactory;

		@Override
		public Vehicle lookup( final Actor.ID id )
		{
			return this.vehicles.computeIfAbsent( id, key ->
			{
				return this.actorFactory.create( id )
						.specialist( Vehicle.class );
			} );
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public Number getRegionalValue( final Region.ID key )
	{
		LOG.trace( "Unknown region: {}", key );
		return this.distFact.getStream().nextInt( 200 );
	}
}