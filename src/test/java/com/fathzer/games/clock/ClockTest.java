package com.fathzer.games.clock;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;

class ClockTest {

	@Test
	void test() throws InterruptedException {
		final List<Color> flagFallColor = new ArrayList<>();
		final ClockSettings settings = new ClockSettings(2).withIncrement(1, 1, true);
		Clock clock = new Clock(settings, c -> flagFallColor.add(c.getPlaying()));
		assertTrue(clock.isPaused());
		assertNull(clock.getPlaying());
		assertEquals(2000, clock.getRemaining(Color.WHITE));
		assertEquals(2000, clock.getRemaining(Color.BLACK));
		
		// White tap the clock => Black is playing
		clock.tap();
		assertFalse(clock.isPaused());
		assertEquals(Color.BLACK, clock.getPlaying());
		Thread.sleep(1000);
		assertEquals(2000, clock.getRemaining(Color.WHITE));
		// Black tap the clock => White is playing
		clock.tap();
		assertEquals(Color.WHITE, clock.getPlaying());
		long blackRemaining = clock.getRemaining(Color.BLACK);
		assertTrue(2000-blackRemaining < 50, "Time remaining for black should be near 10000 but is "+blackRemaining);
		assertFalse(clock.isPaused());
		// Pause the clock
		clock.pause();
		assertTrue(clock.isPaused());
		assertNull(clock.getPlaying());
		long whiteRemaining = clock.getRemaining(Color.WHITE);
		Thread.sleep(1000);
		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
		assertEquals(whiteRemaining, clock.getRemaining(Color.WHITE));
		// Restart clock
		clock.tap();
		assertEquals(Color.WHITE, clock.getPlaying());
		Thread.sleep(2200);
		// WHITE flag should be down
		assertEquals(1,flagFallColor.size());
		assertEquals(Color.WHITE,flagFallColor.get(0));
		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
		assertTrue(clock.getRemaining(Color.WHITE)<=0);
		assertTrue(clock.isPaused());
		// Tap should not work anymore
		assertFalse(clock.tap());
		assertTrue(clock.isPaused());
		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
		assertTrue(clock.getRemaining(Color.WHITE)<=0);
		// Pause should not work anymore
		assertFalse(clock.pause());
	}

}
