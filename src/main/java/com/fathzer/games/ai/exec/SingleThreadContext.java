package com.fathzer.games.ai.exec;

import java.util.Collection;

import com.fathzer.games.MoveGenerator;

public class SingleThreadContext<M> implements ExecutionContext<M> {
	private final MoveGenerator<M> moveGenerator;
	
	public SingleThreadContext(MoveGenerator<M> moveGenerator) {
		this.moveGenerator = moveGenerator;
	}

	@Override
	public MoveGenerator<M> getMoveGenerator() {
		return this.moveGenerator;
	}
	
	@Override
	public void execute(Collection<Runnable> tasks) {
		for (Runnable task : tasks) {
			task.run();
		}
	}
}
