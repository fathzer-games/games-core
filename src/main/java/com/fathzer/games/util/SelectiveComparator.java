package com.fathzer.games.util;

import java.util.Comparator;
import java.util.function.Predicate;

/** A comparator combined with a Predicate that returns false if an element should not be sorted.
 * <br>It allows to implement the partial sort in {@link MoveList}
 * @param <T> The type of elements
 */
public interface SelectiveComparator<T> extends Comparator<T>, Predicate<T> {

	/** Checks if an element should be sorted or not.
	 * @param t The element to test
	 * @return true if element should be sorted, false if not.<br>The default implementation returns true (all elements should be sorted).
	 */
	@Override
	default boolean test(T t) {
		return true;
	}
}
