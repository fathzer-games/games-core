package com.fathzer.games.clock.timeutils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FakeScheduler {
	ScheduledExecutorService executor;
	SortedSet<SimpleScheduledFuture> tasks;
	FakeClock clock;
	
	public FakeScheduler() {
		this.clock = new FakeClock();
		this.tasks = new TreeSet<>();
		executor = mock(ScheduledExecutorService.class);
		when(executor.schedule((Runnable)any(), anyLong(), any())).thenAnswer(i -> {
			return schedule(i.getArgument(0), i.getArgument(1), i.getArgument(2));
		});
	}

	private SimpleScheduledFuture schedule(Runnable r, long delay, TimeUnit unit) {
		if (delay<0) {
			throw new IllegalArgumentException();
		}
		final SimpleScheduledFuture result = new SimpleScheduledFuture(r, clock.now() + unit.toMillis(delay));
		tasks.add(result);
		return result;
	}

	public ScheduledExecutorService get() {
		return executor;
	}
	
	public void sleep(long ms) {
		while (!tasks.isEmpty()) {
			final SimpleScheduledFuture first = tasks.first();
			long remaining = first.at()-clock.now();
			if (remaining<=ms) {
				tasks.remove(first);
				clock.add(remaining);
				ms = ms - remaining;
				first.run();
			} else {
				break;
			}
		}
		clock.add(ms);
	}
}
