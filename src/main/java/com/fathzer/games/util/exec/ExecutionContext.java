package com.fathzer.games.util.exec;

import java.util.Collection;

public interface ExecutionContext<T> extends AutoCloseable {
	T getContext();
	
	void execute(Collection<Runnable> tasks);
	 
	@Override
	default void close() {
		// Nothing to do;
	}
}
