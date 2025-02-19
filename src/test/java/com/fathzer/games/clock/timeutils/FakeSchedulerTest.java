package com.fathzer.games.clock.timeutils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/** A class to test the FakeScheduler class test.
 * <br>It should seem strange to test a test class, but FakeScheduler is quite complex, and I'm not very confident ...
 */
class FakeSchedulerTest {
	
	private static class MyTask {
		String id;
		long at;
		
		public MyTask(String id, long at) {
			this.id = id;
			this.at = at;
		}

		@Override
		public String toString() {
			return "[id=" + id + ", at=" + at + "]";
		}
	}

	@Test
	void test() {
		final FakeScheduler fakeScheduler = new FakeScheduler();
		fakeScheduler.sleep(1000); // To be sure it work if clock is already used
		// We will schedule 3 tasks in reverse time order, then cancel one and verify they are executed in the right order.
		final List<MyTask> results = new ArrayList<>();
		fakeScheduler.get().schedule(()-> {results.add(new MyTask("1",fakeScheduler.clock.now()));}, 3, TimeUnit.SECONDS);
		final ScheduledFuture<?> toBeCancelled = fakeScheduler.get().schedule(()->{results.add(new MyTask("2",fakeScheduler.clock.now()));}, 2, TimeUnit.SECONDS);
		fakeScheduler.get().schedule(()->{results.add(new MyTask("3",fakeScheduler.clock.now()));}, 1, TimeUnit.SECONDS);
		fakeScheduler.sleep(500);
		assertTrue(toBeCancelled.cancel(false));
		
		// Adds a new Task that should not be executed
		fakeScheduler.get().schedule(()->{results.add(new MyTask("4",fakeScheduler.clock.now()));}, 10, TimeUnit.SECONDS);
		
		// Wait for task to be executed
		fakeScheduler.sleep(3000);
		assertEquals(2, results.size());
		assertEquals("3", results.get(0).id);
		assertEquals(2000, results.get(0).at);
		assertEquals("1", results.get(1).id);
		assertEquals(4000, results.get(1).at);
		results.clear();
		
		// Verify last task runs
		fakeScheduler.sleep(11000);
		assertEquals(1, results.size());
		assertEquals("4", results.get(0).id);
		assertEquals(11500, results.get(0).at);
	}

}
