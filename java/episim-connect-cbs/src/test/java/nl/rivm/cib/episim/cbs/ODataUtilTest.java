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
package nl.rivm.cib.episim.cbs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.olingo.client.api.domain.ODataEntitySetIterator;
import org.apache.olingo.commons.api.domain.v4.ODataEntity;
import org.apache.olingo.commons.api.domain.v4.ODataEntitySet;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.api.serialization.ODataDeserializerException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.coala.log.LogUtil;

/**
 * {@link ODataUtilTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ODataUtilTest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( ODataUtilTest.class );

	private static final String serviceUrl = "http://opendata.cbs.nl/ODataApi/OData/71071ned";

	private static final String entity = "TypedDataSet";

	private static final String relation = "Regio";

	private static final String filter = "Perioden eq '2009JJ00'";

	private static Edm edm = null;

	@BeforeClass
	public static void initializeEdm() throws IOException
	{
		edm = ODataUtil.readEdm( serviceUrl );
	}

	@Test
	public void readEdmTest()
	{
		LOG.trace( "----- Read Edm ------------------------------" );
		final List<FullQualifiedName> ctFqns = new ArrayList<>();
		final List<FullQualifiedName> etFqns = new ArrayList<>();
		for( EdmSchema schema : edm.getSchemas() )
		{
			for( EdmComplexType complexType : schema.getComplexTypes() )
				ctFqns.add( complexType.getFullQualifiedName() );

			for( EdmEntityType entityType : schema.getEntityTypes() )
				etFqns.add( entityType.getFullQualifiedName() );
		}
		LOG.trace( "Found ComplexTypes:\n\t"
				+ ctFqns.toString().replace( ", ", ",\n\t " ) );
		LOG.trace( "Found EntityTypes:\n\t"
				+ etFqns.toString().replace( ", ", ",\n\t " ) );

		final FullQualifiedName entity = new FullQualifiedName(
				"Cbs.OData.GeoDetailCategory" );//etFqns.get( 0 );
		LOG.trace(
				"----- Inspect each property and its type of the first entity: "
						+ entity + "----" );
		final EdmEntityType etype = edm.getEntityType( entity );
		for( String propertyName : etype.getPropertyNames() )
			LOG.trace( etype.getStructuralProperty( propertyName ).getType()
					.getFullQualifiedName() + " '"
					+ entity.getFullQualifiedNameAsString() + "." + propertyName
					+ "'" );
	}

	@Test
	public void readEntitiesTest()
	{
		LOG.trace( "----- Read Entities " + " ------------------------------" );
		final ODataEntitySetIterator<ODataEntitySet, ODataEntity> iterator = ODataUtil
				.readEntities( edm, serviceUrl, entity );

		while( iterator.hasNext() )
		{
			final ODataEntity ce = iterator.next();
			LOG.trace( "Entry:\n"
					+ ODataUtil.prettyPrint( ce.getProperties(), 1 ) );
		}
	}

	@Test
	public void readEntityWithKeyTest()
	{
		LOG.trace( "----- Read Entry ------------------------------" );
		final ODataEntity entry = ODataUtil.readEntityWithKey( edm, serviceUrl,
				entity, 1 );
		LOG.trace( "Single Entry:\n"
				+ ODataUtil.prettyPrint( entry.getProperties(), 1 ) );

	}

	@Test
	@Ignore // FIXME
	public void readEntityWithKeyExpandTest()
	{
		LOG.trace( "----- Read Entity with $expand relation: " + relation );
		final ODataEntity entry = ODataUtil.readEntityWithKeyExpand( edm,
				serviceUrl, entity, 1, relation );
		LOG.trace( "Single Entry with expanded relation:\n"
				+ ODataUtil.prettyPrint( entry.getProperties(), 0 ) );
	}

	@Test
	public void readEntitiesWithFilterTest()
	{
		LOG.trace( "----- Read Entities with $filter query: " + filter );
		final ODataEntitySetIterator<ODataEntitySet, ODataEntity> iterator = ODataUtil
				.readEntitiesWithFilter( edm, serviceUrl, entity, filter );
		while( iterator.hasNext() )
		{
			ODataEntity ce = iterator.next();
			LOG.trace( "Entry:\n"
					+ ODataUtil.prettyPrint( ce.getProperties(), 1 ) );
		}
	}

	private static final int key = 123;

	@Test
	@Ignore // skip everything as odata4 sample/server only supporting retrieval
	public void entityCRUDTest() throws ODataDeserializerException, IOException
	{
		LOG.trace( "----- Create Entry ------------------------------" );
		ODataEntity ce = ODataUtil.parseEntity( "/mymanufacturer.json" );
		LOG.trace( "Got data: " + ce );
		ODataUtil.createEntity( edm, serviceUrl, entity, ce );

		LOG.trace( "----- Update Entry ------------------------------" );
		final ODataEntity ce2 = ODataUtil
				.parseEntity( "/mymanufacturer2.json" );
		final HttpStatusCode sc = ODataUtil.updateEntity( edm, serviceUrl,
				entity, key, ce2 );
		LOG.trace( "Updated successfully: " + sc );
		final ODataEntity entry = ODataUtil.readEntityWithKey( edm, serviceUrl,
				entity, key );
		LOG.trace( "Updated Entry: "
				+ ODataUtil.prettyPrint( entry.getProperties(), 0 ) );

		LOG.trace( "----- Delete Entry ------------------------------" );
		final HttpStatusCode sc2 = ODataUtil.deleteEntity( serviceUrl, entity,
				key );
		LOG.trace( "Deletion of Entry was successful: " + sc2 );

		try
		{
			LOG.trace( "----- Verify Delete Entry ---------------------" );
			ODataUtil.readEntityWithKey( edm, serviceUrl, entity, key );
		} catch( Exception e )
		{
			LOG.trace( e.getMessage() );
		}
	}

}
