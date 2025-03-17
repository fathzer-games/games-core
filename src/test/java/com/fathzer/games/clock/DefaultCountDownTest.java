package com.fathzer.games.clock;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fathzer.games.clock.timeutils.FakeClock;

class DefaultCountDownTest {
	static final FakeClock CLOCK = new FakeClock();

	static class FakeTimedCountDown extends DefaultCountDown {
		FakeTimedCountDown(ClockSettings settings) {
			super(settings);
		}

		@Override
		protected long now() {
			return CLOCK.now();
		}
		
		protected long superNow() {
			return super.now();
		}
	}

	@Test
	void test() {
		CountDown state = new FakeTimedCountDown(new ClockSettings(2).withIncrement(1, 2, true));
		assertTrue(state.isPaused());
		assertEquals(2000, state.getRemainingTime());
		
		// Test remaining time does not change when it's paused
		CLOCK.add(500);
		assertEquals(2000, state.getRemainingTime());
		
		// Test move done fails if count down is paused
		final CountDown cd = state;
		assertThrows(IllegalStateException.class, cd::moveDone);
		// Test pause does nothing
		state.pause();
		assertEquals(2000, state.getRemainingTime());
		assertTrue(state.isPaused());
		
		// Start the count down
		assertEquals(2000, state.start());
		assertFalse(state.isPaused());
		CLOCK.add(500);
		// Ensure a second start fails
		final CountDown cd2 = state;
		assertThrows(IllegalStateException.class, cd2::start);
		assertEquals(1500, state.getRemainingTime());
		CLOCK.add(500);
		// Play a move
		state = state.moveDone();
		assertEquals(1000, state.getRemainingTime());
		assertTrue(state.isPaused());
		CLOCK.add(100);
		assertEquals(1000, state.start());
		CLOCK.add(500);
		// Play a second move => increment time
		state = state.moveDone();
		assertEquals(1500, state.getRemainingTime());
	}
	
	@Test
	void testBronstein() {
		// Let's try with Bronstein delay
		CountDown cd = new FakeTimedCountDown(new ClockSettings(3).withIncrement(1, 2, false));
		cd.start();
		CLOCK.add(100);
		cd = cd.moveDone();
		assertEquals(2900, cd.getRemainingTime());
		CLOCK.add(100);
		cd.start();
		// Ensure pause with no move does not break things
		CLOCK.add(100);
		assertEquals(2800, cd.pause());
		cd.start();
		CLOCK.add(100);
		cd = cd.moveDone();
		assertEquals(3000, cd.getRemainingTime());
		
		// Test when the increment is less than time spent to play moves
		cd = new FakeTimedCountDown(new ClockSettings(3).withIncrement(1, 1, false));
		cd.start();
		CLOCK.add(1500);
		cd = cd.moveDone();
		assertEquals(2500, cd.getRemainingTime());
		cd.start();
		CLOCK.add(500);
		cd = cd.moveDone();
		assertEquals(2500, cd.getRemainingTime());
	}

	@Test
	void testNextPhase() {
		CountDown cd = new FakeTimedCountDown(new ClockSettings(3).withNext(2, Integer.MAX_VALUE, new ClockSettings(4).withIncrement(1, 1, true)));
		assertEquals(2, cd.getRemainingMovesBeforeNext());
		assertEquals(3, cd.getCurrentSettings().getInitialTime());
		cd.start();
		CLOCK.add(500);
		cd = cd.moveDone();
		assertEquals(2500, cd.getRemainingTime());
		assertEquals(1, cd.getRemainingMovesBeforeNext());
		assertEquals(3, cd.getCurrentSettings().getInitialTime());
		cd.start();
		CLOCK.add(1500);
		cd = cd.moveDone();
		assertEquals(4, cd.getCurrentSettings().getInitialTime());
		assertEquals(0, cd.getRemainingMovesBeforeNext());
		assertEquals(5000, cd.getRemainingTime());
		cd.start();
		CLOCK.add(500);
		cd = cd.moveDone();
		assertEquals(5500, cd.getRemainingTime());
	}
	
	@Test
	void testDefaultClock() {
		FakeTimedCountDown cd = new FakeTimedCountDown(new ClockSettings(2));
		assertTrue(Math.abs(cd.superNow()-System.currentTimeMillis())<5);
	}
}
