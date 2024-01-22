package com.fathzer.games.ai.transposition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

class OneLongEntryTranspositionTableTest {
	private static class TT extends OneLongEntryTranspositionTable<Integer> {
		public TT(int size, SizeUnit unit) {
			super(size, unit);
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
		OneLongEntryTranspositionTable<Integer> table = new TT(512, SizeUnit.KB);
		
		long key = 1L;
		assertFalse(table.get(1L).isValid());
		
		table.store(key, EntryType.UPPER_BOUND, 4, Short.MIN_VALUE, -10, p->true);
		
		TranspositionTableEntry<Integer> entry = table.get(key);
		assertTrue(entry.isValid());
		assertEquals(EntryType.UPPER_BOUND, entry.getEntryType());
		assertEquals(4, entry.getDepth());
		assertEquals(Short.MIN_VALUE, entry.getValue());
		assertEquals(-10, entry.getMove());

		table.store(key, EntryType.LOWER_BOUND, 5, 32737, -15, p->true);
		// Verify entry is replaced
		entry = table.get(key);
		assertEquals(5,entry.getDepth());
		assertEquals(EntryType.LOWER_BOUND,entry.getEntryType());
		assertEquals(32737,entry.getValue());
		assertEquals(-15, entry.getMove());
		
		// Verify there's no problem with 0 values
		table.newPosition();
		
		table.store(key, EntryType.EXACT, 0, 0, 0, p->true);
		entry = table.get(key);
		assertTrue(entry.isValid());
		assertEquals(0, entry.getDepth());
		assertEquals(EntryType.EXACT, entry.getEntryType());
		assertEquals(0, entry.getValue());
		assertEquals(0, entry.getMove());
	}
	
	@Test
	void collisionTest() {
		OneLongEntryTranspositionTable<Integer> table = new TT(32, SizeUnit.B);
		assertEquals(2, table.getSize());
		assertEquals(0, count(table));
		
		table.store(1L, EntryType.UPPER_BOUND, 4, Short.MIN_VALUE, -10, p->true);
		assertEquals(1, count(table));

		table.store(3L, EntryType.EXACT, 0, 0, 0, p->true);
		assertEquals(1, count(table));
		assertTrue(table.get(3L).isValid());
		assertFalse(table.get(1L).isValid());
		
		table.store(2L, EntryType.UPPER_BOUND, 1, 1000, 30, p->true);
		assertEquals(2, count(table));
	}

	private int count(TranspositionTable<?> table) {
		int count = 0;
		final Iterator<?> entries = table.getEntries();
		while (entries.hasNext()) {
			entries.next();
			count++;
		}
		return count;
	}
	
	@Test
	void memorySizeTest() {
		OneLongEntryTranspositionTable<Integer> table = new TT(32, SizeUnit.MB);
		assertEquals(32, table.getMemorySizeMB());
	}
}
