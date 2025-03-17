package com.fathzer.games.util;

import java.util.Collections;
import java.util.List;

/** A utility class to deal with ordered lists.
 */
public final class SortedUtils {
	private SortedUtils() {
		super();
	}
	
	/** Insert an element in an sorted list.
	 * @param <T> The type of the elements
	 * @param list The list where to insert the new element (The list should be sorted in ascending (natural) order).
	 * @param element The new element.
	 */
	public static <T extends Comparable<T>> void insert(List<T> list, T element) {
	    int index = Collections.binarySearch(list, element);
	    if (index < 0) {
	        index = -index - 1;
	    }
	    list.add(index, element);
	}
}
