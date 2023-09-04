package com.fathzer.games.ai;

import static com.github.bhlangonijr.chesslib.Square.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.exec.ExecutionContext;
import com.fathzer.games.ai.exec.MultiThreadsContext;
import com.fathzer.games.ai.experimental.Negamax3;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.ContextualizedExecutor;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;

class MinimaxTest {
	static ChessLibMoveGenerator fromFEN(String fen) {
		Board board = new Board();
		board.loadFromFen(fen);
		return new ChessLibMoveGenerator(board);
	}
	
	static class ChessLibTest {
		private final Evaluator<ChessLibMoveGenerator> ev;
		private final ChessLibMoveGenerator mg;
		private final Supplier<ChessLibMoveGenerator> supplier;
		private final int depth;
		
		ChessLibTest(String fen, int depth) {
			mg = fromFEN(fen);
			ev = new BasicEvaluator();
			ev.setViewPoint(mg.getBoard().getSideToMove()==Side.WHITE ? Color.WHITE : Color.BLACK);
			supplier = () -> new ChessLibMoveGenerator(mg.getBoard());
			this.depth = depth;
		}

		List<EvaluatedMove<Move>> search(BiFunction<ExecutionContext<Move, ChessLibMoveGenerator>, Evaluator<ChessLibMoveGenerator>, AbstractAI<Move, ChessLibMoveGenerator>> aiBuilder) {
			return MinimaxTest.search(supplier, depth, ev, aiBuilder);
		}

		public Evaluator<ChessLibMoveGenerator> getEvaluator() {
			return ev;
		}
	}

	@Test
	void test() {
		ChessLibTest t = new ChessLibTest("1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5", 2);
		final List<EvaluatedMove<Move>> expected = Arrays.asList(
				new EvaluatedMove<>(new Move(C3,C2), Evaluation.win(1, t.getEvaluator().getWinScore(1))),
				new EvaluatedMove<>(new Move(A2,B2), Evaluation.score(100)),
				new EvaluatedMove<>(new Move(A2,C2), Evaluation.score(0)),
				new EvaluatedMove<>(new Move(D2,E1), Evaluation.score(0)),
				new EvaluatedMove<>(new Move(D2,C1), Evaluation.score(-200)),
				new EvaluatedMove<>(new Move(B3,B2), Evaluation.score(-400)),
				new EvaluatedMove<>(new Move(A2,A1), Evaluation.score(-400))
				);
		assertEquals(expected, t.search(Minimax::new));
		assertEquals(expected, t.search(AlphaBeta::new));
		assertEquals(expected, t.search(Negamax::new));
		assertEquals(expected, t.search(Negamax3::new));
	}
	
	private static <M, B extends MoveGenerator<M>> List<EvaluatedMove<M>> search(Supplier<B> supplier, int depth, Evaluator<B> evaluator, BiFunction<ExecutionContext<M, B>, Evaluator<B>, AbstractAI<M, B>> aiBuilder) {
		try (ExecutionContext<M, B> context = new MultiThreadsContext<>(supplier, new ContextualizedExecutor<>(4))) {
			AbstractAI<M, B> ai = aiBuilder.apply(context, evaluator);
			return ai.getBestMoves(new SearchParameters(depth, Integer.MAX_VALUE, 0)).getList();
		}
	}
}
