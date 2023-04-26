package com.fathzer.games.clock;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ClockSettingsTest {

	@Test
	void test() {
		ClockSettings settings = new ClockSettings(2).withIncrement(1, 1, true);
		assertEquals(2, settings.getInitialTime());
		ClockState state = settings.buildClockState();
		assertEquals(2000, state.getRemainingTime());
	}

}
