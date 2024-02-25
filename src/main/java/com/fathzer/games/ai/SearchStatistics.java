package com.fathzer.games.ai;

import java.util.concurrent.atomic.AtomicLong;

public class SearchStatistics {
	private final AtomicLong evalCount = new AtomicLong();
	private final AtomicLong moveGenerationCount = new AtomicLong();
	private final AtomicLong generatedMoveCount = new AtomicLong();
	private final AtomicLong movePlayedCount = new AtomicLong();
	private final AtomicLong moveFromTTPlayedCount = new AtomicLong();
	private long startMs = System.currentTimeMillis();
	
	private boolean enabled = true;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void clear() {
		startMs = System.currentTimeMillis();
		evalCount.set(0);
		moveGenerationCount.set(0);
		generatedMoveCount.set(0);
		movePlayedCount.set(0);
	}

	public long getEvaluationCount() {
		return evalCount.get();
	}

	public long getMoveGenerationCount() {
		return moveGenerationCount.get();
	}

	public long getGeneratedMoveCount() {
		return generatedMoveCount.get();
	}

	public long getMovePlayedCount() {
		return movePlayedCount.get();
	}

	public long getMoveFromTTPlayedCount() {
		return moveFromTTPlayedCount.get();
	}
	
	public void movePlayed() {
		movePlayedCount.incrementAndGet();
	}

	public void moveFromTTPlayed() {
		moveFromTTPlayedCount.incrementAndGet();
	}

	public void evaluationDone() {
		if (enabled) {
			evalCount.incrementAndGet();
		}
	}

	public void movesGenerated(int moveCount) {
		if (enabled) {
			moveGenerationCount.incrementAndGet();
			generatedMoveCount.addAndGet(moveCount);
		}
	}
	
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