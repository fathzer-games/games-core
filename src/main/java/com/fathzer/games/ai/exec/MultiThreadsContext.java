package com.fathzer.games.ai.exec;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.util.ContextualizedExecutor;

public class MultiThreadsContext<M, B extends MoveGenerator<M>> implements ExecutionContext<M,B> {
	private final Supplier<B> moveGeneratorBuilder;
	protected final ContextualizedExecutor<B> exec;
	private final B globalGamePosition;
	
	public MultiThreadsContext(Supplier<B> moveGeneratorBuilder, ContextualizedExecutor<B> exec) {
		this.moveGeneratorBuilder = moveGeneratorBuilder;
		this.exec = exec;
		this.globalGamePosition = moveGeneratorBuilder.get();
	}

	@Override
	public B getGamePosition() {
		final B result = exec.getContext();
		if (result==null) {
			return globalGamePosition;
		}
		return result;
	}
	
	@Override
	public void execute(Collection<Runnable> tasks) {
		Collection<Callable<Void>> callables = tasks.stream().map(this::toCallable).collect(Collectors.toList());
		try {
			final List<Future<Void>> futures = exec.invokeAll(callables, moveGeneratorBuilder);
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
