package com.fathzer.games.util.exec;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class MultiThreadsContext<T extends Forkable<T>> implements ExecutionContext<T> {
	protected final ContextualizedExecutor<T> exec;
	private final T globalContext;
	
	public MultiThreadsContext(T context, ContextualizedExecutor<T> exec) {
		this.exec = exec;
		this.globalContext = context;
	}

	@Override
	public T getContext() {
		final T result = exec.getContext();
		if (result==null) {
			return globalContext;
		}
		return result;
	}
	
	@Override
	public void execute(Collection<Runnable> tasks) {
		Collection<Callable<Void>> callables = tasks.stream().map(this::toCallable).toList();
		try {
			final List<Future<Void>> futures = exec.invokeAll(callables, globalContext);
			exec.checkExceptions(futures);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private Callable<Void> toCallable(Runnable task) {
		return () -> {
			task.run();
			return null;
		};
	}

	@Override
	public void close() {
		exec.close();
	}
}
