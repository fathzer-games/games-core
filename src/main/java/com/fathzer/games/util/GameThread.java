package com.fathzer.games.util;

import com.fathzer.games.MoveGenerator;

class GameThread<M> extends Thread {
	MoveGenerator<M> context;

	public GameThread(Runnable target) {
		super(target);
	}

	public GameThread(Runnable target, MoveGenerator<M> context) { 	//TODO Remove?
		super(target);
		this.context = context;
	}
}