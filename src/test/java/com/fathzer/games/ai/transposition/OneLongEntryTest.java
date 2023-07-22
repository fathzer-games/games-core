package com.fathzer.games.ai.transposition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OneLongEntryTest {

	@Test
	void test() {
		
		OneLongEntry entry = new OneLongEntry(1L,0);
		assertFalse(entry.isValid());
		entry.setDepth(5);
		entry.setValue(Short.MIN_VALUE);
		entry.setEntryType(EntryType.UPPER_BOUND);
		
		OneLongEntry other = new OneLongEntry(entry.getKey(), entry.toLong());
		assertTrue(other.isValid());
		assertEquals(entry.getDepth(),other.getDepth());
		assertEquals(entry.getEntryType(),other.getEntryType());
		assertEquals(entry.getValue(),other.getValue());
		
		entry.setDepth(0);
		entry.setValue(0);
		entry.setEntryType(EntryType.values()[0]);
		assertNotEquals(0, entry.toLong());
		
		other.setDepth(4);
		assertEquals(4,other.getDepth());
		other.setEntryType(EntryType.LOWER_BOUND);
		assertEquals(EntryType.LOWER_BOUND,other.getEntryType());
		other.setValue(200);
		assertEquals(4,other.getDepth());
		assertEquals(EntryType.LOWER_BOUND,other.getEntryType());
		assertEquals(200,other.getValue());
	}

}
