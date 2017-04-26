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

/**
 * {@link SparkTest} stub...
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class SparkTest
{

	public static class RegionNode // implements Location
	{
//		private RegionNode parent;

//		private Map<Object, Object> residents;

		public RegionNode()
		{
			this( null );
		}

		public RegionNode( final RegionNode parent )
		{
//			this.parent = parent;
		}

		public void registerResident( Object ind )
		{
			//this.residents.put( ind, ind );
		}
	}

	/** */
//	private static final Logger LOG = LogUtil.getLogger( SparkTest.class );

	public static void main( final String[] args )
	{
		// nation
		RegionNode nl = new RegionNode();
		// part
		RegionNode mn = new RegionNode( nl );
		// province -- > 40 COROP vs 25 GHOR / GGD
		RegionNode nh = new RegionNode( mn );
		// corop/metropolitan region, see https://www.wikiwand.com/nl/COROP
		RegionNode aggl = new RegionNode( nh );
		// GHOR safety/GGD health region
		RegionNode knm = new RegionNode( aggl );
		// municipality
		RegionNode hlm = new RegionNode( knm );
		// burough
		RegionNode cent = new RegionNode( hlm );
		// ward
		RegionNode kamp = new RegionNode( cent );
		// street code
		RegionNode turf = new RegionNode( kamp );

		turf.registerResident( null );

//		final String appName = "demoApp"; // UI name
//		final String master = "local[*]"; // obtain via https://spark.apache.org/docs/latest/submitting-applications.html
//		SparkConf conf = new SparkConf().setAppName( appName )
//				.setMaster( master );
//		JavaSparkContext sc = new JavaSparkContext( conf );
//		List<Integer> data = Arrays.asList( 1, 2, 3, 4, 5 );
//		JavaRDD<Integer> distData = sc.parallelize( data );
//		final Integer res = distData.reduce( ( a, b ) -> a + b );
//		LOG.info( "res={}", res );
	}
}
