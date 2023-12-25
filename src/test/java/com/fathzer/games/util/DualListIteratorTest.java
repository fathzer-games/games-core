package com.fathzer.games.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DualListIteratorTest {

    @Test
    void testDualListIterator() {
        // Test data
        List<Integer> list1 = List.of(1, 3, 5);
        List<Integer> list2 = List.of(2, 4, 6, 8);

        // Create the iterator
        DualListIterator<Integer> dualListIterator = new DualListIterator<>(list1, list2);

        // Test hasNext and next methods
        assertTrue(dualListIterator.hasNext());
        assertEquals(1, dualListIterator.next());

        assertTrue(dualListIterator.hasNext());
        assertEquals(3, dualListIterator.next());

        assertTrue(dualListIterator.hasNext());
        assertEquals(5, dualListIterator.next());
        
        assertTrue(dualListIterator.hasNext());
        assertEquals(2, dualListIterator.next());

        assertTrue(dualListIterator.hasNext());
        assertEquals(4, dualListIterator.next());

        assertTrue(dualListIterator.hasNext());
        assertEquals(6, dualListIterator.next());

        assertTrue(dualListIterator.hasNext());
        assertEquals(8, dualListIterator.next());

        assertFalse(dualListIterator.hasNext()); // No more elements

        // Attempting to call next after reaching the end should throw NoSuchElementException
        assertThrows(NoSuchElementException.class, dualListIterator::next);
    }
}
