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
		this.remainingMs = new AtomicLong(settings.getInitialTime()*1000);
	}
	
	synchronized void move() {
		
	}
	
	synchronized void pause() {
		
	}
	
	synchronized void start() {
		
	}
	
	synchronized long getRemainingTime() {
		return 0; //TODO
	}
	
	synchronized boolean isPaused() {
		return false; //TODO
	}
}
