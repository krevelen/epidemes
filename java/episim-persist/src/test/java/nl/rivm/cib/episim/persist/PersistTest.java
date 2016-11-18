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
package nl.rivm.cib.episim.persist;

import java.util.Collections;

import javax.persistence.EntityManagerFactory;

import org.aeonbits.owner.ConfigCache;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.coala.config.ConfigUtil;
import io.coala.log.LogUtil;
import io.coala.persist.HibernateJPAConfig.SchemaPolicy;
import io.coala.persist.JPAUtil;
import io.coala.time.Duration;
import io.coala.time.Instant;
import io.coala.time.TimeUnits;
import nl.rivm.cib.episim.model.disease.Condition;
import nl.rivm.cib.episim.model.disease.infection.ContactEvent;
import nl.rivm.cib.episim.model.disease.infection.TransmissionEvent;
import nl.rivm.cib.episim.model.disease.infection.TransmissionRoute;
import nl.rivm.cib.episim.model.disease.infection.TransmissionSpace;
import nl.rivm.cib.episim.model.locate.Place;
import nl.rivm.cib.episim.model.locate.Region;
import nl.rivm.cib.episim.persist.fact.TransmissionFactDao;

/**
 * {@link PersistTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class PersistTest
{
	/** */
	private static final Logger LOG = LogUtil.getLogger( PersistTest.class );

	private static EntityManagerFactory EMF;

	@BeforeClass
	public static void createEMF()
	{
		final PersistenceConfig conf = ConfigCache
				.getOrCreate( PersistenceConfig.class );
		LOG.trace( "Testing with JPA config: {}", ConfigUtil.export( conf ) );
		EMF = conf.createEntityManagerFactory( "hibernate_test_pu",
				Collections.singletonMap( PersistenceConfig.SCHEMA_POLICY_KEY,
						SchemaPolicy.create ) );
	}

	@AfterClass
	public static void closeEMF()
	{
		if( EMF != null ) EMF.close();
	}

	@Test
	public void testDAOs() throws Exception
	{
		final DateTime offset = DateTime.parse( "2010-01-01T00:00+00:00" );
		final Region region = Region.of( "NL", null, null, null );
		final Place site = Place.of( Place.RIVM_POSITION, Place.NO_ZIP,
				region );
		final Instant start = null;
		final Duration duration = null;
		final TransmissionSpace space = null;
		final TransmissionRoute route = null;
		final Condition primary = null;
		final Condition secondary = null;
		final ContactEvent cause = ContactEvent.of( start, duration, space,
				route, primary, secondary );
		final Instant time = Instant.of( 3.456, TimeUnits.ANNUM );
		final TransmissionEvent event = TransmissionEvent.of( time, site,
				cause );
		JPAUtil.session( EMF, em ->
		{
			final TransmissionFactDao fact = TransmissionFactDao.of( em, offset,
					event );
			LOG.trace( "Persisting: {}", fact );
			em.persist( fact );
		} );
		JPAUtil.session( EMF, em ->
		{
			LOG.trace( "Read table, result: {}",
					em.createQuery( "SELECT f FROM "
							+ PersistenceConfig.TRANSMISSION_FACT_ENTITY
							+ " f" ).getResultList() );
		} );
	}
}
