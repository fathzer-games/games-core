package com.fathzer.games.ai.transposition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OneLongEntryTest {

	@Test
	void test() {
		final OneLongEntry<Integer> entry = new OneLongEntry<Integer>(Integer::valueOf);
		entry.set(1, OneLongEntry.toLong(EntryType.EXACT, Byte.MAX_VALUE, Short.MIN_VALUE, 87, 63));
		assertEquals(EntryType.EXACT, entry.getEntryType());
		assertEquals(Byte.MAX_VALUE, entry.getDepth());
		assertEquals(Short.MIN_VALUE, entry.getValue());
		assertEquals(87, entry.getMove());
		assertEquals(63, entry.getGeneration());
	}

}
