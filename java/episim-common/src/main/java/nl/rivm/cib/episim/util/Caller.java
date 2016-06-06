package nl.rivm.cib.episim.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.coala.exception.ExceptionFactory;

/**
 * {@link Caller} decorates a (checked) {@link Callable} method and provides
 * (checked) invariant {@link Function} {@link #ignore(Object)} which ignores
 * its input
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public interface Caller<T, U, R> extends Callable<R>, Supplier<R>, Runnable
{

	/** @return the {@link Callable} */
	Callable<R> getCallable();

	/**
	 * a (checked) {@link Callable} executing the wrapped {@link #getCallable()}
	 * 
	 * @throws Exception
	 */
	default R call() throws Exception
	{
		return getCallable().call();
	}

	/**
	 * an (unchecked) {@link Supplier} of the wrapped {@link #getCallable()}
	 * which wraps its checked {@link Exception}s within unchecked
	 * {@link RuntimeException}s
	 */
	@Override
	default R get()
	{
		try
		{
			return call();
		} catch( final Exception e )
		{
			throw ExceptionFactory.createUnchecked( e, "Problem calling {}",
					getCallable().getClass() );
		}
	}

	/**
	 * an (unchecked) {@link Runnable} of the wrapped {@link #getCallable()}
	 */
	@Override
	default void run()
	{
		get();
	}

	/**
	 * a checked invariant {@link Function} of the wrapped
	 * {@link #getCallable()}, i.e. which ignores its input
	 * 
	 * @param input a {@link T} to ignore
	 * @throws Exception
	 */
	default R ignore( final T input ) throws Exception
	{
		return call();
	}

	/**
	 * a checked invariant {@link BiFunction} of the wrapped
	 * {@link #getCallable()}, i.e. which ignores its input
	 * 
	 * @param input1 some {@link T} to ignore
	 * @param input2 some {@link U} to ignore
	 * @throws Exception
	 */
	default R ignore( final T input1, final U input2 ) throws Exception
	{
		return call();
	}

	/**
	 * an unchecked invariant {@link Function} of the wrapped
	 * {@link #getCallable()}, i.e. which ignores its input
	 * 
	 * @param input a {@link T} to ignore
	 */
	default R ignoreUnchecked( final T input )
	{
		return get();
	}

	/**
	 * an unchecked invariant {@link BiFunction} of the wrapped
	 * {@link #getCallable()}, i.e. which ignores its input
	 * 
	 * @param input1 some {@link T} to ignore
	 * @param input2 some {@link U} to ignore
	 */
	default R ignoreUnchecked( final T input1, final U input2 )
	{
		return get();
	}

	class Simple<T, U, R> implements Caller<T, U, R>
	{
		private final Callable<R> callable;

		public Simple( final Callable<R> callable )
		{
			this.callable = Objects.requireNonNull( callable );
		}

		@Override
		public Callable<R> getCallable()
		{
			return this.callable;
		}

	}

	/**
	 * @param callable the {@link Callable} method
	 * @return an {@link Caller} instance
	 */
	static <T, U, R> Caller<T, U, R> of( final Callable<R> callable )
	{
		return new Simple<T, U, R>( callable );
	}

	static <T, U> Caller<T, U, Void> of( final Runnable runnable )
	{
		// <void> incompatible with <Void>
		return of( new Callable<Void>()
		{
			@Override
			public Void call()
			{
				runnable.run();
				return null;
			}
		} );
	}

	static <T, U, R> Caller<T, U, R> of( final Supplier<R> supplier )
	{
		return of( (Callable<R>) supplier::get );
	}

	static <T, U, R> Caller<T, U, R> of( final Constructor<R> constructor,
		final Object... argsConstant )
	{
		return of( new Callable<R>()
		{
			@Override
			public R call() throws Exception
			{
				constructor.setAccessible( true );
				return constructor.newInstance( argsConstant );
			}
		} );
	}

	static <T, U, R> Caller<T, U, R> of( final Constructor<R> constructor,
		final Callable<Object[]> argsSupplier )
	{
		return of( new Callable<R>()
		{
			@Override
			public R call() throws Exception
			{
				constructor.setAccessible( true );
				return constructor.newInstance( argsSupplier.call() );
			}
		} );
	}

	static <T, U> Caller<T, U, Object> of( final Method method,
		final Object target, final Object... argsConstant )
	{
		return of( new Callable<Object>()
		{
			@Override
			public Object call() throws Exception
			{
				method.setAccessible( true );
				return method.invoke( target, argsConstant );
			}
		} );
	}

	static <T, U> Caller<T, U, Object> of( final Method method,
		final Object target, final Callable<Object[]> argsSupplier )
	{
		return of( new Callable<Object>()
		{
			@Override
			public Object call() throws Exception
			{
				method.setAccessible( true );
				return method.invoke( target, argsSupplier.call() );
			}
		} );
	}

	static <T, U, V> Caller<T, U, Void> of( final Consumer<V> consumer,
		final V constant )
	{
		// must wrap to prevent cycles
		return of( new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				consumer.accept( constant );
				return null;
			}
		} );
	}

	static <T, U, V> Caller<T, U, Void> of( final Consumer<V> consumer,
		final Callable<V> supplier )
	{
		return of( new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				consumer.accept( supplier.call() );
				return null;
			}
		} );
	}

	static <T, U, V, W> Caller<T, U, Void> of( final BiConsumer<V, W> b,
		final V constant1, final W constant2 )
	{
		// must wrap to prevent cycles
		return of( new Callable<Void>()
		{
			@Override
			public Void call()
			{
				b.accept( constant1, constant2 );
				return null;
			}
		} );
	}

	static <T, U, V, W> Caller<T, U, Void> of( final BiConsumer<V, W> b,
		final Callable<V> supplier1, final Callable<W> supplier2 )
	{
		return of( new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				b.accept( supplier1.call(), supplier2.call() );
				return null;
			}
		} );
	}

	static <T, U, V> Caller<T, U, Boolean> of( final Predicate<V> predicate,
		final V constant )
	{
		// must wrap to prevent cycles
		return of( new Callable<Boolean>()
		{
			@Override
			public Boolean call()
			{
				return predicate.test( constant );
			}
		} );
	}

	static <T, U, V> Caller<T, U, Boolean> of( final Predicate<V> predicate,
		final Callable<V> supplier )
	{
		return of( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return predicate.test( supplier.call() );
			}
		} );
	}

	static <T, U, V, W> Caller<T, U, Boolean> of(
		final BiPredicate<V, W> predicate, final V constant1,
		final W constant2 )
	{
		// must wrap to prevent cycles
		return of( new Callable<Boolean>()
		{
			@Override
			public Boolean call()
			{
				return predicate.test( constant1, constant2 );
			}
		} );
	}

	static <T, U, V, W> Caller<T, U, Boolean> of(
		final BiPredicate<V, W> predicate, final Callable<V> supplier1,
		final Callable<W> supplier2 )
	{
		return of( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return predicate.test( supplier1.call(), supplier2.call() );
			}
		} );
	}

	static <T, U, V, R> Caller<T, U, R> of( final Function<V, R> f,
		final V constant )
	{
		// must wrap to prevent cycles
		return of( new Callable<R>()
		{
			@Override
			public R call()
			{
				return f.apply( constant );
			}
		} );
	}

	static <T, U, V, R> Caller<T, U, R> of( final Function<V, R> f,
		final Callable<V> supplier )
	{
		return of( new Callable<R>()
		{
			@Override
			public R call() throws Exception
			{
				return f.apply( supplier.call() );
			}
		} );
	}

	static <T, U, V, W, R> Caller<T, U, R> of( final BiFunction<V, W, R> f,
		final V constant1, final W constant2 )
	{
		// must wrap to prevent cycles
		return of( new Callable<R>()
		{
			@Override
			public R call()
			{
				return f.apply( constant1, constant2 );
			}
		} );
	}

	static <T, U, V, W, R> Caller<T, U, R> of( final BiFunction<V, W, R> f,
		final Callable<V> supplier1, final Callable<W> supplier2 )
	{
		return of( new Callable<R>()
		{
			@Override
			public R call() throws Exception
			{
				return f.apply( supplier1.call(), supplier2.call() );
			}
		} );
	}

	@FunctionalInterface
	public interface Consumer_WithExceptions<T, E extends Exception>
	{
		void accept( T t ) throws E;
	}

	@FunctionalInterface
	public interface BiConsumer_WithExceptions<T, U, E extends Exception>
	{
		void accept( T t, U u ) throws E;
	}

	@FunctionalInterface
	public interface Function_WithExceptions<T, R, E extends Exception>
	{
		R apply( T t ) throws E;
	}

	@FunctionalInterface
	public interface Supplier_WithExceptions<T, E extends Exception>
	{
		T get() throws E;
	}

	@FunctionalInterface
	public interface Runnable_WithExceptions<E extends Exception>
	{
		void run() throws E;
	}

	/**
	 * .forEach(rethrowConsumer(name ->
	 * System.out.println(Class.forName(name)))); or
	 * .forEach(rethrowConsumer(ClassNameUtil::println));
	 */
	public static <T, E extends Exception> Consumer<T>
		rethrow( final Consumer_WithExceptions<T, E> consumer )
	{
		return t ->
		{
			try
			{
				consumer.accept( t );
			} catch( Exception exception )
			{
				throwAsUnchecked( exception );
			}
		};
	}

	public static <T, U, E extends Exception> BiConsumer<T, U>
		rethrow( final BiConsumer_WithExceptions<T, U, E> biConsumer )
	{
		return ( t, u ) ->
		{
			try
			{
				biConsumer.accept( t, u );
			} catch( Exception exception )
			{
				throwAsUnchecked( exception );
			}
		};
	}

	/**
	 * .map(rethrowFunction(name -> Class.forName(name))) or
	 * .map(rethrowFunction(Class::forName))
	 */
	public static <T, R, E extends Exception> Function<T, R>
		rethrow( final Function_WithExceptions<T, R, E> function )
	{
		return t ->
		{
			try
			{
				return function.apply( t );
			} catch( Exception exception )
			{
				throwAsUnchecked( exception );
				return null;
			}
		};
	}

	/**
	 * rethrowSupplier(() -> new StringJoiner(new String(new byte[]{77, 97, 114,
	 * 107}, "UTF-8"))),
	 */
	public static <T, E extends Exception> Supplier<T>
		rethrow( final Supplier_WithExceptions<T, E> function )
	{
		return () ->
		{
			try
			{
				return function.get();
			} catch( Exception exception )
			{
				throwAsUnchecked( exception );
				return null;
			}
		};
	}

	/** uncheck(() -> Class.forName("xxx")); */
	public static void uncheck( Runnable_WithExceptions<?> t )
	{
		try
		{
			t.run();
		} catch( Exception exception )
		{
			throwAsUnchecked( exception );
		}
	}

	/** uncheck(() -> Class.forName("xxx")); */
	public static <R, E extends Exception> R
		uncheck( Supplier_WithExceptions<R, E> supplier )
	{
		try
		{
			return supplier.get();
		} catch( Exception exception )
		{
			throwAsUnchecked( exception );
			return null;
		}
	}

	/** uncheck(Class::forName, "xxx"); */
	public static <T, R, E extends Exception> R
		uncheck( Function_WithExceptions<T, R, E> function, T t )
	{
		try
		{
			return function.apply( t );
		} catch( Exception exception )
		{
			throwAsUnchecked( exception );
			return null;
		}
	}

	@SuppressWarnings( "unchecked" )
	static <E extends Throwable> void
		throwAsUnchecked( final Exception exception ) throws E
	{
		throw (E) exception;
	}

}