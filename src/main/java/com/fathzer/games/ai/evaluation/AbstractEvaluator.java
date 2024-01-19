package com.fathzer.games.ai.evaluation;

import com.fathzer.games.MoveGenerator;

/** An abstract evaluator that helps implementing <a href="https://en.wikipedia.org/wiki/Zero-sum_game">zero-sum game</a> evaluation functions.
 * <br>It deals with the point of view property. The subclasses should implement the evaluator from the white player perspective,
 * and this class automatically negate the evaluation if required.
 * @param <M> The type of moves
 * @param <B> The type of the chess move generator
 */
public abstract class AbstractEvaluator<M, B extends MoveGenerator<M>> implements ColorSensitiveEvaluator<B> {
	/** Gets the evaluation of a board, from the white point of view.
	 * @param board The board to evaluate
	 * @return an integer
	 */
	protected abstract int evaluateAsWhite(B board);
	
	@Override
	public int evaluate(B board) {
		final int points = evaluateAsWhite(board);
		return board.isWhiteToMove() ? points : - points;
	}
}
