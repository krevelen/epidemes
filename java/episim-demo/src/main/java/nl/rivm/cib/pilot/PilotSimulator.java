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
package nl.rivm.cib.pilot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.hibernate.cfg.AvailableSettings;

import io.coala.bind.LocalBinder;
import io.coala.bind.LocalConfig;
import io.coala.config.YamlUtil;
import io.coala.dsol3.Dsol3Scheduler;
import io.coala.log.LogUtil;
import io.coala.log.LogUtil.Pretty;
import io.coala.math.DecimalUtil;
import io.coala.math3.Math3ProbabilityDistribution;
import io.coala.math3.Math3PseudoRandom;
import io.coala.persist.HibernateJPAConfig;
import io.coala.persist.JPAUtil;
import io.coala.random.DistributionParser;
import io.coala.random.ProbabilityDistribution;
import io.coala.random.PseudoRandom;
import io.coala.time.Scheduler;
import io.coala.time.SchedulerConfig;
import io.coala.time.TimeUnits;
import io.coala.util.FileUtil;
import io.coala.util.MapBuilder;
import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.schedulers.Schedulers;
import nl.rivm.cib.episim.cbs.TimeUtil;

/**
 * {@link PilotSimulator}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PilotSimulator
{

	private static final String CONF_ARG = "conf";

	/**
	 * @param args arguments from the command line
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main( final String[] args )
		throws IOException, InterruptedException
	{
		// convert command-line arguments to map
		final Map<String, String> argMap = Arrays.stream( args )
				.filter( arg -> arg.contains( "=" ) )
				.map( arg -> arg.split( "=" ) ).filter( arr -> arr.length > 1 )
				.collect( Collectors.toMap( arr -> arr[0], arr ->
				{
					final String[] value = new String[arr.length - 1];
					System.arraycopy( arr, 1, value, 0, value.length );
					return String.join( "=", value );
				} ) );

		// merge arguments into configuration imported from YAML file
		final PilotConfig hhConfig = ConfigCache.getOrCreate( PilotConfig.class,
				// CLI args added first: override config resource and defaults 
				argMap,
				YamlUtil.flattenYaml( FileUtil
						.toInputStream( argMap.computeIfAbsent( CONF_ARG,
								confArg -> System.getProperty( CONF_ARG,
										// set default configuration data file base directory/url
										argMap.computeIfAbsent(
												PilotConfig.CONFIG_BASE_KEY,
												baseKey -> System.getProperty(
														PilotConfig.CONFIG_BASE_KEY, PilotConfig.CONFIG_BASE_DIR ) )
												+ PilotConfig.CONFIG_YAML_FILE ) ) ) ) );

		if( System.getProperty(
				ConfigurationFactory.CONFIGURATION_FILE_PROPERTY ) == null )
			try( final InputStream is = FileUtil
					.toInputStream( hhConfig.configBase() + "log4j2.yaml" ) )
			{
			// see https://stackoverflow.com/a/42524443
			final LoggerContext ctx = LoggerContext.getContext( false );
			ctx.start( new YamlConfiguration( ctx, new ConfigurationSource( is ) ) );
			} catch( final IOException ignore )
			{
			}

		final Logger LOG = LogUtil.getLogger( PilotSimulator.class );
		LOG.info( "Starting {}, args: {} -> config: {}",
				PilotSimulator.class.getSimpleName(), args,
				hhConfig.toJSON( PilotConfig.SCENARIO_BASE ) );

		// FIXME move binder configuration to sim.yaml
		final LocalConfig binderConfig = LocalConfig.builder()
				.withId( hhConfig.setupName() ) // replication name, sets random seeds

				// configure event scheduler
				.withProvider( Scheduler.class, Dsol3Scheduler.class )

				// configure randomness
				.withProvider( ProbabilityDistribution.Parser.class,
						DistributionParser.class )

				// FIXME skip until work-around is no longer needed
//				.withProvider( ProbabilityDistribution.Factory.class,
//						Math3ProbabilityDistribution.class )
//				.withProvider( PseudoRandom.Factory.class,
//						Math3PseudoRandom.MersenneTwisterFactory.class )

				.build();

		// FIXME workaround until scheduler becomes configurable in coala binder
		final ZonedDateTime offset = hhConfig.offset()
				.atStartOfDay( TimeUtil.NL_TZ );
		final long durationDays = Duration
				.between( offset, offset.plus( hhConfig.duration() ) ).toDays();
		ConfigCache.getOrCreate( SchedulerConfig.class, MapBuilder.unordered()
				.put( SchedulerConfig.ID_KEY, "" + binderConfig.rawId() )
				.put( SchedulerConfig.OFFSET_KEY, "" + offset )
				.put( SchedulerConfig.DURATION_KEY, "" + durationDays )
				.build() );

		// FIXME workaround until seed becomes configurable in coala
		final LocalBinder binder = binderConfig
				.createBinder( MapBuilder.<Class<?>, Object>unordered()
						.put( ProbabilityDistribution.Factory.class,
								new Math3ProbabilityDistribution.Factory(
										new Math3PseudoRandom.MersenneTwisterFactory()
												.create( PseudoRandom.Config.NAME_DEFAULT,
														hhConfig.randomSeed() ) ) )
						.build() );

		final PilotScenario model = binder.inject( PilotScenario.class );

		final File file = //null;
				new File( "pilot-sir-" + model.seed() + ".txt" );
		final CountDownLatch outFile = new CountDownLatch(
				file != null && file.createNewFile() ? 1 : 0 );

		final String sep = "\t";
		if( outFile.getCount() > 0 ) Observable.using(
				() -> new PrintWriter( FileUtil.toOutputStream( file, false ) ),
				pw ->
				{
					return model.sirTransitions().map( sir ->
					{
						final String line = DecimalUtil.toScale( model.now()
								.toQuantity( TimeUnits.DAYS ).getValue(), 4 )
								+ sep + sir[0] + sep + sir[1] + sep + sir[2]
								+ sep + sir[3];
						pw.println( line );
						return line;
					} );
				}, out ->
				{
					out.close();
					outFile.countDown();
				} ).subscribe( line ->
				{
				}, Exceptions::propagate );

		// persist statistics
		final boolean jpa = hhConfig.dbEnabled();
		final CountDownLatch dbLatch = new CountDownLatch( jpa ? 1 : 0 );
		if( jpa ) try
		{
			// trade-off; see https://stackoverflow.com/a/30347287/1418999
			final int jdbcBatchSize = 25;
			// trade-off; 50K+ are postponed until sim ends, flooding the stack
			final int rowsPerTx = 10000;

			final EntityManagerFactory emf = hhConfig.toJPAConfig(
					HibernateJPAConfig.class,
					// add vendor-specific JPA settings (i.e. Hibernate)
					MapBuilder.unordered()
							.put( AvailableSettings.STATEMENT_BATCH_SIZE,
									"" + jdbcBatchSize )
							.put( AvailableSettings.BATCH_VERSIONED_DATA,
									"" + true )
							.put( AvailableSettings.ORDER_INSERTS, "" + true )
							.put( AvailableSettings.ORDER_UPDATES, "" + true )
							.build() )
					.createEMF();

			// shared between threads generating (sim) and flushing (db) rows
			final AtomicLong rowsPending = new AtomicLong();

			model.statistics().doOnNext( dao -> rowsPending.incrementAndGet() )
					.buffer( 10, TimeUnit.SECONDS, rowsPerTx )
					.observeOn(
							// Schedulers.from( Executors.newFixedThreadPool( 4 ) )
							Schedulers.io() ) // TODO is (unlimited) I/O smart?
					.subscribe( buffer ->
					{
						// TODO hold simulator while pending exceeds a maximum ?
						final long start = System.currentTimeMillis();
						final long n = rowsPending.addAndGet( -buffer.size() );
						JPAUtil.session( emf ).subscribe( em ->
						{
							final AtomicLong it = new AtomicLong();
							buffer.forEach( dao ->
							{
								dao.persist( em, binder.id() );
								if( it.incrementAndGet() % jdbcBatchSize == 0 )
								{
									em.flush();
//									em.clear();
								}
							} );
						}, e -> LOG.error( "Problem persisting stats", e ),
								() -> LOG.trace(
										"Persisted {} rows in {}s, {} pending",
										buffer.size(), Pretty
												.of( () -> DecimalUtil.toScale(
														(System.currentTimeMillis()
																- start)
																/ 1000.,
														1 ) ),
										n ) );
					}, e ->
					{
						LOG.error( "Problem generating household stats", e );
						emf.close(); // clean up connections
						dbLatch.countDown();
					}, () ->
					{
						LOG.trace( "Database persistence completed" );
						emf.close(); // clean up connections
						dbLatch.countDown();
					} );
		} catch( final Exception e )
		{
			LOG.error( "Could not start database", e );
			dbLatch.countDown();
		}
//		model.network().subscribe( e -> LOG.trace( "change: {}", e ) );

		// run injected (Singleton) model; start generating the statistics
		model.run();
		LOG.info( "{} ready, finalizing...",
				model.scheduler().getClass().getSimpleName() );

		// wait until all statistics persisted
		dbLatch.await();

		if( outFile.getCount() > 0 )
		{
			LOG.trace( "Waiting for output to: {}", file );
			outFile.await();
			LOG.trace( "Output written to: {}", file );
		}

		LOG.info( "Completed {}!", model.getClass().getSimpleName() );
	}
}
