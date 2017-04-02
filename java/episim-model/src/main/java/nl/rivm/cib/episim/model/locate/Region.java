package nl.rivm.cib.episim.model.locate;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Singleton;
import javax.measure.Quantity;
import javax.measure.quantity.Area;

import io.coala.math.QuantityUtil;
import io.coala.name.Id;
import io.coala.name.Identified;
import io.coala.time.Instant;
import nl.rivm.cib.episim.model.person.Population;

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
		public static ID of( final String value )
		{
			return Util.of( value, new ID() );
		}
	}

	String name();

	String type();

	SortedMap<Instant, Region> parents();

	default Region currentParent()
	{
		return parents() == null || parents().isEmpty() ? null
				: parents().get( parents().lastKey() );
	}

	Quantity<Area> surfaceArea();

	/**
	 * @return the inhabitants
	 */
	Population<?> population();

	default Quantity<?> populationDensity()
	{
		return QuantityUtil.valueOf( population().members().size() )
				.divide( surfaceArea() );
	}

	/**
	 * {@link Factory} will retrieve or generate specified {@link Region}
	 */
	@Singleton
	interface Factory
	{
		Region get( ID id );
	}

	static Region of( final ID id, final String name, final String type,
		final Map<Instant, Region> parent, final Quantity<Area> surfaceArea,
		final Population<?> population )
	{
		return new Simple( id, name, type, parent, surfaceArea, population );
	}

	static Region of( final ID id, final String name, final String type,
		final Region parent, final Quantity<Area> surfaceArea,
		final Population<?> population )
	{
		return of( id, name, type,
				Collections.singletonMap( Instant.ZERO, parent ), surfaceArea,
				population );
	}

	public static class Simple extends Identified.SimpleOrdinal<ID>
		implements Region
	{
		private String name;
		private String type;
		private SortedMap<Instant, Region> parent;
		private Quantity<Area> surfaceArea;
		private Population<?> population;

		public Simple( final ID id, final String name, final String type,
			final Map<Instant, Region> parent, final Quantity<Area> surfaceArea,
			final Population<?> population )
		{
			this.id = id;
			this.name = name;
			this.type = type;
			this.parent = parent instanceof SortedMap
					? (SortedMap<Instant, Region>) parent
					: new TreeMap<>( parent );
			this.surfaceArea = surfaceArea;
			this.population = population;
		}

		@Override
		public String name()
		{
			return this.name;
		}

		@Override
		public String type()
		{
			return this.type;
		}

		@Override
		public SortedMap<Instant, Region> parents()
		{
			return this.parent;
		}

		@Override
		public Quantity<Area> surfaceArea()
		{
			return this.surfaceArea;
		}

		@Override
		public Population<?> population()
		{
			return this.population;
		}
	}
}
