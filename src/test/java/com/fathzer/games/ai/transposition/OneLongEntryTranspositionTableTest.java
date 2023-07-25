package com.fathzer.games.ai.transposition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OneLongEntryTranspositionTableTest {
	private static class TT extends OneLongEntryTranspositionTable<Integer> {
		public TT(int sizeInMB) {
			super(sizeInMB);
		}
		protected int toInt(Integer move) {
			return move;
		}
		protected Integer toMove(int value) {
			return value;
		}
	}
	

	@Test
	void test() {
		OneLongEntryTranspositionTable<Integer> table = new TT(64);
		
		long key = 1L;
		assertFalse(table.get(1L).isValid());
		
		table.store(key, EntryType.UPPER_BOUND, 4, Short.MIN_VALUE, -10);
		
		TranspositionTableEntry<Integer> entry = table.get(key);
		assertTrue(entry.isValid());
		assertEquals(EntryType.UPPER_BOUND, entry.getEntryType());
		assertEquals(5, entry.getDepth());
		assertEquals(Short.MIN_VALUE, entry.getValue());
		assertEquals(-10, entry.getMove());

		table.store(key, EntryType.LOWER_BOUND, 5, 32737, -15);
		// Verify entry is replaced
		entry = table.get(key);
		assertEquals(5,entry.getDepth());
		assertEquals(EntryType.LOWER_BOUND,entry.getEntryType());
		assertEquals(32737,entry.getValue());
		assertEquals(-15, entry.getMove());
		
		// Verify there's no problem with 0 values
		table.newPosition();
		
		table.store(key, EntryType.values()[0], 0, 0, 0);
		entry = table.get(key);
		assertTrue(entry.isValid());
		assertEquals(0, entry.getDepth());
		assertEquals(EntryType.values()[0], entry.getEntryType());
		assertEquals(0, entry.getValue());
		assertEquals(0, entry.getMove());
	}

}
