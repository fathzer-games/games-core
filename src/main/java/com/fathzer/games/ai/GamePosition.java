package com.fathzer.games.ai;

import com.fathzer.games.MoveGenerator;

/** A game position that can do everything a {@link MoveGenerator} can do and can be statically evaluated.
 * <br>This is the minimal requirement to use @link {@link AI} implementations.
 */
public interface GamePosition<M> extends MoveGenerator<M> {
    /**
     * Evaluates this position <strong>for the current player</strong> after a move.
     * <br>The greatest the value is, the better the position of the current player is.
     * <br>This value should always be less than the one returned by {@link #getWinScore(int)} 
     * @return The evaluation of the position for the current player
     * @see #getWinScore(int)
     */
	int evaluate();
	
    /** Gets the score obtained for a win after nbMoves moves.
     * <br>The default value is Short.MAX_VALUE - nbMoves
     * @param nbMoves The number of moves needed to win.
     * @return a positive int &gt; to any value returned by {@link #evaluate()}
     */
	default int getWinScore(int nbMoves) {
		return Short.MAX_VALUE-nbMoves;
	}

	/** The inverse function of {@link #getWinScore(int)}
	 * @param winScore a score returned by {@link #getWinScore(int)} 
	 * @return The number of moves passed to {@link #getWinScore(int)}
	 */
	default int getNbMovesToWin(int winScore) {
		return Short.MAX_VALUE-winScore;
	}
}
