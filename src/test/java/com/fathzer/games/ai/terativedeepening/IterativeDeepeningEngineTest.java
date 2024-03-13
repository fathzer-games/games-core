package com.fathzer.games.ai.terativedeepening;

import static com.github.bhlangonijr.chesslib.Square.A5;
import static com.github.bhlangonijr.chesslib.Square.A6;
import static com.github.bhlangonijr.chesslib.Square.D3;
import static com.github.bhlangonijr.chesslib.Square.D4;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.iterativedeepening.DeepeningPolicy;
import com.fathzer.games.ai.iterativedeepening.FirstBestMoveSelector;
import com.fathzer.games.ai.iterativedeepening.IterativeDeepeningEngine;
import com.fathzer.games.ai.moveselector.RandomMoveSelector;
import com.fathzer.games.ai.transposition.SizeUnit;
import com.fathzer.games.ai.transposition.TT;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.BasicMoveComparator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.github.bhlangonijr.chesslib.move.Move;

class IterativeDeepeningEngineTest {

	@Test
	void test() { //TODO
		final DeepeningPolicy deepeningPolicy = new DeepeningPolicy(4);
		deepeningPolicy.setSize(Integer.MAX_VALUE);
		IterativeDeepeningEngine<Move, ChessLibMoveGenerator> engine = new IterativeDeepeningEngine<>(deepeningPolicy, new TT(16, SizeUnit.MB), BasicEvaluator::new);
//		engine.setMoveSelectorBuilder(b -> new FirstBestMoveSelector<Move>().setNext(new RandomMoveSelector<>()));
		ChessLibMoveGenerator mg = new ChessLibMoveGenerator("r1bq1rk1/3n1ppp/p3p3/2bpP3/Np1B1P1P/7R/PPPQ2P1/2KR1B2 b - - 1 14", BasicMoveComparator::new);
		System.out.println(engine.apply(mg));
		System.out.println(engine.getBestMoves(mg));
		final Move illegalMove = new Move(D3, D4);
		final Move legalMove = new Move(A6, A5);
		System.out.println(engine.getBestMove(mg, Collections.singletonList(illegalMove)));
	}
	
	@Test
	void matTest() {
		final DeepeningPolicy deepeningPolicy = new DeepeningPolicy(4);
		IterativeDeepeningEngine<Move, ChessLibMoveGenerator> engine = new IterativeDeepeningEngine<>(deepeningPolicy, new TT(16, SizeUnit.MB), BasicEvaluator::new);
		ChessLibMoveGenerator mg = new ChessLibMoveGenerator("r1bq1rk1/3n1ppQ/p3p3/2bpP3/Np1B1P1P/3B3R/PPP3P1/2KR4 b - - 1 14", BasicMoveComparator::new);
		assertNull(engine.apply(mg));
	}
}
