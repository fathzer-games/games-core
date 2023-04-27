package com.fathzer.games.clock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** The default clock state for a player that accepts everything functionalities defined in {@link ClockSettings} class.
 * <br>Warning: This class is not thread safe.
 */
class DefaultCountDown implements CountDown {
	private static final long SECONDS_TO_MS = 1000L;

	protected AtomicLong countingSince;
	protected AtomicLong remainingAtIncrement;
	protected AtomicLong remainingMs;
	protected AtomicInteger movesSinceIncrement;
	protected AtomicInteger movesSinceSettingsChange;
	protected ClockSettings settings;
	
	protected DefaultCountDown(ClockSettings settings) {
		this.settings = settings;
		this.countingSince = new AtomicLong(-1);
		this.remainingMs = new AtomicLong(SECONDS_TO_MS * settings.getInitialTime());
		this.remainingAtIncrement = new AtomicLong(remainingMs.get());
		this.movesSinceIncrement = new AtomicInteger();
		this.movesSinceSettingsChange = new AtomicInteger();
	}
	
	protected long getCurrentTime() {
		return System.currentTimeMillis();
	}
	
	public CountDown moveDone() {
		if (isPaused()) {
			throw new IllegalStateException("Can't move when count down is paused");
		}
		long remaining = pause();
		if (settings.getIncrement()!=0 && movesSinceIncrement.incrementAndGet()==settings.getMovesNumberBeforeIncrement()) {
			// Should perform increment
			remaining += SECONDS_TO_MS*settings.getIncrement();
			movesSinceIncrement.set(0);
			if (!settings.isCanAccumulate() && remaining > remainingAtIncrement.get()) {
				remaining = remainingAtIncrement.get();
			}
			remainingAtIncrement.set(remaining);
		}
		if (settings.getNext()!=null && movesSinceSettingsChange.incrementAndGet()==settings.getMovesNumberBeforeNext()) {
			// Next phase
			remaining = Math.min(remaining, SECONDS_TO_MS*settings.getMaxRemainingKept());
			settings = settings.getNext();
			movesSinceSettingsChange.set(0);
			remaining += SECONDS_TO_MS*settings.getInitialTime();
		}
		remainingMs.set(remaining);
		return this;
	}
	
	@Override
	public long pause() {
		if (!isPaused()) {
			// If clock is already paused, do nothing
			final long elapsed = getCurrentTime()-countingSince.get();
			remainingMs.set(remainingMs.get()-elapsed);
			countingSince.set(-1);
		}
		return remainingMs.get();
	}
	
	@Override
	public long start() {
		if (!isPaused()) {
			throw new IllegalStateException("Can't start an already started count down");
		}
		// If clock is already started, do nothing
		countingSince.set(getCurrentTime());
		return remainingMs.get();
	}
	
	@Override
	public long getRemainingTime() {
		final long start = countingSince.get();
		final long remaining = remainingMs.get();
		return start < 0 ? remaining : remaining + start - getCurrentTime();
	}
	
	@Override
	public boolean isPaused() {
		return countingSince.get()<0;
	}
}
