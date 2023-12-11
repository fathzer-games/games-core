package com.fathzer.games.ai.evaluation;

import com.fathzer.games.Color;
import com.fathzer.games.util.exec.Forkable;

/** A class that can evaluate a game position.
 * <br>This class supports <a href="https://www.chessprogramming.org/Incremental_Updates">incremental evaluator</a>.
 * Nevertheless, implementing incremental evaluation can be a complex (but very useful) task. If you prefer not to implement incremental evaluation,
 * you can ignore {@link #fork()} {@link #prepareMove(Object, Object)}, {@link #commitMove()} and {@link #unmakeMove()} methods that does nothing by default.
 * @param <B> The type of the game position
 * @param <M> The type of a move
 */
public interface Evaluator<M, B> extends Forkable<Evaluator<M, B>> {
	/** Sets the point of view from which the evaluation should be made. 
	 * @param color The color from which the evaluation is made, null to evaluate the position from the point of view of the current player.
	 */
	void setViewPoint(Color color);
	
	default void prepareMove(B board, M move) {
		// By default, the evaluator is not incremental
	}
	default void commitMove() {
		// By default, the evaluator is not incremental
	}
	default void unmakeMove() {
		// By default, the evaluator is not incremental
	}
	default Evaluator<M, B> fork() {
		return this;
	}
	
	/** Evaluates a board's position.
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
	
	default boolean isWinLooseScore(int score, int maxDepth) {
		return getNbHalfMovesToWin(Math.abs(score)) <= maxDepth;
	}
	
	default Evaluation toEvaluation(int score, int maxDepth) {
		if (isWinLooseScore(score, maxDepth)) {
			final int endCount = (getNbHalfMovesToWin(Math.abs(score))+1)/2;
			return score>0 ? Evaluation.win(endCount,score) : Evaluation.loose(endCount,score); 
		} else {
			return Evaluation.score(score);
		}
	}
}
