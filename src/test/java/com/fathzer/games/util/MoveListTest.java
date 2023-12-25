package com.fathzer.games.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveListTest {
	private static final ToIntFunction<Integer> EVALUATOR = i -> i%2==0 ? Integer.MIN_VALUE : i;
	
    @Test
    void testDualListIterator() {
        // Test data
        List<Integer> list1 = List.of(3, 2, 5, 4, 8, 6, 1);

        // Create the iterator
        MoveList<Integer> mvList = new MoveList<>();
        mvList.setEvaluator(EVALUATOR);
        mvList.addAll(list1);
        mvList.sort();
        
        final List<Integer> expected = List.of(5, 3, 1, 2, 4, 8, 6);
		assertEquals(expected, mvList);
        
        Iterator<Integer> iter = mvList.iterator();
        for (Integer ex : expected) {
        	assertEquals(ex, iter.next());
		}
        
        assertFalse(iter.hasNext());
        assertThrows(NoSuchElementException.class, () -> iter.next());
        
        for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), mvList.get(i));
		}
    }
}
