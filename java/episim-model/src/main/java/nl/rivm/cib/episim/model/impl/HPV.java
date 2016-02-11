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
package nl.rivm.cib.episim.model.impl;

import nl.rivm.cib.episim.model.ContactEvent;
import nl.rivm.cib.episim.model.ContagionEvent;
import nl.rivm.cib.episim.model.Disease;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link HPV} or the Human papillomavirus has a
 * <a href="http://www.who.int/mediacentre/factsheets/fs380/en/">WHO fact
 * sheet</a>
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class HPV implements Disease
{

	private Subject<ContagionEvent, ContagionEvent> transmission = PublishSubject
			.create();

	@Override
	public Observable<ContagionEvent> getTransmission()
	{
		return this.transmission.asObservable();
	}

	@Override
	public void onCompleted()
	{
		this.transmission.onCompleted();
	}

	@Override
	public void onError( final Throwable e )
	{
		this.transmission.onError( e );
	}

	@Override
	public void onNext( final ContactEvent t )
	{
		// TODO schedule infectino events for exposed, infective, removed, etc
	}

}
