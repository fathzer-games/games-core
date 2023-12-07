package com.fathzer.games.util.exec;

import java.util.Collection;

public class SingleThreadContext<T> implements ExecutionContext<T> {
	private final T context;
	
	public SingleThreadContext(T position) {
		this.context = position;
	}

	@Override
	public T getContext() {
		return this.context;
	}

	@Override
	public void execute(Collection<Runnable> tasks) {
		for (Runnable task : tasks) {
			task.run();
		}
	}
}
