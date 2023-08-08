package com.fathzer.games.ai.exec;

import java.util.Collection;

import com.fathzer.games.MoveGenerator;

public class SingleThreadContext<M, B extends MoveGenerator<M>> implements ExecutionContext<M,B> {
	private final B gamePosition;
	
	public SingleThreadContext(B position) {
		this.gamePosition = position;
	}

	@Override
	public B getGamePosition() {
		return this.gamePosition;
	}
	
	@Override
	public void execute(Collection<Runnable> tasks) {
		for (Runnable task : tasks) {
			task.run();
		}
	}
}
