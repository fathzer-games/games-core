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
		
		// White tap the clock => Black is playing
		clock.tap();
		assertFalse(clock.isPaused());
		assertEquals(Color.BLACK, clock.getPlaying());
		CLOCK.add(1000);
		assertEquals(2000, clock.getRemaining(Color.WHITE));
		// Black tap the clock => White is playing
		clock.tap();
		assertEquals(Color.WHITE, clock.getPlaying());
		long blackRemaining = clock.getRemaining(Color.BLACK);
		assertEquals(2000, blackRemaining, "Time remaining for black should be near 10000 but is "+blackRemaining);
		assertFalse(clock.isPaused());
		// Pause the clock
		clock.pause();
		assertTrue(clock.isPaused());
		assertNull(clock.getPlaying());
		long whiteRemaining = clock.getRemaining(Color.WHITE);
		CLOCK.add(1000);
		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
		assertEquals(whiteRemaining, clock.getRemaining(Color.WHITE));
		// Restart clock
		clock.tap();
		assertEquals(Color.WHITE, clock.getPlaying());
		clock.pause();
	}	
		
	@Test
	void testFlagFall() {
		final ClockSettings settings = new FakeSettings(2);
		final FakeScheduler scheduler = new FakeScheduler();
		
		final Clock clock = new FakeClock(settings, scheduler);
		clock.withStartingColor(Color.WHITE);
		final List<Color> winner = new ArrayList<>();
		clock.addListener(s -> winner.add(s.winner()));

		assertTrue(clock.tap());
		scheduler.sleep(2100);
		// BLACK should have won
		assertEquals(1,winner.size());
		assertEquals(Color.BLACK, winner.get(0));
		
		assertTrue(clock.isPaused());
		assertFalse(clock.tap());
//		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
//		assertTrue(clock.getRemaining(Color.WHITE) <= 0);
//		assertTrue(clock.isPaused());
//		// Tap should not work anymore
//		assertFalse(clock.tap());
//		assertTrue(clock.isPaused());
//		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
//		assertTrue(clock.getRemaining(Color.WHITE) <= 0);
//		// Pause should not work anymore
//		assertFalse(clock.pause());
	}

//	void testFlagFall() {
//		Thread.sleep(2200);
//		// WHITE flag should be down
//		assertEquals(1,flagFallColor.size());
//		assertEquals(Color.WHITE,flagFallColor.get(0));
//		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
//		assertTrue(clock.getRemaining(Color.WHITE)<=0);
//		assertTrue(clock.isPaused());
//		// Tap should not work anymore
//		assertFalse(clock.tap());
//		assertTrue(clock.isPaused());
//		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
//		assertTrue(clock.getRemaining(Color.WHITE)<=0);
//		// Pause should not work anymore
//		assertFalse(clock.pause());
//	}

}
