package com.fathzer.games.clock.timeutils;

import java.util.concurrent.atomic.AtomicLong;

public class FakeClock {
	private AtomicLong currentTime = new AtomicLong();
	
	public long getTime() {
		return currentTime.get();
	}
	
	public void add(long timeMs) {
		currentTime.addAndGet(timeMs);
	}
	
}