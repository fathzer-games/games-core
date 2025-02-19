package com.fathzer.games.ai.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.bhlangonijr.chesslib.move.Move;

class EvaluatorTest {
	@Test
	void test() {
		Evaluator<Move, ?> ev = new DummyEvaluator<>();
		assertEquals(Short.MAX_VALUE, ev.getWinScore(0));
		assertEquals(Short.MAX_VALUE-1, ev.getWinScore(1));
		assertEquals(Short.MAX_VALUE-10, ev.getWinScore(10));
		
		assertEquals(0, ev.getNbHalfMovesToWin(Short.MAX_VALUE));
		assertEquals(1, ev.getNbHalfMovesToWin(Short.MAX_VALUE-1));
		assertEquals(10, ev.getNbHalfMovesToWin(Short.MAX_VALUE-10));
		assertEquals(1, ev.getNbHalfMovesToWin(-Short.MAX_VALUE+1));
		assertEquals(10, ev.getNbHalfMovesToWin(-Short.MAX_VALUE+10));
		
		assertTrue(ev.isWinLooseScore(Short.MAX_VALUE));
		assertTrue(ev.isWinLooseScore(Short.MAX_VALUE-255));
		assertFalse(ev.isWinLooseScore(Short.MAX_VALUE-256));
	}
}
