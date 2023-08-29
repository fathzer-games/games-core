package com.fathzer.games.ai;

import java.util.concurrent.atomic.AtomicLong;

public class SearchStatistics {
	private final AtomicLong evalCount = new AtomicLong();
	private final AtomicLong moveGenerationCount = new AtomicLong();
	private final AtomicLong generatedMoveCount = new AtomicLong();
	private final AtomicLong movePlayedCount = new AtomicLong();
	private long startMs = System.currentTimeMillis();
	
	public void clear() {
		evalCount.set(0);
		moveGenerationCount.set(0);
		generatedMoveCount.set(0);
		movePlayedCount.set(0);
		startMs = System.currentTimeMillis();
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
	
	public void movePlayed() {
		movePlayedCount.incrementAndGet();
	}
	public void evaluationDone() {
		evalCount.incrementAndGet();
	}
	public void movesGenerated(int moveCount) {
		moveGenerationCount.incrementAndGet();
		generatedMoveCount.addAndGet(moveCount);
	}
	public long getDurationMs() {
		return System.currentTimeMillis()-startMs;
	}
	@Override
	public String toString() {
		return "SearchStatistics [moveGenerationCount=" + moveGenerationCount + ", generatedMoveCount="
				+ generatedMoveCount + ", movePlayedCount=" + movePlayedCount + ", evalCount=" + evalCount + "]";
	}
}