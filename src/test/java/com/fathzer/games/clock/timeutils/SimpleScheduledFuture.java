package com.fathzer.games.clock.timeutils;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

class SimpleScheduledFuture implements ScheduledFuture<Void> {
	private Runnable runnable;
	private long at;
	private AtomicBoolean cancelled = new AtomicBoolean(false);
	private AtomicBoolean done = new AtomicBoolean(false);
	
	SimpleScheduledFuture(Runnable runnable, long at) {
		this.runnable = runnable;
		this.at = at;
	}
	
	@Override
	public long getDelay(TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(Delayed o) {
		if (o instanceof SimpleScheduledFuture) {
			final long gap = at - ((SimpleScheduledFuture)o).at;
			if (gap==0) {
				return 0;
			} else {
				return gap<0 ? -1 : 1;
			}
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (!done.get()) {
			cancelled.set(true);
			return true;
		}
		return false;
	}

	@Override
	public boolean isCancelled() {
		return cancelled.get();
	}

	@Override
	public boolean isDone() {
		return done.get();
	}

	@Override
	public Void get() throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}

	void run() {
		if (!cancelled.get() && done.compareAndSet(false, true)) {
			runnable.run();
		}
	}
	
	long at() {
		return at;
	}

	@Override
	public String toString() {
		return "SimpleScheduledFuture [runnable=" + runnable + ", at=" + at + "]";
	}
}
