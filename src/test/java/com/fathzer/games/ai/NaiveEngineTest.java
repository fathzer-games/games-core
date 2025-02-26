package com.fathzer.games.ai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

class NaiveEngineTest {

	@Test
	void test() {
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator("3b3k/4R3/5P1K/8/8/8/8/8 w - - 6 5", x->null);
		final Evaluator<Move, ChessLibMoveGenerator> ev = new BasicEvaluator();
		
		NaiveEngine<Move, ChessLibMoveGenerator> engine = new NaiveEngine<>(ev);
		
		final Move mat = new Move(Square.E7, Square.E8);
		assertEquals(ev.getWinScore(1), engine.evaluate(mg, mat));
		assertEquals(-200, engine.evaluate(mg, new Move(Square.F6, Square.F7)));
		assertEquals(mat, engine.apply(mg));
	}

}
