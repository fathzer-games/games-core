package com.fathzer.games.clock;

/** The clock state for a player.
 */
public interface ClockState {
	long moveDone();
	
	long pause();
	
	long start();
	
	long getRemainingTime();
	
	boolean isPaused();
}
