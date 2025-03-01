package com.fathzer.games.ai.terativedeepening;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.ai.SearchParameters;
import com.fathzer.games.ai.iterativedeepening.SearchHistory;
import com.fathzer.games.ai.moveselector.RandomMoveSelector;
import com.github.bhlangonijr.chesslib.move.Move;

class SearchHistoryTest {

	@Test
	void test() {
		assertThrows(IllegalArgumentException.class, () -> new SearchHistory<>(null));
		
		SearchHistory<Move> history = new SearchHistory<>(new SearchParameters(1, 0));
		assertTrue(history.isEmpty());
		assertEquals(0, history.getLastDepth());
		assertNull(history.getBestMoves());
		assertNull(history.getLastList());
		
		assertNull(history.getBestMove(new RandomMoveSelector<>()));
	}

}
