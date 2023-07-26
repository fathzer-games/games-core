package com.fathzer.games.ai.exec;

import java.util.Collection;

import com.fathzer.games.MoveGenerator;

public interface ExecutionContext<M> extends AutoCloseable {
	MoveGenerator<M> getMoveGenerator();
	
	void execute(Collection<Runnable> tasks);
	 
	@Override
	default void close() {
		// Nothing to do;
	}
}
