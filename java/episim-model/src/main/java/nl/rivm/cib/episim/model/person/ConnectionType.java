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
package nl.rivm.cib.episim.model.person;

import io.coala.json.Attributed;

/**
 * {@link ConnectionType} determines behavioral/physical contagion, see e.g.
 * https://www.cbs.nl/nl-nl/onze-diensten/methoden/begrippen?tab=p#id=positie-in-het-huishouden
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface ConnectionType
{

	/** affects attitude */
	boolean isInformative(); // e.g. peer, media, authority, ...

	/** affects infections */
	boolean isContiguous(); // e.g. fellow, (class)mate, colleague, ...

	/** affects STD infections */
	boolean isSexual(); // e.g. lover, spouse, ...

//	/** affects attitude */
//	boolean isRepresentative(); // e.g. (foster/step) parent, guardian, ...

	interface Attributable<THIS> extends Attributed
	{
		ConnectionType getRelationType();

		void setRelationType( ConnectionType relationType );

		@SuppressWarnings( "unchecked" )
		default THIS with( final ConnectionType relationType )
		{
			setRelationType( relationType );
			return (THIS) this;
		}
	}

	enum Simple implements ConnectionType
	{
		/** e.g. online forum, media, authority, peer */
		INFORM( true, false, false/* , false */ ),
		/** e.g. parent/child, mate, colleague, fellow, cohabiting */
		SOCIAL( true, true, false/* , false */ ),
		/** e.g. casual, marital */
		PARTNER( true, true, true/* , false */ ),
		/** e.g. bachelor, single parent */
		SINGLE( true, true, false/* , false */ ),
		/** represented by guardian, e.g. biological/foster/step/co-parent */
		WARD( false, true, false/* , true */ );

		private final boolean informative;
		private final boolean contiguous;
		private final boolean sexual;
//		private final boolean representative;

		private Simple( final boolean informative, final boolean contiguous,
			final boolean sexual/* , final boolean representative */ )
		{
			this.informative = informative;
			this.contiguous = contiguous;
			this.sexual = sexual;
//			this.representative = representative;
		}

		@Override
		public boolean isInformative()
		{
			return this.informative;
		}

		@Override
		public boolean isContiguous()
		{
			return this.contiguous;
		}

		@Override
		public boolean isSexual()
		{
			return this.sexual;
		}

//		@Override
//		public boolean isRepresentative()
//		{
//			return this.representative;
//		}
	}
}