package com.fathzer.games.ai.experimental;

public class TreeSearchState<M> {
	public final int depth;
	public final int who;
	public int alphaOrigin;
	public int betaOrigin;
	public int alpha;
	public int beta;
	public int value;
    public M bestMove;
	public M lastMove;
	
	
	public TreeSearchState(int depth, int alpha, int beta, int who) {
		this.depth = depth;
		this.alphaOrigin = alpha;
		this.betaOrigin = beta;
		this.alpha = alpha;
		this.beta = beta;
		this.who = who;
	}
}
