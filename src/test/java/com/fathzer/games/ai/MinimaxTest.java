package com.fathzer.games.ai;

import static com.fathzer.games.ai.AiType.*;
import static com.github.bhlangonijr.chesslib.Square.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.DummyEvaluator;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.ai.transposition.TT;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.exec.ContextualizedExecutor;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.MultiThreadsContext;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

class MinimaxTest {
	static class ChessLibTest {
		private final Board board;
		private final int depth;
		
		ChessLibTest(String fen, int depth) {
			board = new Board();
			board.loadFromFen(fen);
			this.depth = depth;
		}

		List<EvaluatedMove<Move>> search(AiType ai, boolean noQuiece) {
			return search(ai, new SearchParameters(depth, Integer.MAX_VALUE, 0), noQuiece);
		}

		List<EvaluatedMove<Move>> search(AiType aiType, SearchParameters params, boolean noQuiece) {
			final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(board, Minimax==aiType?x->null:BasicMoveComparator::new);
			mg.setNoQuiesce(noQuiece);
			final Evaluator<Move, ChessLibMoveGenerator> ev = new BasicEvaluator();
			final SearchContext<Move, ChessLibMoveGenerator> ctx = new SearchContext<>(mg, ev);
			Function<ExecutionContext<SearchContext<Move, ChessLibMoveGenerator>>, AbstractAI<Move, ChessLibMoveGenerator>> aiBuilder = aiType.getAiBuilder();
			if (aiType==Negamax || aiType==Negamax3) {
				aiBuilder = aiBuilder.andThen(ai -> {
					((Negamax<Move, ChessLibMoveGenerator>)ai).setTranspositonTable(new TT(16, SizeUnit.MB));
					return ai;
				});
			}
			return MinimaxTest.search(ctx, aiBuilder, params);
		}
		
		int getWinScore(int nbHalfMoves) {
			return new DummyEvaluator().getWinScore(nbHalfMoves);
		}
	}
	
	@Test
	void testQuiesce() {
		ChessLibTest t = new ChessLibTest("3n1rk1/1pp2p1p/2r2bq1/2P1p1p1/3pP3/PQ1P2PP/1R3PB1/2B2RK1 w - - 2 26", 1);
		List<EvaluatedMove<Move>> search = t.search(Negamax, false);
		assertEquals(-800, getScore(search, new Move(B3, F7)));
		assertEquals(-600, getScore(search, new Move(B3, B7)));
	}

	private int getScore(List<EvaluatedMove<Move>> search, Move move) {
		return search.stream().filter(em -> move.equals(em.getContent())).findAny().get().getScore();
	}

	@Test
	void matIn1ForBlacks() {
		ChessLibTest t = new ChessLibTest("1R6/8/8/7R/k7/ppp1p3/r2bP3/1K6 b - - 6 5", 2);
		final List<EvaluatedMove<Move>> expected = Arrays.asList(
				new EvaluatedMove<>(new Move(C3,C2), Evaluation.win(1, t.getWinScore(1))),
				new EvaluatedMove<>(new Move(A2,B2), Evaluation.score(100)),
				new EvaluatedMove<>(new Move(A2,C2), Evaluation.score(0)),
				new EvaluatedMove<>(new Move(D2,E1), Evaluation.score(0)),
				new EvaluatedMove<>(new Move(D2,C1), Evaluation.score(-200)),
				new EvaluatedMove<>(new Move(B3,B2), Evaluation.score(-400)),
				new EvaluatedMove<>(new Move(A2,A1), Evaluation.score(-400))
				);
		assertEquals(expected, t.search(Minimax, true));
		assertEquals(expected, t.search(AlphaBeta, true));
		assertEquals(expected, t.search(Negamax, true));
		assertEquals(expected, t.search(Negamax3, true));
	}

	@Test
	void matIn2ForWhites() {
		ChessLibTest t = new ChessLibTest("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1", 4);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(B3,A1), Evaluation.win(2, t.getWinScore(3)));
		final List<EvaluatedMove<Move>> search = t.search(Negamax, true);
		assertEquals(expected, search.get(0));
		assertTrue(search.get(1).getScore()<search.get(0).getScore());
		assertEquals(search, t.search(Minimax, true));
		assertEquals(search, t.search(AlphaBeta, true));
		assertEquals(search, t.search(Negamax3, true));
	}

	@Test
	void threeMatsIn1ForWhites() {
		ChessLibTest t = new ChessLibTest("7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5", 2);
		final List<EvaluatedMove<Move>> search = t.search(Negamax, true);
		final Evaluation max = search.get(0).getEvaluation();
		search.stream().limit(3).forEach(m -> assertEquals(max, m.getEvaluation()));
		assertNotEquals(max, search.get(3).getEvaluation());
		assertEquals(search, t.search(Minimax, true));
		assertEquals(search, t.search(AlphaBeta, true));
		assertEquals(search, t.search(Negamax3, true));
	}

	@Test
	void matIn2ForBlacks() {
		ChessLibTest t = new ChessLibTest("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1", 4);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(G6,H6), Evaluation.win(2, t.getWinScore(3)));
		final List<EvaluatedMove<Move>> search = t.search(Negamax, true);
		assertEquals(expected, search.get(0));
		assertTrue(search.get(1).getScore()<search.get(0).getScore());
		assertEquals(search, t.search(Minimax, true));
		assertEquals(search, t.search(AlphaBeta, true));
		assertEquals(search, t.search(Negamax3, true));
	}
	
	@Test
	void matIn3ForWhites() {
		ChessLibTest t = new ChessLibTest("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1", 6);
		SearchParameters params = new SearchParameters(6);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(D1,D7), Evaluation.win(3, t.getWinScore(5)));
		matIn3forWhitesAssert(expected, t.search(Negamax, params, true));
		matIn3forWhitesAssert(expected, t.search(Negamax3, params, true));
		matIn3forWhitesAssert(expected, t.search(AlphaBeta, params, true));
	}
	
	<M> void matIn3forWhitesAssert(EvaluatedMove<M> expectedBest, List<EvaluatedMove<M>> moves) {
		assertEquals(expectedBest, moves.get(0));
		assertTrue(moves.get(1).getScore()<moves.get(0).getScore());
	}

	@Test
	void noMatSituations() {
		ChessLibTest t = new ChessLibTest("4k3/r1q2bpr/2n1np2/8/8/P4B1N/3R1QPP/RN2K3 w Q - 0 1", 2);
		EvaluatedMove<Move> best = new EvaluatedMove<>(new Move(F3,C6), Evaluation.score(100));
		EvaluatedMove<Move> second = new EvaluatedMove<>(new Move(H3,G1), Evaluation.score(0));
		noMatAssert(best, second, t.search(Minimax, true));
		noMatAssert(best, second, t.search(AlphaBeta, true));
		noMatAssert(best, second, t.search(Negamax3, true));
		noMatAssert(best, second, t.search(Negamax, true));

		t = new ChessLibTest("4k3/r1q2bpr/2n1np2/8/8/P4B1N/3R1QPP/RN2K3 b Q - 0 1", 2);
		best = new EvaluatedMove<>(new Move(C7,E5), Evaluation.score(-100));
		second = new EvaluatedMove<>(new Move(H7,H3), Evaluation.score(-300));
		noMatAssert(best, second, t.search(Minimax, true));
		noMatAssert(best, second, t.search(AlphaBeta, true));
		noMatAssert(best, second, t.search(Negamax3, true));
		noMatAssert(best, second, t.search(Negamax, true));
	}
	
	<M> void noMatAssert(EvaluatedMove<M> expectedBest, EvaluatedMove<M> expectedSecond, List<EvaluatedMove<M>> moves) {
		assertEquals(expectedBest, moves.get(0));
		assertEquals(expectedSecond, moves.get(1));
	}

	@Test
	void bug20240127() {
		ChessLibTest t = new ChessLibTest("3n1rk1/1pp2p1p/2r2bq1/2P1p1p1/3pP3/PQ1P2PP/1B3PB1/R4RK1 b - - 2 26", 1);
		EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(G6,E4), Evaluation.score(100));
		assertContains(expected, t.search(Minimax, true));
		assertContains(expected, t.search(AlphaBeta, true));
		assertContains(expected, t.search(Negamax, true));
		assertContains(expected, t.search(Negamax3, true));
		
		t = new ChessLibTest("3n1rk1/1pp2p1p/2r2bq1/2P1p1p1/3pP3/PQ1P2PP/1B3PB1/R4RK1 w - - 2 26", 1);
		expected = new EvaluatedMove<>(new Move(B3,F7), Evaluation.score(100));
		assertContains(expected, t.search(Minimax, true));
		assertContains(expected, t.search(AlphaBeta, true));
		assertContains(expected, t.search(Negamax, true));
		assertContains(expected, t.search(Negamax3, true));
	}
	
	<M> void assertContains(EvaluatedMove<M> expected, List<EvaluatedMove<M>> moves) {
		Optional<EvaluatedMove<M>> result = moves.stream().filter(em -> em.getContent().equals(expected.getContent())).findAny();
		if (result.isEmpty()) {
			fail("Unable to find "+expected+" in results");
		} else {
			assertEquals(expected.getScore(), result.get().getScore());
		}
	}

	private static <M, B extends MoveGenerator<M>> List<EvaluatedMove<M>> search(SearchContext<M, B> ctx, Function<ExecutionContext<SearchContext<M, B>>, AbstractAI<M, B>> aiBuilder, SearchParameters params) {
		try (ExecutionContext<SearchContext<M, B>> context = new MultiThreadsContext<>(ctx, new ContextualizedExecutor<>(4))) {
			final AbstractAI<M, B> ai = aiBuilder.apply(context);
			final List<EvaluatedMove<M>> list = ai.getBestMoves(params).getList();
			if (ai instanceof Negamax) {
				for (EvaluatedMove<M> ev:list) {
					ev.setPvBuilder(m -> ((Negamax<M, B>)ai).getTranspositionTable().collectPV(ctx.getGamePosition(), m, params.getDepth()));
				}
			}
			return list;
		}
	}
}
