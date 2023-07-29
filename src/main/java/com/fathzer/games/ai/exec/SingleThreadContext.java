package com.fathzer.games.ai.exec;

import java.util.Collection;

import com.fathzer.games.ai.GamePosition;

public class SingleThreadContext<M> implements ExecutionContext<M> {
	private final GamePosition<M> gamePosition;
	
	public SingleThreadContext(GamePosition<M> position) {
		this.gamePosition = position;
	}

	@Override
	public GamePosition<M> getGamePosition() {
		return this.gamePosition;
	}
	
	@Override
	public void execute(Collection<Runnable> tasks) {
		for (Runnable task : tasks) {
			task.run();
		}
	}
}
