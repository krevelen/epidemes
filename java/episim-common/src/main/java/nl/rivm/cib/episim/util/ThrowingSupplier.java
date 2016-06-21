package nl.rivm.cib.episim.util;

/**
 * {@link ThrowingSupplier}
 * 
 * @param <T>
 * @param <E>
 * @version $Id$
 * @author Rick van Krevelen
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable>
{
	T get() throws E;
}