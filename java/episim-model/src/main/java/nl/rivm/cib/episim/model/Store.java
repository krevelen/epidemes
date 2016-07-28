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
package nl.rivm.cib.episim.model;

import java.util.Collection;
import java.util.Iterator;

import io.coala.time.Proactive;
import io.coala.time.Scheduler;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * {@link Store}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Store<T> extends Collection<T>, Proactive
{

	Observable<T> onAdd();

	Observable<T> onRemove();

	Observable<Integer> onSize();

	static <T> Store<T> of( final Scheduler scheduler,
		final Collection<T> source )
	{
		return new Store<T>()
		{

			private final Subject<T, T> add = PublishSubject.create();

			private final Subject<T, T> remove = PublishSubject.create();

			private final Subject<Integer, Integer> size = PublishSubject
					.create();

			@Override
			public Scheduler scheduler()
			{
				return scheduler;
			}

			@Override
			public Observable<T> onAdd()
			{
				return this.add.asObservable();
			}

			@Override
			public Observable<T> onRemove()
			{
				return this.remove.asObservable();
			}

			@Override
			public Observable<Integer> onSize()
			{
				return this.size.asObservable();
			}

			@Override
			public int size()
			{
				return source.size();
			}

			@Override
			public boolean isEmpty()
			{
				return source.isEmpty();
			}

			@Override
			public boolean contains( final Object o )
			{
				return source.contains( o );
			}

			@Override
			public Iterator<T> iterator()
			{
				final Iterator<T> result = source.iterator();
				return new Iterator<T>()
				{
					private T current = null;

					@Override
					public boolean hasNext()
					{
						return result.hasNext();
					}

					@Override
					public void remove()
					{
						result.remove();
						remove.onNext( this.current );
					}

					@Override
					public T next()
					{
						return (this.current = result.next());
					}
				};
			}

			@Override
			public Object[] toArray()
			{
				return source.toArray();
			}

			@SuppressWarnings( "hiding" )
			@Override
			public <T> T[] toArray( final T[] a )
			{
				return source.toArray( a );
			}

			@Override
			public boolean add( final T e )
			{
				final boolean result = source.add( e );
				if( result )
				{
					this.add.onNext( e );
					this.size.onNext( source.size() );
				}
				return result;
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public boolean remove( final Object e )
			{
				final boolean result = source.remove( e );
				if( result )
				{
					this.remove.onNext( (T) e );
					this.size.onNext( source.size() );
				}
				return result;
			}

			@Override
			public boolean containsAll( final Collection<?> c )
			{
				return source.containsAll( c );
			}

			@Override
			public boolean addAll( final Collection<? extends T> c )
			{
				boolean result = false;
				for( T t : c )
					result |= add( t );
				return result;
			}

			@Override
			public boolean removeAll( final Collection<?> c )
			{
				boolean result = false;
				for( Object t : c )
					result |= remove( t );
				return result;
			}

			@Override
			public boolean retainAll( final Collection<?> c )
			{
				return removeIf( e ->
				{
					return !c.contains( e );
				} );
			}

			@Override
			public void clear()
			{
				// calls iterator().remove()
				if( size() != 0 )
				{
					removeIf( e ->
					{
						return true;
					} );
					this.size.onNext( 0 );
				}
			}
		};
	}
}
