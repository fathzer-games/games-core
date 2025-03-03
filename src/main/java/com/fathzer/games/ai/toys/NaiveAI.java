package com.fathzer.games.ai.toys;

import java.util.List;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.games.ai.AI;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;

/**
 * A very basic {@link AI} that that gives to a move the best score according to a given evaluator after the opponent played.
 * <br>Of course in complex games like chess, the move chosen by this engine may be far for the best one due to
 * <a href="https://en.wikipedia.org/wiki/Horizon_effect">horizon effect</a>.
 * @param <M> The type of moves
 * @param <B> The type of board
 */
public class NaiveAI<M,B extends MoveGenerator<M>> extends BasicAI<M, B> {
	private final Evaluator<M, B> evaluator;
	
	/** Constructor
	 * @param evaluator The evaluator to use
	 */
	public NaiveAI (B board, Evaluator<M, B> evaluator) {
		super(board);
		this.evaluator = evaluator;
	}
	
	@Override
	protected Evaluation getEvaluation(M move) {
		return evaluator.toEvaluation(evaluate(move));
	}

	int evaluate(M move) {
		// Play the evaluated move
		evaluator.prepareMove(board, move);
		board.makeMove(move, MoveConfidence.LEGAL);
		try {
			evaluator.commitMove();
			// Gets the opponent responses
			final List<M> moves = board.getLegalMoves();
			if (moves.isEmpty()) {
				// End of game
				final Status status = board.getEndGameStatus();
				return status == Status.DRAW ? 0 : evaluator.getWinScore(1);
			}
			int min = 0;
			for (M m : moves) {
				// For all opponent responses
				final int value = evaluateOpponentMove(board, m);
				if (value<min) {
					min = value;
				}
			}
			return min;
		} finally {
			board.unmakeMove();
			evaluator.unmakeMove();
		}
	}
	
	private int evaluateOpponentMove (B board, M oppMove) {
		// Play the response and evaluate the obtained board
		board.makeMove(oppMove, MoveConfidence.LEGAL);
		try {
			return evaluator.evaluate(board);
		} finally {
			board.unmakeMove();
		}
	}
}
