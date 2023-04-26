package com.fathzer.games.clock;

/** The clock state for a player.
 */
public interface ClockState {
	ClockState moveDone();
	
	long pause();
	
	long start();
	
	long getRemainingTime();
	
	boolean isPaused();
}
