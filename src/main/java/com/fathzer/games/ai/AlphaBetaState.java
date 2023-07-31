package com.fathzer.games.ai;

public class AlphaBetaState {
	private final int depth;
	private int alpha;
	private int beta;
	private int value;
	private boolean valueSet;
	
	public AlphaBetaState(int depth, int alpha, int beta) {
		this.depth = depth;
		this.alpha = alpha;
		this.beta = beta;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getBeta() {
		return beta;
	}

	public void setBeta(int beta) {
		this.beta = beta;
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
}