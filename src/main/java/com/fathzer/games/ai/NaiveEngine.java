package com.fathzer.games.ai;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;

public class NaiveEngine<M,B extends MoveGenerator<M>> implements Function<B, M> {
	@SuppressWarnings("java:S2245") //Ignores Sonar security hot spot, here Random is safe 
	private static final Random RND = new Random();
	private final ToIntFunction<MoveGenerator<M>> evaluator;
	
	private final MoveGenerator<M> board;

	public NaiveEngine (MoveGenerator<M> board, ToIntFunction<MoveGenerator<M>> evaluator) {
		this.board = board;
		this.evaluator = evaluator;
	}
	
	@Override
	public M apply(B board) {
		List<M> possibleMoves = board.getLegalMoves();
		List<EvaluatedMove<M>> moves = IntStream.range(0, possibleMoves.size()).mapToObj(i -> {
			final M mv = possibleMoves.get(i);
			return new EvaluatedMove<>(mv, Evaluation.score(evaluate(mv)));
		}).sorted().toList();
		if (moves.isEmpty()) {
			return null;
		}
		final double best = moves.get(0).getScore();
		List<M> bestMoves = moves.stream().filter(m -> m.getScore()==best).map(EvaluatedMove::getMove).toList();
		return bestMoves.get(RND.nextInt(bestMoves.size()));
	}
	
	private int evaluate(M move) {
		// Play the evaluated move 
		this.board.makeMove(move, MoveConfidence.LEGAL);
		try {
			// Gets the opponent responses
			final List<M> moves = this.board.getLegalMoves();
			//FIXME Does not work in mat and draw situations
			int max = 0;
			for (int i = 0; i < moves.size(); i++) {
				// For all opponent responses
				int value = evaluateOpponentMove(moves.get(i));
				if (value>max) {
					max = value;
				}
			}
			return max;
		} finally {
			this.board.unmakeMove();
		}
	}
	
	private int evaluateOpponentMove (M oppMove) {
		// Play the response and evaluate the obtained board
		board.makeMove(oppMove, MoveConfidence.LEGAL);
		try {
			return evaluator.applyAsInt(board);
		} finally {
			board.unmakeMove();
		}
	}
}
