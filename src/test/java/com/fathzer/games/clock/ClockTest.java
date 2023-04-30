package com.fathzer.games.clock;

import static org.junit.jupiter.api.Assertions.*;
import static com.fathzer.games.clock.DefaultCountDownTest.*;
import static com.fathzer.games.clock.ClockState.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

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
	
	private static final class ClockListener implements Consumer<ClockEvent> {
		ClockEvent e;
		@Override
		public void accept(ClockEvent e) {
			this.e = e;
		}
	}
	
	private void assertClockEvent(ClockListener listener, ClockState expectedPrevious, ClockState expectedNew) {
		assertEquals(expectedPrevious, listener.e.getPreviousState());
		assertEquals(expectedNew, listener.e.getNewState());
		listener.e = null;
	}

	@Test
	void test() throws InterruptedException {
		final ClockSettings settings = new FakeSettings(2).withIncrement(1, 1, true);
		Clock clock = new Clock(settings);
		final ClockListener listener = new ClockListener();
		clock.addClockListener(listener);
		assertEquals(CREATED, clock.getState());
		assertEquals(Color.WHITE, clock.getPlaying());
		assertEquals(2000, clock.getRemaining(Color.WHITE));
		assertEquals(2000, clock.getRemaining(Color.BLACK));
		
		// Pause does nothing
		assertTrue(clock.pause());
		assertNull(listener.e);
		assertEquals(CREATED, clock.getState());
		
		// Clock starts => White is playing
		clock.tap();
		assertClockEvent(listener, CREATED, COUNTING);
		assertThrows(IllegalStateException.class, ()->clock.withStartingColor(Color.WHITE));
		assertEquals(COUNTING, clock.getState());
		assertEquals(Color.WHITE, clock.getPlaying());
		CLOCK.add(1000);
		assertEquals(2000, clock.getRemaining(Color.BLACK));
		// White tap the clock => Black is playing
		clock.tap();
		assertClockEvent(listener, COUNTING, COUNTING);
		assertEquals(Color.BLACK, clock.getPlaying());
		long whiteRemaining = clock.getRemaining(Color.WHITE);
		assertEquals(2000, whiteRemaining, "Time remaining for white should be near 10000 but is "+whiteRemaining);
		// Pause the clock
		clock.pause();
		assertEquals(PAUSED, clock.getState());
		assertClockEvent(listener, COUNTING, PAUSED);
		assertEquals(Color.BLACK, clock.getPlaying());
		long blackRemaining = clock.getRemaining(Color.BLACK);
		CLOCK.add(1000);
		assertEquals(blackRemaining, clock.getRemaining(Color.BLACK));
		assertEquals(whiteRemaining, clock.getRemaining(Color.WHITE));
		// Restart clock
		clock.tap();
		assertClockEvent(listener, PAUSED, COUNTING);
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
		clock.addStatusListener(s -> winner.add(s.winner()));
		final ClockListener listener = new ClockListener();
		clock.addClockListener(listener);

		assertTrue(clock.tap());
		assertEquals(clock, listener.e.getClock());
		assertClockEvent(listener, CREATED, COUNTING);
		scheduler.sleep(2100);
		// WHITE should have won
		assertEquals(1,winner.size());
		assertEquals(Color.WHITE, winner.get(0));
		assertClockEvent(listener, COUNTING, ENDED);
		
		assertEquals(ENDED, clock.getState());
		assertFalse(clock.pause());
		assertFalse(clock.tap());
	}
}
