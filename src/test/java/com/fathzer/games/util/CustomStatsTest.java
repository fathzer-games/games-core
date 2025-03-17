package com.fathzer.games.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

class CustomStatsTest {

	@Test
	void test() {
		var cs = new CustomStats();
		assertFalse(cs.isOn());
		final String key = "x";
		assertEquals(0, cs.get(key));
		cs.increment(key);
		assertEquals(0, cs.get(key));
		cs.increment(key, 10);
		assertEquals(0, cs.get(key));
		
		assertFalse(cs.on());
		assertTrue(cs.on());
		assertTrue(cs.isOn());
		cs.increment(key);
		assertEquals(1, cs.get(key));
		cs.increment(key, 10);
		assertEquals(11, cs.get(key));
		
		cs.increment("y");
		assertEquals(11, cs.get(key));
		assertEquals(1, cs.get("y"));
		
		assertEquals(Set.of(key, "y"), cs.getCounters());

		cs.clear(key);
		assertEquals(0, cs.get(key));
		assertEquals(1, cs.get("y"));
		cs.increment(key);

		assertTrue(cs.off());
		assertFalse(cs.off());
		cs.increment("y");
		assertEquals(1, cs.get("y"));
		
		cs.clear();
		assertTrue(cs.getCounters().isEmpty());
		assertEquals(0, cs.get(key));
		assertEquals(0, cs.get("y"));
	}

}
