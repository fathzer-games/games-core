package com.fathzer.games.clock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** The default clock state for a player that accepts everything functionalities defined in {@link ClockSettings} class.
 */
class DefaultClockState implements ClockState {
	private static final long SECONDS_TO_MS = 1000L;

	private AtomicLong countingSince;
	private AtomicLong remainingAtLastMove;
	private AtomicLong remainingMs;
	private AtomicInteger movesSinceIncrement;
	private AtomicInteger movesSinceSettingsChange;
	private ClockSettings settings;
	
	DefaultClockState(ClockSettings settings) {
		this.settings = settings;
		this.countingSince = new AtomicLong(-1);
		this.remainingMs = new AtomicLong(SECONDS_TO_MS * settings.getInitialTime());
		this.remainingAtLastMove = new AtomicLong();
		this.movesSinceIncrement = new AtomicInteger();
		this.movesSinceSettingsChange = new AtomicInteger();
	}
	
	protected long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public synchronized long moveDone() {
		if (isPaused()) {
			throw new IllegalStateException("Can't move when clock is paused");
		}
		long remaining = pause();
		if (settings.getIncrement()!=0 && movesSinceIncrement.incrementAndGet()==settings.getMovesNumberBeforeIncrement()) {
			// Should perform increment
			remaining += settings.getIncrement();
			if (!settings.isCanAccumulate() && remaining > remainingAtLastMove.get()) {
				remaining = remainingAtLastMove.get();
			}
		}
		// TODO next phase
		remainingMs.set(remaining);
		return remaining;
	}
	
	public synchronized long pause() {
		if (!isPaused()) {
			// If clock is already paused, do nothing
			final long elapsed = getCurrentTime()-countingSince.get();
			remainingMs.set(remainingMs.get()-elapsed);
		}
		return remainingMs.get();
	}
	
	public synchronized long start() {
		if (isPaused()) {
			// If clock is already started, do nothing
			countingSince.set(getCurrentTime()); 
		}
		return remainingMs.get();
	}
	
	public synchronized long getRemainingTime() {
		final long start = countingSince.get();
		final long remaining = remainingMs.get();
		return start < 0 ? remaining : remaining + start - getCurrentTime();
	}
	
	public synchronized boolean isPaused() {
		return countingSince.get()<0;
	}
}
