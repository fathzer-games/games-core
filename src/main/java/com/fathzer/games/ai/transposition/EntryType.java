package com.fathzer.games.ai.transposition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** The type of a <a href="https://en.wikipedia.org/wiki/Transposition_table">transposition table</a> entry. 
 */
public enum EntryType {
	/** Entry is invalid (typically, not yet assigned) */
	INVALID, 
	/** Entry has an exact value */
	EXACT, 
	/** Entry has a lower bound value */
	LOWER_BOUND, 
	/** Entry has an upper bound value */
	UPPER_BOUND;
	
	/** A list of all entry types
	 * <br>This list is immutable and can be used as a constant.
	 * <br>It can also be used as a replacement for the standard {@link #values()} method that allocates a new array every time it is called.
	 */
	public static final List<EntryType> ALL = Collections.unmodifiableList(Arrays.asList(EntryType.values()));
}
