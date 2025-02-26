package com.fathzer.games.ai;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.Status;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;

/**
 * A very basic engine that chooses the move with the highest score according to a given evaluator after the opponent played.
 * <br>Of course in complex games like chess, the move chosen by this engine may be far for the best one due to
 * <a href="https://en.wikipedia.org/wiki/Horizon_effect">horizon effect</a>.
 * @param <M> The type of moves
 * @param <B> The type of board
 */
public class NaiveEngine<M,B extends MoveGenerator<M>> implements Function<B, M> {
	@SuppressWarnings("java:S2245") //Ignores Sonar security hot spot, here Random is safe 
	private static final Random RND = new Random();
	private final Evaluator<M, B> evaluator;
	
	/** Constructor
	 * @param evaluator The evaluator to use
	 */
	public NaiveEngine (Evaluator<M, B> evaluator) {
		this.evaluator = evaluator;
	}
	
	/** Gets the chosen move
	 * @return the chosen move, or null if no moves are possible in the position.
	 */
	@Override
	public M apply(B board) {
		List<M> possibleMoves = board.getLegalMoves();
		List<EvaluatedMove<M>> moves = possibleMoves.stream().
				map(m -> new EvaluatedMove<>(m, Evaluation.score(evaluate(board, m)))).sorted().toList();
		if (moves.isEmpty()) {
			return null;
		}
		final double best = moves.get(0).getScore();
		List<M> bestMoves = moves.stream().filter(m -> m.getScore()==best).map(EvaluatedMove::getMove).toList();
		return bestMoves.get(RND.nextInt(bestMoves.size()));
	}
	
	int evaluate(B board, M move) {
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
