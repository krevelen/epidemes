/* $Id: e67e3079fc342154fce540f31a263151797d8350 $
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
package nl.rivm.cib.episim.model;

import javax.measure.unit.NonSI;

import org.jscience.geography.coordinates.LatLong;
import org.opengis.spatialschema.geometry.geometry.Position;

/**
 * {@link Place} is a stationary {@link TransmissionSpace} located at a
 * geographical position, dimension entity?
 * (wijk/buurt/stad/gemeente/provincie/landsdeel)
 * 
 * @version $Id: e67e3079fc342154fce540f31a263151797d8350 $
 * @author Rick van Krevelen
 */
public interface Place
{

	/** RIVM National Institute for Public Health and the Environment */
	LatLong RIVM_POSITION = LatLong.valueOf( 52.1185272, 5.1868699,
			NonSI.DEGREE_ANGLE );

	/** the NO_ZIP {@link ZipCode} constant */
	ZipCode NO_ZIP = ZipCode.valueOf( "0000" );

	/** @return the global {@link Position} */
	LatLong getPosition();

	/** @return the {@link ZipCode} */
	ZipCode getZip();

	TransmissionSpace getSpace();

	/**
	 * @param position the (centroid) {@link LatLong} position
	 * @param zip the {@link ZipCode}, if any
	 * @param routes the {@link TransmissionRoute}s
	 * @return a {@link Simple} instance of {@link Place}
	 */
	static Place of( final LatLong position, final ZipCode zip,
		final TransmissionSpace space )
	{
		return new Simple( position, zip, space );
	}

	/**
	 * {@link Simple} implementation of {@link Place}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class Simple implements Place
	{

		private final TransmissionSpace space;

		private final LatLong position;

		private final ZipCode zip;

		/**
		 * {@link Simple} constructor
		 * 
		 * @param position the {@link LatLong} position
		 * @param zip the {@link ZipCode}
		 * @param routes the {@link TransmissionRoute}s
		 */
		public Simple( final LatLong position, final ZipCode zip,
			final TransmissionSpace space )
		{
			this.position = position;
			this.zip = zip;
			this.space = space;
		}

		@Override
		public LatLong getPosition()
		{
			return this.position;
		}

		@Override
		public ZipCode getZip()
		{
			return this.zip;
		}

		@Override
		public TransmissionSpace getSpace()
		{
			return this.space;
		}

	}
}
