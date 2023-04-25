package com.fathzer.games.clock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** The clock state for a player.
 */
class ClockState {
	private AtomicLong countingSince;
	private AtomicLong remainingMs;
	private AtomicInteger movesSinceExtraTime;
	private AtomicInteger movesSinceSettingsChange;
	private ClockSettings settings;
	
	ClockState(ClockSettings settings) {
		this.settings = settings;
		this.countingSince = new AtomicLong(-1);
		this.remainingMs = new AtomicLong(settings.getInitialTime()*1000);
	}
	
	protected long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	synchronized void move() {
		
	}
	
	synchronized long pause() {
		if (!isPaused()) {
			// If clock is already paused, do nothing
			final long elapsed = getCurrentTime()-countingSince.get();
			remainingMs.set(remainingMs.get()-elapsed);
		}
		return remainingMs.get();
	}
	
	synchronized long start() {
		if (isPaused()) {
			// If clock is alreadyStarted, do nothing
			countingSince.set(getCurrentTime()); 
		}
		return remainingMs.get();
	}
	
	synchronized long getRemainingTime() {
		final long start = countingSince.get();
		final long remaining = remainingMs.get();
		return start < 0 ? remaining : remaining + start - getCurrentTime();
	}
	
	synchronized boolean isPaused() {
		return countingSince.get()<0;
	}
}
