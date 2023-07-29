package com.fathzer.games.ai.exec;

import java.util.Collection;

import com.fathzer.games.ai.GamePosition;

public interface ExecutionContext<M> extends AutoCloseable {
	GamePosition<M> getGamePosition();
	
	void execute(Collection<Runnable> tasks);
	 
	@Override
	default void close() {
		// Nothing to do;
	}
}
