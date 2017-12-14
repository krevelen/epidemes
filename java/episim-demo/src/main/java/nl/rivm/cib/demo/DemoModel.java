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
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.eaio.uuid.UUID;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.LocalBinder;
import io.coala.data.Table.Property;
import io.coala.data.Table.Tuple;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.name.Identified;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.TimeUnits;
import io.coala.util.Compare;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import nl.rivm.cib.csv.DuoPrimarySchool.EduCol;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Persons.HouseholdPosition;
import nl.rivm.cib.demo.DemoModel.Persons.PersonTuple;
import nl.rivm.cib.demo.DemoModel.Sites.SiteTuple;
import nl.rivm.cib.demo.DemoModel.Social.Pedagogy;
import nl.rivm.cib.epidemes.cbs.json.CBSBirthRank;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.episim.model.SocialGatherer;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Compartment;
import nl.rivm.cib.episim.model.person.HouseholdComposition;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxHesitancy;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;
import tec.uom.se.ComparableQuantity;

/**
 * {@link DemoModel} implements the Epidemes enterprise ontology
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface DemoModel
{
	Long NA = -1L;

	Observable<DemoModel> atEach( String timing );

	Map<String, EnumMap<Compartment, Long>> exportRegionalSIRTotal();

	Map<String, EnumMap<Compartment, Long>> exportRegionalSIRDelta();

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
		class EduCulture extends AtomicReference<Pedagogy>
			implements Property<Pedagogy>
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
				EduCulture.class, HouseholdSeq.class, Complacency.class,
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

		/** person's sequence/serial number */
		class PersonSeq extends AtomicReference<Long> implements Property<Long>
		{
			// increases monotone at every new initiation/birth/immigration/...
		}

		/** reference of person's culture */
		@SuppressWarnings( "rawtypes" )
		class CultureRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		/** reference of person's household */
		class HouseholdRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

		/** person's current household rank */
		class HouseholdRank extends AtomicReference<HouseholdPosition>
			implements Property<HouseholdPosition>
		{
		}

		/** person's gender */
		class Male extends AtomicReference<Boolean> implements Property<Boolean>
		{
		}

		/** virtual absolute time of person's birth */
		class Birth extends AtomicReference<BigDecimal>
			implements Property<BigDecimal>
		{
		}

//		@SuppressWarnings( "rawtypes" )
//		class SiteRef extends AtomicReference<Comparable>
//			implements Property<Comparable>
//		{
//		}

		/** reference of person's current home region */
		@SuppressWarnings( "rawtypes" )
		class HomeRegionRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		/** reference of person's current home site */
		@SuppressWarnings( "rawtypes" )
		class HomeSiteRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		/** person's current epidemiological compartment for some pathogen */
		class PathogenCompartment extends AtomicReference<MSEIRS.Compartment>
			implements Property<MSEIRS.Compartment>
		{
		}

		/** person's current remaining 'resistance' against some pathogen */
		class PathogenResistance extends AtomicReference<Double>
			implements Property<Double>
		{
		}

		/**
		 * person's current vaccination compliance (bitwise, see
		 * https://stackoverflow.com/a/31921748)
		 */
		class VaxCompliance extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				// list frequently accessed fields first
				PathogenCompartment.class, HouseholdRef.class,
				HouseholdRank.class, Birth.class, HomeRegionRef.class,
				HomeSiteRef.class, PathogenResistance.class, Male.class,
				CultureRef.class, VaxCompliance.class, PersonSeq.class );

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

	@SuppressWarnings( "serial" )
	interface Regions
	{
		class RegionName extends AtomicReference<String>
			implements Property<String>
		{

		}

		class ParentRef extends AtomicReference<Object>
			implements Property<Object>
		{
		}

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

		enum BuiltFunction
		{
			RESIDENCE, LARGE_ENTERPRISE, SMALL_ENTERPRISE, PRIMARY_EDUCATION,;
		}

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

		class SiteFunction extends AtomicReference<BuiltFunction>
			implements Property<BuiltFunction>
		{
		}

		class EduCulture extends AtomicReference<Pedagogy>
			implements Property<Pedagogy>
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
				Pressure.class, Occupancy.class, SiteName.class,
				SiteFunction.class, EduCulture.class, RegionRef.class,
				Latitude.class, Longitude.class, Capacity.class );

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

		class EduCulture extends AtomicReference<Pedagogy>
			implements Property<Pedagogy>
		{
		}

		@SuppressWarnings( "rawtypes" )
		class SiteRef extends AtomicReference<Comparable>
			implements Property<Comparable>
		{
		}

		class Capacity extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		class MemberCount extends AtomicReference<Integer>
			implements Property<Integer>
		{
		}

		@SuppressWarnings( "rawtypes" )
		List<Class<? extends Property>> PROPERTIES = Arrays.asList(
				EduCulture.class, MemberCount.class, Purpose.class,
				Capacity.class, SocietyName.class, SiteRef.class );

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

		default ComparableQuantity<Time> ageOf( final PersonTuple pp )
		{
			return QuantityUtil.valueOf(
					now().decimal().subtract( pp.get( Persons.Birth.class ) ),
					scheduler().timeUnit().asType( Time.class ) );
		}
	}

	interface Social
	{
		// social/mental peer pressure networks, dynamics

		enum Pedagogy
		{
			/**
			 * Protestant (Luther), Gereformeerd/Vrijgemaakt/Evangelistisch
			 * (Calvin)
			 */
			REFORMED,

			/** Antroposofisch (Steiner/Waldorf) */
			ALTERNATIVE,

			/** speciaal onderwijs (special needs) */
			SPECIAL,

			/** public, catholic, islamic, mixed */
			OTHERS,

			/** unknown/all */
//			ALL,

			;

			private static final Map<String, Pedagogy> DUO_CACHE = new HashMap<>();

			public static Pedagogy
				resolveDuo( final EnumMap<EduCol, JsonNode> school )
			{
				final String type = school.get( EduCol.PO_SOORT ).asText();
				final String denom = school.get( EduCol.DENOMINATIE ).asText();
				final String key = type + denom;
				final Pedagogy result = DUO_CACHE.computeIfAbsent( key, k ->
				{
					if( //denom.startsWith( "Prot" ) || // 23.1%
					denom.startsWith( "Geref" ) // 1.2%
							|| denom.startsWith( "Evan" ) // 0.1%
					) return REFORMED;

					if( denom.startsWith( "Antro" ) ) // 0.9%
						return ALTERNATIVE;

					if( type.startsWith( "S" ) || type.contains( "s" ) )
						return SPECIAL;

					return OTHERS;
				} );
//				System.err.println( type + " :: " + denom + " -> " + result );
				return result;
			}
		}

		interface TimedGatherer extends SocialGatherer
		{
			String TYPE_KEY = "motor";

			String SITE_SCOPE_KEY = "site-scope-km";

			class Localized extends SocialGatherer.SimpleGatherer
				implements TimedGatherer
			{
				// call site broker to locate a specific site type 

				@Override
				public double maxKm()
				{
					return fromConfig( SITE_SCOPE_KEY, 100d );
				}

			}

			@Singleton
			class SimpleFactory implements SocialGatherer.Factory<TimedGatherer>
			{
				@Inject
				private LocalBinder binder;

				@Override
				public TimedGatherer create( final String name,
					final ObjectNode config )
					throws ClassNotFoundException, ParseException
				{
					final Class<? extends TimedGatherer> type = config
							.has( TYPE_KEY )
									? Class
											.forName(
													config.get( TYPE_KEY )
															.textValue() )
											.asSubclass( TimedGatherer.class )
									: Localized.class;
					return this.binder.inject( type,
							config.put( Identified.ID_JSON_PROPERTY, name ) );
				}
			}

			/**
			 * @return
			 */
			double maxKm();
		}

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
			String GOALS_KEY = "society-goals";

			// TODO from config, or auto-adjust to meet scenario run length
			ComparableQuantity<Time> MEMBER_HORIZON = QuantityUtil.valueOf( 4,
					TimeUnits.YEAR );

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

			String ATTRACTORS_KEY = "peer-attractors";
		}

	}

	interface Medical
	{

		// transmission, intervention, information, decisions, vaccination

		class EpidemicFact extends EpiFact
		{
			public PersonTuple pp = null;
//			public MSEIRS.Transition transition = null;
			public Map<MSEIRS.Compartment, Integer> sirDelta = Collections
					.emptyMap();

			public EpidemicFact withPerson( final PersonTuple pp )
			{
				this.pp = pp;
				return this;
			}

//			public EpidemicFact
//				withTransition( final MSEIRS.Transition transition )
//			{
//				this.transition = transition;
//				return this;
//			}

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

			// TODO from config;
			ComparableQuantity<Time> VAX_HORIZON = QuantityUtil.valueOf( 3,
					TimeUnits.DAYS );
		}

		interface VaxRegimen
		{
			boolean isCompliant( int vaxStatus );

			/** @return the NIP (default) schedule's next dose */
			VaxDose nextRegular( int vaxStatus, Quantity<Time> age );

			/** @return the alternative (outbreak, ZRA) schedule's next dose */
			VaxDose nextSpecial( int vaxStatus, Quantity<Time> age );

			Range<ComparableQuantity<Time>> decisionAgeRange();
		}

		interface VaxDose
		{
			VaxRegimen regimen();

			int bit();

			/** @return {@code true} iff this dose bit is 1 in given status */
			default boolean isFlippedOn( final int vaxStatus )
			{
				return !isFlippedOff( vaxStatus );
			}

			default boolean isFlippedOff( final int vaxStatus )
			{
				return (vaxStatus & bit()) == 0;
			}

			/** @return the status value after setting bit for this dose to 1 */
			default int flippedOn( final int vaxStatus )
			{
				return vaxStatus | bit();
			}

			default int flippedOff( final int vaxStatus )
			{
				return vaxStatus & ~bit();
			}

			Range<ComparableQuantity<Time>> ageRangeNormal();

			Range<ComparableQuantity<Time>> ageRangeSpecial();
		}

		@FunctionalInterface
		interface VaxAcceptanceEvaluator
			extends BiPredicate<HouseholdTuple, VaxOccasion>
		{

			/**
			 * applied {@link VaxHesitancy#minimumConvenience} and
			 * {@link VaxHesitancy#averageBarrier}
			 */
			VaxAcceptanceEvaluator MIN_CONVENIENCE_GE_AVG_ATTITUDE = ( hh,
				occasion ) ->
			{
				final BigDecimal confidence = hh
						.get( Households.Confidence.class ),
						complacency = hh.get( Households.Complacency.class );

				// no occasion, just return general attitude
				if( occasion == null )
					return Compare.gt( confidence, complacency );

				final BigDecimal convenience = VaxHesitancy
						.minimumConvenience( occasion );
				final BigDecimal barrier = VaxHesitancy
						.averageBarrier( confidence, complacency );
				return Compare.ge( convenience, barrier );
			};

			class Average implements VaxAcceptanceEvaluator
			{
				@Override
				public boolean test( final HouseholdTuple hh,
					final VaxOccasion occasion )
				{
					return MIN_CONVENIENCE_GE_AVG_ATTITUDE.test( hh, occasion );
				}
			}
		}
	}

	interface Regional
	{

		interface SiteBroker extends EpiActor
		{
			String SCOPES_KEY = "site-scopes";

			// FTE limit for zip6's inclusion as small-medium vs corporate zone
			int ZIP6_SME_FTE_LIMIT = 50;

			/**
			 * @return this {@link SiteBroker} after reset its distributions and
			 *         event handlers, e.g. for auto-assigning households and
			 *         persons to their residential sites and regions
			 */
			@Override
			SiteBroker reset() throws Exception;

			/**
			 * @param pp the target (particular person)
			 * @return the primary school site, pedagogy picked if not yet set
			 */
			SiteTuple assignLocalPrimarySchool( PersonTuple pp );

			/**
			 * @param pp the target (particular person)
			 * @return the small/medium enterprise site, in zip6 zone with FTE's
			 *         < {@link #ZIP6_SME_FTE_LIMIT}
			 */
			// TODO within various region types/ranges?
			SiteTuple createLocalSME( PersonTuple pp );

			/**
			 * @param pp the target (particular person)
			 * @return the small/medium enterprise site, in zip6 zone with FTE's
			 *         >= {@link #ZIP6_SME_FTE_LIMIT}
			 */
			SiteTuple createLocalIndustry( PersonTuple pp );

			/**
			 * @param pp the target (particular person)
			 * @param options the options to minimize, of some type {@link T}
			 * @param optionSiteKeyMapper maps options to their site reference
			 * @return key-value pair of the nearest option and rough distance
			 *         (squared angular degrees in the WGS84 coordinate system)
			 */
			<T> Entry<T, Double> selectNearest( PersonTuple pp,
				Stream<T> options, Function<T, Object> optionSiteKeyMapper );
		}
	}

	/**
	 * {@link Demical} provides mixing and (in-/epi-/pan-)demic transmission
	 */
	interface Demical
	{

		interface PersonBroker extends EpiActor
		{
			@Override
			PersonBroker reset() throws Exception;

			@Override
			Observable<? extends DemicFact> events();

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
//					HouseholdRef relocatedHHRef;
			}

			class Immigration extends DemicFact
			{
//					HouseholdRef immigratedHHRef;
			}

			class Emigration extends DemicFact
			{
//					HouseholdRef emigratedHHRef;
			}
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
	}
}