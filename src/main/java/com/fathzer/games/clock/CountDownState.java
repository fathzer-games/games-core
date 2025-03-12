package com.fathzer.games.clock;

/** Warning this class will change for sure!!!
 * It is not generic enough to reflect every clock state that can happen.
 * Probably, it should contains a clock settings view
 */
public class CountDownState {
	private final long remainingMs;
	private final long incrementMs;
	private final int movesToGo;
	
	/** Constructor.
	 * @param remainingMs The remaining time in ms
	 * @param incrementMs The increment time in ms
	 * @param movesToGo The number of moves remaining before next phase
	 * @throws IllegalArgumentException if the <code>movesToGo</code> is negative
	 */
	public CountDownState(long remainingMs, long incrementMs, int movesToGo) {
		super();
		if (movesToGo<0) {
			throw new IllegalArgumentException();
		}
		this.remainingMs = remainingMs;
		this.incrementMs = incrementMs;
		this.movesToGo = movesToGo;
	}

	/** Gets the remaining time in ms.
	 * @return a long
	 */
	public long getRemainingMs() {
		return remainingMs;
	}

	/** Gets the increment time in ms.
	 * @return a long
	 */
	public long getIncrementMs() {
		return incrementMs;
	}

	/** Gets the number of moves remaining before next phase.
	 * @return a positive int
	 */
	public int getMovesToGo() {
		return movesToGo;
	}
}
