package com.fathzer.games.ai.terativedeepening;

import static com.github.bhlangonijr.chesslib.Square.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.ai.transposition.TT;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.github.bhlangonijr.chesslib.move.Move;

class IterativeDeepeningEngineTest {

	@Test
	void test() {
		final DeepeningPolicy deepeningPolicy = new DeepeningPolicy(4);
		IterativeDeepeningEngine<Move, ChessLibMoveGenerator> engine = new IterativeDeepeningEngine<>(deepeningPolicy, new TT(16, SizeUnit.MB), BasicEvaluator::new);
		ChessLibMoveGenerator mg = new ChessLibMoveGenerator("r1bq1rk1/3n1ppp/p3p3/2bpP3/Np1B1P1P/7R/PPPQ2P1/2KR1B2 b - - 1 14", BasicMoveComparator::new);
		assertEquals(new Move(C5, D4),engine.apply(mg));
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
		assertNull(engine.apply(mg));
	}
}
