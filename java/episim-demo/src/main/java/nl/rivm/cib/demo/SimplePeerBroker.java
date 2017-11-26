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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.function.Function;

import javax.inject.Inject;
import javax.measure.Quantity;
import javax.measure.quantity.Time;

import org.apache.logging.log4j.Logger;

import io.coala.bind.InjectConfig;
import io.coala.bind.LocalBinder;
import io.coala.config.YamlConfig;
import io.coala.exception.Thrower;
import io.coala.json.JsonUtil;
import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.math.QuantityUtil;
import io.coala.math.Range;
import io.coala.random.ConditionalDistribution;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.ProbabilityDistribution.Factory;
import io.coala.random.ProbabilityDistribution.Parser;
import io.coala.random.PseudoRandom;
import io.coala.time.Instant;
import io.coala.time.Scheduler;
import io.coala.time.Timing;
import io.coala.util.InputStreamConverter;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import nl.rivm.cib.demo.DemoModel.Cultural.PeerBroker;
import nl.rivm.cib.demo.DemoModel.EpiFact;
import nl.rivm.cib.demo.DemoModel.Households.HouseholdTuple;
import nl.rivm.cib.demo.DemoModel.Persons;
import nl.rivm.cib.episim.model.vaccine.attitude.VaxOccasion;
import nl.rivm.cib.json.HesitancyProfileJson;
import nl.rivm.cib.json.HesitancyProfileJson.HesitancyDimension;
import nl.rivm.cib.json.RelationFrequencyJson;
import nl.rivm.cib.pilot.hh.HHAttitudeEvaluator;
import nl.rivm.cib.pilot.hh.HHAttitudePropagator;
import nl.rivm.cib.pilot.hh.HHAttractor;
import nl.rivm.cib.pilot.hh.HHAttribute;
import tec.uom.se.ComparableQuantity;

/**
 * {@link SimplePeerBroker}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class SimplePeerBroker implements PeerBroker
{

	public interface PeerConfig extends YamlConfig
	{

		@DefaultValue( DemoConfig.CONFIG_BASE_DIR )
		@Key( DemoConfig.CONFIG_BASE_KEY )
		String configBase();

		@Key( "invitation-age" )
		@DefaultValue( "[.5 yr; 4 yr)" )
		String vaccinationInvitationAge();

		@SuppressWarnings( "unchecked" )
		default Range<ComparableQuantity<Time>> vaccinationAgeRange()
			throws ParseException
		{
			return Range
					.parse( vaccinationInvitationAge(), QuantityUtil::valueOf )
					.map( v -> v.asType( Time.class ) );
		}

		@Key( "occasion-recurrence" )
		@DefaultValue( "0 0 0 7 * ? *" )
		String vaccinationRecurrence();

		default Iterable<Instant> vaccinationRecurrence(
			final Scheduler scheduler ) throws ParseException
		{
			return Timing.of( vaccinationRecurrence() ).iterate( scheduler );
		}

		/** @see VaxOccasion#utility() */
		@Key( "occasion-utility-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationUtilityDist();

		default ProbabilityDistribution<Number> vaccinationUtilityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationUtilityDist() );
		}

		/** @see VaxOccasion#proximity() */
		@Key( "occasion-proximity-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationProximityDist();

		default ProbabilityDistribution<Number> vaccinationProximityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationProximityDist() );
		}

		/** @see VaxOccasion#clarity() */
		@Key( "occasion-clarity-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationClarityDist();

		default ProbabilityDistribution<Number> vaccinationClarityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationClarityDist() );
		}

		/** @see VaxOccasion#affinity() */
		@Key( "occasion-affinity-dist" )
		@DefaultValue( "const(0.5)" )
		String vaccinationAffinityDist();

		default ProbabilityDistribution<Number> vaccinationAffinityDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( vaccinationAffinityDist() );
		}

		@Key( "attractor-factory" )
		@DefaultValue( "nl.rivm.cib.pilot.hh.HHAttractor$Factory$SimpleBinding" )
		Class<? extends HHAttractor.Factory> hesitancyAttractorFactory();

		default NavigableMap<String, HHAttractor>
			hesitancyAttractors( final LocalBinder binder )
		{
			try
			{
				return Collections.unmodifiableNavigableMap(
						binder.inject( hesitancyAttractorFactory() )
								.createAll( toJSON( "attractors" ) ) );
			} catch( final Exception e )
			{
				return Thrower.rethrowUnchecked( e );
			}
		}

		/** @see RelationFrequencyJson */
		@Key( "relation-frequencies" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
				+ "relation-frequency.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream hesitancyRelationFrequencies();

		default
			ConditionalDistribution<Quantity<Time>, RelationFrequencyJson.Category>
			hesitancyRelationFrequencyDist(
				final ProbabilityDistribution.Factory distFactory )
		{
			final List<RelationFrequencyJson> map = JsonUtil
					.readArrayAsync( () -> hesitancyRelationFrequencies(),
							RelationFrequencyJson.class )
					.toList().blockingGet();
			@SuppressWarnings( "unchecked" )
			final Quantity<Time> defaultDelay = QuantityUtil.valueOf( "1 yr" );
			return c ->
			{
				return map.stream()
						.filter( json -> json.relation == c.relation()
								&& json.gender == c.gender()
								&& json.ageRange().contains( c.floorAge() ) )
						.map( json -> json.intervalDist( distFactory ).draw() )
						.findFirst().orElse( defaultDelay );
			};
		}

		@Key( "relation-impact-rate" )
		@DefaultValue( "1" )
		BigDecimal hesitancyRelationImpactRate();

		/** @see HesitancyProfileJson */
		@Key( "profiles" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM
				+ "hesitancy-univariate.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream hesitancyProfiles();

		default ProbabilityDistribution<HesitancyProfileJson>
			hesitancyProfileDist( final Factory distFactory )
		{
			return distFactory.createCategorical(
					HesitancyProfileJson.parse( () -> hesitancyProfiles() )
							.toList().blockingGet() );
		}

		default <T> ConditionalDistribution<HesitancyProfileJson, T>
			hesitancyProfilesGrouped( final Factory distFactory,
				final Function<HesitancyProfileJson, T> keyMapper )
		{
			return ConditionalDistribution.of( distFactory::createCategorical,
					HesitancyProfileJson.parse( () -> hesitancyProfiles() )
							.toMultimap( wv -> keyMapper.apply( wv.getValue() ),
									wv -> wv )
							.blockingGet() );
		}

		@Key( "profile-sample" )
		@DefaultValue( DemoConfig.CONFIG_BASE_PARAM + "hesitancy-initial.json" )
		@ConverterClass( InputStreamConverter.class )
		InputStream hesitancyProfileSample();

		default
			ConditionalDistribution<Map<HHAttribute, BigDecimal>, HesitancyProfileJson>
			hesitancyProfileSample( final PseudoRandom rng ) throws IOException
		{
			final BigDecimal[][] sample = JsonUtil
					.valueOf( hesitancyProfileSample(), BigDecimal[][].class );
			final Map<HesitancyProfileJson, ProbabilityDistribution<Map<HHAttribute, BigDecimal>>> distCache = new HashMap<>();
			return ConditionalDistribution
					.of( hes -> distCache.computeIfAbsent( hes, key -> () ->
					{
						final int row = rng.nextInt( sample.length );
						final int confCol = hes.indices
								.get( HesitancyDimension.confidence ) - 1;
						final int compCol = hes.indices
								.get( HesitancyDimension.complacency ) - 1;
						return MapBuilder.<HHAttribute, BigDecimal>unordered()
								.put( HHAttribute.CONFIDENCE,
										DecimalUtil.valueOf(
												sample[row][confCol] ) )
								.put( HHAttribute.COMPLACENCY,
										DecimalUtil.valueOf(
												sample[row][compCol] ) )
								.build();
					} ) );
		}

		/**
		 * TODO from profile-data
		 * 
		 * @see HesitancyProfileJson
		 */
		@Key( "calculation-dist" )
		@DefaultValue( "const(0.5)" )
		String hesitancyCalculationDist();

		default ProbabilityDistribution<BigDecimal> hesitancyCalculationDist(
			final Parser distParser ) throws ParseException
		{
			return distParser.<Number>parse( hesitancyCalculationDist() )
					.map( DecimalUtil::valueOf );
		}

		@Key( "social-network-degree" )
		@DefaultValue( "10" )
		int hesitancySocialNetworkDegree();

		@Key( "social-network-beta" )
		@DefaultValue( "0.5" ) // 0 = lattice, 1 = random network
		double hesitancySocialNetworkBeta();

		@Key( "social-assortativity" )
		@DefaultValue( "0.75" )
		double hesitancySocialAssortativity();

		@Key( "school-assortativity-dist" )
		@DefaultValue( "bernoulli(0.75)" )
		String hesitancySchoolAssortativity();

		default ProbabilityDistribution<Boolean> hesitancySchoolAssortativity(
			final Parser distParser ) throws ParseException
		{
			return distParser.parse( hesitancySchoolAssortativity() );
		}

		/** @see HHAttitudeEvaluator */
		@Key( "evaluator" )
		@DefaultValue( "nl.rivm.cib.pilot.hh.HHAttitudeEvaluator$Average" )
		Class<? extends HHAttitudeEvaluator> attitudeEvaluatorType();

		default HHAttitudeEvaluator attitudeEvaluator()
			throws InstantiationException, IllegalAccessException
		{
			return attitudeEvaluatorType().newInstance();
		}

		/** @see HHAttitudePropagator */
		@Key( "propagator" )
		@DefaultValue( "nl.rivm.cib.pilot.hh.HHAttitudePropagator$Shifted" )
		Class<? extends HHAttitudePropagator> attitudePropagatorType();

		default HHAttitudePropagator attitudePropagator()
			throws InstantiationException, IllegalAccessException
		{
			return attitudePropagatorType().newInstance();
		}

		/**
		 * <a
		 * href=http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/crontrigger.html>Cron
		 * trigger pattern</a> for timing the attitude/hesitancy propagation,
		 * e.g.
		 * <ol type=a>
		 * <li>{@code "0 0 0 ? * MON *"} : <em>midnight on every
		 * Monday</em></li>
		 * <li>{@code "0 0 0 1,15 * ? *"} : <em>midnight on every 1st and 15th
		 * day of the month</em></li>
		 * </ol>
		 */
		@Key( "propagator-recurrence" )
		@DefaultValue( "0 0 0 ? * MON *" )
		String attitudePropagatorRecurrence();

		default Iterable<Instant> attitudePropagatorRecurrence(
			final Scheduler scheduler ) throws ParseException
		{
			return Timing.of( attitudePropagatorRecurrence() )
					.iterate( scheduler );
		}

	}

	/** */
	private static final Logger LOG = LogUtil
			.getLogger( SimplePeerBroker.class );

	@InjectConfig
	private PeerConfig config;

	@Inject
	private Scheduler scheduler;

//	@Inject
//	private DataLayer data;
//
//	@Inject
//	private ProbabilityDistribution.Factory distFactory;

	private final PublishSubject<EpiFact> events = PublishSubject.create();

	@Override
	public Scheduler scheduler()
	{
		return this.scheduler;
	}

	@Override
	public Observable<EpiFact> events()
	{
		return this.events;
	}

	private ConditionalDistribution<Comparable<?>, Comparable<?>> regionalCultureDist;

	private ConditionalDistribution<BigDecimal, Object> culturalAttitudeDist;

	@Override
	public SimplePeerBroker reset() throws Exception
	{
		// TODO from CBS
		this.regionalCultureDist = regName -> DemoModel.NA;
		// TODO from PIENTER2
		this.culturalAttitudeDist = cult -> BigDecimal.ZERO;

		// TODO auto-assign to new persons

		LOG.debug( "{} ready", getClass().getSimpleName() );
		return this;
	}

	public void reset( final HouseholdTuple person )
	{
		final Comparable<?> regRef = person.get( Persons.HomeRegionRef.class );
		final Comparable<?> cultureRef = this.regionalCultureDist
				.draw( regRef );
//		final BigDecimal attitude =
				this.culturalAttitudeDist
				.draw( cultureRef ); // TODO split into conf/comp
	}
}
