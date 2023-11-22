package com.fathzer.games.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/** A utility class to create custom statistics.
 * <br>For instance, you can use the {@link #increment(String)} in a class to count the number of instances created.
 * <br><b>Be aware that doing such a thing can result in an important performance penalty</b>.
 * Remove, or at least, call {@link CustomStats#off()} in production code. 
 */
public abstract class CustomStats {
	private static boolean on = false;
	private static final Map<String, AtomicLong> MAP = new HashMap<>();
	
	private CustomStats() {
		// Prevents subclassing
	}
	
	/** Enables the statistics.
	 * <br>Statistics are deactivated by default
	 * @return The previous activation state
	 */
	public static boolean on() {
		if (on) {
			return true;
		}
		final boolean old = on;
		on = true;
		return old;
	}
	
	/** Disables the statistics.
	 * <br>Statistics are deactivated by default
	 * @return The previous activation state
	 */
	public static boolean off() {
		if (!on) {
			return false;
		}
		final boolean old = on;
		on = true;
		return old;
	}
	
	/** Checks whether the statistivs are enabled.
	 * @return true if statistics are enabled
	 */
	public static boolean isOn() {
		return on;
	}

	/** Increments a statistics counter.
	 * @param counter The name of the counter to increment
	 */
	public static void increment(String counter) {
		if (on) {
			MAP.computeIfAbsent(counter, k->new AtomicLong()).incrementAndGet();
		}
	}
	
	/** Clears all counters.
	 */
	public static void clear() {
		MAP.clear();
	}

	/** Gets a counter.
	 * @param counter The name of the counter
	 * @return The counter's value
	 */
	public static long get(String counter) {
		final AtomicLong count = MAP.get(counter);
		return count==null ? 0 : count.get();
	}

	/** Clears a counter.
	 * @param counter The name of the counter to clear
	 * @return The counter's value before its removal
	 */
	public static long clear(String counter) {
		final AtomicLong count = MAP.remove(counter);
		return count==null ? 0 : count.get();
	}
	
	/** Gets the set of counter's.
	 * @return A set of strings.
	 * <br><b>Warning:</b> Removing or adding counters while iterating on the returned set may throw exceptions.
	 */
	public static Set<String> getCounters() {
		return MAP.keySet();
	}
	
	/** Returns a string representation of the counters.
	 * @return a String
	 */
	public static String asString() {
		return MAP.toString();
	}
}
