package com.fathzer.games.ai.transposition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.evaluation.DummyEvaluator;
import com.fathzer.games.ai.evaluation.Evaluator;
import com.github.bhlangonijr.chesslib.move.Move;

class TTAiTest {
	private static class DummyTTAi implements TTAi<Move> {
		@Override
		public void setTranspositonTable(TranspositionTable<Move> table) {
		}

		@Override
		public TranspositionTable<Move> getTranspositionTable() {
			return null;
		}
	}

	@Test
	void test() {
		Evaluator<Move, ?> ev = new DummyEvaluator();
		final TTAi<Move> wse = new DummyTTAi();
		// A mat in 0 found at depth 3 on 8
		int encoded = wse.scoreToTT(-32762, 3, 8, ev);
		assertEquals(-Short.MAX_VALUE, encoded);
		int raw = wse.ttToScore(encoded, 3, 8, ev);
		assertEquals(-32762, raw);

		// A mat in 1 half move found at depth 2 on 8
		assertEquals(Short.MAX_VALUE-1, wse.scoreToTT(32762, 4, 8, ev));
		assertEquals(32762, wse.ttToScore(Short.MAX_VALUE-1, 4, 8, ev));
	}
}
