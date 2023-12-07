package com.fathzer.games.util.exec;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.fathzer.games.util.PhysicalCores;
import com.fathzer.games.util.UncheckedException;

/** A kind of an executor service that manages a context attached to every thread it used.
 * <br>When developing a multithreaded game engine typically requires to have a game representation attached to each thread
 * that are processing the requests. This class allows you to attach a context object to every thread of an executor service and then retrieve it with no information but the calling thread. 
 * @param <T> The context's class
 */
public class ContextualizedExecutor<T> implements AutoCloseable {
	
	private static class ContextThread<T> extends Thread {
		T context;

		public ContextThread(Runnable target) {
			super(target);
		}
	}

	private final AtomicBoolean running;
	private final ExecutorService exec;
	private final List<ContextThread<T>> threads;
	private Supplier<T> contextSupplier;
	
	/** Default constructor.
	 * <br>The created instance will use a number of threads equals to the number of processors.
	 * @see PhysicalCores#count()
	 */
	public ContextualizedExecutor() {
		this(PhysicalCores.count());
	}

	/** Constructor.
	 * @param parallelism The number of threads to use to process tasks submitted to {@link #invokeAll(Collection, Supplier)}
	 */
	public ContextualizedExecutor(int parallelism) {
		this.running = new AtomicBoolean();
		this.threads = new LinkedList<>();
		this.exec = Executors.newFixedThreadPool(parallelism, r -> {
			final ContextThread<T> contextThread = new ContextThread<>(r);
			contextThread.context = contextSupplier.get();
			threads.add(contextThread);
			return contextThread;
		});
	}
	
	/** Executes some tasks with some thread context.
	 * @param <V> The class of the task's results.
	 * @param tasks The list of tasks to execute
	 * @param contextSupplier A context supplier that will be called one time for each worker thread.
	 * <br>Please note that the supplier will be called only when invokeAll is called
	 * @return The futures that corresponds to the executed tasks.
	 * @throws InterruptedException if the executor is shutdown during the execution of tasks.
	 * @see #getContext()
	 */
	public <V> List<Future<V>> invokeAll(Collection<Callable<V>> tasks, Supplier<T> contextSupplier) throws InterruptedException {
		if (running.compareAndSet(false, true)) {
			this.contextSupplier = contextSupplier;
			threads.forEach(t -> t.context = contextSupplier.get());
			final List<Future<V>> result = exec.invokeAll(tasks);
			running.set(false);
			return result;
		} else {
			throw new IllegalStateException();
		}
	}
	
	/** Check if any execution exception occurred and convert it to a RuntimeException.
	 * @param <V>
	 * @param futures The futures to test
	 * @throws InterruptedException if thread is interrupted while waiting for futures to complete.
	 * @throws UncheckedException if an execution exception occurred on a future. 
	 */
	public <V> void checkExceptions(Collection<Future<V>> futures) throws InterruptedException {
		try {
			for (Future<V> future : futures) {
				future.get();
			}
		} catch (ExecutionException e) {
			throw new UncheckedException(e.getCause());
		}
	}
	
	/** Returns the context of current thread.
	 * @return an instance returned by the context supplier passed to {@link #invokeAll(Collection, Supplier)} or null if this method is called
	 * by a thread not managed by this class.
	 */
	@SuppressWarnings("unchecked")
	public T getContext() {
		final Thread currentThread = Thread.currentThread();
		return (currentThread instanceof ContextThread) ? ((ContextThread<T>)currentThread).context : null;
	}

	@Override
	public void close() {
		exec.shutdown();
	}
}