package com.fathzer.games.ai;

public final class AlphaBetaState {
	private int depth;
	private int alpha;
	private int beta;
	
	AlphaBetaState() {
		// Nothing to do
	}
	
	void set(int depth, int alpha, int beta) {
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
}