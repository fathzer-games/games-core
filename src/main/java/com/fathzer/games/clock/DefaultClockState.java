package com.fathzer.games.clock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** The default clock state for a player that accepts everything functionalities defined in {@link ClockSettings} class.
 */
class DefaultClockState implements ClockState {
	private static final long SECONDS_TO_MS = 1000L;

	protected AtomicLong countingSince;
	protected AtomicLong remainingAtLastMove;
	protected AtomicLong remainingMs;
	protected AtomicInteger movesSinceIncrement;
	protected AtomicInteger movesSinceSettingsChange;
	protected ClockSettings settings;
	
	protected DefaultClockState(ClockSettings settings) {
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
			movesSinceIncrement.set(0);
			if (!settings.isCanAccumulate() && remaining > remainingAtLastMove.get()) {
				remaining = remainingAtLastMove.get();
			}
		}
		if (settings.getNext()!=null && movesSinceSettingsChange.incrementAndGet()==settings.getMovesNumberBeforeNext()) {
			// Next phase
			remaining = Math.min(remaining, settings.getMaxRemainingKept());
			settings = settings.getNext();
			movesSinceSettingsChange.set(0);
			remaining += settings.getInitialTime();
		}
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
