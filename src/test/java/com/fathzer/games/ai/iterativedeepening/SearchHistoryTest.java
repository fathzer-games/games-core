package com.fathzer.games.ai.iterativedeepening;

import static org.junit.jupiter.api.Assertions.*;

import static com.fathzer.games.testutils.Utils.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.evaluation.EvaluatedMove;
import com.fathzer.games.ai.moveselector.RandomMoveSelector;
import com.github.bhlangonijr.chesslib.move.Move;

class SearchHistoryTest {

	@Test
	void test() {
		SearchHistory<String> history = new SearchHistory<>(new SearchParameters(1, 0));
        List<EvaluatedMove<String>> list1 = Arrays.asList(eval("B", 5), eval("A", 5), eval("F", 3));
        assertThrows(IllegalArgumentException.class, () -> history.add(list1, -1));
        history.add(list1, 1);
        assertThrows(IllegalArgumentException.class, () -> history.add(list1, 1));
        assertFalse(history.isEmpty());
        assertEquals(1, history.length());
        assertEquals(3, history.getList(0).size());
        assertEquals(2, history.getAccurateMoves(0).size());
        List<EvaluatedMove<String>> list2 = Arrays.asList(eval("C", 5), eval("A", 4), eval("B", 4));
        history.add(list2, 2);
        assertEquals(1, history.getAccurateMoves(1).size());
	}

	@Test
	void testEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new SearchHistory<>(null));
		
		SearchHistory<Move> history = new SearchHistory<>(new SearchParameters(1, 0));
		assertTrue(history.isEmpty());
		assertEquals(0, history.getLastDepth());
		assertTrue(history.getAccurateMoves().isEmpty());
		assertTrue(history.getLastList().isEmpty());
		
		assertNull(history.getBestMove(new RandomMoveSelector<>()));
	}
}
