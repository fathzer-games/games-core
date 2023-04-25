package com.fathzer.games.clock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** The clock state for a player.
 */
public interface ClockState {
	long moveDone();
	
	long pause();
	
	long start();
	
	long getRemainingTime();
	
	boolean isPaused();
}
