package com.fathzer.games.ai.toys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.evaluation.Evaluator;
import com.fathzer.games.chess.BasicEvaluator;
import com.fathzer.games.chess.ChessLibMoveGenerator;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;

class NaiveAITest {

	@Test
	void test() {
		final ChessLibMoveGenerator mg = new ChessLibMoveGenerator("3b3k/4R3/5P1K/8/8/8/8/8 w - - 6 5", x->null);
		final Evaluator<Move, ChessLibMoveGenerator> ev = new BasicEvaluator();
		
		NaiveAI<Move, ChessLibMoveGenerator> engine = new NaiveAI<>(mg, ev);
		
		final Move mat = new Move(Square.E7, Square.E8);
		assertEquals(ev.getWinScore(1), engine.evaluate(mat));
		assertEquals(-200, engine.evaluate(new Move(Square.F6, Square.F7)));
	}

}
