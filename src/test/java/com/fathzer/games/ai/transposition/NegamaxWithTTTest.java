package com.fathzer.games.ai.transposition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.DepthFirstSearchParameters;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.evaluation.Evaluation.Type;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.exec.ExecutionContext;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

class NegamaxWithTTTest {
	@Test
	void test2MatsIn4() {
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator("8/4k3/8/R7/8/8/8/4K2R w K - 0 1", BasicMoveComparator::new);
		final Evaluator<Move, ChessLibMoveGenerator> basicEvaluator = new BasicEvaluator();
		final SearchContext<Move, ChessLibMoveGenerator> sc = SearchContext.get(mg, () -> basicEvaluator);
		try (ExecutionContext<SearchContext<Move, ChessLibMoveGenerator>> exec = ExecutionContext.get(1, sc)) {
			Negamax<Move, ChessLibMoveGenerator> ai = new Negamax<>(exec);
			final TT tt = new TT(16, SizeUnit.MB);
			ai.setTranspositonTable(tt);
			final Move a5a6 = new Move(Square.A5, Square.A6);
			final Move h1h6 = new Move(Square.H1, Square.H6);
			List<EvaluatedMove<Move>> eval = ai.getBestMoves(Arrays.asList(a5a6, h1h6), new DepthFirstSearchParameters(8,2,0)).getCut();
			assertEquals(2, eval.size());
			for (EvaluatedMove<Move> e : eval) {
				assertEquals(Type.WIN, e.getEvaluation().getType());
				assertEquals(4, e.getEvaluation().getCountToEnd());
			}
		}
	}
}
