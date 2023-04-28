package com.fathzer.games.clock;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClockSettingsTest {

	@Test
	void test() {
		assertThrows(IllegalArgumentException.class, () -> new ClockSettings(-1));

		final ClockSettings settings = new ClockSettings(2).withIncrement(1, 3, true);
		assertEquals(2, settings.getInitialTime());
		assertEquals(3, settings.getMovesNumberBeforeIncrement());
		assertEquals(1, settings.getIncrement());
		assertTrue(settings.isCanAccumulate());

		CountDown state = settings.buildCountDown();
		assertEquals(2000, state.getRemainingTime());
		assertThrows(IllegalArgumentException.class, () -> settings.withInitialTime(-1));
		settings.withInitialTime(5);
		assertEquals(5, settings.getInitialTime());
		settings.withIncrement(0, 0, false);
		assertEquals(0, settings.getIncrement());
		
		assertThrows(IllegalArgumentException.class, () -> settings.withIncrement(1, 0, true));

		final ClockSettings next = new ClockSettings(1);
		settings.withNext(10, 5, next);
		assertEquals(next, settings.getNext());
		assertEquals(5, settings.getMaxRemainingKept());
		assertEquals(10, settings.getMovesNumberBeforeNext());
		
		settings.withNext(Integer.MAX_VALUE, 0, null);
		assertNull(settings.getNext());
		
		assertThrows(IllegalArgumentException.class, () -> settings.withNext(0, 0, next));
		assertThrows(IllegalArgumentException.class, () -> settings.withNext(10, -1, next));
		assertThrows(IllegalArgumentException.class, () -> settings.withNext(Integer.MAX_VALUE, 10, next));
		assertThrows(IllegalArgumentException.class, () -> settings.withNext(5, 10, null));
	}

}
