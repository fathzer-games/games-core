package com.fathzer.games.clock;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** The default clock state for a player that accepts everything functionalities defined in {@link ClockSettings} class.
 * <br>Warning: This class is not thread safe.
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
	
	public ClockState moveDone() {
		if (isPaused()) {
			throw new IllegalStateException("Can't move when clock is paused");
		}
		long remaining = pause();
		System.out.println("Entering moveDone with remaining before increment and new settings is "+remaining+" for "+this); //TODO
		if (settings.getIncrement()!=0 && movesSinceIncrement.incrementAndGet()==settings.getMovesNumberBeforeIncrement()) {
			// Should perform increment
			remaining += SECONDS_TO_MS*settings.getIncrement();
			System.out.println("remaining after increment is "+remaining+" for "+this); //TODO
			movesSinceIncrement.set(0);
			if (!settings.isCanAccumulate() && remaining > remainingAtLastMove.get()) {
				remaining = remainingAtLastMove.get();
				System.out.println("remaining after applyint accumulate prevention is "+remaining+" for "+this); //TODO
			}
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
	
	public long pause() {
		if (!isPaused()) {
			// If clock is already paused, do nothing
			final long elapsed = getCurrentTime()-countingSince.get();
			remainingMs.set(remainingMs.get()-elapsed);
			countingSince.set(-1);
			System.out.println("Elapsed before pause is "+elapsed+" remaining is now "+remainingMs+" for "+this); //TODO
		}
		return remainingMs.get();
	}
	
	public long start() {
		if (isPaused()) {
			// If clock is already started, do nothing
			countingSince.set(getCurrentTime());
			System.out.println("Start counting "+this+". It remains "+remainingMs); //TODO
		}
		return remainingMs.get();
	}
	
	public long getRemainingTime() {
		final long start = countingSince.get();
		final long remaining = remainingMs.get();
		return start < 0 ? remaining : remaining + start - getCurrentTime();
	}
	
	public boolean isPaused() {
		return countingSince.get()<0;
	}
}
