package com.fathzer.games.ai;

import static org.junit.jupiter.api.Assertions.*;

import static com.fathzer.games.testutils.Utils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.evaluation.EvaluatedMove;

class SearchParametersTest {

	@Test
	void test() {
		assertThrows(IllegalArgumentException.class, () -> new SearchParameters(0,0));
		assertThrows(IllegalArgumentException.class, () -> new SearchParameters(1, -1));
		SearchParameters params = new SearchParameters(2, 3);
		assertEquals(2, params.getSize());
		assertEquals(3, params.getAccuracy());
		
		params = new SearchParameters();
		assertEquals(1, params.getSize());
		assertEquals(0, params.getAccuracy());
		assertEquals(Integer.MIN_VALUE, params.getLowerBound(Collections.emptyList()));
		assertEquals(0, params.getBestMovesCount(Collections.emptyList()));
		params.setSize(3);
		params.setAccuracy(1);
		
		List<EvaluatedMove<String>> moves = Arrays.asList(eval("A", 10), eval("B", 9), eval("C",7), eval("D", 6));
		assertEquals(5, params.getLowerBound(moves));
		assertEquals(4, params.getAccurateMovesCount(moves));
		assertEquals(2, params.getBestMovesCount(moves));
		params.setAccuracy(0);
		assertEquals(6, params.getLowerBound(moves));
		assertEquals(3, params.getAccurateMovesCount(moves));
		assertEquals(1, params.getBestMovesCount(moves));
		params.setSize(1);
		params.setAccuracy(1);
		assertEquals(2, params.getAccurateMovesCount(moves));
		assertEquals(2, params.getBestMovesCount(moves));
	}

}
