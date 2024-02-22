package com.fathzer.games.clock;

/** Warning this class will change for sure!!!
 * It is not generic enough to reflect every clock state that can happen.
 * Probably, it should contains a clock settings view
 */
public class CountDownState {
	private final long remainingMs;
	private final long incrementMs;
	private final int movesToGo;
	
	public CountDownState(long remainingMs, long incrementMs, int movesToGo) {
		super();
		if (movesToGo<0) {
			throw new IllegalArgumentException();
		}
		this.remainingMs = remainingMs;
		this.incrementMs = incrementMs;
		this.movesToGo = movesToGo;
	}

	public long getRemainingMs() {
		return remainingMs;
	}

	public long getIncrementMs() {
		return incrementMs;
	}

	public int getMovesToGo() {
		return movesToGo;
	}
}
