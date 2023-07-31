package com.fathzer.games.ai;

public class AlphaBetaState {
	private final int depth;
	private final int alpha;
	private final int beta;
	private int alphaUpdated;
	private int betaUpdated;
	private int value;
	private boolean valueSet;
	private boolean alphaBetaUpdated;
	
	public AlphaBetaState(int depth, int alpha, int beta) {
		this.depth = depth;
		this.alpha = alpha;
		this.beta = beta;
	}

	public int getAlpha() {
		return alpha;
	}

	public int getBeta() {
		return beta;
	}

	public int getDepth() {
		return depth;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.valueSet = true;
		this.value = value;
	}

	public boolean isValueSet() {
		return valueSet;
	}
	
	public void updateAlphaBeta(int alpha, int beta) {
		this.alphaUpdated = alpha;
		this.betaUpdated = beta;
	}
	
	public int getAlphaUpdated() {
		return alphaUpdated;
	}

	public int getBetaUpdated() {
		return betaUpdated;
	}

	public boolean isAlphaBetaUpdated() {
		return alphaBetaUpdated;
	}
}