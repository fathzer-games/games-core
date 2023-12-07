package com.fathzer.games.util.exec;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MultiThreadsContext<T> implements ExecutionContext<T> {
	private final Supplier<T> contextBuilder;
	protected final ContextualizedExecutor<T> exec;
	private final T globalContext;
	
	
	public MultiThreadsContext(Supplier<T> contextBuilder, ContextualizedExecutor<T> exec) {
		this.contextBuilder = contextBuilder;
		this.exec = exec;
		this.globalContext = contextBuilder.get();
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
		Collection<Callable<Void>> callables = tasks.stream().map(this::toCallable).collect(Collectors.toList());
		try {
			final List<Future<Void>> futures = exec.invokeAll(callables, contextBuilder);
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
