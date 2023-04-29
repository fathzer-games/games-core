package com.fathzer.games.clock;

public enum ClockState {
	CREATED(false),
	PAUSED(false),
	STARTED(true), //TODO Change to counting
	ENDED(false);

	private final boolean counting;
	
	private ClockState(boolean b) {
		this.counting = b;
	}

	public boolean isCounting() {
		return counting;
	}
}
