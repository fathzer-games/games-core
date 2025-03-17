package com.fathzer.games.ai.time;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.clock.CountDownState;

class TimeTest {

	@Test
	void test() {
		final BasicTimeManager<Void> tm = new BasicTimeManager<>(x -> 3);
		assertEquals(500, tm.getMaxTime(null, new CountDownState(999, 1, 0)));
		assertEquals(600, tm.getMaxTime(null, new CountDownState(600, 0, 1)));
		
		assertThrows(IllegalArgumentException.class, () -> new CountDownState(600, 0, -1));
	}

}
