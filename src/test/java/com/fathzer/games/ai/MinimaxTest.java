package com.fathzer.games.ai;

import static com.fathzer.games.ai.AiType.*;
import static com.github.bhlangonijr.chesslib.Square.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.MoveGenerator.MoveConfidence;
import com.fathzer.games.ai.evaluation.DummyEvaluator;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.ai.evaluation.QuiesceEvaluator;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.ai.transposition.TT;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.exec.ContextualizedExecutor;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.MultiThreadsContext;
import com.github.bhlangonijr.chesslib.move.Move;

class MinimaxTest {
	static class SimpleQuiesce implements QuiesceEvaluator<Move, ChessLibMoveGenerator> {
		private boolean noQuiesce;
		
		private SimpleQuiesce() {
			super();
		}
		
		@Override
		public int evaluate(SearchContext<Move, ChessLibMoveGenerator> context, int depth, int alpha, int beta) {
			final SearchStatistics statistics = context.getStatistics();
			final int standPat = context.getEvaluator().evaluate(context.getGamePosition());
			statistics.evaluationDone();
			if (standPat>=beta) {
				return beta;
			}
			if (alpha < standPat) {
				alpha = standPat;
			}
			final List<Move> moves = getMoves(context);
	    	statistics.movesGenerated(moves.size());
	        for (Move move : moves) {
	            if (context.makeMove(move, MoveConfidence.PSEUDO_LEGAL)) {
	                statistics.movePlayed();
		            final int score = -evaluate(context, depth, -beta, -alpha);
		            context.unmakeMove();
		            if (score >= beta) {
		                return beta;
		            }
		            if (score > alpha) {
		            	alpha = score;
		            }
	            }
	        }
			return alpha;
		}

		private List<Move> getMoves(SearchContext<Move, ChessLibMoveGenerator> context) {
			return noQuiesce ? Collections.emptyList() : context.getGamePosition().getQuiesceMoves();
		}
	}
	
	static class ChessLibTest {
		private final String fen;
		private final int depth;
		
		ChessLibTest(String fen, int depth) {
			this.fen = fen;
			this.depth = depth;
		}

		List<EvaluatedMove<Move>> search(AiType ai, boolean noQuiece) {
			return search(ai, new SearchParameters(depth, Integer.MAX_VALUE, 0), noQuiece);
		}

		List<EvaluatedMove<Move>> search(AiType aiType, SearchParameters params, boolean noQuiece) {
			return search(aiType, params, noQuiece, null);
		}
		
		List<EvaluatedMove<Move>> search(AiType aiType, SearchParameters params, boolean noQuiece, List<Move> searchedMoves) {
			final ChessLibMoveGenerator mg = new ChessLibMoveGenerator(fen, MINIMAX==aiType?x->null:BasicMoveComparator::new);
			final Evaluator<Move, ChessLibMoveGenerator> ev = new BasicEvaluator();
			final SearchContext<Move, ChessLibMoveGenerator> ctx = SearchContext.get(mg, () -> ev);
			Function<ExecutionContext<SearchContext<Move, ChessLibMoveGenerator>>, AbstractAI<Move, ChessLibMoveGenerator>> aiBuilder = aiType.getAiBuilder();
			if (aiType==NEGAMAX || aiType==NEGAMAX_3) {
				aiBuilder = aiBuilder.andThen(ai -> {
					((Negamax<Move, ChessLibMoveGenerator>)ai).setTranspositonTable(new TT(16, SizeUnit.MB));
					if (!noQuiece) {
						((Negamax<Move, ChessLibMoveGenerator>)ai).setQuiesceEvaluator(new SimpleQuiesce());
					}
					return ai;
				});
			}
			return MinimaxTest.search(ctx, aiBuilder, params, searchedMoves);
		}

		int getWinScore(int nbHalfMoves) {
			return new DummyEvaluator<>().getWinScore(nbHalfMoves);
		}
	}
	
	@Test
	void testIllegalSearchedMoves() {
		ChessLibTest t = new ChessLibTest("r1bq1rk1/3n1ppp/p3p3/2bpP3/Np1B1P1P/7R/PPPQ2P1/2KR1B2 b - - 1 14", 1);
		final Move illegalMove = new Move(D3, D4);
		final Move legalMove = new Move(A6, A5);
		Predicate<AiType> f = ai -> {
			final List<EvaluatedMove<Move>> search = t.search(ai, new SearchParameters(2, Integer.MAX_VALUE, 0), false, Collections.singletonList(illegalMove));
			return getEvaluation(search, illegalMove).isEmpty();
		};
		assertTrue(f.test(NEGAMAX));
		assertTrue(f.test(NEGAMAX_3));
		assertTrue(f.test(ALPHA_BETA));
		assertTrue(f.test(MINIMAX));
		f = ai -> {
			final List<EvaluatedMove<Move>> search = t.search(ai, new SearchParameters(2, Integer.MAX_VALUE, 0), false, Arrays.asList(illegalMove, legalMove));
			return getEvaluation(search, legalMove).isPresent() && getEvaluation(search, illegalMove).isEmpty();
		};
		assertTrue(f.test(NEGAMAX));
		assertTrue(f.test(NEGAMAX_3));
		assertTrue(f.test(ALPHA_BETA));
		assertTrue(f.test(MINIMAX));
	}
	
	@Test
	void testQuiesce() {
		ChessLibTest t = new ChessLibTest("3n1rk1/1pp2p1p/2r2bq1/2P1p1p1/3pP3/PQ1P2PP/1R3PB1/2B2RK1 w - - 2 26", 1);
		List<EvaluatedMove<Move>> search = t.search(NEGAMAX, false);
		assertEquals(-800, getScore(search, new Move(B3, F7)));
		assertEquals(-600, getScore(search, new Move(B3, B7)));
		search = t.search(NEGAMAX_3, false);
		assertEquals(-800, getScore(search, new Move(B3, F7)));
		assertEquals(-600, getScore(search, new Move(B3, B7)));
	}

	private int getScore(List<EvaluatedMove<Move>> search, Move move) {
		return getEvaluation(search, move).get().getScore();
	}

	private Optional<EvaluatedMove<Move>> getEvaluation(List<EvaluatedMove<Move>> search, Move move) {
		return search.stream().filter(em -> move.equals(em.getMove())).findAny();
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
		assertEquals(expected, t.search(MINIMAX, true));
		assertEquals(expected, t.search(ALPHA_BETA, true));
		assertEquals(expected, t.search(NEGAMAX, true));
		assertEquals(expected, t.search(NEGAMAX_3, true));
	}

	@Test
	void matIn2ForWhites() {
		ChessLibTest t = new ChessLibTest("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1", 4);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(B3,A1), Evaluation.win(2, t.getWinScore(3)));
		final List<EvaluatedMove<Move>> search = t.search(NEGAMAX, true);
		assertEquals(expected, search.get(0));
		assertTrue(search.get(1).getScore()<search.get(0).getScore());
		assertEquals(search, t.search(MINIMAX, true));
		assertEquals(search, t.search(ALPHA_BETA, true));
		assertEquals(search, t.search(NEGAMAX_3, true));
	}

	@Test
	void threeMatsIn1ForWhites() {
		ChessLibTest t = new ChessLibTest("7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5", 2);
		final List<EvaluatedMove<Move>> search = t.search(NEGAMAX, true);
		final Evaluation max = search.get(0).getEvaluation();
		search.stream().limit(3).forEach(m -> assertEquals(max, m.getEvaluation()));
		assertNotEquals(max, search.get(3).getEvaluation());
		assertEquals(search, t.search(MINIMAX, true));
		assertEquals(search, t.search(ALPHA_BETA, true));
		assertEquals(search, t.search(NEGAMAX_3, true));
	}

	@Test
	void matIn2ForBlacks() {
		ChessLibTest t = new ChessLibTest("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1", 4);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(G6,H6), Evaluation.win(2, t.getWinScore(3)));
		final List<EvaluatedMove<Move>> search = t.search(NEGAMAX, true);
		assertEquals(expected, search.get(0));
		assertTrue(search.get(1).getScore()<search.get(0).getScore());
		assertEquals(search, t.search(MINIMAX, true));
		assertEquals(search, t.search(ALPHA_BETA, true));
		assertEquals(search, t.search(NEGAMAX_3, true));
	}
	
	@Test
	void matIn3ForWhites() {
		ChessLibTest t = new ChessLibTest("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1", 6);
		SearchParameters params = new SearchParameters(6);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(D1,D7), Evaluation.win(3, t.getWinScore(5)));
		matIn3forWhitesAssert(expected, t.search(NEGAMAX, params, true));
		matIn3forWhitesAssert(expected, t.search(NEGAMAX_3, params, true));
		matIn3forWhitesAssert(expected, t.search(ALPHA_BETA, params, true));
	}
	
	<M> void matIn3forWhitesAssert(EvaluatedMove<M> expectedBest, List<EvaluatedMove<M>> moves) {
		assertEquals(expectedBest, moves.get(0));
		assertTrue(moves.get(1).getScore()<moves.get(0).getScore());
	}

	@Test
	void matIn3ForWhitesWasTTPolicyBug() {
		ChessLibTest t = new ChessLibTest("4n2r/2k1Q2p/5B2/2N5/2B2R2/1P6/3PKPP1/6q1 b - - 2 46", 7);
		final List<EvaluatedMove<Move>> search = t.search(NEGAMAX, true);
		assertEquals(4, search.size());
		final Evaluation loose3 = Evaluation.loose(3, -t.getWinScore(6));
		assertEquals(loose3, search.get(0).getEvaluation());
		assertEquals(loose3, search.get(1).getEvaluation());
		final Evaluation loose1 = Evaluation.loose(1, -t.getWinScore(2));
		assertEquals(loose1, search.get(2).getEvaluation());
		assertEquals(loose1, search.get(3).getEvaluation());
	}
	
	@Test
	void noMatSituations() {
		ChessLibTest t = new ChessLibTest("4k3/r1q2bpr/2n1np2/8/8/P4B1N/3R1QPP/RN2K3 w Q - 0 1", 2);
		EvaluatedMove<Move> best = new EvaluatedMove<>(new Move(F3,C6), Evaluation.score(100));
		EvaluatedMove<Move> second = new EvaluatedMove<>(new Move(H3,G1), Evaluation.score(0));
		noMatAssert(best, second, t.search(MINIMAX, true));
		noMatAssert(best, second, t.search(ALPHA_BETA, true));
		noMatAssert(best, second, t.search(NEGAMAX_3, true));
		noMatAssert(best, second, t.search(NEGAMAX, true));

		t = new ChessLibTest("4k3/r1q2bpr/2n1np2/8/8/P4B1N/3R1QPP/RN2K3 b Q - 0 1", 2);
		best = new EvaluatedMove<>(new Move(C7,E5), Evaluation.score(-100));
		second = new EvaluatedMove<>(new Move(H7,H3), Evaluation.score(-300));
		noMatAssert(best, second, t.search(MINIMAX, true));
		noMatAssert(best, second, t.search(ALPHA_BETA, true));
		noMatAssert(best, second, t.search(NEGAMAX_3, true));
		noMatAssert(best, second, t.search(NEGAMAX, true));
	}
	
	<M> void noMatAssert(EvaluatedMove<M> expectedBest, EvaluatedMove<M> expectedSecond, List<EvaluatedMove<M>> moves) {
		assertEquals(expectedBest, moves.get(0));
		assertEquals(expectedSecond, moves.get(1));
	}

	@Test
	void bug20240127() {
		ChessLibTest t = new ChessLibTest("3n1rk1/1pp2p1p/2r2bq1/2P1p1p1/3pP3/PQ1P2PP/1B3PB1/R4RK1 b - - 2 26", 1);
		EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(G6,E4), Evaluation.score(100));
		assertContains(expected, t.search(MINIMAX, true));
		assertContains(expected, t.search(ALPHA_BETA, true));
		assertContains(expected, t.search(NEGAMAX, true));
		assertContains(expected, t.search(NEGAMAX_3, true));
		
		t = new ChessLibTest("3n1rk1/1pp2p1p/2r2bq1/2P1p1p1/3pP3/PQ1P2PP/1B3PB1/R4RK1 w - - 2 26", 1);
		expected = new EvaluatedMove<>(new Move(B3,F7), Evaluation.score(100));
		assertContains(expected, t.search(MINIMAX, true));
		assertContains(expected, t.search(ALPHA_BETA, true));
		assertContains(expected, t.search(NEGAMAX, true));
		assertContains(expected, t.search(NEGAMAX_3, true));
	}
	
	<M> void assertContains(EvaluatedMove<M> expected, List<EvaluatedMove<M>> moves) {
		Optional<EvaluatedMove<M>> result = moves.stream().filter(em -> em.getMove().equals(expected.getMove())).findAny();
		if (result.isEmpty()) {
			fail("Unable to find "+expected+" in results");
		} else {
			assertEquals(expected.getScore(), result.get().getScore());
		}
	}

	private static <M, B extends MoveGenerator<M>> List<EvaluatedMove<M>> search(SearchContext<M, B> ctx, Function<ExecutionContext<SearchContext<M, B>>, AbstractAI<M, B>> aiBuilder, SearchParameters params, List<M> moves) {
		try (ExecutionContext<SearchContext<M, B>> context = new MultiThreadsContext<>(ctx, new ContextualizedExecutor<>(4))) {
			final AbstractAI<M, B> ai = aiBuilder.apply(context);
			final List<EvaluatedMove<M>> list = (moves == null? ai.getBestMoves(params) : ai.getBestMoves(moves, params)).getList();
			if (ai instanceof Negamax) {
				for (EvaluatedMove<M> ev:list) {
					ev.setPvBuilder(m -> ((Negamax<M, B>)ai).getTranspositionTable().collectPV(ctx.getGamePosition(), m, params.getDepth()));
				}
			}
			return list;
		}
	}
}
