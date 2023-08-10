package com.fathzer.games.ai.evaluation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.evaluation.Evaluation;

class EvaluationTest {

	@Test
	void test() {
		final Evaluation win1 = Evaluation.win(1, 999);
		assertEquals(999,win1.getScore());
		final Evaluation loose1 = Evaluation.loose(1, -999);
		assertEquals(-999,loose1.getScore());
		final Evaluation win2 = Evaluation.win(2, 998);
		final Evaluation loose2 = Evaluation.loose(2, -998);
		final Evaluation badScore = Evaluation.score(-1);
		final Evaluation goodScore = Evaluation.score(1);
		
		assertTrue(win1.compareTo(loose1)>0);
		assertTrue(Evaluation.REVERSE.compare(win1,loose1)<0);
		assertTrue(win1.compareTo(win2)>0);
		assertTrue(win2.compareTo(loose1)>0);
		assertTrue(loose2.compareTo(loose1)>0);
		assertTrue(win1.compareTo(goodScore)>0);
		assertTrue(goodScore.compareTo(badScore)>0);
		assertTrue(badScore.compareTo(loose1)>0);
		
		assertEquals(goodScore, Evaluation.score(1));
		assertTrue(goodScore.compareTo(Evaluation.score(10))<0);
		
		assertEquals("WIN+1",win1.toString());
		assertEquals("LOOSE+2",loose2.toString());
		assertEquals("-1",badScore.toString());
	}

}
