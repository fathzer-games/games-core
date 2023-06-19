package com.fathzer.games.util;

import com.fathzer.games.MoveGenerator;

class GameThread<M> extends Thread {
	MoveGenerator<M> context;

	public GameThread(Runnable target, MoveGenerator<M> context) {
		super(target);
		this.context = context;
	}
}