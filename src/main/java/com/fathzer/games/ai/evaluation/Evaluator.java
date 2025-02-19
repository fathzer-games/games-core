package com.fathzer.games.ai.evaluation;

import com.fathzer.games.util.exec.Forkable;

/** A class that can evaluate a game position.
 * <br>This class supports <a href="https://www.chessprogramming.org/Incremental_Updates">incremental evaluator</a>.
 * Nevertheless, implementing incremental evaluation can be a complex (but very useful) task. If you prefer not to implement incremental evaluation,
 * you can implement the simpler {@link StaticEvaluator} interface. It provides default working implementation for incremental related methods.
 * @param <B> The type of the game position
 * @param <M> The type of a move
 */
public interface Evaluator<M, B> extends Forkable<Evaluator<M, B>> {
	/** Initializes the evaluator with a board.
	 * <br>The default implementation does nothing. It is the right thing to do for an evaluator that is not incremental.
	 * @param board The board. 
	 */
	void init(B board);
	
	/** Prepares the evaluation update before a move is done.
	 * @param board The board in its state before the move.
	 * @param move the move that will be played.
	 * @see #commitMove()
	 */
	void prepareMove(B board, M move);
	
	/** Commits the evaluation update previously prepared by {@link #prepareMove(Object, Object)}
	 */
	void commitMove();
	
	/** Reverts the evaluation to the state it had before the last not unmade move was committed. 
	 */
	void unmakeMove();
	
	/** Evaluates a board's position from the point of view of the player who should make the next move.
	 * @return An integer
	 */
	int evaluate(B board);
	
    /** Gets the score obtained for a win after a number of half moves.
     * <br>The default value is Short.MAX_VALUE - nbHalfMoves
     * @param nbHalfMoves The number of half moves needed to win.
     * @return a positive int &gt; to any value returned by {@link #evaluate(Object)}
     */
	default int getWinScore(int nbHalfMoves) {
		return Short.MAX_VALUE-nbHalfMoves;
	}

	/** The inverse function of {@link #getWinScore(int)}
	 * @param winScore a positive score returned by {@link #getWinScore(int)} 
	 * @return The number of half moves passed to {@link #getWinScore(int)}
	 */
	default int getNbHalfMovesToWin(int winScore) {
		return Short.MAX_VALUE-Math.abs(winScore);
	}
	
	/** Checks whether the score is a win or loose.
	 * @param score a score
	 * @return true if the score is a win or a loose, false if it is an evaluation.
	 * <br>The default implementation returns true if score's absolute value is &gt; Short.MAX_VALUE-256
	 */
	default boolean isWinLooseScore(int score) {
		return Math.abs(score) > Short.MAX_VALUE-256;
	}
	
	default Evaluation toEvaluation(int score) {
		if (isWinLooseScore(score)) {
			final int endCount = (getNbHalfMovesToWin(Math.abs(score))+1)/2;
			return score>0 ? Evaluation.win(endCount,score) : Evaluation.loose(endCount,score); 
		} else {
			return Evaluation.score(score);
		}
	}
}
