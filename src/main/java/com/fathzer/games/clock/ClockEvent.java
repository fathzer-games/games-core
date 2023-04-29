package com.fathzer.games.clock;

public class ClockEvent {
	private final Clock clock;
	private final ClockState previousState;
	private final ClockState newState;
	
	ClockEvent(Clock clock, ClockState previousState, ClockState newState) {
		super();
		this.clock = clock;
		this.previousState = previousState;
		this.newState = newState;
	}

	public Clock getClock() {
		return clock;
	}

	public ClockState getPreviousState() {
		return previousState;
	}

	public ClockState getNewState() {
		return newState;
	}
}
