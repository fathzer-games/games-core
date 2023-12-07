package com.fathzer.games.ai;

import static com.fathzer.games.ai.AiType.*;
import static com.github.bhlangonijr.chesslib.Square.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.MoveGenerator;
import com.fathzer.games.ai.evaluation.DummyEvaluator;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.Evaluation;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.exec.ContextualizedExecutor;
import com.fathzer.games.util.exec.ExecutionContext;
import com.fathzer.games.util.exec.MultiThreadsContext;
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
		private final Board board;
		private final int depth;
		
		ChessLibTest(String fen, int depth) {
			board = new Board();
			board.loadFromFen(fen);
			this.depth = depth;
		}

		List<EvaluatedMove<Move>> search(AiType ai) {
			return search(ai, new SearchParameters(depth, Integer.MAX_VALUE, 0));
		}

		List<EvaluatedMove<Move>> search(AiType ai, SearchParameters params) {
			Supplier<SearchContext<Move, ChessLibMoveGenerator>> supplier = () -> {
				ChessLibMoveGenerator mg = ai.getMVGsupplier(board).get();
				mg.setMoveComparator(ai==Minimax?null:new BasicMoveComparator(mg));
				Evaluator<Move, ChessLibMoveGenerator> ev = new BasicEvaluator(mg);
				ev.setViewPoint(board.getSideToMove()==Side.WHITE ? Color.WHITE : Color.BLACK);
				return new SearchContext<Move, ChessLibMoveGenerator>(mg, ev);
			};
			return MinimaxTest.search(supplier, ai.getAiBuilder(), params);
		}
		
		int getWinScore(int nbHalfMoves) {
			return new DummyEvaluator().getWinScore(nbHalfMoves);
		}
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
		assertEquals(expected, t.search(Minimax));
		assertEquals(expected, t.search(AlphaBeta));
		assertEquals(expected, t.search(Negamax));
		assertEquals(expected, t.search(Negamax3));
	}

	@Test
	void matIn2ForWhites() {
		ChessLibTest t = new ChessLibTest("8/8/8/8/1B6/NN6/pk1K4/8 w - - 0 1", 4);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(B3,A1), Evaluation.win(2, t.getWinScore(3)));
		final List<EvaluatedMove<Move>> search = t.search(Negamax);
		assertEquals(expected, search.get(0));
		assertTrue(search.get(1).getScore()<search.get(0).getScore());
		assertEquals(search, t.search(Minimax));
		assertEquals(search, t.search(AlphaBeta));
		assertEquals(search, t.search(Negamax3));
	}

	@Test
	void threeMatsIn1ForWhites() {
		ChessLibTest t = new ChessLibTest("7k/5p2/5PQN/5PPK/6PP/8/8/8 w - - 6 5", 2);
		final List<EvaluatedMove<Move>> search = t.search(Negamax);
		final Evaluation max = search.get(0).getEvaluation();
		search.stream().limit(3).forEach(m -> assertEquals(max, m.getEvaluation()));
		assertNotEquals(max, search.get(3).getEvaluation());
		assertEquals(search, t.search(Minimax));
		assertEquals(search, t.search(AlphaBeta));
		assertEquals(search, t.search(Negamax3));
	}

	@Test
	void matIn2ForBlacks() {
		ChessLibTest t = new ChessLibTest("8/4k1KP/6nn/6b1/8/8/8/8 b - - 0 1", 4);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(G6,H6), Evaluation.win(2, t.getWinScore(3)));
		final List<EvaluatedMove<Move>> search = t.search(Negamax);
		assertEquals(expected, search.get(0));
		assertTrue(search.get(1).getScore()<search.get(0).getScore());
		assertEquals(search, t.search(Minimax));
		assertEquals(search, t.search(AlphaBeta));
		assertEquals(search, t.search(Negamax3));
	}
	
	@Test
	void matIn3ForWhites() {
		ChessLibTest t = new ChessLibTest("r2k1r2/pp1b2pp/1b2Pn2/2p5/Q1B2Bq1/2P5/P5PP/3R1RK1 w - - 0 1", 6);
		SearchParameters params = new SearchParameters(6);
		final EvaluatedMove<Move> expected = new EvaluatedMove<>(new Move(D1,D7), Evaluation.win(3, t.getWinScore(5)));
		matIn3forWhitesAssert(expected, t.search(Negamax, params));
		matIn3forWhitesAssert(expected, t.search(Negamax3, params));
		matIn3forWhitesAssert(expected, t.search(AlphaBeta, params));
	}
	
	<M> void matIn3forWhitesAssert(EvaluatedMove<M> expectedBest, List<EvaluatedMove<M>> moves) {
		assertEquals(expectedBest, moves.get(0));
		assertTrue(moves.get(1).getScore()<moves.get(0).getScore());
	}


	private static <M, B extends MoveGenerator<M>> List<EvaluatedMove<M>> search(Supplier<SearchContext<M, B>> supplier, Function<ExecutionContext<SearchContext<M, B>>, AbstractAI<M, B>> aiBuilder, SearchParameters params) {
		try (ExecutionContext<SearchContext<M, B>> context = new MultiThreadsContext<>(supplier, new ContextualizedExecutor<>(4))) {
			AbstractAI<M, B> ai = aiBuilder.apply(context);
			return ai.getBestMoves(params).getList();
		}
	}
}
