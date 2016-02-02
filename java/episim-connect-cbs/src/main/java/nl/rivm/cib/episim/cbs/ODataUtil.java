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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.olingo.client.api.communication.request.cud.v4.UpdateType;
import org.apache.olingo.client.api.domain.ODataEntitySetIterator;
import org.apache.olingo.client.api.v4.ODataClient;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.domain.ODataCollectionValue;
import org.apache.olingo.commons.api.domain.ODataComplexValue;
import org.apache.olingo.commons.api.domain.v4.ODataEntity;
import org.apache.olingo.commons.api.domain.v4.ODataEntitySet;
import org.apache.olingo.commons.api.domain.v4.ODataEnumValue;
import org.apache.olingo.commons.api.domain.v4.ODataProperty;
import org.apache.olingo.commons.api.domain.v4.ODataValue;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.api.serialization.ODataDeserializerException;

import io.coala.log.LogUtil;

/**
 * {@code Olingo V4.0.0-beta-2} still offers {@code Edm.DateTime} compatibility
 * from V3, but its {@code OData...} types have been refactored to
 * {@code getClient()...} in later V4 releases
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ODataUtil
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( ODataClient.class );

	/** in case of odata4 server limitation: not handling metadata=full */
	//private static final String accept = "application/json;odata.metadata=minimal";

	/** */
	private static ODataClient olingo = null;

	/**
	 * {@link ODataClient} singleton constructor
	 */
	private ODataUtil()
	{
		// unreachable
	}

	private static ODataClient getClient()
	{
		if( olingo == null ) olingo = ODataClientFactory.getV4();
		return olingo;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public static String prettyPrint( Map properties, int level )
	{
		StringBuilder b = new StringBuilder();
		Set<Entry<String, Object>> entries = properties.entrySet();

		for( Entry<String, Object> entry : entries )
		{
			indent( b, level );
			b.append( entry.getKey() ).append( ": " );
			Object value = entry.getValue();
			if( value instanceof Map )
			{
				value = prettyPrint( (Map) value, level + 1 );
			} else if( value instanceof Calendar )
			{
				Calendar cal = (Calendar) value;
				value = SimpleDateFormat.getInstance().format( cal.getTime() );
			}
			b.append( value ).append( "\n" );
		}
		// remove last line break
		b.deleteCharAt( b.length() - 1 );
		return b.toString();
	}

	public static String prettyPrint( Collection<?> properties, int level )
	{
		StringBuilder b = new StringBuilder();

		for( Object p : properties )
		{
			ODataProperty entry = (ODataProperty) p;
			indent( b, level );
			ODataValue value = entry.getValue();
			if( value.isCollection() )
			{
				ODataCollectionValue<ODataValue> cclvalue = value
						.asCollection();
				b.append(
						prettyPrint( cclvalue.asJavaCollection(), level + 1 ) );
			} else if( value.isComplex() )
			{
				ODataComplexValue<?> cpxvalue = value.asComplex();
				b.append( prettyPrint( cpxvalue.asJavaMap(), level + 1 ) );
			} else if( value.isEnum() )
			{
				ODataEnumValue cnmvalue = value.asEnum();
				b.append( entry.getName() ).append( ": " );
				b.append( cnmvalue.getValue() ).append( "\n" );
			} else if( value.isPrimitive() )
			{
				b.append( entry.getName() ).append( ": " );
				b.append( entry.getValue() ).append( "\n" );
			}
		}
		return b.toString();
	}

	private static void indent( StringBuilder builder, int indentLevel )
	{
		for( int i = 0; i < indentLevel; i++ )
		{
			builder.append( "\t" );
		}
	}

	public static Edm readEdm( final URI serviceRoot ) throws IOException
	{
		return readEdm( serviceRoot.toASCIIString() );
	}

	public static Edm readEdm( final String serviceUrl ) throws IOException
	{
		return getClient().getRetrieveRequestFactory()
				.getMetadataRequest( serviceUrl ).execute().getBody();
	}

	public static ODataEntitySetIterator<ODataEntitySet, ODataEntity>
		readEntities( final Edm edm, final String serviceUri,
			final String entitySetName )
	{
		return readEntities( edm, getClient().newURIBuilder( serviceUri )
				.appendEntitySetSegment( entitySetName ).build() );
	}

	public static ODataEntitySetIterator<ODataEntitySet, ODataEntity>
		readEntitiesWithFilter( final Edm edm, final String serviceUri,
			final String entitySetName, final String filterName )
	{
		return readEntities( edm,
				getClient().newURIBuilder( serviceUri )
						.appendEntitySetSegment( entitySetName )
						.filter( filterName ).build() );
	}

	private static ODataEntitySetIterator<ODataEntitySet, ODataEntity>
		readEntities( Edm edm, URI absoluteUri )
	{
		LOG.trace( "URI = " + absoluteUri );
		return //((ODataEntitySetIteratorRequest<ODataEntitySet, ODataEntity>) 
		getClient().getRetrieveRequestFactory()
				.getEntitySetIteratorRequest( absoluteUri )
				//.setAccept( accept ))
				.execute().getBody();
	}

	public static ODataEntity readEntityWithKey( final Edm edm,
		final String serviceUri, final String entitySetName,
		final Object keyValue )
	{
		return readEntity( edm,
				getClient().newURIBuilder( serviceUri )
						.appendEntitySetSegment( entitySetName )
						.appendKeySegment( keyValue ).build() );
	}

	public static ODataEntity readEntityWithKeyExpand( final Edm edm,
		final String serviceUri, final String entitySetName,
		final Object keyValue, final String expandRelationName )
	{
		return readEntity( edm,
				getClient().newURIBuilder( serviceUri )
						.appendEntitySetSegment( entitySetName )
						.appendKeySegment( keyValue )
						.expand( expandRelationName ).build() );
	}

	private static ODataEntity readEntity( final Edm edm,
		final URI absoluteUri )
	{
		return //((ODataEntityRequest<ODataEntity>)
		getClient().getRetrieveRequestFactory().getEntityRequest( absoluteUri )
				//.setAccept( accept ))
				.execute().getBody();
	}

	public static ODataEntity parseEntity( final String classPath )
		throws ODataDeserializerException
	{
		return getClient().getBinder().getODataEntity( getClient()
				.getDeserializer( ODataFormat.APPLICATION_JSON )
				.toEntity( Thread.currentThread().getContextClassLoader()
						.getResourceAsStream( classPath ) ) );
	}

	public static ODataEntity createEntity( final Edm edm,
		final String serviceUri, final String entitySetName,
		final ODataEntity ce )
	{
		return createEntity( edm, getClient().newURIBuilder( serviceUri )
				.appendEntitySetSegment( entitySetName ).build(), ce );
	}

	private static ODataEntity createEntity( final Edm edm,
		final URI absoluteUri, final ODataEntity ce )
	{
		return //((ODataEntityCreateRequest<ODataEntity>) 
		getClient().getCUDRequestFactory()
				.getEntityCreateRequest( absoluteUri, ce )
				//.setAccept( accept ))
				.execute().getBody();
	}

	public static HttpStatusCode updateEntity( final Edm edm,
		final String serviceUri, final String entityName, final Object keyValue,
		final ODataEntity ce )
	{
		return HttpStatusCode.fromStatusCode(
				//((ODataEntityUpdateRequest<ODataEntity>) 
				getClient().getCUDRequestFactory()
						.getEntityUpdateRequest(
								getClient().newURIBuilder( serviceUri )
										.appendEntitySetSegment( entityName )
										.appendKeySegment( keyValue ).build(),
								UpdateType.PATCH, ce )
						//.setAccept( accept ))
						.execute().getStatusCode() );
	}

	public static HttpStatusCode deleteEntity( final String serviceUri,
		final String entityName, final Object keyValue ) throws IOException
	{
		return HttpStatusCode.fromStatusCode(
				//((ODataDeleteRequest) 
				getClient().getCUDRequestFactory()
						.getDeleteRequest(
								getClient().newURIBuilder( serviceUri )
										.appendEntitySetSegment( entityName )
										.appendKeySegment( keyValue ).build() )
						//.setAccept( accept ))
						.execute().getStatusCode() );
	}
}