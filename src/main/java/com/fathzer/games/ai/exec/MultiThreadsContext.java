package com.fathzer.games.ai.exec;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fathzer.games.ai.GamePosition;
import com.fathzer.games.util.ContextualizedExecutor;

public class MultiThreadsContext<M> implements ExecutionContext<M> {
	private final Supplier<GamePosition<M>> moveGeneratorBuilder;
	protected final ContextualizedExecutor<GamePosition<M>> exec;
	private final GamePosition<M> globalGamePosition;
	
	public MultiThreadsContext(Supplier<GamePosition<M>> moveGeneratorBuilder, ContextualizedExecutor<GamePosition<M>> exec) {
		this.moveGeneratorBuilder = moveGeneratorBuilder;
		this.exec = exec;
		this.globalGamePosition = moveGeneratorBuilder.get();
	}

	@Override
	public GamePosition<M> getGamePosition() {
		final GamePosition<M> result = exec.getContext();
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
