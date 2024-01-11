package com.fathzer.games.ai.evaluation;

import static com.fathzer.games.Color.WHITE;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;

/** An abstract evaluator that helps implementing <a href="https://en.wikipedia.org/wiki/Zero-sum_game">zero-sum game</a> evaluation functions.
 * <br>It deals with the point of view property. The subclasses should implement the evaluator from the white player perspective,
 * and this class automatically negate the evaluation if required.
 * @param <M> The type of moves
 * @param <B> The type of the chess move generator
 */
public abstract class AbstractEvaluator<M, B extends MoveGenerator<M>> implements ColorSensitiveEvaluator<B> {
	/** The evaluator point of view (1 for white, -1 for black, 0 for current player.
	 */
	protected int viewPoint;

	@Override
	public void setViewPoint(Color color) {
		if (color==null) {
			this.viewPoint = 0;
		} else {
			this.viewPoint = color==WHITE ? 1 : -1;
		}
	}
	
	/** Gets the evaluation of a board, from the white point of view.
	 * @param board The board to evaluate
	 * @return an integer
	 */
	protected abstract int evaluateAsWhite(B board);
	
	@Override
	public int evaluate(B board) {
		int points = evaluateAsWhite(board);
		if ((viewPoint==0 && !board.isWhiteToMove()) || viewPoint<0) {
			points = -points;
		}
		return points;
	}
}
