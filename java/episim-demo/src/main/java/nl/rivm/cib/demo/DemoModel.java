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

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import com.eaio.uuid.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.LocalBinder;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.name.Identified;
import io.coala.time.Instant;
import io.coala.time.Proactive;
import io.coala.time.TimeUnits;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import nl.rivm.cib.demo.Households.HouseholdTuple;
import nl.rivm.cib.demo.Persons.HouseholdPosition;
import nl.rivm.cib.demo.Persons.PersonTuple;
import nl.rivm.cib.demo.Sites.SiteTuple;
import nl.rivm.cib.epidemes.cbs.json.CBSHousehold;
import nl.rivm.cib.episim.model.SocialGatherer;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS;
import nl.rivm.cib.episim.model.disease.infection.MSEIRS.Compartment;
import nl.rivm.cib.episim.model.person.HouseholdComposition;
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