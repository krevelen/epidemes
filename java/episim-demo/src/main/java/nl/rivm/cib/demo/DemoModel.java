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
package nl.rivm.cib.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.eaio.uuid.UUID;

import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.QuantityUtil;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.TimeUnits;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.epidemes.cbs.json.CBSBirthRank;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.person.HouseholdComposition;

/**
 * {@link DemoModel} implements the Epidemes enterprise ontology
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface DemoModel
{
	Long NA = -1L;

	enum HouseholdPosition
	{
		REFERENT, PARTNER, CHILD1, CHILD2, CHILD3, CHILDMORE;

		public boolean isAdult()
		{
			return ordinal() < 2;
		}

		public static HouseholdPosition ofChildIndex( final int rank )
		{
			return rank < 3 ? values()[2 + rank] : CHILDMORE;
		}

		/**
		 * @param ppPos
		 * @return
		 */
		public HouseholdPosition shift( final HouseholdPosition missing )
		{
			return missing == REFERENT ? (this == PARTNER ? REFERENT : this)
					: (!isAdult() && ordinal() > missing.ordinal()
							? values()[ordinal() - 1] : this);
		}
	}

	interface Cultures
	{
//		@SuppressWarnings( "serial" )
//		class CultureSeq extends AtomicReference<Object>
//			implements Property<Object>
//		{
//			// track cultures through time (index key is data source dependent)
//		}

		@SuppressWarnings( "serial" )
		class NormativeAttitude extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

		List<Class<?>> PROPERTIES = Arrays.asList(
//				Cultures.CultureSeq.class,
				Cultures.NormativeAttitude.class );
	}

	@SuppressWarnings( "serial" )
	interface Households
	{
		BigDecimal NO_MOM = BigDecimal.TEN.pow( 6 );

		class HouseholdSeq extends AtomicReference<Long>
			implements Property<Long>
		{
			// track households through time (index key is data source dependent)
		}

		@SuppressWarnings( "rawtypes" )
		class CultureRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		class Complacency extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

		class Confidence extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

		@SuppressWarnings( "rawtypes" )
		class HomeRegionRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		@SuppressWarnings( "rawtypes" )
		class HomeSiteRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		class Composition extends AtomicReference<CBSHousehold>
			implements Property<CBSHousehold>
		{
		}

		class KidRank extends AtomicReference<CBSBirthRank>
			implements Property<CBSBirthRank>
		{
		}

		class ReferentBirth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{

		}

		class MomBirth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{

		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				HomeRegionRef.class, HomeSiteRef.class, Composition.class,
				KidRank.class, ReferentBirth.class, MomBirth.class,
				CultureRef.class, HouseholdSeq.class, Complacency.class,
				Confidence.class );

		class HouseholdTuple extends Tuple
		{
			@Override
			@SuppressWarnings( "rawtypes" )
			public List<Class<? extends Property>> properties()
			{
				return PROPERTIES;
			}
		}
	}

	@SuppressWarnings( "serial" )
	interface Persons
	{
		class PersonSeq extends AtomicReference<Long> implements Property<Long>
		{
			// increases monotone at every new initiation/birth/immigration/...
		}

		class HouseholdRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		@SuppressWarnings( "rawtypes" )
		class CultureRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		class MemberPosition extends AtomicReference<HouseholdPosition>
			implements Property<HouseholdPosition>
		{
		}

		class Male extends AtomicReference<Boolean> implements Property<Boolean>
		{
		}

		class Birth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

//		@SuppressWarnings( "rawtypes" )
//		class SiteRef extends AtomicReference<Comparable>
//			implements Property<Comparable>
//		{
//		}

		@SuppressWarnings( "rawtypes" )
		class HomeRegionRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		@SuppressWarnings( "rawtypes" )
		class HomeSiteRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		class EpiCompartment extends AtomicReference<MSEIRS.Compartment>
			implements Property<MSEIRS.Compartment>
		{
		}

		class EpiResistance extends AtomicReference<Double>
			implements Property<Double>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				// list frequently accessed fields first
				EpiCompartment.class, HouseholdRef.class, MemberPosition.class,
				Birth.class, HomeRegionRef.class, HomeSiteRef.class,
//				SiteRef.class, 
				EpiResistance.class, Male.class, CultureRef.class,
				PersonSeq.class );

		class PersonTuple extends Tuple
		{
			@Override
			@SuppressWarnings( "rawtypes" )
			public List<Class<? extends Property>> properties()
			{
				return PROPERTIES;
			}
		}
	}

	interface Regions
	{
		@SuppressWarnings( "serial" )
		class RegionName extends AtomicReference<String>
			implements Property<String>
		{

		}

		@SuppressWarnings( "serial" )
		class ParentRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		@SuppressWarnings( "serial" )
		class Population extends AtomicReference<Long> implements Property<Long>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays
				.asList( RegionName.class, ParentRef.class, Population.class );

		class RegionTuple extends Tuple
		{
			@Override
			@SuppressWarnings( "rawtypes" )
			public List<Class<? extends Property>> properties()
			{
				return PROPERTIES;
			}
		}
	}

	@SuppressWarnings( "serial" )
	interface Sites
	{
		class SiteName extends AtomicReference<String>
			implements Property<String>
		{

		}

		class RegionRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		class Latitude extends AtomicReference<Double>
			implements Property<Double>
		{
		}

		class Longitude extends AtomicReference<Double>
			implements Property<Double>
		{
		}

		class Purpose extends AtomicReference<String>
			implements Property<String>
		{
		}

		class Capacity extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		class Occupancy extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		class Pressure extends AtomicReference<Double>
			implements Property<Double>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				Pressure.class, Occupancy.class, SiteName.class, Purpose.class,
				RegionRef.class, Latitude.class, Longitude.class,
				Capacity.class );

		class SiteTuple extends Tuple
		{
			@Override
			@SuppressWarnings( "rawtypes" )
			public List<Class<? extends Property>> properties()
			{
				return PROPERTIES;
			}

		}
	}

	@SuppressWarnings( "serial" )
	interface Societies
	{

		class SocietyName extends AtomicReference<String>
			implements Property<String>
		{
		}

		class Purpose extends AtomicReference<String>
			implements Property<String>
		{
		}

		@SuppressWarnings( "rawtypes" )
		class CultureRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		@SuppressWarnings( "rawtypes" )
		class SiteRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

//		@SuppressWarnings( "rawtypes" )
//		class RegionRef extends AtomicReference<Comparable>
//			implements Property<Comparable>
//		{
//		}
//
//		class SiteLat extends AtomicReference<Double>
//			implements Property<Double>
//		{
//		}
//
//		class SiteLon extends AtomicReference<Double>
//			implements Property<Double>
//		{
//		}

		class MemberCapacity extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				CultureRef.class, //SiteLat.class, SiteLon.class,
				Purpose.class, MemberCapacity.class, SocietyName.class,
//				RegionRef.class, 
				SiteRef.class );

		class SocietyTuple extends Tuple
		{
			@Override
			@SuppressWarnings( "rawtypes" )
			public List<Class<? extends Property>> properties()
			{
				return PROPERTIES;
			}
		}
	}

	class EpiFact
	{
		Instant time;
	}

	interface EpiActor extends Proactive
	{
		EpiActor reset() throws Exception;

		Observable<? extends EpiFact> events();

		default LocalDate dt()
		{
			return dt( now() );
		}

		default LocalDate dt( final Instant t )
		{
			return t.toJava8( scheduler().offset().toLocalDate() );
		}

		default Pretty prettyDate( final Instant t )
		{
			return Pretty.of( () ->
			{
				final ZonedDateTime zdt = scheduler().offset().plus(
						t.to( TimeUnits.MINUTE ).value().longValue(),
						ChronoUnit.MINUTES );
				return QuantityUtil.toScale( t.toQuantity( TimeUnits.DAYS ), 1 )
						+ ";" + zdt.toLocalDateTime() + ";"
						+ zdt.format( DateTimeFormatter.ISO_WEEK_DATE );
			} );
		}

//		default LocalDate dt()
//		{
//			// FIXME fix daylight savings adjustment, seems to adjust the wrong way
//			return now().equals( this.dtInstant ) ? this.dtCache
//					: (this.dtCache = (this.dtInstant = now())
//							.toJava8( scheduler().offset().toLocalDate() ));
//		}

//		default void logError( final Throwable e )
//		{
//			LogUtil.getLogger( EpiActor.class ).error( "Problem", e );
//		}
	}

//	interface Cultural
//	{
//		// group-related variance/subcultures, in social and medical behaviors 
//
//		interface Person extends EpiActor
//		{
//			// accepts all requests...
//		}
//	}

	interface Cultural
	{
		// social/mental peer pressure networks, dynamics

		class GatherFact extends EpiFact
		{
			public Object siteRef = NA;
			public Quantity<Time> duration = null;
			public List<Object> participants = Collections.emptyList();

			public GatherFact withSite( final Object siteRef )
			{
				this.siteRef = siteRef;
				return this;
			}

			public GatherFact withDuration( final Quantity<Time> duration )
			{
				this.duration = duration;
				return this;
			}

			public GatherFact
				withParticipants( final List<Object> participants )
			{
				this.participants = participants;
				return this;
			}
		}

		/** organizes meetings at a location */
		interface SocietyBroker extends EpiActor
		{
			@Override
			SocietyBroker reset() throws Exception;

			@Override
			Observable<? extends GatherFact> events();

//			Object join( LifePurpose purpose, PersonTuple person );

//			Map<String, Object> join( PersonTuple person );

//			void abandon( final PersonTuple person, final Object... socKeys );

		}

		interface PeerBroker extends EpiActor
		{
			// ingroup/peer-to-peer/media/authority advice
		}

	}

	interface Medical
	{

		class EpidemicFact extends EpiFact
		{
			public Object siteRef = NA;
			public MSEIRS.Transition transition = null;
			public Map<MSEIRS.Compartment, Integer> sirDelta = Collections
					.emptyMap();

			public EpidemicFact withSite( final Object siteRef )
			{
				this.siteRef = siteRef;
				return this;
			}

			public EpidemicFact
				withTransition( final MSEIRS.Transition transition )
			{
				this.transition = transition;
				return this;
			}

			public EpidemicFact withSIRDelta(
				final UnaryOperator<MapBuilder<MSEIRS.Compartment, Integer, ?>> mapBuilder )
			{
				this.sirDelta = mapBuilder.apply( MapBuilder
						.ordered( MSEIRS.Compartment.class, Integer.class ) )
						.build();
				return this;
			}
		}

		interface HealthBroker extends EpiActor
		{
			// immunization

			@Override
			Observable<? extends EpidemicFact> events();
		}
	}

	interface Epidemical
	{
		// TODO separate concerns: pathogen from location

		// transmission, intervention, information, decisions, vaccination

		interface SiteBroker extends EpiActor
		{
			@Override
			SiteBroker reset() throws Exception;

//			Object findHome( String regionRef );

			SiteTuple findNearby( String lifeRole, Object originSiteRef );

			/**
			 * @param homeSiteRef
			 * @return
			 */
			double[] positionOf( Object siteRef );

//			void populateHome( Object homeRef,
//				Map<MSEIRS.Compartment, List<Object>> sirDelta );
		}

	}

	/**
	 * {@link Demical} provides mixing and (in-/epi-/pan-)demic transmission
	 */
	interface Demical
	{

		interface Deme extends EpiActor
		{
			@Override
			Deme reset() throws Exception;

			@Override
			Observable<? extends DemicFact> events();
		}

		abstract class DemicFact extends EpiFact
		{
			public Map<? extends HouseholdComposition, Integer> hhDelta = Collections
					.emptyMap();
			public Map<HouseholdPosition, Integer> memberDelta = Collections
					.emptyMap();
			public UUID txRef = null; // population T30/rq
			public Object hhRef = NA; // inhabitant T30/init=T12/exec
			public Object siteRef = NA;

			public DemicFact withContext( final UUID t30rqRef,
				final Object hhRef, final Object siteRef )
			{
				this.txRef = t30rqRef;
				this.hhRef = hhRef;
				this.siteRef = siteRef;
				return this;
			}

			public DemicFact withHouseholdDelta(
				final Function<MapBuilder<CBSHousehold, Integer, ?>, Map<? extends HouseholdComposition, Integer>> mapBuilder )
			{
				this.hhDelta = mapBuilder.apply( MapBuilder
						.ordered( CBSHousehold.class, Integer.class ) );
				return this;
			}

			public DemicFact withMemberDelta(
				final Function<MapBuilder<HouseholdPosition, Integer, ?>, Map<HouseholdPosition, Integer>> mapBuilder )
			{
				this.memberDelta = mapBuilder.apply( MapBuilder
						.ordered( HouseholdPosition.class, Integer.class ) );
				return this;
			}
		}

		enum DemeEventType
		{
			/** */
			EXPANSION( Expansion.class ),
			/** */
			ELIMINATION( Elimination.class ),
			/** */
			UNION( Union.class ),
			/** */
			SEPARATION( Separation.class ),
			/** */
			DIVISION( Division.class ),
			/** */
			RELOCATION( Relocation.class ),
			/** */
			IMMIGRATION( Immigration.class ),
			/** */
			EMIGRATION( Emigration.class );

			private final Class<? extends DemicFact> type;

			private DemeEventType( final Class<? extends DemicFact> type )
			{
				this.type = type;
			}

			public DemicFact create()
				throws InstantiationException, IllegalAccessException
			{
				return this.type.newInstance();
			}
		}

		class Preparation extends DemicFact
		{
			// conception, expecting a baby (triggers new behavior)
		}

		class Expansion extends DemicFact
		{
			// child birth, adoption, possibly 0 representing miscarriage etc
		}

		class Elimination extends DemicFact
		{
			// person dies, possibly leaving orphans, abandoning household
		}

		class Union extends DemicFact
		{
			// households merge, e.g. living together or marriage
		}

		class Separation extends DemicFact
		{
			// household splits, e.g. couple divorces
		}

		class Division extends DemicFact
		{
			// household splits, e.g. child reaches adulthood
		}

		class Relocation extends DemicFact
		{
//				HouseholdRef relocatedHHRef;
		}

		class Immigration extends DemicFact
		{
//				HouseholdRef immigratedHHRef;
		}

		class Emigration extends DemicFact
		{
//				HouseholdRef emigratedHHRef;
		}
	}
}