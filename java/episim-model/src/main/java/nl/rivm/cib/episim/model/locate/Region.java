package nl.rivm.cib.episim.model.locate;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.measure.Quantity;
import javax.measure.quantity.Area;

import io.coala.json.JsonUtil;
import io.coala.name.Id;
import io.coala.name.Identified;

/**
 * {@link Region} is an inert static entity
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Region extends Identified<Region.ID>
{
	class ID extends Id.Ordinal<String>
	{
		static
		{
			JsonUtil.checkRegistered( JsonUtil.getJOM(), Region.ID.class );
		}

		// for Config default conversion
		public static ID valueOf( final String value )
		{
			return of( value );
		}

		public static ID of( final String value )
		{
			return Util.of( value, new ID() );
		}
	}

	class TypeID extends Id.Ordinal<String>
	{
		public static TypeID of( final String value )
		{
			return Util.of( value, new TypeID() );
		}
	}

	String name();

//	TypeID type();

//	Quantity<Area> surfaceArea();

//	LatLong centroid();

	Region parent();

	Collection<? extends Region> children();

	default Stream<? extends Region> childStream( final boolean parallel )
	{
		return StreamSupport.stream( children().spliterator(), parallel );
	}

	default Stream<? extends Region> neighborStream( final boolean parallel )
	{
		return parent() == null ? Stream.empty()
				: Stream.of( parent() )
						.flatMap( r -> r.childStream( parallel ) )
						.filter( r -> r != this );
	}

	/**
	 * {@link Directory} will retrieve or generate specified {@link Region}
	 */
	interface Directory
	{
		Region lookup( ID id );
	}

	public static class Simple extends Identified.SimpleOrdinal<ID>
		implements Region
	{
		static Region.Simple of( final ID id, final String name,
			final TypeID type, final Quantity<Area> surfaceArea,
			final Region parent, final Collection<? extends Region> children )
		{
			return new Region.Simple( id, name, //type, surfaceArea, 
					parent, children );
		}

		private String name;
//		private TypeID type;
		private Region parent;
		private Collection<? extends Region> children;
//		private Quantity<Area> surfaceArea;

		public Simple( final ID id, final String name,
//			final TypeID type,
//			final Quantity<Area> surfaceArea,
			final Region parent, final Collection<? extends Region> children )
		{
			this.id = id;
			this.name = name;
//			this.type = type;
			this.parent = parent;
//			this.surfaceArea = surfaceArea;
			this.children = children;
		}

		@Override
		public String name()
		{
			return this.name;
		}

//		@Override
//		public TypeID type()
//		{
//			return this.type;
//		}

		@Override
		public Region parent()
		{
			return this.parent;
		}

//		@Override
//		public Quantity<Area> surfaceArea()
//		{
//			return this.surfaceArea;
//		}

		@Override
		public Collection<? extends Region> children()
		{
			return this.children;
		}
	}

	interface Habitat<T> extends Region
	{

		@Override
		Habitat<T> parent();

		@Override
		Collection<? extends Habitat<T>> children();

		/** null-safe */
		@Override
		default Stream<? extends Habitat<T>>
			childStream( final boolean parallel )
		{
			return children() == null ? Stream.empty()
					: StreamSupport.stream( children().spliterator(),
							parallel );
		}

		default int population()
		{
			return (inhabitants() == null ? 0 : inhabitants().size())
					+ childStream( false ).map( h -> h.inhabitants().size() )
							.reduce( ( p1, p2 ) -> p1 + p2 ).orElse( 0 );
		}

		Collection<T> inhabitants();

		/** null-safe, recursive */
		default Stream<T> inhabitantStream( final boolean parallel )
		{
			return inhabitants() == null
					? childStream( parallel )
							.flatMap( h -> h.inhabitantStream( parallel ) )
					: StreamSupport.stream( inhabitants().spliterator(),
							parallel );
		}

		public static class Simple<T> extends Region.Simple
			implements Habitat<T>
		{

			private Collection<T> inhabitants;

			public Simple( final ID id, final String name,
//				final TypeID type, final Quantity<Area> surfaceArea, 
				final Habitat<T> parent,
				final Collection<? extends Habitat<T>> children,
				final Collection<T> inhabitants )
			{
				super( id, name, // type, surfaceArea, 
						parent, children );
				this.inhabitants = inhabitants;
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public Habitat<T> parent()
			{
				return (Habitat<T>) super.parent;
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public Collection<? extends Habitat<T>> children()
			{
				return (Collection<? extends Habitat<T>>) super.children();
			}

			@Override
			public Collection<T> inhabitants()
			{
				return this.inhabitants;
			}

		}
	}
}
