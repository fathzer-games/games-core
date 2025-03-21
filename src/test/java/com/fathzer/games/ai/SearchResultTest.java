package com.fathzer.games.ai;

import static com.fathzer.games.ai.evaluation.Evaluation.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class SearchResultTest {

	@Test
	void test() {
		SearchResult<Integer> fns = new SearchResult<>(new SearchParameters(3, 2));
		assertEquals(Integer.MIN_VALUE, fns.getLow());
		fns.add(1, score(0));
		assertEquals(Integer.MIN_VALUE, fns.getLow());
		fns.add(2, score(3));
		assertEquals(Integer.MIN_VALUE, fns.getLow());
		fns.add(3, score(-3));
		assertEquals(-6, fns.getLow());
		fns.add(4, score(6));
		assertEquals(-3, fns.getLow());
		
		assertEquals(Arrays.asList(4,2,1), fns.getCut().stream().map(e -> e.getMove()).toList());
		assertEquals(Arrays.asList(4,2,1,3), fns.getList().stream().map(e -> e.getMove()).toList());
		
		{
		// Add ex-aequo
		fns.add(5, score(0));
		assertEquals(-3, fns.getLow());
		final List<Integer> cut = fns.getCut().stream().map(e -> e.getMove()).toList();
		assertEquals(4, cut.size());
		assertEquals(4, cut.get(0));
		assertEquals(2, cut.get(1));
		assertEquals(new HashSet<>(Arrays.asList(1,5)), new HashSet<>(cut.subList(2, 4)));
		}
		
		// Add other ex aequos that reduce the cut list
		fns.add(6, score(6));
		final List<Integer> cut2 = fns.getCut().stream().map(e -> e.getMove()).toList();
		assertEquals(3, cut2.size());
		assertEquals(new HashSet<>(Arrays.asList(4,6)), new HashSet<>(cut2.subList(0, 2)));
		assertEquals(2, cut2.get(2));
	}

}
