package nl.rivm.cib.episim.model.locate;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.Quantity;
import javax.measure.quantity.Area;

import io.coala.json.JsonUtil;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.time.Instant;

/**
 * {@link Region}
 * 
 * @version $Id: e6c9b6b9dc29f3775464a9a9e2400236f22ef50c $
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

	TypeID type();

	SortedMap<Instant, Region> parents();

	SortedMap<Instant, Region> children();

	default Region currentParent()
	{
		return parents() == null || parents().isEmpty() ? null
				: parents().get( parents().lastKey() );
	}

	Quantity<Area> surfaceArea();

	/**
	 * {@link Directory} will retrieve or generate specified {@link Region}
	 */
	interface Directory
	{
		Region lookup( ID id );
	}

	static Region of( final ID id, final String name, final TypeID type,
		final Map<Instant, Region> parents, final Map<Instant, Region> children,
		final Quantity<Area> surfaceArea )
	{
		return new Simple( id, name, type, parents, children, surfaceArea );
	}

	static Region of( final ID id, final String name, final TypeID type,
		final Region parent, final Map<Instant, Region> children,
		final Quantity<Area> surfaceArea )
	{
		return of( id, name, type,
				Collections.singletonMap( Instant.ZERO, parent ), children,
				surfaceArea );
	}

	public static class Simple extends Identified.SimpleOrdinal<ID>
		implements Region
	{
		private String name;
		private TypeID type;
		private SortedMap<Instant, Region> parents;
		private SortedMap<Instant, Region> children;
		private Quantity<Area> surfaceArea;

		public Simple( final ID id, final String name, final TypeID type,
			final Map<Instant, Region> parents,
			final Map<Instant, Region> children,
			final Quantity<Area> surfaceArea )
		{
			this.id = id;
			this.name = name;
			this.type = type;
			this.parents = parents instanceof SortedMap
					? (SortedMap<Instant, Region>) parents
					: new TreeMap<>( parents );
			this.surfaceArea = surfaceArea;
			this.children = children instanceof SortedMap
					? (SortedMap<Instant, Region>) children
					: new TreeMap<>( children );
		}

		@Override
		public String name()
		{
			return this.name;
		}

		@Override
		public TypeID type()
		{
			return this.type;
		}

		@Override
		public SortedMap<Instant, Region> parents()
		{
			return this.parents;
		}

		@Override
		public Quantity<Area> surfaceArea()
		{
			return this.surfaceArea;
		}

		@Override
		public SortedMap<Instant, Region> children()
		{
			return this.children;
		}
	}
}
