package com.fathzer.games.ai.exec;

import java.util.Collection;

import com.fathzer.games.MoveGenerator;

public interface ExecutionContext<M, B extends MoveGenerator<M>> extends AutoCloseable {
	B getGamePosition();
	
	void execute(Collection<Runnable> tasks);
	 
	@Override
	default void close() {
		// Nothing to do;
	}
}
