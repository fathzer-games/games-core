package com.fathzer.games.ai.experimental;

/**
 * A search state.
 * <br>It is used to store the search state of a <a href="https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning">alpha-beta or Negamax search</a> at a specific depth.
 * <br><b>WARNING:</b> This class is not thread-safe.
 * @param <M> The type of the moves
 */
public class TreeSearchState<M> {
	final int depth;
	int alphaOrigin;
	int betaOrigin;
	int alpha;
	int beta;
	int value;
    M bestMove;
	M lastMove;
	
	/**
	 * Creates a new instance.
	 * @param depth The depth
	 * @param alpha The alpha value
	 * @param beta The beta value
	 */
	TreeSearchState(int depth, int alpha, int beta) {
		this.depth = depth;
		this.alphaOrigin = alpha;
		this.betaOrigin = beta;
		this.alpha = alpha;
		this.beta = beta;
	}

	/** Gets the current depth
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/** Gets value of alpha when the search enters at this depth
	 * @return the alphaOrigin
	 */
	public int getAlphaOrigin() {
		return alphaOrigin;
	}

	/** Gets value of beta when the search enters at this depth
	 * @return the betaOrigin
	 */
	public int getBetaOrigin() {
		return betaOrigin;
	}

	/** Gets current alpha value
	 * @return the alpha
	 */
	public int getAlpha() {
		return alpha;
	}

	/** Gets current beta value
	 * @return the beta
	 */
	public int getBeta() {
		return beta;
	}

	/** Gets the current evaluation
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/** Gets the current best move found at this depth
	 * @return the bestMove
	 */
	public M getBestMove() {
		return bestMove;
	}

	/** Gets the last move played at this depth
	 * @return the lastMove, null if no move has been made
	 */
	public M getLastMove() {
		return lastMove;
	}
}
