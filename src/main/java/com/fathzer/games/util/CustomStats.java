package com.fathzer.games.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/** A class to create custom statistics.
 * <br>For instance, you can use the {@link #increment(String)} in a class to count the number of instances created.
 * <br><b>Be aware that doing such a thing can result in an important performance penalty</b>.
 * Remove, or at least, call {@link CustomStats#off()} in production code.
 * <br><b>Warning:</b> Only {@link #increment(String)} and {@link #increment(String, long)} are thread safe.
 */
public class CustomStats {
	private boolean on;
	private final Map<String, AtomicLong> map;
	
	public CustomStats() {
		map = new HashMap<>();
	}
	
	/** Enables the statistics.
	 * <br>Statistics are deactivated by default
	 * @return The previous activation state
	 */
	public boolean on() {
		if (on) {
			return true;
		}
		on = true;
		return false;
	}
	
	/** Disables the statistics.
	 * <br>Statistics are deactivated by default
	 * @return The previous activation state
	 */
	public boolean off() {
		if (!on) {
			return false;
		}
		on = false;
		return true;
	}
	
	/** Checks whether the statistivs are enabled.
	 * @return true if statistics are enabled
	 */
	public boolean isOn() {
		return on;
	}

	/** Increments a statistics counter.
	 * @param counter The name of the counter to increment
	 */
	public void increment(String counter) {
		if (on) {
			map.computeIfAbsent(counter, k->new AtomicLong()).incrementAndGet();
		}
	}

	/** Increments a statistics counter.
	 * @param counter The name of the counter to increment
	 */
	public void increment(String counter, long count) {
		if (on) {
			map.computeIfAbsent(counter, k->new AtomicLong()).addAndGet(count);
		}
	}

	/** Clears all counters.
	 */
	public void clear() {
		map.clear();
	}

	/** Gets a counter.
	 * @param counter The name of the counter
	 * @return The counter's value
	 */
	public long get(String counter) {
		final AtomicLong count = map.get(counter);
		return count==null ? 0 : count.get();
	}

	/** Clears a counter.
	 * @param counter The name of the counter to clear
	 * @return The counter's value before its removal
	 */
	public long clear(String counter) {
		final AtomicLong count = map.remove(counter);
		return count==null ? 0 : count.get();
	}
	
	/** Gets the set of counter's.
	 * @return A set of strings.
	 * <br><b>Warning:</b> Removing or adding counters while iterating on the returned set may throw exceptions.
	 */
	public Set<String> getCounters() {
		return map.keySet();
	}
	
	/** Returns a string representation of the counters.
	 * @return a String
	 */
	@Override
	public String toString() {
		return map.toString();
	}
}
