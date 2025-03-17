package com.fathzer.games.ai.transposition;

/** 
 * The state of an <a href="https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning">alpha-beta or Negamax search</a> search.
 * @param <M> The type of the move
 */
public class AlphaBetaState<M> {
	private final int depth;
	private final int alpha;
	private final int beta;
	private int alphaUpdated;
	private int betaUpdated;
	private int value;
	private boolean valueSet;
	private boolean alphaBetaUpdated;
	private M bestMove;
	
	/** Constructor.
	 * @param depth The depth
	 * @param alpha The alpha value
	 * @param beta The beta value
	 */
	public AlphaBetaState(int depth, int alpha, int beta) {
		this.depth = depth;
		this.alpha = alpha;
		this.beta = beta;
	}

	/** Gets the depth.
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/** Gets the alpha value.
	 * <br>{@link #updateAlphaBeta(int, int)} has no effect on this value.
	 * @return the alpha value
	 * @see #getAlphaUpdated()
	 */
	public int getAlpha() {
		return alpha;
	}

	/** Gets the beta value.
	 * <br>{@link #updateAlphaBeta(int, int)} has no effect on this value.
	 * @return the beta value
	 * @see #getBetaUpdated()
	 */
	public int getBeta() {
		return beta;
	}

	/** Gets the value.
	 * @return the value. If {@link #isValueSet()} was not called, the returned value is unspecified.
	 * @see #isValueSet()
	 */
	public int getValue() {
		return value;
	}

	/** Sets the value.
	 * @param value The value
	 */
	public void setValue(int value) {
		this.valueSet = true;
		this.value = value;
	}

	/** Checks if the value has been set.
	 * @return true if the value has been set, false otherwise
	 */
	public boolean isValueSet() {
		return valueSet;
	}
	
	/** Updates the alpha and beta values.
	 * <br>This method has no effect on the {@link #getAlpha()} and {@link #getBeta()} values but only the {@link #getAlphaUpdated()} and {@link #getBetaUpdated()} values.
	 * @param alpha The new alpha value
	 * @param beta The new beta value
	 */
	public void updateAlphaBeta(int alpha, int beta) {
		this.alphaUpdated = alpha;
		this.betaUpdated = beta;
	}
	
	/** Gets the last updated alpha value.
	 * @return the last updated alpha value (unspecified if {@link #isAlphaBetaUpdated()} was not called)
	 */
	public int getAlphaUpdated() {
		return alphaUpdated;
	}

	/** Gets the last updated beta value.
	 * @return the last updated beta value (unspecified if {@link #isAlphaBetaUpdated()} was not called)
	 */
	public int getBetaUpdated() {
		return betaUpdated;
	}

	/** Checks if the alpha and beta values have been updated.
	 * @return true if the alpha and beta values have been updated, false otherwise
	 */
	public boolean isAlphaBetaUpdated() {
		return alphaBetaUpdated;
	}

	/** Gets the best move.
	 * @return the best move or null if {@link #setBestMove(M)} was not called
	 */
	public M getBestMove() {
		return bestMove;
	}

	/** Sets the best move.
	 * @param bestMove The best move
	 */
	public void setBestMove(M bestMove) {
		this.bestMove = bestMove;
	}
}