package com.fathzer.games.ai;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A class to keep track of statistics during a search.
 */
public class SearchStatistics {
	private final AtomicLong evalCount = new AtomicLong();
	private final AtomicLong moveGenerationCount = new AtomicLong();
	private final AtomicLong generatedMoveCount = new AtomicLong();
	private final AtomicLong movePlayedCount = new AtomicLong();
	private final AtomicLong moveFromTTPlayedCount = new AtomicLong();
	private long startMs = System.currentTimeMillis();
	
	private boolean enabled = true;
	
	/** Checks whether the statistics are enabled.
	 * @return true if the statistics are enabled (which is the default), false otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/** Sets whether the statistics are enabled.
	 * @param enabled true to enable the statistics, false to disable them
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/** Clears the statistics.
	 * This method resets the start time used by {@link #getDurationMs()} and sets all statistics to zero.
	 */
	public void clear() {
		startMs = System.currentTimeMillis();
		evalCount.set(0);
		moveGenerationCount.set(0);
		generatedMoveCount.set(0);
		movePlayedCount.set(0);
	}

	/** Gets the number of position evaluations done during a search.
	 * @return a positive long
	 */
	public long getEvaluationCount() {
		return evalCount.get();
	}

	/** Gets the number of moves generation done during a search.
	 * @return a positive long
	 */
	public long getMoveGenerationCount() {
		return moveGenerationCount.get();
	}

	/** Gets the number of moves generated during a search.
	 * @return a positive long
	 */
	public long getGeneratedMoveCount() {
		return generatedMoveCount.get();
	}

	/** Gets the number of moves played during a search.
	 * @return a positive long
	 */
	public long getMovePlayedCount() {
		return movePlayedCount.get();
	}

	/** Gets the number of moves from the transposition table played during a search.
	 * @return a positive long
	 */
	public long getMoveFromTTPlayedCount() {
		return moveFromTTPlayedCount.get();
	}
	
	/** Increments the number of moves played. */
	public void movePlayed() {
		movePlayedCount.incrementAndGet();
	}

	/** Increments the number of moves played from the transposition table. */
	public void moveFromTTPlayed() {
		moveFromTTPlayedCount.incrementAndGet();
	}

	/** Increments the number of position evaluations done. */
	public void evaluationDone() {
		if (enabled) {
			evalCount.incrementAndGet();
		}
	}

	/** Increments the number of moves generated.
	 * @param moveCount How many moves have been generated
	*/
	public void movesGenerated(int moveCount) {
		if (enabled) {
			moveGenerationCount.incrementAndGet();
			generatedMoveCount.addAndGet(moveCount);
		}
	}
	
	/** Gets the duration of the search in milliseconds.
	 * <br>This method returns the time elapsed since the call to {@link #clear()} or since this instance was created if {@link #clear()} has not been called yet.
	 * @return a positive long
	 */
	public long getDurationMs() {
		return System.currentTimeMillis()-startMs;
	}

	@Override
	public String toString() {
		return enabled ? "SearchStatistics [moveGenerationCount=" + moveGenerationCount + ", generatedMoveCount="
				+ generatedMoveCount + ", movePlayedCount=" + movePlayedCount + ", evalCount=" + evalCount + "]" :
			"SearchStatistics are disabled";
	}
}