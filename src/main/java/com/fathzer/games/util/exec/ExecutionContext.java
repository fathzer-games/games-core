package com.fathzer.games.util.exec;

import java.util.Collection;

/** A kind of an executor service that manages a {@link Forkable} context attached to every thread it used.
 * <br>Developing a multi-threaded game engine typically requires to have a game representation attached to each thread
 * that is processing the requests.
 * <br>This class allows you to attach a fork of the context object to every thread of an executor service and then retrieve it
 * with no information but the calling thread. 
 * @param <T> The context's class
 */
public interface ExecutionContext<T> extends AutoCloseable {
	/** Gets the context of the calling thread.
	 * @return the context. It is guaranteed that none of the executor's threads have same context.
	 */
	T getContext();
	
	/** Executes some tasks with some thread context.
	 * @param tasks The list of tasks to execute
	 * @see #getContext()
	 */
	void execute(Collection<Runnable> tasks);
	 
	@Override
	default void close() {
		// Nothing to do;
	}
	
	/** Gets a new execution context.
	 * @param parallelism The number of threads to use. If 1, a single thread will be used. Otherwise, a thread pool of the given size will be used.
	 * @param context The context to use. It will be forked to each thread.
	 * @return An execution context.
	 * @throws IllegalArgumentException if the parallelism is less than 1 or the context is null.
	 */
	public static <T extends Forkable<T>> ExecutionContext<T> get(int parallelism, T context) {
		if (parallelism<1 || context==null) {
			throw new IllegalArgumentException();
		}
		if (parallelism==1) {
			return new SingleThreadContext<>(context);
		} else {
			final ContextualizedExecutor<T> exec = new ContextualizedExecutor<>(parallelism);
			return new MultiThreadsContext<>(context, exec);
		}
	}
}
