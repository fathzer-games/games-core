package com.fathzer.games.ai.evaluation;

import com.fathzer.games.MoveGenerator;

/** A <a href="https://en.wikipedia.org/wiki/Zero-sum_game">zero-sum game</a> evaluation functions.
 * <br>It simplifies the implementation by automatically converting evaluations
 * from the white player perspective to the black player perspective if needed.
 * @param <M> The type of moves
 * @param <B> The type of the chess move generator
 */
public interface ZeroSumEvaluator<M, B extends MoveGenerator<M>> extends Evaluator<M, B> {
	/** Evaluates the board from the white player point of view.
	 * @param board The board to evaluate
	 * @return an integer.
	 */
	int evaluateAsWhite(B board);
	
	@Override
	default int evaluate(B board) {
		final int points = evaluateAsWhite(board);
		return board.isWhiteToMove() ? points : - points;
	}
}
