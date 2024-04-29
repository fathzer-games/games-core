package com.fathzer.games.ai.terativedeepening;

import static com.github.bhlangonijr.chesslib.Square.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fathzer.games.MoveGenerator;
import com.fathzer.games.Status;
import com.fathzer.games.ai.Negamax;
import com.fathzer.games.ai.SearchContext;
import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.SearchResult;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.evaluation.StaticEvaluator;
import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine;
import com.fathzer.games.ai.iterativedeepening.SearchHistory;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.ai.transposition.TT;
import com.fathzer.games.ai.transposition.TTAi;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.fathzer.games.util.exec.ExecutionContext;
import com.github.bhlangonijr.chesslib.move.Move;

class IterativeDeepeningEngineTest {
	public static Function<String, List<EvaluatedMove<String>>> encodedEvMovetoList = s -> Arrays.stream(s.split(",")).map(IterativeDeepeningEngineTest::parseMove).filter(Optional::isPresent).map(Optional::get).toList();

	@Test
	void test() {
		final DeepeningPolicy deepeningPolicy = new DeepeningPolicy(4);
		IterativeDeepeningEngine<Move, ChessLibMoveGenerator> engine = new IterativeDeepeningEngine<>(deepeningPolicy, new TT(16, SizeUnit.MB), BasicEvaluator::new);
		ChessLibMoveGenerator mg = new ChessLibMoveGenerator("r1bq1rk1/3n1ppp/p3p3/2bpP3/Np1B1P1P/7R/PPPQ2P1/2KR1B2 b - - 1 14", BasicMoveComparator::new);
		assertEquals(new Move(C5, D4),engine.getBestMoves(mg).getBest());
		final Move illegalMove = new Move(D3, D4);
		assertTrue(engine.getBestMoves(mg, Collections.singletonList(illegalMove)).isEmpty());
		final Move legalMove = new Move(A6, A5);
		assertEquals(legalMove, engine.getBestMoves(mg, Arrays.asList(illegalMove, legalMove)).getBest());
	}
	
	@Test
	void matTest() {
		final DeepeningPolicy deepeningPolicy = new DeepeningPolicy(4);
		IterativeDeepeningEngine<Move, ChessLibMoveGenerator> engine = new IterativeDeepeningEngine<>(deepeningPolicy, new TT(16, SizeUnit.MB), BasicEvaluator::new);
		ChessLibMoveGenerator mg = new ChessLibMoveGenerator("r1bq1rk1/3n1ppQ/p3p3/2bpP3/Np1B1P1P/3B3R/PPP3P1/2KR4 b - - 1 14", BasicMoveComparator::new);
		assertNull(engine.getBestMoves(mg).getBest());
	}
	
	@Test
	void testInterrupted() {
		final DeepeningPolicy policy = new DeepeningPolicy(4);
		IterativeDeepeningEngine<String, FakeMoveGenerator> engine = new IterativeDeepeningEngine<>(policy, null, FakeEvaluator::new) {
			@Override
			protected TTAi<String> buildAi(ExecutionContext<SearchContext<String, FakeMoveGenerator>> context) {
				return new FakeNegamax(context);
			}
		};
		
		FakeNegamax.setSearchData("2:1:As101,Bs100,Cs99,Ds98/3:1:As80,Bi0,Ci0,Di0");
		SearchHistory<String> bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		assertEquals(2, bestMoves.length());
		assertEquals(encodedEvMovetoList.apply("As80"), bestMoves.getBestMoves());

		FakeNegamax.setSearchData("2:1:As101,Bs100,Cs99,Ds98/3:1:As80,Bs90,Ci0,Di0");
		bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		assertEquals(2, bestMoves.length());
		assertEquals(encodedEvMovetoList.apply("As90"), bestMoves.getBestMoves());
	}
	
	@Test
	void multiPVTest() {
		final DeepeningPolicy policy = new DeepeningPolicy(6);
		IterativeDeepeningEngine<String, FakeMoveGenerator> engine = new IterativeDeepeningEngine<>(policy, null, FakeEvaluator::new) {
			@Override
			protected TTAi<String> buildAi(ExecutionContext<SearchContext<String, FakeMoveGenerator>> context) {
				return new FakeNegamax(context);
			}
		};
		FakeNegamax.setSearchData("2:1:Aw1,Bs100,Cs-100,Ds-150");
		SearchHistory<String> bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		// Test it stops after first evaluation (because there's one win move)
		assertEquals(1, bestMoves.length());
		assertEquals(4, bestMoves.getList().size());
		
		// Now look for 2 bestmoves
		policy.setSize(2);
		FakeNegamax.setSearchData("2:2:Aw1,Bs100,Cs-105,Ds-106/"
								 + "3:1:Bl1,Cs104,Ds105/"
								 + "4:1:Dl1,Cs80");
		bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		assertEquals(3,bestMoves.length());
		assertEquals(encodedEvMovetoList.apply("Aw1,Cs80"), bestMoves.getBestMoves());
		assertEquals(encodedEvMovetoList.apply("Aw1,Bs100"), bestMoves.getBestMoves(0));
		assertEquals(encodedEvMovetoList.apply("Aw1,Ds105"), bestMoves.getBestMoves(1));
		// Check that policy did not change its size
		assertEquals(2, policy.getSize());
		
		// Look for 2 best moves and have two wins at first depth and 1 at second
		policy.setSize(2);
		FakeNegamax.setSearchData("2:2:Aw1,Bw1,Cs-105,Ds-106");
		bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		assertEquals(1,bestMoves.length());
		assertEquals(encodedEvMovetoList.apply("Aw1,Bw1"), bestMoves.getBestMoves());

		// Look for 3 best moves and have two wins at first depth and 1 at second
		policy.setSize(3);
		FakeNegamax.setSearchData("2:3:Aw1,Bw1,Cs-105,Ds-106/"
				 				 + "3:1:Cs104,Dw1/");
		bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		assertEquals(2,bestMoves.length());
		assertEquals(encodedEvMovetoList.apply("Aw1,Bw1,Dw1"), bestMoves.getBestMoves());
		
		// Look for 2 best moves and do no deepen with 1 win and only one non 'end game' move at first depth
		policy.setSize(2);
		policy.setDeepenOnForced(false);
		FakeNegamax.setSearchData("2:2:Aw1,Bs100,Cl2,Dl2");
		bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		assertEquals(1,bestMoves.length());
		assertEquals(encodedEvMovetoList.apply("Aw1,Bs100"), bestMoves.getBestMoves());
		
		// Look for 2 best moves and do forced deepen with 1 win and only one non 'end game' move at first depth
		policy.setSize(2);
		policy.setDeepenOnForced(true);
		FakeNegamax.setSearchData("2:2:Aw1,Bs100,Cl2,Dl2/3:1:Bw2");
		bestMoves = engine.getBestMoves(new FakeMoveGenerator());
		assertEquals(2,bestMoves.length());
		assertEquals(encodedEvMovetoList.apply("Aw1,Bw2"), bestMoves.getBestMoves());
	}
	
	public static Optional<EvaluatedMove<String>> parseMove(String encoded) {
		encoded = encoded.toUpperCase();
		final String mv = encoded.substring(0, 1);
		final char type = encoded.charAt(1);
		final int value = Integer.parseInt(encoded.substring(2));
		var ev = new FakeEvaluator();
		final int score;
		if (type=='S') {
			score = value;
		} else if (type=='W') {
			score = ev.getWinScore(value);
		} else if (type=='L') {
			score = -ev.getWinScore(value);
		} else if (type=='I') {
			return Optional.empty();
		} else {
			throw new IllegalArgumentException();
		}
		return Optional.of(new EvaluatedMove<>(mv, ev.toEvaluation(score)));
	}
	
	private record SearchData(int expectedSearchSize, List<String> requestedMoves, List<EvaluatedMove<String>> returnedMoves, boolean isInterrupted) {};
	
	private static class FakeNegamax extends Negamax<String, FakeMoveGenerator> {
		private static Map<Integer, SearchData> expectedMoveListMap;
		private boolean isInterrupted;
		
		public FakeNegamax(ExecutionContext<SearchContext<String, FakeMoveGenerator>> exec) {
			super(exec);
		}

		@Override
		public SearchResult<String> getBestMoves(SearchParameters params) {
			final SearchData data = expectedMoveListMap.get(params.getDepth());
			assertNotNull(data, "Depth "+params.getDepth()+" is unexpected here");
			assertEquals(data.expectedSearchSize, params.getSize());
			final SearchResult<String> r = new SearchResult<String>(params.getSize(), params.getAccuracy());
			data.returnedMoves.forEach(em -> r.add(em.getContent(), em.getEvaluation()));
			isInterrupted = data.isInterrupted();
			return r;
		}
		
		@Override
	    public SearchResult<String> getBestMoves(List<String> moves, SearchParameters params) {
			final SearchData data = expectedMoveListMap.get(params.getDepth());
			assertNotNull(data, "Depth "+params.getDepth()+" is unexpected here");
			assertEquals(data.expectedSearchSize, params.getSize(),"At depth "+params.getDepth()+" search size is wrong");
			assertEquals(data.requestedMoves(), moves,"At depth "+params.getDepth());
			final SearchResult<String> r = new SearchResult<String>(params.getSize(), params.getAccuracy());
			data.returnedMoves().forEach(em -> r.add(em.getContent(), em.getEvaluation()));
			isInterrupted = data.isInterrupted();
			return r;
	    }
		
		@Override
		public boolean isInterrupted() {
			return isInterrupted;
		}

		public static void setSearchData(String encoded) {
			expectedMoveListMap = Arrays.stream(encoded.split("/")).map(l -> l.split(":")).collect(Collectors.toMap(r -> Integer.parseInt(r[0]), r -> {
				final List<String> expected = Arrays.stream(r[2].split(",")).map(s -> s.substring(0, 1)).toList();
				final List<EvaluatedMove<String>> returned = encodedEvMovetoList.apply(r[2]);
				return new SearchData(Integer.parseInt(r[1]), expected, returned, expected.size()!=returned.size());
			}));
		}
	}
	
	private static class FakeEvaluator implements StaticEvaluator<String, FakeMoveGenerator> {
		@Override
		public int evaluate(FakeMoveGenerator board) {
			return 0;
		}
	}
	
	private static class FakeMoveGenerator implements MoveGenerator<String> {
		@Override
		public MoveGenerator<String> fork() {
			return this;
		}

		@Override
		public boolean isWhiteToMove() {
			return false;
		}

		@Override
		public boolean makeMove(String move, MoveConfidence confidence) {
			return true;
		}

		@Override
		public void unmakeMove() {
		}

		@Override
		public List<String> getMoves() {
			return null;
		}

		@Override
		public Status getEndGameStatus() {
			return null;
		}
	}
}
