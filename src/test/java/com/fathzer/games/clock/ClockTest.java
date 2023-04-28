package com.fathzer.games.clock;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.games.clock.DefaultCountDownTest.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.Test;

import com.fathzer.games.Color;
import com.fathzer.games.clock.timeutils.FakeScheduler;

class ClockTest {
	
	static class FakeSettings extends ClockSettings {
		public FakeSettings(int initialTime) {
			super(initialTime);
		}

		@Override
		public CountDown buildCountDown() {
			return new FakeTimedCountDown(this);
		}
	}
	
	static class FakeClock extends Clock {
		private FakeScheduler scheduler;
		
		FakeClock(ClockSettings settings, FakeScheduler scheduler) {
			super(settings);
			this.scheduler = scheduler;
		}

		@Override
		public ScheduledExecutorService getScheduler() {
			return scheduler.get();
		}
	}

	@Test
	void test() throws InterruptedException {
		final ClockSettings settings = new FakeSettings(2).withIncrement(1, 1, true);
		Clock clock = new Clock(settings);
		assertTrue(clock.isPaused());
		assertNull(clock.getPlaying());
		assertEquals(2000, clock.getRemaining(Color.WHITE));
		assertEquals(2000, clock.getRemaining(Color.BLACK));
		
		// Clock starts => White is playing
		clock.tap();
		assertThrows(IllegalStateException.class, ()->clock.withStartingColor(Color.WHITE));
		assertFalse(clock.isPaused());
		assertEquals(Color.WHITE, clock.getPlaying());
		CLOCK.add(1000);
		assertEquals(2000, clock.getRemaining(Color.BLACK));
		// White tap the clock => Black is playing
		clock.tap();
		assertEquals(Color.BLACK, clock.getPlaying());
		long whiteRemaining = clock.getRemaining(Color.WHITE);
		assertEquals(2000, whiteRemaining, "Time remaining for white should be near 10000 but is "+whiteRemaining);
		assertFalse(clock.isPaused());
		// Pause the clock
		clock.pause();
		assertTrue(clock.isPaused());
		assertNull(clock.getPlaying());
		long blackRemaining = clock.getRemaining(Color.BLACK);
		CLOCK.add(1000);
		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
		assertEquals(whiteRemaining, clock.getRemaining(Color.WHITE));
		// Restart clock
		clock.tap();
		assertEquals(Color.BLACK, clock.getPlaying());
		assertTrue(clock.pause());
	}	
		
	@Test
	void testFlagFall() {
		final ClockSettings settings = new FakeSettings(2);
		final FakeScheduler scheduler = new FakeScheduler();
		
		final Clock clock = new FakeClock(settings, scheduler);
		clock.withStartingColor(Color.BLACK);
		final List<Color> winner = new ArrayList<>();
		clock.addListener(s -> winner.add(s.winner()));

		assertTrue(clock.tap());
		scheduler.sleep(2100);
		// WHITE should have won
		assertEquals(1,winner.size());
		assertEquals(Color.WHITE, winner.get(0));
		
		assertTrue(clock.isPaused());
		assertFalse(clock.pause());
		assertFalse(clock.tap());
	}
}
